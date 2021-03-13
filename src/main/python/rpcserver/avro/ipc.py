# -*-coding:utf-8-*-

from struct import Struct
import json
import os
import io

from avro import ipc, schema, protocol
from avro.io import BinaryDecoder, BinaryEncoder, DatumReader, DatumWriter, SchemaResolutionException
from avro.ipc import AvroRemoteException

NO_FOUND = protocol.parse('{"namespace": "rpcserver.avro","protocol": "NoFound"}')
BIG_ENDIAN_INT_STRUCT = Struct('!I')
BUFFER_HEADER_LENGTH = BUFFER_PACK_LENGTH = 4
BUFFER_SIZE = 8192


class ConnectionClosedException(schema.AvroException):
    pass


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


def adjust_json(proto):
    types = proto.get("types")
    if types is not None and len(types) == 0:
        del proto['types']
    elif types is not None:
        for t in types:
            fields = t.get('fields')
            if fields is not None and len(fields) == 0:
                del t['fields']
    messages = proto.get("messages")
    if messages is not None and len(messages) == 0:
        del proto['messages']
    elif messages is not None:
        for name, message in messages.items():
            errors = message.get('errors')
            if errors is not None and len(errors) == 0:
                message.pop('errors')


class DispatcherResponder(object):
    def __init__(self, local_protocols, proxy_factory):
        if proxy_factory is None:
            raise schema.AvroException('request could not be invoke if proxy_factory is none. ')
        self._local_protocol = None
        self._proxy_factory = proxy_factory
        self._protocol_cache = {}
        self._local_protocol_cache = {}
        for local_protocol in local_protocols:
            self.set_protocol_cache(local_protocol.md5, local_protocol)
            self.set_local_protocol_cache(local_protocol.md5, local_protocol)

    local_protocol = property(lambda self: self._local_protocol)
    local_protocol_cache = property(lambda self: self._local_protocol_cache)
    protocol_cache = property(lambda self: self._protocol_cache)
    proxy_factory = property(lambda self: self._proxy_factory)

    def invoke(self, local_protocol, local_message, request):
        return self.proxy_factory.proxy(local_protocol).invoke(local_message.name, request)

    def respond(self, call_request):
        buffer_reader = io.BytesIO(call_request)
        buffer_decoder = BinaryDecoder(buffer_reader)
        buffer_writer = io.BytesIO()
        buffer_encoder = BinaryEncoder(buffer_writer)
        error = None
        response_metadata = {}
        try:
            remote_protocol = self.process_handshake(buffer_decoder, buffer_encoder)
            if remote_protocol is None or self.local_protocol is None:
                return buffer_writer.getvalue()

            DatumReader(schema.parse('{"type": "map", "values": "bytes"}')).read(buffer_decoder)
            remote_message_name = buffer_decoder.read_utf8()

            remote_message = remote_protocol.messages.get(remote_message_name)
            if remote_message is None:
                fail_msg = 'Unknown remote message: %s' % remote_message_name
                raise schema.AvroException(fail_msg)
            local_message = self.local_protocol.messages.get(remote_message_name)
            if local_message is None:
                fail_msg = 'Unknown local message: %s' % remote_message_name
                raise schema.AvroException(fail_msg)
            writers_schema = remote_message.request
            readers_schema = local_message.request
            request = self.read_request(writers_schema, readers_schema,
                                        buffer_decoder)

            response = None
            try:
                response = self.invoke(self.local_protocol, local_message, request)
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
        try:
            handshake_response = {}
            handshake_request = DatumReader(_load_request_schema()).read(decoder)
        except SchemaResolutionException:
            if self.local_protocol is None:
                raise AvroRemoteException('no successful handshake, and no necessary protocol')
            # reset reader
            decoder.reader.seek(0, 0)
            return self.local_protocol

        client_hash = handshake_request.get('clientHash')
        client_protocol = handshake_request.get('clientProtocol')
        remote_protocol = self.get_protocol_cache(client_hash)

        # new handshake
        if remote_protocol is None and client_protocol is None:
            handshake_response['match'] = 'NONE'
            handshake_response['serverProtocol'] = str(NO_FOUND)
            handshake_response['serverHash'] = NO_FOUND.md5
            DatumWriter(_load_response_schema()).write(handshake_response, encoder)
            return remote_protocol

        # client request handshake
        if remote_protocol is None and client_protocol is not None:
            # compare with client_protocol and cache_protocol
            self._local_protocol = self.contains(client_protocol)
            if self.local_protocol is None:
                handshake_response['match'] = 'NONE'
                handshake_response['serverProtocol'] = str(NO_FOUND)
                handshake_response['serverHash'] = NO_FOUND.md5
                DatumWriter(_load_response_schema()).write(handshake_response, encoder)
                return remote_protocol
            else:
                remote_protocol = protocol.parse(client_protocol)
                self.set_protocol_cache(client_hash, remote_protocol)
                handshake_response['match'] = 'CLIENT'
                handshake_response['serverProtocol'] = str(self.local_protocol)
                handshake_response['serverHash'] = self.local_protocol.md5
                DatumWriter(_load_response_schema()).write(handshake_response, encoder)
                return remote_protocol

        # success handshake
        if remote_protocol is not None:
            handshake_response['match'] = 'BOTH'

        DatumWriter(_load_response_schema()).write(handshake_response, encoder)
        return remote_protocol

    # noinspection PyMethodMayBeStatic
    def read_request(self, writers_schema, readers_schema, decoder):
        datum_reader = DatumReader(writers_schema, readers_schema)
        return datum_reader.read(decoder)

    # noinspection PyMethodMayBeStatic
    def write_response(self, writers_schema, response_datum, encoder):
        datum_writer = DatumWriter(writers_schema)
        datum_writer.write(response_datum, encoder)

    # noinspection PyMethodMayBeStatic
    def write_error(self, writers_schema, error_exception, encoder):
        datum_writer = DatumWriter(writers_schema)
        datum_writer.write(str(error_exception), encoder)

    def contains(self, client_protocol):
        for proto in self.protocol_cache.values():
            local_json = proto.to_json()
            client_json = json.loads(client_protocol)
            adjust_json(local_json)
            adjust_json(client_json)
            if local_json == client_json:
                return proto
        return None


class NettyFramedReader(object):
    def __init__(self, reader):
        self._reader = reader

    reader = property(lambda self: self._reader)

    def read_framed_message(self):
        serial = self._read_serial()
        packs = self._read_packs()
        message = []
        count = 0
        while packs - count > 0:
            buffer = io.BytesIO()
            buffer_length = self._read_buffer_length()
            if buffer_length == 0:
                return b''.join(message)
            while buffer.tell() < buffer_length:
                chunk = self.reader.read(buffer_length - buffer.tell())
                if chunk == '':
                    raise ConnectionClosedException("Reader read 0 bytes.")
                buffer.write(chunk)
            message.append(buffer.getvalue())
            count += 1
        return b''.join(message), serial

    def _read_serial(self):
        read = self.reader.read(4)
        if read == b'':
            raise ConnectionClosedException("Reader read 0 bytes.")
        return BIG_ENDIAN_INT_STRUCT.unpack(read)[0]

    def _read_packs(self):
        read = self.reader.read(4)
        if read == b'':
            raise ConnectionClosedException("Reader read 0 bytes.")
        return BIG_ENDIAN_INT_STRUCT.unpack(read)[0]

    def _read_buffer_length(self):
        read = self.reader.read(BUFFER_HEADER_LENGTH)
        if read == b'':
            raise ConnectionClosedException("Reader read 0 bytes.")
        return BIG_ENDIAN_INT_STRUCT.unpack(read)[0]


class NettyFramedWriter(object):
    def __init__(self, writer):
        self._writer = writer

    writer = property(lambda self: self._writer)

    def write_framed_message(self, message, serial):
        self._write_serial(serial)
        self._write_packs(1)
        message_length = len(message)
        total_bytes_sent = 0
        while message_length - total_bytes_sent > 0:
            if message_length - total_bytes_sent > BUFFER_SIZE:
                buffer_length = BUFFER_SIZE
            else:
                buffer_length = message_length - total_bytes_sent
            self.write_buffer(message[total_bytes_sent:
                                      (total_bytes_sent + buffer_length)])
            total_bytes_sent += buffer_length
        self.writer.flush()
        # A message is always terminated by a zero-length buffer.
        # note netty trans ignore this
        # self.write_buffer_length(0)

    def _write_serial(self, serial):
        self.writer.write(BIG_ENDIAN_INT_STRUCT.pack(serial))

    def _write_packs(self, packs):
        self.writer.write(BIG_ENDIAN_INT_STRUCT.pack(packs))

    def write_buffer(self, chunk):
        buffer_length = len(chunk)
        self.write_buffer_length(buffer_length)
        self.writer.write(chunk)

    def write_buffer_length(self, n):
        self.writer.write(BIG_ENDIAN_INT_STRUCT.pack(n))


async def read_framed_message_netty(reader, frame_count):
    message = []
    count = 0
    while frame_count - count > 0:
        buffer = io.BytesIO()
        buffer_length = await _read_buffer_length(reader)
        if buffer_length == 0:
            return b''.join(message)
        while buffer.tell() < buffer_length:
            chunk = await reader.read(buffer_length - buffer.tell())
            if chunk == '':
                raise ConnectionClosedException("Reader read 0 bytes.")
            buffer.write(chunk)
        message.append(buffer.getvalue())
        count += 1
    return b''.join(message)


async def _read_buffer_length(reader):
    read = await reader.read(BUFFER_HEADER_LENGTH)
    if read == b'':
        raise ConnectionClosedException("Reader read 0 bytes.")
    return BIG_ENDIAN_INT_STRUCT.unpack(read)[0]
