package io.ttyys.core.rpc;

import com.sun.jna.Platform;
import io.ttyys.core.processor.UnsupportedOSException;
import io.ttyys.core.processor.WatchdogShutdownHookProcessDestroyer;
import org.apache.commons.exec.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

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
        executor.setStreamHandler(new PumpStreamHandler(new LogHandler(Level.INFO), new LogHandler(Level.ERROR)));
        executor.setWatchdog(new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT));
        WatchdogShutdownHookProcessDestroyer destroyer = new WatchdogShutdownHookProcessDestroyer().setCleanDir(this.executable.workingDir);
        executor.setProcessDestroyer(destroyer);
        executor.execute(this.executable.commandLine, new DefaultExecuteResultHandler());
        destroyer.watch();
    }

    static class LogHandler extends LogOutputStream {
        private static final Logger logger = LoggerFactory.getLogger(PythonSocketServer.class);

        LogHandler(Level level) {
            super(level.toInt());
        }

        @Override
        protected void processLine(String line, int logLevel) {
            if (Level.ERROR.toInt() == logLevel) {
                logger.error(line);
                return;
            }
            logger.info(line);
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
            return new CommandLine(executeFile);
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
            Path executable = Files.createTempFile(workingDir, "server", "");
            Files.setPosixFilePermissions(executable, PosixFilePermissions.fromString("rwx------"));
            FileUtils.copyInputStreamToFile(is, executable.toFile());
            return executable.toFile();
        }

        private File createWatchDogFile(Path workingDir) throws IOException {
            // watch dog script resolve

            InputStream watchDogStream = null;
            if (Platform.isLinux() || Platform.isAIX() || Platform.isMac()) {
                watchDogStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("shell/watchDog.sh");
            } else if (Platform.isWindows()) {
                // TODO  support windows os
                throw new UnsupportedOSException("this os is not unsupported");
            } else {
                throw new UnsupportedOSException("this os is not unsupported");
            }
            if (watchDogStream == null) {
                throw new IllegalStateException("could not watch dog. unable to find watch dog file");
            }
            Path executableWatchDog = Files.createTempFile(workingDir, "watchDog", "");
            Files.setPosixFilePermissions(executableWatchDog, PosixFilePermissions.fromString("rwx------"));
            FileUtils.copyInputStreamToFile(watchDogStream, executableWatchDog.toFile());
            return executableWatchDog.toFile();
        }
    }

    public static void main(String[] args) throws IOException {
        new PythonSocketServer("test_server").start();
    }
}
