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

package org.citrusframework.citrus.dsl.runner;

import javax.xml.transform.Source;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.citrusframework.citrus.TestCase;
import org.citrusframework.citrus.actions.ReceiveMessageAction;
import org.citrusframework.citrus.container.SequenceAfterTest;
import org.citrusframework.citrus.container.SequenceBeforeTest;
import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.dsl.TestRequest;
import org.citrusframework.citrus.dsl.UnitTestSupport;
import org.citrusframework.citrus.endpoint.Endpoint;
import org.citrusframework.citrus.endpoint.EndpointConfiguration;
import org.citrusframework.citrus.exceptions.TestCaseFailedException;
import org.citrusframework.citrus.json.schema.SimpleJsonSchema;
import org.citrusframework.citrus.message.DefaultMessage;
import org.citrusframework.citrus.message.Message;
import org.citrusframework.citrus.message.MessageType;
import org.citrusframework.citrus.messaging.Consumer;
import org.citrusframework.citrus.messaging.SelectiveConsumer;
import org.citrusframework.citrus.report.TestActionListeners;
import org.citrusframework.citrus.script.ScriptTypes;
import org.citrusframework.citrus.spi.ReferenceResolver;
import org.citrusframework.citrus.validation.AbstractValidationProcessor;
import org.citrusframework.citrus.validation.builder.DefaultMessageBuilder;
import org.citrusframework.citrus.validation.builder.StaticMessageBuilder;
import org.citrusframework.citrus.validation.context.HeaderValidationContext;
import org.citrusframework.citrus.validation.json.JsonMessageValidationContext;
import org.citrusframework.citrus.validation.json.JsonPathMessageValidationContext;
import org.citrusframework.citrus.validation.json.JsonPathVariableExtractor;
import org.citrusframework.citrus.validation.json.report.GraciousProcessingReport;
import org.citrusframework.citrus.validation.script.GroovyJsonMessageValidator;
import org.citrusframework.citrus.validation.script.ScriptValidationContext;
import org.citrusframework.citrus.validation.text.PlainTextMessageValidator;
import org.citrusframework.citrus.validation.xml.XmlMessageValidationContext;
import org.citrusframework.citrus.validation.xml.XpathMessageValidationContext;
import org.citrusframework.citrus.validation.xml.XpathPayloadVariableExtractor;
import org.citrusframework.citrus.variable.MessageHeaderVariableExtractor;
import org.citrusframework.citrus.variable.dictionary.DataDictionary;
import org.citrusframework.citrus.variable.dictionary.xml.NodeMappingDataDictionary;
import org.citrusframework.citrus.xml.Marshaller;
import org.citrusframework.citrus.xml.MarshallerAdapter;
import org.citrusframework.citrus.xml.StringSource;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import org.hamcrest.core.AnyOf;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.xml.validation.XmlValidator;
import org.springframework.xml.xsd.XsdSchema;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.xml.sax.SAXParseException;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class ReceiveMessageTestRunnerTest extends UnitTestSupport {

    private Endpoint messageEndpoint = Mockito.mock(Endpoint.class);
    private Consumer messageConsumer = Mockito.mock(Consumer.class);
    private EndpointConfiguration configuration = Mockito.mock(EndpointConfiguration.class);
    private Resource resource = Mockito.mock(Resource.class);
    private ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);

    private XStreamMarshaller marshaller = new XStreamMarshaller();

    @BeforeClass
    public void prepareMarshaller() {
        marshaller.getXStream().processAnnotations(TestRequest.class);
    }

    @Test
    public void testReceiveEmpty() {
        reset(messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new DefaultMessage("<Message>Hello</Message>"));
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint));
            }
        };

        TestCase test = builder.getTestCase();
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
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                        .message(new DefaultMessage("Foo").setHeader("operation", "foo")));
            }
        };

        TestCase test = builder.getTestCase();
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

        when(referenceResolver.resolve(TestContext.class)).thenReturn(applicationContext.getBean(TestContext.class));
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(Marshaller.class)).thenReturn(Collections.<String, Marshaller>singletonMap("marshaller", new MarshallerAdapter(marshaller)));
        when(referenceResolver.resolve(Marshaller.class)).thenReturn(new MarshallerAdapter(marshaller));

        context.setReferenceResolver(referenceResolver);
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                        .payloadModel(new TestRequest("Hello Citrus!")));
            }
        };

        TestCase test = builder.getTestCase();
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
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                        .payload(new TestRequest("Hello Citrus!"), marshaller));
            }
        };

        TestCase test = builder.getTestCase();
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

        when(referenceResolver.resolve(TestContext.class)).thenReturn(applicationContext.getBean(TestContext.class));
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.isResolvable("myMarshaller")).thenReturn(true);
        when(referenceResolver.resolve("myMarshaller", Marshaller.class)).thenReturn(new MarshallerAdapter(marshaller));

        context.setReferenceResolver(referenceResolver);
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                        .payload(new TestRequest("Hello Citrus!"), "myMarshaller"));
            }
        };

        TestCase test = builder.getTestCase();
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
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>"));
            }
        };

        TestCase test = builder.getTestCase();
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

        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("<TestRequest><Message>Hello World!</Message></TestRequest>".getBytes()));
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                        .payload(resource));
            }
        };

        TestCase test = builder.getTestCase();
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
        TestContext context = applicationContext.getBean(TestContext.class);

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
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint("fooMessageEndpoint")
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>"));
            }
        };

        TestCase test = builder.getTestCase();
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
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .timeout(1000L));
            }
        };

        TestCase test = builder.getTestCase();
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
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .headers(Collections.singletonMap("some", "value"))
                        .header("operation", "sayHello")
                        .header("foo", "bar"));

                receive(action -> action.endpoint(messageEndpoint)
                        .header("operation", "sayHello")
                        .header("foo", "bar")
                        .headers(Collections.singletonMap("some", "value"))
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>"));
            }
        };

        TestCase test = builder.getTestCase();
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
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .header("<Header><Name>operation</Name><Value>foo</Value></Header>"));

                receive(action -> action.endpoint(messageEndpoint)
                        .message(new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>"))
                        .header("<Header><Name>operation</Name><Value>foo</Value></Header>"));
            }
        };

        TestCase test = builder.getTestCase();
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
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .header("<Header><Name>operation</Name><Value>foo1</Value></Header>")
                        .header("<Header><Name>operation</Name><Value>foo2</Value></Header>"));

                receive(action -> action.endpoint(messageEndpoint)
                        .message(new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>"))
                        .header("<Header><Name>operation</Name><Value>foo1</Value></Header>")
                        .header("<Header><Name>operation</Name><Value>foo2</Value></Header>"));
            }
        };

        TestCase test = builder.getTestCase();
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

        when(referenceResolver.resolve(TestContext.class)).thenReturn(applicationContext.getBean(TestContext.class));
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(Marshaller.class)).thenReturn(Collections.<String, Marshaller>singletonMap("marshaller", new MarshallerAdapter(marshaller)));
        when(referenceResolver.resolve(Marshaller.class)).thenReturn(new MarshallerAdapter(marshaller));

        context.setReferenceResolver(referenceResolver);
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                        .headerFragment(new TestRequest("Hello Citrus!")));
            }
        };

        TestCase test = builder.getTestCase();
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
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                        .headerFragment(new TestRequest("Hello Citrus!"), marshaller));
            }
        };

        TestCase test = builder.getTestCase();
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

        when(referenceResolver.resolve(TestContext.class)).thenReturn(applicationContext.getBean(TestContext.class));
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.isResolvable("myMarshaller")).thenReturn(true);
        when(referenceResolver.resolve("myMarshaller", Marshaller.class)).thenReturn(new MarshallerAdapter(marshaller));

        context.setReferenceResolver(referenceResolver);
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                        .headerFragment(new TestRequest("Hello Citrus!"), "myMarshaller"));
            }
        };

        TestCase test = builder.getTestCase();
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

        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("<Header><Name>operation</Name><Value>foo</Value></Header>".getBytes()))
                                       .thenReturn(new ByteArrayInputStream("<Header><Name>operation</Name><Value>bar</Value></Header>".getBytes()));

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .header(resource));

                receive(action -> action.endpoint(messageEndpoint)
                        .message(new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>"))
                        .header(resource));
            }
        };

        TestCase test = builder.getTestCase();
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

        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("<Header><Name>operation</Name><Value>foo</Value></Header>".getBytes()))
                                       .thenReturn(new ByteArrayInputStream("<Header><Name>operation</Name><Value>bar</Value></Header>".getBytes()))
                                       .thenReturn(new ByteArrayInputStream("<Header><Name>operation</Name><Value>foo</Value></Header>".getBytes()))
                                       .thenReturn(new ByteArrayInputStream("<Header><Name>operation</Name><Value>bar</Value></Header>".getBytes()));
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .header("<Header><Name>operation</Name><Value>sayHello</Value></Header>")
                        .header(resource)
                        .header(resource));

                receive(action -> action.endpoint(messageEndpoint)
                        .message(new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>"))
                        .header("<Header><Name>operation</Name><Value>sayHello</Value></Header>")
                        .header(resource)
                        .header(resource));
            }
        };

        TestCase test = builder.getTestCase();
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
    public void testReceiveBuilderWithValidator() {
        reset(messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new DefaultMessage("TestMessage").setHeader("operation", "sayHello"));
        final PlainTextMessageValidator validator = new PlainTextMessageValidator();

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                        .messageType(MessageType.PLAINTEXT)
                        .payload("TestMessage")
                        .header("operation", "sayHello")
                        .validator(validator));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.PLAINTEXT.name());
        Assert.assertEquals(action.getValidators().size(), 1L);
        Assert.assertEquals(action.getValidators().get(0), validator);

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "TestMessage");
        Assert.assertTrue(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaders(context).containsKey("operation"));

    }

    @Test
    public void testReceiveBuilderWithValidatorName() {
        final PlainTextMessageValidator validator = new PlainTextMessageValidator();

        reset(referenceResolver, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new DefaultMessage("TestMessage").setHeader("operation", "sayHello"));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(applicationContext.getBean(TestContext.class));
        when(referenceResolver.resolve("plainTextValidator")).thenReturn(validator);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                        .messageType(MessageType.PLAINTEXT)
                        .payload("TestMessage")
                        .header("operation", "sayHello")
                        .validator("plainTextValidator"));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.PLAINTEXT.name());
        Assert.assertEquals(action.getValidators().size(), 1L);
        Assert.assertEquals(action.getValidators().get(0), validator);

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "TestMessage");
        Assert.assertTrue(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaders(context).containsKey("operation"));
    }

    @Test
    public void testReceiveBuilderWithDictionary() {
        final DataDictionary<?> dictionary = new NodeMappingDataDictionary();

        reset(referenceResolver, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new DefaultMessage("TestMessage").setHeader("operation", "sayHello"));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(applicationContext.getBean(TestContext.class));
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                                .messageType(MessageType.PLAINTEXT)
                                .payload("TestMessage")
                                .header("operation", "sayHello")
                                .dictionary(dictionary));
            }
        };

        TestCase test = builder.getTestCase();
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
        final DataDictionary<?> dictionary = new NodeMappingDataDictionary();

        reset(referenceResolver, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new DefaultMessage("TestMessage").setHeader("operation", "sayHello"));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(applicationContext.getBean(TestContext.class));
        when(referenceResolver.resolve("customDictionary", DataDictionary.class)).thenReturn(dictionary);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                                .messageType(MessageType.PLAINTEXT)
                                .payload("TestMessage")
                                .header("operation", "sayHello")
                                .dictionary("customDictionary"));
            }
        };

        TestCase test = builder.getTestCase();
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

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                                .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .selector(messageSelector));
            }
        };

        TestCase test = builder.getTestCase();
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
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                                .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .selector("operation = 'sayHello'"));
            }
        };

        TestCase test = builder.getTestCase();
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
    public void testReceiveBuilderExtractFromPayload() {
        reset(referenceResolver, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                        .setHeader("operation", "sayHello"));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(applicationContext.getBean(TestContext.class));
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                                .payload("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                                .extractFromPayload("/TestRequest/Message", "text")
                                .extractFromPayload("/TestRequest/Message/@lang", "language"));
            }
        };

        TestContext context = builder.getTestContext();
        Assert.assertNotNull(context.getVariable("text"));
        Assert.assertNotNull(context.getVariable("language"));
        Assert.assertEquals(context.getVariable("text"), "Hello World!");
        Assert.assertEquals(context.getVariable("language"), "ENG");

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertEquals(action.getVariableExtractors().size(), 2);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof XpathPayloadVariableExtractor);
        Assert.assertTrue(action.getVariableExtractors().get(1) instanceof XpathPayloadVariableExtractor);
        Assert.assertTrue(((XpathPayloadVariableExtractor) action.getVariableExtractors().get(0)).getXpathExpressions().containsKey("/TestRequest/Message"));
        Assert.assertTrue(((XpathPayloadVariableExtractor) action.getVariableExtractors().get(1)).getXpathExpressions().containsKey("/TestRequest/Message/@lang"));

    }

    @Test
    public void testReceiveBuilderExtractJsonPathFromPayload() {
        reset(referenceResolver, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                        .setHeader("operation", "sayHello"));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(applicationContext.getBean(TestContext.class));
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                                .messageType(MessageType.JSON)
                                .payload("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                                .extractFromPayload("$.text", "text")
                                .extractFromPayload("$.toString()", "payload")
                                .extractFromPayload("$.person", "person"));
            }
        };

        TestContext context = builder.getTestContext();
        Assert.assertNotNull(context.getVariable("text"));
        Assert.assertNotNull(context.getVariable("person"));
        Assert.assertNotNull(context.getVariable("payload"));
        Assert.assertEquals(context.getVariable("text"), "Hello World!");
        Assert.assertEquals(context.getVariable("payload"), "{\"person\":{\"surname\":\"Doe\",\"name\":\"John\"},\"index\":5,\"text\":\"Hello World!\",\"id\":\"x123456789x\"}");
        Assert.assertTrue(context.getVariable("person").contains("\"John\""));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.JSON.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertEquals(action.getVariableExtractors().size(), 3);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof JsonPathVariableExtractor);
        Assert.assertTrue(action.getVariableExtractors().get(1) instanceof JsonPathVariableExtractor);
        Assert.assertTrue(action.getVariableExtractors().get(2) instanceof JsonPathVariableExtractor);
        Assert.assertTrue(((JsonPathVariableExtractor) action.getVariableExtractors().get(0)).getJsonPathExpressions().containsKey("$.text"));
        Assert.assertTrue(((JsonPathVariableExtractor) action.getVariableExtractors().get(1)).getJsonPathExpressions().containsKey("$.toString()"));
        Assert.assertTrue(((JsonPathVariableExtractor) action.getVariableExtractors().get(2)).getJsonPathExpressions().containsKey("$.person"));

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

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                                .payload("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                                .extractFromHeader("operation", "operationHeader")
                                .extractFromHeader("requestId", "id"));
            }
        };

        TestContext context = builder.getTestContext();
        Assert.assertNotNull(context.getVariable("operationHeader"));
        Assert.assertNotNull(context.getVariable("id"));
        Assert.assertEquals(context.getVariable("operationHeader"), "sayHello");
        Assert.assertEquals(context.getVariable("id"), "123456");

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertEquals(action.getVariableExtractors().size(), 2);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        Assert.assertTrue(action.getVariableExtractors().get(1) instanceof MessageHeaderVariableExtractor);
        Assert.assertTrue(((MessageHeaderVariableExtractor) action.getVariableExtractors().get(0)).getHeaderMappings().containsKey("operation"));
        Assert.assertTrue(((MessageHeaderVariableExtractor) action.getVariableExtractors().get(1)).getHeaderMappings().containsKey("requestId"));

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

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                                .payload("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                                .extractFromHeader("operation", "operationHeader")
                                .extractFromHeader("requestId", "id")
                                .extractFromPayload("/TestRequest/Message", "text")
                                .extractFromPayload("/TestRequest/Message/@lang", "language"));
            }
        };

        TestContext context = builder.getTestContext();
        Assert.assertNotNull(context.getVariable("operationHeader"));
        Assert.assertNotNull(context.getVariable("id"));
        Assert.assertEquals(context.getVariable("operationHeader"), "sayHello");
        Assert.assertEquals(context.getVariable("id"), "123456");

        Assert.assertNotNull(context.getVariable("text"));
        Assert.assertNotNull(context.getVariable("language"));
        Assert.assertEquals(context.getVariable("text"), "Hello World!");
        Assert.assertEquals(context.getVariable("language"), "ENG");

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertEquals(action.getVariableExtractors().size(), 4);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        Assert.assertTrue(action.getVariableExtractors().get(1) instanceof MessageHeaderVariableExtractor);
        Assert.assertTrue(((MessageHeaderVariableExtractor) action.getVariableExtractors().get(0)).getHeaderMappings().containsKey("operation"));
        Assert.assertTrue(((MessageHeaderVariableExtractor) action.getVariableExtractors().get(1)).getHeaderMappings().containsKey("requestId"));

        Assert.assertTrue(action.getVariableExtractors().get(2) instanceof XpathPayloadVariableExtractor);
        Assert.assertTrue(action.getVariableExtractors().get(3) instanceof XpathPayloadVariableExtractor);
        Assert.assertTrue(((XpathPayloadVariableExtractor) action.getVariableExtractors().get(2)).getXpathExpressions().containsKey("/TestRequest/Message"));
        Assert.assertTrue(((XpathPayloadVariableExtractor) action.getVariableExtractors().get(3)).getXpathExpressions().containsKey("/TestRequest/Message/@lang"));

    }

    @Test
    public void testReceiveBuilderWithValidationProcessor() {
        final AbstractValidationProcessor processor = Mockito.mock(AbstractValidationProcessor.class);

        reset(processor, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new DefaultMessage("TestMessage").setHeader("operation", "sayHello"));

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                                .messageType(MessageType.PLAINTEXT)
                                .payload("TestMessage")
                                .header("operation", "sayHello")
                                .validationCallback(processor));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.PLAINTEXT.name());
        Assert.assertEquals(action.getValidationProcessor(), processor);

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "TestMessage");
        Assert.assertTrue(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessageHeaders(context).containsKey("operation"));

        verify(processor, atLeastOnce()).setReferenceResolver(context.getReferenceResolver());
        verify(processor).validate(any(Message.class), any(TestContext.class));
    }

    @Test
    public void testReceiveBuilderWithValidationScript() {
        final GroovyJsonMessageValidator validator = new GroovyJsonMessageValidator();

        reset(referenceResolver, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new DefaultMessage("{\"message\": \"Hello Citrus!\"}").setHeader("operation", "sayHello"));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(applicationContext.getBean(TestContext.class));
        when(referenceResolver.resolve("groovyMessageValidator")).thenReturn(validator);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                                .messageType(MessageType.JSON)
                                .validateScript("assert json.message == 'Hello Citrus!'")
                                .validator("groovyMessageValidator"));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.JSON.name());
        Assert.assertEquals(action.getValidators().size(), 1L);
        Assert.assertEquals(action.getValidators().get(0), validator);

        Assert.assertEquals(action.getValidationContexts().size(), 3L);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(ScriptValidationContext.class::isInstance));

        ScriptValidationContext validationContext = action.getValidationContexts().stream()
                .filter(ScriptValidationContext.class::isInstance).findFirst()
                .map(ScriptValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));

        Assert.assertEquals(validationContext.getScriptType(), ScriptTypes.GROOVY);
        Assert.assertEquals(validationContext.getValidationScript().trim(), "assert json.message == 'Hello Citrus!'");
        Assert.assertNull(validationContext.getValidationScriptResourcePath());

    }

    @Test
    public void testReceiveBuilderWithValidationScriptResourcePath() throws IOException {
        final GroovyJsonMessageValidator validator = new GroovyJsonMessageValidator();

        reset(referenceResolver, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new DefaultMessage("{\"message\": \"Hello Citrus!\"}").setHeader("operation", "sayHello"));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(applicationContext.getBean(TestContext.class));
        when(referenceResolver.resolve("groovyMessageValidator")).thenReturn(validator);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                                .messageType(MessageType.JSON)
                                .validateScriptResource("classpath:org/citrusframework/citrus/dsl/runner/validation.groovy")
                                .validator("groovyMessageValidator"));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.JSON.name());
        Assert.assertEquals(action.getValidators().size(), 1L);
        Assert.assertEquals(action.getValidators().get(0), validator);

        Assert.assertEquals(action.getValidationContexts().size(), 3L);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(ScriptValidationContext.class::isInstance));

        ScriptValidationContext validationContext = action.getValidationContexts().stream()
                .filter(ScriptValidationContext.class::isInstance).findFirst()
                .map(ScriptValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));

        Assert.assertEquals(validationContext.getScriptType(), ScriptTypes.GROOVY);
        Assert.assertEquals(validationContext.getValidationScript(), "");
        Assert.assertEquals(validationContext.getValidationScriptResourcePath(), "classpath:org/citrusframework/citrus/dsl/runner/validation.groovy");
    }

    @Test
    public void testReceiveBuilderWithValidationScriptResource() throws IOException {
        final GroovyJsonMessageValidator validator = new GroovyJsonMessageValidator();

        reset(referenceResolver, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new DefaultMessage("{\"message\": \"Hello Citrus!\"}").setHeader("operation", "sayHello"));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(applicationContext.getBean(TestContext.class));
        when(referenceResolver.resolve("groovyMessageValidator")).thenReturn(validator);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                                .messageType(MessageType.JSON)
                                .validateScript(new ClassPathResource("org/citrusframework/citrus/dsl/runner/validation.groovy"))
                                .validator("groovyMessageValidator"));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.JSON.name());
        Assert.assertEquals(action.getValidators().size(), 1L);
        Assert.assertEquals(action.getValidators().get(0), validator);

        Assert.assertEquals(action.getValidationContexts().size(), 3L);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(ScriptValidationContext.class::isInstance));

        ScriptValidationContext validationContext = action.getValidationContexts().stream()
                .filter(ScriptValidationContext.class::isInstance).findFirst()
                .map(ScriptValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));

        Assert.assertEquals(validationContext.getScriptType(), ScriptTypes.GROOVY);
        Assert.assertEquals(validationContext.getValidationScript().trim(), "assert json.message == 'Hello Citrus!'");
        Assert.assertNull(validationContext.getValidationScriptResourcePath());
    }

    @Test
    public void testReceiveBuilderWithValidationScriptAndHeader() {
        final GroovyJsonMessageValidator validator = new GroovyJsonMessageValidator();

        reset(referenceResolver, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new DefaultMessage("{\"message\": \"Hello Citrus!\"}").setHeader("operation", "sayHello"));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(applicationContext.getBean(TestContext.class));
        when(referenceResolver.resolve("groovyMessageValidator")).thenReturn(validator);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                                .messageType(MessageType.JSON)
                                .validateScript("assert json.message == 'Hello Citrus!'")
                                .validator("groovyMessageValidator")
                                .header("operation", "sayHello"));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.JSON.name());
        Assert.assertEquals(action.getValidators().size(), 1L);
        Assert.assertEquals(action.getValidators().get(0), validator);

        Assert.assertEquals(action.getValidationContexts().size(), 3L);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(ScriptValidationContext.class::isInstance));

        ScriptValidationContext validationContext = action.getValidationContexts().stream()
                .filter(ScriptValidationContext.class::isInstance).findFirst()
                .map(ScriptValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));

        Assert.assertEquals(validationContext.getScriptType(), ScriptTypes.GROOVY);
        Assert.assertEquals(validationContext.getValidationScript().trim(), "assert json.message == 'Hello Citrus!'");
        Assert.assertNull(validationContext.getValidationScriptResourcePath());

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "");
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaders(context).size(), 1L);
        Assert.assertTrue(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaders(context).containsKey("operation"));

    }

    @Test
    public void testReceiveBuilderWithNamespaceValidation() {
        reset(messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("<TestRequest xmlns:pfx=\"http://www.consol.de/schemas/test\"><Message>Hello World!</Message></TestRequest>")
                        .setHeader("operation", "foo"));
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                                .payload("<TestRequest xmlns:pfx=\"http://www.consol.de/schemas/test\"><Message>Hello World!</Message></TestRequest>")
                                .validateNamespace("pfx", "http://www.consol.de/schemas/test"));
            }
        };

        TestCase test = builder.getTestCase();
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
                "<TestRequest xmlns:pfx=\"http://www.consol.de/schemas/test\"><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(validationContext.getControlNamespaces().get("pfx"), "http://www.consol.de/schemas/test");

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

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                                .payload("<TestRequest><Message lang=\"ENG\">Hello World!</Message><Operation>SayHello</Operation></TestRequest>")
                                .validate("TestRequest.Message", "Hello World!")
                                .validate("TestRequest.Operation", "SayHello"));
            }
        };

        TestCase test = builder.getTestCase();
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
    public void testReceiveBuilderWithJsonPathExpressions() {
        reset(messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\",\"active\": true}, \"index\":5, \"id\":\"x123456789x\"}")
                        .setHeader("operation", "sayHello"));

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                                .messageType(MessageType.JSON)
                                .payload("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\",\"active\": true}, \"index\":5, \"id\":\"x123456789x\"}")
                                .validate("$.person.name", "John")
                                .validate("$.person.active", true)
                                .validate("$.id", anyOf(containsString("123456789"), nullValue()))
                                .validate("$.text", "Hello World!")
                                .validate("$.index", 5));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.JSON.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonPathMessageValidationContext.class::isInstance));

        JsonPathMessageValidationContext validationContext = action.getValidationContexts().stream()
                .filter(JsonPathMessageValidationContext.class::isInstance).findFirst()
                .map(JsonPathMessageValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(validationContext.getJsonPathExpressions().size(), 5L);
        Assert.assertEquals(validationContext.getJsonPathExpressions().get("$.person.name"), "John");
        Assert.assertEquals(validationContext.getJsonPathExpressions().get("$.person.active"), true);
        Assert.assertEquals(validationContext.getJsonPathExpressions().get("$.text"), "Hello World!");
        Assert.assertEquals(validationContext.getJsonPathExpressions().get("$.index"), 5);
        Assert.assertEquals(validationContext.getJsonPathExpressions().get("$.id").getClass(), AnyOf.class);
    }

    @Test(expectedExceptions = TestCaseFailedException.class)
    public void testReceiveBuilderWithJsonPathExpressionsFailure() {
        reset(messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                        .setHeader("operation", "sayHello"));

        new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                                .messageType(MessageType.JSON)
                                .payload("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                                .validate("$.person.name", "John")
                                .validate("$.text", "Hello Citrus!"));
            }
        };
    }

    @Test(expectedExceptions = TestCaseFailedException.class)
    public void testReceiveBuilderWithJsonValidationFailure() {
        reset(messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                        .setHeader("operation", "sayHello"));

        new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                                .messageType(MessageType.JSON)
                                .payload("{\"text\":\"Hello Citrus!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                                .validate("$.person.name", "John")
                                .validate("$.text", "Hello World!"));
            }
        };
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

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                                .payload("<TestRequest><Message>?</Message></TestRequest>")
                                .ignore("TestRequest.Message"));
            }
        };

        TestCase test = builder.getTestCase();
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
    public void testReceiveBuilderWithIgnoreElementsJson() {
        reset(messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                        .setHeader("operation", "sayHello"));

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                                .messageType(MessageType.JSON)
                                .payload("{\"text\":\"?\", \"person\":{\"name\":\"John\",\"surname\":\"?\"}, \"index\":0, \"id\":\"x123456789x\"}")
                                .ignore("$..text")
                                .ignore("$.person.surname")
                                .ignore("$.index"));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.JSON.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 2);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));

        JsonMessageValidationContext validationContext = action.getValidationContexts().stream()
                .filter(JsonMessageValidationContext.class::isInstance).findFirst()
                .map(JsonMessageValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "{\"text\":\"?\", \"person\":{\"name\":\"John\",\"surname\":\"?\"}, \"index\":0, \"id\":\"x123456789x\"}");
        Assert.assertEquals(validationContext.getIgnoreExpressions().size(), 3L);
        Assert.assertTrue(validationContext.getIgnoreExpressions().contains("$..text"));
        Assert.assertTrue(validationContext.getIgnoreExpressions().contains("$.person.surname"));
        Assert.assertTrue(validationContext.getIgnoreExpressions().contains("$.index"));

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

        when(referenceResolver.resolve(TestContext.class)).thenReturn(applicationContext.getBean(TestContext.class));
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        when(schema.createValidator()).thenReturn(validator);
        when(validator.validate(any(Source.class))).thenReturn(new SAXParseException[] {});

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                                .payload("<TestRequest xmlns=\"http://citrusframework.org/test\"><Message>Hello World!</Message></TestRequest>")
                                .xsd("testSchema"));
            }
        };

        TestCase test = builder.getTestCase();
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
    public void testReceiveBuilderWithSchemaRepository() throws IOException {
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
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                                .payload("<TestRequest xmlns=\"http://citrusframework.org/test\"><Message>Hello World!</Message></TestRequest>")
                                .xsdSchemaRepository("customSchemaRepository"));
            }
        };

        TestCase test = builder.getTestCase();
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
    public void testReceiveBuilderWithJsonSchemaRepository() throws ProcessingException {
        SimpleJsonSchema schema = applicationContext.getBean("jsonTestSchema", SimpleJsonSchema.class);

        reset(schema, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("{}")
                        .setHeader("operation", "sayHello"));

        JsonSchema jsonSchemaMock = mock(JsonSchema.class);
        when(jsonSchemaMock.validate(any())).thenReturn(new GraciousProcessingReport(true));
        when(schema.getSchema()).thenReturn(jsonSchemaMock);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                        .payload("{}")
                        .jsonSchemaRepository("customJsonSchemaRepository"));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 2);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));

        JsonMessageValidationContext validationContext = action.getValidationContexts().stream()
                .filter(JsonMessageValidationContext.class::isInstance).findFirst()
                .map(JsonMessageValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "{}");
        Assert.assertEquals(validationContext.getSchemaRepository(), "customJsonSchemaRepository");

    }

    @Test
    public void testReceiveBuilderWithJsonSchema() throws ProcessingException {
        SimpleJsonSchema schema = applicationContext.getBean("jsonTestSchema", SimpleJsonSchema.class);

        reset(schema, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("{}")
                        .setHeader("operation", "sayHello"));

        JsonSchema jsonSchemaMock = mock(JsonSchema.class);
        when(jsonSchemaMock.validate(any())).thenReturn(new GraciousProcessingReport(true));
        when(schema.getSchema()).thenReturn(jsonSchemaMock);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                        .payload("{}")
                        .jsonSchema("jsonTestSchema"));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 2);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));

        JsonMessageValidationContext validationContext = action.getValidationContexts().stream()
                .filter(JsonMessageValidationContext.class::isInstance).findFirst()
                .map(JsonMessageValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "{}");
        Assert.assertEquals(validationContext.getSchema(), "jsonTestSchema");

    }

    @Test
    public void testActivateSchemaValidation() throws Exception {
        SimpleJsonSchema schema = applicationContext.getBean("jsonTestSchema", SimpleJsonSchema.class);

        reset(schema, messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("{}")
                        .setHeader("operation", "sayHello"));

        JsonSchema jsonSchemaMock = mock(JsonSchema.class);
        when(jsonSchemaMock.validate(any())).thenReturn(new GraciousProcessingReport(true));
        when(schema.getSchema()).thenReturn(jsonSchemaMock);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                        .payload("{}")
                        .schemaValidation(true));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));

        XmlMessageValidationContext xmlMessageValidationContext = action.getValidationContexts().stream()
                        .filter(XmlMessageValidationContext.class::isInstance).findFirst()
                        .map(XmlMessageValidationContext.class::cast)
                        .orElseThrow(() -> new AssertionError("Missing validation context"));
        Assert.assertTrue(xmlMessageValidationContext.isSchemaValidationEnabled());

        JsonMessageValidationContext jsonMessageValidationContext = action.getValidationContexts().stream()
                .filter(JsonMessageValidationContext.class::isInstance).findFirst()
                .map(JsonMessageValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));
        Assert.assertTrue(jsonMessageValidationContext.isSchemaValidationEnabled());

    }

    @Test
    public void testDeactivateSchemaValidation() throws IOException {

        reset(messageEndpoint, messageConsumer, configuration);
        when(messageEndpoint.createConsumer()).thenReturn(messageConsumer);
        when(messageEndpoint.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(messageEndpoint.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(
                new DefaultMessage("{}")
                        .setHeader("operation", "sayHello"));

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                receive(action -> action.endpoint(messageEndpoint)
                        .payload("{}")
                        .schemaValidation(false));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = ((ReceiveMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));

        XmlMessageValidationContext xmlMessageValidationContext = action.getValidationContexts().stream()
                .filter(XmlMessageValidationContext.class::isInstance).findFirst()
                .map(XmlMessageValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));
        Assert.assertFalse(xmlMessageValidationContext.isSchemaValidationEnabled());

        JsonMessageValidationContext jsonMessageValidationContext = action.getValidationContexts().stream()
                .filter(JsonMessageValidationContext.class::isInstance).findFirst()
                .map(JsonMessageValidationContext.class::cast)
                .orElseThrow(() -> new AssertionError("Missing validation context"));
        Assert.assertFalse(jsonMessageValidationContext.isSchemaValidationEnabled());

    }
}
