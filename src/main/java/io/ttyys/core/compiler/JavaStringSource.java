package io.ttyys.core.compiler;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import java.io.IOException;
import java.net.URI;

public class JavaStringSource extends SimpleJavaFileObject {
    private static final char PKG_SEPARATOR = '.';
    private static final char DIR_SEPARATOR = '/';

    private final String contents;

    public JavaStringSource(String className, String contents) {
        super(URI.create("string:///"
                + className.replace(PKG_SEPARATOR, DIR_SEPARATOR)
                + Kind.SOURCE.extension), Kind.SOURCE);
        this.contents = contents;
    }

    @SuppressWarnings("RedundantThrows")
    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
        return this.contents;
    }
}
