package com.consol.citrus.validation;

import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.consol.citrus.testng.AbstractIntegrationTest;

/**
 * 
 * @author deppisch Christoph Deppisch Consol* Software GmbH
 * @since 31.10.2008
 */
public class ValidateValuesTest extends AbstractIntegrationTest {
    @Test
    public void validateValuesTest(ITestContext testContext) {
        executeTest(testContext);
    }
}