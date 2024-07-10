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

import static org.citrusframework.util.StringUtils.quote;
import static org.citrusframework.util.StringUtils.trimTrailingComma;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class StringUtilsTest {

    @Test
    public void appendSegmentToPath() {
        assertEquals(StringUtils.appendSegmentToUrlPath("s1","s2"), "s1/s2");
        assertEquals(StringUtils.appendSegmentToUrlPath("s1/","s2"), "s1/s2");
        assertEquals(StringUtils.appendSegmentToUrlPath("s1/","/s2"), "s1/s2");
        assertEquals(StringUtils.appendSegmentToUrlPath("/s1","/s2"), "/s1/s2");
        assertEquals(StringUtils.appendSegmentToUrlPath("/s1/","/s2"), "/s1/s2");
        assertEquals(StringUtils.appendSegmentToUrlPath("/s1/","/s2/"), "/s1/s2/");
        assertEquals(StringUtils.appendSegmentToUrlPath("/s1/",null), "/s1/");
        assertEquals(StringUtils.appendSegmentToUrlPath(null,"/s2/"), "/s2/");
        Assert.assertNull(StringUtils.appendSegmentToUrlPath(null,null));
    }

    @Test
    public void testQuoteTrue() {
        String input = "Hello, World!";
        String expected = "\"Hello, World!\"";
        String result = quote(input, true);

        assertEquals(result, expected, "The text should be quoted.");
    }

    @Test
    public void testQuoteFalse() {
        String input = "Hello, World!";
        String expected = "Hello, World!";
        String result = quote(input, false);

        assertEquals(result, expected, "The text should not be quoted.");
    }

    @Test
    public void testQuoteEmptyStringTrue() {
        String input = "";
        String expected = "\"\"";
        String result = quote(input, true);

        assertEquals(result, expected, "The empty text should be quoted.");
    }

    @Test
    public void testQuoteEmptyStringFalse() {
        String input = "";
        String expected = "";
        String result = quote(input, false);

        assertEquals(result, expected, "The empty text should not be quoted.");
    }

    @Test
    public void testQuoteNullStringTrue() {
        String input = null;
        String expected = "\"null\"";
        String result = quote(input, true);

        assertEquals(result, expected, "The null text should be treated as a string 'null'.");
    }

    @Test
    public void testQuoteNullStringFalse() {
        String input = null;
        String result = quote(input, false);

        assertNull(result);
    }

    @DataProvider(name = "trimTrailingCommaDataProvider")
    public Object[][] trimTrailingCommaDataProvider() {
        return new Object[][] {
            { new StringBuilder("Example text,    "), "Example text" },
            { new StringBuilder("No trailing comma    "), "No trailing comma" },
            { new StringBuilder("No trailing comma,\n\t\n    "), "No trailing comma" },
            { new StringBuilder("Trailing comma,"), "Trailing comma" },
            { new StringBuilder("Multiple commas and spaces,,,   "), "Multiple commas and spaces,," },
            { new StringBuilder("No trim needed"), "No trim needed" },
            { new StringBuilder(), "" }
        };
    }

    @Test(dataProvider = "trimTrailingCommaDataProvider")
    public void testTrimTrailingComma(StringBuilder input, String expected) {
        trimTrailingComma(input);
        Assert.assertEquals(input.toString(), expected);
    }

    @Test
    public void testTrimTrailingCommaOnlySpaces() {
        StringBuilder builder = new StringBuilder("     ");
        trimTrailingComma(builder);
        Assert.assertEquals(builder.toString(), "");

        builder = new StringBuilder(",");
        trimTrailingComma(builder);
        Assert.assertEquals(builder.toString(), "");

        builder = new StringBuilder(",   ,   ");
        trimTrailingComma(builder);
        Assert.assertEquals(builder.toString(), ",   ");
    }

    @Test
    public void testTrimTrailingCommaWithNull() {
        StringBuilder builder = new StringBuilder();
        trimTrailingComma(builder);
        Assert.assertEquals(builder.toString(), "");
    }

}
