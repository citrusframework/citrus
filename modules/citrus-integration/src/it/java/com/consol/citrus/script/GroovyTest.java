package com.consol.citrus.script;

import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.consol.citrus.testng.AbstractIntegrationTest;

/**
 * 
 * @author deppisch Christoph Deppisch Consol* Software GmbH
 * @since 25.02.2009
 */
public class GroovyTest extends AbstractIntegrationTest {
    @Test
    public void groovyTest(ITestContext testContext) {
        executeTest(testContext);
    }
}