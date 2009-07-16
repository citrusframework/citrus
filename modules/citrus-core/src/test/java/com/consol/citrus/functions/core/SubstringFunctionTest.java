package com.consol.citrus.functions.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.AbstractBaseTest;
import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.functions.core.SubstringFunction;

public class SubstringFunctionTest extends AbstractBaseTest {
    SubstringFunction function = new SubstringFunction();
    
    @Test
    public void testFunction() {
        List params = new ArrayList();
        params.add("Hallo,TestFramework");
        params.add("6");
        Assert.assertEquals(function.execute(params), "TestFramework");
        
        params.clear();
        params.add("This is a test");
        params.add("0");
        Assert.assertEquals(function.execute(params), "This is a test");
    }
    
    @Test
    public void testEndIndex() {
        List params = new ArrayList();
        params.add("Hallo,TestFramework");
        params.add("6");
        params.add("10");
        Assert.assertEquals(function.execute(params), "Test");
        
        params.clear();
        params.add("This is a test");
        params.add("0");
        params.add("4");
        Assert.assertEquals(function.execute(params), "This");
    }

    @Test(expectedExceptions = {StringIndexOutOfBoundsException.class})
    public void testIndexOutOfBounds() {
        List params = new ArrayList();
        params.add("Test");
        params.add("-1");
        function.execute(params);
    }
    
    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testNoParameters() {
        function.execute(Collections.emptyList());
    }
}
