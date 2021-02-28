package io.ttyys.core.compiler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class DynamicClassLoader extends ClassLoader {
    private final Map<String, MemoryByteCode> byteCodes = new HashMap<>();

    public DynamicClassLoader(ClassLoader classLoader) {
        super(classLoader);
    }

    public void registerCompiledSource(MemoryByteCode byteCode) {
        this.byteCodes.put(byteCode.getClassName(), byteCode);
    }

    public Map<String, Class<?>> getClasses(boolean inject) throws ClassNotFoundException {
        Map<String, Class<?>> classes = new HashMap<>(this.byteCodes.size());
        for (MemoryByteCode byteCode: this.byteCodes.values()) {
            classes.put(byteCode.getClassName(), this.findClass(byteCode.getClassName()));
            if (inject) {
                this.injectToParent(byteCode.getClassName(), byteCode.getByteCode());
            }
        }
        return classes;
    }

    public Map<String, byte[]> getByteCodes() {
        Map<String, byte[]> result = new HashMap<>(this.byteCodes.size());
        for (Map.Entry<String, MemoryByteCode> entry : this.byteCodes.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getByteCode());
        }
        return result;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        MemoryByteCode byteCode = this.byteCodes.get(name);
        if (byteCode == null) {
            return super.findClass(name);
        }
        return super.defineClass(name, byteCode.getByteCode(), 0, byteCode.getByteCode().length);
    }

    private void injectToParent(String name, byte[] byteCode) {
        try {
            ClassLoader parent = this.getParent();
            Method method = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
            method.setAccessible(true);
            method.invoke(parent, name, byteCode, 0, byteCode.length);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("could not inject for parent classloader. ", e);
        }
    }
}
