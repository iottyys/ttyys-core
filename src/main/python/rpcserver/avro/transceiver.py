# -*-coding:utf-8-*-

import io
import socket

from avro import ipc

# todo enhance for avro specification
# note do not use!! it's wrong!
class SocketTransceiver(object):
    def __init__(self, host, port):
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.sock.connect((host, port))
        self.remote_name = self.sock.getsockname()

    def transceive(self, request):
        self.write_framed_message(request)
        result = self.read_framed_message()
        return result

    def read_framed_message(self):
        response_reader = ipc.FramedReader(self.sock.makefile(mode='rb', buffering=-1))
        framed_message = response_reader.read_framed_message()
        self.sock.recv(1024)
        return framed_message

    def write_framed_message(self, message):
        req_body_buffer = ipc.FramedWriter(io.BytesIO())
        req_body_buffer.write_framed_message(message)
        body = req_body_buffer.writer.getvalue()
        self.sock.sendall(body)

    def close(self):
        self.sock.close()
