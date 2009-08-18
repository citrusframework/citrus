package com.consol.citrus;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import com.consol.citrus.report.TestListeners;
import com.consol.citrus.report.TestSuiteListeners;

public class TestMetaInfoTest extends AbstractBaseTest {
    
    @Autowired
    TestSuiteListeners testSuiteListeners;
    
    @Autowired
    TestListeners testListeners;
    
    @Test
    public void testExcludeDraftTests() {
        //TODO code this test
    }
    
    @Test
    public void testIncludeDraftTestWhenExecutingSingleTest() {
        //TODO code this test
    }
    
    @Test
    public void testExcludeDraftTestsEvenIfNoTestAtAllAreRun() {
        //TODO code this test
    }
}
