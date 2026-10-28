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
import java.util.Map;

import org.citrusframework.base.DefaultTestCaseRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.TestCase;
import org.citrusframework.base.UnitTestSupport;
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
import org.citrusframework.validation.context.xml.XmlMessageValidationContext;
import org.citrusframework.variable.MessageHeaderVariableExtractor;
import org.citrusframework.variable.VariableExtractor;
import org.citrusframework.variable.dictionary.DataDictionary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.citrusframework.message.MessageType.PLAINTEXT;
import static org.citrusframework.message.MessageType.XML;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ReceiveMessageActionBuilderTest extends UnitTestSupport implements TestActionSupport {

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

    @BeforeEach
    public void prepareTestContext() {
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
        assertEquals(1, test.getActionCount());
        assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals("receive", action.getName());

        assertEquals(PLAINTEXT.name(), action.getMessageType());
        assertEquals(messageEndpoint, action.getEndpoint());
        assertEquals(1, action.getValidationContexts().size());
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
        assertEquals(1, test.getActionCount());
        assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals("receive", action.getName());

        assertEquals(PLAINTEXT.name(), action.getMessageType());
        assertEquals(messageEndpoint, action.getEndpoint());
        assertEquals(2, action.getValidationContexts().size());
        assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        assertTrue(action.getValidationContexts().stream().anyMatch(MessageValidationContext.class::isInstance));

        assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        assertEquals("Foo", ((StaticMessageBuilder) action.getMessageBuilder()).getMessage().getPayload());
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
        assertEquals(1, test.getActionCount());
        assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals("receive", action.getName());

        assertEquals(XML.name(), action.getMessageType());
        assertEquals(messageEndpoint, action.getEndpoint());
        assertEquals(2, action.getValidationContexts().size());
        assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        assertTrue(action.getValidationContexts().stream().anyMatch(DefaultMessageValidationContext.class::isInstance));

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        assertEquals("<TestRequest><Message>Hello Citrus!</Message></TestRequest>",
                ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));
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
        assertEquals(1, test.getActionCount());
        assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals("receive", action.getName());

        assertEquals(XML.name(), action.getMessageType());
        assertEquals(messageEndpoint, action.getEndpoint());
        assertEquals(2, action.getValidationContexts().size());
        assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));

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
        when(resource.location()).thenReturn("message.xml");
        when(resource.getInputStream()).thenAnswer((invocation) -> new ByteArrayInputStream("<TestRequest><Message>Hello World!</Message></TestRequest>".getBytes()));
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .body(resource));

        TestCase test = runner.getTestCase();
        assertEquals(1, test.getActionCount());
        assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals("receive", action.getName());

        assertEquals(XML.name(), action.getMessageType());
        assertEquals(messageEndpoint, action.getEndpoint());
        assertEquals(2, action.getValidationContexts().size());
        assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));

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
        assertEquals(1, test.getActionCount());
        assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals("receive", action.getName());
        assertEquals("fooMessageEndpoint", action.getEndpointUri());
        assertEquals(XML.name(), action.getMessageType());

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
        assertEquals(1, test.getActionCount());
        assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals("receive", action.getName());

        assertEquals(messageEndpoint, action.getEndpoint());
        assertEquals(1000L, action.getReceiveTimeout());

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
        assertEquals(2, test.getActionCount());
        assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());
        assertEquals(ReceiveMessageAction.class, test.getActions().get(1).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals("receive", action.getName());

        assertEquals(messageEndpoint, action.getEndpoint());
        assertEquals(XML.name(), action.getMessageType());

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));
        assertTrue(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaders(context).containsKey("some"));
        assertTrue(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaders(context).containsKey("operation"));
        assertTrue(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaders(context).containsKey("foo"));

        action = ((ReceiveMessageAction) test.getActions().get(1));
        assertEquals("receive", action.getName());

        assertEquals(messageEndpoint, action.getEndpoint());
        assertEquals(XML.name(), action.getMessageType());

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));
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
        assertEquals(2, test.getActionCount());
        assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());
        assertEquals(ReceiveMessageAction.class, test.getActions().get(1).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals("receive", action.getName());

        assertEquals(messageEndpoint, action.getEndpoint());
        assertEquals(XML.name(), action.getMessageType());

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));
        assertEquals(1L, ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size());
        assertEquals("<Header><Name>operation</Name><Value>foo</Value></Header>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0));

        action = ((ReceiveMessageAction) test.getActions().get(1));
        assertEquals("receive", action.getName());

        assertEquals(messageEndpoint, action.getEndpoint());
        assertEquals(XML.name(), action.getMessageType());

        assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", ((StaticMessageBuilder) action.getMessageBuilder()).getMessage().getPayload());
        assertEquals(1L, ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size());
        assertEquals("<Header><Name>operation</Name><Value>foo</Value></Header>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0));
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
        assertEquals(2, test.getActionCount());
        assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());
        assertEquals(ReceiveMessageAction.class, test.getActions().get(1).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals("receive", action.getName());

        assertEquals(messageEndpoint, action.getEndpoint());
        assertEquals(XML.name(), action.getMessageType());

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));
        assertEquals(2L, ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size());
        assertEquals("<Header><Name>operation</Name><Value>foo1</Value></Header>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0));
        assertEquals("<Header><Name>operation</Name><Value>foo2</Value></Header>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(1));

        action = ((ReceiveMessageAction) test.getActions().get(1));
        assertEquals("receive", action.getName());

        assertEquals(messageEndpoint, action.getEndpoint());
        assertEquals(XML.name(), action.getMessageType());

        assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", ((StaticMessageBuilder) action.getMessageBuilder()).getMessage().getPayload());
        assertEquals(2L, ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size());
        assertEquals("<Header><Name>operation</Name><Value>foo1</Value></Header>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0));
        assertEquals("<Header><Name>operation</Name><Value>foo2</Value></Header>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(1));
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
        assertEquals(1, test.getActionCount());
        assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals("receive", action.getName());

        assertEquals(PLAINTEXT.name(), action.getMessageType());
        assertEquals(messageEndpoint, action.getEndpoint());
        assertEquals(1, action.getValidationContexts().size());
        assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        assertEquals(1L, ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size());
        assertEquals("<TestRequest><Message>Hello Citrus!</Message></TestRequest>",
                ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0));
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
        assertEquals(2, test.getActionCount());
        assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());
        assertEquals(ReceiveMessageAction.class, test.getActions().get(1).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals("receive", action.getName());

        assertEquals(messageEndpoint, action.getEndpoint());
        assertEquals(XML.name(), action.getMessageType());

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));
        assertEquals(1L, ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size());
        assertEquals("<Header><Name>operation</Name><Value>foo</Value></Header>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0));

        action = ((ReceiveMessageAction) test.getActions().get(1));
        assertEquals("receive", action.getName());

        assertEquals(messageEndpoint, action.getEndpoint());
        assertEquals(XML.name(), action.getMessageType());

        assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", ((StaticMessageBuilder) action.getMessageBuilder()).getMessage().getPayload());
        assertEquals(1L, ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size());
        assertEquals("<Header><Name>operation</Name><Value>bar</Value></Header>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0));

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
        assertEquals(2, test.getActionCount());
        assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());
        assertEquals(ReceiveMessageAction.class, test.getActions().get(1).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals("receive", action.getName());

        assertEquals(messageEndpoint, action.getEndpoint());
        assertEquals(XML.name(), action.getMessageType());

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));
        assertEquals(3L, ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size());
        assertEquals("<Header><Name>operation</Name><Value>sayHello</Value></Header>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0));
        assertEquals("<Header><Name>operation</Name><Value>foo</Value></Header>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(1));
        assertEquals("<Header><Name>operation</Name><Value>bar</Value></Header>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(2));

        action = ((ReceiveMessageAction) test.getActions().get(1));
        assertEquals("receive", action.getName());

        assertEquals(messageEndpoint, action.getEndpoint());
        assertEquals(XML.name(), action.getMessageType());

        assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", ((StaticMessageBuilder) action.getMessageBuilder()).getMessage().getPayload());
        assertEquals(3L, ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size());
        assertEquals("<Header><Name>operation</Name><Value>sayHello</Value></Header>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0));
        assertEquals("<Header><Name>operation</Name><Value>foo</Value></Header>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(1));
        assertEquals("<Header><Name>operation</Name><Value>bar</Value></Header>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(2));

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
        assertEquals(1, test.getActionCount());
        assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals("receive", action.getName());

        assertEquals(messageEndpoint, action.getEndpoint());
        assertEquals(PLAINTEXT.name(), action.getMessageType());
        assertEquals(1L, action.getValidators().size());
        assertEquals(validator, action.getValidators().get(0));

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        assertEquals("TestMessage", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));
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
        assertEquals(1, test.getActionCount());
        assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals("receive", action.getName());

        assertEquals(messageEndpoint, action.getEndpoint());
        assertEquals(PLAINTEXT.name(), action.getMessageType());
        assertEquals(1L, action.getValidators().size());
        assertEquals(validator, action.getValidators().get(0));

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        assertEquals("TestMessage", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));
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
        assertEquals(1, test.getActionCount());
        assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals("receive", action.getName());

        assertEquals(messageEndpoint, action.getEndpoint());
        assertEquals(PLAINTEXT.name(), action.getMessageType());
        assertEquals(dictionary, action.getDataDictionary());

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        assertEquals("TestMessage", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));
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
        assertEquals(1, test.getActionCount());
        assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals("receive", action.getName());

        assertEquals(messageEndpoint, action.getEndpoint());
        assertEquals(PLAINTEXT.name(), action.getMessageType());
        assertEquals(dictionary, action.getDataDictionary());

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        assertEquals("TestMessage", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));
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
        assertEquals(1, test.getActionCount());
        assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals("receive", action.getName());

        assertEquals(XML.name(), action.getMessageType());
        assertEquals(messageEndpoint, action.getEndpoint());

        assertEquals(messageSelector, action.getMessageSelectorMap());

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
        assertEquals(1, test.getActionCount());
        assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals("receive", action.getName());

        assertEquals(XML.name(), action.getMessageType());
        assertEquals(messageEndpoint, action.getEndpoint());

        assertTrue(action.getMessageSelectorMap().isEmpty());
        assertEquals("operation = 'sayHello'", action.getMessageSelector());

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
        assertEquals(received.getId(), context.getVariable("messageId"));

        TestCase test = runner.getTestCase();
        assertEquals(1, test.getActionCount());
        assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals("receive", action.getName());

        assertEquals(XML.name(), action.getMessageType());
        assertEquals(messageEndpoint, action.getEndpoint());

        assertEquals(1, action.getVariableExtractors().size());
        assertEquals(extractor, action.getVariableExtractors().get(0));
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
                .extract(extractor().fromHeaders()
                        .header("operation", "operationHeader")
                        .header("requestId", "id")));

        assertNotNull(context.getVariable("operationHeader"));
        assertNotNull(context.getVariable("id"));
        assertEquals("sayHello", context.getVariable("operationHeader"));
        assertEquals("123456", context.getVariable("id"));

        TestCase test = runner.getTestCase();
        assertEquals(1, test.getActionCount());
        assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals("receive", action.getName());

        assertEquals(XML.name(), action.getMessageType());
        assertEquals(messageEndpoint, action.getEndpoint());

        assertEquals(1, action.getVariableExtractors().size());
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
                .extract(extractor().message()
                        .headers()
                        .header("operation", "operationHeader")
                        .header("requestId", "id"))
                .process(extractor));

        assertNotNull(context.getVariable("operationHeader"));
        assertNotNull(context.getVariable("id"));
        assertEquals("sayHello", context.getVariable("operationHeader"));
        assertEquals("123456", context.getVariable("id"));

        assertNotNull(context.getVariable("messageId"));
        assertEquals(received.getId(), context.getVariable("messageId"));

        TestCase test = runner.getTestCase();
        assertEquals(1, test.getActionCount());
        assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals("receive", action.getName());

        assertEquals(XML.name(), action.getMessageType());
        assertEquals(messageEndpoint, action.getEndpoint());

        assertEquals(2, action.getVariableExtractors().size());
        assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        assertTrue(((MessageHeaderVariableExtractor) action.getVariableExtractors().get(0)).getHeaderMappings().containsKey("operation"));
        assertTrue(((MessageHeaderVariableExtractor) action.getVariableExtractors().get(0)).getHeaderMappings().containsKey("requestId"));

        assertEquals(extractor, action.getVariableExtractors().get(1));

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
        assertEquals(1, test.getActionCount());
        assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals("receive", action.getName());

        assertEquals(messageEndpoint, action.getEndpoint());
        assertEquals(PLAINTEXT.name(), action.getMessageType());
        assertEquals(callback, action.getValidationProcessor());

        assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        assertEquals("TestMessage", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));
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
                .validate(validation().xml()
                        .schemaValidation(false)));

        TestCase test = runner.getTestCase();
        assertEquals(1, test.getActionCount());
        assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        assertEquals("receive", action.getName());

        assertEquals(messageEndpoint, action.getEndpoint());
        assertEquals(2, action.getValidationContexts().size());
        assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));

        XmlMessageValidationContext xmlMessageValidationContext = action.getValidationContexts().stream()
                .filter(XmlMessageValidationContext.class::isInstance).findFirst()
                .map(XmlMessageValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));
        assertFalse(xmlMessageValidationContext.isSchemaValidationEnabled());
    }
}
