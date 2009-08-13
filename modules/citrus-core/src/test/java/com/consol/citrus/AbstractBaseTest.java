package com.consol.citrus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.functions.FunctionRegistry;
import com.consol.citrus.variable.GlobalVariables;

@ContextConfiguration(locations = {"spring/root-application-ctx.xml",
                                   "/application-ctx.xml", 
                                   "functions/citrus-function-ctx.xml"})
public abstract class AbstractBaseTest extends AbstractTestNGSpringContextTests {
    protected TestContext context;
    
    @Autowired
    FunctionRegistry functionRegistry;
    
    @Autowired
    GlobalVariables globalVariables;
    
    @BeforeMethod
    public void setup() {
        context = new TestContext();
        
        context.setFunctionRegistry(functionRegistry);
        context.setGlobalVariables(globalVariables);
    }
    
    protected TestContext createTestContext() {
        TestContext newContext = new TestContext();
        
        newContext.setFunctionRegistry(functionRegistry);
        newContext.setGlobalVariables(globalVariables);
        
        return newContext;
    }
}
