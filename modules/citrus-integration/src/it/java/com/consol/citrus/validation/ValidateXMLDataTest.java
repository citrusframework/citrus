package com.consol.citrus.validation;

import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.consol.citrus.testng.AbstractIntegrationTest;

/**
 * 
 * @author deppisch Christoph Deppisch Consol* Software GmbH
 * @since 31.10.2008
 */
public class ValidateXMLDataTest extends AbstractIntegrationTest {
    @Test
    public void validateXMLDataTest(ITestContext testContext) {
        executeTest(testContext);
    }
}