package com.consol.citrus.http;

import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.consol.citrus.testng.AbstractIntegrationTest;

/**
 * 
 * @author deppisch Christoph Deppisch Consol* Software GmbH
 * @since 31.10.2008
 */
public class HttpServerTest extends AbstractIntegrationTest {
    @Test
    public void httpServerTest(ITestContext testContext) {
        executeTest(testContext);
    }
}