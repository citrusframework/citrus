/*
 * Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.validation.matcher.core;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.fail;

/**
 * @author Martin Maher
 * @since 2.5
 */
public class DateRangeValidationMatcherTest {

    DateRangeValidationMatcher testling = new DateRangeValidationMatcher();

    @Test(dataProvider = "validateData")
    public void validateDateRanges(String value, List<String> params, String expectedErrorMessage) {
        try {
            testling.validate("xyz", value, params, null);
            if (expectedErrorMessage != null) {
                fail("Was expecting exception with error message " + expectedErrorMessage);
            }
        } catch (Exception e) {
            if (expectedErrorMessage == null) {
                fail("Was not expecting exception but got one", e);
            }
            Assert.assertTrue(e.getMessage().indexOf(expectedErrorMessage) > -1, String.format("Expected error '%s' not found in '%s'", expectedErrorMessage, e.getMessage()));
        }
    }

    @DataProvider
    public Object[][] validateData() {
        String noErrorExpected = null;

        return new Object[][]{
                // {date-to-validate, control-data, expected-error-message}
                {"01-12-2015", Arrays.asList("01-12-2015", "31-12-2015","dd-MM-yyyy"), noErrorExpected},
                {"31-12-2015", Arrays.asList("01-12-2015", "31-12-2015", "dd-MM-yyyy"), noErrorExpected},
                {"01-12-2015", Arrays.asList("01-12-2015", "01-12-2015", "dd-MM-yyyy"), noErrorExpected},
                {"2015.12.01 01:00:00", Arrays.asList("2015.12.01 01:00:00", "2015.12.01 01:00:01", "yyyy.MM.dd HH:mm:ss"), noErrorExpected},
                {"2015.12.01 01:00:01", Arrays.asList("2015.12.01 01:00:00", "2015.12.01 01:00:01", "yyyy.MM.dd HH:mm:ss"), noErrorExpected},
                {"2015-01-01", Arrays.asList("2015-01-01", "2015-01-01"), noErrorExpected},
                {"2015-01-01", Arrays.asList("2015-01-02", "2015-01-03"), "not in range"},
                {"aa-bb-cccc", Arrays.asList("2015-01-02", "2015-01-03"), "Error parsing date"},
        };
    }

    @DataProvider
    public Object[][] invalidControlData() {
        return new Object[][]{
                {"'2015-12-01'"},
                {"2015-12-01,2015-12-31,"},
                {""}
        };
    }

    @Test(dataProvider = "validDates")
    public void shouldSucceedParsingDate(String date, String dateFormat) {
        testling.toCalender(date, dateFormat);
    }

    @DataProvider
    public Object[][] validDates() {
        return new Object[][]{
                {"2015-12-01", "yyyy-MM-dd"},
                {"2001.07.04 12:08:56", "yyyy.MM.dd HH:mm:ss"}
        };
    }
}
