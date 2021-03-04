#!/usr/bin/env /usr/bin/python3
# -*-coding:utf-8-*-

from socketserver import ForkingTCPServer


class JieBaHandler(ForkingTCPServer):
    def handle(self):
        print('Got connection from', self.client_address)
        while True:
            msg = self.request.recv(8192)
            if not msg:
                break
            self.request.send(msg)


if __name__ == '__main__':
    serv = ForkingTCPServer(('127.0.0.1', 20000), JieBaHandler)
    print("启动socketserver服务器！")
    serv.serve_forever()
