/*
 * Copyright 2006-2010 the original author or authors.
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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.consol.citrus.TestCase;
import com.consol.citrus.container.SequenceAfterSuite;
import com.consol.citrus.container.SequenceBeforeSuite;
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
    
    @Autowired(required = false)
    private SequenceBeforeSuite beforeSuite;
    
    @Autowired(required = false)
    private SequenceAfterSuite afterSuite;
    
    /**
     * Logger
     */
    private static Logger log = LoggerFactory.getLogger(LoggingReporter.class);

    static {
        DecimalFormatSymbols symbol = new DecimalFormatSymbols();
        symbol.setDecimalSeparator('.');
        decFormat.setDecimalFormatSymbols(symbol);
    }

    /**
     * @see com.consol.citrus.report.TestReporter#generateTestResults()
     */
    public void generateTestResults() {
        if (log.isInfoEnabled()) {
            log.info(seperator());
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
        log.info("Total number of tests: " + (testResults.getFailed() + testResults.getSuccess()));
        log.info("Skipped:\t" + testResults.getSkipped() + " (" + decFormat.format((double)testResults.getSkipped() / (testResults.size())*100) + "%)");
        log.info("Failed:\t" + testResults.getFailed() + " (" + decFormat.format((double)testResults.getFailed() / (testResults.getFailed() + testResults.getSuccess()) * 100) + "%)");
        log.info("Success:\t" + testResults.getSuccess() + " (" + decFormat.format((double)testResults.getSuccess() / (testResults.getFailed() + testResults.getSuccess()) * 100) + "%)");

        log.info(seperator());
    }

    /**
     * @see com.consol.citrus.report.TestListener#onTestFailure(com.consol.citrus.TestCase, java.lang.Throwable)
     */
    public void onTestFailure(TestCase test, Throwable cause) {
        if (cause != null) {
            testResults.addResult(new TestResult(test.getName(), RESULT.FAILURE, cause, test.getParameters()));
        } else {
            testResults.addResult(new TestResult(test.getName(), RESULT.FAILURE, test.getParameters()));
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

        testResults.addResult(new TestResult(test.getName(), RESULT.SKIP, test.getParameters()));
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
        testResults.addResult(new TestResult(test.getName(), RESULT.SUCCESS, test.getParameters()));
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onFinish()
     */
    public void onFinish() {
        log.info(seperator());
        log.info("FINISHED CITRUS TESTS");
        log.info("");
        
        if (afterSuite != null) {
            log.info(seperator());
            log.info("STARTING AFTER SUITE");
            log.info("");
        }
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onStart()
     */
    public void onStart() {
        log.info(seperator());
        log.info("RUNNING CITRUS TESTS");
        log.info("");

        if (beforeSuite != null) {
            log.info(seperator());
            log.info("STARTING BEFORE SUITE");
            log.info("");
        }
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onFinishFailure(java.lang.Throwable)
     */
    public void onFinishFailure(Throwable cause) {
        if (afterSuite != null) {
            log.info(seperator());
            log.info("AFTER SUITE: failed");
            log.info("");
        }
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onFinishSuccess()
     */
    public void onFinishSuccess() {
        if (afterSuite != null) {
            log.info(seperator());
            log.info("AFTER SUITE: successfully");
            log.info("");
        }
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onStartFailure(java.lang.Throwable)
     */
    public void onStartFailure(Throwable cause) {
        if (beforeSuite != null) {
            log.info(seperator());
            log.info("BEFORE SUITE: failed");
            log.info("");
        }
    }

    /**
     * @see com.consol.citrus.report.TestSuiteListener#onStartSuccess()
     */
    public void onStartSuccess() {
        if (beforeSuite != null) {
            log.info(seperator());
            log.info("BEFORE SUITE: successfully");
            log.info("");
        }
    }

    /**
     * Helper method to build consistent separators
     * @return
     */
    private String seperator() {
        return "------------------------------------------------------------------------";
    }

}
