/*
 * Copyright the original author or authors.
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

package org.citrusframework.jbang.cli.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import org.citrusframework.CitrusInstanceManager;
import org.citrusframework.jbang.cli.CitrusJBangMain;
import org.citrusframework.spi.Resources;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

@ResourceLock("citrus-jbang-run")
public class RunTest extends CommandTest {

    @Test
    public void shouldRunJavaTest() {
        Path source = Resources.fromClasspath("runnable/SampleIT.java").file().toPath();

        CitrusJBangMain main = createCitrusJBangMain();
        main.execute("run", source.toString());
    }

    @Test
    public void shouldRunYamlTest() {
        Path source = Resources.fromClasspath("runnable/yaml-sample.citrus.it.yaml").file().toPath();

        CitrusJBangMain main = createCitrusJBangMain();
        main.execute("run", source.toString());
    }

    @Test
    public void shouldRunXmlTest() {
        Path source = Resources.fromClasspath("runnable/xml-sample.citrus.it.xml").file().toPath();

        CitrusJBangMain main = createCitrusJBangMain();
        main.execute("run", source.toString());
    }

    @Test
    public void shouldRunGroovyTest() {
        Path source = Resources.fromClasspath("runnable/groovy-sample.citrus.it.groovy").file().toPath();

        CitrusJBangMain main = createCitrusJBangMain();
        main.execute("run", source.toString());
    }

    @Test
    public void shouldRunCucumberTest() {
        Path source = Resources.fromClasspath("runnable/cucumber-sample.citrus.it.feature").file().toPath();

        CitrusJBangMain main = createCitrusJBangMain();
        main.execute("run", source.toString());
    }

    @Test
    public void shouldRunMultiTestYamlFile() {
        useNewCitrusInstance();

        Path source = Resources.fromClasspath("multi/multi-sample.citrus.it.yaml").file().toPath();

        CitrusJBangMain main = createCitrusJBangMain();
        main.execute("run", source.toString());
    }

    @Test
    public void shouldFailWhenTestOfMultiTestFileFails() {
        useNewCitrusInstance();

        Path source = Resources.fromClasspath("multi/multi-failing-test.citrus.it.yaml").file().toPath();

        CitrusJBangMain main = createCitrusJBangMain(1);
        main.execute("run", source.toString());
    }

    @Test
    public void shouldFailWhenAfterAllOfMultiTestFileFails() {
        useNewCitrusInstance();

        Path source = Resources.fromClasspath("multi/multi-failing-after-all.citrus.it.yaml").file().toPath();

        CitrusJBangMain main = createCitrusJBangMain(1);
        main.execute("run", source.toString());
    }

    @Test
    public void shouldRunValidFileNextToBrokenMultiTestFile() {
        useNewCitrusInstance();

        Path valid = Resources.fromClasspath("multi/multi-sample.citrus.it.yaml").file().toPath();
        Path broken = Resources.fromClasspath("multi/multi-empty.citrus.it.yaml").file().toPath();

        CitrusJBangMain main = createCitrusJBangMain(1);
        main.execute("run", valid.toString(), broken.toString());
    }

    @Test
    public void shouldRunAllTest() {
        Path source = Resources.fromClasspath("runnable").file().toPath();

        CitrusJBangMain main = createCitrusJBangMain();
        main.execute("run", source.toString());
    }

    @Test
    public void shouldKeepWorkDirectoryAcrossRuns() throws IOException {
        Path source = Resources.fromClasspath("runnable/yaml-sample.citrus.it.yaml").file().toPath();
        Path workDir = workingDir.resolve("keep-work-dir");
        Path reportDirectory = workDir.resolve("citrus-reports");

        deleteRecursively(workDir);

        String previousWorkDir = System.getProperty("citrus.jbang.work.dir");
        String previousReportDirectory = System.getProperty("citrus.jbang.report.directory");
        String previousCitrusReportDirectory = System.getProperty("citrus.report.directory");

        try {
            System.setProperty("citrus.jbang.work.dir", workDir.toString());
            System.setProperty("citrus.jbang.report.directory", reportDirectory.toString());
            System.setProperty("citrus.report.directory", reportDirectory.toString());

            CitrusJBangMain main = createCitrusJBangMain();
            main.execute("run", source.toString());

            List<Path> flowReports = listFlowReports(reportDirectory);
            Assertions.assertFalse(flowReports.isEmpty(), "Expected at least one flow report after the first run");
            int firstRunCount = flowReports.size();

            Path sentinel = Files.writeString(workDir.resolve("sentinel.txt"), "sentinel");

            main = createCitrusJBangMain();
            main.execute("run", source.toString());

            Assertions.assertTrue(Files.exists(sentinel), "Expected the work directory to be kept intact between runs");

            List<Path> flowReportsAfterSecondRun = listFlowReports(reportDirectory);
            Assertions.assertTrue(flowReportsAfterSecondRun.size() > firstRunCount, "Expected additional flow reports after the second run");
        } finally {
            restoreProperty("citrus.jbang.work.dir", previousWorkDir);
            restoreProperty("citrus.jbang.report.directory", previousReportDirectory);
            restoreProperty("citrus.report.directory", previousCitrusReportDirectory);
        }
    }

    @Test
    public void shouldClearWorkDirectoryWithClearOption() throws IOException {
        Path source = Resources.fromClasspath("runnable/yaml-sample.citrus.it.yaml").file().toPath();
        Path workDir = workingDir.resolve("clear-work-dir");
        Path reportDirectory = workDir.resolve("citrus-reports");

        deleteRecursively(workDir);

        String previousWorkDir = System.getProperty("citrus.jbang.work.dir");
        String previousReportDirectory = System.getProperty("citrus.jbang.report.directory");
        String previousCitrusReportDirectory = System.getProperty("citrus.report.directory");

        try {
            System.setProperty("citrus.jbang.work.dir", workDir.toString());
            System.setProperty("citrus.jbang.report.directory", reportDirectory.toString());
            System.setProperty("citrus.report.directory", reportDirectory.toString());

            CitrusJBangMain main = createCitrusJBangMain();
            main.execute("run", source.toString());

            List<Path> flowReports = listFlowReports(reportDirectory);
            Assertions.assertFalse(flowReports.isEmpty(), "Expected at least one flow report after the first run");

            Path sentinel = Files.writeString(workDir.resolve("sentinel.txt"), "sentinel");

            main = createCitrusJBangMain();
            main.execute("run", "--clear", source.toString());

            Assertions.assertFalse(Files.exists(sentinel), "Expected --clear to remove the work directory");
        } finally {
            restoreProperty("citrus.jbang.work.dir", previousWorkDir);
            restoreProperty("citrus.jbang.report.directory", previousReportDirectory);
            restoreProperty("citrus.report.directory", previousCitrusReportDirectory);
        }
    }

    /**
     * The exit status reporter of a run attaches to new Citrus instances only. A run in a fresh JVM always creates
     * one; in this shared test JVM an earlier run may have left its instance behind.
     */
    private static void useNewCitrusInstance() {
        CitrusInstanceManager.reset();
    }

    private static List<Path> listFlowReports(Path reportDirectory) throws IOException {
        if (!Files.isDirectory(reportDirectory)) {
            return List.of();
        }

        try (var paths = Files.list(reportDirectory)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString()
                            .matches(".*-flow-\\d+(-\\d+)?\\.(json|yaml)"))
                    .sorted().toList();
        }
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (Files.notExists(path)) {
            return;
        }

        try (var files = Files.walk(path)) {
            for (Path file : files.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(file);
            }
        }
    }

    private static void restoreProperty(String propertyName, String previousValue) {
        if (previousValue == null) {
            System.clearProperty(propertyName);
        } else {
            System.setProperty(propertyName, previousValue);
        }
    }
}
