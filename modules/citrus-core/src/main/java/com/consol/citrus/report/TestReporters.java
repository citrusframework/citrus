package com.consol.citrus.report;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.consol.citrus.TestCase;
import com.consol.citrus.TestSuite;

/**
 * Reporter implementation delegating to a list of reporters.
 * 
 * @author deppisch Christoph Deppisch ConSol* Software GmbH
 * @since 13.08.2009
 */
public class TestReporters implements TestReporter {

    /** List of testsuite reporter **/
    @Autowired
    private List<TestReporter> reporterList = new ArrayList<TestReporter>();
    
    public void generateReport(TestSuite testsuite) {
        for (TestReporter reporter : reporterList) {
            reporter.generateReport(testsuite);
        }
    }

    public void onTestFailure(TestCase test, Throwable cause) {
        for (TestReporter reporter : reporterList) {
            reporter.onTestFailure(test, cause);
        }
    }

    public void onTestFinish(TestCase test) {
        for (TestReporter reporter : reporterList) {
            reporter.onTestFinish(test);
        }
    }

    public void onTestSkipped(TestCase test) {
        for (TestReporter reporter : reporterList) {
            reporter.onTestSkipped(test);
        }
    }

    public void onTestStart(TestCase test) {
        for (TestReporter reporter : reporterList) {
            reporter.onTestStart(test);
        }
    }

    public void onTestSuccess(TestCase test) {
        for (TestReporter reporter : reporterList) {
            reporter.onTestSuccess(test);
        }
    }

    public void onFinish(TestSuite testsuite) {
        for (TestReporter reporter : reporterList) {
            reporter.onFinish(testsuite);
        }
    }

    public void onFinishFailure(TestSuite testsuite, Throwable cause) {
        for (TestReporter reporter : reporterList) {
            reporter.onFinishFailure(testsuite, cause);
        }
    }

    public void onFinishSuccess(TestSuite testsuite) {
        for (TestReporter reporter : reporterList) {
            reporter.onFinishSuccess(testsuite);
        }
    }

    public void onStart(TestSuite testsuite) {
        for (TestReporter reporter : reporterList) {
            reporter.onStart(testsuite);
        }
    }

    public void onStartFailure(TestSuite testsuite, Throwable cause) {
        for (TestReporter reporter : reporterList) {
            reporter.onStartFailure(testsuite, cause);
        }
    }

    public void onStartSuccess(TestSuite testsuite) {
        for (TestReporter reporter : reporterList) {
            reporter.onStartSuccess(testsuite);
        }
    }

    /**
     * @param reporterList the reporterList to set
     */
    public void setReporterList(List<TestReporter> reporterList) {
        this.reporterList = reporterList;
    }

}
