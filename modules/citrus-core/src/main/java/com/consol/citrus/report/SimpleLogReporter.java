package com.consol.citrus.report;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.consol.citrus.TestCase;
import com.consol.citrus.TestSuite;


public class SimpleLogReporter implements TestReporter {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(SimpleLogReporter.class);

    /** Common decimal format for percentage calculation in report **/
    private static DecimalFormat decFormat = new DecimalFormat("0.0");

    static {
        DecimalFormatSymbols symbol = new DecimalFormatSymbols();
        symbol.setDecimalSeparator('.');
        decFormat.setDecimalFormatSymbols(symbol);
    }

    /** List filled with report messages during the test suite run */
    private List report = new ArrayList();

    public void generateReport(TestSuite testsuite) {
        log.info("________________________________________________________________________");
        log.info("");
        log.info("TEST RESULTS " + testsuite.getName());
        log.info("");

        Iterator it = report.iterator();
        while (it.hasNext()) {
            String entry = it.next().toString();

            if (entry.indexOf("skipped") == (-1))
                log.info(entry);
            else
                log.debug(entry);
        }

        log.info("");
        log.info("Found " + testsuite.getCntTests() + " test cases to execute");
        log.info("Skipped " + testsuite.getCntSkipped() + " test cases" + " (" + decFormat.format((double)testsuite.getCntSkipped()/(testsuite.getCntTests())*100) + "%)");
        log.info("Executed " + (testsuite.getCntCasesFail()+testsuite.getCntCasesSuccess()) + " test cases, containing " +  testsuite.getCntActions() + " actions");
        log.info("Tests failed: \t\t" + testsuite.getCntCasesFail() + " (" + decFormat.format((double)testsuite.getCntCasesFail()/(testsuite.getCntCasesFail()+testsuite.getCntCasesSuccess())*100) + "%)");
        log.info("Tests successfully: \t" + testsuite.getCntCasesSuccess() + " (" + decFormat.format((double)testsuite.getCntCasesSuccess()/(testsuite.getCntCasesFail()+testsuite.getCntCasesSuccess())*100) + "%)");

        log.info("________________________________________________________________________");
    }

    public void onTestFailure(TestCase test, Throwable cause) {
        if (cause != null) {
            buildReportEntry(test.getName(), "failed - Exception is " + cause.getLocalizedMessage());
        } else {
            buildReportEntry(test.getName(), "failed - No exception available");
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

        buildReportEntry(test.getName(), "skipped - because excluded from test suite");
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
        buildReportEntry(test.getName(), "successful");
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

    /**
     * Method to build a report entry
     * @param testName name of current test case
     * @param result message to report test case result
     */
    private void buildReportEntry(String testName, String result) {
        StringBuffer buf = new StringBuffer();

        buf.append("  " + testName);

        int spaces = 50 - testName.length();
        for (int i = 0; i < spaces; i++) {
            buf.append(" ");
        }

        if (result.length() > 100) {
            buf.append(": " + result.substring(0, 100) + " ...");
        } else
            buf.append(": " + result);

        report.add(buf.toString());
    }
}
