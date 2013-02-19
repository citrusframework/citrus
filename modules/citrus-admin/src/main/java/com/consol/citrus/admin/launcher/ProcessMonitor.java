package com.consol.citrus.admin.launcher;

import java.util.List;
import java.util.Set;

/**
 * Acts as a activity monitor for interacting and querying active processes.
 * <br />
 * When a process starts it's registered with the ProcessMonitor. On completion it is removed.
 *
 * @author Martin.Maher@consol.de
 * @since 2013.02.08
 */
public interface ProcessMonitor {

    /**
     * Adds a new process to the monitor. This is usually invoked when the process begins execution.
     *
     * @param processLauncher the process to be added
     */
    void add(ProcessLauncher processLauncher);

    /**
     * Removes the process from the process monitor. This is usually invoked when the process completes execution.
     *
     * @param processLauncher the process to be removed
     */
    void remove(ProcessLauncher processLauncher);

    /**
     * Returns the IDs of all active processes.
     * @return
     */
    Set<String> getProcessIds();

    /**
     * Used for terminating a process.
     *
     * If the supplied processId is unknown or the corresponding process has already terminated then no exception is
     * thrown.
     *
     * @param processId
     */
    void stopProcess(String processId);

    /**
     * Used for terminating all active process.
     */
    void stopAllProcesses();
}
