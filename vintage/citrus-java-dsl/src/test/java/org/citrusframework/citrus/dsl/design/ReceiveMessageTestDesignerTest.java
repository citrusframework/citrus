/*
 * Copyright 2006-2012 the original author or authors.
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

package org.citrusframework.citrus.dsl.design;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.citrusframework.citrus.TestCase;
import org.citrusframework.citrus.actions.ReceiveMessageAction;
import org.citrusframework.citrus.container.SequenceAfterTest;
import org.citrusframework.citrus.container.SequenceBeforeTest;
import org.citrusframework.citrus.dsl.TestRequest;
import org.citrusframework.citrus.dsl.UnitTestSupport;
import org.citrusframework.citrus.endpoint.Endpoint;
import org.citrusframework.citrus.message.DefaultMessage;
import org.citrusframework.citrus.message.MessageType;
import org.citrusframework.citrus.report.TestActionListeners;
import org.citrusframework.citrus.script.ScriptTypes;
import org.citrusframework.citrus.spi.ReferenceResolver;
import org.citrusframework.citrus.validation.ValidationProcessor;
import org.citrusframework.citrus.validation.builder.DefaultMessageBuilder;
import org.citrusframework.citrus.validation.builder.StaticMessageBuilder;
import org.citrusframework.citrus.validation.context.HeaderValidationContext;
import org.citrusframework.citrus.validation.json.JsonMessageValidationContext;
import org.citrusframework.citrus.validation.json.JsonPathMessageValidationContext;
import org.citrusframework.citrus.validation.json.JsonPathVariableExtractor;
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
import org.hamcrest.core.StringContains;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;


/**
 * @author Christoph Deppisch
 */
public class ReceiveMessageTestDesignerTest extends UnitTestSupport {

    private Endpoint messageEndpoint = Mockito.mock(Endpoint.class);
    private Resource resource = Mockito.mock(Resource.class);
    private ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);

    private XStreamMarshaller marshaller = new XStreamMarshaller();

    @BeforeClass
    public void prepareMarshaller() {
        marshaller.getXStream().processAnnotations(TestRequest.class);
    }

    @Test
    public void testReceiveEmpty() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
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
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                    .message(new DefaultMessage("Foo").setHeader("operation", "foo"));
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.PLAINTEXT.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));

        Assert.assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "Foo");
        Assert.assertNotNull(((StaticMessageBuilder)action.getMessageBuilder()).getMessage().getHeader("operation"));
    }

    @Test
    public void testReceiveBuilderWithPayloadModel() {
        reset(referenceResolver);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(Marshaller.class)).thenReturn(Collections.<String, Marshaller>singletonMap("marshaller", new MarshallerAdapter(marshaller)));
        when(referenceResolver.resolve(Marshaller.class)).thenReturn(new MarshallerAdapter(marshaller));

        context.setReferenceResolver(referenceResolver);
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                        .payloadModel(new TestRequest("Hello Citrus!"));
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
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
    public void testReceiveBuilderWithPayloadModelExplicitMarshaller() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                        .payload(new TestRequest("Hello Citrus!"), marshaller);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
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
        reset(referenceResolver);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.isResolvable("myMarshaller")).thenReturn(true);
        when(referenceResolver.resolve("myMarshaller", Marshaller.class)).thenReturn(new MarshallerAdapter(marshaller));

        context.setReferenceResolver(referenceResolver);
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                        .payload(new TestRequest("Hello Citrus!"), "myMarshaller");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
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
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
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
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                    .payload(resource);
            }
        };

        reset(resource);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("somePayload".getBytes()));
        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(HeaderValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(XmlMessageValidationContext.class::isInstance));
        Assert.assertTrue(action.getValidationContexts().stream().anyMatch(JsonMessageValidationContext.class::isInstance));

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "somePayload");

    }

    @Test
    public void testReceiveBuilderWithEndpointName() {
        reset(referenceResolver);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive("fooMessageEndpoint")
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "receive");
        Assert.assertEquals(action.getEndpointUri(), "fooMessageEndpoint");
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

    }

    @Test
    public void testReceiveBuilderWithTimeout() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .timeout(1000L);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getReceiveTimeout(), 1000L);
    }

    @Test
    public void testReceiveBuilderWithHeaders() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .headers(Collections.singletonMap("some", "value"))
                    .header("operation", "sayHello")
                    .header("foo", "bar");

                receive(messageEndpoint)
                    .header("operation", "sayHello")
                    .header("foo", "bar")
                    .headers(Collections.singletonMap("some", "value"))
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertTrue(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaders(context).containsKey("some"));
        Assert.assertTrue(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaders(context).containsKey("operation"));
        Assert.assertTrue(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaders(context).containsKey("foo"));

        action = (ReceiveMessageAction) test.getActions().get(1);
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
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .header("<Header><Name>operation</Name><Value>foo</Value></Header>");

                receive(messageEndpoint)
                    .message(new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>"))
                    .header("<Header><Name>operation</Name><Value>foo</Value></Header>");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).size(), 1L);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(0), "<Header><Name>operation</Name><Value>foo</Value></Header>");

        action = (ReceiveMessageAction) test.getActions().get(1);
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        Assert.assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).size(), 1L);
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(0), "<Header><Name>operation</Name><Value>foo</Value></Header>");
    }

    @Test
    public void testReceiveBuilderWithMultipleHeaderData() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .header("<Header><Name>operation</Name><Value>foo1</Value></Header>")
                        .header("<Header><Name>operation</Name><Value>foo2</Value></Header>");

                receive(messageEndpoint)
                        .message(new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>"))
                        .header("<Header><Name>operation</Name><Value>foo1</Value></Header>")
                        .header("<Header><Name>operation</Name><Value>foo2</Value></Header>");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).size(), 2L);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(0), "<Header><Name>operation</Name><Value>foo1</Value></Header>");
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(1), "<Header><Name>operation</Name><Value>foo2</Value></Header>");

        action = (ReceiveMessageAction) test.getActions().get(1);
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        Assert.assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).size(), 2L);
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(0), "<Header><Name>operation</Name><Value>foo1</Value></Header>");
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(1), "<Header><Name>operation</Name><Value>foo2</Value></Header>");
    }

    @Test
    public void testReceiveBuilderWithHeaderFragment() {
        reset(referenceResolver);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(Marshaller.class)).thenReturn(Collections.<String, Marshaller>singletonMap("marshaller", new MarshallerAdapter(marshaller)));
        when(referenceResolver.resolve(Marshaller.class)).thenReturn(new MarshallerAdapter(marshaller));

        context.setReferenceResolver(referenceResolver);
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                        .headerFragment(new TestRequest("Hello Citrus!"));
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
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
    public void testReceiveBuilderWithHeaderFragmentExplicitMarshaller() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                        .headerFragment(new TestRequest("Hello Citrus!"), marshaller);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
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
        reset(referenceResolver);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.isResolvable("myMarshaller")).thenReturn(true);
        when(referenceResolver.resolve("myMarshaller", Marshaller.class)).thenReturn(new MarshallerAdapter(marshaller));

        context.setReferenceResolver(referenceResolver);
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                        .headerFragment(new TestRequest("Hello Citrus!"), "myMarshaller");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
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
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .header(resource);

                receive(messageEndpoint)
                    .message(new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>"))
                    .header(resource);
            }
        };

        reset(resource);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("someHeaderData".getBytes()))
                                       .thenReturn(new ByteArrayInputStream("otherHeaderData".getBytes()));
        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).size(), 1L);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(0), "someHeaderData");

        action = (ReceiveMessageAction) test.getActions().get(1);
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        Assert.assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).size(), 1L);
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(0), "otherHeaderData");

    }

    @Test
    public void testReceiveBuilderWithMultipleHeaderResource() throws IOException {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .header("<Header><Name>operation</Name><Value>foo</Value></Header>")
                        .header(resource);

                receive(messageEndpoint)
                        .message(new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>"))
                        .header("<Header><Name>operation</Name><Value>foo</Value></Header>")
                        .header(resource);
            }
        };

        reset(resource);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("someHeaderData".getBytes()))
                                       .thenReturn(new ByteArrayInputStream("otherHeaderData".getBytes()));
        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).size(), 2L);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(0), "<Header><Name>operation</Name><Value>foo</Value></Header>");
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(1), "someHeaderData");

        action = (ReceiveMessageAction) test.getActions().get(1);
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        Assert.assertTrue(action.getMessageBuilder() instanceof StaticMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).size(), 2L);
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(0), "<Header><Name>operation</Name><Value>foo</Value></Header>");
        Assert.assertEquals(((StaticMessageBuilder)action.getMessageBuilder()).buildMessageHeaderData(context).get(1), "otherHeaderData");

    }

    @Test
    public void testReceiveBuilderWithValidator() {
        final PlainTextMessageValidator validator = new PlainTextMessageValidator();

        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                    .messageType(MessageType.PLAINTEXT)
                    .payload("TestMessage")
                    .header("operation", "sayHello")
                    .validator(validator);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
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
    public void testReceiveBuilderWithValidatorName() {
        final PlainTextMessageValidator validator = new PlainTextMessageValidator();

        reset(referenceResolver);

        when(referenceResolver.resolve("plainTextValidator")).thenReturn(validator);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                    .messageType(MessageType.PLAINTEXT)
                    .payload("TestMessage")
                    .header("operation", "sayHello")
                    .validator("plainTextValidator");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
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

        reset(referenceResolver);

        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                        .messageType(MessageType.PLAINTEXT)
                        .payload("TestMessage")
                        .header("operation", "sayHello")
                        .dictionary(dictionary);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
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

        reset(referenceResolver);

        when(referenceResolver.resolve("customDictionary", DataDictionary.class)).thenReturn(dictionary);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                        .messageType(MessageType.PLAINTEXT)
                        .payload("TestMessage")
                        .header("operation", "sayHello")
                        .dictionary("customDictionary");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
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
        final Map<String, String> messageSelector = new HashMap<>();
        messageSelector.put("operation", "sayHello");

        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .selector(messageSelector);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertEquals(action.getMessageSelectorMap(), messageSelector);
    }

    @Test
    public void testReceiveBuilderWithSelectorExpression() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .selector("operation = 'sayHello'");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertTrue(action.getMessageSelectorMap().isEmpty());
        Assert.assertEquals(action.getMessageSelector(), "operation = 'sayHello'");
    }

    @Test
    public void testReceiveBuilderExtractFromPayload() {
        reset(referenceResolver);

        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                    .payload("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                    .extractFromPayload("/TestRequest/Message", "text")
                    .extractFromPayload("/TestRequest/Message/@lang", "language");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertEquals(action.getVariableExtractors().size(), 2);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof XpathPayloadVariableExtractor);
        Assert.assertTrue(action.getVariableExtractors().get(1) instanceof XpathPayloadVariableExtractor);
        Assert.assertTrue(((XpathPayloadVariableExtractor)action.getVariableExtractors().get(0)).getXpathExpressions().containsKey("/TestRequest/Message"));
        Assert.assertTrue(((XpathPayloadVariableExtractor)action.getVariableExtractors().get(1)).getXpathExpressions().containsKey("/TestRequest/Message/@lang"));

    }

    @Test
    public void testReceiveBuilderExtractJsonPathFromPayload() {
        reset(referenceResolver);

        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                        .messageType(MessageType.JSON)
                        .payload("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                        .extractFromPayload("$.text", "text")
                        .extractFromPayload("$.person", "person");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.JSON.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertEquals(action.getVariableExtractors().size(), 2);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof JsonPathVariableExtractor);
        Assert.assertTrue(action.getVariableExtractors().get(1) instanceof JsonPathVariableExtractor);
        Assert.assertTrue(((JsonPathVariableExtractor)action.getVariableExtractors().get(0)).getJsonPathExpressions().containsKey("$.text"));
        Assert.assertTrue(((JsonPathVariableExtractor)action.getVariableExtractors().get(1)).getJsonPathExpressions().containsKey("$.person"));

    }

    @Test
    public void testReceiveBuilderExtractFromHeader() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                    .payload("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                    .extractFromHeader("operation", "ops")
                    .extractFromHeader("requestId", "id");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertEquals(action.getVariableExtractors().size(), 2);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        Assert.assertTrue(action.getVariableExtractors().get(1) instanceof MessageHeaderVariableExtractor);
        Assert.assertTrue(((MessageHeaderVariableExtractor)action.getVariableExtractors().get(0)).getHeaderMappings().containsKey("operation"));
        Assert.assertTrue(((MessageHeaderVariableExtractor)action.getVariableExtractors().get(1)).getHeaderMappings().containsKey("requestId"));
    }

    @Test
    public void testReceiveBuilderExtractCombined() {
        reset(referenceResolver);

        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                    .payload("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                    .extractFromHeader("operation", "ops")
                    .extractFromHeader("requestId", "id")
                    .extractFromPayload("/TestRequest/Message", "text")
                    .extractFromPayload("/TestRequest/Message/@lang", "language");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertEquals(action.getVariableExtractors().size(), 4);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        Assert.assertTrue(action.getVariableExtractors().get(1) instanceof MessageHeaderVariableExtractor);
        Assert.assertTrue(((MessageHeaderVariableExtractor)action.getVariableExtractors().get(0)).getHeaderMappings().containsKey("operation"));
        Assert.assertTrue(((MessageHeaderVariableExtractor)action.getVariableExtractors().get(1)).getHeaderMappings().containsKey("requestId"));

        Assert.assertTrue(action.getVariableExtractors().get(2) instanceof XpathPayloadVariableExtractor);
        Assert.assertTrue(action.getVariableExtractors().get(3) instanceof XpathPayloadVariableExtractor);
        Assert.assertTrue(((XpathPayloadVariableExtractor)action.getVariableExtractors().get(2)).getXpathExpressions().containsKey("/TestRequest/Message"));
        Assert.assertTrue(((XpathPayloadVariableExtractor)action.getVariableExtractors().get(3)).getXpathExpressions().containsKey("/TestRequest/Message/@lang"));

    }

    @Test
    public void testReceiveBuilderWithValidationProcessor() {
        final ValidationProcessor processor = Mockito.mock(ValidationProcessor.class);

        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                    .messageType(MessageType.PLAINTEXT)
                    .payload("TestMessage")
                    .header("operation", "sayHello")
                    .validationCallback(processor);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageType(), MessageType.PLAINTEXT.name());
        Assert.assertEquals(action.getValidationProcessor(), processor);

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "TestMessage");
        Assert.assertTrue(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaders(context).containsKey("operation"));
    }

    @Test
    public void testReceiveBuilderWithValidationScript() {
        final GroovyJsonMessageValidator validator = new GroovyJsonMessageValidator();

        reset(referenceResolver);

        when(referenceResolver.resolve("groovyMessageValidator")).thenReturn(validator);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                    .messageType(MessageType.JSON)
                    .validateScript("assert true")
                    .validator("groovyMessageValidator");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
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
        Assert.assertEquals(validationContext.getValidationScript(), "assert true");
        Assert.assertNull(validationContext.getValidationScriptResourcePath());

    }

    @Test
    public void testReceiveBuilderWithValidationScriptResource() throws IOException {
        final GroovyJsonMessageValidator validator = new GroovyJsonMessageValidator();

        reset(referenceResolver);

        when(referenceResolver.resolve("groovyMessageValidator")).thenReturn(validator);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                    .messageType(MessageType.JSON)
                    .validateScript(new ClassPathResource("org/citrusframework/citrus/dsl/runner/validation.groovy"))
                    .validator("groovyMessageValidator");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
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

        reset(referenceResolver);

        when(referenceResolver.resolve("groovyMessageValidator")).thenReturn(validator);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                        .messageType(MessageType.JSON)
                        .validateScriptResource("/path/to/file/File.groovy")
                        .validator("groovyMessageValidator");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
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
        Assert.assertEquals(validationContext.getValidationScriptResourcePath(), "/path/to/file/File.groovy");

    }

    @Test
    public void testReceiveBuilderWithValidationScriptAndHeader() {
        final GroovyJsonMessageValidator validator = new GroovyJsonMessageValidator();

        reset(referenceResolver);

        when(referenceResolver.resolve("groovyMessageValidator")).thenReturn(validator);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                    .messageType(MessageType.JSON)
                    .validateScript("assert true")
                    .validator("groovyMessageValidator")
                    .header("operation", "sayHello");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
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
        Assert.assertEquals(validationContext.getValidationScript(), "assert true");
        Assert.assertNull(validationContext.getValidationScriptResourcePath());

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertTrue(((DefaultMessageBuilder)action.getMessageBuilder()).buildMessageHeaders(context).containsKey("operation"));

    }

    @Test
    public void testReceiveBuilderWithNamespaceValidation() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                    .payload("<TestRequest xmlns:pfx=\"http://www.consol.de/schemas/test\"><Message>Hello World!</Message></TestRequest>")
                    .validateNamespace("pfx", "http://www.consol.de/schemas/test");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
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
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                    .payload("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                    .validate("Foo.operation", "foo")
                    .validate("Foo.message", "control");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
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
        Assert.assertEquals(validationContext.getXpathExpressions().get("Foo.operation"), "foo");
        Assert.assertEquals(validationContext.getXpathExpressions().get("Foo.message"), "control");
    }

    @Test
    public void testReceiveBuilderWithJsonPathExpressions() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                        .messageType(MessageType.JSON)
                        .payload("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\",\"active\": true}, \"index\":5, \"id\":\"x123456789x\"}")
                        .validate("$.person.name", "foo")
                        .validate("$.person.active", true)
                        .validate("$.id", containsString("123456789"))
                        .validate("$.text", "Hello World!")
                        .validate("$.index", 5);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
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
        Assert.assertEquals(validationContext.getJsonPathExpressions().get("$.person.name"), "foo");
        Assert.assertEquals(validationContext.getJsonPathExpressions().get("$.person.active"), true);
        Assert.assertEquals(validationContext.getJsonPathExpressions().get("$.text"), "Hello World!");
        Assert.assertEquals(validationContext.getJsonPathExpressions().get("$.index"), 5);
        Assert.assertEquals(validationContext.getJsonPathExpressions().get("$.id").getClass(), StringContains.class);
    }

    @Test
    public void testReceiveBuilderWithJsonPathExpressionsInvalidMessageType() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                        .messageType(MessageType.XML)
                        .payload("{\"text\":\"Hello World!\"}")
                        .validate("$.text", "Hello World!");
            }
        };

        builder.configure();
    }

    @Test
    public void testReceiveBuilderWithIgnoreElementsXpath() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                    .payload("<TestRequest><Message>?</Message></TestRequest>")
                    .ignore("TestRequest.Message");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
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
    public void testReceiveBuilderWithIgnoreElementsJsonPath() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                        .messageType(MessageType.JSON)
                        .payload("{\"text\":\"Hello World!\", \"person\": {\"name\": \"Penny\", age: 25}}")
                        .ignore("$..text")
                        .ignore("$.person.age");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
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
        Assert.assertEquals(((DefaultMessageBuilder) action.getMessageBuilder()).buildMessagePayload(context, action.getMessageType()), "{\"text\":\"Hello World!\", \"person\": {\"name\": \"Penny\", age: 25}}");
        Assert.assertEquals(validationContext.getIgnoreExpressions().size(), 2L);
        Assert.assertTrue(validationContext.getIgnoreExpressions().contains("$..text"));
        Assert.assertTrue(validationContext.getIgnoreExpressions().contains("$.person.age"));
    }

    @Test
    public void testReceiveBuilderWithSchema() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                    .payload("<TestRequest><Message>?</Message></TestRequest>")
                    .xsd("testSchema");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
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
        Assert.assertEquals(validationContext.getSchema(), "testSchema");
    }

    @Test
    public void testReceiveBuilderWithSchemaRepository() {
        MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                    .payload("<TestRequest><Message>?</Message></TestRequest>")
                    .xsdSchemaRepository("testSchemaRepository");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveMessageAction.class);

        ReceiveMessageAction action = (ReceiveMessageAction) test.getActions().get(0);
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
        Assert.assertEquals(validationContext.getSchemaRepository(), "testSchemaRepository");
    }
}
