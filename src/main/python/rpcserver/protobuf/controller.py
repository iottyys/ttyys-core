# -*-coding:utf-8-*-

import google.protobuf.service as service


class DispatcherController(service.RpcController):
    def __init__(self):
        self._fail = False
        self._error = None
        self.reason = None

    def Reset(self):
        self._fail = False
        self._error = None
        self.reason = None

    def Failed(self):
        return self._fail

    def ErrorText(self):
        return self._error

    def StartCancel(self):
        pass

    def SetFailed(self, reason):
        self._fail = True
        self.reason = reason
        self._error = reason

    def IsCanceled(self):
        pass

    def NotifyOnCancel(self, callback):
        pass
