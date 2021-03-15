package io.ttyys.core.rpc;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class PythonSocketServer {

    private static final String tempPath = System.getProperty("java.io.tmpdir");


    public void start() throws IOException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream("bin/server");
        File targetFile = new File(tempPath + "PythonSocketServer");
        FileUtils.copyInputStreamToFile(stream, targetFile);
        Executor executor = new DefaultExecutor();
        executor.execute(CommandLine.parse("chmod +x " + targetFile.getAbsolutePath()));
        CommandLine cmdLine = new CommandLine(targetFile.getAbsolutePath());
        executor.execute(cmdLine);
    }
}
