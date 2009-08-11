package com.consol.citrus.variables;

import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.consol.citrus.testng.AbstractIntegrationTest;

/**
 * 
 * @author deppisch Christoph Deppisch Consol* Software GmbH
 * @since 31.10.2008
 */
public class VariablesOrderTest extends AbstractIntegrationTest {
    @Test
    public void varibalesOrderTest(ITestContext testContext) {
        executeTest(testContext);
    }
}