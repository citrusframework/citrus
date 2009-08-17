package com.consol.citrus.report;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.consol.citrus.TestSuite;

public class ReporterInvokingTestSuiteListener extends BasicTestSuiteListener {
    private int testSuitesStarted = 0;
    private int testSuitesFinished = 0;
    
    ArrayList<TestSuite> suites = new ArrayList<TestSuite>();
    
    /** List of testsuite reporter **/
    @Autowired
    private List<TestReporter> reporterList = new ArrayList<TestReporter>();
    
    @Override
    public void onStart(TestSuite testsuite) {
        testSuitesStarted++;
    }
    
    @Override
    public void onFinish(TestSuite testsuite) {
        suites.add(testsuite);
        
        //in case last testsuite has finished
        if(++testSuitesFinished == testSuitesStarted) {
            for (TestReporter reporter : reporterList) {
                reporter.generateTestResults(suites.toArray(new TestSuite[]{}));
            }
        }
    }
}
