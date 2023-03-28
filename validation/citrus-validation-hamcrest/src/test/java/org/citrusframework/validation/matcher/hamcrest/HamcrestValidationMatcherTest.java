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

package org.citrusframework.validation.matcher.hamcrest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.citrusframework.context.TestContextFactory;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.hamcrest.CustomMatcher;
import org.hamcrest.Matcher;
import org.springframework.util.AntPathMatcher;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class HamcrestValidationMatcherTest extends AbstractTestNGUnitTest {

    private HamcrestValidationMatcher validationMatcher = new HamcrestValidationMatcher();

    @Override
    protected TestContextFactory createTestContextFactory() {
        TestContextFactory factory = super.createTestContextFactory();

        HamcrestMatcherProvider customMatcherProvider = new HamcrestMatcherProvider() {
            @Override
            public String getName() {
                return "matchesPath";
            }

            @Override
            public Matcher<String> provideMatcher(String predicate) {
                return new CustomMatcher<String>(String.format("path matching %s", predicate)) {
                    @Override
                    public boolean matches(Object item) {
                        return ((item instanceof String) && new AntPathMatcher().match(predicate, (String) item));
                    }
                };
            }
        };

        factory.getReferenceResolver().bind("matchesPath", customMatcherProvider);
        return factory;
    }

    @Test(dataProvider = "testData")
    public void testValidate(String path, String value, List<String> params) throws Exception {
        validationMatcher.validate( path, value, params, context);
    }

    @DataProvider
    public Object[][] testData() {
        return new Object[][] {
            new Object[]{ "foo", "value", Collections.singletonList("equalTo(value)") },
            new Object[]{ "foo", "value", Collections.singletonList("equalTo('value')") },
            new Object[]{"foo", "value", Collections.singletonList("not(equalTo(other))")},
            new Object[]{"foo", "value", Collections.singletonList("is(not(other))")},
            new Object[]{"foo", "value", Collections.singletonList("not(is(other))")},
            new Object[]{"foo", "value", Collections.singletonList("equalToIgnoringCase(VALUE)")},
            new Object[]{"foo", "value", Collections.singletonList("containsString(lue)")},
            new Object[]{"foo", "value", Collections.singletonList("not(containsString(other))")},
            new Object[]{"foo", "value", Collections.singletonList("startsWith(val)")},
            new Object[]{"foo", "value", Collections.singletonList("endsWith(lue)")},
            new Object[]{"foo", "value", Collections.singletonList("anyOf(startsWith(val), endsWith(lue))")},
            new Object[]{"foo", "value", Collections.singletonList("allOf(startsWith(val), endsWith(lue))")},
            new Object[]{"foo", "value/12345", Collections.singletonList("matchesPath(value/{id})")},
            new Object[]{"foo", "value/12345/test", Collections.singletonList("matchesPath(value/{id}/test)")},
            new Object[]{"foo", "value", Collections.singletonList("isOneOf(value, other)")},
            new Object[]{"foo", "test value", Collections.singletonList("isOneOf('test value', 'other ')")},
            new Object[]{"foo", "9.0", Collections.singletonList("isOneOf(9, 9.0)")},
            new Object[]{"foo", "value", Collections.singletonList("isIn(value, other)")},
            new Object[]{"foo", "test value", Collections.singletonList("isIn('test value', 'other ')")},
            new Object[]{"foo", "9.0", Collections.singletonList("isIn(9, 9.0)")},
            new Object[]{"foo", "", Collections.singletonList("isEmptyString()")},
            new Object[]{"foo", "bar", Collections.singletonList("not(isEmptyString())")},
            new Object[]{"foo", null, Collections.singletonList("isEmptyOrNullString()")},
            new Object[]{"foo", null, Collections.singletonList("nullValue()")},
            new Object[]{"foo", "bar", Collections.singletonList("notNullValue()")},
            new Object[]{"foo", "[]", Collections.singletonList("empty()")},
            new Object[]{"foo", "", Collections.singletonList("empty()")},
            new Object[]{"foo", "bar", Collections.singletonList("not(empty())")},
            new Object[]{"foo", "10", Collections.singletonList("greaterThan(9)")},
            new Object[]{"foo", "10.0", Collections.singletonList("greaterThan(9.0)")},
            new Object[]{"foo", "10.4", Collections.singletonList("greaterThanOrEqualTo(10.4)")},
            new Object[]{"foo", "10.5", Collections.singletonList("greaterThanOrEqualTo(10.4)")},
            new Object[]{"foo", "10", Collections.singletonList("allOf(greaterThan(9), lessThan(11), not(lessThan(10)))")},
            new Object[]{"foo", "10", Collections.singletonList("is(not(greaterThan(10)))")},
            new Object[]{"foo", "10", Collections.singletonList("greaterThanOrEqualTo(10)")},
            new Object[]{"foo", "9", Collections.singletonList("lessThan(10)")},
            new Object[]{"foo", "9", Collections.singletonList("not(lessThan(1))")},
            new Object[]{"foo", "9", Collections.singletonList("lessThanOrEqualTo(9)")},
            new Object[]{"foo", "8.9", Collections.singletonList("closeTo(9.0, 0.1)")},
            new Object[]{"foo", "9.1", Collections.singletonList("closeTo(9.0, 0.1)")},
            new Object[]{"foo", "9.0", Collections.singletonList("closeTo(9)")},
            new Object[]{"foo", "9.0", Collections.singletonList("allOf(greaterThanOrEqualTo(9), lessThanOrEqualTo(9))")},
            new Object[]{"foo", "", Arrays.asList("1", "lessThanOrEqualTo(9)")},
            new Object[]{"foo", "", Arrays.asList("9", "lessThanOrEqualTo(9)")},
            new Object[]{"foo", "{value1=value2,value4=value5}", Collections.singletonList("hasSize(2)") },
            new Object[]{"foo", "{value1=value2,value4=value5}", Collections.singletonList("hasEntry(value1,value2)") },
            new Object[]{"foo", "{value1=value2,value4=value5}", Collections.singletonList("hasKey(value1)") },
            new Object[]{"foo", "{\"value1\"=\"value2\",\"value4\"=\"value5\"}", Collections.singletonList("hasKey(value1)") },
            new Object[]{"foo", "{value1=value2,value4=value5}", Collections.singletonList("hasValue(value2)") },
            new Object[]{"foo", "[value1,value2,value3,value4,value5]", Collections.singletonList("hasSize(5)") },
            new Object[]{"foo", "[value1,value2,value3,value4,value5]", Collections.singletonList("everyItem(startsWith(value))") },
            new Object[]{"foo", "[value1,value2,value3,value4,value5]", Collections.singletonList("hasItem(value2)") },
            new Object[]{"foo", "[value1,value2,value3,value4,value5]", Collections.singletonList("hasItems(value2,value5)") },
            new Object[]{"foo", "[\"value1\",\"value2\",\"value3\",\"value4\",\"value5\"]", Collections.singletonList("hasItems(value2,value5)") },
            new Object[]{"foo", "[value1,value2,value3,value4,value5]", Collections.singletonList("contains(value1,value2,value3,value4,value5)") },
            new Object[]{"foo", "[value1,value2,value3,value4,value5]", Collections.singletonList("containsInAnyOrder(value2,value4,value1,value3,value5)") },
            new Object[]{"foo", "[\"unique_value\",\"different_unique_value\"]", Collections.singletonList("hasSize(2)") },
            new Object[]{"foo", "[\"duplicate_value\",\"duplicate_value\"]", Collections.singletonList("hasSize(2)") }
        };
    }

    @Test(dataProvider = "testDataFailed", expectedExceptions = ValidationException.class)
    public void testValidateFailed(String path, String value, List<String> params) throws Exception {
        validationMatcher.validate( path, value, params, context);
    }

    @DataProvider
    public Object[][] testDataFailed() {
        return new Object[][] {
            new Object[]{ "foo", "value", Collections.singletonList("equalTo(wrong)") },
            new Object[]{"foo", "value", Collections.singletonList("not(equalTo(value))")},
            new Object[]{"foo", "value", Collections.singletonList("is(not(value))")},
            new Object[]{"foo", "value", Collections.singletonList("not(is(value))")},
            new Object[]{"foo", "value", Collections.singletonList("equalToIgnoringCase(WRONG)")},
            new Object[]{"foo", "value", Collections.singletonList("containsString(wrong)")},
            new Object[]{"foo", "value", Collections.singletonList("not(containsString(value))")},
            new Object[]{"foo", "value", Collections.singletonList("startsWith(wrong)")},
            new Object[]{"foo", "value", Collections.singletonList("endsWith(wrong)")},
            new Object[]{"foo", "value", Collections.singletonList("anyOf(startsWith(wrong), endsWith(wrong))")},
            new Object[]{"foo", "value", Collections.singletonList("allOf(startsWith(wrong), endsWith(wrong))")},
            new Object[]{"foo", "value/12345", Collections.singletonList("matchesPath(value/{id}/{operation})")},
            new Object[]{"foo", "value/12345/test", Collections.singletonList("matchesPath(value/{id})")},
            new Object[]{"foo", "value", Collections.singletonList("matchesPath(value/{id})")},
            new Object[]{"foo", "value", Collections.singletonList("isOneOf(some, other)")},
            new Object[]{"foo", "test value", Collections.singletonList("isOneOf('test value ' , 'other ')")},
            new Object[]{"foo", "9.0", Collections.singletonList("isOneOf(8, 8.0)")},
            new Object[]{"foo", "value", Collections.singletonList("isIn(some, other)")},
            new Object[]{"foo", "test value", Collections.singletonList("isIn('test value ' , 'other ')")},
            new Object[]{"foo", "9.0", Collections.singletonList("isIn(8, 8.0)")},
            new Object[]{"foo", "bar", Collections.singletonList("isEmptyString()")},
            new Object[]{"foo", "", Collections.singletonList("not(isEmptyString())")},
            new Object[]{"foo", "bar", Collections.singletonList("isEmptyOrNullString()")},
            new Object[]{"foo", "bar", Collections.singletonList("nullValue()")},
            new Object[]{"foo", null, Collections.singletonList("notNullValue()")},
            new Object[]{"foo", "[bar]", Collections.singletonList("empty()")},
            new Object[]{"foo", "bar", Collections.singletonList("empty()")},
            new Object[]{"foo", "9", Collections.singletonList("greaterThan(9)")},
            new Object[]{"foo", "9.0", Collections.singletonList("greaterThan(9.0)")},
            new Object[]{"foo", "9.3", Collections.singletonList("greaterThanOrEqualTo(9.4)")},
            new Object[]{"foo", "9", Collections.singletonList("allOf(greaterThan(9), lessThan(11), not(lessThan(10)))")},
            new Object[]{"foo", "11", Collections.singletonList("is(not(greaterThan(10)))")},
            new Object[]{"foo", "9", Collections.singletonList("greaterThanOrEqualTo(10)")},
            new Object[]{"foo", "10", Collections.singletonList("lessThan(10)")},
            new Object[]{"foo", "ten", Collections.singletonList("lessThan(10)")},
            new Object[]{"foo", "0", Collections.singletonList("not(lessThan(1))")},
            new Object[]{"foo", "10", Collections.singletonList("lessThanOrEqualTo(9)")},
            new Object[]{"foo", "8.9", Collections.singletonList("closeTo(9)")},
            new Object[]{"foo", "9.1", Collections.singletonList("closeTo(9)")},
            new Object[]{"foo", "9.0", Collections.singletonList("closeTo(9.5, 0.1)")},
            new Object[]{"foo", "9.1", Collections.singletonList("allOf(greaterThanOrEqualTo(9), lessThanOrEqualTo(9))")},
            new Object[]{"foo", "", Arrays.asList("10", "lessThanOrEqualTo(9)")},
            new Object[]{"foo", "{value1=value2}", Collections.singletonList("hasSize(5)") },
            new Object[]{"foo", "{value1=value2}", Collections.singletonList("hasEntry(value4,value5)") },
            new Object[]{"foo", "{value1=value2}", Collections.singletonList("hasKey(value4)") },
            new Object[]{"foo", "{value1=value2}", Collections.singletonList("hasValue(value5)") },
            new Object[]{"foo", "[value1,value2]", Collections.singletonList("hasSize(5)") },
            new Object[]{"foo", "[value1,value2]", Collections.singletonList("everyItem(endsWith(1))") },
            new Object[]{"foo", "[value1,value2]", Collections.singletonList("hasItem(value5)") },
            new Object[]{"foo", "[value1,value2]", Collections.singletonList("hasItems(value1,value2,value5)") },
            new Object[]{"foo", "[value1,value2]", Collections.singletonList("contains(value1)") },
            new Object[]{"foo", "[value1,value2]", Collections.singletonList("containsInAnyOrder(value2,value4)") }
        };
    }
}
