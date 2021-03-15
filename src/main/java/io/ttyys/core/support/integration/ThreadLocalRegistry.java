package io.ttyys.core.support.integration;

import org.apache.camel.NoSuchBeanException;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.spi.Registry;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class ThreadLocalRegistry implements Registry {

    static final ThreadLocal<Map<String, Map<Class<?>, Object>>> LOCAL = new ThreadLocal<>();

    @Override
    public Object lookupByName(String name) {
        return lookupByNameAndType(name, Object.class);
    }

    @Override
    public <T> T lookupByNameAndType(String name, Class<T> type) {
        Map<Class<?>, Object> map = self().get(name);
        if (map == null) {
            return null;
        }

        Object answer = map.get(type);
        if (answer == null) {
            // look for first entry that is the type
            for (Object value : map.values()) {
                if (type.isInstance(value)) {
                    answer = value;
                    break;
                }
            }
        }
        if (answer == null) {
            return null;
        }
        try {
            answer = unwrap(answer);
            return type.cast(answer);
        } catch (Throwable e) {
            String msg = "Found bean: " + name + " in ThreadLocalRegistry: " + this
                    + " of type: " + answer.getClass().getName() + " expected type was: " + type;
            throw new NoSuchBeanException(name, msg, e);
        }
    }

    @Override
    public <T> Map<String, T> findByTypeWithName(Class<T> type) {
        Map<String, T> result = new LinkedHashMap<>();
        for (Map.Entry<String, Map<Class<?>, Object>> entry : self().entrySet()) {
            for (Object value : entry.getValue().values()) {
                if (type.isInstance(value)) {
                    value = unwrap(value);
                    result.put(entry.getKey(), type.cast(value));
                }
            }
        }
        return result;
    }

    @Override
    public <T> Set<T> findByType(Class<T> type) {
        Set<T> result = new LinkedHashSet<>();
        for (Map.Entry<String, Map<Class<?>, Object>> entry : self().entrySet()) {
            for (Object value : entry.getValue().values()) {
                if (type.isInstance(value)) {
                    value = unwrap(value);
                    result.add(type.cast(value));
                }
            }
        }
        return result;
    }

    @Override
    public void bind(String id, Class<?> type, Object bean) throws RuntimeCamelException {
        self().computeIfAbsent(id, k -> new LinkedHashMap<>()).put(type, wrap(bean));
    }

    private Map<String, Map<Class<?>, Object>> self() {
        if (LOCAL.get() == null) {
            LOCAL.set(new LinkedHashMap<>());
        }
        return LOCAL.get();
    }
}
