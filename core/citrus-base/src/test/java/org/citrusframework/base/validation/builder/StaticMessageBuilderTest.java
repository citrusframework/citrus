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

package org.citrusframework.base.validation.builder;

import java.util.Collections;
import java.util.Map;

import org.citrusframework.base.UnitTestSupport;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.message.MessageType;
import org.citrusframework.message.builder.DefaultHeaderBuilder;
import org.citrusframework.message.builder.DefaultHeaderDataBuilder;
import org.citrusframework.message.builder.FileResourceHeaderDataBuilder;
import org.citrusframework.validation.builder.StaticMessageBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StaticMessageBuilderTest extends UnitTestSupport {

    private StaticMessageBuilder messageBuilder;

    @Test
    public void testBuildMessageContent() {
        final Message testMessage = new DefaultMessage("TestMessage")
                .setHeader("header1", "value1");

        messageBuilder = new StaticMessageBuilder(testMessage);

        final Message message = messageBuilder.build(context, MessageType.PLAINTEXT.name());
        Assertions.assertEquals(testMessage.getPayload(), message.getPayload());
        Assertions.assertNotEquals(testMessage.getHeader(MessageHeaders.ID), message.getHeader(MessageHeaders.ID));
        Assertions.assertEquals(testMessage.getHeaders().size() + 1, message.getHeaders().size());
        Assertions.assertEquals(testMessage.getHeader("header1"), message.getHeader("header1"));
        Assertions.assertEquals(MessageType.PLAINTEXT.name(), message.getType());
    }

    @Test
    public void testBuildMessageContentWithAdditionalHeader() {
        final Message testMessage = new DefaultMessage("TestMessage")
                .setHeader("header1", "value1");

        messageBuilder = new StaticMessageBuilder(testMessage);
        messageBuilder.addHeaderBuilder(new DefaultHeaderBuilder(Collections.singletonMap("additional", "new")));

        final Message message = messageBuilder.build(context, MessageType.PLAINTEXT.name());
        Assertions.assertEquals(testMessage.getPayload(), message.getPayload());
        Assertions.assertNotEquals(testMessage.getHeader(MessageHeaders.ID), message.getHeader(MessageHeaders.ID));
        Assertions.assertNotNull(message.getHeader("additional"));
        Assertions.assertEquals("new", message.getHeader("additional"));
    }

    @Test
    public void testBuildMessageContentWithAdditionalHeaderData() {
        final Message testMessage = new DefaultMessage("TestMessage")
                .setHeader("header1", "value1");

        messageBuilder = new StaticMessageBuilder(testMessage);
        messageBuilder.addHeaderBuilder(new DefaultHeaderDataBuilder("TestMessageData"));

        final Message message = messageBuilder.build(context, MessageType.PLAINTEXT.name());
        Assertions.assertEquals(testMessage.getPayload(), message.getPayload());
        Assertions.assertNotEquals(testMessage.getHeader(MessageHeaders.ID), message.getHeader(MessageHeaders.ID));
        Assertions.assertEquals(1L, message.getHeaderData().size());
        Assertions.assertEquals("TestMessageData", message.getHeaderData().get(0));
    }

    @Test
    public void testBuildMessageContentWithMultipleHeaderData() {
        final Message testMessage = new DefaultMessage("TestMessage")
                .setHeader("header1", "value1");

        messageBuilder = new StaticMessageBuilder(testMessage);
        messageBuilder.addHeaderBuilder(new DefaultHeaderDataBuilder("TestMessageData1"));
        messageBuilder.addHeaderBuilder(new DefaultHeaderDataBuilder("TestMessageData2"));

        final Message message = messageBuilder.build(context, MessageType.PLAINTEXT.name());
        Assertions.assertEquals(testMessage.getPayload(), message.getPayload());
        Assertions.assertEquals(testMessage.getHeader("header1"), message.getHeader("header1"));
        Assertions.assertNotEquals(testMessage.getHeader(MessageHeaders.ID), message.getHeader(MessageHeaders.ID));
        Assertions.assertEquals(2L, message.getHeaderData().size());
        Assertions.assertEquals("TestMessageData1", message.getHeaderData().get(0));
        Assertions.assertEquals("TestMessageData2", message.getHeaderData().get(1));
    }

    @Test
    public void testBuildMessageContentWithAdditionalHeaderResource() {
        final Message testMessage = new DefaultMessage("TestMessage")
                .setHeader("header1", "value1");

        messageBuilder = new StaticMessageBuilder(testMessage);
        messageBuilder.addHeaderBuilder(
                new FileResourceHeaderDataBuilder("classpath:org/citrusframework/base/validation/builder/payload-data-resource.txt"));

        final Message message = messageBuilder.build(context, MessageType.PLAINTEXT.name());
        Assertions.assertEquals(testMessage.getPayload(), message.getPayload());
        Assertions.assertEquals(testMessage.getHeader("header1"), message.getHeader("header1"));
        Assertions.assertNotEquals(testMessage.getHeader(MessageHeaders.ID), message.getHeader(MessageHeaders.ID));
        Assertions.assertEquals(1L, message.getHeaderData().size());
        Assertions.assertEquals("TestMessageData", message.getHeaderData().get(0));
    }

    @Test
    public void testBuildMessageContentWithVariableSupport() {
        context.setVariable("payload", "TestMessage");
        context.setVariable("header", "value1");

        final Message testMessage = new DefaultMessage("${payload}")
                .setHeader("header1", "${header}");

        messageBuilder = new StaticMessageBuilder(testMessage);

        final Message message = messageBuilder.build(context, MessageType.PLAINTEXT.name());
        Assertions.assertEquals("TestMessage", message.getPayload());
        Assertions.assertNotEquals(testMessage.getHeader(MessageHeaders.ID), message.getHeader(MessageHeaders.ID));
        Assertions.assertEquals("value1", message.getHeader("header1"));
    }

    @Test
    public void testBuildMessageContentWithObjectPayload() {
        final Message testMessage = new DefaultMessage(1000)
                .setHeader("header1", 1000);

        messageBuilder = new StaticMessageBuilder(testMessage);

        final Message message = messageBuilder.build(context, MessageType.PLAINTEXT.name());
        Assertions.assertEquals(testMessage.getPayload(), message.getPayload());
        Assertions.assertNotEquals(testMessage.getHeader(MessageHeaders.ID), message.getHeader(MessageHeaders.ID));
        Assertions.assertEquals(testMessage.getHeader("header1"), message.getHeader("header1"));
    }

    @Test
    public void testNullValueInHeaders() {
        //GIVEN
        messageBuilder = new StaticMessageBuilder(new DefaultMessage()
                                            .setHeader("foo", "bar")
                                            .setHeader("bar", null));

        //WHEN
        final Map<String, Object> headers = messageBuilder.buildMessageHeaders(context);

        //THEN
        Assertions.assertTrue(headers.containsKey("bar"));
        Assertions.assertNull(headers.get("bar"));
        Assertions.assertTrue(headers.containsKey("foo"));
        Assertions.assertNotNull(headers.get("foo"));
        Assertions.assertEquals("bar", headers.get("foo"));
    }
}
