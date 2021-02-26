package io.ttyys.core.compiler;

import com.google.common.collect.ImmutableList;

import javax.tools.*;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringSourceDynamicCompiler {
    static final Map<String, JavaFileObject> fileObjectMap = new ConcurrentHashMap<>();
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("package\\s+(.*);\\s*");
    private static final Pattern CLASS_PATTERN = Pattern.compile("(class|interface)\\s+([$_a-zA-Z][$_a-zA-Z0-9]*)\\s*");

    public static byte[] compile(String source, DiagnosticCollector<JavaFileObject> compileCollector) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        JavaFileManager javaFileManager =
                new JavaSourceFileManager(compiler.getStandardFileManager(compileCollector, null, null));
        Matcher classMatcher = CLASS_PATTERN.matcher(source);
        Matcher packageMatcher = PACKAGE_PATTERN.matcher(source);
        if (!classMatcher.find()) {
            throw new IllegalArgumentException("Invalid class");
        }
        String className = classMatcher.group(2);
        String fullClassName = className;
        if (packageMatcher.find()) {
            className = fullClassName = packageMatcher.group(1) + '.' + className;
        }
        JavaFileObject javaSourceFile = new JavaSourceFile(className, source);
        Boolean result = compiler.getTask(null, javaFileManager, compileCollector,
                null, null, ImmutableList.of(javaSourceFile)).call();
        if (!result) {
            List<Diagnostic<? extends JavaFileObject>> compileError = compileCollector.getDiagnostics();
            StringBuilder compileErrorRes = new StringBuilder();
            for (Diagnostic<? extends JavaFileObject> diagnostic : compileError) {
                compileErrorRes.append("Compilation error at ");
                compileErrorRes.append(diagnostic.getLineNumber());
                compileErrorRes.append(".");
                compileErrorRes.append(System.lineSeparator());
                compileErrorRes.append(diagnostic.getMessage(Locale.getDefault()));
                compileErrorRes.append(System.lineSeparator());
            }
            System.out.println(compileErrorRes.toString());
            return null;
        }
        JavaFileObject bytesJavaFileObject = fileObjectMap.get(fullClassName);
        if (bytesJavaFileObject != null) {
            return ((JavaSourceFile) bytesJavaFileObject).getCompiledBytes();
        }
        return null;
    }
}
