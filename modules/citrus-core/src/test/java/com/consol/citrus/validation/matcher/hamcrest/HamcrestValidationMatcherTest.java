/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.validation.matcher.hamcrest;

import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class HamcrestValidationMatcherTest extends AbstractTestNGUnitTest {

    @Test(dataProvider = "testData")
    public void testValidate(String path, String value, List<String> params) throws Exception {
        HamcrestValidationMatcher validationMatcher = new HamcrestValidationMatcher();
        validationMatcher.validate( path, value, params, context);
    }

    @DataProvider
    public Object[][] testData() {
        return new Object[][] {
            new Object[]{ "foo", "value", Arrays.asList("equalTo(value)") },
            new Object[]{"foo", "value", Arrays.asList("not(equalTo(other))")},
            new Object[]{"foo", "value", Arrays.asList("is(not(other))")},
            new Object[]{"foo", "value", Arrays.asList("not(is(other))")},
            new Object[]{"foo", "value", Arrays.asList("equalToIgnoringCase(VALUE)")},
            new Object[]{"foo", "value", Arrays.asList("containsString(lue)")},
            new Object[]{"foo", "value", Arrays.asList("not(containsString(other))")},
            new Object[]{"foo", "value", Arrays.asList("startsWith(val)")},
            new Object[]{"foo", "value", Arrays.asList("endsWith(lue)")},
            new Object[]{"foo", "value", Arrays.asList("anyOf(startsWith(val), endsWith(lue))")},
            new Object[]{"foo", "value", Arrays.asList("allOf(startsWith(val), endsWith(lue))")},
            new Object[]{"foo", "", Arrays.asList("isEmptyString()")},
            new Object[]{"foo", "bar", Arrays.asList("not(isEmptyString())")},
            new Object[]{"foo", null, Arrays.asList("isEmptyOrNullString()")},
            new Object[]{"foo", null, Arrays.asList("nullValue()")},
            new Object[]{"foo", "bar", Arrays.asList("notNullValue()")},
            new Object[]{"foo", "[]", Arrays.asList("empty()")},
            new Object[]{"foo", "", Arrays.asList("empty()")},
            new Object[]{"foo", "bar", Arrays.asList("not(empty())")},
            new Object[]{"foo", "5", Arrays.asList("greaterThan(4)")},
            new Object[]{"foo", "5", Arrays.asList("allOf(greaterThan(4), lessThan(6), not(lessThan(5)))")},
            new Object[]{"foo", "5", Arrays.asList("is(not(greaterThan(5)))")},
            new Object[]{"foo", "5", Arrays.asList("greaterThanOrEqualTo(5)")},
            new Object[]{"foo", "4", Arrays.asList("lessThan(5)")},
            new Object[]{"foo", "4", Arrays.asList("not(lessThan(1))")},
            new Object[]{"foo", "4", Arrays.asList("lessThanOrEqualTo(4)")},
            new Object[]{"foo", "", Arrays.asList("1", "lessThanOrEqualTo(4)")},
            new Object[]{"foo", "", Arrays.asList("4", "lessThanOrEqualTo(4)")},
            new Object[]{"foo", "[value1,value2,value3,value4,value5]", Arrays.asList("hasSize(5)") }
        };
    }

    @Test(dataProvider = "testDataFailed", expectedExceptions = AssertionError.class)
    public void testValidateFailed(String path, String value, List<String> params) throws Exception {
        HamcrestValidationMatcher validationMatcher = new HamcrestValidationMatcher();
        validationMatcher.validate( path, value, params, context);
    }

    @DataProvider
    public Object[][] testDataFailed() {
        return new Object[][] {
            new Object[]{ "foo", "value", Arrays.asList("equalTo(wrong)") },
            new Object[]{"foo", "value", Arrays.asList("not(equalTo(value))")},
            new Object[]{"foo", "value", Arrays.asList("is(not(value))")},
            new Object[]{"foo", "value", Arrays.asList("not(is(value))")},
            new Object[]{"foo", "value", Arrays.asList("equalToIgnoringCase(WRONG)")},
            new Object[]{"foo", "value", Arrays.asList("containsString(wrong)")},
            new Object[]{"foo", "value", Arrays.asList("not(containsString(value))")},
            new Object[]{"foo", "value", Arrays.asList("startsWith(wrong)")},
            new Object[]{"foo", "value", Arrays.asList("endsWith(wrong)")},
            new Object[]{"foo", "value", Arrays.asList("anyOf(startsWith(wrong), endsWith(wrong))")},
            new Object[]{"foo", "value", Arrays.asList("allOf(startsWith(wrong), endsWith(wrong))")},
            new Object[]{"foo", "bar", Arrays.asList("isEmptyString()")},
            new Object[]{"foo", "", Arrays.asList("not(isEmptyString())")},
            new Object[]{"foo", "bar", Arrays.asList("isEmptyOrNullString()")},
            new Object[]{"foo", "bar", Arrays.asList("nullValue()")},
            new Object[]{"foo", null, Arrays.asList("notNullValue()")},
            new Object[]{"foo", "[bar]", Arrays.asList("empty()")},
            new Object[]{"foo", "bar", Arrays.asList("empty()")},
            new Object[]{"foo", "4", Arrays.asList("greaterThan(4)")},
            new Object[]{"foo", "4", Arrays.asList("allOf(greaterThan(4), lessThan(6), not(lessThan(5)))")},
            new Object[]{"foo", "6", Arrays.asList("is(not(greaterThan(5)))")},
            new Object[]{"foo", "4", Arrays.asList("greaterThanOrEqualTo(5)")},
            new Object[]{"foo", "5", Arrays.asList("lessThan(5)")},
            new Object[]{"foo", "0", Arrays.asList("not(lessThan(1))")},
            new Object[]{"foo", "5", Arrays.asList("lessThanOrEqualTo(4)")},
            new Object[]{"foo", "", Arrays.asList("5", "lessThanOrEqualTo(4)")},
            new Object[]{"foo", "[value1,value2]", Arrays.asList("hasSize(5)") }
        };
    }
}