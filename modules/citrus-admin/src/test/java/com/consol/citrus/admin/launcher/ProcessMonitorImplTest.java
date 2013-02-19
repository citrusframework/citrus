package com.consol.citrus.admin.launcher;

import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Set;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.testng.Assert.fail;

/**
 * @author Martin.Maher@consol.de
 * @since 2013.02.08
 */
public class ProcessMonitorImplTest {
    private ProcessMonitor testling;
    private ProcessLauncher processLauncherMock1;
    private ProcessLauncher processLauncherMock2;

    @BeforeMethod
    public void setUp() throws Exception {
        processLauncherMock1 = EasyMock.createMock(ProcessLauncher.class);
        processLauncherMock2 = EasyMock.createMock(ProcessLauncher.class);
        testling = new ProcessMonitorImpl();
    }

    private void replayMocks() {
        replay(
                processLauncherMock1,
                processLauncherMock2
        );
    }

    private void verifyMocks() {
        verify(
                processLauncherMock1,
                processLauncherMock2
        );
    }

    @Test
    public void testAdd() throws Exception {
        expect(processLauncherMock1.getProcessId()).andReturn("p1").anyTimes();
        expect(processLauncherMock2.getProcessId()).andReturn("p2").anyTimes();

        replayMocks();

        Set<String> processIds = testling.getProcessIds();
        Assert.assertEquals(processIds.size(), 0);

        testling.add(processLauncherMock1);
        processIds = testling.getProcessIds();
        Assert.assertEquals(processIds.size(), 1);

        testling.add(processLauncherMock2);
        processIds = testling.getProcessIds();
        Assert.assertEquals(processIds.size(), 2);

        verifyMocks();
    }

    @Test
    public void testAdd_duplicate() throws Exception {
        expect(processLauncherMock1.getProcessId()).andReturn("p1").anyTimes();
        expect(processLauncherMock2.getProcessId()).andReturn("p1").anyTimes();

        replayMocks();

        Set<String> processIds = testling.getProcessIds();
        Assert.assertEquals(processIds.size(), 0);

        testling.add(processLauncherMock1);
        processIds = testling.getProcessIds();
        Assert.assertEquals(processIds.size(), 1);

        try {
            testling.add(processLauncherMock2);
            fail();
        }
        catch (ProcessLauncherException e) {
            processIds = testling.getProcessIds();
            Assert.assertEquals(processIds.size(), 1);
        }

        verifyMocks();
    }

    @Test
    public void testRemove() throws Exception {
        expect(processLauncherMock1.getProcessId()).andReturn("p1").anyTimes();

        replayMocks();

        Set<String> processIds = null;

        testling.add(processLauncherMock1);
        processIds = testling.getProcessIds();
        Assert.assertEquals(processIds.size(), 1);

        testling.remove(processLauncherMock1);
        processIds = testling.getProcessIds();
        Assert.assertEquals(processIds.size(), 0);

        testling.remove(processLauncherMock1);
        processIds = testling.getProcessIds();
        Assert.assertEquals(processIds.size(), 0);

        verifyMocks();
    }

    @Test
    public void testStopProcess() throws Exception {
        expect(processLauncherMock1.getProcessId()).andReturn("p1").anyTimes();
        processLauncherMock1.stop();

        replayMocks();

        Set<String> processIds = null;

        // stop known process
        testling.add(processLauncherMock1);
        testling.stopProcess("p1");

        // stop unknown process
        testling.stopProcess("p2");

        verifyMocks();
    }

    @Test
    public void testStopAllProcesses() throws Exception {
        expect(processLauncherMock1.getProcessId()).andReturn("p1").anyTimes();
        expect(processLauncherMock2.getProcessId()).andReturn("p2").anyTimes();
        processLauncherMock1.stop();
        processLauncherMock2.stop();

        replayMocks();

        testling.add(processLauncherMock1);
        testling.add(processLauncherMock2);

        testling.stopAllProcesses();

        verifyMocks();

    }
}
