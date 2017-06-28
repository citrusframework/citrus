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

package com.consol.citrus.validation;

import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.hamcrest.Matchers;
import org.springframework.beans.TypeMismatchException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class ValidationUtilsTest extends AbstractTestNGUnitTest {

    @Test(dataProvider = "testData")
    public void testValidateValues(String actualValue, Object expectedValue, String path) throws Exception {
        ValidationUtils.validateValues(actualValue, expectedValue, path, context);
    }

    @Test(dataProvider = "testDataFailed", expectedExceptions = ValidationException.class)
    public void testValidateValuesFailure(String actualValue, Object expectedValue, String path) throws Exception {
        ValidationUtils.validateValues(actualValue, expectedValue, path, context);
    }

    @Test(dataProvider = "testDataTypeFailed", expectedExceptions = TypeMismatchException.class)
    public void testValidateValuesTypeFailure(String actualValue, Object expectedValue, String path) throws Exception {
        ValidationUtils.validateValues(actualValue, expectedValue, path, context);
    }

    @DataProvider
    public Object[][] testData() {
        return new Object[][] {
            new Object[] {"foo", "foo", "stringCompare"},
            new Object[] {"true", true, "booleanCompare"},
            new Object[] {"5", 5, "integerCompare"},
            new Object[] {"5", 5L, "longCompare"},
            new Object[] {"5.0", 5.0D, "doubleCompare"},
            new Object[] {"[a, b, c, d]", "[a, b, c, d]", "arrayStringCompare"},
            new Object[] {"a,b,c,d", new String[] {"a", "b", "c", "d"}, "arrayCompare"},
            new Object[] {"[a,b,c,d]", new String[] {"a", "b", "c", "d"}, "arrayCompare"},
            new Object[] {"[[a],[b],[c],[d]]", new String[] {"[a]", "[b]", "[c]", "[d]"}, "arrayCompare"},
            new Object[] {"a,b,c,d", Arrays.asList("a", "b", "c", "d"), "listCompare"},
            new Object[] {"[a,b,c,d]", Arrays.asList("a", "b", "c", "d"), "listCompare"},
            new Object[] {"[[a],[b],[c],[d]]", Arrays.asList("[a]", "[b]", "[c]", "[d]"), "listCompare"},
            new Object[] {"{a=b}", Collections.singletonMap("a", "b"), "mapCompare"},
            new Object[] {"foo", "foo".getBytes(), "bytesCompare"},
            new Object[] {null, null, "nullCompare"},
            new Object[] {null, "", "nullEmptyStringCompare"},
            new Object[] {null, "@assertThat(nullValue())@", "nullValidationMatcherCompare"},
            new Object[] {null, Matchers.nullValue(), "nullHamcrestMatcherCompare"},
            new Object[] {"foo", Matchers.allOf(Matchers.not(Matchers.isEmptyString()), Matchers.equalTo("foo")), "hamcrestMatcherCompare"}
        };
    }

    @DataProvider
    public Object[][] testDataFailed() {
        return new Object[][] {
                new Object[] {"foo", "bar", "stringCompare"},
                new Object[] {"true", false, "booleanCompare"},
                new Object[] {"5", 6, "integerCompare"},
                new Object[] {"5", 4L, "longCompare"},
                new Object[] {"5.0", 5.5D, "doubleCompare"},
                new Object[] {"[a, b, c, d]", "[a, c, d]", "arrayStringCompare"},
                new Object[] {"a,b,c,d", new String[] {"a", "c", "b", "d"}, "arrayCompare"},
                new Object[] {"[a,b,c,d]", new String[] {"a", "b", "c", "f"}, "arrayCompare"},
                new Object[] {"[[a],[b],[c],[d]]", new String[] {"[a]", "[b]", "[c]", "[f]"}, "arrayCompare"},
                new Object[] {"abcd", new String[] {"a", "c", "b", "d"}, "arrayCompare"},
                new Object[] {"a,b,c,d", Arrays.asList("a", "b", "c", "f"), "listCompare"},
                new Object[] {"[a,b,c,d]", Arrays.asList("a", "c", "d"), "listCompare"},
                new Object[] {"[[a],[b],[c],[d]]", Arrays.asList("[a]", "[b]", "[c]", "[f]"), "listCompare"},
                new Object[] {"abcd", Arrays.asList("a", "b", "c", "f"), "listCompare"},
                new Object[] {"{a=b}", Collections.singletonMap("a", "c"), "mapCompare"},
                new Object[] {"foo", "bar".getBytes(), "bytesCompare"},
                new Object[] {null, 5, "nullCompare"},
                new Object[] {"foo", null, "nullCompare"},
                new Object[] {null, "bar", "nullCompare"},
                new Object[] {null, "@assertThat(notNullValue())@", "nullValidationMatcherCompare"},
                new Object[] {"foo", Matchers.allOf(Matchers.isEmptyString(), Matchers.equalTo("bar")), "hamcrestMatcherCompare"}
        };
    }

    @DataProvider
    public Object[][] testDataTypeFailed() {
        return new Object[][] {
                new Object[] {"foo", 0, "stringCompare"},
                new Object[] {"bar", false, "booleanCompare"}
        };
    }
}