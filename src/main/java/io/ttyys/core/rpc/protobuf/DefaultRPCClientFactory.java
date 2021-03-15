package io.ttyys.core.rpc.protobuf;

import com.google.protobuf.BlockingRpcChannel;
import com.google.protobuf.RpcController;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.net.SocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultRPCClientFactory extends RPCClientFactory {
    private final String host;
    private final int port;
    private final SocketFactory socketFactory;
    private final Map<String, ClientProxy> proxies = new ConcurrentHashMap<>(0);

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
    public ClientProxy getProxy(String xxxxxxx) {
        return null;
    }

    @Override
    public void createProxies(String[] locations) throws Exception {
        if (this.proxies.isEmpty()) {
            synchronized (this) {
                if (!this.proxies.isEmpty()) {
                    return;
                }
                for (String location: locations) {
                    ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
                    Resource[] resources = resolver.getResources(location);
                    for (Resource resource: resources) {
                        this.createProxy(resource.getInputStream());
                    }

                }
            }
        }
    }

    private void createProxy(InputStream proto) throws Exception {
        // 反射创建channel和controller对象
        Class<?> clazz = Class.forName(RPCClientFactory.CONTROLLER_CLASS_NAME,
                false, Thread.currentThread().getContextClassLoader());
        // 编译输入流的proto，获得java类字符串和描述字符串
        // 使用java类字符串进行编译并注入loader
        Class<?> protoClass = Class.forName("", false, Thread.currentThread().getContextClassLoader());
        // 使用描述字符串构建描述对象获取java类名，services名称及每个service的rpcs名称
        Map<String, Map<String, Map<String, String>>> services = new HashMap<>(0);
        this.proxies.put("", new ClientProxy(protoClass, services, this, clazz));
    }

}
