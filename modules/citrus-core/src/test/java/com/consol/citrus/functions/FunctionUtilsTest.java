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

import com.consol.citrus.exceptions.*;
import com.consol.citrus.functions.core.CurrentDateFunction;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;

/**
 * @author Christoph Deppisch
 */
public class FunctionUtilsTest extends AbstractTestNGUnitTest {
    @Test
    public void testResolveFunction() {
        Assert.assertEquals(FunctionUtils.resolveFunction("citrus:concat('Hello', ' TestFramework!')", context), "Hello TestFramework!");
        Assert.assertEquals(FunctionUtils.resolveFunction("citrus:concat('citrus', ':citrus')", context), "citrus:citrus");
        Assert.assertEquals(FunctionUtils.resolveFunction("citrus:concat('citrus:citrus')", context), "citrus:citrus");
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
        Assert.assertEquals(FunctionUtils.resolveFunction("citrus:concat(citrus:currentDate('yyyy-mm-dd'))", context), new CurrentDateFunction().execute(Collections.singletonList("yyyy-mm-dd"), context));
        Assert.assertEquals(FunctionUtils.resolveFunction("citrus:concat('Now is: ', citrus:currentDate('yyyy-mm-dd'))", context), "Now is: " + new CurrentDateFunction().execute(Collections.singletonList("yyyy-mm-dd"), context));
        Assert.assertEquals(FunctionUtils.resolveFunction("citrus:concat(citrus:currentDate('yyyy-mm-dd'), ' ', citrus:concat('Hello', ' TestFramework!'))", context), new CurrentDateFunction().execute(Collections.singletonList("yyyy-mm-dd"), context) + " Hello TestFramework!");
    }
    
    @Test
    public void testWithNestedFunctionsAndVariables() {
        context.setVariable("greeting", "Hello");
        context.setVariable("dateFormat", "yyyy-mm-dd");
        
        Assert.assertEquals(FunctionUtils.resolveFunction("citrus:concat(citrus:currentDate('${dateFormat}'), ' ', citrus:concat(${greeting}, ' TestFramework!'))", context), new CurrentDateFunction().execute(Collections.singletonList("yyyy-mm-dd"), context) + " Hello TestFramework!");
    }

    @Test
    public void testWithCommaValue() {
        Assert.assertEquals(FunctionUtils.resolveFunction("citrus:upperCase('Yes, I like Citrus!')", context), "YES, I LIKE CITRUS!");
        Assert.assertEquals(FunctionUtils.resolveFunction("citrus:upperCase('Yes,I like Citrus!')", context), "YES,I LIKE CITRUS!");
        Assert.assertEquals(FunctionUtils.resolveFunction("citrus:upperCase('Yes', 'I like Citrus!')", context), "YES");
        Assert.assertEquals(FunctionUtils.resolveFunction("citrus:concat('Hello Yes, I like Citrus!')", context), "Hello Yes, I like Citrus!");
        Assert.assertEquals(FunctionUtils.resolveFunction("citrus:concat('Hello Yes,I like Citrus!')", context), "Hello Yes,I like Citrus!");
        Assert.assertEquals(FunctionUtils.resolveFunction("citrus:concat('Hello Yes , I like Citrus!')", context), "Hello Yes , I like Citrus!");
        Assert.assertEquals(FunctionUtils.resolveFunction("citrus:concat('Hello Yes, I like Citrus!', 'Hello Yes,we like Citrus!')", context), "Hello Yes, I like Citrus!Hello Yes,we like Citrus!");
        Assert.assertEquals(FunctionUtils.resolveFunction("citrus:escapeXml('<Message>Hello Yes, I like Citrus!</Message>')", context), "&lt;Message&gt;Hello Yes, I like Citrus!&lt;/Message&gt;");
        Assert.assertEquals(FunctionUtils.resolveFunction("citrus:escapeXml('<Message>Hello Yes , I like Citrus!</Message>')", context), "&lt;Message&gt;Hello Yes , I like Citrus!&lt;/Message&gt;");
    }
    
    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testInvalidFunction() {
        FunctionUtils.resolveFunction("citrus:citrus", context);
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
