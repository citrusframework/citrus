package com.consol.citrus.report;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.TestCase;
import com.consol.citrus.TestSuite;
import com.consol.citrus.report.TestResult.RESULT;


public class SimpleLogReporter implements TestSuiteListener, TestListener, TestReporter {
    
    private TestResults testResults = new TestResults();
    
    /** Common decimal format for percentage calculation in report **/
    private static DecimalFormat decFormat = new DecimalFormat("0.0");
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(SimpleLogReporter.class);

    static {
        DecimalFormatSymbols symbol = new DecimalFormatSymbols();
        symbol.setDecimalSeparator('.');
        decFormat.setDecimalFormatSymbols(symbol);
    }

    public void generateTestResults(TestSuite[] suites) {
        if (log.isInfoEnabled()) {
            log.info("________________________________________________________________________");
            log.info("");
            log.info("CITRUS TEST RESULTS");
            log.info("");
        }
        
        for (TestResult testResult : testResults) {
            if (testResult.getResult().equals(RESULT.SKIP))
                log.debug(testResult.toString());
            else
                log.info(testResult.toString());
        }
        
        log.info("");
        log.info("Found " + testResults.size() + " test cases to execute");
        log.info("Skipped " + testResults.getSkipped() + " test cases" + " (" + decFormat.format((double)testResults.getSkipped() / (testResults.size())*100) + "%)");
        log.info("Executed " + (testResults.getFailed() + testResults.getSuccess()) + " test cases");
        log.info("Tests failed: \t\t" + testResults.getFailed() + " (" + decFormat.format((double)testResults.getFailed() / (testResults.getFailed() + testResults.getSuccess()) * 100) + "%)");
        log.info("Tests successfully: \t" + testResults.getSuccess() + " (" + decFormat.format((double)testResults.getSuccess() / (testResults.getFailed() + testResults.getSuccess()) * 100) + "%)");

        log.info("________________________________________________________________________");
    }

    public void onTestFailure(TestCase test, Throwable cause) {
        if (cause != null) {
            testResults.addResult(new TestResult(test.getName(), RESULT.FAILURE, cause));
        } else {
            testResults.addResult(new TestResult(test.getName(), RESULT.FAILURE));
        }

        log.error("Execution of test: " + test.getName() + " failed! Nested exception is: ", cause);
    }

    public void onTestSkipped(TestCase test) {
        if (log.isDebugEnabled()) {
            log.debug(seperator());
            log.debug("SKIP TEST: " + test.getName());
            log.debug("Test explicitly excluded from test suite");
            log.debug(seperator());
        }

        testResults.addResult(new TestResult(test.getName(), RESULT.SKIP));
    }

    public void onTestStart(TestCase test) {
        log.info(seperator());
        log.info("STARTING TEST: " + test.getName());
    }

    public void onTestFinish(TestCase test) {
        log.info("");
        log.info("TEST FINISHED: " + test.getName());
        log.info(seperator());
    }

    public void onTestSuccess(TestCase test) {
        testResults.addResult(new TestResult(test.getName(), RESULT.SUCCESS));
    }

    public void onFinish(TestSuite testsuite) {
        log.info("FINISH TESTSUITE " + testsuite.getName());
        log.info(seperator());
    }

    public void onStart(TestSuite testsuite) {
        log.info("________________________________________________________________________");
        log.info("RUNNING TESTSUITE " + testsuite.getName());
        log.info("");

        log.info(seperator());
        log.info("INIT");
        log.info("");
    }

    public void onFinishFailure(TestSuite testsuite, Throwable cause) {
        log.info(seperator());
        log.info("FINISH failed");
        log.info("");
    }

    public void onFinishSuccess(TestSuite testsuite) {
        log.info(seperator());
        log.info("FINISH successfully");
        log.info("");
    }

    public void onStartFailure(TestSuite testsuite, Throwable cause) {
        log.info(seperator());
        log.info("INIT failed");
        log.info("");
    }

    public void onStartSuccess(TestSuite testsuite) {
        log.info(seperator());
        log.info("INIT successfully");
        log.info("");
    }

    /**
     * Helper method to build consistent seperators
     * @return
     */
    private String seperator() {
        return "------------------------------------------------------------------------";
    }

}
