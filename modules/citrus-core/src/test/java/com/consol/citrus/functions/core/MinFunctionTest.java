package com.consol.citrus.functions.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.AbstractBaseTest;
import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.functions.core.MinFunction;

public class MinFunctionTest extends AbstractBaseTest {
    MinFunction function = new MinFunction();
    
    @Test
    public void testFunction() {
        List params = new ArrayList();
        params.add("3");
        params.add("5.2");
        params.add("4.7");
        
        Assert.assertEquals(function.execute(params), "3.0");
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
