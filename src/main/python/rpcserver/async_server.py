

from struct import Struct
import asyncio

from avro import ipc, schema

from rpcserver.avro.ipc import read_framed_message_netty, DispatcherResponder
from rpcserver.logger import logger

protocols = []


BIG_ENDIAN_INT_STRUCT = Struct('!I')
BUFFER_HEADER_LENGTH = 4
BUFFER_SIZE = 8192


class ConnectionClosedException(schema.AvroException):
    pass


#  todo complete it!
async def handle_echo(reader, writer):
    logger.debug('client connected...')
    while True:
        read = await reader.read(4)
        serial = BIG_ENDIAN_INT_STRUCT.unpack(read)[0]
        read = await reader.read(4)
        packs = BIG_ENDIAN_INT_STRUCT.unpack(read)[0]
        request = await read_framed_message_netty(reader, packs)
        logger.debug('receiving buffer, serial: %d; data packs: %d; total size: %d' % (serial, packs, len(request)))
        response = DispatcherResponder(protocols).respond(request)
        writer.write(BIG_ENDIAN_INT_STRUCT.pack(serial))
        writer.write(BIG_ENDIAN_INT_STRUCT.pack(1))
        response_writer = ipc.FramedWriter(writer)
        response_writer.write_framed_message(response)
        writer.drain()


async def main():
    server = await asyncio.start_server(handle_echo, 'localhost', 22222)
    async with server:
        await server.serve_forever()
