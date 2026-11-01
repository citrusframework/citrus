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

package org.citrusframework.xml.actons.dsl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.Source;

import org.citrusframework.TestCase;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.api.xml.Marshaller;
import org.citrusframework.api.xml.StringSource;
import org.citrusframework.base.DefaultTestCaseRunner;
import org.citrusframework.base.xml.Jaxb2Marshaller;
import org.citrusframework.container.SequenceAfterTest;
import org.citrusframework.container.SequenceBeforeTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageType;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.messaging.SelectiveConsumer;
import org.citrusframework.report.TestActionListeners;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.Resource;
import org.citrusframework.spring.context.SpringBeanReferenceResolver;
import org.citrusframework.validation.AbstractValidationProcessor;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.builder.StaticMessageBuilder;
import org.citrusframework.validation.context.DefaultMessageValidationContext;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.context.xml.XmlMessageValidationContext;
import org.citrusframework.validation.context.xml.XpathMessageValidationContext;
import org.citrusframework.validation.xml.XpathPayloadVariableExtractor;
import org.citrusframework.variable.MessageHeaderVariableExtractor;
import org.citrusframework.variable.dictionary.DataDictionary;
import org.citrusframework.xml.UnitTestSupport;
import org.citrusframework.xml.message.builder.MarshallingHeaderDataBuilder;
import org.citrusframework.xml.variable.dictionary.NodeMappingDataDictionary;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.Mockito;
import org.springframework.xml.validation.XmlValidator;
import org.springframework.xml.xsd.XsdSchema;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.xml.sax.SAXParseException;

import static org.citrusframework.message.MessageType.PLAINTEXT;
import static org.citrusframework.message.MessageType.XML;
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

    private final Marshaller marshaller = new Jaxb2Marshaller(TestRequest.class);

    @BeforeEach
    public void prepareMarshaller() {
        ((Jaxb2Marshaller) marshaller).setProperty(jakarta.xml.bind.Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
    }

    @Test
    public void testReceiveEmpty() {
        reset(messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new DefaultMessage("<Message>Hello</Message>"));
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(XML.name(), action.getMessageType());
        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(1, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
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
                .type(MessageType.PLAINTEXT));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(MessageType.PLAINTEXT.name(), action.getMessageType());
        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(2, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(DefaultMessageValidationContext.class::isInstance));

        Assertions.assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        Assertions.assertEquals("Foo", ((StaticMessageBuilder) action.getMessageBuilder()).getMessage().getPayload());
        Assertions.assertNotNull(((StaticMessageBuilder) action.getMessageBuilder()).getMessage().getHeader("operation"));
    }

    @Test
    public void testReceiveBuilderWithPayloadModel() {
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
        when(referenceResolver.resolveAll(Marshaller.class)).thenReturn(Collections.singletonMap("marshaller", marshaller));
        when(referenceResolver.resolve(Marshaller.class)).thenReturn(marshaller);

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .body(buildPayload().xml().marshal(new TestRequest("Hello Citrus!"))));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(XML.name(), action.getMessageType());
        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(2, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(DefaultMessageValidationContext.class::isInstance));

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals("<TestRequest><Message>Hello Citrus!</Message></TestRequest>",
                ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));
    }

    @Test
    public void testReceiveBuilderWithPayloadModelExplicitMarshaller() {
        reset(messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("<TestRequest><Message>Hello Citrus!</Message></TestRequest>")
                        .setHeader("operation", "foo"));
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .body(buildPayload().xml().marshal(new TestRequest("Hello Citrus!"), marshaller)));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(XML.name(), action.getMessageType());
        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(2, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(DefaultMessageValidationContext.class::isInstance));

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals("<TestRequest><Message>Hello Citrus!</Message></TestRequest>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));
    }

    @Test
    public void testReceiveBuilderWithPayloadModelExplicitMarshallerName() {
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
        when(referenceResolver.isResolvable("myMarshaller")).thenReturn(true);
        when(referenceResolver.resolve("myMarshaller", Marshaller.class)).thenReturn(marshaller);

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .body(buildPayload().xml().marshal(new TestRequest("Hello Citrus!"), "myMarshaller")));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(XML.name(), action.getMessageType());
        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(2, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(DefaultMessageValidationContext.class::isInstance));

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals("<TestRequest><Message>Hello Citrus!</Message></TestRequest>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));

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
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(XML.name(), action.getMessageType());
        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(2, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));

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
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(XML.name(), action.getMessageType());
        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(2, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));

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
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());
        Assertions.assertEquals("fooMessageEndpoint", action.getEndpointUri());
        Assertions.assertEquals(XML.name(), action.getMessageType());

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
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(1000L, action.getReceiveTimeout());

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
        Assertions.assertEquals(2, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(1).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(XML.name(), action.getMessageType());

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));
        Assertions.assertTrue(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaders(context).containsKey("some"));
        Assertions.assertTrue(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaders(context).containsKey("operation"));
        Assertions.assertTrue(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaders(context).containsKey("foo"));

        action = ((ReceiveMessageAction) test.getActions().get(1));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(XML.name(), action.getMessageType());

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));
        Assertions.assertTrue(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaders(context).containsKey("some"));
        Assertions.assertTrue(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaders(context).containsKey("operation"));
        Assertions.assertTrue(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaders(context).containsKey("foo"));

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
        Assertions.assertEquals(2, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(1).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(XML.name(), action.getMessageType());

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(1L, ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size());
        Assertions.assertEquals("<Header><Name>operation</Name><Value>foo</Value></Header>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0));

        action = ((ReceiveMessageAction) test.getActions().get(1));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(XML.name(), action.getMessageType());

        Assertions.assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", ((StaticMessageBuilder) action.getMessageBuilder()).getMessage().getPayload());
        Assertions.assertEquals(1L, ((StaticMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size());
        Assertions.assertEquals("<Header><Name>operation</Name><Value>foo</Value></Header>", ((StaticMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0));

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
        Assertions.assertEquals(2, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(1).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(XML.name(), action.getMessageType());

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(2L, ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size());
        Assertions.assertEquals("<Header><Name>operation</Name><Value>foo1</Value></Header>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0));
        Assertions.assertEquals("<Header><Name>operation</Name><Value>foo2</Value></Header>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(1));

        action = ((ReceiveMessageAction) test.getActions().get(1));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(XML.name(), action.getMessageType());

        Assertions.assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", ((StaticMessageBuilder) action.getMessageBuilder()).getMessage().getPayload());
        Assertions.assertEquals(2L, ((StaticMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size());
        Assertions.assertEquals("<Header><Name>operation</Name><Value>foo1</Value></Header>", ((StaticMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0));
        Assertions.assertEquals("<Header><Name>operation</Name><Value>foo2</Value></Header>", ((StaticMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(1));

    }

    @Test
    public void testReceiveBuilderWithHeaderFragment() {
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
        when(referenceResolver.resolveAll(Marshaller.class)).thenReturn(Collections.singletonMap("marshaller", marshaller));
        when(referenceResolver.resolve(Marshaller.class)).thenReturn(marshaller);

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .header(new MarshallingHeaderDataBuilder(new TestRequest("Hello Citrus!"))));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(PLAINTEXT.name(), action.getMessageType());
        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(1, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals(1L, ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size());
        Assertions.assertEquals("<TestRequest><Message>Hello Citrus!</Message></TestRequest>",
                ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0));
    }

    @Test
    public void testReceiveBuilderWithHeaderFragmentExplicitMarshaller() {
        reset(messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage()
                        .addHeaderData("<TestRequest><Message>Hello Citrus!</Message></TestRequest>")
                        .setHeader("operation", "foo"));
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .header(new MarshallingHeaderDataBuilder(new TestRequest("Hello Citrus!"), marshaller)));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(PLAINTEXT.name(), action.getMessageType());
        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(1, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals(1L, ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size());
        Assertions.assertEquals("<TestRequest><Message>Hello Citrus!</Message></TestRequest>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0));

    }

    @Test
    public void testReceiveBuilderWithHeaderFragmentExplicitMarshallerName() {
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
        when(referenceResolver.isResolvable("myMarshaller")).thenReturn(true);
        when(referenceResolver.resolve("myMarshaller", Marshaller.class)).thenReturn(marshaller);

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .header(new MarshallingHeaderDataBuilder(new TestRequest("Hello Citrus!"), "myMarshaller")));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(PLAINTEXT.name(), action.getMessageType());
        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(1, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals(1L, ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size());
        Assertions.assertEquals("<TestRequest><Message>Hello Citrus!</Message></TestRequest>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0));

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
        Assertions.assertEquals(2, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(1).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(XML.name(), action.getMessageType());

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(1L, ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size());
        Assertions.assertEquals("<Header><Name>operation</Name><Value>foo</Value></Header>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0));

        action = ((ReceiveMessageAction) test.getActions().get(1));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(XML.name(), action.getMessageType());

        Assertions.assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", ((StaticMessageBuilder) action.getMessageBuilder()).getMessage().getPayload());
        Assertions.assertEquals(1L, ((StaticMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size());
        Assertions.assertEquals("<Header><Name>operation</Name><Value>bar</Value></Header>", ((StaticMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0));

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
        Assertions.assertEquals(2, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(1).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(XML.name(), action.getMessageType());

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(3L, ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size());
        Assertions.assertEquals("<Header><Name>operation</Name><Value>sayHello</Value></Header>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0));
        Assertions.assertEquals("<Header><Name>operation</Name><Value>foo</Value></Header>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(1));
        Assertions.assertEquals("<Header><Name>operation</Name><Value>bar</Value></Header>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(2));

        action = ((ReceiveMessageAction) test.getActions().get(1));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(XML.name(), action.getMessageType());

        Assertions.assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        Assertions.assertEquals("<TestRequest><Message>Hello World!</Message></TestRequest>", ((StaticMessageBuilder) action.getMessageBuilder()).getMessage().getPayload());
        Assertions.assertEquals(3L, ((StaticMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size());
        Assertions.assertEquals("<Header><Name>operation</Name><Value>sayHello</Value></Header>", ((StaticMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0));
        Assertions.assertEquals("<Header><Name>operation</Name><Value>foo</Value></Header>", ((StaticMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(1));
        Assertions.assertEquals("<Header><Name>operation</Name><Value>bar</Value></Header>", ((StaticMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(2));

    }

    @Test
    public void testReceiveBuilderWithDictionary() {
        final NodeMappingDataDictionary dictionary = new NodeMappingDataDictionary();

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
                .type(MessageType.PLAINTEXT)
                .body("TestMessage")
                .header("operation", "sayHello")
                .dictionary(dictionary));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(MessageType.PLAINTEXT.name(), action.getMessageType());
        Assertions.assertEquals(dictionary, action.getDataDictionary());

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals("TestMessage", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));
        Assertions.assertTrue(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaders(context).containsKey("operation"));
    }

    @Test
    public void testReceiveBuilderWithDictionaryName() {
        final NodeMappingDataDictionary dictionary = new NodeMappingDataDictionary();

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
                .type(MessageType.PLAINTEXT)
                .body("TestMessage")
                .header("operation", "sayHello")
                .dictionary("customDictionary"));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(MessageType.PLAINTEXT.name(), action.getMessageType());
        Assertions.assertEquals(dictionary, action.getDataDictionary());

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals("TestMessage", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));
        Assertions.assertTrue(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaders(context).containsKey("operation"));
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
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(XML.name(), action.getMessageType());
        Assertions.assertEquals(messageEndpoint, action.getEndpoint());

        Assertions.assertEquals(messageSelector, action.getMessageSelectorMap());

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
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(XML.name(), action.getMessageType());
        Assertions.assertEquals(messageEndpoint, action.getEndpoint());

        Assertions.assertTrue(action.getMessageSelectorMap().isEmpty());
        Assertions.assertEquals("operation = 'sayHello'", action.getMessageSelector());

    }

    @Test
    public void testReceiveBuilderExtractFromBody() {
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
                .extract(extractor().fromBody().expression("/TestRequest/Message", "message")));

        Assertions.assertNotNull(context.getVariable("message"));
        Assertions.assertEquals("Hello World!", context.getVariable("message"));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(XML.name(), action.getMessageType());
        Assertions.assertEquals(messageEndpoint, action.getEndpoint());

        Assertions.assertEquals(1, action.getVariableExtractors().size());
    }

    @Test
    public void testReceiveBuilderExtractFromPathExpression() {
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
                .extract(extractor().path().expression("/TestRequest/Message", "message")));

        Assertions.assertNotNull(context.getVariable("message"));
        Assertions.assertEquals("Hello World!", context.getVariable("message"));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(XML.name(), action.getMessageType());
        Assertions.assertEquals(messageEndpoint, action.getEndpoint());

        Assertions.assertEquals(1, action.getVariableExtractors().size());
    }

    @Test
    public void testReceiveBuilderExtractFromXpathExpression() {
        reset(referenceResolver, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                        .setHeader("operation", "sayHello"));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .body("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                .extract(extractor().xpath()
                        .expression("/TestRequest/Message", "text")
                        .expression("/TestRequest/Message/@lang", "language")));

        Assertions.assertNotNull(context.getVariable("text"));
        Assertions.assertNotNull(context.getVariable("language"));
        Assertions.assertEquals("Hello World!", context.getVariable("text"));
        Assertions.assertEquals("ENG", context.getVariable("language"));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(XML.name(), action.getMessageType());
        Assertions.assertEquals(messageEndpoint, action.getEndpoint());

        Assertions.assertEquals(1, action.getVariableExtractors().size());
        Assertions.assertTrue(action.getVariableExtractors().get(0) instanceof XpathPayloadVariableExtractor);
        Assertions.assertTrue(((XpathPayloadVariableExtractor) action.getVariableExtractors().get(0)).getXpathExpressions().containsKey("/TestRequest/Message"));
        Assertions.assertTrue(((XpathPayloadVariableExtractor) action.getVariableExtractors().get(0)).getXpathExpressions().containsKey("/TestRequest/Message/@lang"));
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

        Assertions.assertNotNull(context.getVariable("operationHeader"));
        Assertions.assertNotNull(context.getVariable("id"));
        Assertions.assertEquals("sayHello", context.getVariable("operationHeader"));
        Assertions.assertEquals("123456", context.getVariable("id"));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(XML.name(), action.getMessageType());
        Assertions.assertEquals(messageEndpoint, action.getEndpoint());

        Assertions.assertEquals(1, action.getVariableExtractors().size());
        Assertions.assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        Assertions.assertTrue(((MessageHeaderVariableExtractor) action.getVariableExtractors().get(0)).getHeaderMappings().containsKey("operation"));
        Assertions.assertTrue(((MessageHeaderVariableExtractor) action.getVariableExtractors().get(0)).getHeaderMappings().containsKey("requestId"));

    }

    @Test
    public void testReceiveBuilderExtractCombined() {
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
                        .header("requestId", "id"))
                .extract(extractor().xpath()
                        .expression("/TestRequest/Message", "text")
                        .expression("/TestRequest/Message/@lang", "language")));

        Assertions.assertNotNull(context.getVariable("operationHeader"));
        Assertions.assertNotNull(context.getVariable("id"));
        Assertions.assertEquals("sayHello", context.getVariable("operationHeader"));
        Assertions.assertEquals("123456", context.getVariable("id"));

        Assertions.assertNotNull(context.getVariable("text"));
        Assertions.assertNotNull(context.getVariable("language"));
        Assertions.assertEquals("Hello World!", context.getVariable("text"));
        Assertions.assertEquals("ENG", context.getVariable("language"));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(XML.name(), action.getMessageType());
        Assertions.assertEquals(messageEndpoint, action.getEndpoint());

        Assertions.assertEquals(2, action.getVariableExtractors().size());
        Assertions.assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        Assertions.assertTrue(((MessageHeaderVariableExtractor) action.getVariableExtractors().get(0)).getHeaderMappings().containsKey("operation"));
        Assertions.assertTrue(((MessageHeaderVariableExtractor) action.getVariableExtractors().get(0)).getHeaderMappings().containsKey("requestId"));

        Assertions.assertTrue(action.getVariableExtractors().get(1) instanceof XpathPayloadVariableExtractor);
        Assertions.assertTrue(((XpathPayloadVariableExtractor) action.getVariableExtractors().get(1)).getXpathExpressions().containsKey("/TestRequest/Message"));
        Assertions.assertTrue(((XpathPayloadVariableExtractor) action.getVariableExtractors().get(1)).getXpathExpressions().containsKey("/TestRequest/Message/@lang"));

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
                .type(MessageType.PLAINTEXT)
                .body("TestMessage")
                .header("operation", "sayHello")
                .validate(callback));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(MessageType.PLAINTEXT.name(), action.getMessageType());
        Assertions.assertEquals(callback, action.getValidationProcessor());

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals("TestMessage", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));
        Assertions.assertTrue(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaders(context).containsKey("operation"));

        verify(callback, atLeastOnce()).setReferenceResolver(context.getReferenceResolver());
        verify(callback).validate(any(Message.class), any(TestContext.class));
    }

    @Test
    public void testReceiveBuilderWithNamespaceValidation() {
        reset(messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("<TestRequest xmlns:pfx=\"http://citrusframework.org/schemas/test\"><Message>Hello World!</Message></TestRequest>")
                        .setHeader("operation", "foo"));
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .body("<TestRequest xmlns:pfx=\"http://citrusframework.org/schemas/test\"><Message>Hello World!</Message></TestRequest>")
                .validate(validation().xml()
                        .namespace("pfx", "http://citrusframework.org/schemas/test")));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(XML.name(), action.getMessageType());
        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(2, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));

        XmlMessageValidationContext validationContext = action.getValidationContexts().stream()
                .filter(XmlMessageValidationContext.class::isInstance).findFirst()
                .map(XmlMessageValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals("<TestRequest xmlns:pfx=\"http://citrusframework.org/schemas/test\"><Message>Hello World!</Message></TestRequest>",
                ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals("http://citrusframework.org/schemas/test", validationContext.getControlNamespaces().get("pfx"));

    }

    @Test
    public void testReceiveBuilderWithXpathExpressions() {
        reset(messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("<TestRequest><Message lang=\"ENG\">Hello World!</Message><Operation>SayHello</Operation></TestRequest>")
                        .setHeader("operation", "sayHello"));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .body("<TestRequest><Message lang=\"ENG\">Hello World!</Message><Operation>SayHello</Operation></TestRequest>")
                .validate(validation().xpath()
                        .expression("TestRequest.Message", "Hello World!")
                        .expression("TestRequest.Operation", "SayHello")));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(XML.name(), action.getMessageType());
        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(2, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(XpathMessageValidationContext.class::isInstance));

        XpathMessageValidationContext validationContext = action.getValidationContexts().stream()
                .filter(XpathMessageValidationContext.class::isInstance).findFirst()
                .map(XpathMessageValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals(2L, validationContext.getXpathExpressions().size());
        Assertions.assertEquals("Hello World!", validationContext.getXpathExpressions().get("TestRequest.Message"));
        Assertions.assertEquals("SayHello", validationContext.getXpathExpressions().get("TestRequest.Operation"));

    }

    @Test
    public void testReceiveBuilderWithIgnoreElementsXpath() {
        reset(messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .setHeader("operation", "sayHello"));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .body("<TestRequest><Message>?</Message></TestRequest>")
                .validate(validation().xml()
                        .ignore("TestRequest.Message")));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(XML.name(), action.getMessageType());
        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(2, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));

        XmlMessageValidationContext validationContext = action.getValidationContexts().stream()
                .filter(XmlMessageValidationContext.class::isInstance).findFirst()
                .map(XmlMessageValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals("<TestRequest><Message>?</Message></TestRequest>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(1L, validationContext.getIgnoreExpressions().size());
        Assertions.assertEquals("TestRequest.Message", validationContext.getIgnoreExpressions().iterator().next());

    }

    @Test
    public void testReceiveBuilderWithSchema() throws IOException {
        XsdSchema schema = applicationContext.getBean("testSchema", XsdSchema.class);
        XmlValidator validator = Mockito.mock(XmlValidator.class);

        reset(referenceResolver, schema, validator, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("<TestRequest xmlns=\"http://citrusframework.org/test\"><Message>Hello World!</Message></TestRequest>")
                        .setHeader("operation", "sayHello"));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        when(schema.createValidator()).thenReturn(validator);
        when(validator.validate(any(Source.class))).thenReturn(new SAXParseException[]{});

        context.setReferenceResolver(new SpringBeanReferenceResolver(applicationContext).withFallback(referenceResolver));
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .body("<TestRequest xmlns=\"http://citrusframework.org/test\"><Message>Hello World!</Message></TestRequest>")
                .validate(validation().xml()
                        .schema("testSchema")));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(XML.name(), action.getMessageType());
        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(2, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));

        XmlMessageValidationContext validationContext = action.getValidationContexts().stream()
                .filter(XmlMessageValidationContext.class::isInstance).findFirst()
                .map(XmlMessageValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals("<TestRequest xmlns=\"http://citrusframework.org/test\"><Message>Hello World!</Message></TestRequest>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals("testSchema", validationContext.getSchema());

    }

    @Test
    public void testReceiveBuilderWithSchemaRepository() {
        XsdSchema schema = applicationContext.getBean("testSchema", XsdSchema.class);

        reset(schema, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("<TestRequest xmlns=\"http://citrusframework.org/test\"><Message>Hello World!</Message></TestRequest>")
                        .setHeader("operation", "sayHello"));

        when(schema.getTargetNamespace()).thenReturn("http://citrusframework.org/test");
        when(schema.getSource()).thenReturn(new StringSource("""
                <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
                     xmlns="http://citrusframework.org/test"
                     targetNamespace="http://citrusframework.org/test"
                     elementFormDefault="qualified"
                     attributeFormDefault="unqualified">
                    <xs:element name="TestRequest">
                      <xs:complexType>
                          <xs:sequence>
                            <xs:element name="Message" type="xs:string"/>
                          </xs:sequence>
                      </xs:complexType>
                    </xs:element></xs:schema>"""));

        context.setReferenceResolver(new SpringBeanReferenceResolver(applicationContext).withFallback(referenceResolver));
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .body("<TestRequest xmlns=\"http://citrusframework.org/test\"><Message>Hello World!</Message></TestRequest>")
                .validate(validation().xml()
                        .schemaRepository("customSchemaRepository")));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(XML.name(), action.getMessageType());
        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(2, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));

        XmlMessageValidationContext validationContext = action.getValidationContexts().stream()
                .filter(XmlMessageValidationContext.class::isInstance).findFirst()
                .map(XmlMessageValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals("<TestRequest xmlns=\"http://citrusframework.org/test\"><Message>Hello World!</Message></TestRequest>", ((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals("customSchemaRepository", validationContext.getSchemaRepository());

    }

    @Test
    public void testDeactivateSchemaValidation() {

        reset(messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("<TestRequest><Message>Hello Citrus!</Message></TestRequest>")
                        .setHeader("operation", "sayHello"));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                .message()
                .body("<TestRequest><Message>Hello Citrus!</Message></TestRequest>")
                .validate(validation().xml()
                        .schemaValidation(false)));

        TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(ReceiveMessageAction.class, test.getActions().get(0).getClass());

        ReceiveMessageAction action = ((ReceiveMessageAction) test.getActions().get(0));
        Assertions.assertEquals("receive", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(2, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assertions.assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));

        XmlMessageValidationContext xmlMessageValidationContext = action.getValidationContexts().stream()
                .filter(XmlMessageValidationContext.class::isInstance).findFirst()
                .map(XmlMessageValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));
        Assertions.assertFalse(xmlMessageValidationContext.isSchemaValidationEnabled());

    }
}
