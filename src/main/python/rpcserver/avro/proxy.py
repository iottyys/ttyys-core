# -*-coding:utf-8-*-

import re
import importlib

from avro import schema


class AvroProxyFactory(object):
    def __init__(self):
        self.proxies = {}

    def proxy(self, protocol):
        proxy = self.proxies.get(protocol.md5)
        if proxy is None:
            proxy = self.load(protocol)
        return proxy

    def load(self, protocol):
        module_name = protocol.fullname.lower()
        handler = importlib.import_module(module_name)
        p = re.compile(r'([a-z]|\d)([A-Z])')
        func_names = [re.sub(p, r'\1_\2', func_name).lower() for func_name in protocol.messages.keys()]
        proxy = AvroProxy(handler, func_names)
        self.proxies[protocol.md5] = proxy
        return proxy


class AvroProxy(object):
    def __init__(self, handler, func_names):
        self.handler = handler
        self.processors = {}
        for func_name in func_names:
            if not hasattr(handler, func_name):
                raise schema.AvroException('no func. could not create proxy for func: %s', func_name)
            else:
                func = getattr(handler, func_name)
                if not hasattr(func, '__call__'):
                    raise schema.AvroException('not func. could not create proxy for func: %s', func_name)
                self.processors[func_name] = func

    def invoke(self, func_name, params):
        processor = self.processors.get(func_name)
        if processor is None:
            raise schema.AvroException('not found func: %s', func_name)
        return processor(params)
