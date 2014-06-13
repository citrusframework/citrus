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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

/**
 * @author Martin.Maher@consol.de
 * @since 2012.11.30
 */
public class ProcessLauncherImpl implements ProcessLauncher {

    /** Logger */
    private static final Logger LOG = LoggerFactory.getLogger(ProcessLauncherImpl.class);
    private static final int LOG_CACHE_SIZE = 10;

    private boolean processCompleted;
    private String processId;
    private Process process;
    private ProcessMonitor processMonitor;

    /** Listeners get informed on process or test events */
    private List<ProcessListener> processListeners = new ArrayList<ProcessListener>();

    /**
     * Default constructor.
     * @param processMonitor
     * @param processId
     */
    public ProcessLauncherImpl(ProcessMonitor processMonitor, String processId) {
        this.processId = processId;
        this.processMonitor = processMonitor;
    }

    public String getProcessId() {
        return processId;
    }

    /**
     * {@inheritDoc}
     */
    public void launchAndWait(ProcessBuilder processBuilder, int maxExecutionTimeSeconds) {
        Thread thread = launch(processBuilder, maxExecutionTimeSeconds);
        try {
            thread.join();
        } catch (InterruptedException e) {
            LOG.error("Interrupted exception caught waiting for process to complete", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void launchAndContinue(ProcessBuilder processBuilder, int maxExecutionTimeSeconds) {
        launch(processBuilder, maxExecutionTimeSeconds);
    }

    /**
     * {@inheritDoc}
     */
    public void stop() {
        destroyProcess(process);
    }

    /**
     * {@inheritDoc}
     */
    public void addProcessListener(ProcessListener processListener) {
        this.processListeners.add(processListener);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isComplete() {
        return processCompleted;
    }

    /**
     * Launches the process, returning the thread the process is executing within
     *
     * @param processBuilder the process to be launched
     * @param maxExecutionTimeSeconds the maximum length time to allow the process to execute for
     * @return the thread the process is executing within
     */
    private Thread launch(final ProcessBuilder processBuilder, final int maxExecutionTimeSeconds) {
        notifyStart(processId);

        Thread thread = new Thread(new LaunchJob(maxExecutionTimeSeconds, processBuilder));
        thread.setDaemon(true);
        thread.start();
        return thread;
    }
    
    /**
     * Launch job runnable doing the process start magic.
     */
    private final class LaunchJob implements Runnable {
        private final int maxExecutionTimeSeconds;
        private final ProcessBuilder processBuilder;

        /**
         * @param maxExecutionTimeSeconds
         * @param processBuilder
         */
        private LaunchJob(int maxExecutionTimeSeconds,
                ProcessBuilder processBuilder) {
            this.maxExecutionTimeSeconds = maxExecutionTimeSeconds;
            this.processBuilder = processBuilder;
        }

        /**
         * {@inheritDoc}
         */
        public void run() {
            processCompleted = false;
            Timer timer = null;
            int result = 0;
            Exception caughtException = null;

            BufferedReader br = null;
            try {
                processBuilder.redirectErrorStream(true);
                LOG.info("Starting process: " + processBuilder.command());

                addProcessToMonitor();
                process = processBuilder.start();

                if (maxExecutionTimeSeconds > 0) {
                    // ensure the process doesn't exceed the max execution time
                    timer = startTimer(maxExecutionTimeSeconds, process);
                }

                // Read output
                br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder lineCache = new StringBuilder();
                int lineCacheSize = LOG_CACHE_SIZE;
                String line = null;
                while ((line = br.readLine()) != null) {
                    notifyActivity(processId, line);

                    lineCache.append(line);
                    lineCache.append(System.getProperty("line.separator"));

                    if (lineCacheSize > 0) {
                        lineCacheSize--;
                    } else {
                        notifyOutput(processId, lineCache.toString());
                        lineCacheSize = LOG_CACHE_SIZE;
                        lineCache = new StringBuilder();
                    }
                }

                if (lineCacheSize > 0) {
                    notifyOutput(processId, lineCache.toString());
                }

                // store result
                result = process.waitFor();
            } catch (Exception e) {
                caughtException = e;
            } finally {
                // check result
                if (caughtException != null) {
                    notifyFail(processId, caughtException);
                } else if (result == 0) {
                    notifySuccess(processId);
                } else {
                    notifyFail(processId, process.exitValue());
                }

                // tidy up
                close(br);
                destroyProcess(process);
                cancelTimer(timer);
                processCompleted = true;
                removeProcessFromMonitor();
            }
        }
    }

    private void notifyStart(String processId) {
        for (ProcessListener processListener : processListeners) {
            processListener.onProcessStart(processId);
        }
    }

    private void notifySuccess(String processId) {
        for (ProcessListener processListener : processListeners) {
            processListener.onProcessSuccess(processId);
        }
    }

    private void notifyFail(String processId, int exitCode) {
        for (ProcessListener processListener : processListeners) {
            processListener.onProcessFail(processId, exitCode);
        }
    }

    private void notifyFail(String processId, Exception e) {
        for (ProcessListener processListener : processListeners) {
            processListener.onProcessFail(processId, e);
        }
    }

    private void notifyOutput(String processId, String output) {
        for (ProcessListener processListener : processListeners) {
            processListener.onProcessOutput(processId, output);
        }
    }

    private void notifyActivity(String processId, String output) {
        for (ProcessListener processListener : processListeners) {
            processListener.onProcessActivity(processId, output);
        }
    }

    private void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                // ignore
                LOG.trace("Error closing source or destination", e);
            }
        }
    }

    private void destroyProcess(Process process) {
        if (process != null) {
            try {
                process.destroy();
            } catch (Exception e) {
                // ignore
                LOG.trace("Error destroying process", e);
            }
        }
    }

    private Timer startTimer(final long maxExecutionTimeSeconds, final Process timedProcess) {
        Timer timer = new Timer(true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                LOG.info(String.format("Stopping process as it didn't complete within the allocated time (%s seconds)", 
                        maxExecutionTimeSeconds));
                timedProcess.destroy();
            }
        };
        timer.schedule(task, maxExecutionTimeSeconds * 1000L);
        return timer;
    }

    private void cancelTimer(Timer timer) {
        if (timer != null) {
            timer.cancel();
        }
    }

    private void addProcessToMonitor() {
        this.processMonitor.add(this);
    }

    private void removeProcessFromMonitor() {
        this.processMonitor.remove(this);
    }

}
