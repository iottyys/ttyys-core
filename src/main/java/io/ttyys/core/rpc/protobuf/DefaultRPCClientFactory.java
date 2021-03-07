package io.ttyys.core.rpc.protobuf;

import com.google.protobuf.BlockingRpcChannel;

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

    @Override
    public ClientProxy createProxy() {
        BlockingRpcChannel channel = ClientChannel.defaultChannel(this);
        // 构造控制器
        // 获取具体服务
        //
        return null;
    }
}
