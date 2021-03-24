package io.ttyys.core.processor;

import com.sun.jna.Platform;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DaemonExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Destroys all registered Processes when the VM exits.
 *
 * @author tengwang
 * @version 1.0.0
 * @date 2021/3/22 11:18 上午
 */
@Slf4j
public class CustomShutdownHookProcessDestroyer extends ShutdownHookProcessDestroyer {

    private List<String> pids = new ArrayList<>();


    /**
     * the list of currently running processes
     */
    private final Vector<Process> processes = new Vector<Process>();

    /**
     * The thread registered at the JVM to execute the shutdown handler
     */
    private CustomShutdownHookProcessDestroyer.ProcessDestroyerImpl destroyProcessThread = null;

    /**
     * Whether or not this ProcessDestroyer has been registered as a shutdown hook
     */
    private boolean added = false;

    /**
     * Whether or not this ProcessDestroyer is currently running as shutdown hook
     */
    private volatile boolean running = false;

    private class ProcessDestroyerImpl extends Thread {

        private boolean shouldDestroy = true;

        public ProcessDestroyerImpl() {
            super("ProcessDestroyer Shutdown Hook");
        }

        @Override
        public void run() {
            if (shouldDestroy) {
                CustomShutdownHookProcessDestroyer.this.run();
            }
        }

        public void setShouldDestroy(final boolean shouldDestroy) {
            this.shouldDestroy = shouldDestroy;
        }
    }

    /**
     * Constructs a {@code ProcessDestroyer} and obtains
     * {@code Runtime.addShutdownHook()} and
     * {@code Runtime.removeShutdownHook()} through reflection. The
     * ProcessDestroyer manages a list of processes to be destroyed when the VM
     * exits. If a process is added when the list is empty, this
     * {@code ProcessDestroyer} is registered as a shutdown hook. If
     * removing a process results in an empty list, the
     * {@code ProcessDestroyer} is removed as a shutdown hook.
     */
    public CustomShutdownHookProcessDestroyer() {
    }

    /**
     * Registers this {@code ProcessDestroyer} as a shutdown hook, uses
     * reflection to ensure pre-JDK 1.3 compatibility.
     */
    private void addShutdownHook() {
        if (!running) {
            destroyProcessThread = new CustomShutdownHookProcessDestroyer.ProcessDestroyerImpl();
            Runtime.getRuntime().addShutdownHook(destroyProcessThread);
            added = true;
        }
    }

    /**
     * Removes this {@code ProcessDestroyer} as a shutdown hook, uses
     * reflection to ensure pre-JDK 1.3 compatibility
     */
    private void removeShutdownHook() {
        if (added && !running) {
            final boolean removed = Runtime.getRuntime().removeShutdownHook(
                    destroyProcessThread);
            if (!removed) {
                System.err.println("Could not remove shutdown hook");
            }
            /*
             * start the hook thread, a unstarted thread may not be eligible for
             * garbage collection Cf.: http://developer.java.sun.com/developer/
             * bugParade/bugs/4533087.html
             */

            destroyProcessThread.setShouldDestroy(false);
            destroyProcessThread.start();
            // this should return quickly, since it basically is a NO-OP.
            try {
                destroyProcessThread.join(20000);
            } catch (final InterruptedException ie) {
                // the thread didn't die in time
                // it should not kill any processes unexpectedly
            }
            destroyProcessThread = null;
            added = false;
        }
    }

    /**
     * Returns whether or not the ProcessDestroyer is registered as as shutdown
     * hook
     *
     * @return true if this is currently added as shutdown hook
     */
    public boolean isAddedAsShutdownHook() {
        return added;
    }

    /**
     * Returns {@code true} if the specified {@code Process} was
     * successfully added to the list of processes to destroy upon VM exit.
     *
     * @param process the process to add
     * @return {@code true} if the specified {@code Process} was
     * successfully added
     */
    public boolean add(final Process process) {
        synchronized (processes) {
            if (null != process) {
                pids.add(this.getProcessId(process));
            }
            // if this list is empty, register the shutdown hook
            if (processes.size() == 0) {
                addShutdownHook();
            }
            processes.addElement(process);
            return processes.contains(process);
        }
    }

    /**
     * Returns {@code true} if the specified {@code Process} was
     * successfully removed from the list of processes to destroy upon VM exit.
     *
     * @param process the process to remove
     * @return {@code true} if the specified {@code Process} was
     * successfully removed
     */
    public boolean remove(final Process process) {
        synchronized (processes) {
            final boolean processRemoved = processes.removeElement(process);
            if (processRemoved && processes.size() == 0) {
                removeShutdownHook();
            }
            return processRemoved;
        }
    }

    /**
     * Returns the number of registered processes.
     *
     * @return the number of register process
     */
    public int size() {
        return processes.size();
    }

    /**
     * Invoked by the VM when it is exiting.
     */
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

    /**
     * get process pid
     *
     * @param process
     * @return
     */
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
        } else if (Platform.isLinux() || Platform.isAIX()) {
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

//    private String getPidInJava10() {
//        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
//        final long pid = runtimeMXBean.getPid();
//        return String.valueOf(pid);
//    }

    private String getPidBeforeJava9() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        String pid = runtimeMXBean.getName().split("@")[0];
        return pid;
    }

//    private String getPidInJava9() {
//        long pid = ProcessHandle.current().pid();
//        return String.valueOf(pid);
//    }

    /**
     * 启动守护进程
     */
    protected void startWatchDog() {
        // 1.获取当前进程pid
        String currentPid = this.getPidBeforeJava9();
        Map<String, String> params = new HashMap<>();
        params.put("mainPid", currentPid);
        params.put("extensionPids", this.pids.stream().collect(Collectors.joining(", ")));
        Thread thread = new Thread(() -> {
            // 调用executor 启动守护shell
            Executor executor = new DaemonExecutor();
            try {
                URL res = getClass().getClassLoader().getResource("shell/watchDog.sh");
                File file = Paths.get(res.toURI()).toFile();
                this.changeFilePermission(file);
                executor.execute(new CommandLine(file), params);
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
                log.error("start watch dog fail......");
            }
        });
        thread.start();
    }

    /**
     * 修改文件权限
     *
     * @param file
     */
    private void changeFilePermission(File file) {
        Path path = Paths.get(file.getAbsolutePath());
        Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        perms.add(PosixFilePermission.GROUP_READ);
        perms.add(PosixFilePermission.GROUP_WRITE);
        perms.add(PosixFilePermission.GROUP_EXECUTE);
        try {
            Files.setPosixFilePermissions(path, perms);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("file can not given Permission......");
        }
    }

    /**
     * 修改文件权限
     *
     * @param file
     * @param perms
     */
    private void changeFilePermission(File file, Set<PosixFilePermission> perms) {
        Path path = Paths.get(file.getAbsolutePath());
        try {
            Files.setPosixFilePermissions(path, perms);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("file can not given Permission......");
        }
    }

}
