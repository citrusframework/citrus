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

/**
 * @author Christoph Deppisch
 */
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
        testcase.setActions(Collections.singletonList(echoAction));
        
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
        testcase.setActions(Collections.singletonList(echoAction));
        
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
        testcase.setActions(Collections.singletonList(echoAction));
        
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
        testcase.setActions(Collections.singletonList(echoAction));
        
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
        testcase1.setActions(Collections.singletonList(echoAction));
        
        TestCase testcase2 = new TestCase();
        testcase2.setTestContext(createTestContext());
        testcase2.setName("TestCase2");
        TestCaseMetaInfo metaInfo2 = new TestCaseMetaInfo();
        metaInfo2.setStatus(Status.FINAL);
        
        testcase2.setMetaInfo(metaInfo2);
        
        TestAction echoAction2 = new EchoAction();
        testcase2.setActions(Collections.singletonList(echoAction2));
        
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
        testcase1.setActions(Collections.singletonList(echoAction));
        
        TestCase testcase2 = new TestCase();
        testcase2.setTestContext(createTestContext());
        testcase2.setName("TestCase2");
        TestCaseMetaInfo metaInfo2 = new TestCaseMetaInfo();
        metaInfo2.setStatus(Status.FINAL);
        
        testcase2.setMetaInfo(metaInfo2);
        
        TestAction echoAction2 = new EchoAction();
        testcase2.setActions(Collections.singletonList(echoAction2));
        
        testsuite.setTestSuiteListeners(testSuiteListeners);
        
        TestAction failBean = new FailAction();
        testsuite.setTasksBetween(Collections.singletonList(failBean));

        testsuite.beforeTest();
    }
}
