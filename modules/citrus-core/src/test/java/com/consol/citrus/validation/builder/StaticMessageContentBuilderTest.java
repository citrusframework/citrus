/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.validation.builder;

import com.consol.citrus.message.*;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.interceptor.AbstractMessageConstructionInterceptor;
import com.consol.citrus.variable.dictionary.json.JsonMappingDataDictionary;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class StaticMessageContentBuilderTest extends AbstractTestNGUnitTest {

    private StaticMessageContentBuilder messageBuilder;

    @Test
    public void testBuildMessageContent() throws Exception {
        Message testMessage = new DefaultMessage("TestMessage")
                .setHeader("header1", "value1");

        messageBuilder = new StaticMessageContentBuilder(testMessage);

        Message message = messageBuilder.buildMessageContent(context, MessageType.PLAINTEXT.name());
        Assert.assertEquals(message, testMessage);
        Assert.assertEquals(message.getPayload(), testMessage.getPayload());
        Assert.assertEquals(message.getHeaders().get(MessageHeaders.ID), testMessage.getHeaders().get(MessageHeaders.ID));
    }

    @Test
    public void testBuildMessageContentWithAdditionalHeader() throws Exception {
        Message testMessage = new DefaultMessage("TestMessage")
                .setHeader("header1", "value1");

        messageBuilder = new StaticMessageContentBuilder(testMessage);
        messageBuilder.getMessageHeaders().put("additional", "new");

        Message message = messageBuilder.buildMessageContent(context, MessageType.PLAINTEXT.name());
        Assert.assertEquals(message.getPayload(), testMessage.getPayload());
        Assert.assertNotEquals(message.getHeaders().get(MessageHeaders.ID), testMessage.getHeaders().get(MessageHeaders.ID));
        Assert.assertTrue(message.getHeaders().containsKey("additional"));
        Assert.assertEquals(message.getHeaders().get("additional"), "new");
    }

    @Test
    public void testBuildMessageContentWithAdditionalHeaderData() throws Exception {
        Message testMessage = new DefaultMessage("TestMessage")
                .setHeader("header1", "value1");

        messageBuilder = new StaticMessageContentBuilder(testMessage);
        messageBuilder.setMessageHeaderData("TestMessageData");

        Message message = messageBuilder.buildMessageContent(context, MessageType.PLAINTEXT.name());
        Assert.assertEquals(message.getPayload(), testMessage.getPayload());
        Assert.assertNotEquals(message.getHeaders().get(MessageHeaders.ID), testMessage.getHeaders().get(MessageHeaders.ID));
        Assert.assertTrue(message.getHeaders().containsKey(MessageHeaders.HEADER_CONTENT));
        Assert.assertEquals(message.getHeaders().get(MessageHeaders.HEADER_CONTENT), "TestMessageData");
    }

    @Test
    public void testBuildMessageContentWithAdditionalHeaderResource() throws Exception {
        Message testMessage = new DefaultMessage("TestMessage")
                .setHeader("header1", "value1");

        messageBuilder = new StaticMessageContentBuilder(testMessage);
        messageBuilder.setMessageHeaderResourcePath("classpath:com/consol/citrus/validation/builder/payload-data-resource.txt");

        Message message = messageBuilder.buildMessageContent(context, MessageType.PLAINTEXT.name());
        Assert.assertEquals(message.getPayload(), testMessage.getPayload());
        Assert.assertNotEquals(message.getHeaders().get(MessageHeaders.ID), testMessage.getHeaders().get(MessageHeaders.ID));
        Assert.assertTrue(message.getHeaders().containsKey(MessageHeaders.HEADER_CONTENT));
        Assert.assertEquals(message.getHeaders().get(MessageHeaders.HEADER_CONTENT), "TestMessageData");
    }

    @Test
    public void testBuildMessageContentWithMessageInterceptor() throws Exception {
        Message testMessage = new DefaultMessage("TestMessage")
                .setHeader("header1", "value1");

        messageBuilder = new StaticMessageContentBuilder(testMessage);
        messageBuilder.getMessageInterceptors().add(new AbstractMessageConstructionInterceptor() {
            @Override
            public boolean supportsMessageType(String messageType) {
                return true;
            }
        });

        Message message = messageBuilder.buildMessageContent(context, MessageType.PLAINTEXT.name());
        Assert.assertEquals(message.getPayload(), testMessage.getPayload());
        Assert.assertNotEquals(message.getHeaders().get(MessageHeaders.ID), testMessage.getHeaders().get(MessageHeaders.ID));
    }

    @Test
    public void testBuildMessageContentWithDataDictionary() throws Exception {
        Message testMessage = new DefaultMessage("TestMessage")
                .setHeader("header1", "value1");

        messageBuilder = new StaticMessageContentBuilder(testMessage);
        messageBuilder.setDataDictionary(new JsonMappingDataDictionary());

        Message message = messageBuilder.buildMessageContent(context, MessageType.PLAINTEXT.name());
        Assert.assertEquals(message.getPayload(), testMessage.getPayload());
        Assert.assertNotEquals(message.getHeaders().get(MessageHeaders.ID), testMessage.getHeaders().get(MessageHeaders.ID));
    }
}
