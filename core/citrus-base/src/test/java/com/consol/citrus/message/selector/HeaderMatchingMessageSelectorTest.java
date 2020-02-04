/*
 * Copyright 2006-2012 the original author or authors.
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
package com.consol.citrus.message.selector;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageHeaders;
import com.consol.citrus.validation.matcher.ValidationMatcherLibrary;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class HeaderMatchingMessageSelectorTest {

    private TestContext context;

    @BeforeMethod
    public void setupMocks() {
        context = new TestContext();
    }

    @Test
    public void testHeaderMatchingSelector() {
        HeaderMatchingMessageSelector messageSelector = new HeaderMatchingMessageSelector("operation", "foo", context);

        Message acceptMessage = new DefaultMessage("FooTest")
                .setHeader("operation", "foo");

        Message declineMessage = new DefaultMessage("FooTest")
                .setHeader("operation", "foobar");

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));
    }

    @Test
    public void testHeaderMatchingSelectorValidationMatcher() {
        ValidationMatcherLibrary library = new ValidationMatcherLibrary();
        library.getMembers().put("contains", (fieldName, value, controlParameters, context) -> {
            if (!value.contains(controlParameters.get(0))) {
                throw new ValidationException("Not containing " + controlParameters.get(0));
            }
        });
        context.getValidationMatcherRegistry().getValidationMatcherLibraries().add(library);

        HeaderMatchingMessageSelector messageSelector = new HeaderMatchingMessageSelector("operation", "@contains(foo)@", context);

        Message acceptMessage = new DefaultMessage("FooTest")
                .setHeader("operation", "barfoobar");

        Message declineMessage = new DefaultMessage("FooTest")
                .setHeader("operation", "bar");

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));
    }

    @Test
    public void testHeaderMatchingSelectorMultipleValues() {
        HeaderMatchingMessageSelector messageSelector = new HeaderMatchingMessageSelector("foo", "bar", context);

        Message acceptMessage = new DefaultMessage("FooTest")
                .setHeader("foo", "bar")
                .setHeader("operation", "foo");

        Message declineMessage = new DefaultMessage("FooTest")
                .setHeader("operation", "foo");

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));
    }

    @Test
    public void testHeaderMatchingSelectorMissingHeader() {
        HeaderMatchingMessageSelector messageSelector = new HeaderMatchingMessageSelector("operation", "foo", context);

        Message acceptMessage = new DefaultMessage("FooTest")
                .setHeader("operation", "foo");

        Message declineMessage = new DefaultMessage("FooTest");

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));
    }

    @Test
    public void testHeaderMatchingSelectorWithMessageObjectPayload() {
        HeaderMatchingMessageSelector messageSelector = new HeaderMatchingMessageSelector("operation", "foo", context);

        Message acceptMessage = new DefaultMessage(new DefaultMessage("FooTest")
                .setHeader("operation", "foo"));

        Message declineMessage = new DefaultMessage(new DefaultMessage("FooTest")
                .setHeader("operation", "foobar"));

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));

        messageSelector = new HeaderMatchingMessageSelector(MessageHeaders.ID, acceptMessage.getId().toString(), context);

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));
    }

}
