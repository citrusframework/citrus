package com.consol.citrus.ws;

import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.consol.citrus.testng.AbstractIntegrationTest;

/**
 * 
 * @author deppisch Christoph Deppisch Consol* Software GmbH
 * @since 31.10.2008
 */
public class WebServiceClientTest extends AbstractIntegrationTest {
    @Test
    public void webServiceClientTest(ITestContext testContext) {
        executeTest(testContext);
    }
}