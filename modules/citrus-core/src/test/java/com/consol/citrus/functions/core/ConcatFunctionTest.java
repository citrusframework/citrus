package com.consol.citrus.functions.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.AbstractBaseTest;
import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.functions.core.ConcatFunction;

public class ConcatFunctionTest extends AbstractBaseTest {
    ConcatFunction function = new ConcatFunction();
    
    @Test
    public void testFunction() {
        List params = new ArrayList();
        params.add("Hallo ");
        params.add("TestFramework");
        params.add("!");
        
        Assert.assertEquals(function.execute(params), "Hallo TestFramework!");
    }
    
    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testNoParameters() {
        function.execute(Collections.emptyList());
    }
}
