package io.ttyys.core.rpc;

import lombok.Getter;
import org.apache.commons.exec.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;

public class PythonSocketServer {
    private final Executable executable;

    public PythonSocketServer(String serverName) throws IOException {
        this.executable = Executable.newInstance(serverName);
    }

    public void start() throws IOException {
        Executor executor = new DaemonExecutor();
        executor.setWorkingDirectory(this.executable.workingDir);
        executor.setStreamHandler(new PumpStreamHandler(System.out, System.err));
        executor.setWatchdog(new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT));
        executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());
        executor.execute(this.executable.commandLine);
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
            return new CommandLine(executeFile);
        }

        private File createExecutableFile(Path workingDir) throws IOException {
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("bin/osx/server");
            if (is == null) {
                is = Thread.currentThread().getContextClassLoader().getResourceAsStream("bin/server");
            }
            if (is == null) {
                throw new IllegalStateException("could not exec server. unable to find server executable");
            }
            Path executable = Files.createTempFile(workingDir, "server", "");
            Files.copy(is, executable);
            Files.setPosixFilePermissions(executable, PosixFilePermissions.fromString("500"));
            return executable.toFile();
        }
    }
}
