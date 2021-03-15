# -*-coding:utf-8-*-

from socketserver import StreamRequestHandler

from rpcserver.avro.ipc import NettyFramedReader, NettyFramedWriter, DispatcherResponder, ConnectionClosedException
from rpcserver.logger import logger


class RequestHandler(StreamRequestHandler):
    def __init__(self, request, client_address, server, socket_server):
        self.socket_server = socket_server
        self.responder = DispatcherResponder(self.socket_server.protocols, self.socket_server.avro_proxy_factory)
        StreamRequestHandler.__init__(self, request, client_address, server)

    def reset_responder(self):
        self.responder = DispatcherResponder(self.socket_server.protocols, self.socket_server.avro_proxy_factory)

    def handle(self) -> None:
        logger.info('client connected...')
        self.reset_responder()
        try:
            while True:
                request, serial = NettyFramedReader(self.rfile).read_framed_message()
                response = self.responder.respond(request)
                response_writer = NettyFramedWriter(self.wfile)
                response_writer.write_framed_message(response, serial)
        except ConnectionClosedException:
            logger.info('client disconnected...')
            self.finish()
