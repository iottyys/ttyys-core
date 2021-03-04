package io.ttyys.data;

import com.google.protobuf.BlockingRpcChannel;
import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;
import io.ttyys.data.rpcclient.ClientChannel;
import io.ttyys.data.rpcclient.ClientRPCController;
import io.ttyys.data.rpcclient.GameMessage;
import io.ttyys.data.rpcclient.RPCClientFactory;

public class Test {
    public static void main(String[] args) throws ServiceException {
        RPCClientFactory clientFactory = RPCClientFactory.newRPCClientFactory("localhost", 8888);
        BlockingRpcChannel channel = ClientChannel.defaultChannel(clientFactory);
        GameMessage.IEchoService.BlockingInterface service = GameMessage.IEchoService.newBlockingStub(channel);
        RpcController controller = new ClientRPCController();
        GameMessage.ResponseMessage responseMessage = service.echo(controller, GameMessage.RequestMessage.newBuilder().setMsg("test test").build());
        System.out.println(responseMessage);
    }
}

