package org.citrusframework.util;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.citrusframework.util.StringUtils.hasText;
import static org.citrusframework.util.StringUtils.isEmpty;
import static org.citrusframework.util.StringUtils.quote;
import static org.citrusframework.util.StringUtils.trimTrailingComma;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class StringUtilsTest {

    @DataProvider
    public static Object[][] text() {
        return new Object[][]{{"foo"}};
    }

    @DataProvider
    public static Object[][] blankText() {
        return new Object[][]{{" "}};
    }

    @DataProvider
    public static Object[][] emptyText() {
        return new Object[][]{{null}, {""}};
    }

    @Test(dataProvider = "text")
    public void hasText_returnsTrue(String str) {
        assertTrue(hasText(str));
    }

    @Test(dataProvider = "blankText")
    public void hasText_returnsFalse_forBlankText(String str) {
        assertFalse(hasText(str));
    }

    @Test(dataProvider = "emptyText")
    public void hasText_returnsFalse_forEmptyText(String str) {
        assertFalse(hasText(str));
    }

    @Test(dataProvider = "emptyText")
    public void isEmpty_returnsTrue(String str) {
        assertTrue(isEmpty(str));
    }

    @Test(dataProvider = "text")
    public void isEmpty_returnsFalse_forText(String str) {
        assertFalse(isEmpty(str));
    }

    @Test(dataProvider = "blankText")
    public void isEmpty_returnsFalse_forBlankText(String str) {
        assertFalse(isEmpty(str));
    }

    @Test
    public void appendSegmentToPath() {
        assertEquals(StringUtils.appendSegmentToUrlPath("s1", "s2"), "s1/s2");
        assertEquals(StringUtils.appendSegmentToUrlPath("s1/", "s2"), "s1/s2");
        assertEquals(StringUtils.appendSegmentToUrlPath("s1/", "/s2"), "s1/s2");
        assertEquals(StringUtils.appendSegmentToUrlPath("/s1", "/s2"), "/s1/s2");
        assertEquals(StringUtils.appendSegmentToUrlPath("/s1/", "/s2"), "/s1/s2");
        assertEquals(StringUtils.appendSegmentToUrlPath("/s1/", "/s2/"), "/s1/s2/");
        assertEquals(StringUtils.appendSegmentToUrlPath("/s1/", null), "/s1/");
        assertEquals(StringUtils.appendSegmentToUrlPath(null, "/s2/"), "/s2/");
        assertNull(StringUtils.appendSegmentToUrlPath(null, null));
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
        assertNull(quote(null, false));
    }

    @DataProvider(name = "trimTrailingCommaDataProvider")
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

    @Test(dataProvider = "trimTrailingCommaDataProvider")
    public void testTrimTrailingComma(StringBuilder input, String expected) {
        trimTrailingComma(input);
        assertEquals(input.toString(), expected);
    }

    @Test
    public void testTrimTrailingCommaOnlySpaces() {
        StringBuilder builder = new StringBuilder("     ");
        trimTrailingComma(builder);
        assertEquals(builder.toString(), "");

        builder = new StringBuilder(",");
        trimTrailingComma(builder);
        assertEquals(builder.toString(), "");

        builder = new StringBuilder(",   ,   ");
        trimTrailingComma(builder);
        assertEquals(builder.toString(), ",   ");
    }

    @Test
    public void testTrimTrailingCommaWithNull() {
        StringBuilder builder = new StringBuilder();
        trimTrailingComma(builder);
        assertEquals(builder.toString(), "");
    }

    @DataProvider
    public Object[][] convertFirstChartToUpperCaseData() {
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

    @Test(dataProvider = "convertFirstChartToUpperCaseData")
    public void testConvertFirstChartToUpperCase(String input, String expected) {
        String actual = StringUtils.convertFirstChartToUpperCase(input);
        assertEquals(actual, expected,
                "The titleCase method did not return the expected result.");
    }
}
