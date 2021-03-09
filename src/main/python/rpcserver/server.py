# -*-coding:utf-8-*-

import socketserver
from socketserver import TCPServer, ThreadingMixIn
from typing import Tuple
from enum import Enum, unique

from rpcserver.logger import logger
from rpcserver.protobuf import handler as protobuf_handler
from rpcserver.avro import handler as avro_handler


@unique
class Protocol(Enum):
    Protobuf = 0
    Avro = 1


class SocketServer:
    def __init__(self, port, host='localhost', protocol=Protocol.Avro):
        self.host = host
        self.port = port
        self.serviceMap = {}
        self.protocols = []
        self.proto = protocol

    def register_service(self, service=None) -> None:
        if service is None:
            return
        self.serviceMap[service.GetDescriptor().full_name] = service

    def register_avro_protocols(self, protocols) -> None:
        self.protocols = protocols

    def run(self) -> None:
        logger.info('starting server on host: %s - port: %d' % (self.host, self.port))
        handler = avro_handler.RequestHandler
        if self.proto == Protocol.Protobuf:
            handler = protobuf_handler.RequestHandler
        ThreadingTCPServer((self.host, self.port), handler, self).serve_forever()


class ThreadingTCPServer(ThreadingMixIn, TCPServer):
    socketserver.allow_reuse_address = True

    def __init__(self, server_address, handler, server):
        socketserver.TCPServer.__init__(self, server_address, handler)
        self.server = server

    def finish_request(self, request: bytes,
                       client_address: Tuple[str, int]) -> None:
        self.RequestHandlerClass(request, client_address, self, self.server)
