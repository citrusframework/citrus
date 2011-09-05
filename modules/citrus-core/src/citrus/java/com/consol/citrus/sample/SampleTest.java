package com.consol.citrus.sample;

import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.consol.citrus.testng.AbstractTestNGCitrusTest;

/**
 * This is a sample test
 *
 * @author Christoph
 * @since 2011-09-05
 */
public class SampleTest extends AbstractTestNGCitrusTest {
    @Test
    public void sampleTest(ITestContext testContext) {
        executeTest(testContext);
    }
}
