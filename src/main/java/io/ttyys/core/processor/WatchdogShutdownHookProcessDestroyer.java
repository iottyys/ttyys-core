package io.ttyys.core.processor;

import com.sun.jna.Platform;
import org.apache.commons.exec.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;

public class WatchdogShutdownHookProcessDestroyer implements ProcessDestroyer, Runnable, Watchdog {
    private final Vector<Process> processes = new Vector<Process>();

    private WatchdogShutdownHookProcessDestroyer.ProcessDestroyerImpl destroyProcessThread = null;
    private volatile boolean running = false;
    private boolean added = false;

    private File cleanDir;

    @Override
    public void run() {
        synchronized (processes) {
            running = true;
            final Enumeration<Process> e = processes.elements();
            while (e.hasMoreElements()) {
                final Process process = e.nextElement();
                try {
                    process.destroy();
                } catch (final Throwable t) {
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
            new WatchdogProcess(this.processes, this.cleanDir).start();
        } catch (IOException e) {
            throw new RuntimeException("start watch dog fail, please check files");
        }
    }

    public WatchdogShutdownHookProcessDestroyer setCleanDir(File cleanDir) {
        this.cleanDir = cleanDir;
        return this;
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

        WatchdogProcess(Vector<Process> processes, File cleanDir) throws IOException {
            Path workingPath = Files.createTempDirectory("watchdog"); // 主进程pid+watchdog
            this.workingDir = workingPath.toFile();
            workingPath.toFile().deleteOnExit();
            String currentPid = this.getCurrentPid();
            Map<String, String> params = new HashMap<>();
            params.put("mainPid", currentPid);
            params.put("extensionPids", processes.stream().map(process -> this.getProcessId(process)).collect(Collectors.joining(", ")));
            params.put("workingDir",cleanDir.getAbsolutePath());
            // TODO tengwang 添加shell后台删除自己目录功能
            InputStream watchDogStream;
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
            Path executable = Files.createTempFile(workingPath, "watchdog", "");
            Files.setPosixFilePermissions(executable, PosixFilePermissions.fromString("rwx------"));
            FileUtils.copyInputStreamToFile(watchDogStream, executable.toFile());
            this.commandLine = new CommandLine(executable.toFile());
        }

        public void start() throws IOException {
            Executor executor = new DaemonExecutor();
            executor.setWorkingDirectory(this.workingDir);
            executor.setStreamHandler(new PumpStreamHandler(System.out, System.err)); // todo 日志可以不处理
            executor.setWatchdog(new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT));
            executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());
            executor.execute(this.commandLine, new DefaultExecuteResultHandler());
        }

        private String getCurrentPid() {
            RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
            String pid = runtimeMXBean.getName().split("@")[0];
            return pid;
        }

        private String getProcessId(Process process) {
            long pid = -1;
            Field field = null;
            if (Platform.isWindows()) {
                try {
                    field = process.getClass().getDeclaredField("handle");
                    field.setAccessible(true);
                    pid = Kernel32.INSTANCE.GetProcessId((Long) field.get(process));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (Platform.isLinux() || Platform.isAIX() || Platform.isMac()) {
                try {
                    Class<?> clazz = Class.forName("java.lang.UNIXProcess");
                    field = clazz.getDeclaredField("pid");
                    field.setAccessible(true);
                    pid = (Integer) field.get(process);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            return String.valueOf(pid);
        }
    }
}
