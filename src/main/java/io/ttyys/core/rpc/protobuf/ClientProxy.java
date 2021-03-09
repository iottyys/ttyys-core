package io.ttyys.core.rpc.protobuf;

import com.github.dozermapper.core.Mapper;
import com.google.protobuf.BlockingRpcChannel;
import com.google.protobuf.RpcController;
import org.springframework.util.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class ClientProxy {
    private final Map<String, Map<String, Map<String, String>>> services;
    private final Map<String, Class<?>> serviceInterfaces;
    private final Map<String, Method> newBlockStubs;
    private final Class<?> protoClass;
    private final RPCClientFactory factory;
    private final Constructor<?> controllerConstructor;
    private Object serviceObject;
    private String currentService;

    public ClientProxy(Class<?> protoClass, Map<String, Map<String, Map<String, String>>> services, RPCClientFactory factory, Class<?> controllerClass) throws Exception {
        this.protoClass = protoClass;
        this.services = services;
        this.factory = factory;
        this.controllerConstructor = controllerClass.getConstructor();
        this.serviceInterfaces = new HashMap<>(services.size());
        this.newBlockStubs = new HashMap<>(services.size());
    }

    protected void init() throws Exception {
        for (String serviceName: this.services.keySet()) {
            serviceName = protoClass.getName() + "$" + serviceName;
            Class<?> serviceInterface = Class.forName(serviceName, false, Thread.currentThread().getContextClassLoader());
            this.serviceInterfaces.put(serviceName, serviceInterface);
            Method method = serviceInterface.getDeclaredMethod("newBlockingStub", BlockingRpcChannel.class);
            assert Modifier.isStatic(method.getModifiers());
            this.newBlockStubs.put(serviceName, method);
        }
    }

    public ClientProxy service(String serviceName) {
        if (!this.services.containsKey(serviceName)) {
            throw new IllegalArgumentException("no found service: " + serviceName);
        }
        try {
            this.serviceObject = this.newBlockStubs.get(serviceName).invoke(null,
                    ClientChannel.defaultChannel(this.factory));
            this.currentService = serviceName;
            return this;
        } catch (Exception e) {
            throw new IllegalStateException("could not create service. ", e);
        }
    }

    public Object send(String method, Object param, Mapper mapper) {
        if (!StringUtils.hasText(this.currentService)
                || !this.services.get(this.currentService).containsKey(method)
                || this.serviceInterfaces.get(this.currentService) == null
                || this.serviceObject == null) {
            throw new IllegalStateException("service not exists, call service() create it first. ");
        }
        try {
            RpcController controller = (RpcController) this.controllerConstructor.newInstance();
            String paramClassName = this.protoClass.getName() + "$"
                    + this.services.get(this.currentService).get(method).get("param");
            Class<?> paramClass = Class.forName(paramClassName,
                    false,
                    Thread.currentThread().getContextClassLoader());
            Method rpc = this.serviceInterfaces.get(this.currentService)
                    .getDeclaredMethod(method, RpcController.class, paramClass);
            return rpc.invoke(this.serviceObject, controller, mapper.map(param, paramClass));
        } catch (Exception e) {
            throw new IllegalStateException("could not send message. ", e);
        }

    }
}
