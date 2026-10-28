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

import java.nio.charset.StandardCharsets;

import org.citrusframework.base.UnitTestSupport;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.validation.DefaultTextEqualsMessageValidator;
import org.citrusframework.validation.context.DefaultValidationContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DefaultTextEqualsMessageValidatorTest extends UnitTestSupport {

    private final DefaultTextEqualsMessageValidator validator = new DefaultTextEqualsMessageValidator();
    private final DefaultValidationContext validationContext = new DefaultValidationContext();

    @ParameterizedTest
    @MethodSource("successTests")
    public void testValidate(Object received, Object control) {
        Message receivedMessage = new DefaultMessage(received);
        Message controlMessage = new DefaultMessage(control);

        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }

    @ParameterizedTest
    @MethodSource("errorTests")
    public void testValidateError(Object received, Object control) {
        Message receivedMessage = new DefaultMessage(received);
        Message controlMessage = new DefaultMessage(control);

        assertThrows(ValidationException.class, () -> {
            validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
        });
    }

    @Test
    public void testFirstDiff() {
        Assertions.assertEquals("", validator.getFirstDiff("Hello", "Hello"));
        Assertions.assertEquals("at position 2 expected 'i', but was 'ello'", validator.getFirstDiff("Hello", "Hi"));
        Assertions.assertEquals("at position 7 expected 'foo', but was 'bar'", validator.getFirstDiff("Hello bar", "Hello foo"));
        Assertions.assertEquals("at position 29 expected '?', but was '!'", validator.getFirstDiff("Hello foo, how are you doing!", "Hello foo, how are you doing?"));
        Assertions.assertEquals("at position 29 expected '', but was '!'", validator.getFirstDiff("Hello foo, how are you doing!", "Hello foo, how are you doing"));
        Assertions.assertEquals("at position 29 expected '!', but was ''", validator.getFirstDiff("Hello foo, how are you doing", "Hello foo, how are you doing!"));
        Assertions.assertEquals("at position 1 expected '2', but was '1'", validator.getFirstDiff("1", "2"));
        Assertions.assertEquals("at position 3 expected '43', but was '34'", validator.getFirstDiff("1234", "1243"));
        Assertions.assertEquals("at position 3 expected ' spaces at all', but was 'spacesatall'", validator.getFirstDiff("nospacesatall", "no spaces at all"));
    }

    private Object[][] successTests() {
        return new Object[][] {
           new Object[]{ null, null },
           new Object[]{ "", null },
           new Object[]{ null, "" },
           new Object[]{ "Hello World!", "Hello World!" },
           new Object[]{ "Hello World!  ", "Hello World!" },
           new Object[]{ "Hello World!", "Hello World!  " },
           new Object[]{ "Hello World!\n", "Hello World!" },
           new Object[]{ "Hello World!\n", "Hello World!\n" },
           new Object[]{ "\nHello World!", "\nHello World!" },
           new Object[]{ "Hello\nWorld!\n", "Hello\nWorld!\n" },
           new Object[]{ "Hello\r\nWorld!\r\n", "Hello\nWorld!\n" },
           new Object[]{ "Hello World!", null }, // empty control message
           new Object[]{ "Hello World!", "" }, // no control message
           new Object[]{ "Hello World!".getBytes(StandardCharsets.UTF_8), "" } // no control message
        };
    }

    private Object[][] errorTests() {
        return new Object[][] {
           new Object[]{ null, "Hello World!" },
           new Object[]{ "", "Hello World!" },
           new Object[]{ "Hello  World!", "Hello World!" },
           new Object[]{ "Hello World!", "Hello  World!" },
           new Object[]{ "Hello\nWorld!", "Hello World!" },
           new Object[]{ "Hello World!", "Hello\nWorld!" },
           new Object[]{ "Hello!", "Hi!" },
        };
    }
}
