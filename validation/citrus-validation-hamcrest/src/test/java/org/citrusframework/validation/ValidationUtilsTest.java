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

import org.citrusframework.context.TestContextFactory;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.hamcrest.Matchers;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public class ValidationUtilsTest extends AbstractTestNGUnitTest {

    @Override
    protected TestContextFactory createTestContextFactory() {
        return TestContextFactory.newInstance();
    }

    @Test(dataProvider = "testData")
    public void testValidateValues(String actualValue, Object expectedValue, String path) throws Exception {
        ValidationUtils.validateValues(actualValue, expectedValue, path, context);
    }

    @Test(dataProvider = "testDataFailed", expectedExceptions = ValidationException.class)
    public void testValidateValuesFailure(String actualValue, Object expectedValue, String path) throws Exception {
        ValidationUtils.validateValues(actualValue, expectedValue, path, context);
    }

    @DataProvider
    public Object[][] testData() {
        return new Object[][] {
            new Object[] {null, "@assertThat(nullValue())@", "nullValidationMatcherCompare"},
            new Object[] {null, Matchers.nullValue(), "nullHamcrestMatcherCompare"},
            new Object[] {"foo", Matchers.allOf(Matchers.not(Matchers.isEmptyString()), Matchers.equalTo("foo")), "hamcrestMatcherCompare"}
        };
    }

    @DataProvider
    public Object[][] testDataFailed() {
        return new Object[][] {
                new Object[] {null, "@assertThat(notNullValue())@", "nullValidationMatcherCompare"},
                new Object[] {"foo", Matchers.allOf(Matchers.isEmptyString(), Matchers.equalTo("bar")), "hamcrestMatcherCompare"}
        };
    }
}
