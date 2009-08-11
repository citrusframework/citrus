package com.consol.citrus.variables;

import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.consol.citrus.testng.AbstractTestNGCitrusTest;

/**
 * 
 * @author deppisch Christoph Deppisch Consol* Software GmbH
 * @since 31.10.2008
 */
public class UseVariablesTest extends AbstractTestNGCitrusTest {
    @Test
    public void useVariablesTest(ITestContext testContext) {
        executeTest(testContext);
    }
}