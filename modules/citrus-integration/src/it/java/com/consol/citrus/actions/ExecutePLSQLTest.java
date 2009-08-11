package com.consol.citrus.actions;

import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.consol.citrus.testng.AbstractIntegrationTest;

/**
 * 
 * @author deppisch Christoph Deppisch Consol* Software GmbH
 * @since 31.10.2008
 */
public class ExecutePLSQLTest extends AbstractIntegrationTest {
    @Test
    public void executePLSQLTest(ITestContext testContext) {
        executeTest(testContext);
    }
}