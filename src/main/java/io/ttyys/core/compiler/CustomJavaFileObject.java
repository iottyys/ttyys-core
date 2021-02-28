package io.ttyys.core.compiler;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;
import java.io.*;
import java.net.URI;

public class CustomJavaFileObject implements JavaFileObject {
    private final String binaryName;
    private final URI uri;
    private final String name;

    public CustomJavaFileObject(String binaryName, URI uri) {
        this.uri = uri;
        this.binaryName = binaryName;
        this.name = uri.getPath() == null ? uri.getSchemeSpecificPart() : uri.getPath();
    }

    @Override
    public URI toUri() {
        return this.uri;
    }

    @Override
    public InputStream openInputStream() throws IOException {
        return uri.toURL().openStream();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @SuppressWarnings("RedundantThrows")
    @Override
    public OutputStream openOutputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("RedundantThrows")
    @Override
    public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("RedundantThrows")
    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("RedundantThrows")
    @Override
    public Writer openWriter() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getLastModified() {
        return 0;
    }

    @Override
    public Kind getKind() {
        return Kind.CLASS;
    }

    @Override
    public boolean isNameCompatible(String simpleName, Kind kind) {
        String baseName = simpleName + kind.extension;
        return kind.equals(this.getKind())
                && (baseName.equals(this.getName()) || this.getName().endsWith("/" + baseName));
    }

    @Override
    public NestingKind getNestingKind() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Modifier getAccessLevel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean delete() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[" + this.toUri() + "]";
    }

    public String binaryName() {
        return this.binaryName;
    }
}
