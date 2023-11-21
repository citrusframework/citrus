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

package org.citrusframework.actions.dsl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.Source;

import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.TestCase;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.container.SequenceAfterTest;
import org.citrusframework.container.SequenceBeforeTest;
import org.citrusframework.context.SpringBeanReferenceResolver;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageType;
import org.citrusframework.message.builder.MarshallingHeaderDataBuilder;
import org.citrusframework.message.builder.MarshallingPayloadBuilder;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.messaging.SelectiveConsumer;
import org.citrusframework.report.TestActionListeners;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.Resource;
import org.citrusframework.validation.AbstractValidationProcessor;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.builder.StaticMessageBuilder;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.json.JsonMessageValidationContext;
import org.citrusframework.validation.xml.XmlMessageValidationContext;
import org.citrusframework.validation.xml.XpathMessageValidationContext;
import org.citrusframework.validation.xml.XpathPayloadVariableExtractor;
import org.citrusframework.variable.MessageHeaderVariableExtractor;
import org.citrusframework.variable.dictionary.DataDictionary;
import org.citrusframework.variable.dictionary.xml.NodeMappingDataDictionary;
import org.citrusframework.xml.Jaxb2Marshaller;
import org.citrusframework.xml.Marshaller;
import org.citrusframework.xml.StringSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.xml.validation.XmlValidator;
import org.springframework.xml.xsd.XsdSchema;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.xml.sax.SAXParseException;

import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.dsl.MessageSupport.MessageBodySupport.fromBody;
import static org.citrusframework.dsl.MessageSupport.MessageHeaderSupport.fromHeaders;
import static org.citrusframework.dsl.PathExpressionSupport.path;
import static org.citrusframework.dsl.XmlSupport.xml;
import static org.citrusframework.dsl.XpathSupport.xpath;
import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 */
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

    private final Marshaller marshaller = new Jaxb2Marshaller(TestRequest.class);

    @BeforeClass
    public void prepareMarshaller() {
        MockitoAnnotations.openMocks(this);
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
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));
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
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.PLAINTEXT.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));

        Assert.assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).getMessage().getPayload(), "Foo");
        Assert.assertNotNull(((StaticMessageBuilder)action.getMessageBuilder()).getMessage().getHeader("operation"));
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
                        .body(new MarshallingPayloadBuilder(new TestRequest("Hello Citrus!"))));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()),
                "<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
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
                        .body(new MarshallingPayloadBuilder(new TestRequest("Hello Citrus!"), marshaller)));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
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
                        .body(new MarshallingPayloadBuilder(new TestRequest("Hello Citrus!"), "myMarshaller")));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello Citrus!</Message></TestRequest>");

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
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");

    }

    @Test
    public void testReceiveBuilderWithPayloadResource() throws IOException {
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
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");

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
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");
        Assert.assertEquals(action.getEndpointUri(), "fooMessageEndpoint");
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

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
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getReceiveTimeout(), 1000L);

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
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertTrue(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaders(context).containsKey("some"));
        Assert.assertTrue(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaders(context).containsKey("operation"));
        Assert.assertTrue(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaders(context).containsKey("foo"));

        action = ((ReceiveMessageAction)test.getActions().get(1));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertTrue(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaders(context).containsKey("some"));
        Assert.assertTrue(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaders(context).containsKey("operation"));
        Assert.assertTrue(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaders(context).containsKey("foo"));

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
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).size(), 1L);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(0), "<Header><Name>operation</Name><Value>foo</Value></Header>");

        action = ((ReceiveMessageAction) test.getActions().get(1));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        Assert.assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).size(), 1L);
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(0), "<Header><Name>operation</Name><Value>foo</Value></Header>");

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
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).size(), 2L);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(0), "<Header><Name>operation</Name><Value>foo1</Value></Header>");
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(1), "<Header><Name>operation</Name><Value>foo2</Value></Header>");

        action = ((ReceiveMessageAction)test.getActions().get(1));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        Assert.assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).size(), 2L);
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(0), "<Header><Name>operation</Name><Value>foo1</Value></Header>");
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(1), "<Header><Name>operation</Name><Value>foo2</Value></Header>");

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
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).size(), 1L);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(0),
                "<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
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
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).size(), 1L);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(0), "<TestRequest><Message>Hello Citrus!</Message></TestRequest>");

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
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).size(), 1L);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(0), "<TestRequest><Message>Hello Citrus!</Message></TestRequest>");

    }

    @Test
    public void testReceiveBuilderWithHeaderResource() throws IOException {
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
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).size(), 1L);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(0), "<Header><Name>operation</Name><Value>foo</Value></Header>");

        action = ((ReceiveMessageAction)test.getActions().get(1));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        Assert.assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).size(), 1L);
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(0), "<Header><Name>operation</Name><Value>bar</Value></Header>");

    }

    @Test
    public void testReceiveBuilderWithMultipleHeaderResource() throws IOException {
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
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size(), 3L);
        Assert.assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).get(0), "<Header><Name>operation</Name><Value>sayHello</Value></Header>");
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(1), "<Header><Name>operation</Name><Value>foo</Value></Header>");
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(2), "<Header><Name>operation</Name><Value>bar</Value></Header>");

        action = ((ReceiveMessageAction)test.getActions().get(1));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        Assert.assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        Assert.assertEquals(((StaticMessageBuilder) action.getMessageBuilder()).getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(((StaticMessageBuilder) action.getMessageBuilder()).buildMessageHeaderData(context).size(), 3L);
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(0), "<Header><Name>operation</Name><Value>sayHello</Value></Header>");
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(1), "<Header><Name>operation</Name><Value>foo</Value></Header>");
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(2), "<Header><Name>operation</Name><Value>bar</Value></Header>");

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
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.PLAINTEXT.name());
        Assert.assertEquals(action.getDataDictionary(), dictionary);

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "TestMessage");
        Assert.assertTrue(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaders(context).containsKey("operation"));
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
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.PLAINTEXT.name());
        Assert.assertEquals(action.getDataDictionary(), dictionary);

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "TestMessage");
        Assert.assertTrue(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaders(context).containsKey("operation"));
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
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertEquals(action.getMessageSelectorMap(), messageSelector);

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
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertTrue(action.getMessageSelectorMap().isEmpty());
        Assert.assertEquals(action.getMessageSelector(), "operation = 'sayHello'");

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
                .extract(fromBody().expression("/TestRequest/Message", "message")));

        Assert.assertNotNull(context.getVariable("message"));
        Assert.assertEquals(context.getVariable("message"), "Hello World!");

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertEquals(action.getVariableExtractors().size(), 1);
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
                .extract(path().expression("/TestRequest/Message", "message")));

        Assert.assertNotNull(context.getVariable("message"));
        Assert.assertEquals(context.getVariable("message"), "Hello World!");

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertEquals(action.getVariableExtractors().size(), 1);
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
                                .extract(xpath()
                                            .expression("/TestRequest/Message", "text")
                                            .expression("/TestRequest/Message/@lang", "language")));

        Assert.assertNotNull(context.getVariable("text"));
        Assert.assertNotNull(context.getVariable("language"));
        Assert.assertEquals(context.getVariable("text"), "Hello World!");
        Assert.assertEquals(context.getVariable("language"), "ENG");

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertEquals(action.getVariableExtractors().size(), 1);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof XpathPayloadVariableExtractor);
        Assert.assertTrue(((XpathPayloadVariableExtractor) action.getVariableExtractors().get(0)).getXpathExpressions().containsKey("/TestRequest/Message"));
        Assert.assertTrue(((XpathPayloadVariableExtractor) action.getVariableExtractors().get(0)).getXpathExpressions().containsKey("/TestRequest/Message/@lang"));
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

        Assert.assertNotNull(context.getVariable("operationHeader"));
        Assert.assertNotNull(context.getVariable("id"));
        Assert.assertEquals(context.getVariable("operationHeader"), "sayHello");
        Assert.assertEquals(context.getVariable("id"), "123456");

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertEquals(action.getVariableExtractors().size(), 1);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        Assert.assertTrue(((MessageHeaderVariableExtractor) action.getVariableExtractors().get(0)).getHeaderMappings().containsKey("operation"));
        Assert.assertTrue(((MessageHeaderVariableExtractor) action.getVariableExtractors().get(0)).getHeaderMappings().containsKey("requestId"));

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
                                .extract(fromHeaders()
                                        .header("operation", "operationHeader")
                                        .header("requestId", "id"))
                                .extract(xpath()
                                        .expression("/TestRequest/Message", "text")
                                        .expression("/TestRequest/Message/@lang", "language")));

        Assert.assertNotNull(context.getVariable("operationHeader"));
        Assert.assertNotNull(context.getVariable("id"));
        Assert.assertEquals(context.getVariable("operationHeader"), "sayHello");
        Assert.assertEquals(context.getVariable("id"), "123456");

        Assert.assertNotNull(context.getVariable("text"));
        Assert.assertNotNull(context.getVariable("language"));
        Assert.assertEquals(context.getVariable("text"), "Hello World!");
        Assert.assertEquals(context.getVariable("language"), "ENG");

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertEquals(action.getVariableExtractors().size(), 2);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        Assert.assertTrue(((MessageHeaderVariableExtractor) action.getVariableExtractors().get(0)).getHeaderMappings().containsKey("operation"));
        Assert.assertTrue(((MessageHeaderVariableExtractor) action.getVariableExtractors().get(0)).getHeaderMappings().containsKey("requestId"));

        Assert.assertTrue(action.getVariableExtractors().get(1) instanceof XpathPayloadVariableExtractor);
        Assert.assertTrue(((XpathPayloadVariableExtractor) action.getVariableExtractors().get(1)).getXpathExpressions().containsKey("/TestRequest/Message"));
        Assert.assertTrue(((XpathPayloadVariableExtractor) action.getVariableExtractors().get(1)).getXpathExpressions().containsKey("/TestRequest/Message/@lang"));

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
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.PLAINTEXT.name());
        Assert.assertEquals(action.getValidationProcessor(), callback);

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "TestMessage");
        Assert.assertTrue(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaders(context).containsKey("operation"));

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
                                .validate(xml()
                                        .namespace("pfx", "http://citrusframework.org/schemas/test")));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 2);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));

        XmlMessageValidationContext validationContext = action.getValidationContexts().stream()
                .filter(XmlMessageValidationContext.class::isInstance).findFirst()
                .map(XmlMessageValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()),
                "<TestRequest xmlns:pfx=\"http://citrusframework.org/schemas/test\"><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(validationContext.getControlNamespaces().get("pfx"), "http://citrusframework.org/schemas/test");

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
                                .validate(xpath()
                                        .expression("TestRequest.Message", "Hello World!")
                                        .expression("TestRequest.Operation", "SayHello")));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 2);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(XpathMessageValidationContext.class::isInstance));

        XpathMessageValidationContext validationContext = action.getValidationContexts().stream()
                .filter(XpathMessageValidationContext.class::isInstance).findFirst()
                .map(XpathMessageValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(validationContext.getXpathExpressions().size(), 2L);
        Assert.assertEquals(validationContext.getXpathExpressions().get("TestRequest.Message"), "Hello World!");
        Assert.assertEquals(validationContext.getXpathExpressions().get("TestRequest.Operation"), "SayHello");

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
                                .validate(xml()
                                        .ignore("TestRequest.Message")));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 2);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));

        XmlMessageValidationContext validationContext = action.getValidationContexts().stream()
                .filter(XmlMessageValidationContext.class::isInstance).findFirst()
                .map(XmlMessageValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>?</Message></TestRequest>");
        Assert.assertEquals(validationContext.getIgnoreExpressions().size(), 1L);
        Assert.assertEquals(validationContext.getIgnoreExpressions().iterator().next(), "TestRequest.Message");

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
        when(validator.validate(any(Source.class))).thenReturn(new SAXParseException[] {});

        context.setReferenceResolver(new SpringBeanReferenceResolver(applicationContext).withFallback(referenceResolver));
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                                .message()
                                .body("<TestRequest xmlns=\"http://citrusframework.org/test\"><Message>Hello World!</Message></TestRequest>")
                                .validate(xml()
                                        .schema("testSchema")));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 2);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));

        XmlMessageValidationContext validationContext = action.getValidationContexts().stream()
                .filter(XmlMessageValidationContext.class::isInstance).findFirst()
                .map(XmlMessageValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest xmlns=\"http://citrusframework.org/test\"><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(validationContext.getSchema(), "testSchema");

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
        when(schema.getSource()).thenReturn(new StringSource("<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\n" +
                "     xmlns=\"http://citrusframework.org/test\"\n" +
                "     targetNamespace=\"http://citrusframework.org/test\"\n" +
                "     elementFormDefault=\"qualified\"\n" +
                "     attributeFormDefault=\"unqualified\">\n" +
                "    <xs:element name=\"TestRequest\">\n" +
                "      <xs:complexType>\n" +
                "          <xs:sequence>\n" +
                "            <xs:element name=\"Message\" type=\"xs:string\"/>\n" +
                "          </xs:sequence>\n" +
                "      </xs:complexType>\n" +
                "    </xs:element></xs:schema>"));

        context.setReferenceResolver(new SpringBeanReferenceResolver(applicationContext).withFallback(referenceResolver));
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(receive(messageEndpoint)
                                .message()
                                .body("<TestRequest xmlns=\"http://citrusframework.org/test\"><Message>Hello World!</Message></TestRequest>")
                                .validate(xml()
                                        .schemaRepository("customSchemaRepository")));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 2);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));

        XmlMessageValidationContext validationContext = action.getValidationContexts().stream()
                .filter(XmlMessageValidationContext.class::isInstance).findFirst()
                .map(XmlMessageValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest xmlns=\"http://citrusframework.org/test\"><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(validationContext.getSchemaRepository(), "customSchemaRepository");

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
                        .validate(xml()
                                .schemaValidation(false)));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 2);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));

        XmlMessageValidationContext xmlMessageValidationContext = action.getValidationContexts().stream()
                .filter(XmlMessageValidationContext.class::isInstance).findFirst()
                .map(XmlMessageValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));
        Assert.assertFalse(xmlMessageValidationContext.isSchemaValidationEnabled());

    }
}
