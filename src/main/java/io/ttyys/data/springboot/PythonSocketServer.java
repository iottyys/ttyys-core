package io.ttyys.data.springboot;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;

public class PythonSocketServer {
    public void start() throws IOException {
        File execution = ResourceUtils.getFile("bin/PythonSocketServer");
        CommandLine cmdLine = new CommandLine(execution.getAbsolutePath());
        Executor executor = new DefaultExecutor();
        executor.execute(cmdLine);
    }
}
