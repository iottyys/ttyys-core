# -*-coding:utf-8-*-

import psutil
import time
from threading import Thread
import socketserver
from socketserver import TCPServer, ThreadingMixIn
from typing import Tuple
from enum import Enum, unique

from rpcserver.avro.proxy import AvroProxyFactory
from rpcserver.logger import logger
from rpcserver.protobuf import handler as protobuf_handler
from rpcserver.avro import handler as avro_handler


@unique
class Protocol(Enum):
    Protobuf = 0
    Avro = 1


def process_check(pid, server):
    while 1:
        try:
            psutil.Process(pid)
            time.sleep(1)
        except psutil.NoSuchProcess:
            break
    server.shutdown()


class SocketServer:
    def __init__(self, port, host='0.0.0.0', protocol=Protocol.Avro):
        self.host = host
        self.port = port
        self.serviceMap = {}
        self.protocols = []
        self.avro_proxy_factory = None
        self.proto = protocol

    def register_service(self, service=None) -> None:
        if service is None:
            return
        self.serviceMap[service.GetDescriptor().full_name] = service

    def register_avro_protocols(self, protocols) -> None:
        self.protocols = protocols
        self.avro_proxy_factory = AvroProxyFactory()
        for protocol in protocols:
            self.avro_proxy_factory.load(protocol)

    def run(self, pid) -> None:
        logger.info('starting server on host: %s - port: %d' % (self.host, self.port))
        handler = avro_handler.RequestHandler
        if self.proto == Protocol.Protobuf:
            handler = protobuf_handler.RequestHandler
        server = None
        try:
            server = ThreadingTCPServer((self.host, self.port), handler, self)
            if pid is not None:
                Thread(target=process_check, args=(pid, server), daemon=True).start()
            server.serve_forever()
        except KeyboardInterrupt:
            if server is not None:
                server.shutdown()


class ThreadingTCPServer(ThreadingMixIn, TCPServer):
    socketserver.allow_reuse_address = True

    def __init__(self, server_address, handler, server):
        socketserver.TCPServer.__init__(self, server_address, handler)
        self.server = server

    def finish_request(self, request: bytes,
                       client_address: Tuple[str, int]) -> None:
        self.RequestHandlerClass(request, client_address, self, self.server)
