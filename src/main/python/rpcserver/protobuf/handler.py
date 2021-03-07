# -*-coding:utf-8-*-

from socketserver import StreamRequestHandler

from rpcserver.logger import logger
from rpcserver import rpc_pb2 as rpc_pb
from rpcserver import controller
from rpcserver.protobuf import error


def handle_error(e):
    msg = "%d : %s" % (e.rpc_error_code, e.message)
    logger.error(msg)

    response = rpc_pb.Response()
    response.error_reason = e.rpc_error_code
    response.error = e.message
    return response


class Callback:
    def __init__(self):
        self.invoked = False
        self.response = None

    def run(self, response):
        self.response = response
        self.invoked = True


class RequestHandler(StreamRequestHandler):
    def __init__(self, request, client_address, server, socket_server):
        self.socket_server = socket_server
        StreamRequestHandler.__init__(self, request, client_address, server)

    def handle(self) -> None:
        logger.debug('receiving request...')
        recv = self.rfile.read()

        try:
            request, service, method, proto_request = self.validate(recv)

            logger.debug('Calling service %s' % service)
            logger.debug('Calling method %s' % method)

            handler = controller.DispatcherController()
            callback = Callback()
            try:
                service.CallMethod(method, handler, proto_request, callback)
            except Exception as e:
                raise error.RpcError(str(e))

            response = rpc_pb.Response()
            if callback.response:
                response.callback = True
                response.response_proto = callback.response.SerializeToString()
            else:
                response.callback = callback.invoked

            if handler.Failed():
                response.error = handler.ErrorText()
                response.error_reason = rpc_pb.RPC_FAILED

            logger.debug("Response to return to client \n %s" % response)
            self.wfile.write(response.SerializeToString())
        except (error.BadRequestDataError,
                error.ServiceNotFoundError,
                error.MethodNotFoundError,
                error.BadRequestProtoError) as e:
            err_response = handle_error(e)
            self.wfile.write(err_response.SerializeToString())

    def validate(self, recv):
        try:
            request = rpc_pb.Request()
            request.MergeFromString(recv)
            # request = self.parseServiceRequest(input)
        except Exception as e:
            raise error.BadRequestDataError("Invalid request from \
                                            client (decodeError): " + str(e))

        if not request.IsInitialized():
            raise error.BadRequestDataError("Client request is missing \
                                             mandatory fields")
        logger.debug('Request = %s' % request)

        service = self.socket_server.serviceMap.get(request.service_name)
        if service is None:
            msg = "Could not find service '%s'" % request.service_name
            raise error.ServiceNotFoundError(msg)
        method = service.DESCRIPTOR.FindMethodByName(request.method_name)
        if method is None:
            msg = "Could not find method '%s' in service '%s'" \
                  % (request.method_name, service.DESCRIPTOR.name)
            raise error.MethodNotFoundError(msg)

        try:
            proto_request = service.GetRequestClass(method)()
            proto_request.ParseFromString(request.request_proto)
        except Exception as e:
            raise error.BadRequestProtoError(str(e))

        if not proto_request.IsInitialized():
            raise error.BadRequestProtoError('Invalid protocol request \
                                              from client')

        return request, service, method, proto_request
