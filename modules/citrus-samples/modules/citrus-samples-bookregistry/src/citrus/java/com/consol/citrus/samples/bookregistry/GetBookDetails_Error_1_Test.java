package com.consol.citrus.samples.bookregistry;

import org.testng.ITestContext;
import org.testng.annotations.*;

import com.consol.citrus.testng.AbstractTestNGCitrusTest;

/**
 * TODO: Description
 *
 * @author Christoph Deppisch
 * @since 2010-02-24
 */
public class GetBookDetails_Error_1_Test extends AbstractTestNGCitrusTest {
    
    BookRegistryDemo demo = new BookRegistryDemo();
    
    @BeforeSuite(alwaysRun = true)
    public void beforeSuite(ITestContext testContext) throws Exception {
        demo.start();
        
        super.beforeSuite(testContext);
    }
    
    @Test
    public void getBookDetails_Error_1_Test(ITestContext testContext) {
        executeTest(testContext);
    }
    
    @AfterSuite(alwaysRun = true)
    public void afterSuite(ITestContext testContext) {
        super.afterSuite(testContext);
        
        demo.stop();
    }
}
