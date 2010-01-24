/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
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
public class ReporterInvokingTestSuiteListener extends BasicTestSuiteListener {
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
     * @see com.consol.citrus.report.BasicTestSuiteListener#onStart(com.consol.citrus.TestSuite)
     */
    @Override
    public void onStart(TestSuite testsuite) {
        testSuitesStarted++;
    }

    /**
     * @see com.consol.citrus.report.BasicTestSuiteListener#onFinish(com.consol.citrus.TestSuite)
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
