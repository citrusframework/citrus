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

package org.citrusframework.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.citrusframework.util.StringUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StringUtilsTest {

    public static Object[][] text() {
        return new Object[][]{{"foo"}};
    }

    public static Object[][] blankText() {
        return new Object[][]{{" "}};
    }

    public static Object[][] emptyText() {
        return new Object[][]{{null}, {""}};
    }

    @ParameterizedTest
    @MethodSource("text")
    public void hasText_returnsTrue(String str) {
        assertTrue(hasText(str));
    }

    @ParameterizedTest
    @MethodSource("blankText")
    public void hasText_returnsFalse_forBlankText(String str) {
        assertFalse(hasText(str));
    }

    @ParameterizedTest
    @MethodSource("emptyText")
    public void hasText_returnsFalse_forEmptyText(String str) {
        assertFalse(hasText(str));
    }

    @ParameterizedTest
    @MethodSource("emptyText")
    public void isEmpty_returnsTrue(String str) {
        assertTrue(isEmpty(str));
    }

    @ParameterizedTest
    @MethodSource("text")
    public void isEmpty_returnsFalse_forText(String str) {
        assertFalse(isEmpty(str));
    }

    @ParameterizedTest
    @MethodSource("blankText")
    public void isEmpty_returnsFalse_forBlankText(String str) {
        assertFalse(isEmpty(str));
    }

    @Test
    public void appendSegmentToPath() {
        assertEquals("s1/s2", appendSegmentToUrlPath("s1", "s2"));
        assertEquals("s1", appendSegmentToUrlPath("s1", ""));
        assertEquals("s1/s2", appendSegmentToUrlPath("s1/", "s2"));
        assertEquals("s1/", appendSegmentToUrlPath("s1/", ""));
        assertEquals("s1/s2", appendSegmentToUrlPath("s1/", "/s2"));
        assertEquals("/s1/s2", appendSegmentToUrlPath("/s1", "/s2"));
        assertEquals("/s1/s2", appendSegmentToUrlPath("/s1/", "/s2"));
        assertEquals("/s1/s2/", appendSegmentToUrlPath("/s1/", "/s2/"));
        assertEquals("/s1/", appendSegmentToUrlPath("/s1/", null));
        assertEquals("/s2/", appendSegmentToUrlPath(null, "/s2/"));
        assertNull(appendSegmentToUrlPath(null, null));
    }

    @Test
    public void testQuoteTrue() {
        String input = "Hello, World!";
        String expected = "\"Hello, World!\"";
        String result = quote(input, true);

        assertEquals(expected, result, "The text should be quoted.");
    }

    @Test
    public void testQuoteFalse() {
        String input = "Hello, World!";
        String expected = "Hello, World!";
        String result = quote(input, false);

        assertEquals(expected, result, "The text should not be quoted.");
    }

    @Test
    public void testQuoteEmptyStringTrue() {
        String input = "";
        String expected = "\"\"";
        String result = quote(input, true);

        assertEquals(expected, result, "The empty text should be quoted.");
    }

    @Test
    public void testQuoteEmptyStringFalse() {
        String input = "";
        String expected = "";
        String result = quote(input, false);

        assertEquals(expected, result, "The empty text should not be quoted.");
    }

    @Test
    public void testQuoteNullStringTrue() {
        String input = null;
        String expected = "\"null\"";
        String result = quote(input, true);

        assertEquals(expected, result, "The null text should be treated as a string 'null'.");
    }

    @Test
    public void testQuoteNullStringFalse() {
        assertNull(quote(null, false));
    }

    public Object[][] trimTrailingCommaDataProvider() {
        return new Object[][]{
                {new StringBuilder("Example text,    "), "Example text"},
                {new StringBuilder("No trailing comma    "), "No trailing comma"},
                {new StringBuilder("No trailing comma,\n\t\n    "), "No trailing comma"},
                {new StringBuilder("Trailing comma,"), "Trailing comma"},
                {new StringBuilder("Multiple commas and spaces,,,   "), "Multiple commas and spaces,,"},
                {new StringBuilder("No trim needed"), "No trim needed"},
                {new StringBuilder(), ""}
        };
    }

    @ParameterizedTest
    @MethodSource("trimTrailingCommaDataProvider")
    public void testTrimTrailingComma(StringBuilder input, String expected) {
        trimTrailingComma(input);
        assertEquals(expected, input.toString());
    }

    @Test
    public void testTrimTrailingCommaOnlySpaces() {
        StringBuilder builder = new StringBuilder("     ");
        trimTrailingComma(builder);
        assertEquals("", builder.toString());

        builder = new StringBuilder(",");
        trimTrailingComma(builder);
        assertEquals("", builder.toString());

        builder = new StringBuilder(",   ,   ");
        trimTrailingComma(builder);
        assertEquals(",   ", builder.toString());
    }

    @Test
    public void testTrimTrailingCommaWithNull() {
        StringBuilder builder = new StringBuilder();
        trimTrailingComma(builder);
        assertEquals("", builder.toString());
    }

    public Object[][] convertFirstCharToUpperCaseData() {
        return new Object[][]{
                {"hello", "Hello"},
                {"h", "H"},
                {"Hello", "Hello"},
                {null, ""},
                {"", ""},
                {"hello world", "Hello world"},
                {" hello", " hello"},
                {"1test", "1test"},
                {"!special", "!special"}
        };
    }

    @ParameterizedTest
    @MethodSource("convertFirstCharToUpperCaseData")
    public void testConvertFirstCharToUpperCase(String input, String expected) {
        String actual = StringUtils.convertFirstCharToUpperCase(input);
        assertEquals(expected, actual,
                "The convertFirstCharToUpperCaseData method did not return the expected result.");
    }

    public Object[][] convertFirstCharToLowerCaseData() {
        return new Object[][]{
                {"hello", "hello"},
                {"H", "h"},
                {"hello", "hello"},
                {null, ""},
                {"", ""},
                {"Hello world", "hello world"},
                {" Hello", " Hello"},
                {"1Test", "1Test"},
                {"!Special", "!Special"}
        };
    }

    @ParameterizedTest
    @MethodSource("convertFirstCharToLowerCaseData")
    public void testConvertFirstCharToLowerCase(String input, String expected) {
        String actual = StringUtils.convertFirstCharToLowerCase(input);
        assertEquals(expected, actual,
                "The convertFirstCharToLowerCaseData method did not return the expected result.");
    }

    public Object[][] normalizeWhitespaceProvider() {
        return new Object[][] {
            // Test data: payload, ignoreWhitespace, ignoreNewLineType, expected result
            {"Hello    \t\r\nWorld\r\n", true, true, "Hello World"},
            {"Hello    \t\r\nWorld\r\n", true, false, "Hello World"},
            {"Hello    \t\r\nWorld\r\n", false, true, "Hello    \t\nWorld\n"},
            {"Hello    \t\r\nWorld\r\n", false, false, "Hello    \t\r\nWorld\r\n"},
            {"", true, true, ""},
            {"", false, false, ""},
            {null, true, true, null},
            {null, false, false, null}
        };
    }

    @ParameterizedTest
    @MethodSource("normalizeWhitespaceProvider")
    public void normalizeWhitespace(String text, boolean normalizeWhitespace, boolean normalizeLineEndingsToUnix, String expected) {
        assertEquals(expected, StringUtils.normalizeWhitespace(text, normalizeWhitespace, normalizeLineEndingsToUnix));
    }

    @Test
    public void testConvertToStringEmpty() {
        assertEquals("{}", convertToString(Collections.emptyMap()));
    }

    @Test
    public void testConvertToStringWithStringValues() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", "citrus");
        map.put("type", "framework");
        assertEquals("{\"name\": \"citrus\", \"type\": \"framework\"}", convertToString(map));
    }

    @Test
    public void testConvertToStringWithNullValue() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("body", null);
        assertEquals("{\"body\": null}", convertToString(map));
    }

    @Test
    public void testConvertToStringWithNumericValue() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("count", 42);
        map.put("rate", 3.14);
        assertEquals("{\"count\": 42, \"rate\": 3.14}", convertToString(map));
    }

    @Test
    public void testConvertToStringWithMixedValues() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", "test");
        map.put("count", 5);
        map.put("data", null);
        assertEquals("{\"name\": \"test\", \"count\": 5, \"data\": null}", convertToString(map));
    }

    @Test
    public void testExtractMapFromNull() {
        assertTrue(extractMap(null).isEmpty());
    }

    @Test
    public void testExtractMapFromEmpty() {
        assertTrue(extractMap("").isEmpty());
    }

    @Test
    public void testExtractMapFromJson() {
        Map<String, Object> result = extractMap("{\"name\": \"citrus\", \"type\": \"framework\"}");
        assertEquals(2, result.size());
        assertEquals("citrus", result.get("name"));
        assertEquals("framework", result.get("type"));
    }

    @Test
    public void testExtractMapFromJsonWithNullValue() {
        Map<String, Object> result = extractMap("{\"body\": null}");
        assertEquals(1, result.size());
        assertEquals("null", result.get("body"));
    }

    @Test
    public void testExtractMapFromJsonWithNumericValue() {
        Map<String, Object> result = extractMap("{\"count\": 42}");
        assertEquals(1, result.size());
        assertEquals("42", result.get("count"));
    }

    @Test
    public void testExtractMapFromKeyValuePairs() {
        Map<String, Object> result = extractMap("name=citrus,type=framework");
        assertEquals(2, result.size());
        assertEquals("citrus", result.get("name"));
        assertEquals("framework", result.get("type"));
    }

    @Test
    public void testExtractMapFromKeyValuePairsWithSpaces() {
        Map<String, Object> result = extractMap(" name = citrus , type = framework ");
        assertEquals(2, result.size());
        assertEquals("citrus", result.get("name"));
        assertEquals("framework", result.get("type"));
    }

    @Test
    public void testExtractMapFromKeyValuePairsWithQuotes() {
        Map<String, Object> result = extractMap("\"name\"=\"citrus\",\"type\"=\"framework\"");
        assertEquals(2, result.size());
        assertEquals("citrus", result.get("name"));
        assertEquals("framework", result.get("type"));
    }

    @Test
    public void testStripQuotesWithQuotes() {
        assertEquals("hello", stripQuotes("\"hello\""));
    }

    @Test
    public void testStripQuotesWithoutQuotes() {
        assertEquals("hello", stripQuotes("hello"));
    }

    @Test
    public void testStripQuotesWithNull() {
        assertNull(stripQuotes(null));
    }

    @Test
    public void testStripQuotesEmptyQuoted() {
        assertEquals("", stripQuotes("\"\""));
    }

    @Test
    public void testStripQuotesWithSurroundingSpaces() {
        assertEquals("hello", stripQuotes("  \"hello\"  "));
    }

    @Test
    public void testStripQuotesSingleQuoteOnly() {
        assertEquals("\"hello", stripQuotes("\"hello"));
    }
}
