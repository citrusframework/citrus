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

package org.citrusframework.validation.matcher.core;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import static java.util.Collections.singletonList;

public class WeekdayValidationMatcherTest extends UnitTestSupport {

    private WeekdayValidationMatcher matcher = new WeekdayValidationMatcher();

    private SimpleDateFormat defaultDateFormat = new SimpleDateFormat("dd.MM.yyyy");

    @Test
    public void testValidationMatcher() {
        matcher.validate("fieldName", defaultDateFormat.format(getNext(Calendar.MONDAY).getTime()), singletonList("MONDAY"), context);
        matcher.validate("fieldName", defaultDateFormat.format(getNext(Calendar.TUESDAY).getTime()), singletonList("TUESDAY"), context);
        matcher.validate("fieldName", defaultDateFormat.format(getNext(Calendar.WEDNESDAY).getTime()), singletonList("WEDNESDAY"), context);
        matcher.validate("fieldName", defaultDateFormat.format(getNext(Calendar.THURSDAY).getTime()), singletonList("THURSDAY"), context);
        matcher.validate("fieldName", defaultDateFormat.format(getNext(Calendar.FRIDAY).getTime()), singletonList("FRIDAY"), context);
        matcher.validate("fieldName", defaultDateFormat.format(getNext(Calendar.SATURDAY).getTime()), singletonList("SATURDAY"), context);
        matcher.validate("fieldName", defaultDateFormat.format(getNext(Calendar.SUNDAY).getTime()), singletonList("SUNDAY"), context);

        try {
            matcher.validate("fieldName", defaultDateFormat.format(getNext(Calendar.MONDAY).getTime()), singletonList("SUNDAY"), context);
            Assert.fail("Missing validation matcher failed exception");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().endsWith("expected date to be a 'SUNDAY'"));
        }
    }

    @Test
    public void testCustomDateFormat() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        matcher.validate("fieldName", dateFormat.format(getNext(Calendar.MONDAY).getTime()), List.of("MONDAY", "yyyy-MM-dd"), context);
        matcher.validate("fieldName", dateFormat.format(getNext(Calendar.TUESDAY).getTime()), List.of("TUESDAY", "yyyy-MM-dd"), context);
        matcher.validate("fieldName", dateFormat.format(getNext(Calendar.WEDNESDAY).getTime()), List.of("WEDNESDAY", "yyyy-MM-dd"), context);
        matcher.validate("fieldName", dateFormat.format(getNext(Calendar.THURSDAY).getTime()), List.of("THURSDAY", "yyyy-MM-dd"), context);
        matcher.validate("fieldName", dateFormat.format(getNext(Calendar.FRIDAY).getTime()), List.of("FRIDAY", "yyyy-MM-dd"), context);
        matcher.validate("fieldName", dateFormat.format(getNext(Calendar.SATURDAY).getTime()), List.of("SATURDAY", "yyyy-MM-dd"), context);
        matcher.validate("fieldName", dateFormat.format(getNext(Calendar.SUNDAY).getTime()), List.of("SUNDAY", "yyyy-MM-dd"), context);

        try {
            matcher.validate("fieldName", dateFormat.format(getNext(Calendar.MONDAY).getTime()), List.of("SUNDAY", "yyyy-MM-dd"), context);
            Assert.fail("Missing validation matcher failed exception");
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getMessage().endsWith("expected date to be a 'SUNDAY'"));
        }
    }

    @Test(expectedExceptions = {CitrusRuntimeException.class})
    public void testInvalidDefaultDateFormat() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        matcher.validate("fieldName", dateFormat.format(getNext(Calendar.MONDAY).getTime()), singletonList("MONDAY"), context);
    }

    @Test(expectedExceptions = {CitrusRuntimeException.class})
    public void testInvalidCustomDateFormat() {
        matcher.validate("fieldName", defaultDateFormat.format(getNext(Calendar.MONDAY).getTime()), List.of("MONDAY", "dd-MM-yyyy"), context);
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void testInvalidDateFormatSyntax() {
        matcher.validate("fieldName", defaultDateFormat.format(getNext(Calendar.MONDAY).getTime()), List.of("MONDAY", "ABC"), context);
    }

    @DataProvider
    public Object[][] validParameters() {
        return new Object[][]{
                {"MONDAY", singletonList("MONDAY")},
                {"MONDAY('yyyy-MM-dd')", List.of("MONDAY", "yyyy-MM-dd")},
        };
    }
    @Test(dataProvider = "validParameters")
    public void shouldExtractParametersSuccessfully(String controlExpression, List<String> expectedParameters) {
        List<String> controlValues = matcher.extractControlValues(controlExpression, null);
        Assert.assertEquals(controlValues.size(), expectedParameters.size());

        for (int i = 0; i < controlValues.size(); i++) {
            Assert.assertEquals(controlValues.get(i), expectedParameters.get(i));
        }
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
