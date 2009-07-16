package com.consol.citrus;

import java.util.Collections;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.EchoBean;
import com.consol.citrus.report.SimpleLogReporter;

public class TestMetaInfoTest extends AbstractBaseTest {
    
    @Test
    public void testExcludeDraftTests() {
        TestSuite testsuite = new TestSuite();
        
        TestCase testcase1 = new TestCase();
        testcase1.setName("TestCase1");
        
        testcase1.setTestChain(Collections.singletonList(new EchoBean()));
        
        TestCase testcase2 = new TestCase();
        testcase2.setName("TestCase2");
        TestCaseMetaInfo metaInfo = new TestCaseMetaInfo();
        metaInfo.setStatus("DRAFT");
        
        testcase2.setMetaInfo(metaInfo);
        
        testcase2.setTestChain(Collections.singletonList(new EchoBean()));
        
        testsuite.setReporter(Collections.singletonList(new SimpleLogReporter()));

        Assert.assertTrue(testsuite.beforeSuite());
        Assert.assertTrue(testsuite.run(new TestCase[] {testcase1, testcase2}));
        Assert.assertTrue(testsuite.afterSuite());
        
        Assert.assertEquals(testsuite.getCntSkipped(), 1);
        Assert.assertEquals(testsuite.getCntCasesFail(), 0);
        Assert.assertEquals(testsuite.getCntCasesSuccess(), 1);
    }
}
