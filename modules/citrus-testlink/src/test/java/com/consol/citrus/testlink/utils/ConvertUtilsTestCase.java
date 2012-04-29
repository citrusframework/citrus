/*
 * File: ConvertUtilsTestCase.java
 *
 * Copyright (c) 2006-2012 the original author or authors.
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
 *
 * last modified: Sunday, April 29, 2012 (15:39) by: Matthias Beil
 */
package com.consol.citrus.testlink.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * JUnit class for testing {@link ConvertUtils} class.
 *
 * @author Matthias Beil
 * @since CITRUS 1.2 M2
 */
public class ConvertUtilsTestCase {

    // ~ Methods ---------------------------------------------------------------------------------

    /**
     * Test for {@link ConvertUtils#throwableToString(Throwable)} method.
     */
    @Test
    public void testThrowableToString() {

        Throwable th = null;
        String strTh = ConvertUtils.throwableToString(th);
        assertEquals("", strTh);

        final String exp = "This is a throwable";
        th = new Throwable(exp);
        assertEquals(exp, th.getMessage());

        strTh = ConvertUtils.throwableToString(th);
        assertTrue(strTh.contains(exp));
    }

    /**
     * Test for {@link ConvertUtils#convertToString(Object)} method.
     */
    @Test
    public void testConvertToString() {

        String tst = ConvertUtils.convertToString(null);
        assertNull(tst);

        String exp = "TestString";
        tst = ConvertUtils.convertToString(exp);
        assertNotNull(tst);
        assertEquals(exp, tst);

        tst = ConvertUtils.convertToString(Integer.valueOf(42));
        assertNotNull(tst);
        assertEquals("42", tst);
    }

    /**
     * Test for {@link ConvertUtils#convertToBoolean(Object)} method.
     */
    @Test
    public void testConvertToBoolean() {

        Object tst = ConvertUtils.convertToBoolean(null);
        assertNull(tst);

        String exp = "";
        tst = ConvertUtils.convertToBoolean(exp);
        assertNull(tst);

        exp = "true";
        tst = ConvertUtils.convertToBoolean(exp);
        assertNotNull(tst);
        assertTrue(((Boolean) tst).booleanValue());

        exp = "invalid";
        tst = ConvertUtils.convertToBoolean(exp);
        assertNotNull(tst);
        assertFalse(((Boolean) tst).booleanValue());
    }

    /**
     * Test for {@link ConvertUtils#convertToInteger(Object)} method.
     */
    @Test
    public void testConvertToInteger() {

        Object tst = ConvertUtils.convertToInteger(null);
        assertNull(tst);

        String exp = "";
        tst = ConvertUtils.convertToInteger(exp);
        assertNull(tst);

        exp = "invalid";
        tst = ConvertUtils.convertToInteger(exp);
        assertNull(tst);

        exp = "42";
        tst = ConvertUtils.convertToInteger(exp);
        assertNotNull(tst);
        assertEquals(Integer.valueOf(42), tst);
    }

}
