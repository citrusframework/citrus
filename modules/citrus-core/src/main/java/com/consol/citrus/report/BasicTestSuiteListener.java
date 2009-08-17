package com.consol.citrus.report;

import com.consol.citrus.TestSuite;

public class BasicTestSuiteListener implements TestSuiteListener {

    public void onFinish(TestSuite testsuite) {}

    public void onFinishFailure(TestSuite testsuite, Throwable cause) {}

    public void onFinishSuccess(TestSuite testsuite) {}

    public void onStart(TestSuite testsuite) {}

    public void onStartFailure(TestSuite testsuite, Throwable cause) {}

    public void onStartSuccess(TestSuite testsuite) {}
}
