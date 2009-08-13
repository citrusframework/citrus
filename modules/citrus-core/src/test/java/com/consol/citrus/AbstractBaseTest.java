package com.consol.citrus;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;

import com.consol.citrus.context.TestContext;

@ContextConfiguration(locations = {"spring/root-application-ctx.xml",
                                   "/application-ctx.xml", 
                                   "functions/citrus-function-ctx.xml"})
public abstract class AbstractBaseTest extends AbstractTestNGSpringContextTests {
    protected TestContext context;
    
    @BeforeMethod
    public void setup() {
        context = new TestContext();
    }
}
