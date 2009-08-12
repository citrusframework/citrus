package com.consol.citrus.samples.flightbooking;

import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.consol.citrus.testng.AbstractTestNGCitrusTest;

/**
 * TODO: Description
 *
 * @author Christoph Deppisch
 * @since 2009-08-12
 */
public class FlightBookingTest extends AbstractTestNGCitrusTest {
    @Test
    public void flightBookingTest(ITestContext testContext) {
        executeTest(testContext);
    }
}
