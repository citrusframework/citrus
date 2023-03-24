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
public class JsonPathPayloadMessageSelectorTest extends UnitTestSupport {

    @Test
    public void testJsonPathEvaluation() {
        JsonPathPayloadMessageSelector messageSelector = new JsonPathPayloadMessageSelector("jsonPath:$.foo.text", "foobar", context);

        Assert.assertTrue(messageSelector.accept(MessageBuilder.withPayload("{ \"foo\": { \"text\": \"foobar\" } }").build()));
        Assert.assertFalse(messageSelector.accept(MessageBuilder.withPayload("{ \"foo\": { \"text\": \"barfoo\" } }").build()));
        Assert.assertFalse(messageSelector.accept(MessageBuilder.withPayload("{ \"bar\": { \"text\": \"foobar\" } }").build()));
        Assert.assertFalse(messageSelector.accept(MessageBuilder.withPayload("This is plain text!").build()));
    }

    @Test
    public void testJsonPathEvaluationValidationMatcher() {
        JsonPathPayloadMessageSelector messageSelector = new JsonPathPayloadMessageSelector("jsonPath:$.foo.text", "@startsWith(foo)@", context);

        Assert.assertTrue(messageSelector.accept(MessageBuilder.withPayload("{ \"foo\": { \"text\": \"foobar\" } }").build()));
        Assert.assertFalse(messageSelector.accept(MessageBuilder.withPayload("{ \"foo\": { \"text\": \"barfoo\" } }").build()));
        Assert.assertFalse(messageSelector.accept(MessageBuilder.withPayload("{ \"bar\": { \"text\": \"foobar\" } }").build()));
        Assert.assertFalse(messageSelector.accept(MessageBuilder.withPayload("This is plain text!").build()));
    }

    @Test
    public void testJsonPathEvaluationWithMessageObjectPayload() {
        JsonPathPayloadMessageSelector messageSelector = new JsonPathPayloadMessageSelector("jsonPath:$.foo.text", "foobar", context);

        Assert.assertTrue(messageSelector.accept(MessageBuilder.withPayload(new DefaultMessage("{ \"foo\": { \"text\": \"foobar\" } }")).build()));
        Assert.assertFalse(messageSelector.accept(MessageBuilder.withPayload(new DefaultMessage("{ \"foo\": { \"text\": \"barfoo\" } }")).build()));
        Assert.assertFalse(messageSelector.accept(MessageBuilder.withPayload(new DefaultMessage("{ \"bar\": { \"text\": \"foobar\" } }")).build()));
        Assert.assertFalse(messageSelector.accept(MessageBuilder.withPayload(new DefaultMessage("This is plain text!")).build()));
    }
}
