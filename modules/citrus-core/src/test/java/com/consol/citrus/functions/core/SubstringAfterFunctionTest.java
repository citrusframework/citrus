package com.consol.citrus.functions.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.AbstractBaseTest;
import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.functions.core.SubstringAfterFunction;

public class SubstringAfterFunctionTest extends AbstractBaseTest {
    SubstringAfterFunction function = new SubstringAfterFunction();
    
    @Test
    public void testFunction() {
        List params = new ArrayList();
        params.add("Hallo,TestFramework");
        params.add(",");
        Assert.assertEquals(function.execute(params), "TestFramework");
        
        params.clear();
        params.add("This is a test");
        params.add("a");
        Assert.assertEquals(function.execute(params), " test");
    }
    
    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testNoParameters() {
        function.execute(Collections.<String>emptyList());
    }
}
