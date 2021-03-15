# -*-coding:utf-8-*-

from rpcserver.protobuf import rpc_pb2 as rpc_pb


class ProtobufError(Exception):
    def __init__(self, message, rpc_error_code):
        Exception.__init__(self, message)
        self.rpc_error_code = rpc_error_code


class BadRequestDataError(ProtobufError):
    def __init__(self, message):
        super(BadRequestDataError, self).__init__(
            message, rpc_pb.BAD_REQUEST_DATA)


class BadRequestProtoError(ProtobufError):
    def __init__(self, message):
        super(BadRequestProtoError, self).__init__(
            message, rpc_pb.BAD_REQUEST_PROTO)


class ServiceNotFoundError(ProtobufError):
    def __init__(self, message):
        super(ServiceNotFoundError, self).__init__(
            message, rpc_pb.SERVICE_NOT_FOUND)


class MethodNotFoundError(ProtobufError):
    def __init__(self, message):
        super(MethodNotFoundError, self).__init__(
            message, rpc_pb.METHOD_NOT_FOUND)


class RpcError(ProtobufError):
    def __init__(self, message):
        super(RpcError, self).__init__(message, rpc_pb.RPC_ERROR)


class RpcFailed(ProtobufError):
    def __init__(self, message):
        super(RpcFailed, self).__init__(message, rpc_pb.RPC_FAILED)
