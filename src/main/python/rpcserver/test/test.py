
import os
from avro import ipc
from avro import protocol

PROTOCOL = protocol.parse(open(os.path.dirname(__file__) + os.path.sep + 'test.avpr').read())


class TestResponder(ipc.Responder):
    def __init__(self):
        ipc.Responder.__init__(self, PROTOCOL)

    def invoke(self, msg, req):
        print(PROTOCOL.fullname)
