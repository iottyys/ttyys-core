# -*-coding:utf-8-*-
import algo

import sys
import glob
import os
import asyncio

from avro import protocol

from rpcserver.server import SocketServer, Protocol


def path(relative):
    base = getattr(sys, '_MEIPASS', os.path.dirname(os.path.abspath(__file__)))
    return os.path.join(base, relative)


def load_avro_services():
    avpr = glob.glob(os.path.join(path(os.path.dirname(__file__)), 'rpcserver', 'avro', 'proto', '*.avpr'))
    return [protocol.parse(open(proto).read()) for proto in avpr]


if __name__ == '__main__':
    # import rpcserver.http_server as http
    # server = http.HTTPServer(('localhost', 22222), http.MailHandler)
    # server.allow_reuse_address = True
    # server.serve_forever()
    # import rpcserver.async_server as server
    # from rpcserver.async_server import main
    # server.protocols = load_services()
    # asyncio.run(main())
    server = SocketServer(host='0.0.0.0', port=22222, protocol=Protocol.Avro)
    server.register_service()
    server.register_avro_protocols(load_avro_services())
    server.run()
