package com.consol.citrus.report;

import com.consol.citrus.TestSuite;

public interface TestReporter extends TestListener, TestSuiteListener {
    public void generateReport(TestSuite testsuite);
}
