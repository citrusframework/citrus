/*
 * Copyright 2006-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.admin.launcher;

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
