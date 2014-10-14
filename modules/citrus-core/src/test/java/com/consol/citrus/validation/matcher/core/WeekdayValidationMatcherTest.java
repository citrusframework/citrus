/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.validation.matcher.core;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author Christoph Deppisch
 */
public class WeekdayValidationMatcherTest extends AbstractTestNGUnitTest {

    private WeekdayValidationMatcher matcher = new WeekdayValidationMatcher();

    private SimpleDateFormat defaultDateFormat = new SimpleDateFormat("dd.MM.yyyy");

    @Test
    public void testValidationMatcher() {
        matcher.validate("fieldName", defaultDateFormat.format(getNext(Calendar.MONDAY).getTime()), "MONDAY", context);
        matcher.validate("fieldName", defaultDateFormat.format(getNext(Calendar.TUESDAY).getTime()), "TUESDAY", context);
        matcher.validate("fieldName", defaultDateFormat.format(getNext(Calendar.WEDNESDAY).getTime()), "WEDNESDAY", context);
        matcher.validate("fieldName", defaultDateFormat.format(getNext(Calendar.THURSDAY).getTime()), "THURSDAY", context);
        matcher.validate("fieldName", defaultDateFormat.format(getNext(Calendar.FRIDAY).getTime()), "FRIDAY", context);
        matcher.validate("fieldName", defaultDateFormat.format(getNext(Calendar.SATURDAY).getTime()), "SATURDAY", context);
        matcher.validate("fieldName", defaultDateFormat.format(getNext(Calendar.SUNDAY).getTime()), "SUNDAY", context);

        try {
            matcher.validate("fieldName", defaultDateFormat.format(getNext(Calendar.MONDAY).getTime()), "SUNDAY", context);
            Assert.fail("Missing validation matcher failed exception");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().endsWith("expected date to be a 'SUNDAY'"));
        }
    }

    @Test
    public void testCustomDateFormat() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        matcher.validate("fieldName", dateFormat.format(getNext(Calendar.MONDAY).getTime()), "MONDAY('yyyy-MM-dd')", context);
        matcher.validate("fieldName", dateFormat.format(getNext(Calendar.TUESDAY).getTime()), "TUESDAY('yyyy-MM-dd')", context);
        matcher.validate("fieldName", dateFormat.format(getNext(Calendar.WEDNESDAY).getTime()), "WEDNESDAY('yyyy-MM-dd')", context);
        matcher.validate("fieldName", dateFormat.format(getNext(Calendar.THURSDAY).getTime()), "THURSDAY('yyyy-MM-dd')", context);
        matcher.validate("fieldName", dateFormat.format(getNext(Calendar.FRIDAY).getTime()), "FRIDAY('yyyy-MM-dd')", context);
        matcher.validate("fieldName", dateFormat.format(getNext(Calendar.SATURDAY).getTime()), "SATURDAY('yyyy-MM-dd')", context);
        matcher.validate("fieldName", dateFormat.format(getNext(Calendar.SUNDAY).getTime()), "SUNDAY('yyyy-MM-dd')", context);

        try {
            matcher.validate("fieldName", dateFormat.format(getNext(Calendar.MONDAY).getTime()), "SUNDAY('yyyy-MM-dd')", context);
            Assert.fail("Missing validation matcher failed exception");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().endsWith("expected date to be a 'SUNDAY'"));
        }
    }

    @Test(expectedExceptions = {CitrusRuntimeException.class})
    public void testInvalidDefaultDateFormat() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        matcher.validate("fieldName", dateFormat.format(getNext(Calendar.MONDAY).getTime()), "MONDAY", context);
    }

    @Test(expectedExceptions = {CitrusRuntimeException.class})
    public void testInvalidCustomDateFormat() {
        matcher.validate("fieldName", defaultDateFormat.format(getNext(Calendar.MONDAY).getTime()), "MONDAY('dd-MM-yyyy')", context);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void testInvalidDateFormatSyntax() {
        matcher.validate("fieldName", defaultDateFormat.format(getNext(Calendar.MONDAY).getTime()), "MONDAY('ABC')", context);
    }

    /**
     * Get next desired day of week.
     * @param dayField
     * @return
     */
    private Calendar getNext(int dayField) {
        Calendar calendar = Calendar.getInstance();

        while (calendar.get(Calendar.DAY_OF_WEEK) != dayField) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        return calendar;
    }
}
