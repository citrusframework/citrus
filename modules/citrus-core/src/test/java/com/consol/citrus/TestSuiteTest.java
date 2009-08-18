package com.consol.citrus;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.TestCaseMetaInfo.Status;
import com.consol.citrus.actions.EchoBean;
import com.consol.citrus.actions.FailBean;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.report.TestListeners;
import com.consol.citrus.report.TestSuiteListeners;

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
        
        testcase.setTestChain(Collections.singletonList(new EchoBean()));
        
        testsuite.setTestSuiteListeners(testSuiteListeners);
        
        testsuite.setTasksBefore(Collections.singletonList(new EchoBean()));
        
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
        
        testcase.setTestChain(Collections.singletonList(new EchoBean()));
        
        testsuite.setTestSuiteListeners(testSuiteListeners);
        
        TestAction failBean = new FailBean();
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
        
        testcase.setTestChain(Collections.singletonList(new EchoBean()));
        
        testsuite.setTestSuiteListeners(testSuiteListeners);
        
        testsuite.setTasksAfter(Collections.singletonList(new EchoBean()));
        
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
        
        testcase.setTestChain(Collections.singletonList(new EchoBean()));
        
        testsuite.setTestSuiteListeners(testSuiteListeners);
        
        TestAction failBean = new FailBean();
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
        
        testcase1.setTestChain(Collections.singletonList(new EchoBean()));
        
        TestCase testcase2 = new TestCase();
        testcase2.setTestContext(createTestContext());
        testcase2.setName("TestCase2");
        TestCaseMetaInfo metaInfo2 = new TestCaseMetaInfo();
        metaInfo2.setStatus(Status.FINAL);
        
        testcase2.setMetaInfo(metaInfo2);
        
        testcase2.setTestChain(Collections.singletonList(new EchoBean()));
        
        testsuite.setTestSuiteListeners(testSuiteListeners);
        
        testsuite.setTasksBetween(Collections.singletonList(new EchoBean()));
        
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
        
        testcase1.setTestChain(Collections.singletonList(new EchoBean()));
        
        TestCase testcase2 = new TestCase();
        testcase2.setTestContext(createTestContext());
        testcase2.setName("TestCase2");
        TestCaseMetaInfo metaInfo2 = new TestCaseMetaInfo();
        metaInfo2.setStatus(Status.FINAL);
        
        testcase2.setMetaInfo(metaInfo2);
        
        testcase2.setTestChain(Collections.singletonList(new EchoBean()));
        
        testsuite.setTestSuiteListeners(testSuiteListeners);
        
        TestAction failBean = new FailBean();
        testsuite.setTasksBetween(Collections.singletonList(failBean));

        testsuite.beforeTest();
    }
}
