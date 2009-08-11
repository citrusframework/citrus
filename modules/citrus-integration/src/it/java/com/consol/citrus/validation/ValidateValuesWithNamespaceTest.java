package com.consol.citrus.validation;

import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.consol.citrus.testng.AbstractIntegrationTest;

/**
 * 
 * @author deppisch Christoph Deppisch Consol* Software GmbH
 * @since 31.10.2008
 */
public class ValidateValuesWithNamespaceTest extends AbstractIntegrationTest {
    @Test
    public void validateValuesWithNamespaceTest(ITestContext testContext) {
        executeTest(testContext);
    }
}