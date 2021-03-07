# -*-coding:utf-8-*-

import logging


class NullHandler(logging.Handler):
    def emit(self, record):
        pass


logging.basicConfig(level=logging.DEBUG)
logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)
logger.addHandler(NullHandler())
