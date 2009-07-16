package com.consol.citrus.functions.core;

import java.util.Collections;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.AbstractBaseTest;
import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.functions.core.UpperCaseFunction;

public class UpperCaseFunctionTest extends AbstractBaseTest {
    UpperCaseFunction function = new UpperCaseFunction();
    
    @Test
    public void testFunction() {
        Assert.assertEquals(function.execute(Collections.singletonList("1000")), "1000");
        Assert.assertEquals(function.execute(Collections.singletonList("Hallo TestFramework!")), "HALLO TESTFRAMEWORK!");
        Assert.assertEquals(function.execute(Collections.singletonList("Today is: 09.02.2009")), "TODAY IS: 09.02.2009");
    }
    
    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testNoParameters() {
        function.execute(Collections.emptyList());
    }
}
