# -*-coding:utf-8-*-

import time


def probe(ping):
    t = time.time()
    ret_val = dict()
    ret_val['from'] = ping['from']
    ret_val['to'] = 'unknown'
    ret_val['timestamp'] = t
    ret_val['timing'] = t - ping.timestamp
    ret_val['status'] = 'ok'
