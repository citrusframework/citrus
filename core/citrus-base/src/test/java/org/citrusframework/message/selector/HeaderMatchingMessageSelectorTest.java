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
package org.citrusframework.message.selector;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageHeaders;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class HeaderMatchingMessageSelectorTest extends UnitTestSupport {

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
