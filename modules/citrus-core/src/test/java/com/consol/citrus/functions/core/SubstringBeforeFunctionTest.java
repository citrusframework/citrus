package com.consol.citrus.functions.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.AbstractBaseTest;
import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.functions.core.SubstringBeforeFunction;

public class SubstringBeforeFunctionTest extends AbstractBaseTest {
    SubstringBeforeFunction function = new SubstringBeforeFunction();
    
    @Test
    public void testFunction() {
        List params = new ArrayList();
        params.add("Hallo,TestFramework");
        params.add(",");
        Assert.assertEquals(function.execute(params), "Hallo");
        
        params.clear();
        params.add("This is a test");
        params.add("a");
        Assert.assertEquals(function.execute(params), "This is ");
    }
    
    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testNoParameters() {
        function.execute(Collections.emptyList());
    }
}
