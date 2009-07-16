package com.consol.citrus.functions;

import java.util.Collections;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.AbstractBaseTest;
import com.consol.citrus.exceptions.NoSuchFunctionException;
import com.consol.citrus.exceptions.NoSuchFunctionLibraryException;
import com.consol.citrus.functions.core.CurrentDateFunction;

public class FunctionUtilsTest extends AbstractBaseTest {
    @Test
    public void testResolveFunction() {
        Assert.assertEquals(FunctionUtils.resolveFunction("citrus:concat('Hello', ' TestFramework!')", context), "Hello TestFramework!");
    }
    
    @Test
    public void testWithVariables() {
        context.setVariable("greeting", "Hello");
        context.setVariable("text", "TestFramework!");
        
        Assert.assertEquals(FunctionUtils.resolveFunction("citrus:concat('Hello', ' ', ${text})", context), "Hello TestFramework!");
        Assert.assertEquals(FunctionUtils.resolveFunction("citrus:concat(${greeting}, ' ', ${text})", context), "Hello TestFramework!");
    }
    
    @Test
    public void testWithNestedFunctions() {
        Assert.assertEquals(FunctionUtils.resolveFunction("citrus:concat(citrus:currentDate('yyyy-mm-dd'))", context), new CurrentDateFunction().execute(Collections.singletonList("yyyy-mm-dd")));
        Assert.assertEquals(FunctionUtils.resolveFunction("citrus:concat('Now is: ', citrus:currentDate('yyyy-mm-dd'))", context), "Now is: " + new CurrentDateFunction().execute(Collections.singletonList("yyyy-mm-dd")));
        Assert.assertEquals(FunctionUtils.resolveFunction("citrus:concat(citrus:currentDate('yyyy-mm-dd'), ' ', citrus:concat('Hello', ' TestFramework!'))", context), new CurrentDateFunction().execute(Collections.singletonList("yyyy-mm-dd")) + " Hello TestFramework!");
    }
    
    @Test
    public void testWithNestedFunctionsAndVariables() {
        context.setVariable("greeting", "Hello");
        context.setVariable("dateFormat", "yyyy-mm-dd");
        
        Assert.assertEquals(FunctionUtils.resolveFunction("citrus:concat(citrus:currentDate('${dateFormat}'), ' ', citrus:concat(${greeting}, ' TestFramework!'))", context), new CurrentDateFunction().execute(Collections.singletonList("yyyy-mm-dd")) + " Hello TestFramework!");
    }
    
    @Test(expectedExceptions = {NoSuchFunctionException.class})
    public void testUnknownFunction() {
        FunctionUtils.resolveFunction("citrus:functiondoesnotexist()", context);
    }
    
    @Test(expectedExceptions = {NoSuchFunctionLibraryException.class})
    public void testUnknownFunctionLibrary() {
        FunctionUtils.resolveFunction("doesnotexist:concat('Hello', ' TestFramework!')", context);
    }
}
