package com.consol.citrus.functions.core;

import java.util.Collections;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.AbstractBaseTest;
import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.functions.core.AbsoluteFunction;

public class AbsoluteFunctionTest extends AbstractBaseTest {
    AbsoluteFunction function = new AbsoluteFunction();
    
    @Test
    public void testFunction() {
        Assert.assertEquals(function.execute(Collections.singletonList("-0.0")), "0.0");
        Assert.assertEquals(function.execute(Collections.singletonList("-0")), "0");
        Assert.assertEquals(function.execute(Collections.singletonList("2.0")), "2.0");
        Assert.assertEquals(function.execute(Collections.singletonList("2")), "2");
        Assert.assertEquals(function.execute(Collections.singletonList("2.5")), "2.5");
        Assert.assertEquals(function.execute(Collections.singletonList("-2.0")), "2.0");
        Assert.assertEquals(function.execute(Collections.singletonList("-2")), "2");
        Assert.assertEquals(function.execute(Collections.singletonList("-2.5")), "2.5");
    }
    
    @Test(expectedExceptions = {NumberFormatException.class})
    public void testWrongParameterUsage() {
        function.execute(Collections.singletonList("no digit"));
    }
    
    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testNoParameters() {
        function.execute(Collections.emptyList());
    }
}
