package com.consol.citrus.report;

import com.consol.citrus.TestSuite;


public interface TestReporter {
    public void generateTestResults(TestSuite[] suites);
}
