package io.ttyys.core.compiler;

import com.google.common.collect.Iterables;

import javax.tools.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DynamicJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {
    private static final String[] superLocationNames = { StandardLocation.PLATFORM_CLASS_PATH.name(), "SYSTEM_MODULES" };
    private final PackageInternalsFinder finder;
    private final DynamicClassLoader classLoader;
    private final List<MemoryByteCode> byteCodes = new ArrayList<>();

    public DynamicJavaFileManager(JavaFileManager javaFileManager, DynamicClassLoader classLoader) {
        super(javaFileManager);
        this.classLoader = classLoader;
        this.finder = new PackageInternalsFinder(classLoader);
    }

    @SuppressWarnings("RedundantThrows")
    @Override
    public JavaFileObject getJavaFileForOutput(
            Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
        for (MemoryByteCode byteCode : this.byteCodes) {
            if (byteCode.getClassName().equals(className)) {
                return byteCode;
            }
        }

        MemoryByteCode innerClass = new MemoryByteCode(className);
        this.byteCodes.add(innerClass);
        this.classLoader.registerCompiledSource(innerClass);
        return innerClass;
    }

    @Override
    public ClassLoader getClassLoader(Location location) {
        return this.classLoader;
    }

    @Override
    public Iterable<JavaFileObject> list(
            Location location, String packageName, Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {
        if (location instanceof StandardLocation) {
            String locationName = ((StandardLocation) location).name();
            for (String name : superLocationNames) {
                if (name.equals(locationName)) {
                    return super.list(location, packageName, kinds, recurse);
                }
            }
        }

        if (location == StandardLocation.CLASS_PATH && kinds.contains(JavaFileObject.Kind.CLASS)) {
            return Iterables.concat(super.list(location, packageName, kinds, recurse), this.finder.find(packageName));
        }

        return super.list(location, packageName, kinds, recurse);
    }

    @Override
    public String inferBinaryName(Location location, JavaFileObject file) {
        if (file instanceof CustomJavaFileObject) {
            return ((CustomJavaFileObject) file).binaryName();
        }
        return super.inferBinaryName(location, file);
    }
}
