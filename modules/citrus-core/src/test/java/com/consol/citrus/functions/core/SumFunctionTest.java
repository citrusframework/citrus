package com.consol.citrus.functions.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.AbstractBaseTest;
import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.functions.core.SumFunction;

public class SumFunctionTest extends AbstractBaseTest {
    SumFunction function = new SumFunction();
    
    @Test
    public void testFunction() {
        List params = new ArrayList();
        params.add("3");
        params.add("5.3");
        params.add("4.7");
        params.add("0");
        
        Assert.assertEquals(function.execute(params), "13.0");
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
