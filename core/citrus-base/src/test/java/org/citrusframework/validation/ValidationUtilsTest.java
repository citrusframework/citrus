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

package org.citrusframework.validation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.validation.matcher.ValidationMatcher;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.citrusframework.validation.ValidationUtils.buildValueToBeInCollectionErrorMessage;
import static org.testng.Assert.assertEquals;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class ValidationUtilsTest extends UnitTestSupport {

    @Override
    protected TestContextFactory createTestContextFactory() {
        TestContextFactory factory = super.createTestContextFactory();
        factory.getValidationMatcherRegistry().getLibraryForPrefix("").getMembers().put("assertThat", new NullValueMatcher());
        return factory;
    }

    @Test(dataProvider = "testData")
    public void testValidateValues(Object actualValue, Object expectedValue, String path) throws Exception {
        ValidationUtils.validateValues(actualValue, expectedValue, path, context);
    }

    @Test(dataProvider = "testDataFailed", expectedExceptions = ValidationException.class)
    public void testValidateValuesFailure(Object actualValue, Object expectedValue, String path) throws Exception {
        ValidationUtils.validateValues(actualValue, expectedValue, path, context);
    }

    @Test(dataProvider = "testDataTypeFailed", expectedExceptions = ValidationException.class)
    public void testValidateValuesTypeFailure(String actualValue, Object expectedValue, String path) throws Exception {
        ValidationUtils.validateValues(actualValue, expectedValue, path, context);
    }

    @Test
    public void testBuildValueToBeInCollectionErrorMessage() {
        String actual = buildValueToBeInCollectionErrorMessage(
                "This is not right",
                "lorem",
                List.of("dolor", "sit", "amet")
        );
        String expected = "This is not right, expected 'lorem' to be in '[dolor, sit, amet]'";
        assertEquals(actual, expected);
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
            new Object[] {new FooValueMatcher.FooValue("foo"), new FooValueMatcher.FooValue("foo"), "fooMatcherCompare"},
            new Object[] {null, "@assertThat(nullValue())@", "nullValidationMatcherCompare"}
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
                new Object[] {new FooValueMatcher.FooValue("foo"), new FooValueMatcher.FooValue("bar"), "fooMatcherCompare"},
                new Object[] {null, "@assertThat(notNullValue())@", "nullValidationMatcherCompare"}
        };
    }

    @DataProvider
    public Object[][] testDataTypeFailed() {
        return new Object[][] {
                new Object[] {"foo", 0, "intCompare"},
                new Object[] {"bar", 1L, "longCompare"}
        };
    }

    private static class NullValueMatcher implements ValidationMatcher {
        @Override
        public void validate(String fieldName, String value, List<String> controlParameters, TestContext context) throws ValidationException {
            if (controlParameters.get(0).equals("nullValue()")) {
                Assert.assertNull(value);
            } else if (controlParameters.get(0).equals("notNullValue()")) {
                Assert.assertNotNull(value);
            }
        }
    }

}
