package com.consol.citrus.functions;

import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.consol.citrus.testng.AbstractTestNGCitrusTest;

/**
 * 
 * @author deppisch Christoph Deppisch Consol* Software GmbH
 * @since 31.10.2008
 */
public class CustomFunctionTest extends AbstractTestNGCitrusTest {
    @Test
    public void customFunctionTest(ITestContext testContext) {
        executeTest(testContext);
    }
}