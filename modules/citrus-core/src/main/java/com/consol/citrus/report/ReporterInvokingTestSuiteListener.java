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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.consol.citrus.TestSuite;

/**
 * Test suite listener invoking several test reporter on start and end of execution.
 * 
 * @author Christoph Deppisch
 */
public class ReporterInvokingTestSuiteListener extends AbstractTestSuiteListener {
    /** Track number of startet test suites */
    private int testSuitesStarted = 0;
    /** Track number of finished test suites */
    private int testSuitesFinished = 0;
    
    /** List of test suites */
    private ArrayList<TestSuite> suites = new ArrayList<TestSuite>();
    
    /** List of testsuite reporter **/
    @Autowired
    private List<TestReporter> reporterList = new ArrayList<TestReporter>();
    
    /**
     * @see com.consol.citrus.report.AbstractTestSuiteListener#onStart(com.consol.citrus.TestSuite)
     */
    @Override
    public void onStart(TestSuite testsuite) {
        testSuitesStarted++;
    }

    /**
     * @see com.consol.citrus.report.AbstractTestSuiteListener#onFinish(com.consol.citrus.TestSuite)
     */
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
