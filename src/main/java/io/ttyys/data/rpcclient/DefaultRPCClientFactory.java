package io.ttyys.data.rpcclient;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.Socket;

public class DefaultRPCClientFactory extends RPCClientFactory {
    private final String host;
    private final int port;
    private final SocketFactory socketFactory;

    public DefaultRPCClientFactory(String host, int port) {
        this.host = host;
        this.port = port;
        this.socketFactory = SocketFactory.getDefault();
    }

    @Override
    public Connection createConnection() throws IOException {
        Socket socket = socketFactory.createSocket(host, port);
        return new DefaultConnection(socket);
    }
}
