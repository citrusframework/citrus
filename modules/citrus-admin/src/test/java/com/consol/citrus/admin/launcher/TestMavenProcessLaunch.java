package com.consol.citrus.admin.launcher;

import com.consol.citrus.admin.launcher.process.ExecuteAllTests;
import com.consol.citrus.admin.launcher.process.ExecuteSingleTest;
import com.consol.citrus.admin.launcher.process.RebuildProject;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Used for *manually* testing the process launcher for maven processes
 *
 * @author Martin.Maher@consol.de
 * @version $Id$
 * @since 2012.11.30
 */
public class TestMavenProcessLaunch {

    // adapt this accordingly
    private static final File PROJECT_ROOT_DIR = new File("/Users/maherm/dev/projects/internal/citrus/modules/citrus-integration");

    private static ProcessMonitor processMonitor = new ProcessMonitorImpl();

    public static void main(final String[] args) throws IOException, InterruptedException {
        rebuildProject(0);

        allTests(30);

        singleTest(30);
    }

    private static ProcessLauncher singleTest(int maxExecutionTimeSeconds) throws InterruptedException {
        ProcessBuilder pb = new ExecuteSingleTest(PROJECT_ROOT_DIR, "CreateVariablesITest").getProcessBuilder();
        ProcessListener pli = getProcessListener();
        ProcessLauncherImpl pla = new ProcessLauncherImpl(processMonitor, "CreateVariablesITest");
        pla.addProcessListener(pli);
        pla.launchAndWait(pb, maxExecutionTimeSeconds);
        return pla;
    }

    private static ProcessLauncher allTests(int maxExecutionTimeSeconds) throws InterruptedException {
        ProcessBuilder pb = new ExecuteAllTests(PROJECT_ROOT_DIR).getProcessBuilder();
        ProcessListener pli = getProcessListener();
        ProcessLauncherImpl pla = new ProcessLauncherImpl(processMonitor, "all-tests");
        pla.addProcessListener(pli);
        pla.launchAndWait(pb, maxExecutionTimeSeconds);
        return pla;
    }

    private static ProcessLauncher rebuildProject(int maxExecutionTimeSeconds) throws InterruptedException {
        ProcessBuilder pb = new RebuildProject(PROJECT_ROOT_DIR).getProcessBuilder();
        ProcessListener pli = getProcessListener();
        ProcessLauncherImpl pla = new ProcessLauncherImpl(processMonitor, "rebuild");
        pla.addProcessListener(pli);
        pla.launchAndWait(pb, maxExecutionTimeSeconds);
        return pla;
    }

    private static ProcessListener getProcessListener() {
        return new ProcessListener() {
            public void start(String processId) {
                System.out.println("Starting:" + processId + ", " + new Date());
            }

            public void success(String processId) {
                System.out.println("Success:" + processId);
            }

            public void fail(String processId, int exitCode) {
                System.err.println("Failed:" + processId + ", errorCode:" + exitCode);
            }

            public void fail(String processId, Exception e) {
                System.err.println("Failed:" + processId + ", ex:" + e.getLocalizedMessage());
                e.printStackTrace();
            }

            public void output(String processId, String output) {
                System.out.println(processId + ":" + output);
            }
        };
    }

}
