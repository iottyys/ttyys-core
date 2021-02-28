package io.ttyys.core.compiler;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class DynamicCompilerException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final List<Diagnostic<? extends JavaFileObject>> diagnostics;

    public DynamicCompilerException(String message, List<Diagnostic<? extends JavaFileObject>> diagnostics) {
        super(message);
        this.diagnostics = diagnostics;
    }

    public DynamicCompilerException(Throwable cause, List<Diagnostic<? extends JavaFileObject>> diagnostics) {
        super(cause);
        this.diagnostics = diagnostics;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + "\n" + this.getErrors();
    }

    private List<Map<String, Object>> getErrorList() {
        if (this.diagnostics != null) {
            return this.diagnostics.stream().map(v ->
                    ImmutableMap.<String, Object>of("line", v.getLineNumber(),
                            "message", v.getMessage(Locale.getDefault())))
                    .collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    private String getErrors() {
        return Joiner.on(", ").join(
                this.getErrorList().stream().map(v ->
                        Joiner.on("").withKeyValueSeparator(": ").join(v)).collect(Collectors.toList()));
    }
}
