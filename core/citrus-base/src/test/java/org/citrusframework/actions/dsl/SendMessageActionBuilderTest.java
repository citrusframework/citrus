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

package org.citrusframework.actions.dsl;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.HashMap;

import org.citrusframework.base.DefaultTestCaseRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.TestCase;
import org.citrusframework.base.UnitTestSupport;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.container.SequenceAfterTest;
import org.citrusframework.container.SequenceBeforeTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.dictionary.SimpleMappingDictionary;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageHeaders;
import org.citrusframework.message.MessagePayloadBuilder;
import org.citrusframework.message.MessageType;
import org.citrusframework.messaging.Producer;
import org.citrusframework.report.TestActionListeners;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.Resource;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.builder.StaticMessageBuilder;
import org.citrusframework.variable.MessageHeaderVariableExtractor;
import org.citrusframework.variable.VariableExtractor;
import org.citrusframework.variable.dictionary.DataDictionary;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SendMessageActionBuilderTest extends UnitTestSupport implements TestActionSupport {

    private final ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);
    private final Endpoint messageEndpoint = Mockito.mock(Endpoint.class);
    private final Producer messageProducer = Mockito.mock(Producer.class);
    private final Resource resource = Mockito.mock(Resource.class);

    @Mock
    private MessageValidator<?> validator;

    @Override
    protected TestContextFactory createTestContextFactory() {
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
            Assertions.assertEquals("Foo", message.getPayload(String.class));
            Assertions.assertNotNull(message.getHeader("operation"));
            Assertions.assertEquals("foo", message.getHeader("operation"));
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint)
                        .message(new DefaultMessage("Foo").setHeader("operation", "foo"))
                            .header("additional", "additionalValue"));

        final TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendMessageAction.class, test.getActions().get(0).getClass());

        final SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assertions.assertEquals("send", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(StaticMessageBuilder.class, action.getMessageBuilder().getClass());

        final StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals("Foo", messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals("foo", messageBuilder.buildMessageHeaders(context).get("operation"));
        Assertions.assertEquals("additionalValue", messageBuilder.buildMessageHeaders(context).get("additional"));

    }

    @Test
    public void testSendBuilderWithObjectMessageInstance() {
        reset(messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assertions.assertEquals(Integer.valueOf(10), message.getPayload(Integer.class));
            Assertions.assertNotNull(message.getHeader("operation"));
            Assertions.assertEquals("foo", message.getHeader("operation"));
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));
        final Message message = new DefaultMessage(10).setHeader("operation", "foo");
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint)
                        .message(message));

        final TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendMessageAction.class, test.getActions().get(0).getClass());

        final SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assertions.assertEquals("send", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(StaticMessageBuilder.class, action.getMessageBuilder().getClass());

        final StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals(message.getPayload(), messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(1L, messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals("foo", messageBuilder.buildMessageHeaders(context).get("operation"));
        Assertions.assertEquals(message.getHeader(MessageHeaders.ID), messageBuilder.getMessage().getHeader(MessageHeaders.ID));
        Assertions.assertEquals("foo", messageBuilder.getMessage().getHeader("operation"));

        final Message constructed = messageBuilder.build(new TestContext(), MessageType.PLAINTEXT.name());
        Assertions.assertEquals(message.getHeaders().size() + 1, constructed.getHeaders().size());
        Assertions.assertEquals("foo", constructed.getHeader("operation"));
        Assertions.assertNotEquals(message.getHeader(MessageHeaders.ID), constructed.getHeader(MessageHeaders.ID));

    }

    @Test
    public void testSendBuilderWithObjectMessageInstanceAdditionalHeader() {
        reset(messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assertions.assertEquals(Integer.valueOf(10), message.getPayload(Integer.class));
            Assertions.assertNotNull(message.getHeader("operation"));
            Assertions.assertEquals("foo", message.getHeader("operation"));
            Assertions.assertNotNull(message.getHeader("additional"));
            Assertions.assertEquals("new", message.getHeader("additional"));
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));
        final Message message = new DefaultMessage(10).setHeader("operation", "foo");
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint)
                        .message(message)
                        .header("additional", "new"));

        final TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendMessageAction.class, test.getActions().get(0).getClass());

        final SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assertions.assertEquals("send", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(StaticMessageBuilder.class, action.getMessageBuilder().getClass());

        final StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals(10, messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(2L, messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals("new", messageBuilder.buildMessageHeaders(context).get("additional"));
        Assertions.assertEquals("foo", messageBuilder.buildMessageHeaders(context).get("operation"));
        Assertions.assertEquals(message.getHeader(MessageHeaders.ID), messageBuilder.getMessage().getHeader(MessageHeaders.ID));
        Assertions.assertEquals("foo", messageBuilder.getMessage().getHeader("operation"));

        final Message constructed = messageBuilder.build(new TestContext(), MessageType.PLAINTEXT.name());
        Assertions.assertEquals(message.getHeaders().size() + 2, constructed.getHeaders().size());
        Assertions.assertEquals("foo", constructed.getHeader("operation"));
        Assertions.assertEquals("new", constructed.getHeader("additional"));

    }

    @Test
    public void testSendBuilderWithPayloadBuilder() {
        MessagePayloadBuilder payloadBuilder = context -> "<TestRequest><Message>Hello Citrus!</Message></TestRequest>";

        reset(referenceResolver, messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assertions.assertEquals("<TestRequest><Message>Hello Citrus!</Message></TestRequest>", message.getPayload(String.class));
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
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendMessageAction.class, test.getActions().get(0).getClass());

        final SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assertions.assertEquals("send", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(DefaultMessageBuilder.class, action.getMessageBuilder().getClass());

        final DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals("<TestRequest><Message>Hello Citrus!</Message></TestRequest>", messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(0L, messageBuilder.buildMessageHeaders(context).size());

    }

    @Test
    public void testSendBuilderWithPayloadData() {
        reset(messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", message.getPayload(String.class));
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint)
                        .message()
                        .body("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        final TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendMessageAction.class, test.getActions().get(0).getClass());

        final SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assertions.assertEquals("send", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(DefaultMessageBuilder.class, action.getMessageBuilder().getClass());

        final DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(0L, messageBuilder.buildMessageHeaders(context).size());

    }

    @Test
    public void testSendBuilderWithPayloadResource() {
        reset(resource, messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", message.getPayload(String.class));
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenAnswer((invocation) -> new ByteArrayInputStream("<TestRequest><Message>Hello World!</Message></TestRequest>".getBytes()));
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint)
                        .message()
                        .body(resource));

        final TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendMessageAction.class, test.getActions().get(0).getClass());

        final SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assertions.assertEquals("send", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(DefaultMessageBuilder.class, action.getMessageBuilder().getClass());

        final DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(0L, messageBuilder.buildMessageHeaders(context).size());

    }

    @Test
    public void testSendBuilderWithEndpointName() {
        reset(referenceResolver, messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", message.getPayload(String.class));
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.isResolvable("fooMessageEndpoint", Endpoint.class)).thenReturn(true);
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
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendMessageAction.class, test.getActions().get(0).getClass());

        final SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assertions.assertEquals("send", action.getName());
        Assertions.assertEquals("fooMessageEndpoint", action.getEndpointUri());

    }

    @Test
    public void testSendBuilderWithHeaders() {
        reset(messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", message.getPayload(String.class));
            Assertions.assertNotNull(message.getHeader("operation"));
            Assertions.assertEquals("foo", message.getHeader("operation"));
            Assertions.assertNotNull(message.getHeader("language"));
            Assertions.assertEquals("eng", message.getHeader("language"));
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
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendMessageAction.class, test.getActions().get(0).getClass());

        final SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assertions.assertEquals("send", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(DefaultMessageBuilder.class, action.getMessageBuilder().getClass());

        final DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(3L, messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals("value", messageBuilder.buildMessageHeaders(context).get("some"));
        Assertions.assertEquals("foo", messageBuilder.buildMessageHeaders(context).get("operation"));
        Assertions.assertEquals("eng", messageBuilder.buildMessageHeaders(context).get("language"));

    }

    @Test
    public void testSendBuilderWithHeaderData() {
        reset(messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", message.getPayload(String.class));
            Assertions.assertNotNull(message.getHeaderData());
            Assertions.assertEquals(1L, message.getHeaderData().size());
            Assertions.assertEquals("<Header><Name>operation</Name><Value>foo</Value></Header>", message.getHeaderData().get(0));
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
        Assertions.assertEquals(2, test.getActionCount());
        Assertions.assertEquals(SendMessageAction.class, test.getActions().get(0).getClass());
        Assertions.assertEquals(SendMessageAction.class, test.getActions().get(1).getClass());

        SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assertions.assertEquals("send", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(DefaultMessageBuilder.class, action.getMessageBuilder().getClass());

        final DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(0L, messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals(1L, messageBuilder.buildMessageHeaderData(context).size());
        Assertions.assertEquals("<Header><Name>operation</Name><Value>foo</Value></Header>", messageBuilder.buildMessageHeaderData(context).get(0));

        action = ((SendMessageAction)test.getActions().get(1));
        Assertions.assertEquals("send", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(StaticMessageBuilder.class, action.getMessageBuilder().getClass());

        final StaticMessageBuilder staticMessageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", staticMessageBuilder.getMessage().getPayload(String.class));
        Assertions.assertEquals(0L, staticMessageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals(1L, staticMessageBuilder.buildMessageHeaderData(context).size());
        Assertions.assertEquals("<Header><Name>operation</Name><Value>foo</Value></Header>", staticMessageBuilder.buildMessageHeaderData(context).get(0));

    }

    @Test
    public void testSendBuilderWithMultipleHeaderData() {
        reset(messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", message.getPayload(String.class));
            Assertions.assertNotNull(message.getHeaderData());
            Assertions.assertEquals(2L, message.getHeaderData().size());
            Assertions.assertEquals("<Header><Name>operation</Name><Value>foo1</Value></Header>", message.getHeaderData().get(0));
            Assertions.assertEquals("<Header><Name>operation</Name><Value>foo2</Value></Header>", message.getHeaderData().get(1));
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
        Assertions.assertEquals(2, test.getActionCount());
        Assertions.assertEquals(SendMessageAction.class, test.getActions().get(0).getClass());
        Assertions.assertEquals(SendMessageAction.class, test.getActions().get(1).getClass());

        SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assertions.assertEquals("send", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(DefaultMessageBuilder.class, action.getMessageBuilder().getClass());

        final DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(0L, messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals(2L, messageBuilder.buildMessageHeaderData(context).size());
        Assertions.assertEquals("<Header><Name>operation</Name><Value>foo1</Value></Header>", messageBuilder.buildMessageHeaderData(context).get(0));
        Assertions.assertEquals("<Header><Name>operation</Name><Value>foo2</Value></Header>", messageBuilder.buildMessageHeaderData(context).get(1));

        action = ((SendMessageAction)test.getActions().get(1));
        Assertions.assertEquals("send", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(StaticMessageBuilder.class, action.getMessageBuilder().getClass());

        final StaticMessageBuilder staticMessageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", staticMessageBuilder.getMessage().getPayload(String.class));
        Assertions.assertEquals(0L, staticMessageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals(2L, staticMessageBuilder.buildMessageHeaderData(context).size());
        Assertions.assertEquals("<Header><Name>operation</Name><Value>foo1</Value></Header>", staticMessageBuilder.buildMessageHeaderData(context).get(0));
        Assertions.assertEquals("<Header><Name>operation</Name><Value>foo2</Value></Header>", staticMessageBuilder.buildMessageHeaderData(context).get(1));

    }

    @Test
    public void testSendBuilderWithHeaderDataResource() {
        reset(resource, messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", message.getPayload(String.class));
            Assertions.assertNotNull(message.getHeaderData());
            Assertions.assertEquals(1L, message.getHeaderData().size());
            Assertions.assertEquals("<Header><Name>operation</Name><Value>foo1</Value></Header>", message.getHeaderData().get(0));
            return null;
        }).doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", message.getPayload(String.class));
            Assertions.assertNotNull(message.getHeaderData());
            Assertions.assertEquals(1L, message.getHeaderData().size());
            Assertions.assertEquals("<Header><Name>operation</Name><Value>foo2</Value></Header>", message.getHeaderData().get(0));
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        when(resource.exists()).thenReturn(true);
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
        Assertions.assertEquals(2, test.getActionCount());
        Assertions.assertEquals(SendMessageAction.class, test.getActions().get(0).getClass());
        Assertions.assertEquals(SendMessageAction.class, test.getActions().get(1).getClass());

        SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assertions.assertEquals("send", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(DefaultMessageBuilder.class, action.getMessageBuilder().getClass());

        final DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(0L, messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals(1L, messageBuilder.buildMessageHeaderData(context).size());
        Assertions.assertEquals("<Header><Name>operation</Name><Value>foo1</Value></Header>", messageBuilder.buildMessageHeaderData(context).get(0));

        action = ((SendMessageAction)test.getActions().get(1));
        Assertions.assertEquals("send", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(StaticMessageBuilder.class, action.getMessageBuilder().getClass());

        final StaticMessageBuilder staticMessageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", staticMessageBuilder.getMessage().getPayload(String.class));
        Assertions.assertEquals(0L, staticMessageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals(1L, staticMessageBuilder.buildMessageHeaderData(context).size());
        Assertions.assertEquals("<Header><Name>operation</Name><Value>foo2</Value></Header>", staticMessageBuilder.buildMessageHeaderData(context).get(0));

    }

    @Test
    public void testSendBuilderExtractFromPayload() {
        VariableExtractor extractor = (message, context) -> context.setVariable("messageId", message.getId());

        reset(messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assertions.assertEquals("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>", message.getPayload(String.class));
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint)
                        .message()
                        .body("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                        .process(extractor));

        Assertions.assertNotNull(context.getVariable("messageId"));

        final TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendMessageAction.class, test.getActions().get(0).getClass());

        final SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assertions.assertEquals("send", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());

        Assertions.assertEquals(1, action.getVariableExtractors().size());
        Assertions.assertEquals(extractor, action.getVariableExtractors().get(0));

    }

    @Test
    public void testSendBuilderExtractFromHeader() {
        reset(messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assertions.assertEquals("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>", message.getPayload(String.class));
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint)
                        .message()
                        .body("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                        .header("operation", "sayHello")
                        .header("requestId", "123456")
                        .extract(extractor().message()
                                .headers()
                                .header("operation", "operationHeader")
                                .header("requestId", "id")));

        Assertions.assertNotNull(context.getVariable("operationHeader"));
        Assertions.assertNotNull(context.getVariable("id"));
        Assertions.assertEquals("sayHello", context.getVariable("operationHeader"));
        Assertions.assertEquals("123456", context.getVariable("id"));

        final TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendMessageAction.class, test.getActions().get(0).getClass());

        final SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assertions.assertEquals("send", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());

        Assertions.assertEquals(1, action.getVariableExtractors().size());
        Assertions.assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        Assertions.assertTrue(((MessageHeaderVariableExtractor) action.getVariableExtractors().get(0)).getHeaderMappings().containsKey("operation"));
        Assertions.assertTrue(((MessageHeaderVariableExtractor) action.getVariableExtractors().get(0)).getHeaderMappings().containsKey("requestId"));

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
            Assertions.assertEquals("{\"TestRequest\":{\"Message\":\"HelloWorld!\"}}",
                    message.getPayload(String.class).replaceAll("\\s", ""));
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint)
                        .message()
                        .type(MessageType.JSON)
                        .body("{ \"TestRequest\": { \"Message\": \"?\" }}")
                        .dictionary(dictionary));

        final TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendMessageAction.class, test.getActions().get(0).getClass());

        final SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assertions.assertEquals("send", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(dictionary, action.getDataDictionary());
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
            Assertions.assertEquals("{\"TestRequest\":{\"Message\":\"HelloWorld!\"}}",
                    message.getPayload(String.class).replaceAll("\\s", ""));
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
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendMessageAction.class, test.getActions().get(0).getClass());

        final SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assertions.assertEquals("send", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(dictionary, action.getDataDictionary());
    }
}
