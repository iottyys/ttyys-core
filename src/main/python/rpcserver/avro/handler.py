# -*-coding:utf-8-*-

from socketserver import StreamRequestHandler


class RequestHandler(StreamRequestHandler):
    def __init__(self, request, client_address, server, socket_server):
        self.socket_server = socket_server
        StreamRequestHandler.__init__(self, request, client_address, server)
