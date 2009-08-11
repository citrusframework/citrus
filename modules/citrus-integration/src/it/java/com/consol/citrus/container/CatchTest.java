package com.consol.citrus.container;

import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.consol.citrus.testng.AbstractIntegrationTest;

/**
 * 
 * @author deppisch Christoph Deppisch Consol* Software GmbH
 * @since 31.10.2008
 */
public class CatchTest extends AbstractIntegrationTest {
    @Test
    public void catchTest(ITestContext testContext) {
        executeTest(testContext);
    }
}