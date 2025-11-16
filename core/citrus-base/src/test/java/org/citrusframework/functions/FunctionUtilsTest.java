/*
 * Copyright the original author or authors.
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

package org.citrusframework.functions;

import java.util.Collections;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.exceptions.NoSuchFunctionException;
import org.citrusframework.exceptions.NoSuchFunctionLibraryException;
import org.citrusframework.functions.core.CurrentDateFunction;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class FunctionUtilsTest extends UnitTestSupport {

    @Test
    public void testResolveFunction() {
        assertEquals(FunctionUtils.resolveFunction("citrus:concat('Hello', ' TestFramework!')", context), "Hello TestFramework!");
        assertEquals(FunctionUtils.resolveFunction("citrus:concat('citrus', ':citrus')", context), "citrus:citrus");
        assertEquals(FunctionUtils.resolveFunction("citrus:concat('citrus:citrus')", context), "citrus:citrus");
    }

    @Test
    public void testWithVariables() {
        context.setVariable("greeting", "Hello");
        context.setVariable("text", "TestFramework!");

        assertEquals(FunctionUtils.resolveFunction("citrus:concat('Hello', ' ', ${text})", context), "Hello TestFramework!");
        assertEquals(FunctionUtils.resolveFunction("citrus:concat(${greeting}, ' ', ${text})", context), "Hello TestFramework!");
    }

    @Test
    public void testWithNestedFunctions() {
        assertEquals(FunctionUtils.resolveFunction("citrus:concat(citrus:currentDate('yyyy-mm-dd'))", context), new CurrentDateFunction().execute(Collections.singletonList("yyyy-mm-dd"), context));
        assertEquals(FunctionUtils.resolveFunction("citrus:concat('Now is: ', citrus:currentDate('yyyy-mm-dd'))", context), "Now is: " + new CurrentDateFunction().execute(Collections.singletonList("yyyy-mm-dd"), context));
        assertEquals(FunctionUtils.resolveFunction("citrus:concat(citrus:currentDate('yyyy-mm-dd'), ' ', citrus:concat('Hello', ' TestFramework!'))", context), new CurrentDateFunction().execute(Collections.singletonList("yyyy-mm-dd"), context) + " Hello TestFramework!");
    }

    @Test
    public void testWithNestedFunctionsAndVariables() {
        context.setVariable("greeting", "Hello");
        context.setVariable("dateFormat", "yyyy-mm-dd");

        assertEquals(FunctionUtils.resolveFunction("citrus:concat(citrus:currentDate('${dateFormat}'), ' ', citrus:concat(${greeting}, ' TestFramework!'))", context), new CurrentDateFunction().execute(Collections.singletonList("yyyy-mm-dd"), context) + " Hello TestFramework!");
    }

    @Test
    public void testWithCommaValue() {
        assertEquals(FunctionUtils.resolveFunction("citrus:concat(citrus:upperCase(Yes), ' ', citrus:upperCase(I like Citrus!))", context), "YES I LIKE CITRUS!");
        assertEquals(FunctionUtils.resolveFunction("citrus:upperCase('Monday, Tuesday, wednesday')", context), "MONDAY, TUESDAY, WEDNESDAY");
        assertEquals(FunctionUtils.resolveFunction("citrus:concat('Monday, Tuesday', ' Wednesday')", context), "Monday, Tuesday Wednesday");
        assertEquals(FunctionUtils.resolveFunction("citrus:upperCase('Yes, I like Citrus!)", context), "'YES, I LIKE CITRUS!");
        assertEquals(FunctionUtils.resolveFunction("citrus:upperCase(''Yes, I like Citrus!)", context), "''YES, I LIKE CITRUS!");
        assertEquals(FunctionUtils.resolveFunction("citrus:upperCase(Yes I like Citrus!')", context), "YES I LIKE CITRUS!'");
        assertEquals(FunctionUtils.resolveFunction("citrus:upperCase('Yes, I like Citrus!')", context), "YES, I LIKE CITRUS!");
        assertEquals(FunctionUtils.resolveFunction("citrus:upperCase('Yes, I like Citrus, and this is great!')", context), "YES, I LIKE CITRUS, AND THIS IS GREAT!");
        assertEquals(FunctionUtils.resolveFunction("citrus:upperCase('Yes,I like Citrus!')", context), "YES,I LIKE CITRUS!");
        assertEquals(FunctionUtils.resolveFunction("citrus:concat('Hello Yes, I like Citrus!')", context), "Hello Yes, I like Citrus!");
        assertEquals(FunctionUtils.resolveFunction("citrus:concat('Hello Yes,I like Citrus!')", context), "Hello Yes,I like Citrus!");
        assertEquals(FunctionUtils.resolveFunction("citrus:concat('Hello Yes,I like Citrus, and this is great!')", context), "Hello Yes,I like Citrus, and this is great!");
        assertEquals(FunctionUtils.resolveFunction("citrus:concat('Hello Yes , I like Citrus!')", context), "Hello Yes , I like Citrus!");
        assertEquals(FunctionUtils.resolveFunction("citrus:concat('Hello Yes, I like Citrus!', 'Hello Yes,we like Citrus!')", context), "Hello Yes, I like Citrus!Hello Yes,we like Citrus!");
        assertEquals(FunctionUtils.resolveFunction("citrus:concat('Hello Yes, I like Citrus, and this is great!', 'Hello Yes,we like Citrus, and this is great!')", context), "Hello Yes, I like Citrus, and this is great!Hello Yes,we like Citrus, and this is great!");
    }

    @Test
    public void testEscapeJsonFunction() {
        assertEquals(FunctionUtils.resolveFunction("citrus:escapeJson('{ \"name\": \"bond\" }')", context), "{ \\\"name\\\": \\\"bond\\\" }");
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
