/*
 * Copyright 2006-2024 the original author or authors.
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

import org.citrusframework.UnitTestSupport;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.exceptions.NoSuchFunctionException;
import org.citrusframework.exceptions.NoSuchFunctionLibraryException;
import org.citrusframework.functions.core.CurrentDateFunction;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.citrusframework.functions.FunctionUtils.resolveFunction;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * @author Christoph Deppisch
 */
public class FunctionUtilsTest extends UnitTestSupport {

    @Test
    public void testResolveFunction() {
        assertEquals(resolveFunction("citrus:concat('Hello',' TestFramework!')", context), "Hello TestFramework!");
        assertEquals(resolveFunction("citrus:concat('citrus', ':citrus')", context), "citrus:citrus");
        assertEquals(resolveFunction("citrus:concat('citrus:citrus')", context), "citrus:citrus");
    }

    @Test
    public void testWithVariables() {
        context.setVariable("greeting", "Hello");
        context.setVariable("text", "TestFramework!");

        assertEquals(resolveFunction("citrus:concat('Hello', ' ', ${text})", context), "Hello TestFramework!");
        assertEquals(resolveFunction("citrus:concat(${greeting}, ' ', ${text})", context), "Hello TestFramework!");
    }

    @Test
    public void testWithNestedFunctions() {
        assertEquals(resolveFunction("citrus:concat(citrus:currentDate('yyyy-mm-dd'))", context), new CurrentDateFunction().execute(Collections.singletonList("yyyy-mm-dd"), context));
        assertEquals(resolveFunction("citrus:concat('Now is: ', citrus:currentDate('yyyy-mm-dd'))", context), "Now is: " + new CurrentDateFunction().execute(Collections.singletonList("yyyy-mm-dd"), context));
        assertEquals(resolveFunction("citrus:concat(citrus:currentDate('yyyy-mm-dd'), ' ', citrus:concat('Hello', ' TestFramework!'))", context), new CurrentDateFunction().execute(Collections.singletonList("yyyy-mm-dd"), context) + " Hello TestFramework!");
    }

    @Test
    public void testWithNestedFunctionsAndVariables() {
        context.setVariable("greeting", "Hello");
        context.setVariable("dateFormat", "yyyy-mm-dd");

        assertEquals(resolveFunction("citrus:concat(citrus:currentDate('${dateFormat}'), ' ', citrus:concat(${greeting}, ' TestFramework!'))", context), new CurrentDateFunction().execute(Collections.singletonList("yyyy-mm-dd"), context) + " Hello TestFramework!");
    }

    @Test
    public void testWithCommaValue() {
        assertEquals(resolveFunction("citrus:concat(citrus:upperCase(Yes), ' ', citrus:upperCase(I like Citrus!))", context), "YES I LIKE CITRUS!");
        assertEquals(resolveFunction("citrus:upperCase('Monday, Tuesday, wednesday')", context), "MONDAY, TUESDAY, WEDNESDAY");
        assertEquals(resolveFunction("citrus:concat('Monday, Tuesday', ' Wednesday')", context), "Monday, Tuesday Wednesday");
        assertEquals(resolveFunction("citrus:upperCase('Yes, I like Citrus!')", context), "YES, I LIKE CITRUS!");
        assertEquals(resolveFunction("citrus:upperCase('Yes, I like Citrus, and this is great!')", context), "YES, I LIKE CITRUS, AND THIS IS GREAT!");
        assertEquals(resolveFunction("citrus:upperCase('Yes,I like Citrus!')", context), "YES,I LIKE CITRUS!");
        assertEquals(resolveFunction("citrus:upperCase('Yes', 'I like Citrus!')", context), "YES");
        assertEquals(resolveFunction("citrus:concat('Hello Yes, I like Citrus!')", context), "Hello Yes, I like Citrus!");
        assertEquals(resolveFunction("citrus:concat('Hello Yes,I like Citrus!')", context), "Hello Yes,I like Citrus!");
        assertEquals(resolveFunction("citrus:concat('Hello Yes,I like Citrus, and this is great!')", context), "Hello Yes,I like Citrus, and this is great!");
        assertEquals(resolveFunction("citrus:concat('Hello Yes , I like Citrus!')", context), "Hello Yes , I like Citrus!");
        assertEquals(resolveFunction("citrus:concat('Hello Yes, I like Citrus!', 'Hello Yes,we like Citrus!')", context), "Hello Yes, I like Citrus!Hello Yes,we like Citrus!");
        assertEquals(resolveFunction("citrus:concat('Hello Yes, I like Citrus, and this is great!', 'Hello Yes,we like Citrus, and this is great!')", context), "Hello Yes, I like Citrus, and this is great!Hello Yes,we like Citrus, and this is great!");

//        assertEquals(resolveFunction("citrus:upperCase(''Yes, I like Citrus!)", context), "''YES, I LIKE CITRUS!");
//        assertEquals(resolveFunction("citrus:upperCase('Yes, I like Citrus!)", context), "'YES, I LIKE CITRUS!");
//        assertEquals(resolveFunction("citrus:upperCase(Yes I like Citrus!')", context), "YES I LIKE CITRUS!'");
    }

    @Test(expectedExceptions = {InvalidFunctionUsageException.class})
    public void testInvalidFunction() {
        resolveFunction("citrus:citrus", context);
    }

    @Test(expectedExceptions = {NoSuchFunctionException.class})
    public void testUnknownFunction() {
        resolveFunction("citrus:functiondoesnotexist()", context);
    }

    @Test(expectedExceptions = {NoSuchFunctionLibraryException.class})
    public void testUnknownFunctionLibrary() {
        resolveFunction("doesnotexist:concat('Hello', ' TestFramework!')", context);
    }

    @Test
    void shouldReplaceIfStringIsJson() {
        var contextSpy = spy(context);
        when(contextSpy.getFunctionRegistry()).thenReturn(spy(context.getFunctionRegistry()));
        List<FunctionLibrary> functionLibraries = List.of(new DefaultFunctionLibrary());
        when(contextSpy.getFunctionRegistry().getFunctionLibraries()).thenReturn(functionLibraries);
        var input = """
        {
            "myValues": [
                "O15o3a8",
                "PhDjdSruZgG"
            ]
        }
        """;

        var result = FunctionUtils.replaceFunctionsInString(input, context, false);

        assertThat(result).isEqualTo(input);
    }
}
