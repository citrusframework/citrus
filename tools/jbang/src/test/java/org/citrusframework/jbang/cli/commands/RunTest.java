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

import org.citrusframework.jbang.cli.CitrusJBangMain;
import org.citrusframework.spi.Resources;
import org.testng.Assert;
import org.testng.annotations.Test;

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
    public void shouldRunAllTest() {
        Path source = Resources.fromClasspath("runnable").file().toPath();

        CitrusJBangMain main = createCitrusJBangMain();
        main.execute("run", source.toString());
    }

    @Test
    public void shouldKeepFlowReportHistoryAcrossRuns() throws IOException {
        Path source = Resources.fromClasspath("runnable/yaml-sample.citrus.it.yaml").file().toPath();
        Path workDir = workingDir.resolve("flow-report-history");
        Path reportDirectory = workDir.resolve("citrus-reports");

        deleteRecursively(workDir);

        String previousWorkDir = System.getProperty("citrus.jbang.work.dir");
        String previousReportDirectory = System.getProperty("citrus.jbang.report.directory");
        String previousCitrusReportDirectory = System.getProperty("citrus.report.directory");

        try {
            System.setProperty("citrus.jbang.work.dir", workDir.toString());
            System.setProperty("citrus.jbang.report.directory", reportDirectory.toString());

            CitrusJBangMain main = createCitrusJBangMain();
            main.execute("run", source.toString());

            List<Path> runLocations = listRunLocations(reportDirectory);
            Assert.assertEquals(runLocations.size(), 1, "Expected one run location after the first run");
            Path firstRunLocation = runLocations.get(0);
            Assert.assertTrue(containsFlowReport(firstRunLocation), "Expected flow reports in the first run location");

            Path sentinel = Files.writeString(workDir.resolve("sentinel.txt"), "sentinel");

            main = createCitrusJBangMain();
            main.execute("run", source.toString());

            Assert.assertFalse(Files.exists(sentinel), "Expected the next run to clean up the JBang work directory");
            Assert.assertTrue(Files.exists(firstRunLocation), "Expected the previous run location to be preserved");
            Assert.assertTrue(containsFlowReport(firstRunLocation), "Expected flow reports of the previous run to be preserved");

            List<Path> runLocationsAfterSecondRun = listRunLocations(reportDirectory);
            Assert.assertEquals(runLocationsAfterSecondRun.size(), 2, "Expected a second run location after the second run");
        } finally {
            restoreProperty("citrus.jbang.work.dir", previousWorkDir);
            restoreProperty("citrus.jbang.report.directory", previousReportDirectory);
            restoreProperty("citrus.report.directory", previousCitrusReportDirectory);
        }
    }

    private static boolean containsFlowReport(Path runLocation) throws IOException {
        try (var files = Files.list(runLocation)) {
            return files.anyMatch(file -> file.getFileName().toString().endsWith("-flow.json"));
        }
    }

    private static List<Path> listRunLocations(Path reportDirectory) throws IOException {
        if (!Files.isDirectory(reportDirectory)) {
            return List.of();
        }

        try (var paths = Files.list(reportDirectory)) {
            return paths.filter(Files::isDirectory)
                    .filter(path -> path.getFileName().toString()
                            .matches("\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}(-\\d+)?"))
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
