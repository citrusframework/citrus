package com.consol.citrus.functions.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.AbstractBaseTest;
import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.functions.core.RandomStringFunction;

public class RandomStringFunctionTest extends AbstractBaseTest {
    RandomStringFunction function = new RandomStringFunction();
    
    @Test
    public void testFunction() {
        List params = new ArrayList();
        params.add("3");
        
        Assert.assertTrue(function.execute(params).length() == 3);
        
        params = new ArrayList();
        params.add("3");
        params.add("UPPERCASE");
        
        Assert.assertTrue(function.execute(params).length() == 3);
        
        params = new ArrayList();
        params.add("3");
        params.add("LOWERCASE");
        
        Assert.assertTrue(function.execute(params).length() == 3);
        
        params = new ArrayList();
        params.add("3");
        params.add("MIXED");
        
        Assert.assertTrue(function.execute(params).length() == 3);
        
        params = new ArrayList();
        params.add("3");
        params.add("UNKNOWN");
        
        Assert.assertTrue(function.execute(params).length() == 3);
    }
    
    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testWrongParameterUsage() {
        function.execute(Collections.singletonList("-1"));
    }
    
    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testNoParameters() {
        function.execute(Collections.emptyList());
    }
    
    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testTooManyParameters() {
        List params = new ArrayList();
        params.add("3");
        params.add("UPPERCASE");
        params.add("too much");
        
        function.execute(params);
    }
}
