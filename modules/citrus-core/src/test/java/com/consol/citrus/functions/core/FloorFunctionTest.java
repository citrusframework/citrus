package com.consol.citrus.functions.core;

import java.util.Collections;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.AbstractBaseTest;
import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.functions.core.FloorFunction;

public class FloorFunctionTest extends AbstractBaseTest {
    FloorFunction function = new FloorFunction();
    
    @Test
    public void testFunction() {
        Assert.assertEquals(function.execute(Collections.singletonList("0.0")), "0.0");
        Assert.assertEquals(function.execute(Collections.singletonList("0")), "0.0");
        Assert.assertEquals(function.execute(Collections.singletonList("0.3")), "0.0");
        Assert.assertEquals(function.execute(Collections.singletonList("1")), "1.0");
        Assert.assertEquals(function.execute(Collections.singletonList("-1.5")), "-2.0");
        Assert.assertEquals(function.execute(Collections.singletonList("1.3")), "1.0");
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
