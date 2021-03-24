package io.ttyys.core.processor;

import org.apache.commons.exec.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Enumeration;
import java.util.Vector;

public class WatchdogShutdownHookProcessDestroyer implements ProcessDestroyer, Runnable, Watchdog {
    private final Vector<Process> processes = new Vector<Process>();

    private WatchdogShutdownHookProcessDestroyer.ProcessDestroyerImpl destroyProcessThread = null;
    private volatile boolean running = false;
    private boolean added = false;

    @Override
    public void run() {
        synchronized (processes) {
            running = true;
            final Enumeration<Process> e = processes.elements();
            while (e.hasMoreElements()) {
                final Process process = e.nextElement();
                try {
                    process.destroy();
                }
                catch (final Throwable t) {
                    System.err.println("Unable to terminate process during process shutdown");
                }
            }
        }
    }

    @Override
    public boolean add(Process process) {
        synchronized (processes) {
            if (processes.size() == 0) {
                addShutdownHook();
            }
            processes.addElement(process);
            return processes.contains(process);
        }
    }

    @Override
    public boolean remove(Process process) {
        synchronized (processes) {
            final boolean processRemoved = processes.removeElement(process);
            if (processRemoved && processes.size() == 0) {
                removeShutdownHook();
            }
            return processRemoved;
        }
    }

    @Override
    public int size() {
        return processes.size();
    }

    private void addShutdownHook() {
        if (!running) {
            destroyProcessThread = new WatchdogShutdownHookProcessDestroyer.ProcessDestroyerImpl();
            Runtime.getRuntime().addShutdownHook(destroyProcessThread);
            added = true;
        }
    }

    private void removeShutdownHook() {
        if (added && !running) {
            final boolean removed = Runtime.getRuntime().removeShutdownHook(
                    destroyProcessThread);
            if (!removed) {
                System.err.println("Could not remove shutdown hook");
            }
            destroyProcessThread.setShouldDestroy(false);
            destroyProcessThread.start();
            try {
                destroyProcessThread.join(20000);
            } catch (final InterruptedException ignore) {
            }
            destroyProcessThread = null;
            added = false;
        }
    }

    @Override
    public void watch() {
        try {
            new WatchdogProcess().start();
        } catch (IOException e) {
            // todo runtime exception
        }
    }

    private class ProcessDestroyerImpl extends Thread {

        private boolean shouldDestroy = true;

        public ProcessDestroyerImpl() {
            super("ProcessDestroyer Shutdown Hook");
        }

        @Override
        public void run() {
            if (shouldDestroy) {
                WatchdogShutdownHookProcessDestroyer.this.run();
            }
        }

        public void setShouldDestroy(final boolean shouldDestroy) {
            this.shouldDestroy = shouldDestroy;
        }
    }

    private static class WatchdogProcess {
        private final File workingDir;
        private final CommandLine commandLine;

        WatchdogProcess() throws IOException {
            Path workingPath = Files.createTempDirectory(""); // 主进程pid+watchdog
            this.workingDir = workingPath.toFile();
            workingPath.toFile().deleteOnExit();
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("bin/watchdog");
            if (is == null) {
                is = Thread.currentThread().getContextClassLoader().getResourceAsStream("bin/watchdog.bat");
            }
            if (is == null) {
                throw new IllegalStateException("could not exec watchdog. unable to find watchdog executable");
            }
            Path executable = Files.createTempFile(workingPath, "server", "");
            Files.setPosixFilePermissions(executable, PosixFilePermissions.fromString("rwx------"));
            FileUtils.copyInputStreamToFile(is, executable.toFile());
            executable.toFile().deleteOnExit();
            this.commandLine = new CommandLine(executable.toFile());
            // todo  add param   main pid   watch pids
        }

        public void start() throws IOException {
            Executor executor = new DaemonExecutor();
            executor.setWorkingDirectory(this.workingDir);
            executor.setStreamHandler(new PumpStreamHandler(System.out, System.err)); // todo 日志可以不处理
            executor.setWatchdog(new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT));
            executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());
            executor.execute(this.commandLine, new DefaultExecuteResultHandler());
        }
    }
}
