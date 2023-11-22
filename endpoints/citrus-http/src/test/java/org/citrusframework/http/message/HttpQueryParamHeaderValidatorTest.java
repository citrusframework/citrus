/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.http.message;

import java.util.Collections;

import org.citrusframework.context.TestContextFactory;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.is;

/**
 * @author Christoph Deppisch
 * @since 2.7.6
 */
public class HttpQueryParamHeaderValidatorTest extends AbstractTestNGUnitTest {

    private HttpQueryParamHeaderValidator validator = new HttpQueryParamHeaderValidator();
    private HeaderValidationContext validationContext = new HeaderValidationContext();

    @Override
    protected TestContextFactory createTestContextFactory() {
        return TestContextFactory.newInstance();
    }

    @Test(dataProvider = "successData")
    public void testValidateHeader(Object receivedValue, Object controlValue) {
        validator.validateHeader(HttpMessageHeaders.HTTP_QUERY_PARAMS, receivedValue, controlValue, context, validationContext);
    }

    @DataProvider
    public Object[][] successData() {
        return new Object[][] {
                new Object[] { "foobar", "@contains(foo)@" },
                new Object[] { "foo=fooValue,bar=barValue", "foo=fooValue,bar=barValue" },
                new Object[] { "foo=,bar=barValue", "foo=,bar=barValue" },
                new Object[] { null, null },
                new Object[] { Collections.singletonMap("key", "value"), Collections.singletonMap("key", "value") },
                new Object[] { Collections.singletonMap("key", "value"), Collections.singletonMap("key", is("value")) }
        };
    }

    @Test
    public void testValidateHeaderVariableSupport() {
        context.setVariable("control", "barValue");
        validator.validateHeader(HttpMessageHeaders.HTTP_QUERY_PARAMS, "foo=fooValue,bar=barValue", "foo=fooValue,bar=${control}", context, validationContext);
    }

    @Test
    public void testValidateHeaderValidationMatcherSupport() {
        validator.validateHeader(HttpMessageHeaders.HTTP_QUERY_PARAMS, "foo=fooValue,bar=barValue", "foo=fooValue,bar=@ignore@", context, validationContext);
        validator.validateHeader(HttpMessageHeaders.HTTP_QUERY_PARAMS, "foo=fooValue,bar=barValue", "foo=fooValue,bar=@hasLength(8)@", context, validationContext);
    }

    @Test(dataProvider = "errorData", expectedExceptions = ValidationException.class)
    public void testValidateHeaderError(Object receivedValue, Object controlValue) {
        validator.validateHeader(HttpMessageHeaders.HTTP_QUERY_PARAMS, receivedValue, controlValue, context, validationContext);
    }

    @DataProvider
    public Object[][] errorData() {
        return new Object[][] {
                new Object[] { "foobar", "@contains(wrong)@" },
                new Object[] { "foo=fooValue,bar=barValue", "foo=fooValue,bar=wrong" },
                new Object[] { "foo=,bar=barValue", "foo=fooValue,bar=barValue" },
                new Object[] { "foo=fooValue,bar=barValue", "foo=,bar=barValue" },
                new Object[] { Collections.singletonMap("key", "value"), Collections.singletonMap("key", "wrong") }
        };
    }
}
