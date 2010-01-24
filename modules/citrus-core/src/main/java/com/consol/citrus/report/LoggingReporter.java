/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.report;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.TestCase;
import com.consol.citrus.TestSuite;
import com.consol.citrus.report.TestResult.RESULT;

/**
 * Simple logging reporter printing test start and ending to the console/logger.
 * 
 * @author Christoph Deppisch
 */
public class LoggingReporter implements TestSuiteListener, TestListener, TestReporter {
    
    /** Collect test results for overall result overview at the very end of test execution */
    private TestResults testResults = new TestResults();
    
    /** Common decimal format for percentage calculation in report **/
    private static DecimalFormat decFormat = new DecimalFormat("0.0");
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(LoggingReporter.class);

    static {
        DecimalFormatSymbols symbol = new DecimalFormatSymbols();
        symbol.setDecimalSeparator('.');
        decFormat.setDecimalFormatSymbols(symbol);
    }

    /**
     * @see com.consol.citrus.report.TestReporter#generateTestResults(com.consol.citrus.TestSuite[])
     */
    public void generateTestResults(TestSuite[] suites) {
        if (log.isInfoEnabled()) {
            log.info("________________________________________________________________________");
            log.info("");
            log.info("CITRUS TEST RESULTS");
            log.info("");
        }
        
        for (TestResult testResult : testResults) {
            if (testResult.getResult().equals(RESULT.SKIP)) {
                log.debug(testResult.toString());
            } else {
                log.info(testResult.toString());
            }
        }
        
        log.info("");
        log.info("Found " + testResults.size() + " test cases to execute");
        log.info("Skipped " + testResults.getSkipped() + " test cases" + " (" + decFormat.format((double)testResults.getSkipped() / (testResults.size())*100) + "%)");
        log.info("Executed " + (testResults.getFailed() + testResults.getSuccess()) + " test cases");
        log.info("Tests failed: \t\t" + testResults.getFailed() + " (" + decFormat.format((double)testResults.getFailed() / (testResults.getFailed() + testResults.getSuccess()) * 100) + "%)");
        log.info("Tests successfully: \t" + testResults.getSuccess() + " (" + decFormat.format((double)testResults.getSuccess() / (testResults.getFailed() + testResults.getSuccess()) * 100) + "%)");

        log.info("________________________________________________________________________");
    }

    /**
     * @see com.consol.citrus.report.TestListener#onTestFailure(com.consol.citrus.TestCase, java.lang.Throwable)
     */
    public void onTestFailure(TestCase test, Throwable cause) {
        if (cause != null) {
            testResults.addResult(new TestResult(test.getName(), RESULT.FAILURE, cause));
        } else {
            testResults.addResult(new TestResult(test.getName(), RESULT.FAILURE));
        }

        log.error("Execution of test: " + test.getName() + " failed! Nested exception is: ", cause);
    }

    /**
     * @see com.consol.citrus.report.TestListener#onTestSkipped(com.consol.citrus.TestCase)
     */
    public void onTestSkipped(TestCase test) {
        if (log.isDebugEnabled()) {
            log.debug(seperator());
            log.debug("SKIP TEST: " + test.getName());
            log.debug("Test explicitly excluded from test suite");
            log.debug(seperator());
        }

        testResults.addResult(new TestResult(test.getName(), RESULT.SKIP));
    }

    /**
     * @see com.consol.citrus.report.TestListener#onTestStart(com.consol.citrus.TestCase)
     */
    public void onTestStart(TestCase test) {
        log.info(seperator());
        log.info("STARTING TEST: " + test.getName());
    }

    /**
     * @see com.consol.citrus.report.TestListener#onTestFinish(com.consol.citrus.TestCase)
     */
    public void onTestFinish(TestCase test) {
        log.info("");
        log.info("TEST FINISHED: " + test.getName());
        log.info(seperator());
    }

    /**
     * @see com.consol.citrus.report.TestListener#onTestSuccess(com.consol.citrus.TestCase)
     */
    public void onTestSuccess(TestCase test) {
        testResults.addResult(new TestResult(test.getName(), RESULT.SUCCESS));
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onFinish(com.consol.citrus.TestSuite)
     */
    public void onFinish(TestSuite testsuite) {
        log.info("FINISH TESTSUITE " + testsuite.getName());
        log.info(seperator());
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onStart(com.consol.citrus.TestSuite)
     */
    public void onStart(TestSuite testsuite) {
        log.info("________________________________________________________________________");
        log.info("RUNNING TESTSUITE " + testsuite.getName());
        log.info("");

        log.info(seperator());
        log.info("INIT");
        log.info("");
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onFinishFailure(com.consol.citrus.TestSuite, java.lang.Throwable)
     */
    public void onFinishFailure(TestSuite testsuite, Throwable cause) {
        log.info(seperator());
        log.info("FINISH failed");
        log.info("");
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onFinishSuccess(com.consol.citrus.TestSuite)
     */
    public void onFinishSuccess(TestSuite testsuite) {
        log.info(seperator());
        log.info("FINISH successfully");
        log.info("");
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onStartFailure(com.consol.citrus.TestSuite, java.lang.Throwable)
     */
    public void onStartFailure(TestSuite testsuite, Throwable cause) {
        log.info(seperator());
        log.info("INIT failed");
        log.info("");
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onStartSuccess(com.consol.citrus.TestSuite)
     */
    public void onStartSuccess(TestSuite testsuite) {
        log.info(seperator());
        log.info("INIT successfully");
        log.info("");
    }

    /**
     * Helper method to build consistent separators
     * @return
     */
    private String seperator() {
        return "------------------------------------------------------------------------";
    }

}
