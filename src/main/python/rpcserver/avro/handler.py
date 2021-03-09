# -*-coding:utf-8-*-

from socketserver import StreamRequestHandler

from avro import ipc

from rpcserver.avro import responder
from rpcserver.logger import logger


class RequestHandler(StreamRequestHandler):
    def __init__(self, request, client_address, server, socket_server):
        self.socket_server = socket_server
        StreamRequestHandler.__init__(self, request, client_address, server)

    def handle(self) -> None:
        logger.debug('receiving request...')
        request_reader = ipc.FramedReader(self.rfile)
        request = request_reader.read_framed_message()
        response = responder.DispatcherResponder(self.socket_server.protocols).respond(request)
        response_writer = ipc.FramedWriter(self.wfile)
        response_writer.write_framed_message(response)
