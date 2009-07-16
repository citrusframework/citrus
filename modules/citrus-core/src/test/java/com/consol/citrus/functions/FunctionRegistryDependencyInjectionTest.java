package com.consol.citrus.functions;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.AbstractBaseTest;
import com.consol.citrus.context.TestContext;

public class FunctionRegistryDependencyInjectionTest extends AbstractBaseTest {
    @Test
    public void testDependencyInjection() {
        TestContext context = new TestContext();
        
        Assert.assertNotNull(context.getFunctionRegistry());
    }
}
