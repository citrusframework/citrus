package com.consol.citrus.container;

import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.consol.citrus.testng.AbstractTestNGCitrusTest;

/**
 * @author Christoph Deppisch
 * @since 2011-01-26
 */
public class TemplateVariablesITest extends AbstractTestNGCitrusTest {
    @Test
    public void testVariablesITest(ITestContext testContext) {
        executeTest(testContext);
    }
}
