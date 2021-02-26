package io.ttyys.core.compiler;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

public class JavaSourceFile extends SimpleJavaFileObject {
    private final String source;
    private ByteArrayOutputStream outputStream;

    public JavaSourceFile(String name, String source) {
        super(URI.create("String:///" + name + Kind.SOURCE.extension), Kind.SOURCE);
        this.source = source;
    }

    public JavaSourceFile(String name, Kind kind) {
        super(URI.create("String:///" + name + Kind.SOURCE.extension), kind);
        this.source = null;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        if (source == null) {
            throw new IllegalArgumentException("source == null");
        }
        return source;
    }

    @Override
    public OutputStream openOutputStream() throws IOException {
        outputStream = new ByteArrayOutputStream();
        return outputStream;
    }

    public byte[] getCompiledBytes() {
        return outputStream.toByteArray();
    }
}
