
# from rpcserver import server
# import serv
#
#
# server = server.ProtobufSocketServer(8888)
# server.register_service(serv.GameMessage())
# print("serving...")
# server.run()

import os
from avro import ipc, protocol

import rpcserver.avro as test

from rpcserver.avro.transceiver import SocketTransceiver

PROTOCOL = protocol.parse(open(os.path.dirname(test.__file__)
                               + os.path.sep + 'proto'
                               + os.path.sep + 'test.avpr').read())

client = SocketTransceiver("localhost", 22222)
requestor = ipc.Requestor(PROTOCOL, client)
message = dict()
message['to'] = 'a'
message['from'] = 'b'
message['body'] = 'c'

params = dict()
params['message'] = message
resp = requestor.request('send', params)
print(resp)

requestor.request('send', params)
