package io.ttyys.core.compiler;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import java.io.IOException;

public class JavaSourceFileManager extends ForwardingJavaFileManager<JavaFileManager> {
    protected JavaSourceFileManager(JavaFileManager fileManager) {
        super(fileManager);
    }

    @Override
    public JavaFileObject getJavaFileForInput(JavaFileManager.Location location,
                                              String className,
                                              JavaFileObject.Kind kind) throws IOException {
        JavaFileObject javaFileObject = StringSourceDynamicCompiler.fileObjectMap.get(className);
        if (javaFileObject == null) {
            return super.getJavaFileForInput(location, className, kind);
        }
        return javaFileObject;
    }

    @Override
    public JavaFileObject getJavaFileForOutput(JavaFileManager.Location location,
                                               String className,
                                               JavaFileObject.Kind kind,
                                               FileObject sibling) throws IOException {
        JavaFileObject javaFileObject = new JavaSourceFile(className, kind);
        StringSourceDynamicCompiler.fileObjectMap.put(className, javaFileObject);
        return javaFileObject;
    }
}
