package com.consol.citrus.admin.launcher;

import java.io.File;
import java.util.*;

import org.apache.commons.lang.SystemUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.admin.launcher.process.ExecuteCommand;

/**
 * Tests the process launcher functionality
 *
 * @author Martin.Maher@consol.de
 * @version $Id$
 * @since 2013.01.25
 */
public class ProcessLauncherTest {

    private final static int STARTED = 0;
    private final static int SUCCESS = 1;
    private final static int FAILED_EXIT_CODE = 2;
    private final static int FAILED_EXCEPTION = 3;

    private List<Boolean> callbacks = Arrays.asList(new Boolean[]{false, false, false, false});

    @Test
    public void testSyncSuccess_noMaxExecutionTime() throws Exception {
        ProcessBuilder pb = getSleepProcessBuilder(1);
        ProcessLauncher pl = launchAndWait(pb, "sync-success-no-timeout");
        assertSuccess();
        Assert.assertTrue(pl.isComplete());
    }

    @Test
    public void testSyncSuccess_withMaxExecutionTime() throws Exception {
        ProcessBuilder pb = getSleepProcessBuilder(1);
        ProcessLauncher pl = launchAndWait(pb, "sync-success-with-timeout", 5);
        assertSuccess();
        Assert.assertTrue(pl.isComplete());
    }

    @Test
    public void testSyncFailed_exceededMaxExecutionTime() throws Exception {
        ProcessBuilder pb = getSleepProcessBuilder(5);
        ProcessLauncher pl = launchAndWait(pb, "sync-failed-timeout", 2);
        assertFailed();
        Assert.assertTrue(pl.isComplete());
    }

    @Test
    public void testAsyncSuccess_noMaxExecutionTime() throws Exception {
        ProcessBuilder pb = getSleepProcessBuilder(3);
        ProcessLauncher pl = launchAndContinue(pb, "async-success-no-timeout");

        // check started
        Thread.sleep(1000);
        Assert.assertFalse(pl.isComplete());
        assertStarted();

        // check completed successfully
        Thread.sleep(3000);
        assertSuccess();
        Assert.assertTrue(pl.isComplete());

        // check calling stop on stopped process is OK
        pl.stop();
        pl.stop();
    }

    @Test
    public void testAsyncSuccess_withMaxExecutionTime() throws Exception {
        ProcessBuilder pb = getSleepProcessBuilder(2);
        ProcessLauncher pl = launchAndContinue(pb, "async-success-with-timeout", 3);

        // check started
        Thread.sleep(1000);
        Assert.assertFalse(pl.isComplete());
        assertStarted();

        // check completed successfully
        Thread.sleep(2000);
        assertSuccess();
        Assert.assertTrue(pl.isComplete());
    }

    @Test
    public void testAsyncFailed_exceededMaxExecutionTime() throws Exception {
        ProcessBuilder pb = getSleepProcessBuilder(5);
        ProcessLauncher pl = launchAndContinue(pb, "async-failed-timeout", 2);

        // check started
        Thread.sleep(1000);
        Assert.assertFalse(pl.isComplete());
        assertStarted();

        // check failed
        Thread.sleep(5000);
        assertFailed();
        Assert.assertTrue(pl.isComplete());
    }

    @Test
    public void testAsyncFailed_stopped() throws Exception {
        ProcessBuilder pb = getSleepProcessBuilder(5);
        ProcessLauncher pl = launchAndContinue(pb, "async-failed-stopped", 2);

        // check started
        Thread.sleep(1000);
        Assert.assertFalse(pl.isComplete());
        assertStarted();

        // stop
        pl.stop();

        // check failed
        Thread.sleep(5000);
        assertFailed();
        Assert.assertTrue(pl.isComplete());
    }

    private ProcessBuilder getSleepProcessBuilder(int sleepInSeconds) throws InterruptedException {
        String command;
        if (SystemUtils.IS_OS_UNIX) {
            command = String.format("ping -c %s 127.0.0.1", sleepInSeconds);
        } else {
            command = String.format("ping -n %s 127.0.0.1", sleepInSeconds);
        }
        return new ExecuteCommand(command, new File(System.getProperty("user.dir"))).getProcessBuilder();
    }

    private ProcessLauncher launchAndWait(ProcessBuilder processBuilder, String processName) throws InterruptedException {
        return launchAndWait(processBuilder, processName, 0);
    }

    private ProcessLauncher launchAndWait(ProcessBuilder processBuilder, String processName, int maxExecutionTime) throws InterruptedException {
        ProcessListener pli = getProcessListener(callbacks);
        ProcessLauncherImpl pla = new ProcessLauncherImpl(processName);
        pla.addProcessListener(pli);
        pla.launchAndWait(processBuilder, maxExecutionTime);
        return pla;
    }

    private ProcessLauncher launchAndContinue(ProcessBuilder processBuilder, String processName) throws InterruptedException {
        return launchAndContinue(processBuilder, processName, 0);
    }

    private ProcessLauncher launchAndContinue(ProcessBuilder processBuilder, String processName, int maxExecutionTime) throws InterruptedException {
        ProcessListener pli = getProcessListener(callbacks);
        ProcessLauncherImpl pla = new ProcessLauncherImpl(processName);
        pla.addProcessListener(pli);
        pla.launchAndContinue(processBuilder, maxExecutionTime);
        return pla;
    }

    private ProcessListener getProcessListener(final List<Boolean> callbacks) {
        // reset callbacks
        for (int i = 0; i < callbacks.size(); i++) {
            callbacks.set(i, false);
        }

        return new ProcessListener() {
            public void start(String processId) {
                System.out.println("Starting:" + processId + ", " + new Date());
                callbacks.set(0, Boolean.TRUE);
            }

            public void success(String processId) {
                System.out.println("Success:" + processId);
                callbacks.set(1, Boolean.TRUE);
            }

            public void fail(String processId, int exitCode) {
                System.err.println("Failed:" + processId + ", errorCode:" + exitCode);
                callbacks.set(2, Boolean.TRUE);
            }

            public void fail(String processId, Exception e) {
                System.err.println("Failed:" + processId + ", ex:" + e.getLocalizedMessage());
                e.printStackTrace();
                callbacks.set(3, Boolean.TRUE);
            }

            public void stop(String processId) {
                System.out.println("Stopped:" + processId);
                callbacks.set(4, Boolean.TRUE);
            }

            public void output(String processId, String output) {
                System.out.println(processId + ":" + output);
            }
        };
    }

    private void assertCallbacks(Boolean expectStart, Boolean expectSuccess, Boolean expectFailCode, Boolean expectFailEx) {
        Assert.assertEquals(callbacks.get(STARTED), expectStart);
        Assert.assertEquals(callbacks.get(SUCCESS), expectSuccess);
        Assert.assertEquals(callbacks.get(FAILED_EXIT_CODE), expectFailCode);
        Assert.assertEquals(callbacks.get(FAILED_EXCEPTION), expectFailEx);
    }

    private void assertStarted() {
        Assert.assertTrue(callbacks.get(STARTED));
    }

    private void assertSuccess() {
        Assert.assertTrue(callbacks.get(SUCCESS));
    }

    private void assertFailedWithErrorCode() {
        Assert.assertTrue(callbacks.get(FAILED_EXIT_CODE));
    }

    private void assertFailedWithException() {
        Assert.assertTrue(callbacks.get(FAILED_EXCEPTION));
    }

    private void assertFailed() {
        Assert.assertTrue(callbacks.get(FAILED_EXIT_CODE) || callbacks.get(FAILED_EXCEPTION));
    }

}
