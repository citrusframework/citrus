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

package org.citrusframework.http.message;

import java.util.Collections;

import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;

/**
 * @since 2.7.6
 */
public class HttpQueryParamHeaderValidatorTest extends AbstractUnitTest {

    private final HttpQueryParamHeaderValidator validator = new HttpQueryParamHeaderValidator();
    private final HeaderValidationContext validationContext = new HeaderValidationContext.Builder().build();

    @ParameterizedTest
    @MethodSource("successData")
    public void testValidateHeader(Object receivedValue, Object controlValue) {
        validator.validateHeader(HttpMessageHeaders.HTTP_QUERY_PARAMS, receivedValue, controlValue, context, validationContext);
    }

    static Stream<Arguments> successData() {
        return Stream.of(
                Arguments.of("foobar", "@contains(foo)@"),
                Arguments.of("foo=fooValue,bar=barValue", "foo=fooValue,bar=barValue"),
                Arguments.of("foo=,bar=barValue", "foo=,bar=barValue"),
                Arguments.of("foo=1,foo=2,foo=3,bar=barValue", "foo=1,foo=2,foo=3,bar=barValue"),
                Arguments.of("foo=1,foo=2,foo=3,bar=barValue", "foo=3,foo=2,foo=1,bar=barValue"),
                Arguments.of("$select=Name%2CValue,$top=10", "$select=Name%2CValue,$top=10"),
                Arguments.of(null, null),
                Arguments.of(Collections.singletonMap("key", "value"), Collections.singletonMap("key", "value")),
                Arguments.of(Collections.singletonMap("key", "value"), Collections.singletonMap("key", is("value")))
        );
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

    @ParameterizedTest
    @MethodSource("errorData")
    public void testValidateHeaderError(Object receivedValue, Object controlValue) {
        Assertions.assertThrows(ValidationException.class, () ->
                validator.validateHeader(HttpMessageHeaders.HTTP_QUERY_PARAMS, receivedValue, controlValue, context, validationContext));
    }

    static Stream<Arguments> errorData() {
        return Stream.of(
                Arguments.of("foobar", "@contains(wrong)@"),
                Arguments.of("foo=fooValue,bar=barValue", "foo=fooValue,bar=wrong"),
                Arguments.of("foo=,bar=barValue", "foo=fooValue,bar=barValue"),
                Arguments.of("foo=fooValue,bar=barValue", "foo=,bar=barValue"),
                Arguments.of(Collections.singletonMap("key", "value"), Collections.singletonMap("key", "wrong"))
        );
    }
}
