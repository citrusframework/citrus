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

import org.citrusframework.citrus.TestCase;
import org.citrusframework.citrus.actions.SendMessageAction;
import org.citrusframework.citrus.container.SequenceAfterTest;
import org.citrusframework.citrus.container.SequenceBeforeTest;
import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.dsl.TestRequest;
import org.citrusframework.citrus.dsl.UnitTestSupport;
import org.citrusframework.citrus.endpoint.Endpoint;
import org.citrusframework.citrus.message.DefaultMessage;
import org.citrusframework.citrus.message.Message;
import org.citrusframework.citrus.message.MessageHeaders;
import org.citrusframework.citrus.message.MessageType;
import org.citrusframework.citrus.report.TestActionListeners;
import org.citrusframework.citrus.spi.ReferenceResolver;
import org.citrusframework.citrus.validation.builder.DefaultMessageBuilder;
import org.citrusframework.citrus.validation.builder.StaticMessageBuilder;
import org.citrusframework.citrus.validation.json.JsonPathMessageProcessor;
import org.citrusframework.citrus.validation.json.JsonPathVariableExtractor;
import org.citrusframework.citrus.validation.xml.XpathMessageProcessor;
import org.citrusframework.citrus.validation.xml.XpathPayloadVariableExtractor;
import org.citrusframework.citrus.variable.MessageHeaderVariableExtractor;
import org.citrusframework.citrus.variable.dictionary.DataDictionary;
import org.citrusframework.citrus.variable.dictionary.xml.NodeMappingDataDictionary;
import org.citrusframework.citrus.xml.Marshaller;
import org.citrusframework.citrus.xml.MarshallerAdapter;
import org.mockito.Mockito;
import org.springframework.core.io.Resource;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;


/**
 * @author Christoph Deppisch
 */
public class SendMessageTestDesignerTest extends UnitTestSupport {

    private Endpoint messageEndpoint = Mockito.mock(Endpoint.class);

    private ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);
    private Resource resource = Mockito.mock(Resource.class);

    private XStreamMarshaller marshaller = new XStreamMarshaller();

    @BeforeClass
    public void prepareMarshaller() {
        marshaller.getXStream().processAnnotations(TestRequest.class);
    }

    @Test
    public void testSendBuilderWithMessageInstance() {
        final MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                send(messageEndpoint)
                    .message(new DefaultMessage("Foo").setHeader("operation", "foo"))
                        .header("additional", "additionalValue");
            }
        };

        builder.configure();

        final TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = (SendMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageBuilder.class);

        final StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(String.class), "Foo");
        Assert.assertEquals(messageBuilder.getMessage().getHeader("operation"), "foo");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 2L);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("additional"), "additionalValue");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("operation"), "foo");
    }

    @Test
    public void testSendBuilderWithObjectMessageInstance() {
        final Message message = new DefaultMessage(10).setHeader("operation", "foo");
        final MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                send(messageEndpoint)
                    .message(message);
            }
        };

        builder.configure();

        final TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = (SendMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageBuilder.class);

        final StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(), 10);
        Assert.assertEquals(messageBuilder.getMessage().getHeaders().size(), message.getHeaders().size());
        Assert.assertEquals(messageBuilder.getMessage().getHeader(MessageHeaders.ID), message.getHeader(MessageHeaders.ID));
        Assert.assertEquals(messageBuilder.getMessage().getHeader("operation"), "foo");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 1L);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("operation"), "foo");

        final Message constructed = messageBuilder.build(new TestContext(), MessageType.PLAINTEXT.name());
        Assert.assertEquals(constructed.getHeaders().size(), message.getHeaders().size() + 1);
        Assert.assertEquals(constructed.getHeader("operation"), "foo");
        Assert.assertEquals(constructed.getType(), MessageType.PLAINTEXT.name());
        Assert.assertNotEquals(constructed.getHeader(MessageHeaders.ID), message.getHeader(MessageHeaders.ID));
    }

    @Test
    public void testSendBuilderWithObjectMessageInstanceAdditionalHeader() {
        final Message message = new DefaultMessage(10).setHeader("operation", "foo");
        final MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                send(messageEndpoint)
                    .message(message)
                    .header("additional", "new");
            }
        };

        builder.configure();

        final TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = (SendMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageBuilder.class);

        final StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(), 10);
        Assert.assertEquals(messageBuilder.getMessage().getHeaders().size(), message.getHeaders().size());
        Assert.assertEquals(messageBuilder.getMessage().getHeader(MessageHeaders.ID), message.getHeader(MessageHeaders.ID));
        Assert.assertEquals(messageBuilder.getMessage().getHeader("operation"), "foo");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 2L);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("additional"), "new");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("operation"), "foo");

        final Message constructed = messageBuilder.build(new TestContext(), MessageType.PLAINTEXT.name());
        Assert.assertEquals(constructed.getHeaders().size(), message.getHeaders().size() + 2);
        Assert.assertEquals(constructed.getHeader("operation"), "foo");
        Assert.assertEquals(constructed.getHeader("additional"), "new");
        Assert.assertEquals(constructed.getType(), MessageType.PLAINTEXT.name());
    }

    @Test
    public void testSendBuilderWithPayloadModel() {
        reset(referenceResolver);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(Marshaller.class)).thenReturn(Collections.singletonMap("marshaller", new MarshallerAdapter(marshaller)));
        when(referenceResolver.resolve(Marshaller.class)).thenReturn(new MarshallerAdapter(marshaller));

        context.setReferenceResolver(referenceResolver);
        final MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                send(messageEndpoint)
                        .payloadModel(new TestRequest("Hello Citrus!"));
            }
        };

        builder.configure();

        final TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = (SendMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), DefaultMessageBuilder.class);

        final DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0L);

    }

    @Test
    public void testSendBuilderWithPayloadModelExplicitMarshaller() {
        final MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                send(messageEndpoint)
                    .payload(new TestRequest("Hello Citrus!"), marshaller);
            }
        };

        builder.configure();

        final TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = (SendMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), DefaultMessageBuilder.class);

        final DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0L);

    }

    @Test
    public void testSendBuilderWithPayloadModelExplicitMarshallerName() {
        reset(referenceResolver);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.isResolvable("myMarshaller")).thenReturn(true);
        when(referenceResolver.resolve("myMarshaller", Marshaller.class)).thenReturn(new MarshallerAdapter(marshaller));

        context.setReferenceResolver(referenceResolver);
        final MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                send(messageEndpoint)
                        .payload(new TestRequest("Hello Citrus!"), "myMarshaller");
            }
        };

        builder.configure();

        final TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = (SendMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), DefaultMessageBuilder.class);

        final DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0L);

    }

    @Test
    public void testSendBuilderWithPayloadData() {
        final MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                send(messageEndpoint)
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
            }
        };

        builder.configure();

        final TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = (SendMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), DefaultMessageBuilder.class);

        final DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0L);
    }

    @Test
    public void testSendBuilderWithPayloadResource() throws IOException {
        final MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                send(messageEndpoint)
                    .payload(resource);
            }
        };

        reset(resource);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("somePayloadData".getBytes()));
        builder.configure();

        final TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = (SendMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), DefaultMessageBuilder.class);

        final DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "somePayloadData");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0L);

    }

    @Test
    public void testSendBuilderWithEndpointName() {
        reset(referenceResolver);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        final MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                send("fooMessageEndpoint")
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
            }
        };

        builder.configure();

        final TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = (SendMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");
        Assert.assertEquals(action.getEndpointUri(), "fooMessageEndpoint");

    }

    @Test
    public void testSendBuilderWithHeaders() {
        final MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                send(messageEndpoint)
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .headers(Collections.singletonMap("some", "value"))
                    .header("operation", "foo")
                    .header("language", "eng");
            }
        };

        builder.configure();

        final TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = (SendMessageAction) test.getActions().get(0);
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
        final MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                send(messageEndpoint)
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .header("<Header><Name>operation</Name><Value>foo</Value></Header>");

                send(messageEndpoint)
                    .message(new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>"))
                    .header("<Header><Name>operation</Name><Value>foo</Value></Header>");
            }
        };

        builder.configure();

        final TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), SendMessageAction.class);

        SendMessageAction action = (SendMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), DefaultMessageBuilder.class);

        final DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0L);
        Assert.assertEquals(messageBuilder.buildMessageHeaderData(context).size(), 1L);
        Assert.assertEquals(messageBuilder.buildMessageHeaderData(context).get(0), "<Header><Name>operation</Name><Value>foo</Value></Header>");

        action = (SendMessageAction) test.getActions().get(1);
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
        final MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                send(messageEndpoint)
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .header("<Header><Name>operation</Name><Value>foo1</Value></Header>")
                        .header("<Header><Name>operation</Name><Value>foo2</Value></Header>");

                send(messageEndpoint)
                        .message(new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>"))
                        .header("<Header><Name>operation</Name><Value>foo1</Value></Header>")
                        .header("<Header><Name>operation</Name><Value>foo2</Value></Header>");
            }
        };

        builder.configure();

        final TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), SendMessageAction.class);

        SendMessageAction action = (SendMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), DefaultMessageBuilder.class);

        final DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0L);
        Assert.assertEquals(messageBuilder.buildMessageHeaderData(context).size(), 2L);
        Assert.assertEquals(messageBuilder.buildMessageHeaderData(context).get(0), "<Header><Name>operation</Name><Value>foo1</Value></Header>");
        Assert.assertEquals(messageBuilder.buildMessageHeaderData(context).get(1), "<Header><Name>operation</Name><Value>foo2</Value></Header>");

        action = (SendMessageAction) test.getActions().get(1);
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
        final MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                send(messageEndpoint)
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .header(resource);

                send(messageEndpoint)
                    .message(new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>"))
                    .header(resource);
            }
        };

        reset(resource);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("someHeaderData".getBytes()))
                                       .thenReturn(new ByteArrayInputStream("otherHeaderData".getBytes()));
        builder.configure();

        final TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), SendMessageAction.class);

        SendMessageAction action = (SendMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), DefaultMessageBuilder.class);

        final DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0L);
        Assert.assertEquals(messageBuilder.buildMessageHeaderData(context).size(), 1L);
        Assert.assertEquals(messageBuilder.buildMessageHeaderData(context).get(0), "someHeaderData");

        action = (SendMessageAction) test.getActions().get(1);
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageBuilder.class);

        final StaticMessageBuilder staticMessageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(staticMessageBuilder.getMessage().getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(staticMessageBuilder.buildMessageHeaders(context).size(), 0L);
        Assert.assertEquals(staticMessageBuilder.buildMessageHeaderData(context).size(), 1L);
        Assert.assertEquals(staticMessageBuilder.buildMessageHeaderData(context).get(0), "otherHeaderData");

    }

    @Test
    public void testSendBuilderExtractFromPayload() {
        final MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                send(messageEndpoint)
                    .payload("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                    .extractFromPayload("/TestRequest/Message", "text")
                    .extractFromPayload("/TestRequest/Message/@lang", "language");
            }
        };

        builder.configure();

        final TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = (SendMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertEquals(action.getVariableExtractors().size(), 2);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof XpathPayloadVariableExtractor);
        Assert.assertTrue(action.getVariableExtractors().get(1) instanceof XpathPayloadVariableExtractor);
        Assert.assertTrue(((XpathPayloadVariableExtractor)action.getVariableExtractors().get(0)).getXpathExpressions().containsKey("/TestRequest/Message"));
        Assert.assertTrue(((XpathPayloadVariableExtractor)action.getVariableExtractors().get(1)).getXpathExpressions().containsKey("/TestRequest/Message/@lang"));
    }

    @Test
    public void testSendBuilderExtractJsonPathFromPayload() {
        final MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                send(messageEndpoint)
                        .messageType(MessageType.JSON)
                        .payload("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                        .extractFromPayload("$.text", "text")
                        .extractFromPayload("$.person", "language");
            }
        };

        builder.configure();

        final TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = (SendMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertEquals(action.getVariableExtractors().size(), 2);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof JsonPathVariableExtractor);
        Assert.assertTrue(action.getVariableExtractors().get(1) instanceof JsonPathVariableExtractor);
        Assert.assertTrue(((JsonPathVariableExtractor)action.getVariableExtractors().get(0)).getJsonPathExpressions().containsKey("$.text"));
        Assert.assertTrue(((JsonPathVariableExtractor)action.getVariableExtractors().get(1)).getJsonPathExpressions().containsKey("$.person"));
    }

    @Test
    public void testSendBuilderExtractFromHeader() {
        final MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                send(messageEndpoint)
                    .payload("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                    .extractFromHeader("operation", "ops")
                    .extractFromHeader("requestId", "id");
            }
        };

        builder.configure();

        final TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = (SendMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertEquals(action.getVariableExtractors().size(), 2);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        Assert.assertTrue(action.getVariableExtractors().get(1) instanceof MessageHeaderVariableExtractor);
        Assert.assertTrue(((MessageHeaderVariableExtractor)action.getVariableExtractors().get(0)).getHeaderMappings().containsKey("operation"));
        Assert.assertTrue(((MessageHeaderVariableExtractor)action.getVariableExtractors().get(1)).getHeaderMappings().containsKey("requestId"));
    }

    @Test
    public void testXpathSupport() {
        final MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                send(messageEndpoint)
                        .payload("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                        .xpath("/TestRequest/Message", "Hello World!");
            }
        };

        builder.configure();

        final TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = (SendMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(action.getMessageProcessors().size(), 1);
        Assert.assertTrue(action.getMessageProcessors().get(0) instanceof XpathMessageProcessor);
        Assert.assertEquals(((XpathMessageProcessor)action.getMessageProcessors().get(0)).getXPathExpressions().get("/TestRequest/Message"), "Hello World!");
    }

    @Test
    public void testJsonPathSupport() {
        final MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                send(messageEndpoint)
                        .payload("{ \"TestRequest\": { \"Message\": \"?\" }}")
                        .jsonPath("$.TestRequest.Message", "Hello World!");
            }
        };

        builder.configure();

        final TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = (SendMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(action.getMessageProcessors().size(), 1);
        Assert.assertTrue(action.getMessageProcessors().get(0) instanceof JsonPathMessageProcessor);
        Assert.assertEquals(((JsonPathMessageProcessor)action.getMessageProcessors().get(0)).getJsonPathExpressions().get("$.TestRequest.Message"), "Hello World!");
    }

    @Test
    public void testSendBuilderWithDictionary() {
        final DataDictionary<?> dictionary = new NodeMappingDataDictionary();

        reset(referenceResolver);

        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        final MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                send(messageEndpoint)
                        .payload("{ \"TestRequest\": { \"Message\": \"?\" }}")
                        .dictionary(dictionary);
            }
        };

        builder.configure();

        final TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = (SendMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getDataDictionary(), dictionary);
    }

    @Test
    public void testSendBuilderWithDictionaryName() {
        final DataDictionary<?> dictionary = new NodeMappingDataDictionary();

        reset(referenceResolver);

        when(referenceResolver.resolve("customDictionary", DataDictionary.class)).thenReturn(dictionary);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        final MockTestDesigner builder = new MockTestDesigner(context) {
            @Override
            public void configure() {
                send(messageEndpoint)
                        .payload("TestMessage")
                        .header("operation", "sayHello")
                        .dictionary("customDictionary");
            }
        };

        builder.configure();

        final TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = (SendMessageAction) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getDataDictionary(), dictionary);
    }

}
