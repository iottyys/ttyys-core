# -*-coding:utf-8-*-

import os
import io
import importlib
import re

from avro import ipc, schema, protocol
from avro.io import BinaryDecoder, BinaryEncoder, DatumReader, DatumWriter
from avro.ipc import AvroRemoteException


NO_FOUND = protocol.parse(open(os.path.dirname(__file__) + os.path.sep + 'no_found.avpr').read())


def _load_request_schema():
    dir_path = os.path.dirname(ipc.__file__)
    rsrc_path = os.path.join(dir_path, 'HandshakeRequest.avsc')
    with open(rsrc_path, 'r') as f:
        return schema.parse(f.read())


def _load_response_schema():
    dir_path = os.path.dirname(ipc.__file__)
    rsrc_path = os.path.join(dir_path, 'HandshakeResponse.avsc')
    with open(rsrc_path, 'r') as f:
        return schema.parse(f.read())


class DispatcherResponder(object):
    def __init__(self, local_protocols):
        self._protocol_cache = {}
        self._local_protocol_cache = {}
        for local_protocol in local_protocols:
            self.set_protocol_cache(local_protocol.md5, local_protocol)
            self.set_local_protocol_cache(local_protocol.md5, local_protocol)

    local_protocol_cache = property(lambda self: self._local_protocol_cache)
    protocol_cache = property(lambda self: self._protocol_cache)

    def invoke(self, local_protocol, local_message, request):
        module_name = local_protocol.fullname.lower()
        func_name = local_message.name
        handler = importlib.import_module(module_name)

        p = re.compile(r'([a-z]|\d)([A-Z])')
        func_name = re.sub(p, r'\1_\2', func_name).lower()
        processor = getattr(handler, func_name)
        return processor(request)

    def respond(self, call_request):
        buffer_reader = io.BytesIO(call_request)
        buffer_decoder = BinaryDecoder(buffer_reader)
        buffer_writer = io.BytesIO()
        buffer_encoder = BinaryEncoder(buffer_writer)
        error = None
        response_metadata = {}

        try:
            remote_protocol, local_protocol = self.process_handshake(buffer_decoder, buffer_encoder)
            if remote_protocol is None or local_protocol is None:
                return buffer_writer.getvalue()

            DatumReader(schema.parse('{"type": "map", "values": "bytes"}')).read(buffer_decoder)
            remote_message_name = buffer_decoder.read_utf8()

            remote_message = remote_protocol.messages.get(remote_message_name)
            if remote_message is None:
                fail_msg = 'Unknown remote message: %s' % remote_message_name
                raise schema.AvroException(fail_msg)
            local_message = local_protocol.messages.get(remote_message_name)
            if local_message is None:
                fail_msg = 'Unknown local message: %s' % remote_message_name
                raise schema.AvroException(fail_msg)
            writers_schema = remote_message.request
            readers_schema = local_message.request
            request = self.read_request(writers_schema, readers_schema,
                                        buffer_decoder)

            response = None
            try:
                response = self.invoke(local_protocol, local_message, request)
            except AvroRemoteException as e:
                error = e
            except Exception as e:
                error = AvroRemoteException(str(e))

            DatumWriter(schema.parse('{"type": "map", "values": "bytes"}')).write(response_metadata, buffer_encoder)
            buffer_encoder.write_boolean(error is not None)
            if error is None:
                writers_schema = local_message.response
                self.write_response(writers_schema, response, buffer_encoder)
            else:
                writers_schema = local_message.errors
                self.write_error(writers_schema, error, buffer_encoder)
        except schema.AvroException as e:
            error = AvroRemoteException(str(e))
            buffer_encoder = BinaryEncoder(io.BytesIO())
            DatumWriter(schema.parse('{"type": "map", "values": "bytes"}')).write(response_metadata, buffer_encoder)
            buffer_encoder.write_boolean(True)
            self.write_error(schema.parse('["string"]'), error, buffer_encoder)
        return buffer_writer.getvalue()

    def set_protocol_cache(self, local_protocol_hash, local_protocol):
        self.protocol_cache[local_protocol_hash] = local_protocol

    def set_local_protocol_cache(self, local_protocol_hash, local_protocol):
        self.local_protocol_cache[local_protocol_hash] = local_protocol

    def get_protocol_cache(self, local_protocol_hash):
        return self.protocol_cache.get(local_protocol_hash)

    def process_handshake(self, decoder, encoder):
        handshake_request = DatumReader(_load_request_schema()).read(decoder)
        handshake_response = {}

        client_hash = handshake_request.get('clientHash')
        client_protocol = handshake_request.get('clientProtocol')
        remote_protocol = self.get_protocol_cache(client_hash)
        if remote_protocol is None and client_protocol is not None:
            remote_protocol = protocol.parse(client_protocol)
            self.set_protocol_cache(client_hash, remote_protocol)

        server_hash = handshake_request.get('serverHash')
        if server_hash in self.protocol_cache:
            if remote_protocol is None:
                handshake_response['match'] = 'NONE'
            else:
                handshake_response['match'] = 'BOTH'
        else:
            if remote_protocol is None:
                handshake_response['match'] = 'NONE'
            else:
                handshake_response['match'] = 'CLIENT'

        local_protocol = NO_FOUND
        local_hash = NO_FOUND.md5
        if remote_protocol is not None:
            for tmp_protocol in self.local_protocol_cache.values():
                if remote_protocol.fullname == tmp_protocol.fullname:
                    local_protocol = tmp_protocol
                    local_hash = tmp_protocol.md5
                    break

        if handshake_response['match'] != 'BOTH':
            handshake_response['serverProtocol'] = str(local_protocol)
            handshake_response['serverHash'] = local_hash

        DatumWriter(_load_response_schema()).write(handshake_response, encoder)
        return remote_protocol, local_protocol

    def read_request(self, writers_schema, readers_schema, decoder):
        datum_reader = DatumReader(writers_schema, readers_schema)
        return datum_reader.read(decoder)

    def write_response(self, writers_schema, response_datum, encoder):
        datum_writer = DatumWriter(writers_schema)
        datum_writer.write(response_datum, encoder)

    def write_error(self, writers_schema, error_exception, encoder):
        datum_writer = DatumWriter(writers_schema)
        datum_writer.write(str(error_exception), encoder)
