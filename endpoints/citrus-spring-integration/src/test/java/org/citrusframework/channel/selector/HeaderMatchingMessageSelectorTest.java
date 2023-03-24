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
package org.citrusframework.channel.selector;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.message.DefaultMessage;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class HeaderMatchingMessageSelectorTest extends UnitTestSupport {

    @Test
    public void testHeaderMatchingSelector() {
        HeaderMatchingMessageSelector messageSelector = new HeaderMatchingMessageSelector("operation", "foo", context);

        Message<String> acceptMessage = MessageBuilder.withPayload("FooTest")
                .setHeader("operation", "foo")
                .build();

        Message<String> declineMessage = MessageBuilder.withPayload("FooTest")
                .setHeader("operation", "foobar")
                .build();

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));
    }

    @Test
    public void testHeaderMatchingSelectorValidationMatcher() {
        HeaderMatchingMessageSelector messageSelector = new HeaderMatchingMessageSelector("operation", "@contains(foo)@", context);

        Message<String> acceptMessage = MessageBuilder.withPayload("FooTest")
                .setHeader("operation", "barfoobar")
                .build();

        Message<String> declineMessage = MessageBuilder.withPayload("FooTest")
                .setHeader("operation", "bar")
                .build();

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));
    }

    @Test
    public void testHeaderMatchingSelectorMultipleValues() {
        HeaderMatchingMessageSelector messageSelector = new HeaderMatchingMessageSelector("foo", "bar", context);

        Message<String> acceptMessage = MessageBuilder.withPayload("FooTest")
                .setHeader("foo", "bar")
                .setHeader("operation", "foo")
                .build();

        Message<String> declineMessage = MessageBuilder.withPayload("FooTest")
                .setHeader("operation", "foo")
                .build();

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));
    }

    @Test
    public void testHeaderMatchingSelectorMissingHeader() {
        HeaderMatchingMessageSelector messageSelector = new HeaderMatchingMessageSelector("operation", "foo", context);

        Message<String> acceptMessage = MessageBuilder.withPayload("FooTest")
                .setHeader("operation", "foo")
                .build();

        Message<String> declineMessage = MessageBuilder.withPayload("FooTest")
                .build();

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));
    }

    @Test
    public void testHeaderMatchingSelectorWithMessageObjectPayload() {
        HeaderMatchingMessageSelector messageSelector = new HeaderMatchingMessageSelector("operation", "foo", context);

        Message<DefaultMessage> acceptMessage = MessageBuilder.withPayload(new DefaultMessage("FooTest")
                .setHeader("operation", "foo"))
                .build();

        Message<DefaultMessage> declineMessage = MessageBuilder.withPayload(new DefaultMessage("FooTest")
                .setHeader("operation", "foobar"))
                .build();

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));

        messageSelector = new HeaderMatchingMessageSelector(MessageHeaders.ID, acceptMessage.getHeaders().getId().toString(), context);

        Assert.assertTrue(messageSelector.accept(acceptMessage));
        Assert.assertFalse(messageSelector.accept(declineMessage));
    }

}
