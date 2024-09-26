package org.citrusframework.util;

import static org.citrusframework.util.StringUtils.hasText;
import static org.citrusframework.util.StringUtils.isEmpty;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

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
}
