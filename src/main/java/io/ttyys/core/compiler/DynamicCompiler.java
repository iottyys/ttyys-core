package io.ttyys.core.compiler;

import javax.tools.*;
import java.io.Writer;
import java.util.*;

public class DynamicCompiler {
    private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    private final StandardJavaFileManager standardFileManager;
    private final List<String> options = new ArrayList<>(0);
    private final DynamicClassLoader dynamicClassLoader;
    private final Collection<JavaFileObject> compilationUnits = new ArrayList<>(0);
    private final List<Diagnostic<? extends JavaFileObject>> errors = new ArrayList<>(0);
    private final List<Diagnostic<? extends JavaFileObject>> warnings = new ArrayList<>(0);

    public DynamicCompiler(ClassLoader classLoader) {
        if (this.compiler == null) {
            throw new IllegalStateException("The application must running in JDK that include tools.jar or just add it.");
        }
        this.standardFileManager = this.compiler.getStandardFileManager(null, null, null);
        options.add("-Xlint:unchecked");
        this.dynamicClassLoader = new DynamicClassLoader(classLoader);
    }

    public void addSource(String className, String source) {
        this.addSource(new JavaStringSource(className, source));
    }

    public void addSource(JavaFileObject javaFileObject) {
        this.compilationUnits.add(javaFileObject);
    }

    public Map<String, byte[]> compile() throws DynamicCompilerException {
        this.doCompile();
        return this.dynamicClassLoader.getByteCodes();
    }

    public Map<String, Class<?>> build(boolean injectParentClassloader) throws DynamicCompilerException {
        this.doCompile();
        try {
            return this.dynamicClassLoader.getClasses(injectParentClassloader);
        } catch (ClassNotFoundException e) {
            throw new DynamicCompilerException(e, this.errors);
        }
    }

    public List<String> getErrors() {
        return this.diagnosticToString(this.errors);
    }

    public List<String> getWarnings() {
        return this.diagnosticToString(this.warnings);
    }

    public ClassLoader getClassLoader() {
        return this.dynamicClassLoader;
    }

    protected void doCompile() throws DynamicCompilerException {
        this.errors.clear();
        this.warnings.clear();
        JavaFileManager fileManager = new DynamicJavaFileManager(this.standardFileManager, this.dynamicClassLoader);
        DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
        JavaCompiler.CompilationTask task = compiler.getTask(
                null, fileManager, collector, this.options, null, this.compilationUnits);
        try {
            if (!this.compilationUnits.isEmpty()) {
                boolean result = task.call();
                if (!result || !collector.getDiagnostics().isEmpty()) {
                    for (Diagnostic<? extends JavaFileObject> diagnostic : collector.getDiagnostics()) {
                        switch (diagnostic.getKind()) {
                            case ERROR:
                            case OTHER:
                                this.errors.add(diagnostic);
                                break;
                            default:
                                this.warnings.add(diagnostic);
                                break;
                        }
                    }
                    if (!this.errors.isEmpty()) {
                        throw new DynamicCompilerException("Compilation Error", this.errors);
                    }
                }
            }
        } catch (Throwable th) {
            throw new DynamicCompilerException(th, this.errors);
        } finally {
            compilationUnits.clear();
        }
    }

    private List<String> diagnosticToString(List<Diagnostic<? extends JavaFileObject>> diagnostics) {
        List<String> diagnosticMessages = new ArrayList<>(diagnostics.size());
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics) {
            diagnosticMessages.add("line: "
                    + diagnostic.getLineNumber()
                    + ", message: "
                    + diagnostic.getMessage(Locale.getDefault()));
        }
        return diagnosticMessages;
    }
}
