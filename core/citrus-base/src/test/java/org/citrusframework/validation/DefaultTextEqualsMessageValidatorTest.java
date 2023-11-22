/*
 * Copyright 2006-2023 the original author or authors.
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

import java.nio.charset.StandardCharsets;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.validation.context.DefaultValidationContext;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class DefaultTextEqualsMessageValidatorTest extends UnitTestSupport {

    private final DefaultTextEqualsMessageValidator validator = new DefaultTextEqualsMessageValidator();
    private final DefaultValidationContext validationContext = new DefaultValidationContext();

    @Test(dataProvider = "successTests")
    public void testValidate(Object received, Object control) {
        Message receivedMessage = new DefaultMessage(received);
        Message controlMessage = new DefaultMessage(control);

        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }

    @Test(dataProvider = "errorTests", expectedExceptions = ValidationException.class)
    public void testValidateError(Object received, Object control) throws Exception {
        Message receivedMessage = new DefaultMessage(received);
        Message controlMessage = new DefaultMessage(control);

        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }

    @Test
    public void testFirstDiff() {
        Assert.assertEquals(validator.getFirstDiff("Hello", "Hello"), "");
        Assert.assertEquals(validator.getFirstDiff("Hello", "Hi"), "at position 2 expected 'i', but was 'ello'");
        Assert.assertEquals(validator.getFirstDiff("Hello bar", "Hello foo"), "at position 7 expected 'foo', but was 'bar'");
        Assert.assertEquals(validator.getFirstDiff("Hello foo, how are you doing!", "Hello foo, how are you doing?"), "at position 29 expected '?', but was '!'");
        Assert.assertEquals(validator.getFirstDiff("Hello foo, how are you doing!", "Hello foo, how are you doing"), "at position 29 expected '', but was '!'");
        Assert.assertEquals(validator.getFirstDiff("Hello foo, how are you doing", "Hello foo, how are you doing!"), "at position 29 expected '!', but was ''");
        Assert.assertEquals(validator.getFirstDiff("1", "2"), "at position 1 expected '2', but was '1'");
        Assert.assertEquals(validator.getFirstDiff("1234", "1243"), "at position 3 expected '43', but was '34'");
        Assert.assertEquals(validator.getFirstDiff("nospacesatall", "no spaces at all"), "at position 3 expected ' spaces at all', but was 'spacesatall'");
    }

    @DataProvider
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

    @DataProvider
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
