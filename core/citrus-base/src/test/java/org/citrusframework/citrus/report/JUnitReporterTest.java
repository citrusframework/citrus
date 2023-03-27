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

package org.citrusframework.citrus.report;

import java.io.File;

import org.citrusframework.citrus.TestResult;
import org.citrusframework.citrus.util.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Christoph Deppisch
 * @since 2.7.5
 */
public class JUnitReporterTest {

    private JUnitReporter reporter = new JUnitReporter();

    @Test
    public void testGenerateTestResults() throws Exception {
        TestResults testResults = new TestResults();
        testResults.addResult(TestResult.success("fooTest", JUnitReporterTest.class.getName()));
        reporter.generate(testResults);

        String reportFile = FileUtils.readToString(new File(reporter.getReportDirectory() + File.separator + reporter.getOutputDirectory() + File.separator + String.format(reporter.getReportFileNamePattern(), JUnitReporterTest.class.getName())));
        String testSuiteFile = FileUtils.readToString(new File(reporter.getReportDirectory() + File.separator + String.format(reporter.getReportFileNamePattern(), reporter.getSuiteName())));

        Assert.assertEquals(reportFile, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator() +
                "<testsuite name=\"org.citrusframework.citrus.report.JUnitReporterTest\" time=\"0.0\" tests=\"1\" errors=\"0\" skipped=\"0\" failures=\"0\">" + System.lineSeparator() +
                "    <testcase name=\"fooTest\" classname=\"org.citrusframework.citrus.report.JUnitReporterTest\" time=\"0.0\"/>" + System.lineSeparator() +
                "</testsuite>");

        Assert.assertEquals(testSuiteFile, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator() +
                "<testsuite name=\"" + reporter.getSuiteName() + "\" time=\"0.0\" tests=\"1\" errors=\"0\" skipped=\"0\" failures=\"0\">" + System.lineSeparator() +
                "    <testcase name=\"fooTest\" classname=\"org.citrusframework.citrus.report.JUnitReporterTest\" time=\"0.0\"/>" + System.lineSeparator() +
                "</testsuite>");
    }

    @Test
    public void testGenerateTestResultsMultipleTests() throws Exception {
        TestResults testResults = new TestResults();
        testResults.addResult(TestResult.success("fooTest", JUnitReporterTest.class.getName()));
        testResults.addResult(TestResult.success("barTest", JUnitReporterTest.class.getName()));
        reporter.generate(testResults);

        String reportFile = FileUtils.readToString(new File(reporter.getReportDirectory() + File.separator + reporter.getOutputDirectory() + File.separator + String.format(reporter.getReportFileNamePattern(), JUnitReporterTest.class.getName())));

        Assert.assertEquals(reportFile, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator() +
                "<testsuite name=\"org.citrusframework.citrus.report.JUnitReporterTest\" time=\"0.0\" tests=\"2\" errors=\"0\" skipped=\"0\" failures=\"0\">" + System.lineSeparator() +
                "    <testcase name=\"fooTest\" classname=\"org.citrusframework.citrus.report.JUnitReporterTest\" time=\"0.0\"/>" + System.lineSeparator() +
                "    <testcase name=\"barTest\" classname=\"org.citrusframework.citrus.report.JUnitReporterTest\" time=\"0.0\"/>" + System.lineSeparator() +
                "</testsuite>");
    }

    @Test
    public void testGenerateTestResultsWithFailedTests() throws Exception {
        TestResults testResults = new TestResults();
        testResults.addResult(TestResult.success("fooTest", JUnitReporterTest.class.getName()));
        testResults.addResult(TestResult.failed("barTest", JUnitReporterTest.class.getName(), new NullPointerException("Something went wrong!")));
        reporter.generate(testResults);

        String reportFile = FileUtils.readToString(new File(reporter.getReportDirectory() + File.separator + reporter.getOutputDirectory() + File.separator + String.format(reporter.getReportFileNamePattern(), JUnitReporterTest.class.getName())));
        String testSuiteFile = FileUtils.readToString(new File(reporter.getReportDirectory() + File.separator + String.format(reporter.getReportFileNamePattern(), reporter.getSuiteName())));

        Assert.assertTrue(reportFile.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator() +
                "<testsuite name=\"org.citrusframework.citrus.report.JUnitReporterTest\" time=\"0.0\" tests=\"2\" errors=\"0\" skipped=\"0\" failures=\"1\">" + System.lineSeparator() +
                "    <testcase name=\"fooTest\" classname=\"org.citrusframework.citrus.report.JUnitReporterTest\" time=\"0.0\"/>" + System.lineSeparator() +
                "    <testcase name=\"barTest\" classname=\"org.citrusframework.citrus.report.JUnitReporterTest\" time=\"0.0\">" + System.lineSeparator() +
                "      <failure type=\"java.lang.NullPointerException\" message=\"Something went wrong!\">" + System.lineSeparator() +
                "        <![CDATA[" + System.lineSeparator() +
                "        java.lang.NullPointerException: Something went wrong!"));

        Assert.assertTrue(testSuiteFile.contains("<testsuite name=\"" + reporter.getSuiteName() + "\""));
        Assert.assertTrue(testSuiteFile.contains("tests=\"2\" errors=\"0\" skipped=\"0\" failures=\"1\""));
        Assert.assertTrue(testSuiteFile.contains("<failure type=\"java.lang.NullPointerException\" message=\"Something went wrong!\">"));
    }

    @Test
    public void testGenerateTestResultsWithSkippedTests() throws Exception {
        TestResults testResults = new TestResults();
        testResults.addResult(TestResult.success("fooTest", JUnitReporterTest.class.getName()));
        testResults.addResult(TestResult.skipped("barTest", JUnitReporterTest.class.getName()));
        reporter.generate(testResults);

        String reportFile = FileUtils.readToString(new File(reporter.getReportDirectory() + File.separator + reporter.getOutputDirectory() + File.separator + String.format(reporter.getReportFileNamePattern(), JUnitReporterTest.class.getName())));

        Assert.assertEquals(reportFile, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator() +
                "<testsuite name=\"org.citrusframework.citrus.report.JUnitReporterTest\" time=\"0.0\" tests=\"2\" errors=\"0\" skipped=\"1\" failures=\"0\">" + System.lineSeparator() +
                "    <testcase name=\"fooTest\" classname=\"org.citrusframework.citrus.report.JUnitReporterTest\" time=\"0.0\"/>" + System.lineSeparator() +
                "    <testcase name=\"barTest\" classname=\"org.citrusframework.citrus.report.JUnitReporterTest\" time=\"0.0\"/>" + System.lineSeparator() +
                "</testsuite>");
    }

    @Test
    public void testGenerateTestResultsWithFailedTestsWithInvalidXMLChars() throws Exception {
        TestResults testResults = new TestResults();
        testResults.addResult(TestResult.success("foo\"Test", JUnitReporterTest.class.getName()));
        testResults.addResult(TestResult.failed("bar\"Test", JUnitReporterTest.class.getName(), new NullPointerException("Something \"went wrong!")));
        reporter.generate(testResults);

        String reportFile = FileUtils.readToString(new File(reporter.getReportDirectory() + File.separator + reporter.getOutputDirectory() + File.separator + String.format(reporter.getReportFileNamePattern(), JUnitReporterTest.class.getName())));
        String testSuiteFile = FileUtils.readToString(new File(reporter.getReportDirectory() + File.separator + String.format(reporter.getReportFileNamePattern(), reporter.getSuiteName())));

        Assert.assertTrue(reportFile.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator() +
            "<testsuite name=\"org.citrusframework.citrus.report.JUnitReporterTest\" time=\"0.0\" tests=\"2\" errors=\"0\" skipped=\"0\" failures=\"1\">" + System.lineSeparator() +
            "    <testcase name=\"foo&quot;Test\" classname=\"org.citrusframework.citrus.report.JUnitReporterTest\" time=\"0.0\"/>" + System.lineSeparator() +
            "    <testcase name=\"bar&quot;Test\" classname=\"org.citrusframework.citrus.report.JUnitReporterTest\" time=\"0.0\">" + System.lineSeparator() +
            "      <failure type=\"java.lang.NullPointerException\" message=\"Something &quot;went wrong!\">" + System.lineSeparator() +
            "        <![CDATA[" + System.lineSeparator() +
            "        java.lang.NullPointerException: Something \"went wrong!"));

        Assert.assertTrue(testSuiteFile.contains("<testsuite name=\"" + reporter.getSuiteName() + "\""));
        Assert.assertTrue(testSuiteFile.contains("tests=\"2\" errors=\"0\" skipped=\"0\" failures=\"1\""));
        Assert.assertTrue(testSuiteFile.contains("<failure type=\"java.lang.NullPointerException\" message=\"Something &quot;went wrong!\">"));
    }

    @Test
    public void testGenerateTestResultsWithFailedTestsWhenFailureTypeAndFailureStackAreNull() throws Exception {
        TestResults testResults = new TestResults();
        testResults.addResult(TestResult.success("fooTest", JUnitReporterTest.class.getName()));
        testResults.addResult(TestResult.failed("barTest", JUnitReporterTest.class.getName(), "Something went wrong!"));
        reporter.generate(testResults);

        String reportFile = FileUtils.readToString(new File(reporter.getReportDirectory() + File.separator + reporter.getOutputDirectory() + File.separator + String.format(reporter.getReportFileNamePattern(), JUnitReporterTest.class.getName())));
        String testSuiteFile = FileUtils.readToString(new File(reporter.getReportDirectory() + File.separator + String.format(reporter.getReportFileNamePattern(), reporter.getSuiteName())));

        assertTrue(reportFile.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator() +
                "<testsuite name=\"org.citrusframework.citrus.report.JUnitReporterTest\" time=\"0.0\" tests=\"2\" errors=\"0\" skipped=\"0\" failures=\"1\">" + System.lineSeparator() +
                "    <testcase name=\"fooTest\" classname=\"org.citrusframework.citrus.report.JUnitReporterTest\" time=\"0.0\"/>" + System.lineSeparator() +
                "    <testcase name=\"barTest\" classname=\"org.citrusframework.citrus.report.JUnitReporterTest\" time=\"0.0\">" + System.lineSeparator() +
                "      <failure type=\"\" message=\"Something went wrong!\">"));

        assertTrue(testSuiteFile.contains("<testsuite name=\"" + reporter.getSuiteName() + "\""));
        assertTrue(testSuiteFile.contains("tests=\"2\" errors=\"0\" skipped=\"0\" failures=\"1\""));
        assertTrue(testSuiteFile.contains("<failure type=\"\" message=\"Something went wrong!\">"));
    }
}
