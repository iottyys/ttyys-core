

import os
from http.server import BaseHTTPRequestHandler

from avro import ipc, protocol, schema


import rpcserver.avro as avro


PROTOCOL = protocol.parse(open(os.path.join(os.path.dirname(avro.__file__), 'proto', 'pingpong.avpr')).read())


class MailResponder(ipc.Responder):
    def __init__(self):
        ipc.Responder.__init__(self, PROTOCOL)

    def Invoke(self, msg, req):
        if msg.name == 'send':
            message = req['message']
            return ("Sent message to " + message['to']
                    + " from " + message['from']
                    + " with body " + message['body'])
        else:
            raise schema.AvroException("unexpected message:", msg.getname())


class MailHandler(BaseHTTPRequestHandler):
    def __init__(self):
        BaseHTTPRequestHandler.__init__(self)
        self.responder = MailResponder()

    def do_POST(self):
        call_request_reader = ipc.FramedReader(self.rfile)
        call_request = call_request_reader.read_framed_message()
        resp_body = self.responder.respond(call_request)
        self.send_response(200)
        self.send_header('Content-Type', 'avro/binary')
        self.end_headers()
        resp_writer = ipc.FramedWriter(self.wfile)
        resp_writer.write_framed_message(resp_body)
