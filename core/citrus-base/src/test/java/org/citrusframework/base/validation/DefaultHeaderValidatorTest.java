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

package org.citrusframework.base.validation;

import java.util.Arrays;
import java.util.Collections;

import org.citrusframework.base.UnitTestSupport;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.validation.DefaultHeaderValidator;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @since 2.7.6
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DefaultHeaderValidatorTest extends UnitTestSupport {

    private final DefaultHeaderValidator validator = new DefaultHeaderValidator();
    private final HeaderValidationContext validationContext = new HeaderValidationContext.Builder().build();

    @ParameterizedTest
    @MethodSource("successData")
    public void testValidateHeader(Object receivedValue, Object controlValue) {
        validator.validateHeader("foo", receivedValue, controlValue, context, validationContext);
    }

    public Object[][] successData() {
        return new Object[][] {
            new Object[] { "foo", "foo" },
            new Object[] { null, "" },
            new Object[] { null, null },
            new Object[] { Arrays.asList("foo", "bar"), Arrays.asList("foo", "bar") },
            new Object[] { new String[] {"foo", "bar"}, new String[] {"foo", "bar"} },
            new Object[] { Collections.singletonMap("key", "value"), Collections.singletonMap("key", "value") }
        };
    }

    @Test
    public void testValidateHeaderVariableSupport() {
        context.setVariable("control", "bar");

        validator.validateHeader("foo", "bar", "${control}", context, validationContext);
    }

    @Test
    public void testValidateHeaderValidationMatcherSupport() {
        validator.validateHeader("foo", "bar", "@ignore@", context, validationContext);
        validator.validateHeader("foo", "bar", "@hasLength(3)@", context, validationContext);
    }

    @ParameterizedTest
    @MethodSource("errorData")
    public void testValidateHeaderError(Object receivedValue, Object controlValue) {
        assertThrows(ValidationException.class, () -> {
            validator.validateHeader("foo", receivedValue, controlValue, context, validationContext);
        });
    }

    public Object[][] errorData() {
        return new Object[][] {
                new Object[] { "foo", "wrong" },
                new Object[] { null, "wrong" },
                new Object[] { "foo", null },
                new Object[] { Arrays.asList("foo", "bar"), Arrays.asList("foo", "wrong") },
                new Object[] { new String[] {"foo", "bar"}, new String[] {"foo", "wrong"} },
                new Object[] { Collections.singletonMap("key", "value"), Collections.singletonMap("key", "wrong") }
        };
    }
}
