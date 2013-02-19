package com.consol.citrus.admin.launcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * {@inheritDoc}
 *
 * @author Martin.Maher@consol.de
 * @since 2012.11.30
 */
public class ProcessLauncherImpl implements ProcessLauncher {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessLauncherImpl.class);

    private boolean processCompleted;
    private String processId;
    private Process process;
    private ProcessMonitor processMonitor;
    private List<ProcessListener> processListeners = new ArrayList<ProcessListener>();

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

        Runnable runnable = new Runnable() {
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
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        notifyOutput(processId, line);
                        Thread.sleep(100);
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
        };
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    private void notifyStart(String processId) {
        for (ProcessListener processListener : processListeners) {
            processListener.start(processId);
        }
    }

    private void notifySuccess(String processId) {
        for (ProcessListener processListener : processListeners) {
            processListener.success(processId);
        }
    }

    private void notifyFail(String processId, int exitCode) {
        for (ProcessListener processListener : processListeners) {
            processListener.fail(processId, exitCode);
        }
    }

    private void notifyFail(String processId, Exception e) {
        for (ProcessListener processListener : processListeners) {
            processListener.fail(processId, e);
        }
    }

    private void notifyOutput(String processId, String output) {
        for (ProcessListener processListener : processListeners) {
            processListener.output(processId, output);
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
        final int SECOND_IN_MILLISECONDS = 1000;
        Timer timer = new Timer(true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                LOG.info(String.format("Stopping process as it didn't complete within the allocated time (%s seconds)", maxExecutionTimeSeconds));
                timedProcess.destroy();
            }
        };
        timer.schedule(task, maxExecutionTimeSeconds * SECOND_IN_MILLISECONDS);
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
