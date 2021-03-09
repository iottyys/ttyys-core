# -*-coding:utf-8-*-

import glob
import os

from avro import protocol

from rpcserver.server import SocketServer, Protocol


def load_services():
    avpr = glob.glob(os.path.dirname(__file__) + os.path.sep
                     + 'rpcserver' + os.path.sep
                     + 'avro' + os.path.sep
                     + 'proto' + os.path.sep
                     + '*.avpr')
    return [protocol.parse(open(proto).read()) for proto in avpr]


if __name__ == '__main__':
    server = SocketServer(port=22222, protocol=Protocol.Avro)
    server.register_service()
    server.register_avro_protocols(load_services())
    server.run()
    load_services()
