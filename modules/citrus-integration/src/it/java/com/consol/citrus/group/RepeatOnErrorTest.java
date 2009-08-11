package com.consol.citrus.group;

import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.consol.citrus.testng.AbstractTestNGCitrusTest;

/**
 * 
 * @author deppisch Christoph Deppisch Consol* Software GmbH
 * @since 31.10.2008
 */
public class RepeatOnErrorTest extends AbstractTestNGCitrusTest {
    @Test
    public void repeatOnErrorTest(ITestContext testContext) {
        executeTest(testContext);
    }
}