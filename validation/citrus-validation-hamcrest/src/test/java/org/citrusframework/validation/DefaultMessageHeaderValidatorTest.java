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

import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class DefaultMessageHeaderValidatorTest extends AbstractTestNGUnitTest {

    private DefaultMessageHeaderValidator validator = new DefaultMessageHeaderValidator();
    private HeaderValidationContext validationContext = new HeaderValidationContext();

    @Test
    public void testValidateMessageHeadersHamcrestMatcherSupport() throws Exception {
        Message receivedMessage = new DefaultMessage("Hello World!")
                .setHeader("foo", "foo_test")
                .setHeader("additional", "additional")
                .setHeader("bar", "bar_test");
        Message controlMessage = new DefaultMessage("Hello World!")
                .setHeader("foo", startsWith("foo"))
                .setHeader("bar", endsWith("_test"));

        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testValidateHamcrestMatcherError() throws Exception {
        Message receivedMessage = new DefaultMessage("Hello World!")
                .setHeader("foo", "foo_test")
                .setHeader("bar", "bar_test");
        Message controlMessage = new DefaultMessage("Hello World!")
                .setHeader("foo", startsWith("bar"))
                .setHeader("bar", endsWith("_test"));

        validator.validateMessage(receivedMessage, controlMessage, context, validationContext);
    }

}
