package io.ttyys.core.rpc.protobuf;

import com.google.protobuf.MessageLite;
import org.springframework.util.StringUtils;

import java.io.Closeable;
import java.io.IOException;
import java.net.UnknownHostException;

public abstract class RPCClientFactory {
    protected static final String CHANNEL_CLASS_NAME = RPCClientFactory.class.getPackage().getName()
            + "DefaultClientChannel";
    protected static final String CONTROLLER_CLASS_NAME = RPCClientFactory.class.getPackage().getName()
            + "ClientRPCController";

    private static String PROTO_CLASS_NAME;

    public static RPCClientFactory newRPCClientFactory(String host, int port) {
        loadRPCProtoClass();
        return new DefaultRPCClientFactory(host, port);
    }

    public abstract Connection createConnection() throws UnknownHostException, IOException;
    public abstract ClientProxy getProxy(String xxxxxxx);

    public abstract void createProxies(String[] locations) throws Exception;

    public interface Connection extends Closeable {
        void sendProtoMessage(MessageLite message) throws IOException;
        void receiveProtoMessage(MessageLite.Builder messageBuilder) throws IOException;
        void close() throws IOException;
        boolean isClosed();
    }

    private static void loadRPCProtoClass() {
        try {
            if (!StringUtils.hasText(PROTO_CLASS_NAME)) {
                throw new ClassNotFoundException();
            }
            Class.forName(PROTO_CLASS_NAME, false, Thread.currentThread().getContextClassLoader());
            Class.forName(CHANNEL_CLASS_NAME, false, Thread.currentThread().getContextClassLoader());
            Class.forName(CONTROLLER_CLASS_NAME, false, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            // 获取并编译协议文件
            // 通过编译生成的描述文件构造描述器进而获取并设置类名
            // 编译协议生成的类和相关通道和控制器文件并注入loader
        }
    }
}
