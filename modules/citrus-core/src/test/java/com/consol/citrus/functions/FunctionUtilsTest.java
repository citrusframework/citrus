/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.functions;

import java.util.Collections;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.exceptions.NoSuchFunctionException;
import com.consol.citrus.exceptions.NoSuchFunctionLibraryException;
import com.consol.citrus.functions.core.CurrentDateFunction;
import com.consol.citrus.testng.AbstractBaseTest;

/**
 * @author Christoph Deppisch
 */
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
