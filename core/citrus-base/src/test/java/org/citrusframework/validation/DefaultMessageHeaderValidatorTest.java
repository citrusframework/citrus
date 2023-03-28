/*
 * Copyright 2006-2017 the original author or authors.
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

import org.citrusframework.UnitTestSupport;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class DefaultMessageHeaderValidatorTest extends UnitTestSupport {

    private DefaultMessageHeaderValidator validator = new DefaultMessageHeaderValidator();
    private HeaderValidationContext validationContext = new HeaderValidationContext();

    @Test
    public void testValidateNoMessageHeaders() throws Exception {
        Message receivedMessage = new DefaultMessage("Hello World!");
        Message controlMessage = new DefaultMessage("Hello World!");

        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }

    @Test
    public void testValidateMessageHeaders() throws Exception {
        Message receivedMessage = new DefaultMessage("Hello World!")
                .setHeader("foo", "foo_test")
                .setHeader("additional", "additional")
                .setHeader("bar", "bar_test");
        Message controlMessage = new DefaultMessage("Hello World!")
                .setHeader("foo", "foo_test")
                .setHeader("bar", "bar_test");

        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }

    @Test
    public void testValidateMessageHeadersIgnoreCase() throws Exception {
        try {
            Message receivedMessage = new DefaultMessage("Hello World!")
                    .setHeader("X-Foo", "foo_test")
                    .setHeader("X-Additional", "additional")
                    .setHeader("X-Bar", "bar_test");
            Message controlMessage = new DefaultMessage("Hello World!")
                    .setHeader("x-foo", "foo_test")
                    .setHeader("x-bar", "bar_test");

            validationContext.setHeaderNameIgnoreCase(true);
            validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
        } finally {
            validationContext.setHeaderNameIgnoreCase(false);
        }
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testValidateMessageHeadersIgnoreCaseError() throws Exception {
        Message receivedMessage = new DefaultMessage("Hello World!")
                .setHeader("X-Foo", "foo_test")
                .setHeader("X-Additional", "additional")
                .setHeader("X-Bar", "bar_test");
        Message controlMessage = new DefaultMessage("Hello World!")
                .setHeader("x-foo", "foo_test")
                .setHeader("x-bar", "bar_test");

        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }

    @Test
    public void testValidateMessageHeadersVariableSupport() throws Exception {
        Message receivedMessage = new DefaultMessage("Hello World!")
                .setHeader("foo", "foo_test")
                .setHeader("additional", "additional")
                .setHeader("bar", "bar_test");
        Message controlMessage = new DefaultMessage("Hello World!")
                .setHeader("foo", "citrus:concat('foo', '_test')")
                .setHeader("bar", "${bar}");

        context.setVariable("bar", "bar_test");

        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }

    @Test
    public void testValidateMessageHeadersMatcherSupport() throws Exception {
        Message receivedMessage = new DefaultMessage("Hello World!")
                .setHeader("foo", "foo_test")
                .setHeader("additional", "additional")
                .setHeader("bar", "bar_test");
        Message controlMessage = new DefaultMessage("Hello World!")
                .setHeader("foo", "@startsWith('foo')@")
                .setHeader("bar", "@endsWith('_test')@");

        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testValidateError() throws Exception {
        Message receivedMessage = new DefaultMessage("Hello World!")
                .setHeader("foo", "other_value")
                .setHeader("bar", "bar_test");
        Message controlMessage = new DefaultMessage("Hello World!")
                .setHeader("foo", "foo_test")
                .setHeader("bar", "bar_test");

        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testValidateErrorMissingHeader() throws Exception {
        Message receivedMessage = new DefaultMessage("Hello World!")
                .setHeader("bar", "bar_test");
        Message controlMessage = new DefaultMessage("Hello World!")
                .setHeader("foo", "foo_test")
                .setHeader("bar", "bar_test");

        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }

}
