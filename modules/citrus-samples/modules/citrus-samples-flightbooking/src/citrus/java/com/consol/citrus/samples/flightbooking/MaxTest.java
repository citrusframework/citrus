package com.consol.citrus.samples.flightbooking;

import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.consol.citrus.testng.AbstractTestNGCitrusTest;

public class MaxTest extends AbstractTestNGCitrusTest {
    @Test
    public void maxTest(ITestContext testContext) {
        executeTest(testContext);
    }

}
