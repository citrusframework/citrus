package com.consol.citrus;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.TestCaseMetaInfo.Status;
import com.consol.citrus.actions.EchoBean;
import com.consol.citrus.report.TestListeners;
import com.consol.citrus.report.TestSuiteListeners;

public class TestMetaInfoTest extends AbstractBaseTest {
    
    @Autowired
    TestSuiteListeners testSuiteListeners;
    
    @Autowired
    TestListeners testListeners;
    
    @Test
    public void testExcludeDraftTests() {
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
        metaInfo2.setStatus(Status.DRAFT);
        
        testcase2.setMetaInfo(metaInfo2);
        
        testcase2.setTestChain(Collections.singletonList(new EchoBean()));
        
        testsuite.setTestSuiteListeners(testSuiteListeners);
        testsuite.setTestListeners(testListeners);

        Assert.assertTrue(testsuite.beforeSuite());
        Assert.assertTrue(testsuite.run(new TestCase[] {testcase1, testcase2}));
        Assert.assertTrue(testsuite.afterSuite());
        
        Assert.assertEquals(testsuite.getCntSkipped(), 1);
        Assert.assertEquals(testsuite.getCntCasesFail(), 0);
        Assert.assertEquals(testsuite.getCntCasesSuccess(), 1);
    }
    
    @Test
    public void testIncludeDraftTestWhenExecutingSingleTest() {
        TestSuite testsuite = new TestSuite();
        
        TestCase testcase1 = new TestCase();
        testcase1.setTestContext(createTestContext());
        testcase1.setName("TestCase1");
        
        TestCaseMetaInfo metaInfo = new TestCaseMetaInfo();
        metaInfo.setStatus(Status.DRAFT);
        
        testcase1.setMetaInfo(metaInfo);
        
        testcase1.setTestChain(Collections.singletonList(new EchoBean()));
        
        testsuite.setTestSuiteListeners(testSuiteListeners);
        testsuite.setTestListeners(testListeners);

        Assert.assertTrue(testsuite.beforeSuite());
        Assert.assertTrue(testsuite.run(testcase1));
        Assert.assertTrue(testsuite.afterSuite());
        
        Assert.assertEquals(testsuite.getCntSkipped(), 0);
        Assert.assertEquals(testsuite.getCntCasesFail(), 0);
        Assert.assertEquals(testsuite.getCntCasesSuccess(), 1);
    }
    
    @Test
    public void testExcludeDraftTestsEvenIfNoTestAtAllAreRun() {
        TestSuite testsuite = new TestSuite();
        
        TestCase testcase1 = new TestCase();
        testcase1.setTestContext(createTestContext());
        testcase1.setName("TestCase1");
        
        TestCaseMetaInfo metaInfo = new TestCaseMetaInfo();
        metaInfo.setStatus(Status.DRAFT);
        
        testcase1.setMetaInfo(metaInfo);
        
        testcase1.setTestChain(Collections.singletonList(new EchoBean()));
        
        testsuite.setTestSuiteListeners(testSuiteListeners);
        testsuite.setTestListeners(testListeners);

        Assert.assertTrue(testsuite.beforeSuite());
        Assert.assertTrue(testsuite.run(new TestCase[] {testcase1}));
        Assert.assertTrue(testsuite.afterSuite());
        
        Assert.assertEquals(testsuite.getCntSkipped(), 1);
        Assert.assertEquals(testsuite.getCntCasesFail(), 0);
        Assert.assertEquals(testsuite.getCntCasesSuccess(), 0);
    }
}
