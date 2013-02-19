package com.consol.citrus.admin.launcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@inheritDoc}
 *
 * @author Martin.Maher@consol.de
 * @since 2013.02.08
 */
public class ProcessMonitorImpl implements ProcessMonitor {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessMonitorImpl.class);

    private Map<String, ProcessLauncher> processMap = new ConcurrentHashMap<String, ProcessLauncher>();

    /**
     * Adds a new process to the monitor. This is usually invoked when the process begins execution.
     *
     * @param processLauncher the process to be added
     */
    public void add(ProcessLauncher processLauncher) {
        String id = processLauncher.getProcessId();
        if(processMap.containsKey(id)) {
            String msg = String.format("An active process already exists with the Id '%s'", id);
            LOG.error(msg);
            throw new ProcessLauncherException(msg);
        }
        processMap.put(id,processLauncher);
    }

    /**
     * Removes the process from the process monitor. This is usually invoked when the process completes execution.
     *
     * @param processLauncher the process to be removed
     */
    public void remove(ProcessLauncher processLauncher) {
        String id = processLauncher.getProcessId();
        processMap.remove(id);
    }

    /**
     * Returns the IDs of all active processes.
     *
     * @return
     */
    public Set<String> getProcessIds() {
        return processMap.keySet();
    }

    /**
     * Used for terminating a process.
     * <p/>
     * If the supplied processId is unknown or the corresponding process has already terminated then no exception is
     * thrown.
     *
     * @param processId
     */
    public void stopProcess(String processId) {
        if(processMap.containsKey(processId)) {
            processMap.get(processId).stop();
        }
    }

    /**
     * Used for terminating all active process.
     */
    public void stopAllProcesses() {
        for(Map.Entry<String,ProcessLauncher> entry: processMap.entrySet()) {
            entry.getValue().stop();
        }
    }
}
