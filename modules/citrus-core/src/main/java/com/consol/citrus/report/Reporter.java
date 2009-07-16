package com.consol.citrus.report;

import com.consol.citrus.TestSuite;

public interface Reporter extends TestListener, TestSuiteListener {
    public void generateReport(TestSuite testsuite);
}
