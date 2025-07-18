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

import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.TestCase;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.container.SequenceAfterTest;
import org.citrusframework.container.SequenceBeforeTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.dictionary.SimpleMappingDictionary;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageHeaderDataBuilder;
import org.citrusframework.message.MessagePayloadBuilder;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.messaging.SelectiveConsumer;
import org.citrusframework.report.TestActionListeners;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.Resource;
import org.citrusframework.validation.AbstractValidationProcessor;
import org.citrusframework.validation.DefaultTextEqualsMessageValidator;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.builder.StaticMessageBuilder;
import org.citrusframework.validation.context.DefaultMessageValidationContext;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.context.MessageValidationContext;
import org.citrusframework.validation.xml.XmlMessageValidationContext;
import org.citrusframework.variable.MessageHeaderVariableExtractor;
import org.citrusframework.variable.VariableExtractor;
import org.citrusframework.variable.dictionary.DataDictionary;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.dsl.MessageSupport.MessageHeaderSupport.fromHeaders;
import static org.citrusframework.dsl.MessageSupport.message;
import static org.citrusframework.message.MessageType.PLAINTEXT;
import static org.citrusframework.message.MessageType.XML;
import static org.citrusframework.validation.xml.XmlMessageValidationContext.Builder.xml;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class ReceiveMessageActionBuilderTest extends UnitTestSupport {

    @Mock
    private Endpoint messageEndpoint;
    @Mock
    private Consumer messageConsumer;
    @Mock
    private EndpointConfiguration configuration;
    @Mock
    private Resource resource;
    @Mock
    private ReferenceResolver referenceResolver;

    @BeforeMethod
    public void prepareTestContext() {
        MockitoAnnotations.openMocks(this);
        context.getMessageValidatorRegistry().addMessageValidator("default", new DefaultTextEqualsMessageValidator());
    }

    @Test
    public void testReceiveEmpty() {
        reset(messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new DefaultMessage(""));
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint));

        TestCase test = runner.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals(action.getName(), "receive");

        assertEquals(action.getMessageType(), PLAINTEXT.name());
        assertEquals(action.getEndpoint(), messageEndpoint);
        assertEquals(action.getValidationContexts().size(), 1);
        assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
    }

    @Test
    public void testReceiveBuilder() {
        reset(messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new DefaultMessage("Foo").setHeader("operation", "foo"));
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message(new DefaultMessage("Foo").setHeader("operation", "foo"))
                .type(PLAINTEXT));

        TestCase test = runner.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals(action.getName(), "receive");

        assertEquals(action.getMessageType(), PLAINTEXT.name());
        assertEquals(action.getEndpoint(), messageEndpoint);
        assertEquals(action.getValidationContexts().size(), 2);
        assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        assertTrue(action.getValidationContexts().stream().anyMatch(MessageValidationContext.class::isInstance));

        assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        assertEquals(((StaticMessageBuilder) action.getMessageBuilder()).getMessage().getPayload(), "Foo");
        assertNotNull(((StaticMessageBuilder) action.getMessageBuilder()).getMessage().getHeader("operation"));
    }

    @Test
    public void testReceiveBuilderWithPayloadBuilder() {
        MessagePayloadBuilder payloadBuilder = context -> "<TestRequest><Message>Hello Citrus!</Message></TestRequest>";

        reset(referenceResolver, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("<TestRequest><Message>Hello Citrus!</Message></TestRequest>")
                        .setHeader("operation", "foo"));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .body(payloadBuilder));

        TestCase test = runner.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals(action.getName(), "receive");

        assertEquals(action.getMessageType(), XML.name());
        assertEquals(action.getEndpoint(), messageEndpoint);
        assertEquals(action.getValidationContexts().size(), 2);
        assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        assertTrue(action.getValidationContexts().stream().anyMatch(DefaultMessageValidationContext.class::isInstance));

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()),
                "<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
    }

    @Test
    public void testReceiveBuilderWithPayloadString() {
        reset(messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .setHeader("operation", "foo"));
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .body("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        TestCase test = runner.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals(action.getName(), "receive");

        assertEquals(action.getMessageType(), XML.name());
        assertEquals(action.getEndpoint(), messageEndpoint);
        assertEquals(action.getValidationContexts().size(), 2);
        assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");

    }

    @Test
    public void testReceiveBuilderWithPayloadResource() {
        reset(resource, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .setHeader("operation", "foo"));

        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("<TestRequest><Message>Hello World!</Message></TestRequest>".getBytes()));
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .body(resource));

        TestCase test = runner.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals(action.getName(), "receive");

        assertEquals(action.getMessageType(), XML.name());
        assertEquals(action.getEndpoint(), messageEndpoint);
        assertEquals(action.getValidationContexts().size(), 2);
        assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");

    }

    @Test
    public void testReceiveBuilderWithEndpointName() {
        reset(referenceResolver, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .setHeader("operation", "foo"));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.isResolvable("fooMessageEndpoint", Endpoint.class)).thenReturn(true);
        when(referenceResolver.resolve("fooMessageEndpoint", Endpoint.class)).thenReturn(messageEndpoint);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive("fooMessageEndpoint")
                .message()
                .body("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        TestCase test = runner.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals(action.getName(), "receive");
        assertEquals(action.getEndpointUri(), "fooMessageEndpoint");
        assertEquals(action.getMessageType(), XML.name());

    }

    @Test
    public void testReceiveBuilderWithTimeout() {
        reset(messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .setHeader("operation", "foo"));
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .body("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .timeout(1000L));

        TestCase test = runner.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals(action.getName(), "receive");

        assertEquals(action.getEndpoint(), messageEndpoint);
        assertEquals(action.getReceiveTimeout(), 1000L);

    }

    @Test
    public void testReceiveBuilderWithHeaders() {
        reset(messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .setHeader("some", "value")
                        .setHeader("operation", "sayHello")
                        .setHeader("foo", "bar"));
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .body("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .headers(Collections.singletonMap("some", "value"))
                .header("operation", "sayHello")
                .header("foo", "bar"));

        runner.run(receive(messageEndpoint)
                .message()
                .header("operation", "sayHello")
                .header("foo", "bar")
                .headers(Collections.singletonMap("some", "value"))
                .body("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        TestCase test = runner.getTestCase();
        assertEquals(test.getActionCount(), 2);
        assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);
        assertEquals(test.getActions().get(1).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals(action.getName(), "receive");

        assertEquals(action.getEndpoint(), messageEndpoint);
        assertEquals(action.getMessageType(), XML.name());

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        assertTrue(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaders(context).containsKey("some"));
        assertTrue(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaders(context).containsKey("operation"));
        assertTrue(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaders(context).containsKey("foo"));

        action = ((ReceiveMessageAction) test.getActions().get(1));
        assertEquals(action.getName(), "receive");

        assertEquals(action.getEndpoint(), messageEndpoint);
        assertEquals(action.getMessageType(), XML.name());

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        assertTrue(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaders(context).containsKey("some"));
        assertTrue(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaders(context).containsKey("operation"));
        assertTrue(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaders(context).containsKey("foo"));

    }

    @Test
    public void testReceiveBuilderWithHeaderData() {
        reset(messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .setHeader("operation", "foo")
                        .addHeaderData("<Header><Name>operation</Name><Value>foo</Value></Header>"));
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .body("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .header("<Header><Name>operation</Name><Value>foo</Value></Header>"));

        runner.run(receive(messageEndpoint)
                .message(new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>"))
                .header("<Header><Name>operation</Name><Value>foo</Value></Header>"));

        TestCase test = runner.getTestCase();
        assertEquals(test.getActionCount(), 2);
        assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);
        assertEquals(test.getActions().get(1).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals(action.getName(), "receive");

        assertEquals(action.getEndpoint(), messageEndpoint);
        assertEquals(action.getMessageType(), XML.name());

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size(), 1L);
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0), "<Header><Name>operation</Name><Value>foo</Value></Header>");

        action = ((ReceiveMessageAction) test.getActions().get(1));
        assertEquals(action.getName(), "receive");

        assertEquals(action.getEndpoint(), messageEndpoint);
        assertEquals(action.getMessageType(), XML.name());

        assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        assertEquals(((StaticMessageBuilder) action.getMessageBuilder()).getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size(), 1L);
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0), "<Header><Name>operation</Name><Value>foo</Value></Header>");
    }

    @Test
    public void testReceiveBuilderWithMultipleHeaderData() {
        reset(messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .setHeader("operation", "foo")
                        .addHeaderData("<Header><Name>operation</Name><Value>foo1</Value></Header>")
                        .addHeaderData("<Header><Name>operation</Name><Value>foo2</Value></Header>"));
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .body("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .header("<Header><Name>operation</Name><Value>foo1</Value></Header>")
                .header("<Header><Name>operation</Name><Value>foo2</Value></Header>"));

        runner.run(receive(messageEndpoint)
                .message(new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>"))
                .header("<Header><Name>operation</Name><Value>foo1</Value></Header>")
                .header("<Header><Name>operation</Name><Value>foo2</Value></Header>"));

        TestCase test = runner.getTestCase();
        assertEquals(test.getActionCount(), 2);
        assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);
        assertEquals(test.getActions().get(1).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals(action.getName(), "receive");

        assertEquals(action.getEndpoint(), messageEndpoint);
        assertEquals(action.getMessageType(), XML.name());

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size(), 2L);
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0), "<Header><Name>operation</Name><Value>foo1</Value></Header>");
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(1), "<Header><Name>operation</Name><Value>foo2</Value></Header>");

        action = ((ReceiveMessageAction) test.getActions().get(1));
        assertEquals(action.getName(), "receive");

        assertEquals(action.getEndpoint(), messageEndpoint);
        assertEquals(action.getMessageType(), XML.name());

        assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        assertEquals(((StaticMessageBuilder) action.getMessageBuilder()).getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size(), 2L);
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0), "<Header><Name>operation</Name><Value>foo1</Value></Header>");
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(1), "<Header><Name>operation</Name><Value>foo2</Value></Header>");
    }

    @Test
    public void testReceiveBuilderWithHeaderDataBuilder() {
        MessageHeaderDataBuilder headerDataBuilder = context -> "<TestRequest><Message>Hello Citrus!</Message></TestRequest>";

        reset(referenceResolver, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage()
                        .addHeaderData("<TestRequest><Message>Hello Citrus!</Message></TestRequest>")
                        .setHeader("operation", "foo"));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .header(headerDataBuilder));

        TestCase test = runner.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals(action.getName(), "receive");

        assertEquals(action.getMessageType(), PLAINTEXT.name());
        assertEquals(action.getEndpoint(), messageEndpoint);
        assertEquals(action.getValidationContexts().size(), 1);
        assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size(), 1L);
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0),
                "<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
    }

    @Test
    public void testReceiveBuilderWithHeaderResource() {
        reset(resource, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong()))
                .thenReturn(new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .setHeader("operation", "foo")
                        .addHeaderData("<Header><Name>operation</Name><Value>foo</Value></Header>"))
                .thenReturn(new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .setHeader("operation", "bar")
                        .addHeaderData("<Header><Name>operation</Name><Value>bar</Value></Header>"));

        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("<Header><Name>operation</Name><Value>foo</Value></Header>".getBytes()))
                .thenReturn(new ByteArrayInputStream("<Header><Name>operation</Name><Value>bar</Value></Header>".getBytes()));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .body("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .header(resource));

        runner.run(receive(messageEndpoint)
                .message(new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>"))
                .header(resource));

        TestCase test = runner.getTestCase();
        assertEquals(test.getActionCount(), 2);
        assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);
        assertEquals(test.getActions().get(1).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals(action.getName(), "receive");

        assertEquals(action.getEndpoint(), messageEndpoint);
        assertEquals(action.getMessageType(), XML.name());

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size(), 1L);
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0), "<Header><Name>operation</Name><Value>foo</Value></Header>");

        action = ((ReceiveMessageAction) test.getActions().get(1));
        assertEquals(action.getName(), "receive");

        assertEquals(action.getEndpoint(), messageEndpoint);
        assertEquals(action.getMessageType(), XML.name());

        assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        assertEquals(((StaticMessageBuilder) action.getMessageBuilder()).getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size(), 1L);
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0), "<Header><Name>operation</Name><Value>bar</Value></Header>");

    }

    @Test
    public void testReceiveBuilderWithMultipleHeaderResource() {
        reset(resource, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .setHeader("operation", "foo")
                        .addHeaderData("<Header><Name>operation</Name><Value>sayHello</Value></Header>")
                        .addHeaderData("<Header><Name>operation</Name><Value>foo</Value></Header>")
                        .addHeaderData("<Header><Name>operation</Name><Value>bar</Value></Header>"));

        when(resource.exists()).thenReturn(true);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("<Header><Name>operation</Name><Value>foo</Value></Header>".getBytes()))
                .thenReturn(new ByteArrayInputStream("<Header><Name>operation</Name><Value>bar</Value></Header>".getBytes()))
                .thenReturn(new ByteArrayInputStream("<Header><Name>operation</Name><Value>foo</Value></Header>".getBytes()))
                .thenReturn(new ByteArrayInputStream("<Header><Name>operation</Name><Value>bar</Value></Header>".getBytes()));
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .body("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .header("<Header><Name>operation</Name><Value>sayHello</Value></Header>")
                .header(resource)
                .header(resource));

        runner.run(receive(messageEndpoint)
                .message(new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>"))
                .header("<Header><Name>operation</Name><Value>sayHello</Value></Header>")
                .header(resource)
                .header(resource));

        TestCase test = runner.getTestCase();
        assertEquals(test.getActionCount(), 2);
        assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);
        assertEquals(test.getActions().get(1).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals(action.getName(), "receive");

        assertEquals(action.getEndpoint(), messageEndpoint);
        assertEquals(action.getMessageType(), XML.name());

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size(), 3L);
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0), "<Header><Name>operation</Name><Value>sayHello</Value></Header>");
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(1), "<Header><Name>operation</Name><Value>foo</Value></Header>");
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(2), "<Header><Name>operation</Name><Value>bar</Value></Header>");

        action = ((ReceiveMessageAction) test.getActions().get(1));
        assertEquals(action.getName(), "receive");

        assertEquals(action.getEndpoint(), messageEndpoint);
        assertEquals(action.getMessageType(), XML.name());

        assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        assertEquals(((StaticMessageBuilder) action.getMessageBuilder()).getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size(), 3L);
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0), "<Header><Name>operation</Name><Value>sayHello</Value></Header>");
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(1), "<Header><Name>operation</Name><Value>foo</Value></Header>");
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(2), "<Header><Name>operation</Name><Value>bar</Value></Header>");

    }

    @Test
    public void testReceiveBuilderWithValidator() {
        reset(messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new DefaultMessage("TestMessage").setHeader("operation", "sayHello"));
        final DefaultTextEqualsMessageValidator validator = new DefaultTextEqualsMessageValidator();

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .type(PLAINTEXT)
                .body("TestMessage")
                .header("operation", "sayHello")
                .validator(validator));

        TestCase test = runner.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals(action.getName(), "receive");

        assertEquals(action.getEndpoint(), messageEndpoint);
        assertEquals(action.getMessageType(), PLAINTEXT.name());
        assertEquals(action.getValidators().size(), 1L);
        assertEquals(action.getValidators().get(0), validator);

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "TestMessage");
        assertTrue(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaders(context).containsKey("operation"));

    }

    @Test
    public void testReceiveBuilderWithValidatorName() {
        final DefaultTextEqualsMessageValidator validator = new DefaultTextEqualsMessageValidator();

        reset(referenceResolver, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new DefaultMessage("TestMessage").setHeader("operation", "sayHello"));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve("plainTextValidator")).thenReturn(validator);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .type(PLAINTEXT)
                .body("TestMessage")
                .header("operation", "sayHello")
                .validator("plainTextValidator"));

        TestCase test = runner.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals(action.getName(), "receive");

        assertEquals(action.getEndpoint(), messageEndpoint);
        assertEquals(action.getMessageType(), PLAINTEXT.name());
        assertEquals(action.getValidators().size(), 1L);
        assertEquals(action.getValidators().get(0), validator);

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "TestMessage");
        assertTrue(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaders(context).containsKey("operation"));
    }

    @Test
    public void testReceiveBuilderWithDictionary() {
        final SimpleMappingDictionary dictionary = new SimpleMappingDictionary();

        reset(referenceResolver, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new DefaultMessage("TestMessage").setHeader("operation", "sayHello"));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .type(PLAINTEXT)
                .body("TestMessage")
                .header("operation", "sayHello")
                .dictionary(dictionary));

        TestCase test = runner.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals(action.getName(), "receive");

        assertEquals(action.getEndpoint(), messageEndpoint);
        assertEquals(action.getMessageType(), PLAINTEXT.name());
        assertEquals(action.getDataDictionary(), dictionary);

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "TestMessage");
        assertTrue(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaders(context).containsKey("operation"));
    }

    @Test
    public void testReceiveBuilderWithDictionaryName() {
        final SimpleMappingDictionary dictionary = new SimpleMappingDictionary();

        reset(referenceResolver, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new DefaultMessage("TestMessage").setHeader("operation", "sayHello"));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve("customDictionary", DataDictionary.class)).thenReturn(dictionary);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .type(PLAINTEXT)
                .body("TestMessage")
                .header("operation", "sayHello")
                .dictionary("customDictionary"));

        TestCase test = runner.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals(action.getName(), "receive");

        assertEquals(action.getEndpoint(), messageEndpoint);
        assertEquals(action.getMessageType(), PLAINTEXT.name());
        assertEquals(action.getDataDictionary(), dictionary);

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "TestMessage");
        assertTrue(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaders(context).containsKey("operation"));
    }

    @Test
    public void testReceiveBuilderWithSelector() {
        SelectiveConsumer selectiveConsumer = Mockito.mock(SelectiveConsumer.class);

        reset(messageEndpoint, selectiveConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(selectiveConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(selectiveConsumer.receive(eq("operation = 'sayHello'"), any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .setHeader("operation", "sayHello"));
        final Map<String, String> messageSelector = new HashMap<>();
        messageSelector.put("operation", "sayHello");

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .body("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .selector(messageSelector));

        TestCase test = runner.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals(action.getName(), "receive");

        assertEquals(action.getMessageType(), XML.name());
        assertEquals(action.getEndpoint(), messageEndpoint);

        assertEquals(action.getMessageSelectorMap(), messageSelector);

    }

    @Test
    public void testReceiveBuilderWithSelectorExpression() {
        SelectiveConsumer selectiveConsumer = Mockito.mock(SelectiveConsumer.class);

        reset(messageEndpoint, selectiveConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(selectiveConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(selectiveConsumer.receive(eq("operation = 'sayHello'"), any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .setHeader("operation", "sayHello"));
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .body("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .selector("operation = 'sayHello'"));

        TestCase test = runner.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals(action.getName(), "receive");

        assertEquals(action.getMessageType(), XML.name());
        assertEquals(action.getEndpoint(), messageEndpoint);

        assertTrue(action.getMessageSelectorMap().isEmpty());
        assertEquals(action.getMessageSelector(), "operation = 'sayHello'");

    }

    @Test
    public void testReceiveBuilderExtractor() {
        VariableExtractor extractor = (message, context) -> context.setVariable("messageId", message.getId());

        Message received = new DefaultMessage("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                .setHeader("operation", "sayHello");
        reset(referenceResolver, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(received);

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .body("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                .process(extractor));

        assertNotNull(context.getVariable("messageId"));
        assertEquals(context.getVariable("messageId"), received.getId());

        TestCase test = runner.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals(action.getName(), "receive");

        assertEquals(action.getMessageType(), XML.name());
        assertEquals(action.getEndpoint(), messageEndpoint);

        assertEquals(action.getVariableExtractors().size(), 1);
        assertEquals(action.getVariableExtractors().get(0), extractor);
    }

    @Test
    public void testReceiveBuilderExtractFromHeader() {
        reset(messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                        .setHeader("operation", "sayHello")
                        .setHeader("requestId", "123456"));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .body("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                .extract(fromHeaders()
                        .header("operation", "operationHeader")
                        .header("requestId", "id")));

        assertNotNull(context.getVariable("operationHeader"));
        assertNotNull(context.getVariable("id"));
        assertEquals(context.getVariable("operationHeader"), "sayHello");
        assertEquals(context.getVariable("id"), "123456");

        TestCase test = runner.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals(action.getName(), "receive");

        assertEquals(action.getMessageType(), XML.name());
        assertEquals(action.getEndpoint(), messageEndpoint);

        assertEquals(action.getVariableExtractors().size(), 1);
        assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        assertTrue(((MessageHeaderVariableExtractor) action.getVariableExtractors().get(0)).getHeaderMappings().containsKey("operation"));
        assertTrue(((MessageHeaderVariableExtractor) action.getVariableExtractors().get(0)).getHeaderMappings().containsKey("requestId"));

    }

    @Test
    public void testReceiveBuilderExtractCombined() {
        VariableExtractor extractor = (message, context) -> context.setVariable("messageId", message.getId());

        Message received = new DefaultMessage("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                .setHeader("operation", "sayHello")
                .setHeader("requestId", "123456");

        reset(messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(received);

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .body("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                .extract(message()
                        .headers()
                        .header("operation", "operationHeader")
                        .header("requestId", "id"))
                .process(extractor));

        assertNotNull(context.getVariable("operationHeader"));
        assertNotNull(context.getVariable("id"));
        assertEquals(context.getVariable("operationHeader"), "sayHello");
        assertEquals(context.getVariable("id"), "123456");

        assertNotNull(context.getVariable("messageId"));
        assertEquals(context.getVariable("messageId"), received.getId());

        TestCase test = runner.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals(action.getName(), "receive");

        assertEquals(action.getMessageType(), XML.name());
        assertEquals(action.getEndpoint(), messageEndpoint);

        assertEquals(action.getVariableExtractors().size(), 2);
        assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        assertTrue(((MessageHeaderVariableExtractor) action.getVariableExtractors().get(0)).getHeaderMappings().containsKey("operation"));
        assertTrue(((MessageHeaderVariableExtractor) action.getVariableExtractors().get(0)).getHeaderMappings().containsKey("requestId"));

        assertEquals(action.getVariableExtractors().get(1), extractor);

    }

    @Test
    public void testReceiveBuilderWithValidationProcessor() {
        final AbstractValidationProcessor<?> callback = Mockito.mock(AbstractValidationProcessor.class);

        reset(callback, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new DefaultMessage("TestMessage").setHeader("operation", "sayHello"));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .type(PLAINTEXT)
                .body("TestMessage")
                .header("operation", "sayHello")
                .validate(callback));

        TestCase test = runner.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals(action.getName(), "receive");

        assertEquals(action.getEndpoint(), messageEndpoint);
        assertEquals(action.getMessageType(), PLAINTEXT.name());
        assertEquals(action.getValidationProcessor(), callback);

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "TestMessage");
        assertTrue(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaders(context).containsKey("operation"));

        verify(callback, atLeastOnce()).setReferenceResolver(context.getReferenceResolver());
        verify(callback).validate(any(Message.class), any(TestContext.class));
    }

    @Test
    public void testDeactivateSchemaValidation() {

        reset(messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("{}")
                        .setHeader("operation", "sayHello"));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .body("{}")
                .validate(xml()
                        .schemaValidation(false)));

        TestCase test = runner.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals(action.getName(), "receive");

        assertEquals(action.getEndpoint(), messageEndpoint);
        assertEquals(action.getValidationContexts().size(), 2);
        assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));

        XmlMessageValidationContext xmlMessageValidationContext = action.getValidationContexts().stream()
                .filter(XmlMessageValidationContext.class::isInstance).findFirst()
                .map(XmlMessageValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));
        assertFalse(xmlMessageValidationContext.isSchemaValidationEnabled());
    }
}
