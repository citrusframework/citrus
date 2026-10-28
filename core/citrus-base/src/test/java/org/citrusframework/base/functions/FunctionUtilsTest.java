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

package org.citrusframework.base.functions;

import java.util.Collections;

import org.citrusframework.base.UnitTestSupport;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.exceptions.NoSuchFunctionException;
import org.citrusframework.exceptions.NoSuchFunctionLibraryException;
import org.citrusframework.base.functions.core.CurrentDateFunction;
import org.citrusframework.functions.FunctionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FunctionUtilsTest extends UnitTestSupport {

    @Test
    public void testResolveFunction() {
        Assertions.assertEquals("Hello TestFramework!", FunctionUtils.resolveFunction("citrus:concat('Hello', ' TestFramework!')", context));
        assertEquals("citrus:citrus", FunctionUtils.resolveFunction("citrus:concat('citrus', ':citrus')", context));
        assertEquals("citrus:citrus", FunctionUtils.resolveFunction("citrus:concat('citrus:citrus')", context));
    }

    @Test
    public void testWithVariables() {
        context.setVariable("greeting", "Hello");
        context.setVariable("text", "TestFramework!");

        assertEquals("Hello TestFramework!", FunctionUtils.resolveFunction("citrus:concat('Hello', ' ', ${text})", context));
        assertEquals("Hello TestFramework!", FunctionUtils.resolveFunction("citrus:concat(${greeting}, ' ', ${text})", context));
    }

    @Test
    public void testWithNestedFunctions() {
        assertEquals(new CurrentDateFunction().execute(Collections.singletonList("yyyy-mm-dd"), context), FunctionUtils.resolveFunction("citrus:concat(citrus:currentDate('yyyy-mm-dd'))", context));
        assertEquals("Now is: " + new CurrentDateFunction().execute(Collections.singletonList("yyyy-mm-dd"), context), FunctionUtils.resolveFunction("citrus:concat('Now is: ', citrus:currentDate('yyyy-mm-dd'))", context));
        assertEquals(new CurrentDateFunction().execute(Collections.singletonList("yyyy-mm-dd"), context) + " Hello TestFramework!", FunctionUtils.resolveFunction("citrus:concat(citrus:currentDate('yyyy-mm-dd'), ' ', citrus:concat('Hello', ' TestFramework!'))", context));
    }

    @Test
    public void testWithNestedFunctionsAndVariables() {
        context.setVariable("greeting", "Hello");
        context.setVariable("dateFormat", "yyyy-mm-dd");

        assertEquals(new CurrentDateFunction().execute(Collections.singletonList("yyyy-mm-dd"), context) + " Hello TestFramework!", FunctionUtils.resolveFunction("citrus:concat(citrus:currentDate('${dateFormat}'), ' ', citrus:concat(${greeting}, ' TestFramework!'))", context));
    }

    @Test
    public void testWithCommaValue() {
        assertEquals("YES I LIKE CITRUS!", FunctionUtils.resolveFunction("citrus:concat(citrus:upperCase(Yes), ' ', citrus:upperCase(I like Citrus!))", context));
        assertEquals("MONDAY, TUESDAY, WEDNESDAY", FunctionUtils.resolveFunction("citrus:upperCase('Monday, Tuesday, wednesday')", context));
        assertEquals("Monday, Tuesday Wednesday", FunctionUtils.resolveFunction("citrus:concat('Monday, Tuesday', ' Wednesday')", context));
        assertEquals("'YES, I LIKE CITRUS!", FunctionUtils.resolveFunction("citrus:upperCase('Yes, I like Citrus!)", context));
        assertEquals("''YES, I LIKE CITRUS!", FunctionUtils.resolveFunction("citrus:upperCase(''Yes, I like Citrus!)", context));
        assertEquals("YES I LIKE CITRUS!'", FunctionUtils.resolveFunction("citrus:upperCase(Yes I like Citrus!')", context));
        assertEquals("YES, I LIKE CITRUS!", FunctionUtils.resolveFunction("citrus:upperCase('Yes, I like Citrus!')", context));
        assertEquals("YES, I LIKE CITRUS, AND THIS IS GREAT!", FunctionUtils.resolveFunction("citrus:upperCase('Yes, I like Citrus, and this is great!')", context));
        assertEquals("YES,I LIKE CITRUS!", FunctionUtils.resolveFunction("citrus:upperCase('Yes,I like Citrus!')", context));
        assertEquals("Hello Yes, I like Citrus!", FunctionUtils.resolveFunction("citrus:concat('Hello Yes, I like Citrus!')", context));
        assertEquals("Hello Yes,I like Citrus!", FunctionUtils.resolveFunction("citrus:concat('Hello Yes,I like Citrus!')", context));
        assertEquals("Hello Yes,I like Citrus, and this is great!", FunctionUtils.resolveFunction("citrus:concat('Hello Yes,I like Citrus, and this is great!')", context));
        assertEquals("Hello Yes , I like Citrus!", FunctionUtils.resolveFunction("citrus:concat('Hello Yes , I like Citrus!')", context));
        assertEquals("Hello Yes, I like Citrus!Hello Yes,we like Citrus!", FunctionUtils.resolveFunction("citrus:concat('Hello Yes, I like Citrus!', 'Hello Yes,we like Citrus!')", context));
        assertEquals("Hello Yes, I like Citrus, and this is great!Hello Yes,we like Citrus, and this is great!", FunctionUtils.resolveFunction("citrus:concat('Hello Yes, I like Citrus, and this is great!', 'Hello Yes,we like Citrus, and this is great!')", context));
    }

    @Test
    public void testEscapeJsonFunction() {
        assertEquals("{ \\\"name\\\": \\\"bond\\\" }", FunctionUtils.resolveFunction("citrus:escapeJson('{ \"name\": \"bond\" }')", context));
    }

    @Test
    public void testInvalidFunction() {
        assertThrows(InvalidFunctionUsageException.class, () -> {
            FunctionUtils.resolveFunction("citrus:citrus", context);
        });
    }

    @Test
    public void testUnknownFunction() {
        assertThrows(NoSuchFunctionException.class, () -> {
            FunctionUtils.resolveFunction("citrus:functiondoesnotexist()", context);
        });
    }

    @Test
    public void testUnknownFunctionLibrary() {
        assertThrows(NoSuchFunctionLibraryException.class, () -> {
            FunctionUtils.resolveFunction("doesnotexist:concat('Hello', ' TestFramework!')", context);
        });
    }
}
