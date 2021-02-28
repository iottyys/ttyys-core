package io.ttyys.core.compiler;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

public class MemoryByteCode extends SimpleJavaFileObject {
    private static final char PKG_SEPARATOR = '.';
    private static final char DIR_SEPARATOR = '/';
    private static final String CLASS_FILE_SUFFIX = ".class";

    private ByteArrayOutputStream byteArrayOutputStream;

    public MemoryByteCode(String className) {
        super(URI.create("byte:///" + className.replace(PKG_SEPARATOR, DIR_SEPARATOR) + Kind.CLASS.extension), Kind.CLASS);
    }

    public MemoryByteCode(String className, ByteArrayOutputStream byteArrayOutputStream) {
        this(className);
        this.byteArrayOutputStream = byteArrayOutputStream;
    }

    @SuppressWarnings("RedundantThrows")
    @Override
    public OutputStream openOutputStream() throws IOException {
        if (this.byteArrayOutputStream == null) {
            this.byteArrayOutputStream = new ByteArrayOutputStream();
        }
        return this.byteArrayOutputStream;
    }

    public byte[] getByteCode() {
        return this.byteArrayOutputStream.toByteArray();
    }

    public String getClassName() {
        String className = super.getName();
        className = className.replace(DIR_SEPARATOR, PKG_SEPARATOR);
        className = className.substring(1, className.indexOf(CLASS_FILE_SUFFIX));
        return className;
    }
}
