package com.consol.citrus.functions.core;

import java.util.Collections;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.AbstractBaseTest;
import com.consol.citrus.exceptions.InvalidFunctionUsageException;
import com.consol.citrus.functions.core.StringLengthFunction;

public class StringLengthFunctionTest extends AbstractBaseTest {
    StringLengthFunction function = new StringLengthFunction();
    
    @Test
    public void testFunction() {
        Assert.assertEquals(function.execute(Collections.singletonList("Hallo")), "5");
        Assert.assertEquals(function.execute(Collections.singletonList("Hallo TestFramework!")), "20");
    }
    
    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testNoParameters() {
        function.execute(Collections.<String>emptyList());
    }
}
