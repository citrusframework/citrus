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

import com.consol.citrus.admin.launcher.process.ExecuteCommand;
import org.apache.commons.lang.SystemUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Tests the process launcher functionality.
 *
 * This test is more an integration test than a unit test, since it tests the threading behaviour, process monitor
 * interaction as well.
 *
 * @author Martin.Maher@consol.de
 * @since 2013.01.25
 */
public class ProcessLauncherTest {

    private final static int STARTED = 0;
    private final static int SUCCESS = 1;
    private final static int FAILED_EXIT_CODE = 2;
    private final static int FAILED_EXCEPTION = 3;

    private List<Boolean> callbacks = Arrays.asList(new Boolean[]{false, false, false, false});

    private ProcessMonitor processMonitor;

    @BeforeMethod
    public void setUp() throws Exception {
        processMonitor = new ProcessMonitorImpl();
    }

    @Test
    public void testSyncSuccess_noMaxExecutionTime() throws Exception {
        ProcessBuilder pb = getSleepProcessBuilder(1);
        ProcessLauncher pl = launchAndWait(pb, "sync-success-no-timeout");
        assertSuccess(pl);
    }

    @Test
    public void testSyncSuccess_withMaxExecutionTime() throws Exception {
        ProcessBuilder pb = getSleepProcessBuilder(1);
        ProcessLauncher pl = launchAndWait(pb, "sync-success-with-timeout", 5);
        assertSuccess(pl);
    }

    @Test
    public void testSyncFailed_exceededMaxExecutionTime() throws Exception {
        ProcessBuilder pb = getSleepProcessBuilder(5);
        ProcessLauncher pl = launchAndWait(pb, "sync-failed-timeout", 2);
        assertFailed(pl);
    }

    @Test
    public void testAsyncSuccess_noMaxExecutionTime() throws Exception {
        ProcessBuilder pb = getSleepProcessBuilder(3);
        ProcessLauncher pl = launchAndContinue(pb, "async-success-no-timeout");

        // check started
        Thread.sleep(1000);
        Assert.assertFalse(pl.isComplete());
        assertStarted(pl);

        // check completed successfully
        Thread.sleep(3000);
        assertSuccess(pl);

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
        assertStarted(pl);

        // check completed successfully
        Thread.sleep(2000);
        assertSuccess(pl);
    }

    @Test
    public void testAsyncFailed_exceededMaxExecutionTime() throws Exception {
        ProcessBuilder pb = getSleepProcessBuilder(5);
        ProcessLauncher pl = launchAndContinue(pb, "async-failed-timeout", 2);

        // check started
        Thread.sleep(1000);
        Assert.assertFalse(pl.isComplete());
        assertStarted(pl);

        // check failed
        Thread.sleep(5000);
        assertFailed(pl);
    }

    @Test
    public void testAsyncFailed_stopped() throws Exception {
        ProcessBuilder pb = getSleepProcessBuilder(5);
        ProcessLauncher pl = launchAndContinue(pb, "async-failed-stopped", 2);

        // check started
        Thread.sleep(1000);
        Assert.assertFalse(pl.isComplete());
        assertStarted(pl);

        // stop
        pl.stop();

        // check failed
        Thread.sleep(5000);
        assertFailed(pl);
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
        ProcessLauncherImpl pla = new ProcessLauncherImpl(processMonitor, processName);
        pla.addProcessListener(pli);
        pla.launchAndWait(processBuilder, maxExecutionTime);
        return pla;
    }

    private ProcessLauncher launchAndContinue(ProcessBuilder processBuilder, String processName) throws InterruptedException {
        return launchAndContinue(processBuilder, processName, 0);
    }

    private ProcessLauncher launchAndContinue(ProcessBuilder processBuilder, String processName, int maxExecutionTime) throws InterruptedException {
        ProcessListener pli = getProcessListener(callbacks);
        ProcessLauncherImpl pla = new ProcessLauncherImpl(processMonitor, processName);
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
            public void onProcessStart(String processId) {
                System.out.println("Starting:" + processId + ", " + new Date());
                callbacks.set(0, Boolean.TRUE);
            }

            public void onProcessSuccess(String processId) {
                System.out.println("Success:" + processId);
                callbacks.set(1, Boolean.TRUE);
            }

            public void onProcessFail(String processId, int exitCode) {
                System.err.println("Failed:" + processId + ", errorCode:" + exitCode);
                callbacks.set(2, Boolean.TRUE);
            }

            public void onProcessFail(String processId, Throwable e) {
                System.err.println("Failed:" + processId + ", ex:" + e.getLocalizedMessage());
                e.printStackTrace();
                callbacks.set(3, Boolean.TRUE);
            }

            public void onProcessOutput(String processId, String output) {
                //do nothing as activity was already printed
            }

            public void onProcessActivity(String processId, String output) {
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

    private void assertStarted(ProcessLauncher pl) {
        Assert.assertTrue(callbacks.get(STARTED));
        Assert.assertEquals(processMonitor.getProcessIds().size(), 1);
    }

    private void assertSuccess(ProcessLauncher pl) {
        Assert.assertTrue(callbacks.get(SUCCESS));
        Assert.assertTrue(pl.isComplete());
        Assert.assertTrue(processMonitor.getProcessIds().isEmpty());
    }

    private void assertFailed(ProcessLauncher pl) {
        Assert.assertTrue(callbacks.get(FAILED_EXIT_CODE) || callbacks.get(FAILED_EXCEPTION));
        Assert.assertTrue(pl.isComplete());
        Assert.assertTrue(processMonitor.getProcessIds().isEmpty());
    }

}
