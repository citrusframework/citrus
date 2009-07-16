package com.consol.citrus.functions.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.AbstractBaseTest;
import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.functions.core.TranslateFunction;

public class TranslateFunctionTest extends AbstractBaseTest {
    TranslateFunction function = new TranslateFunction();
    
    @Test
    public void testFunction() {
        List params = new ArrayList();
        params.add("H.llo TestFr.mework");
        params.add("\\.");
        params.add("a");
        
        Assert.assertEquals(function.execute(params), "Hallo TestFramework");
    }
    
    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testMissingParameter() {
        List params = new ArrayList();
        params.add("H.llo TestFr.mework");
        params.add("\\.");
        function.execute(params);
    }
    
    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testNoParameters() {
        function.execute(Collections.emptyList());
    }
}
