package io.ttyys.data.rpcclient;

import com.google.protobuf.MessageLite;

import java.io.Closeable;
import java.io.IOException;
import java.net.UnknownHostException;

public abstract class RPCClientFactory {
    public static RPCClientFactory newRPCClientFactory(String host, int port) {
        return new DefaultRPCClientFactory(host, port);
    }

    public abstract Connection createConnection() throws UnknownHostException, IOException;

    public interface Connection extends Closeable {
        void sendProtoMessage(MessageLite message) throws IOException;
        void receiveProtoMessage(MessageLite.Builder messageBuilder) throws IOException;
        void close() throws IOException;
        boolean isClosed();
    }
}
