package io.ttyys.core.rpc.protobuf;

import com.google.protobuf.MessageLite;
import org.springframework.util.StringUtils;

import java.io.Closeable;
import java.io.IOException;
import java.net.UnknownHostException;

public abstract class RPCClientFactory {
    private static final String CHANNEL_CLASS_NAME = RPCClientFactory.class.getPackage().getName()
            + "DefaultClientChannel";
    private static final String CONTROLLER_CLASS_NAME = RPCClientFactory.class.getPackage().getName()
            + "ClientRPCController";

    private static String PROTO_CLASS_NAME;

    public static RPCClientFactory newRPCClientFactory(String host, int port) {
        loadRPCClass();
        return new DefaultRPCClientFactory(host, port);
    }

    public abstract Connection createConnection() throws UnknownHostException, IOException;
    public abstract ClientProxy createProxy();

    public interface Connection extends Closeable {
        void sendProtoMessage(MessageLite message) throws IOException;
        void receiveProtoMessage(MessageLite.Builder messageBuilder) throws IOException;
        void close() throws IOException;
        boolean isClosed();
    }

    private static void loadRPCClass() {
        try {
            if (!StringUtils.hasText(PROTO_CLASS_NAME)) {
                throw new ClassNotFoundException();
            }
            Class.forName(PROTO_CLASS_NAME, false, Thread.currentThread().getContextClassLoader());
            Class.forName(CHANNEL_CLASS_NAME, false, Thread.currentThread().getContextClassLoader());
            Class.forName(CONTROLLER_CLASS_NAME, false, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            // 获取并编译协议文件
            // 获取并设置类名
            // 编译proto文件
            // 编译生成的类文件并注入
        }
    }
}
