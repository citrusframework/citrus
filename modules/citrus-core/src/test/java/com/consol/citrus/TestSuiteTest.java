/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.TestCaseMetaInfo.Status;
import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.actions.FailAction;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.report.TestListeners;
import com.consol.citrus.report.TestSuiteListeners;
import com.consol.citrus.testng.AbstractBaseTest;

public class TestSuiteTest extends AbstractBaseTest {
    @Autowired
    TestSuiteListeners testSuiteListeners;
    
    @Autowired
    TestListeners testListeners;
    
    @Test
    public void testBeforeSuite() {
        TestSuite testsuite = new TestSuite();
        
        TestCase testcase = new TestCase();
        testcase.setTestContext(createTestContext());
        testcase.setName("testBeforeSuite");
        TestCaseMetaInfo metaInfo = new TestCaseMetaInfo();
        metaInfo.setStatus(Status.FINAL);
        
        testcase.setMetaInfo(metaInfo);
        
        TestAction echoAction = new EchoAction();
        testcase.setTestChain(Collections.singletonList(echoAction));
        
        testsuite.setTestSuiteListeners(testSuiteListeners);
        
        TestAction beforeAction = new EchoAction();
        testsuite.setTasksBefore(Collections.singletonList(beforeAction));
        
        Assert.assertTrue(testsuite.beforeSuite());
    }
    
    @Test
    public void testFailBeforeSuite() {
        TestSuite testsuite = new TestSuite();
        
        TestCase testcase = new TestCase();
        testcase.setTestContext(createTestContext());
        testcase.setName("testFailBeforeSuite");
        TestCaseMetaInfo metaInfo = new TestCaseMetaInfo();
        metaInfo.setStatus(Status.FINAL);
        
        testcase.setMetaInfo(metaInfo);
        
        TestAction echoAction = new EchoAction();
        testcase.setTestChain(Collections.singletonList(echoAction));
        
        testsuite.setTestSuiteListeners(testSuiteListeners);
        
        TestAction failBean = new FailAction();
        testsuite.setTasksBefore(Collections.singletonList(failBean));
        
        Assert.assertFalse(testsuite.beforeSuite());
    }
    
    @Test
    public void testAfterSuite() {
        TestSuite testsuite = new TestSuite();
        
        TestCase testcase = new TestCase();
        testcase.setTestContext(createTestContext());
        testcase.setName("testBeforeSuite");
        TestCaseMetaInfo metaInfo = new TestCaseMetaInfo();
        metaInfo.setStatus(Status.FINAL);
        
        testcase.setMetaInfo(metaInfo);
        
        TestAction echoAction = new EchoAction();
        testcase.setTestChain(Collections.singletonList(echoAction));
        
        testsuite.setTestSuiteListeners(testSuiteListeners);
        
        TestAction afterAction = new EchoAction();
        testsuite.setTasksAfter(Collections.singletonList(afterAction));
        
        Assert.assertTrue(testsuite.afterSuite());
    }
    
    @Test
    public void testFailAfterSuite() {
        TestSuite testsuite = new TestSuite();
        
        TestCase testcase = new TestCase();
        testcase.setTestContext(createTestContext());
        testcase.setName("testFailAfterSuite");
        TestCaseMetaInfo metaInfo = new TestCaseMetaInfo();
        metaInfo.setStatus(Status.FINAL);
        
        testcase.setMetaInfo(metaInfo);
        
        TestAction echoAction = new EchoAction();
        testcase.setTestChain(Collections.singletonList(echoAction));
        
        testsuite.setTestSuiteListeners(testSuiteListeners);
        
        TestAction failBean = new FailAction();
        testsuite.setTasksAfter(Collections.singletonList(failBean));
        
        Assert.assertFalse(testsuite.afterSuite());
    }
    
    @Test
    public void testTasksBetween() {
        TestSuite testsuite = new TestSuite();
        
        TestCase testcase1 = new TestCase();
        testcase1.setTestContext(createTestContext());
        testcase1.setName("TestCase1");
        TestCaseMetaInfo metaInfo1 = new TestCaseMetaInfo();
        metaInfo1.setStatus(Status.FINAL);
        
        testcase1.setMetaInfo(metaInfo1);
        
        TestAction echoAction = new EchoAction();
        testcase1.setTestChain(Collections.singletonList(echoAction));
        
        TestCase testcase2 = new TestCase();
        testcase2.setTestContext(createTestContext());
        testcase2.setName("TestCase2");
        TestCaseMetaInfo metaInfo2 = new TestCaseMetaInfo();
        metaInfo2.setStatus(Status.FINAL);
        
        testcase2.setMetaInfo(metaInfo2);
        
        TestAction echoAction2 = new EchoAction();
        testcase2.setTestChain(Collections.singletonList(echoAction2));
        
        testsuite.setTestSuiteListeners(testSuiteListeners);
        
        TestAction betweenAction = new EchoAction();
        testsuite.setTasksBetween(Collections.singletonList(betweenAction));
        
        testsuite.beforeTest();
    }
    
    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testFailTasksBetween() {
        TestSuite testsuite = new TestSuite();
        
        TestCase testcase1 = new TestCase();
        testcase1.setTestContext(createTestContext());
        testcase1.setName("TestCase1");
        TestCaseMetaInfo metaInfo1 = new TestCaseMetaInfo();
        metaInfo1.setStatus(Status.FINAL);
        
        testcase1.setMetaInfo(metaInfo1);
        
        TestAction echoAction = new EchoAction();
        testcase1.setTestChain(Collections.singletonList(echoAction));
        
        TestCase testcase2 = new TestCase();
        testcase2.setTestContext(createTestContext());
        testcase2.setName("TestCase2");
        TestCaseMetaInfo metaInfo2 = new TestCaseMetaInfo();
        metaInfo2.setStatus(Status.FINAL);
        
        testcase2.setMetaInfo(metaInfo2);
        
        TestAction echoAction2 = new EchoAction();
        testcase2.setTestChain(Collections.singletonList(echoAction2));
        
        testsuite.setTestSuiteListeners(testSuiteListeners);
        
        TestAction failBean = new FailAction();
        testsuite.setTasksBetween(Collections.singletonList(failBean));

        testsuite.beforeTest();
    }
}
