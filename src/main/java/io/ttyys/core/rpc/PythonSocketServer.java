package io.ttyys.core.rpc;

import org.apache.commons.exec.*;
import org.apache.commons.io.FileUtils;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;
import org.zeroturnaround.process.PidUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PythonSocketServer {
    private final Executable executable;
    private final int delay;

    public PythonSocketServer(String serverName, int delay) throws IOException {
        this.executable = Executable.newInstance(serverName);
        this.delay = delay;
    }

    public void start() throws IOException {
        Future<ProcessResult> future = new ProcessExecutor()
                .directory(this.executable.workingDir)
                .command(this.executable.commandLine.toStrings())
                .redirectOutput(Slf4jStream.of(this.getClass()).asInfo())
                .redirectError(Slf4jStream.of(this.getClass()).asError())
                .start().getFuture();
        try {
            future.get(this.delay, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ignore) {
        }
    }

    static class Executable {
        private final File workingDir;
        private final CommandLine commandLine;

        private Executable(String serverName) throws IOException {
            Path workingPath = Files.createTempDirectory(serverName);
            this.workingDir = workingPath.toFile();
            this.workingDir.deleteOnExit();
            File executeFile = this.createExecutableFile(workingPath);
            executeFile.deleteOnExit();
            this.commandLine = this.createCommandLine(executeFile);
        }

        static Executable newInstance(String serverName) throws IOException {
            return new Executable(serverName);
        }

        private CommandLine createCommandLine(File executeFile) {
            CommandLine commandLine = new CommandLine(executeFile);
            commandLine.addArgument(String.valueOf(PidUtil.getMyPid()));
            return commandLine;
        }

        private File createExecutableFile(Path workingDir) throws IOException {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("bin/osx/server");
            if (is == null) {
                is = Thread.currentThread().getContextClassLoader().getResourceAsStream("bin/server");
            }
            if (is == null) {
                is = Thread.currentThread().getContextClassLoader().getResourceAsStream("bin/server.exe");
            }
            if (is == null) {
                throw new IllegalStateException("could not exec server. unable to find server executable");
            }
            Path executable = Files.createTempFile(workingDir, "server_", "_tmp");
            Files.setPosixFilePermissions(executable, PosixFilePermissions.fromString("rwx------"));
            FileUtils.copyInputStreamToFile(is, executable.toFile());
            return executable.toFile();
        }
    }
}
