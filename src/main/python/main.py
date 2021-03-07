# -*-coding:utf-8-*-

from rpcserver.server import ProtobufSocketServer, Protocol

if __name__ == '__main__':
    server = ProtobufSocketServer(port=22222, protocol=Protocol.Protobuf)
    server.register_service()
    server.run()

# def loadServices():
#
