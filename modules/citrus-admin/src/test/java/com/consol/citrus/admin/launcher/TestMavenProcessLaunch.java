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

import com.consol.citrus.admin.configuration.MavenRunConfiguration;
import com.consol.citrus.admin.launcher.process.maven.MavenRebuildProjectCommand;
import com.consol.citrus.admin.launcher.process.maven.MavenRunTestsCommand;
import com.consol.citrus.admin.launcher.process.maven.MavenRunSingleTestCommand;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Used for *manually* testing the process launcher for maven processes
 *
 * @author Martin.Maher@consol.de
 */
public class TestMavenProcessLaunch {

    // adapt this accordingly
    private static final File PROJECT_ROOT_DIR = new File("/Users/christoph/Projekte/Citrus/citrus/modules/citrus-integration");

    private static ProcessMonitor processMonitor = new ProcessMonitorImpl();

    public static void main(final String[] args) throws IOException, InterruptedException {
        rebuildProject(0);

        allTests(30);

        singleTest(30);
    }

    private static ProcessLauncher singleTest(int maxExecutionTimeSeconds) throws InterruptedException {
        ProcessBuilder pb = new MavenRunSingleTestCommand(PROJECT_ROOT_DIR, "CreateVariablesITest", new MavenRunConfiguration()).getProcessBuilder();
        ProcessListener pli = getProcessListener();
        ProcessLauncherImpl pla = new ProcessLauncherImpl(processMonitor, "CreateVariablesITest");
        pla.addProcessListener(pli);
        pla.launchAndWait(pb, maxExecutionTimeSeconds);
        return pla;
    }

    private static ProcessLauncher allTests(int maxExecutionTimeSeconds) throws InterruptedException {
        ProcessBuilder pb = new MavenRunTestsCommand(PROJECT_ROOT_DIR, new MavenRunConfiguration()).getProcessBuilder();
        ProcessListener pli = getProcessListener();
        ProcessLauncherImpl pla = new ProcessLauncherImpl(processMonitor, "all-tests");
        pla.addProcessListener(pli);
        pla.launchAndWait(pb, maxExecutionTimeSeconds);
        return pla;
    }

    private static ProcessLauncher rebuildProject(int maxExecutionTimeSeconds) throws InterruptedException {
        ProcessBuilder pb = new MavenRebuildProjectCommand(PROJECT_ROOT_DIR, new MavenRunConfiguration()).getProcessBuilder();
        ProcessListener pli = getProcessListener();
        ProcessLauncherImpl pla = new ProcessLauncherImpl(processMonitor, "rebuild");
        pla.addProcessListener(pli);
        pla.launchAndWait(pb, maxExecutionTimeSeconds);
        return pla;
    }

    private static ProcessListener getProcessListener() {
        return new ProcessListener() {
            public void onProcessStart(String processId) {
                System.out.println("Starting:" + processId + ", " + new Date());
            }

            public void onProcessSuccess(String processId) {
                System.out.println("Success:" + processId);
            }

            public void onProcessFail(String processId, int exitCode) {
                System.err.println("Failed:" + processId + ", errorCode:" + exitCode);
            }

            public void onProcessFail(String processId, Throwable e) {
                System.err.println("Failed:" + processId + ", ex:" + e.getLocalizedMessage());
                e.printStackTrace();
            }

            public void onProcessOutput(String processId, String output) {
                //do nothing as activity was already printed
            }

            public void onProcessActivity(String processId, String output) {
                System.out.println(processId + ":" + output);
            }
        };
    }

}
