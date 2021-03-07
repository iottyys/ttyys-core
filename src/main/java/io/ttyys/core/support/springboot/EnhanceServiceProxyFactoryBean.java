package io.ttyys.core.support.springboot;

import io.ttyys.core.support.architecture.EnhanceServiceProxy;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

public class EnhanceServiceProxyFactoryBean<T> implements FactoryBean<T> {
    private final Class<T> interfaceType;

    public EnhanceServiceProxyFactoryBean(Class<T> interfaceType) {
        this.interfaceType = interfaceType;
    }

    @SuppressWarnings({"unchecked", "RedundantThrows"})
    @Override
    public T getObject() throws Exception {
        return (T) Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class[]{interfaceType},
                new EnhanceServiceProxy<>(interfaceType));
    }

    @Override
    public Class<?> getObjectType() {
        return this.interfaceType;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
