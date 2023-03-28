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

package org.citrusframework.channel.selector;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.message.DefaultMessage;
import org.springframework.integration.support.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class PayloadMessageSelectorTest extends UnitTestSupport {

    @Test
    public void testPayloadEvaluation() {
        PayloadMatchingMessageSelector messageSelector = new PayloadMatchingMessageSelector("payload", "foobar", context);

        Assert.assertTrue(messageSelector.accept(MessageBuilder.withPayload("foobar").build()));
        Assert.assertFalse(messageSelector.accept(MessageBuilder.withPayload("barfoo").build()));
    }

    @Test
    public void testPayloadEvaluationValidationMatcher() {
        PayloadMatchingMessageSelector messageSelector = new PayloadMatchingMessageSelector("payload", "@startsWith(foo)@", context);

        Assert.assertTrue(messageSelector.accept(MessageBuilder.withPayload("foobar").build()));
        Assert.assertFalse(messageSelector.accept(MessageBuilder.withPayload("barfoo").build()));
    }

    @Test
    public void testPayloadEvaluationWithMessageObjectPayload() {
        PayloadMatchingMessageSelector messageSelector = new PayloadMatchingMessageSelector("payload", "foobar", context);

        Assert.assertTrue(messageSelector.accept(MessageBuilder.withPayload(new DefaultMessage("foobar")).build()));
        Assert.assertFalse(messageSelector.accept(MessageBuilder.withPayload(new DefaultMessage("barfoo")).build()));
    }
}
