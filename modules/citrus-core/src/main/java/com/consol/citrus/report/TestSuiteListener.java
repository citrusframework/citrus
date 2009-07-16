package com.consol.citrus.report;

import com.consol.citrus.TestSuite;

public interface TestSuiteListener {

    public void onStart(TestSuite testsuite);

    public void onStartSuccess(TestSuite testsuite);

    public void onStartFailure(TestSuite testsuite, Throwable cause);

    public void onFinish(TestSuite testsuite);

    public void onFinishSuccess(TestSuite testsuite);

    public void onFinishFailure(TestSuite testsuite, Throwable cause);
}
