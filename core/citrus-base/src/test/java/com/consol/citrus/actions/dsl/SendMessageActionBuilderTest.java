/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.actions.dsl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

import com.consol.citrus.DefaultTestCaseRunner;
import com.consol.citrus.TestCase;
import com.consol.citrus.UnitTestSupport;
import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.container.SequenceAfterTest;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.dictionary.SimpleMappingDictionary;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageHeaders;
import com.consol.citrus.message.MessagePayloadBuilder;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.messaging.Producer;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.builder.DefaultMessageBuilder;
import com.consol.citrus.validation.builder.StaticMessageBuilder;
import com.consol.citrus.variable.MessageHeaderVariableExtractor;
import com.consol.citrus.variable.VariableExtractor;
import com.consol.citrus.variable.dictionary.DataDictionary;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.consol.citrus.actions.SendMessageAction.Builder.send;
import static com.consol.citrus.dsl.MessageSupport.message;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class SendMessageActionBuilderTest extends UnitTestSupport {

    private ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);
    private Endpoint messageEndpoint = Mockito.mock(Endpoint.class);
    private Producer messageProducer = Mockito.mock(Producer.class);
    private Resource resource = Mockito.mock(Resource.class);

    @Mock
    private MessageValidator<?> validator;

    @Override
    protected TestContextFactory createTestContextFactory() {
        MockitoAnnotations.openMocks(this);
        when(validator.supportsMessageType(any(String.class), any(Message.class))).thenReturn(true);

        TestContextFactory factory = super.createTestContextFactory();
        factory.getMessageValidatorRegistry().addMessageValidator("validator", validator);

        return factory;
    }

    @Test
    public void testSendBuilderWithMessageInstance() {
        reset(messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "Foo");
            Assert.assertNotNull(message.getHeader("operation"));
            Assert.assertEquals(message.getHeader("operation"), "foo");
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint)
                        .message(new DefaultMessage("Foo").setHeader("operation", "foo"))
                            .header("additional", "additionalValue"));

        final TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageBuilder.class);

        final StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "Foo");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("operation"), "foo");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("additional"), "additionalValue");

    }

    @Test
    public void testSendBuilderWithObjectMessageInstance() {
        reset(messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(Integer.class), new Integer(10));
            Assert.assertNotNull(message.getHeader("operation"));
            Assert.assertEquals(message.getHeader("operation"), "foo");
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));
        final Message message = new DefaultMessage(10).setHeader("operation", "foo");
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint)
                        .message(message));

        final TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageBuilder.class);

        final StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), message.getPayload());
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 1L);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("operation"), "foo");
        Assert.assertEquals(messageBuilder.getMessage().getHeader(MessageHeaders.ID), message.getHeader(MessageHeaders.ID));
        Assert.assertEquals(messageBuilder.getMessage().getHeader("operation"), "foo");

        final Message constructed = messageBuilder.build(new TestContext(), MessageType.PLAINTEXT.name());
        Assert.assertEquals(constructed.getHeaders().size(), message.getHeaders().size() + 1);
        Assert.assertEquals(constructed.getHeader("operation"), "foo");
        Assert.assertNotEquals(constructed.getHeader(MessageHeaders.ID), message.getHeader(MessageHeaders.ID));

    }

    @Test
    public void testSendBuilderWithObjectMessageInstanceAdditionalHeader() {
        reset(messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(Integer.class), new Integer(10));
            Assert.assertNotNull(message.getHeader("operation"));
            Assert.assertEquals(message.getHeader("operation"), "foo");
            Assert.assertNotNull(message.getHeader("additional"));
            Assert.assertEquals(message.getHeader("additional"), "new");
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));
        final Message message = new DefaultMessage(10).setHeader("operation", "foo");
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint)
                        .message(message)
                        .header("additional", "new"));

        final TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageBuilder.class);

        final StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), 10);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 2L);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("additional"), "new");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("operation"), "foo");
        Assert.assertEquals(messageBuilder.getMessage().getHeader(MessageHeaders.ID), message.getHeader(MessageHeaders.ID));
        Assert.assertEquals(messageBuilder.getMessage().getHeader("operation"), "foo");

        final Message constructed = messageBuilder.build(new TestContext(), MessageType.PLAINTEXT.name());
        Assert.assertEquals(constructed.getHeaders().size(), message.getHeaders().size() + 2);
        Assert.assertEquals(constructed.getHeader("operation"), "foo");
        Assert.assertEquals(constructed.getHeader("additional"), "new");

    }

    @Test
    public void testSendBuilderWithPayloadBuilder() {
        MessagePayloadBuilder payloadBuilder = context -> "<TestRequest><Message>Hello Citrus!</Message></TestRequest>";

        reset(referenceResolver, messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint)
                        .message()
                        .body(payloadBuilder));

        final TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), DefaultMessageBuilder.class);

        final DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0L);

    }

    @Test
    public void testSendBuilderWithPayloadData() {
        reset(messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint)
                        .message()
                        .body("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        final TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), DefaultMessageBuilder.class);

        final DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0L);

    }

    @Test
    public void testSendBuilderWithPayloadResource() throws IOException {
        reset(resource, messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("<TestRequest><Message>Hello World!</Message></TestRequest>".getBytes()));
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint)
                        .message()
                        .body(resource));


        final TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), DefaultMessageBuilder.class);

        final DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0L);

    }

    @Test
    public void testSendBuilderWithEndpointName() {
        reset(referenceResolver, messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve("fooMessageEndpoint", Endpoint.class)).thenReturn(messageEndpoint);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send("fooMessageEndpoint")
                        .message()
                        .body("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        final TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");
        Assert.assertEquals(action.getEndpointUri(), "fooMessageEndpoint");

    }

    @Test
    public void testSendBuilderWithHeaders() {
        reset(messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
            Assert.assertNotNull(message.getHeader("operation"));
            Assert.assertEquals(message.getHeader("operation"), "foo");
            Assert.assertNotNull(message.getHeader("language"));
            Assert.assertEquals(message.getHeader("language"), "eng");
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint)
                        .message()
                        .body("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .headers(Collections.singletonMap("some", "value"))
                        .header("operation", "foo")
                        .header("language", "eng"));

        final TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), DefaultMessageBuilder.class);

        final DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 3L);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("some"), "value");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("operation"), "foo");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("language"), "eng");

    }

    @Test
    public void testSendBuilderWithHeaderData() {
        reset(messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
            Assert.assertNotNull(message.getHeaderData());
            Assert.assertEquals(message.getHeaderData().size(), 1L);
            Assert.assertEquals(message.getHeaderData().get(0), "<Header><Name>operation</Name><Value>foo</Value></Header>");
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint)
                        .message()
                        .body("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .header("<Header><Name>operation</Name><Value>foo</Value></Header>"));

        runner.run(send(messageEndpoint)
                    .message(new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>"))
                    .header("<Header><Name>operation</Name><Value>foo</Value></Header>"));

        final TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), SendMessageAction.class);

        SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), DefaultMessageBuilder.class);

        final DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0L);
        Assert.assertEquals(messageBuilder.buildMessageHeaderData(context).size(), 1L);
        Assert.assertEquals(messageBuilder.buildMessageHeaderData(context).get(0), "<Header><Name>operation</Name><Value>foo</Value></Header>");

        action = ((SendMessageAction)test.getActions().get(1));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageBuilder.class);

        final StaticMessageBuilder staticMessageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(staticMessageBuilder.getMessage().getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(staticMessageBuilder.buildMessageHeaders(context).size(), 0L);
        Assert.assertEquals(staticMessageBuilder.buildMessageHeaderData(context).size(), 1L);
        Assert.assertEquals(staticMessageBuilder.buildMessageHeaderData(context).get(0), "<Header><Name>operation</Name><Value>foo</Value></Header>");

    }

    @Test
    public void testSendBuilderWithMultipleHeaderData() {
        reset(messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
            Assert.assertNotNull(message.getHeaderData());
            Assert.assertEquals(message.getHeaderData().size(), 2L);
            Assert.assertEquals(message.getHeaderData().get(0), "<Header><Name>operation</Name><Value>foo1</Value></Header>");
            Assert.assertEquals(message.getHeaderData().get(1), "<Header><Name>operation</Name><Value>foo2</Value></Header>");
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint)
                        .message()
                        .body("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .header("<Header><Name>operation</Name><Value>foo1</Value></Header>")
                        .header("<Header><Name>operation</Name><Value>foo2</Value></Header>"));

        runner.run(send(messageEndpoint)
                    .message(new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>"))
                    .header("<Header><Name>operation</Name><Value>foo1</Value></Header>")
                    .header("<Header><Name>operation</Name><Value>foo2</Value></Header>"));

        final TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), SendMessageAction.class);

        SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), DefaultMessageBuilder.class);

        final DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0L);
        Assert.assertEquals(messageBuilder.buildMessageHeaderData(context).size(), 2L);
        Assert.assertEquals(messageBuilder.buildMessageHeaderData(context).get(0), "<Header><Name>operation</Name><Value>foo1</Value></Header>");
        Assert.assertEquals(messageBuilder.buildMessageHeaderData(context).get(1), "<Header><Name>operation</Name><Value>foo2</Value></Header>");

        action = ((SendMessageAction)test.getActions().get(1));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageBuilder.class);

        final StaticMessageBuilder staticMessageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(staticMessageBuilder.getMessage().getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(staticMessageBuilder.buildMessageHeaders(context).size(), 0L);
        Assert.assertEquals(staticMessageBuilder.buildMessageHeaderData(context).size(), 2L);
        Assert.assertEquals(staticMessageBuilder.buildMessageHeaderData(context).get(0), "<Header><Name>operation</Name><Value>foo1</Value></Header>");
        Assert.assertEquals(staticMessageBuilder.buildMessageHeaderData(context).get(1), "<Header><Name>operation</Name><Value>foo2</Value></Header>");

    }

    @Test
    public void testSendBuilderWithHeaderDataResource() throws IOException {
        reset(resource, messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
            Assert.assertNotNull(message.getHeaderData());
            Assert.assertEquals(message.getHeaderData().size(), 1L);
            Assert.assertEquals(message.getHeaderData().get(0), "<Header><Name>operation</Name><Value>foo1</Value></Header>");
            return null;
        }).doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
            Assert.assertNotNull(message.getHeaderData());
            Assert.assertEquals(message.getHeaderData().size(), 1L);
            Assert.assertEquals(message.getHeaderData().get(0), "<Header><Name>operation</Name><Value>foo2</Value></Header>");
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("<Header><Name>operation</Name><Value>foo1</Value></Header>".getBytes()))
                                       .thenReturn(new ByteArrayInputStream("<Header><Name>operation</Name><Value>foo2</Value></Header>".getBytes()));
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint)
                        .message()
                        .body("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .header(resource));

        runner.run(send(messageEndpoint)
                        .message(new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>"))
                        .header(resource));

        final TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), SendMessageAction.class);

        SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), DefaultMessageBuilder.class);

        final DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0L);
        Assert.assertEquals(messageBuilder.buildMessageHeaderData(context).size(), 1L);
        Assert.assertEquals(messageBuilder.buildMessageHeaderData(context).get(0), "<Header><Name>operation</Name><Value>foo1</Value></Header>");

        action = ((SendMessageAction)test.getActions().get(1));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageBuilder.class);

        final StaticMessageBuilder staticMessageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(staticMessageBuilder.getMessage().getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(staticMessageBuilder.buildMessageHeaders(context).size(), 0L);
        Assert.assertEquals(staticMessageBuilder.buildMessageHeaderData(context).size(), 1L);
        Assert.assertEquals(staticMessageBuilder.buildMessageHeaderData(context).get(0), "<Header><Name>operation</Name><Value>foo2</Value></Header>");

    }

    @Test
    public void testSendBuilderExtractFromPayload() {
        VariableExtractor extractor = (message, context) -> context.setVariable("messageId", message.getId());

        reset(messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>");
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint)
                        .message()
                        .body("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                        .process(extractor));

        Assert.assertNotNull(context.getVariable("messageId"));

        final TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertEquals(action.getVariableExtractors().size(), 1);
        Assert.assertEquals(action.getVariableExtractors().get(0), extractor);

    }

    @Test
    public void testSendBuilderExtractFromHeader() {
        reset(messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>");
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint)
                        .message()
                        .body("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                        .header("operation", "sayHello")
                        .header("requestId", "123456")
                        .extract(message()
                                .headers()
                                .header("operation", "operationHeader")
                                .header("requestId", "id")));

        Assert.assertNotNull(context.getVariable("operationHeader"));
        Assert.assertNotNull(context.getVariable("id"));
        Assert.assertEquals(context.getVariable("operationHeader"), "sayHello");
        Assert.assertEquals(context.getVariable("id"), "123456");

        final TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertEquals(action.getVariableExtractors().size(), 1);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        Assert.assertTrue(((MessageHeaderVariableExtractor) action.getVariableExtractors().get(0)).getHeaderMappings().containsKey("operation"));
        Assert.assertTrue(((MessageHeaderVariableExtractor) action.getVariableExtractors().get(0)).getHeaderMappings().containsKey("requestId"));

    }

    @Test
    public void testSendBuilderWithDictionary() {
        final SimpleMappingDictionary dictionary =
                new SimpleMappingDictionary(Collections.singletonMap("\\?", "Hello World!"));

        reset(messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(StringUtils.trimAllWhitespace(message.getPayload(String.class)),
                    "{\"TestRequest\":{\"Message\":\"HelloWorld!\"}}");
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint)
                        .message()
                        .type(MessageType.JSON)
                        .body("{ \"TestRequest\": { \"Message\": \"?\" }}")
                        .dictionary(dictionary));

        final TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getDataDictionary(), dictionary);
    }

    @Test
    public void testSendBuilderWithDictionaryName() {
        final SimpleMappingDictionary dictionary =
                new SimpleMappingDictionary(Collections.singletonMap("\\?", "Hello World!"));

        reset(referenceResolver, messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(StringUtils.trimAllWhitespace(message.getPayload(String.class)),
                    "{\"TestRequest\":{\"Message\":\"HelloWorld!\"}}");
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolve("customDictionary", DataDictionary.class)).thenReturn(dictionary);

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint)
                        .message()
                        .type(MessageType.JSON)
                        .body("{ \"TestRequest\": { \"Message\": \"?\" }}")
                        .dictionary("customDictionary"));

        final TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getDataDictionary(), dictionary);
    }

}
