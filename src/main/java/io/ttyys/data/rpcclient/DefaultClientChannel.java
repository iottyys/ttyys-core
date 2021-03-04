package io.ttyys.data.rpcclient;

import com.google.protobuf.*;
import io.ttyys.data.rpc.SocketRPCProto;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.UnknownHostException;

@Slf4j
public class DefaultClientChannel extends ClientChannel {
    private final RPCClientFactory clientFactory;

    public DefaultClientChannel(RPCClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public Message callBlockingMethod(Descriptors.MethodDescriptor method,
                                      RpcController controller,
                                      Message request,
                                      Message responsePrototype) throws ServiceException {
        ClientRPCController socketController = (ClientRPCController) controller;
        final RPCClientFactory.Connection connection = this.createConnection(socketController);
        try {
            this.sendRpcRequest(method, socketController, request, connection);
            SocketRPCProto.Response rpcResponse = this.receiveRpcResponse(socketController, connection);
            return this.handleRpcResponse(responsePrototype, rpcResponse,
                    socketController);
        } finally {
            this.close(connection);
        }
    }

    private RPCClientFactory.Connection createConnection(ClientRPCController socketController)
            throws ServiceException {
        try {
            return this.clientFactory.createConnection();
        } catch (UnknownHostException e) {
            return handleError(socketController, SocketRPCProto.ErrorReason.UNKNOWN_HOST,
                    "Could not find host: " + e.getMessage(), e);
        } catch (IOException e) {
            return handleError(socketController, SocketRPCProto.ErrorReason.IO_ERROR, String.format(
                    "Error creating connection using factory %s", this.clientFactory), e);
        }
    }

    private void sendRpcRequest(Descriptors.MethodDescriptor method,
                                ClientRPCController socketController, Message request,
                                RPCClientFactory.Connection connection) throws ServiceException {
        if (!request.isInitialized()) {
            handleError(socketController, SocketRPCProto.ErrorReason.INVALID_REQUEST_PROTO,
                    "Request is uninitialized", null);
        }

        SocketRPCProto.Request rpcRequest = SocketRPCProto.Request.newBuilder()
                .setRequestProto(request.toByteString())
                .setServiceName(method.getService().getFullName())
                .setMethodName(method.getName())
                .build();

        // Send request
        try {
            connection.sendProtoMessage(rpcRequest);
        } catch (IOException e) {
            handleError(socketController, SocketRPCProto.ErrorReason.IO_ERROR, String.format(
                    "Error writing over connection %s", connection), e);
        }
    }

    private SocketRPCProto.Response receiveRpcResponse(ClientRPCController socketController,
                                                       RPCClientFactory.Connection connection) throws ServiceException {
        try {
            SocketRPCProto.Response.Builder builder = SocketRPCProto.Response
                    .newBuilder();
            connection.receiveProtoMessage(builder);
            if (!builder.isInitialized()) {
                return handleError(socketController, SocketRPCProto.ErrorReason.BAD_REQUEST_PROTO,
                        "Bad response from server", null);
            }
            return builder.build();
        } catch (IOException e) {
            return handleError(socketController, SocketRPCProto.ErrorReason.IO_ERROR, String.format(
                    "Error reading over connection %s", connection), e);
        }
    }

    private Message handleRpcResponse(Message responsePrototype,
                                      SocketRPCProto.Response rpcResponse,
                                      ClientRPCController socketController)
            throws ServiceException {
        if (rpcResponse.hasError()) {
            return handleError(socketController, rpcResponse.getErrorReason(),
                    rpcResponse.getError(), null);
        }

        if (!rpcResponse.hasResponseProto()) {
            return null;
        }

        try {
            Message.Builder builder = responsePrototype.newBuilderForType()
                    .mergeFrom(rpcResponse.getResponseProto());
            if (!builder.isInitialized()) {
                return handleError(socketController, SocketRPCProto.ErrorReason.BAD_REQUEST_PROTO,
                        "Uninitialized RPC Response Proto", null);
            }
            return builder.build();
        } catch (InvalidProtocolBufferException e) {
            return handleError(socketController, SocketRPCProto.ErrorReason.BAD_REQUEST_PROTO,
                    "Response could be parsed as "
                            + responsePrototype.getClass().getName(), e);
        }
    }

    private void close(RPCClientFactory.Connection connection) {
        try {
            connection.close();
        } catch (IOException e) {
            // don't mind
        }
    }

    private <T> T handleError(ClientRPCController socketController,
                              SocketRPCProto.ErrorReason reason, String msg, Exception e)
            throws ServiceException {
        if (e == null) {
            log.warn(reason + ": " + msg);
        } else {
            log.warn(reason + ": " + msg, e);
        }
        socketController.setFailed(msg, reason);
        throw new ServiceException(msg);
    }
}
