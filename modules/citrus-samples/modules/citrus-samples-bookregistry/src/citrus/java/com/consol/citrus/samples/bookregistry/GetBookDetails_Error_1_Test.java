package com.consol.citrus.samples.bookregistry;

import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.consol.citrus.samples.CitrusSamplesDemo;
import com.consol.citrus.samples.common.DemoAwareTestNGCitrusTest;

/**
 * TODO: Description
 *
 * @author Christoph Deppisch
 * @since 2010-02-24
 */
public class GetBookDetails_Error_1_Test extends DemoAwareTestNGCitrusTest {
    
    BookRegistryDemo demo = new BookRegistryDemo();
    
    @Test
    public void getBookDetails_Error_1_Test(ITestContext testContext) {
        executeTest(testContext);
    }
    
    @Override
    public CitrusSamplesDemo getDemo() {
        return demo;
    }
}
