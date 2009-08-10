package com.consol.citrus.functions.core;

import java.util.Collections;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.AbstractBaseTest;
import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.functions.core.LowerCaseFunction;

public class LowerCaseFunctionTest extends AbstractBaseTest {
    LowerCaseFunction function = new LowerCaseFunction();
    
    @Test
    public void testFunction() {
        Assert.assertEquals(function.execute(Collections.singletonList("1000")), "1000");
        Assert.assertEquals(function.execute(Collections.singletonList("Hallo TestFramework!")), "hallo testframework!");
        Assert.assertEquals(function.execute(Collections.singletonList("Today is: 09.02.2009")), "today is: 09.02.2009");
    }
    
    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testNoParameters() {
        function.execute(Collections.<String>emptyList());
    }
}
