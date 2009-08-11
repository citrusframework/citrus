package com.consol.citrus.actions;

import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.consol.citrus.testng.AbstractTestNGCitrusTest;

/**
 * 
 * @author deppisch Christoph Deppisch Consol* Software GmbH
 * @since 31.10.2008
 */
public class JmsLoopBackTests extends AbstractTestNGCitrusTest {
    @Test
    public void jmsLoopBackTest(ITestContext testContext) {
        executeTest(testContext);
    }
}