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

package com.consol.citrus.dsl.runner;

import com.consol.citrus.TestCase;
import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.container.SequenceAfterTest;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.TestRequest;
import com.consol.citrus.dsl.builder.BuilderSupport;
import com.consol.citrus.dsl.builder.SendMessageBuilder;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.message.*;
import com.consol.citrus.messaging.Producer;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.builder.*;
import com.consol.citrus.validation.json.JsonPathMessageConstructionInterceptor;
import com.consol.citrus.validation.json.JsonPathVariableExtractor;
import com.consol.citrus.validation.xml.XpathMessageConstructionInterceptor;
import com.consol.citrus.variable.MessageHeaderVariableExtractor;
import com.consol.citrus.validation.xml.XpathPayloadVariableExtractor;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.util.StringUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public class SendMessageTestRunnerTest extends AbstractTestNGUnitTest {
    
    private Endpoint messageEndpoint = EasyMock.createMock(Endpoint.class);
    private Producer messageProducer = EasyMock.createMock(Producer.class);
    private ApplicationContext applicationContextMock = EasyMock.createMock(ApplicationContext.class);
    private Resource resource = EasyMock.createMock(Resource.class);

    private XStreamMarshaller marshaller = new XStreamMarshaller();

    @BeforeClass
    public void prepareMarshaller() {
        marshaller.getXStream().processAnnotations(TestRequest.class);
    }

    @Test
    public void testSendBuilderWithMessageInstance() {
        reset(messageEndpoint, messageProducer);
        expect(messageEndpoint.createProducer()).andReturn(messageProducer).once();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        messageProducer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                Message message = (Message) getCurrentArguments()[0];
                Assert.assertEquals(message.getPayload(String.class), "Foo");
                Assert.assertNotNull(message.getHeader("operation"));
                Assert.assertEquals(message.getHeader("operation"), "foo");
                return null;
            }
        }).atLeastOnce();
        replay(messageEndpoint, messageProducer);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                send(new BuilderSupport<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .message(new DefaultMessage("Foo").setHeader("operation", "foo"));
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "Foo");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 1L);
        Assert.assertEquals(messageBuilder.getMessageHeaders().get("operation"), "foo");

        verify(messageEndpoint, messageProducer);
    }

    @Test
    public void testSendBuilderWithObjectMessageInstance() {
        reset(messageEndpoint, messageProducer);
        expect(messageEndpoint.createProducer()).andReturn(messageProducer).once();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        messageProducer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                Message message = (Message) getCurrentArguments()[0];
                Assert.assertEquals(message.getPayload(Integer.class), new Integer(10));
                Assert.assertNotNull(message.getHeader("operation"));
                Assert.assertEquals(message.getHeader("operation"), "foo");
                return null;
            }
        }).atLeastOnce();
        replay(messageEndpoint, messageProducer);

        final Message message = new DefaultMessage(new Integer(10)).setHeader("operation", "foo");
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                send(new BuilderSupport<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .message(message);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageContentBuilder.class);

        StaticMessageContentBuilder messageBuilder = (StaticMessageContentBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(), 10);
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        Assert.assertEquals(messageBuilder.getMessage().copyHeaders().size(), message.copyHeaders().size());
        Assert.assertEquals(messageBuilder.getMessage().getHeader(MessageHeaders.ID), message.getHeader(MessageHeaders.ID));
        Assert.assertEquals(messageBuilder.getMessage().getHeader("operation"), "foo");

        Message constructed = messageBuilder.buildMessageContent(new TestContext(), MessageType.PLAINTEXT.name());
        Assert.assertEquals(constructed.copyHeaders().size(), message.copyHeaders().size());
        Assert.assertEquals(constructed.getHeader("operation"), "foo");
        Assert.assertEquals(constructed.getHeader(MessageHeaders.ID), message.getHeader(MessageHeaders.ID));

        verify(messageEndpoint, messageProducer);
    }

    @Test
    public void testSendBuilderWithObjectMessageInstanceAdditionalHeader() {
        reset(messageEndpoint, messageProducer);
        expect(messageEndpoint.createProducer()).andReturn(messageProducer).once();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        messageProducer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                Message message = (Message) getCurrentArguments()[0];
                Assert.assertEquals(message.getPayload(Integer.class), new Integer(10));
                Assert.assertNotNull(message.getHeader("operation"));
                Assert.assertEquals(message.getHeader("operation"), "foo");
                Assert.assertNotNull(message.getHeader("additional"));
                Assert.assertEquals(message.getHeader("additional"), "new");
                return null;
            }
        }).atLeastOnce();
        replay(messageEndpoint, messageProducer);

        final Message message = new DefaultMessage(new Integer(10)).setHeader("operation", "foo");
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                send(new BuilderSupport<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .message(message)
                                .header("additional", "new");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageContentBuilder.class);

        StaticMessageContentBuilder messageBuilder = (StaticMessageContentBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(), 10);
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 1L);
        Assert.assertEquals(messageBuilder.getMessageHeaders().get("additional"), "new");
        Assert.assertEquals(messageBuilder.getMessage().copyHeaders().size(), message.copyHeaders().size());
        Assert.assertEquals(messageBuilder.getMessage().getHeader(MessageHeaders.ID), message.getHeader(MessageHeaders.ID));
        Assert.assertEquals(messageBuilder.getMessage().getHeader("operation"), "foo");

        Message constructed = messageBuilder.buildMessageContent(new TestContext(), MessageType.PLAINTEXT.name());
        Assert.assertEquals(constructed.copyHeaders().size(), message.copyHeaders().size() + 1);
        Assert.assertEquals(constructed.getHeader("operation"), "foo");
        Assert.assertEquals(constructed.getHeader("additional"), "new");

        verify(messageEndpoint, messageProducer);
    }

    @Test
    public void testSendBuilderWithPayloadModel() {
        reset(applicationContextMock, messageEndpoint, messageProducer);
        expect(messageEndpoint.createProducer()).andReturn(messageProducer).once();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        messageProducer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                Message message = (Message) getCurrentArguments()[0];
                Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
                return null;
            }
        }).atLeastOnce();

        expect(applicationContextMock.getBean(TestContext.class)).andReturn(applicationContext.getBean(TestContext.class)).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();
        expect(applicationContextMock.getBean(Marshaller.class)).andReturn(marshaller).once();
        replay(applicationContextMock, messageEndpoint, messageProducer);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock) {
            @Override
            public void execute() {
                send(new BuilderSupport<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .payloadModel(new TestRequest("Hello Citrus!"));
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);

        verify(applicationContextMock, messageEndpoint, messageProducer);
    }

    @Test
    public void testSendBuilderWithPayloadModelExplicitMarshaller() {
        reset(messageEndpoint, messageProducer);
        expect(messageEndpoint.createProducer()).andReturn(messageProducer).once();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        messageProducer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                Message message = (Message) getCurrentArguments()[0];
                Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
                return null;
            }
        }).atLeastOnce();

        replay(messageEndpoint, messageProducer);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                send(new BuilderSupport<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .payload(new TestRequest("Hello Citrus!"), marshaller);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);

        verify(messageEndpoint, messageProducer);
    }

    @Test
    public void testSendBuilderWithPayloadModelExplicitMarshallerName() {
        reset(applicationContextMock, messageEndpoint, messageProducer);
        expect(messageEndpoint.createProducer()).andReturn(messageProducer).once();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        messageProducer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                Message message = (Message) getCurrentArguments()[0];
                Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
                return null;
            }
        }).atLeastOnce();

        expect(applicationContextMock.getBean(TestContext.class)).andReturn(applicationContext.getBean(TestContext.class)).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();
        expect(applicationContextMock.getBean("myMarshaller", Marshaller.class)).andReturn(marshaller).once();
        replay(applicationContextMock, messageEndpoint, messageProducer);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock) {
            @Override
            public void execute() {
                send(new BuilderSupport<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .payload(new TestRequest("Hello Citrus!"), "myMarshaller");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);

        verify(applicationContextMock, messageEndpoint, messageProducer);
    }
    
    @Test
    public void testSendBuilderWithPayloadData() {
        reset(messageEndpoint, messageProducer);
        expect(messageEndpoint.createProducer()).andReturn(messageProducer).once();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        messageProducer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                Message message = (Message) getCurrentArguments()[0];
                Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
                return null;
            }
        }).atLeastOnce();

        replay(messageEndpoint, messageProducer);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                send(new BuilderSupport<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);
        
        SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");
        
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);

        verify(messageEndpoint, messageProducer);
    }

    @Test
    public void testSendBuilderWithPayloadResource() throws IOException {
        reset(resource, messageEndpoint, messageProducer);
        expect(messageEndpoint.createProducer()).andReturn(messageProducer).once();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        messageProducer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                Message message = (Message) getCurrentArguments()[0];
                Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
                return null;
            }
        }).atLeastOnce();

        expect(resource.getInputStream()).andReturn(new ByteArrayInputStream("<TestRequest><Message>Hello World!</Message></TestRequest>".getBytes())).once();
        replay(resource, messageEndpoint, messageProducer);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                send(new BuilderSupport<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .payload(resource);
                    }
                });
            }
        };


        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);
        
        SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");
        
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);
        
        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        
        verify(resource, messageEndpoint, messageProducer);
    }
    
    @Test
    public void testSendBuilderWithEndpointName() {
        reset(applicationContextMock, messageEndpoint, messageProducer);
        expect(messageEndpoint.createProducer()).andReturn(messageProducer).once();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        messageProducer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                Message message = (Message) getCurrentArguments()[0];
                Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
                return null;
            }
        }).atLeastOnce();

        expect(applicationContextMock.getBean(TestContext.class)).andReturn(applicationContext.getBean(TestContext.class)).once();
        expect(applicationContextMock.getBean("fooMessageEndpoint", Endpoint.class)).andReturn(messageEndpoint).atLeastOnce();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();
        replay(applicationContextMock, messageEndpoint, messageProducer);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock) {
            @Override
            public void execute() {
                send(new BuilderSupport<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint("fooMessageEndpoint")
                                .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");
        Assert.assertEquals(action.getEndpointUri(), "fooMessageEndpoint");

        verify(applicationContextMock, messageEndpoint, messageProducer);
    }
    
    @Test
    public void testSendBuilderWithHeaders() {
        reset(messageEndpoint, messageProducer);
        expect(messageEndpoint.createProducer()).andReturn(messageProducer).once();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        messageProducer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                Message message = (Message) getCurrentArguments()[0];
                Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
                Assert.assertNotNull(message.getHeader("operation"));
                Assert.assertEquals(message.getHeader("operation"), "foo");
                Assert.assertNotNull(message.getHeader("language"));
                Assert.assertEquals(message.getHeader("language"), "eng");
                return null;
            }
        }).atLeastOnce();

        replay(messageEndpoint, messageProducer);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                send(new BuilderSupport<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .header("operation", "foo")
                                .header("language", "eng");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 2L);
        Assert.assertEquals(messageBuilder.getMessageHeaders().get("operation"), "foo");
        Assert.assertEquals(messageBuilder.getMessageHeaders().get("language"), "eng");

        verify(messageEndpoint, messageProducer);
    }
    
    @Test
    public void testSendBuilderWithHeaderData() {
        reset(messageEndpoint, messageProducer);
        expect(messageEndpoint.createProducer()).andReturn(messageProducer).times(2);
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        messageProducer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                Message message = (Message) getCurrentArguments()[0];
                Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
                Assert.assertNotNull(message.getHeaderData());
                Assert.assertEquals(message.getHeaderData().size(), 1L);
                Assert.assertEquals(message.getHeaderData().get(0), "<Header><Name>operation</Name><Value>foo</Value></Header>");
                return null;
            }
        }).atLeastOnce();

        replay(messageEndpoint, messageProducer);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                send(new BuilderSupport<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                             .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                             .header("<Header><Name>operation</Name><Value>foo</Value></Header>");
                    }
                });
                
                send(new BuilderSupport<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                            .message(new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>"))
                            .header("<Header><Name>operation</Name><Value>foo</Value></Header>");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), SendMessageAction.class);
        
        SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");
        
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        Assert.assertEquals(messageBuilder.getHeaderData().size(), 1L);
        Assert.assertEquals(messageBuilder.getHeaderData().get(0), "<Header><Name>operation</Name><Value>foo</Value></Header>");
        Assert.assertEquals(messageBuilder.getHeaderResources().size(), 0L);

        action = ((SendMessageAction)test.getActions().get(1));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        Assert.assertEquals(messageBuilder.getHeaderData().size(), 1L);
        Assert.assertEquals(messageBuilder.getHeaderData().get(0), "<Header><Name>operation</Name><Value>foo</Value></Header>");
        Assert.assertEquals(messageBuilder.getHeaderResources().size(), 0L);

        verify(messageEndpoint, messageProducer);
    }

    @Test
    public void testSendBuilderWithMultipleHeaderData() {
        reset(messageEndpoint, messageProducer);
        expect(messageEndpoint.createProducer()).andReturn(messageProducer).times(2);
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        messageProducer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                Message message = (Message) getCurrentArguments()[0];
                Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
                Assert.assertNotNull(message.getHeaderData());
                Assert.assertEquals(message.getHeaderData().size(), 2L);
                Assert.assertEquals(message.getHeaderData().get(0), "<Header><Name>operation</Name><Value>foo1</Value></Header>");
                Assert.assertEquals(message.getHeaderData().get(1), "<Header><Name>operation</Name><Value>foo2</Value></Header>");
                return null;
            }
        }).atLeastOnce();

        replay(messageEndpoint, messageProducer);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                send(new BuilderSupport<SendMessageBuilder>() {
                @Override
                    public void configure(SendMessageBuilder builder) {
                    builder.endpoint(messageEndpoint)
                             .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                             .header("<Header><Name>operation</Name><Value>foo1</Value></Header>")
                             .header("<Header><Name>operation</Name><Value>foo2</Value></Header>");
                    }
                });

                send(new BuilderSupport<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                            .message(new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>"))
                            .header("<Header><Name>operation</Name><Value>foo1</Value></Header>")
                            .header("<Header><Name>operation</Name><Value>foo2</Value></Header>");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), SendMessageAction.class);

        SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        Assert.assertEquals(messageBuilder.getHeaderData().size(), 2L);
        Assert.assertEquals(messageBuilder.getHeaderData().get(0), "<Header><Name>operation</Name><Value>foo1</Value></Header>");
        Assert.assertEquals(messageBuilder.getHeaderData().get(1), "<Header><Name>operation</Name><Value>foo2</Value></Header>");
        Assert.assertEquals(messageBuilder.getHeaderResources().size(), 0L);

        action = ((SendMessageAction)test.getActions().get(1));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        Assert.assertEquals(messageBuilder.getHeaderData().size(), 2L);
        Assert.assertEquals(messageBuilder.getHeaderData().get(0), "<Header><Name>operation</Name><Value>foo1</Value></Header>");
        Assert.assertEquals(messageBuilder.getHeaderData().get(1), "<Header><Name>operation</Name><Value>foo2</Value></Header>");
        Assert.assertEquals(messageBuilder.getHeaderResources().size(), 0L);

        verify(messageEndpoint, messageProducer);
    }
    
    @Test
    public void testSendBuilderWithHeaderDataResource() throws IOException {
        reset(resource, messageEndpoint, messageProducer);
        expect(messageEndpoint.createProducer()).andReturn(messageProducer).times(2);
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        messageProducer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                Message message = (Message) getCurrentArguments()[0];
                Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
                Assert.assertNotNull(message.getHeaderData());
                Assert.assertEquals(message.getHeaderData().size(), 1L);
                Assert.assertEquals(message.getHeaderData().get(0), "<Header><Name>operation</Name><Value>foo1</Value></Header>");
                return null;
            }
        }).once();

        messageProducer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                Message message = (Message) getCurrentArguments()[0];
                Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
                Assert.assertNotNull(message.getHeaderData());
                Assert.assertEquals(message.getHeaderData().size(), 1L);
                Assert.assertEquals(message.getHeaderData().get(0), "<Header><Name>operation</Name><Value>foo2</Value></Header>");
                return null;
            }
        }).once();

        expect(resource.getInputStream()).andReturn(new ByteArrayInputStream("<Header><Name>operation</Name><Value>foo1</Value></Header>".getBytes())).once();
        expect(resource.getInputStream()).andReturn(new ByteArrayInputStream("<Header><Name>operation</Name><Value>foo2</Value></Header>".getBytes())).once();
        replay(resource, messageEndpoint, messageProducer);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                send(new BuilderSupport<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .header(resource);
                    }
                });

                send(new BuilderSupport<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .message(new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>"))
                                .header(resource);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), SendMessageAction.class);
        
        SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");
        
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        Assert.assertEquals(messageBuilder.getHeaderData().size(), 1L);
        Assert.assertEquals(messageBuilder.getHeaderData().get(0), "<Header><Name>operation</Name><Value>foo1</Value></Header>");
        Assert.assertEquals(messageBuilder.getHeaderResources().size(), 0L);

        action = ((SendMessageAction)test.getActions().get(1));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        Assert.assertEquals(messageBuilder.getHeaderData().size(), 1L);
        Assert.assertEquals(messageBuilder.getHeaderData().get(0), "<Header><Name>operation</Name><Value>foo2</Value></Header>");
        Assert.assertEquals(messageBuilder.getHeaderResources().size(), 0L);

        verify(resource, messageEndpoint, messageProducer);
    }
    
    @Test
    public void testSendBuilderExtractFromPayload() {
        reset(messageEndpoint, messageProducer);
        expect(messageEndpoint.createProducer()).andReturn(messageProducer).once();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        messageProducer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                Message message = (Message) getCurrentArguments()[0];
                Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>");
                return null;
            }
        }).atLeastOnce();

        replay(messageEndpoint, messageProducer);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                send(new BuilderSupport<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .payload("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                                .extractFromPayload("/TestRequest/Message", "text")
                                .extractFromPayload("/TestRequest/Message/@lang", "language");
                    }
                });
            }
        };

        TestContext context = builder.createTestContext();
        Assert.assertNotNull(context.getVariable("text"));
        Assert.assertNotNull(context.getVariable("language"));
        Assert.assertEquals(context.getVariable("text"), "Hello World!");
        Assert.assertEquals(context.getVariable("language"), "ENG");

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);
        
        SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");
        
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        
        Assert.assertEquals(action.getVariableExtractors().size(), 1);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof XpathPayloadVariableExtractor);
        Assert.assertTrue(((XpathPayloadVariableExtractor)action.getVariableExtractors().get(0)).getXpathExpressions().containsKey("/TestRequest/Message"));
        Assert.assertTrue(((XpathPayloadVariableExtractor)action.getVariableExtractors().get(0)).getXpathExpressions().containsKey("/TestRequest/Message/@lang"));

        verify(messageEndpoint, messageProducer);
    }

    @Test
    public void testSendBuilderExtractJsonPathFromPayload() {
        reset(messageEndpoint, messageProducer);
        expect(messageEndpoint.createProducer()).andReturn(messageProducer).once();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        messageProducer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                Message message = (Message) getCurrentArguments()[0];
                Assert.assertEquals(message.getPayload(String.class), "{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}");
                return null;
            }
        }).atLeastOnce();

        replay(messageEndpoint, messageProducer);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                send(new BuilderSupport<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .messageType(MessageType.JSON)
                                .payload("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                                .extractFromPayload("$.text", "text")
                                .extractFromPayload("$.person", "person");
                    }
                });
            }
        };

        TestContext context = builder.createTestContext();
        Assert.assertNotNull(context.getVariable("text"));
        Assert.assertNotNull(context.getVariable("person"));
        Assert.assertEquals(context.getVariable("text"), "Hello World!");
        Assert.assertTrue(context.getVariable("person").contains("\"John\""));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertEquals(action.getVariableExtractors().size(), 1);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof JsonPathVariableExtractor);
        Assert.assertTrue(((JsonPathVariableExtractor)action.getVariableExtractors().get(0)).getJsonPathExpressions().containsKey("$.text"));
        Assert.assertTrue(((JsonPathVariableExtractor)action.getVariableExtractors().get(0)).getJsonPathExpressions().containsKey("$.person"));

        verify(messageEndpoint, messageProducer);
    }
    
    @Test
    public void testSendBuilderExtractFromHeader() {
        reset(messageEndpoint, messageProducer);
        expect(messageEndpoint.createProducer()).andReturn(messageProducer).once();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        messageProducer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                Message message = (Message) getCurrentArguments()[0];
                Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>");
                return null;
            }
        }).atLeastOnce();

        replay(messageEndpoint, messageProducer);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                send(new BuilderSupport<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .payload("<TestRequest><Message lang=\"ENG\">Hello World!</Message></TestRequest>")
                                .header("operation", "sayHello")
                                .header("requestId", "123456")
                                .extractFromHeader("operation", "operationHeader")
                                .extractFromHeader("requestId", "id");
                    }
                });
            }
        };

        TestContext context = builder.createTestContext();
        Assert.assertNotNull(context.getVariable("operationHeader"));
        Assert.assertNotNull(context.getVariable("id"));
        Assert.assertEquals(context.getVariable("operationHeader"), "sayHello");
        Assert.assertEquals(context.getVariable("id"), "123456");

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertEquals(action.getVariableExtractors().size(), 1);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        Assert.assertTrue(((MessageHeaderVariableExtractor) action.getVariableExtractors().get(0)).getHeaderMappings().containsKey("operation"));
        Assert.assertTrue(((MessageHeaderVariableExtractor) action.getVariableExtractors().get(0)).getHeaderMappings().containsKey("requestId"));

        verify(messageEndpoint, messageProducer);
    }

    @Test
    public void testXpathSupport() {
        reset(messageEndpoint, messageProducer);
        expect(messageEndpoint.createProducer()).andReturn(messageProducer).once();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        messageProducer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                Message message = (Message) getCurrentArguments()[0];
                Assert.assertEquals(StringUtils.trimAllWhitespace(message.getPayload(String.class)),
                        "<?xmlversion=\"1.0\"encoding=\"UTF-8\"?><TestRequest><Messagelang=\"ENG\">HelloWorld!</Message></TestRequest>");
                return null;
            }
        }).atLeastOnce();

        replay(messageEndpoint, messageProducer);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                send(new BuilderSupport<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .payload("<TestRequest><Message lang=\"ENG\">?</Message></TestRequest>")
                                .xpath("/TestRequest/Message", "Hello World!");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertTrue(action.getMessageBuilder() instanceof AbstractMessageContentBuilder);
        Assert.assertEquals(((AbstractMessageContentBuilder) action.getMessageBuilder()).getMessageInterceptors().size(), 1);
        Assert.assertTrue(((AbstractMessageContentBuilder) action.getMessageBuilder()).getMessageInterceptors().get(0) instanceof XpathMessageConstructionInterceptor);
        Assert.assertEquals(((XpathMessageConstructionInterceptor)((AbstractMessageContentBuilder) action.getMessageBuilder()).getMessageInterceptors().get(0)).getXPathExpressions().get("/TestRequest/Message"), "Hello World!");

        verify(messageEndpoint, messageProducer);
    }

    @Test
    public void testJsonPathSupport() {
        reset(messageEndpoint, messageProducer);
        expect(messageEndpoint.createProducer()).andReturn(messageProducer).once();
        expect(messageEndpoint.getActor()).andReturn(null).atLeastOnce();
        messageProducer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                Message message = (Message) getCurrentArguments()[0];
                Assert.assertEquals(StringUtils.trimAllWhitespace(message.getPayload(String.class)),
                        "{\"TestRequest\":{\"Message\":\"HelloWorld!\"}}");
                return null;
            }
        }).atLeastOnce();

        replay(messageEndpoint, messageProducer);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                send(new BuilderSupport<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint(messageEndpoint)
                                .messageType(MessageType.JSON)
                                .payload("{ \"TestRequest\": { \"Message\": \"?\" }}")
                                .jsonPath("$.TestRequest.Message", "Hello World!");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertTrue(action.getMessageBuilder() instanceof AbstractMessageContentBuilder);
        Assert.assertEquals(((AbstractMessageContentBuilder) action.getMessageBuilder()).getMessageInterceptors().size(), 1);
        Assert.assertTrue(((AbstractMessageContentBuilder) action.getMessageBuilder()).getMessageInterceptors().get(0) instanceof JsonPathMessageConstructionInterceptor);
        Assert.assertEquals(((JsonPathMessageConstructionInterceptor)((AbstractMessageContentBuilder) action.getMessageBuilder()).getMessageInterceptors().get(0)).getJsonPathExpressions().get("$.TestRequest.Message"), "Hello World!");

        verify(messageEndpoint, messageProducer);
    }

}
