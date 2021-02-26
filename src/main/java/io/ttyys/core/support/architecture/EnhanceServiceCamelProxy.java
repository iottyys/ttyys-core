package io.ttyys.core.support.architecture;

import io.ttyys.core.support.camel.Execution;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class EnhanceServiceCamelProxy<T> implements InvocationHandler {

    private final Class<T> interfaceType;

    public EnhanceServiceCamelProxy(Class<T> interfaceType) {
        this.interfaceType = interfaceType;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?> declaring = method.getDeclaringClass();
        if (declaring.equals(Object.class)) {
            return method.invoke(this, args);
        }
        if (method.getDeclaringClass().equals(interfaceType)) {
            throw new UnsupportedOperationException(
                    "Must be annotated with " + Execution.class.getName()
                            + " on method: " + interfaceType.getName() + "." + method.toGenericString());
        }
        throw new IllegalAccessError("unexpect method invoke.");
    }
}
