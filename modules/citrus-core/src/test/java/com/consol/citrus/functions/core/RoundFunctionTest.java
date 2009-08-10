package com.consol.citrus.functions.core;

import java.util.Collections;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.AbstractBaseTest;
import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.functions.core.RoundFunction;

public class RoundFunctionTest extends AbstractBaseTest {
    RoundFunction function = new RoundFunction();
    
    @Test
    public void testFunction() {
        Assert.assertEquals(function.execute(Collections.singletonList("5.0")), "5");
        Assert.assertEquals(function.execute(Collections.singletonList("5.2")), "5");
        Assert.assertEquals(function.execute(Collections.singletonList("5.7")), "6");
        Assert.assertEquals(function.execute(Collections.singletonList("-5.0")), "-5");
        Assert.assertEquals(function.execute(Collections.singletonList("-5.2")), "-5");
        Assert.assertEquals(function.execute(Collections.singletonList("-5.7")), "-6");
        Assert.assertEquals(function.execute(Collections.singletonList("5")), "5");
        Assert.assertEquals(function.execute(Collections.singletonList("-5")), "-5");
        Assert.assertEquals(function.execute(Collections.singletonList("5.5")), "6");
    }
    
    @Test(expectedExceptions = {NumberFormatException.class})
    public void testWrongParameterUsage() {
        function.execute(Collections.singletonList("no digit"));
    }
    
    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testNoParameters() {
        function.execute(Collections.<String>emptyList());
    }
}
