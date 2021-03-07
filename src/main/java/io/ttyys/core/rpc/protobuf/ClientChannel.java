package io.ttyys.core.rpc.protobuf;

import com.google.protobuf.BlockingRpcChannel;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public abstract class ClientChannel implements BlockingRpcChannel {
    public static ClientChannel defaultChannel(RPCClientFactory connectionFactory) {
        try {
            Class<?> clazz = Class.forName("", false, Thread.currentThread().getContextClassLoader());
            Constructor<?> constructor = clazz.getDeclaredConstructor(RPCClientFactory.class);
            return (ClientChannel) constructor.newInstance(connectionFactory);
        } catch (ClassNotFoundException | NoSuchMethodException
                | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new IllegalStateException("could not instance channel. ", e);
        }
    }
}
