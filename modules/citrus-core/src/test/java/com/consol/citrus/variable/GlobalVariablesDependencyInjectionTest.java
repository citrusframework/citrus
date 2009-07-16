package com.consol.citrus.variable;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.AbstractBaseTest;
import com.consol.citrus.context.TestContext;

public class GlobalVariablesDependencyInjectionTest extends AbstractBaseTest {
    @Test
    public void testDependencyInjection() {
        TestContext context = new TestContext();
        
        Assert.assertNotNull(context.getGlobalVariables());
    }
}
