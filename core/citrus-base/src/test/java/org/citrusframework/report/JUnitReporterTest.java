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

package org.citrusframework.report;

import java.io.File;
import java.time.Duration;

import org.testng.annotations.Test;

import static java.lang.String.format;
import static org.citrusframework.TestResult.failed;
import static org.citrusframework.TestResult.skipped;
import static org.citrusframework.TestResult.success;
import static org.citrusframework.util.FileUtils.readToString;
import static org.citrusframework.util.TestUtils.normalizeLineEndings;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @since 2.7.5
 */
public class JUnitReporterTest {

    private final JUnitReporter reporter = new JUnitReporter();

    @Test
    public void testGenerateTestResults() throws Exception {
        TestResults testResults = new TestResults();
        testResults.addResult(success("fooTest", JUnitReporterTest.class.getName()).withDuration(Duration.ofMillis(100)));
        reporter.generate(testResults);

        String reportFile = readToString(new File(reporter.getReportDirectory() + File.separator + reporter.getOutputDirectory() + File.separator + format(reporter.getReportFileNamePattern(), JUnitReporterTest.class.getName())));
        String testSuiteFile = readToString(new File(reporter.getReportDirectory() + File.separator + format(reporter.getReportFileNamePattern(), reporter.getSuiteName())));

        assertEquals(
                normalizeLineEndings(reportFile),
                """
                        <?xml version="1.0" encoding="UTF-8"?>
                        <testsuite name="org.citrusframework.report.JUnitReporterTest" time="0.100" tests="1" errors="0" skipped="0" failures="0">
                            <testcase name="fooTest" classname="org.citrusframework.report.JUnitReporterTest" time="0.100"/>
                        </testsuite>"""
        );

        assertEquals(
                normalizeLineEndings(testSuiteFile),
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<testsuite name=\"" + reporter.getSuiteName() + "\" time=\"0.100\" tests=\"1\" errors=\"0\" skipped=\"0\" failures=\"0\">\n" +
                        "    <testcase name=\"fooTest\" classname=\"org.citrusframework.report.JUnitReporterTest\" time=\"0.100\"/>\n" +
                        "</testsuite>"
        );
    }

    @Test
    public void testGenerateTestResultsMultipleTests() throws Exception {
        TestResults testResults = new TestResults();
        testResults.addResult(success("fooTest", JUnitReporterTest.class.getName()).withDuration(Duration.ofMillis(123)));
        testResults.addResult(success("barTest", JUnitReporterTest.class.getName()).withDuration(Duration.ofMillis(2500)));
        reporter.generate(testResults);

        String reportFile = readToString(new File(reporter.getReportDirectory() + File.separator + reporter.getOutputDirectory() + File.separator + format(reporter.getReportFileNamePattern(), JUnitReporterTest.class.getName())));

        assertEquals(
                normalizeLineEndings(reportFile),
                """
                        <?xml version="1.0" encoding="UTF-8"?>
                        <testsuite name="org.citrusframework.report.JUnitReporterTest" time="2.623" tests="2" errors="0" skipped="0" failures="0">
                            <testcase name="fooTest" classname="org.citrusframework.report.JUnitReporterTest" time="0.123"/>
                            <testcase name="barTest" classname="org.citrusframework.report.JUnitReporterTest" time="2.500"/>
                        </testsuite>""");
    }

    @Test
    public void testGenerateTestResultsWithFailedTests() throws Exception {
        TestResults testResults = new TestResults();
        testResults.addResult(success("fooTest", JUnitReporterTest.class.getName()).withDuration(Duration.ofMillis(100)));
        testResults.addResult(failed("barTest", JUnitReporterTest.class.getName(), new NullPointerException("Something went wrong!")).withDuration(Duration.ofMillis(200)));
        reporter.generate(testResults);

        String reportFile = readToString(new File(reporter.getReportDirectory() + File.separator + reporter.getOutputDirectory() + File.separator + format(reporter.getReportFileNamePattern(), JUnitReporterTest.class.getName())));
        String testSuiteFile = readToString(new File(reporter.getReportDirectory() + File.separator + format(reporter.getReportFileNamePattern(), reporter.getSuiteName())));

        assertTrue(
                normalizeLineEndings(reportFile)
                        .startsWith(
                                """
                                        <?xml version="1.0" encoding="UTF-8"?>
                                        <testsuite name="org.citrusframework.report.JUnitReporterTest" time="0.300" tests="2" errors="0" skipped="0" failures="1">
                                            <testcase name="fooTest" classname="org.citrusframework.report.JUnitReporterTest" time="0.100"/>
                                            <testcase name="barTest" classname="org.citrusframework.report.JUnitReporterTest" time="0.200">
                                              <failure type="java.lang.NullPointerException" message="Something went wrong!">
                                                <![CDATA[
                                                java.lang.NullPointerException: Something went wrong!"""
                        )
        );

        assertTrue(testSuiteFile.contains("<testsuite name=\"" + reporter.getSuiteName() + "\""));
        assertTrue(testSuiteFile.contains("tests=\"2\" errors=\"0\" skipped=\"0\" failures=\"1\""));
        assertTrue(testSuiteFile.contains("<failure type=\"java.lang.NullPointerException\" message=\"Something went wrong!\">"));
    }

    @Test
    public void testGenerateTestResultsWithSkippedTests() throws Exception {
        TestResults testResults = new TestResults();
        testResults.addResult(success("fooTest", JUnitReporterTest.class.getName()).withDuration(Duration.ofMillis(100)));
        testResults.addResult(skipped("barTest", JUnitReporterTest.class.getName()));
        reporter.generate(testResults);

        String reportFile = readToString(new File(reporter.getReportDirectory() + File.separator + reporter.getOutputDirectory() + File.separator + format(reporter.getReportFileNamePattern(), JUnitReporterTest.class.getName())));

        assertEquals(
                normalizeLineEndings(reportFile),
                """
                        <?xml version="1.0" encoding="UTF-8"?>
                        <testsuite name="org.citrusframework.report.JUnitReporterTest" time="0.100" tests="2" errors="0" skipped="1" failures="0">
                            <testcase name="fooTest" classname="org.citrusframework.report.JUnitReporterTest" time="0.100"/>
                            <testcase name="barTest" classname="org.citrusframework.report.JUnitReporterTest" time="0.000"/>
                        </testsuite>""");
    }

    @Test
    public void testGenerateTestResultsWithFailedTestsWithInvalidXMLChars() throws Exception {
        TestResults testResults = new TestResults();
        testResults.addResult(success("foo\"Test", JUnitReporterTest.class.getName()).withDuration(Duration.ofMillis(500)));
        testResults.addResult(failed("bar\"Test", JUnitReporterTest.class.getName(), new NullPointerException("Something \"went wrong!")).withDuration(Duration.ofMillis(600)));
        reporter.generate(testResults);

        String reportFile = readToString(new File(reporter.getReportDirectory() + File.separator + reporter.getOutputDirectory() + File.separator + format(reporter.getReportFileNamePattern(), JUnitReporterTest.class.getName())));
        String testSuiteFile = readToString(new File(reporter.getReportDirectory() + File.separator + format(reporter.getReportFileNamePattern(), reporter.getSuiteName())));

        assertTrue(
                normalizeLineEndings(reportFile)
                        .startsWith(
                                """
                                        <?xml version="1.0" encoding="UTF-8"?>
                                        <testsuite name="org.citrusframework.report.JUnitReporterTest" time="1.100" tests="2" errors="0" skipped="0" failures="1">
                                            <testcase name="foo&quot;Test" classname="org.citrusframework.report.JUnitReporterTest" time="0.500"/>
                                            <testcase name="bar&quot;Test" classname="org.citrusframework.report.JUnitReporterTest" time="0.600">
                                              <failure type="java.lang.NullPointerException" message="Something &quot;went wrong!">
                                                <![CDATA[
                                                java.lang.NullPointerException: Something "went wrong!"""
                        )
        );

        assertTrue(testSuiteFile.contains("<testsuite name=\"" + reporter.getSuiteName() + "\""));
        assertTrue(testSuiteFile.contains("tests=\"2\" errors=\"0\" skipped=\"0\" failures=\"1\""));
        assertTrue(testSuiteFile.contains("<failure type=\"java.lang.NullPointerException\" message=\"Something &quot;went wrong!\">"));
    }

    @Test
    public void testGenerateTestResultsWithFailedTestsWhenFailureTypeAndFailureStackAreNull() throws Exception {
        TestResults testResults = new TestResults();
        testResults.addResult(success("fooTest", JUnitReporterTest.class.getName()).withDuration(Duration.ofMillis(5500)));
        testResults.addResult(failed("barTest", JUnitReporterTest.class.getName(), "Something went wrong!").withDuration(Duration.ofMillis(6500)));
        reporter.generate(testResults);

        String reportFile = readToString(new File(reporter.getReportDirectory() + File.separator + reporter.getOutputDirectory() + File.separator + format(reporter.getReportFileNamePattern(), JUnitReporterTest.class.getName())));
        String testSuiteFile = readToString(new File(reporter.getReportDirectory() + File.separator + format(reporter.getReportFileNamePattern(), reporter.getSuiteName())));

        assertTrue(
                normalizeLineEndings(reportFile)
                        .startsWith(
                                """
                                        <?xml version="1.0" encoding="UTF-8"?>
                                        <testsuite name="org.citrusframework.report.JUnitReporterTest" time="12.000" tests="2" errors="0" skipped="0" failures="1">
                                            <testcase name="fooTest" classname="org.citrusframework.report.JUnitReporterTest" time="5.500"/>
                                            <testcase name="barTest" classname="org.citrusframework.report.JUnitReporterTest" time="6.500">
                                              <failure type="" message="Something went wrong!">"""
                        )
        );

        assertTrue(testSuiteFile.contains("<testsuite name=\"" + reporter.getSuiteName() + "\""));
        assertTrue(testSuiteFile.contains("tests=\"2\" errors=\"0\" skipped=\"0\" failures=\"1\""));
        assertTrue(testSuiteFile.contains("<failure type=\"\" message=\"Something went wrong!\">"));
    }
}
