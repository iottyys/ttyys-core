package io.ttyys.core.support.springboot;

import io.ttyys.core.support.camel.ApplicationServiceCamelProxy;
import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.Proxy;

public class ApplicationServiceCamelProxyFactoryBean<T> implements FactoryBean<T> {
    private final Class<T> interfaceType;

    public ApplicationServiceCamelProxyFactoryBean(Class<T> interfaceType) {
        this.interfaceType = interfaceType;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getObject() throws Exception {
        return (T) Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class[]{interfaceType},
                new ApplicationServiceCamelProxy<>(interfaceType));
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
