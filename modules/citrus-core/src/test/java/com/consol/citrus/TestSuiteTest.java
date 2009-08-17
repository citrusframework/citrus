package com.consol.citrus;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.TestCaseMetaInfo.Status;
import com.consol.citrus.actions.EchoBean;
import com.consol.citrus.actions.FailBean;
import com.consol.citrus.report.TestListeners;
import com.consol.citrus.report.TestSuiteListeners;

public class TestSuiteTest extends AbstractBaseTest {
    @Autowired
    TestSuiteListeners testSuiteListeners;
    
    @Autowired
    TestListeners testListeners;
    
    @Test
    public void testRunSingleTest() {
        TestSuite testsuite = new TestSuite();
        
        TestCase testcase = new TestCase();
        testcase.setTestContext(createTestContext());
        testcase.setName("testRunSingleTest");
        TestCaseMetaInfo metaInfo = new TestCaseMetaInfo();
        metaInfo.setStatus(Status.FINAL);
        
        testcase.setMetaInfo(metaInfo);
        
        testcase.setTestChain(Collections.singletonList(new EchoBean()));
        
        testsuite.setTestSuiteListeners(testSuiteListeners);
        testsuite.setTestListeners(testListeners);
        
        Assert.assertTrue(testsuite.beforeSuite());
        Assert.assertTrue(testsuite.run(testcase));
        Assert.assertTrue(testsuite.afterSuite());
        
        Assert.assertEquals(testsuite.getSkipped(), 0);
        Assert.assertEquals(testsuite.getFailed(), 0);
        Assert.assertEquals(testsuite.getSuccess(), 1);
    }
    
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
        testsuite.setTestListeners(testListeners);
        
        testsuite.setTasksBefore(Collections.singletonList(new EchoBean()));
        
        Assert.assertTrue(testsuite.beforeSuite());
        Assert.assertTrue(testsuite.run(testcase));
        Assert.assertTrue(testsuite.afterSuite());
        
        Assert.assertEquals(testsuite.getSkipped(), 0);
        Assert.assertEquals(testsuite.getFailed(), 0);
        Assert.assertEquals(testsuite.getSuccess(), 1);
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
        testsuite.setTestListeners(testListeners);
        
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
        testsuite.setTestListeners(testListeners);
        
        testsuite.setTasksAfter(Collections.singletonList(new EchoBean()));
        
        Assert.assertTrue(testsuite.beforeSuite());
        Assert.assertTrue(testsuite.run(testcase));
        Assert.assertTrue(testsuite.afterSuite());
        
        Assert.assertEquals(testsuite.getSkipped(), 0);
        Assert.assertEquals(testsuite.getFailed(), 0);
        Assert.assertEquals(testsuite.getSuccess(), 1);
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
        testsuite.setTestListeners(testListeners);
        
        TestAction failBean = new FailBean();
        testsuite.setTasksAfter(Collections.singletonList(failBean));
        
        Assert.assertTrue(testsuite.beforeSuite());
        Assert.assertTrue(testsuite.run(testcase));
        Assert.assertFalse(testsuite.afterSuite());
        
        Assert.assertEquals(testsuite.getSkipped(), 0);
        Assert.assertEquals(testsuite.getFailed(), 0);
        Assert.assertEquals(testsuite.getSuccess(), 1);
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
        testsuite.setTestListeners(testListeners);
        
        testsuite.setTasksBetween(Collections.singletonList(new EchoBean()));
        
        Assert.assertTrue(testsuite.beforeSuite());
        Assert.assertTrue(testsuite.run(new TestCase[] {testcase1, testcase2}));
        Assert.assertTrue(testsuite.afterSuite());
        
        Assert.assertEquals(testsuite.getSkipped(), 0);
        Assert.assertEquals(testsuite.getFailed(), 0);
        Assert.assertEquals(testsuite.getSuccess(), 2);
    }
    
    @Test
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
        testsuite.setTestListeners(testListeners);
        
        TestAction failBean = new FailBean();
        testsuite.setTasksBetween(Collections.singletonList(failBean));

        Assert.assertTrue(testsuite.beforeSuite());
        Assert.assertFalse(testsuite.run(new TestCase[] {testcase1, testcase2}));
        Assert.assertTrue(testsuite.afterSuite());
        
        Assert.assertEquals(testsuite.getSkipped(), 0);
        Assert.assertEquals(testsuite.getFailed(), 2);
        Assert.assertEquals(testsuite.getSuccess(), 0);
    }
    
    @Test
    public void testRunTests() {
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
        testsuite.setTestListeners(testListeners);

        Assert.assertTrue(testsuite.beforeSuite());
        Assert.assertTrue(testsuite.run(new TestCase[] {testcase1, testcase2}));
        Assert.assertTrue(testsuite.afterSuite());
        
        Assert.assertEquals(testsuite.getSkipped(), 0);
        Assert.assertEquals(testsuite.getFailed(), 0);
        Assert.assertEquals(testsuite.getSuccess(), 2);
    }
    
    @Test
    public void testIncludeTests() {
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
        testsuite.setTestListeners(testListeners);

        testsuite.setIncludeTests(Collections.singletonList("TestCase1"));
        
        Assert.assertTrue(testsuite.beforeSuite());
        Assert.assertTrue(testsuite.run(new TestCase[] {testcase1, testcase2}));
        Assert.assertTrue(testsuite.afterSuite());
        
        Assert.assertEquals(testsuite.getSkipped(), 1);
        Assert.assertEquals(testsuite.getFailed(), 0);
        Assert.assertEquals(testsuite.getSuccess(), 1);
    }
    
    @Test
    public void testPatternIncludeTests() {
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
        
        TestCase testcase3 = new TestCase();
        testcase3.setTestContext(createTestContext());
        testcase3.setName("ExcludeTestCase");
        TestCaseMetaInfo metaInfo3 = new TestCaseMetaInfo();
        metaInfo3.setStatus(Status.FINAL);
        
        testcase3.setMetaInfo(metaInfo3);
        
        testcase3.setTestChain(Collections.singletonList(new EchoBean()));
        
        testsuite.setTestSuiteListeners(testSuiteListeners);
        testsuite.setTestListeners(testListeners);

        testsuite.setIncludeTests(Collections.singletonList("TestCase*"));
        
        Assert.assertTrue(testsuite.beforeSuite());
        Assert.assertTrue(testsuite.run(new TestCase[] {testcase1, testcase2, testcase3}));
        Assert.assertTrue(testsuite.afterSuite());
        
        Assert.assertEquals(testsuite.getSkipped(), 1);
        Assert.assertEquals(testsuite.getFailed(), 0);
        Assert.assertEquals(testsuite.getSuccess(), 2);
        
        testsuite = new TestSuite();
        testsuite.setTestSuiteListeners(testSuiteListeners);
        testsuite.setTestListeners(testListeners);
        
        testsuite.setIncludeTests(Collections.singletonList("*TestCase"));
        
        Assert.assertTrue(testsuite.beforeSuite());
        Assert.assertTrue(testsuite.run(new TestCase[] {testcase1, testcase2, testcase3}));
        Assert.assertTrue(testsuite.afterSuite());
        
        Assert.assertEquals(testsuite.getSkipped(), 2);
        Assert.assertEquals(testsuite.getFailed(), 0);
        Assert.assertEquals(testsuite.getSuccess(), 1);
        
        testsuite = new TestSuite();
        testsuite.setTestSuiteListeners(testSuiteListeners);
        testsuite.setTestListeners(testListeners);
        
        testsuite.setIncludeTests(Collections.singletonList("*TestCase*"));
        
        Assert.assertTrue(testsuite.beforeSuite());
        Assert.assertTrue(testsuite.run(new TestCase[] {testcase1, testcase2, testcase3}));
        Assert.assertTrue(testsuite.afterSuite());
        
        Assert.assertEquals(testsuite.getSkipped(), 0);
        Assert.assertEquals(testsuite.getFailed(), 0);
        Assert.assertEquals(testsuite.getSuccess(), 3);
    }
    
    @Test
    public void testExcludeTests() {
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
        testsuite.setTestListeners(testListeners);

        testsuite.setExcludeTests(Collections.singletonList("TestCase1"));
        
        Assert.assertTrue(testsuite.beforeSuite());
        Assert.assertTrue(testsuite.run(new TestCase[] {testcase1, testcase2}));
        Assert.assertTrue(testsuite.afterSuite());
        
        Assert.assertEquals(testsuite.getSkipped(), 1);
        Assert.assertEquals(testsuite.getFailed(), 0);
        Assert.assertEquals(testsuite.getSuccess(), 1);
    }
    
    @Test
    public void testPatternExcludeTests() {
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
        
        TestCase testcase3 = new TestCase();
        testcase3.setTestContext(createTestContext());
        testcase3.setName("ExcludeTestCase");
        
        testcase3.setTestChain(Collections.singletonList(new EchoBean()));
        
        testsuite.setTestSuiteListeners(testSuiteListeners);
        testsuite.setTestListeners(testListeners);

        testsuite.setExcludeTests(Collections.singletonList("Exclude*"));
        
        Assert.assertTrue(testsuite.beforeSuite());
        Assert.assertTrue(testsuite.run(new TestCase[] {testcase1, testcase2, testcase3}));
        Assert.assertTrue(testsuite.afterSuite());
        
        Assert.assertEquals(testsuite.getSkipped(), 1);
        Assert.assertEquals(testsuite.getFailed(), 0);
        Assert.assertEquals(testsuite.getSuccess(), 2);
        
        testsuite = new TestSuite();
        testsuite.setTestSuiteListeners(testSuiteListeners);
        testsuite.setTestListeners(testListeners);
        
        testsuite.setExcludeTests(Collections.singletonList("*TestCase"));
        
        Assert.assertTrue(testsuite.beforeSuite());
        Assert.assertTrue(testsuite.run(new TestCase[] {testcase1, testcase2, testcase3}));
        Assert.assertTrue(testsuite.afterSuite());
        
        Assert.assertEquals(testsuite.getSkipped(), 1);
        Assert.assertEquals(testsuite.getFailed(), 0);
        Assert.assertEquals(testsuite.getSuccess(), 2);
        
        testsuite = new TestSuite();
        testsuite.setTestSuiteListeners(testSuiteListeners);
        testsuite.setTestListeners(testListeners);
        
        testsuite.setExcludeTests(Collections.singletonList("*TestCase*"));
        
        Assert.assertTrue(testsuite.beforeSuite());
        Assert.assertTrue(testsuite.run(new TestCase[] {testcase1, testcase2, testcase3}));
        Assert.assertTrue(testsuite.afterSuite());
        
        Assert.assertEquals(testsuite.getSkipped(), 3);
        Assert.assertEquals(testsuite.getFailed(), 0);
        Assert.assertEquals(testsuite.getSuccess(), 0);
    }
}
