package com.consol.citrus.script;

import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.consol.citrus.testng.AbstractTestNGCitrusTest;

/**
 * 
 * @author deppisch Christoph Deppisch Consol* Software GmbH
 * @since 25.02.2009
 */
public class GroovyActionIntegrationTest extends AbstractTestNGCitrusTest {
    @Test
    public void groovyActionIntegrationTest(ITestContext testContext) {
        executeTest(testContext);
    }
}