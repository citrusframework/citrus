/*
 * Copyright 2006-2018 the original author or authors.
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

package com.consol.citrus.report;

import com.consol.citrus.TestResult;
import com.consol.citrus.util.FileUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Christoph Deppisch
 * @since 2.7.5
 */
public class JUnitReporterJunitTest {

    private JUnitReporter reporter = new JUnitReporter();

    @BeforeEach
    public void clearResults() {
        reporter.clearTestResults();
    }

    @Test
    public void testGenerateTestResults() throws Exception {
        reporter.getTestResults().addResult(TestResult.success("fooTest", JUnitReporterJunitTest.class.getName()));
        reporter.generateTestResults();

        String reportFile = FileUtils.readToString(new File(reporter.getReportDirectory() + File.separator + reporter.getOutputDirectory() + File.separator + String.format(reporter.getReportFileNamePattern(), JUnitReporterJunitTest.class.getName())));
        String testSuiteFile = FileUtils.readToString(new File(reporter.getReportDirectory() + File.separator + String.format(reporter.getReportFileNamePattern(), reporter.getSuiteName())));

        assertEquals(reportFile, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator() +
                "<testsuite name=\"com.consol.citrus.report.JUnitReporterJunitTest\" time=\"0.0\" tests=\"1\" errors=\"0\" skipped=\"0\" failures=\"0\">" + System.lineSeparator() +
                "    <testcase name=\"fooTest\" classname=\"com.consol.citrus.report.JUnitReporterJunitTest\" time=\"0.0\"/>" + System.lineSeparator() +
                "</testsuite>");

        assertEquals(testSuiteFile, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator() +
                "<testsuite name=\"" + reporter.getSuiteName() + "\" time=\"0.0\" tests=\"1\" errors=\"0\" skipped=\"0\" failures=\"0\">" + System.lineSeparator() +
                "    <testcase name=\"fooTest\" classname=\"com.consol.citrus.report.JUnitReporterJunitTest\" time=\"0.0\"/>" + System.lineSeparator() +
                "</testsuite>");
    }

    @Test
    public void testGenerateTestResultsMultipleTests() throws Exception {
        reporter.getTestResults().addResult(TestResult.success("fooTest", JUnitReporterJunitTest.class.getName()));
        reporter.getTestResults().addResult(TestResult.success("barTest", JUnitReporterJunitTest.class.getName()));
        reporter.generateTestResults();

        String reportFile = FileUtils.readToString(new File(reporter.getReportDirectory() + File.separator + reporter.getOutputDirectory() + File.separator + String.format(reporter.getReportFileNamePattern(), JUnitReporterJunitTest.class.getName())));

        assertEquals(reportFile, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator() +
                "<testsuite name=\"com.consol.citrus.report.JUnitReporterJunitTest\" time=\"0.0\" tests=\"2\" errors=\"0\" skipped=\"0\" failures=\"0\">" + System.lineSeparator() +
                "    <testcase name=\"fooTest\" classname=\"com.consol.citrus.report.JUnitReporterJunitTest\" time=\"0.0\"/>" + System.lineSeparator() +
                "    <testcase name=\"barTest\" classname=\"com.consol.citrus.report.JUnitReporterJunitTest\" time=\"0.0\"/>" + System.lineSeparator() +
                "</testsuite>");
    }

    @Test
    public void testGenerateTestResultsWithFailedTests() throws Exception {
        reporter.getTestResults().addResult(TestResult.success("fooTest", JUnitReporterJunitTest.class.getName()));
        reporter.getTestResults().addResult(TestResult.failed("barTest", JUnitReporterJunitTest.class.getName(), new NullPointerException("Something went wrong!")));
        reporter.generateTestResults();

        String reportFile = FileUtils.readToString(new File(reporter.getReportDirectory() + File.separator + reporter.getOutputDirectory() + File.separator + String.format(reporter.getReportFileNamePattern(), JUnitReporterJunitTest.class.getName())));
        String testSuiteFile = FileUtils.readToString(new File(reporter.getReportDirectory() + File.separator + String.format(reporter.getReportFileNamePattern(), reporter.getSuiteName())));

        assertTrue(reportFile.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator() +
                "<testsuite name=\"com.consol.citrus.report.JUnitReporterJunitTest\" time=\"0.0\" tests=\"2\" errors=\"0\" skipped=\"0\" failures=\"1\">" + System.lineSeparator() +
                "    <testcase name=\"fooTest\" classname=\"com.consol.citrus.report.JUnitReporterJunitTest\" time=\"0.0\"/>" + System.lineSeparator() +
                "    <testcase name=\"barTest\" classname=\"com.consol.citrus.report.JUnitReporterJunitTest\" time=\"0.0\">" + System.lineSeparator() +
                "      <failure type=\"java.lang.NullPointerException\" message=\"Something went wrong!\">" + System.lineSeparator() +
                "        <![CDATA[" + System.lineSeparator() +
                "        java.lang.NullPointerException: Something went wrong!"));

        assertTrue(testSuiteFile.contains("<testsuite name=\"" + reporter.getSuiteName() + "\""));
        assertTrue(testSuiteFile.contains("tests=\"2\" errors=\"0\" skipped=\"0\" failures=\"1\""));
        assertTrue(testSuiteFile.contains("<failure type=\"java.lang.NullPointerException\" message=\"Something went wrong!\">"));
    }

    @Test
    public void testGenerateTestResultsWithFailedTestsWhenFailureTypeAndFailureStackAreNull() throws Exception {
        reporter.getTestResults().addResult(TestResult.success("fooTest", JUnitReporterJunitTest.class.getName()));
        reporter.getTestResults().addResult(TestResult.failed("barTest", JUnitReporterJunitTest.class.getName(), "Something went wrong!"));
        reporter.generateTestResults();

        String reportFile = FileUtils.readToString(new File(reporter.getReportDirectory() + File.separator + reporter.getOutputDirectory() + File.separator + String.format(reporter.getReportFileNamePattern(), JUnitReporterJunitTest.class.getName())));
        String testSuiteFile = FileUtils.readToString(new File(reporter.getReportDirectory() + File.separator + String.format(reporter.getReportFileNamePattern(), reporter.getSuiteName())));

        assertTrue(reportFile.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator() +
                "<testsuite name=\"com.consol.citrus.report.JUnitReporterJunitTest\" time=\"0.0\" tests=\"2\" errors=\"0\" skipped=\"0\" failures=\"1\">" + System.lineSeparator() +
                "    <testcase name=\"fooTest\" classname=\"com.consol.citrus.report.JUnitReporterJunitTest\" time=\"0.0\"/>" + System.lineSeparator() +
                "    <testcase name=\"barTest\" classname=\"com.consol.citrus.report.JUnitReporterJunitTest\" time=\"0.0\">" + System.lineSeparator() +
                "      <failure type=\"\" message=\"Something went wrong!\">"));

        assertTrue(testSuiteFile.contains("<testsuite name=\"" + reporter.getSuiteName() + "\""));
        assertTrue(testSuiteFile.contains("tests=\"2\" errors=\"0\" skipped=\"0\" failures=\"1\""));
        assertTrue(testSuiteFile.contains("<failure type=\"\" message=\"Something went wrong!\">"));
    }

    @Test
    public void testGenerateTestResultsWithSkippedTests() throws Exception {
        reporter.getTestResults().addResult(TestResult.success("fooTest", JUnitReporterJunitTest.class.getName()));
        reporter.getTestResults().addResult(TestResult.skipped("barTest", JUnitReporterJunitTest.class.getName()));
        reporter.generateTestResults();

        String reportFile = FileUtils.readToString(new File(reporter.getReportDirectory() + File.separator + reporter.getOutputDirectory() + File.separator + String.format(reporter.getReportFileNamePattern(), JUnitReporterJunitTest.class.getName())));

        assertEquals(reportFile, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator() +
                "<testsuite name=\"com.consol.citrus.report.JUnitReporterJunitTest\" time=\"0.0\" tests=\"2\" errors=\"0\" skipped=\"1\" failures=\"0\">" + System.lineSeparator() +
                "    <testcase name=\"fooTest\" classname=\"com.consol.citrus.report.JUnitReporterJunitTest\" time=\"0.0\"/>" + System.lineSeparator() +
                "    <testcase name=\"barTest\" classname=\"com.consol.citrus.report.JUnitReporterJunitTest\" time=\"0.0\"/>" + System.lineSeparator() +
                "</testsuite>");
    }

    @Test
    public void testGenerateTestResultsWithFailedTestsWithInvalidXMLChars() throws Exception {
        reporter.getTestResults().addResult(TestResult.success("foo\"Test", JUnitReporterJunitTest.class.getName()));
        reporter.getTestResults().addResult(TestResult.failed("bar\"Test", JUnitReporterJunitTest.class.getName(), new NullPointerException("Something \"went wrong!")));
        reporter.generateTestResults();

        String reportFile = FileUtils.readToString(new File(reporter.getReportDirectory() + File.separator + reporter.getOutputDirectory() + File.separator + String.format(reporter.getReportFileNamePattern(), JUnitReporterJunitTest.class.getName())));
        String testSuiteFile = FileUtils.readToString(new File(reporter.getReportDirectory() + File.separator + String.format(reporter.getReportFileNamePattern(), reporter.getSuiteName())));

        assertTrue(reportFile.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator() +
            "<testsuite name=\"com.consol.citrus.report.JUnitReporterJunitTest\" time=\"0.0\" tests=\"2\" errors=\"0\" skipped=\"0\" failures=\"1\">" + System.lineSeparator() +
            "    <testcase name=\"foo&quot;Test\" classname=\"com.consol.citrus.report.JUnitReporterJunitTest\" time=\"0.0\"/>" + System.lineSeparator() +
            "    <testcase name=\"bar&quot;Test\" classname=\"com.consol.citrus.report.JUnitReporterJunitTest\" time=\"0.0\">" + System.lineSeparator() +
            "      <failure type=\"java.lang.NullPointerException\" message=\"Something &quot;went wrong!\">" + System.lineSeparator() +
            "        <![CDATA[" + System.lineSeparator() +
            "        java.lang.NullPointerException: Something \"went wrong!"));

        assertTrue(testSuiteFile.contains("<testsuite name=\"" + reporter.getSuiteName() + "\""));
        assertTrue(testSuiteFile.contains("tests=\"2\" errors=\"0\" skipped=\"0\" failures=\"1\""));
        assertTrue(testSuiteFile.contains("<failure type=\"java.lang.NullPointerException\" message=\"Something &quot;went wrong!\">"));
    }
}
