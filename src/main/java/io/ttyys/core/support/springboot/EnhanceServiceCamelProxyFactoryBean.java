package io.ttyys.core.support.springboot;

import io.ttyys.core.support.architecture.EnhanceServiceCamelProxy;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

public class EnhanceServiceCamelProxyFactoryBean<T> implements FactoryBean<T> {
    private final Class<T> interfaceType;

    public EnhanceServiceCamelProxyFactoryBean(Class<T> interfaceType) {
        this.interfaceType = interfaceType;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getObject() throws Exception {
        return (T) Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class[]{interfaceType},
                new EnhanceServiceCamelProxy<>(interfaceType));
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
