package io.ttyys.data.rpcclient;

import com.google.protobuf.MessageLite;

import java.io.*;
import java.net.Socket;

final class DefaultConnection implements RPCClientFactory.Connection {
    private final Socket socket;
    private final OutputStream out;
    private final InputStream in;

    public DefaultConnection(Socket socket) throws IOException {
        this.socket = socket;
        try {
            this.out = new BufferedOutputStream(socket.getOutputStream());
            this.in = new BufferedInputStream(socket.getInputStream());
        } catch (IOException e) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
            throw e;
        }
    }

    @Override
    public void sendProtoMessage(MessageLite message) throws IOException {
        message.writeTo(this.out);
        this.out.flush();
        this.socket.shutdownOutput();
    }

    @Override
    public void receiveProtoMessage(MessageLite.Builder messageBuilder) throws IOException {
        messageBuilder.mergeFrom(this.in);
    }

    @Override
    public void close() throws IOException {
        if (!this.socket.isClosed()) {
            this.socket.close();
        }
    }

    @Override
    public boolean isClosed() {
        return this.socket.isClosed();
    }
}
