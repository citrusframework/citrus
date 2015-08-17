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
import com.consol.citrus.dsl.builder.BuilderSupport;
import com.consol.citrus.dsl.builder.SendMessageBuilder;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.*;
import com.consol.citrus.messaging.Producer;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.ws.actions.SendSoapMessageAction;
import com.consol.citrus.ws.client.WebServiceClient;
import com.consol.citrus.ws.message.*;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class SendSoapMessageTestRunnerTest extends AbstractTestNGUnitTest {
    
    private WebServiceClient soapClient = EasyMock.createMock(WebServiceClient.class);
    private Producer messageProducer = EasyMock.createMock(Producer.class);
    private ApplicationContext applicationContextMock = EasyMock.createMock(ApplicationContext.class);
    private Resource resource = EasyMock.createMock(Resource.class);
    
    private SoapAttachment testAttachment = new SoapAttachment();
    
    /**
     * Setup test attachment.
     */
    @BeforeClass
    public void setup() {
        testAttachment.setContentId("attachment01");
        testAttachment.setContent("This is an attachment");
        testAttachment.setContentType("text/plain");
        testAttachment.setCharsetName("UTF-8");
    }

    @Test
    public void testFork() {
        reset(soapClient, messageProducer);
        expect(soapClient.createProducer()).andReturn(messageProducer).atLeastOnce();
        expect(soapClient.getActor()).andReturn(null).atLeastOnce();
        messageProducer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                Message message = (Message) getCurrentArguments()[0];
                Assert.assertEquals(message.getPayload(String.class), "Foo");
                return null;
            }
        }).atLeastOnce();
        replay(soapClient, messageProducer);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                send(new BuilderSupport<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint(soapClient)
                                .soap()
                                .message(new DefaultMessage("Foo").setHeader("operation", "foo"));
                    }
                });
                
                send(new BuilderSupport<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint(soapClient)
                                .soap()
                                .message(new DefaultMessage("Foo").setHeader("operation", "foo"))
                                .fork(true);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendSoapMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), SendSoapMessageAction.class);
        
        SendSoapMessageAction action = ((SendSoapMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");
        
        Assert.assertEquals(action.getEndpoint(), soapClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);
        
        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "Foo");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 1L);
        Assert.assertEquals(messageBuilder.getMessageHeaders().get("operation"), "foo");
        
        Assert.assertFalse(action.isForkMode());
        
        action = ((SendSoapMessageAction)test.getActions().get(1));
        Assert.assertEquals(action.getName(), "send");
        
        Assert.assertEquals(action.getEndpoint(), soapClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);
        
        Assert.assertTrue(action.isForkMode());

        verify(soapClient, messageProducer);
    }

    @Test
    public void testSoapAction() {
        reset(soapClient, messageProducer);
        expect(soapClient.createProducer()).andReturn(messageProducer).once();
        expect(soapClient.getActor()).andReturn(null).atLeastOnce();
        messageProducer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                SoapMessage message = (SoapMessage) getCurrentArguments()[0];
                Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
                Assert.assertEquals(message.getSoapAction(), "TestService/sayHello");
                return null;
            }
        }).once();
        replay(soapClient, messageProducer);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                send(new BuilderSupport<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint(soapClient)
                                .soap()
                                .soapAction("TestService/sayHello")
                                .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendSoapMessageAction.class);

        SendSoapMessageAction action = ((SendSoapMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), soapClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 1L);
        Assert.assertEquals(messageBuilder.getMessageHeaders().get(SoapMessageHeaders.SOAP_ACTION), "TestService/sayHello");

        verify(soapClient, messageProducer);
    }
    
    @Test
    public void testSoapAttachment() {
        reset(soapClient, messageProducer);
        expect(soapClient.createProducer()).andReturn(messageProducer).once();
        expect(soapClient.getActor()).andReturn(null).atLeastOnce();
        messageProducer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                SoapMessage message = (SoapMessage) getCurrentArguments()[0];
                Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
                Assert.assertEquals(message.getAttachments().size(), 1L);
                Assert.assertEquals(message.getAttachments().get(0).getContent(), testAttachment.getContent());
                return null;
            }
        }).once();
        replay(soapClient, messageProducer);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                send(new BuilderSupport<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint(soapClient)
                                .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .soap()
                                .attachment(testAttachment);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendSoapMessageAction.class);
        
        SendSoapMessageAction action = ((SendSoapMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");
        
        Assert.assertEquals(action.getEndpoint(), soapClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);

        Assert.assertEquals(action.getAttachments().size(), 1L);
        Assert.assertNull(action.getAttachments().get(0).getContentResourcePath());
        Assert.assertEquals(action.getAttachments().get(0).getContent(), testAttachment.getContent());
        Assert.assertEquals(action.getAttachments().get(0).getContentId(), testAttachment.getContentId());
        Assert.assertEquals(action.getAttachments().get(0).getContentType(), testAttachment.getContentType());
        Assert.assertEquals(action.getAttachments().get(0).getCharsetName(), testAttachment.getCharsetName());

        verify(soapClient, messageProducer);
    }
    
    @Test
    public void testSoapAttachmentData() {
        reset(soapClient, messageProducer);
        expect(soapClient.createProducer()).andReturn(messageProducer).once();
        expect(soapClient.getActor()).andReturn(null).atLeastOnce();
        messageProducer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                SoapMessage message = (SoapMessage) getCurrentArguments()[0];
                Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
                Assert.assertEquals(message.getAttachments().size(), 1L);
                Assert.assertEquals(message.getAttachments().get(0).getContent(), testAttachment.getContent());
                return null;
            }
        }).once();
        replay(soapClient, messageProducer);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                send(new BuilderSupport<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint(soapClient)
                                .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .soap()
                                .attachment(testAttachment.getContentId(), testAttachment.getContentType(), testAttachment.getContent());
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendSoapMessageAction.class);
        
        SendSoapMessageAction action = ((SendSoapMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");
        
        Assert.assertEquals(action.getEndpoint(), soapClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);

        Assert.assertEquals(action.getAttachments().size(), 1L);
        Assert.assertNull(action.getAttachments().get(0).getContentResourcePath());
        Assert.assertEquals(action.getAttachments().get(0).getContent(), testAttachment.getContent());
        Assert.assertEquals(action.getAttachments().get(0).getContentId(), testAttachment.getContentId());
        Assert.assertEquals(action.getAttachments().get(0).getContentType(), testAttachment.getContentType());
        Assert.assertEquals(action.getAttachments().get(0).getCharsetName(), testAttachment.getCharsetName());

        verify(soapClient, messageProducer);
    }

    @Test
    public void testMultipleSoapAttachmentData() {
        reset(soapClient, messageProducer);
        expect(soapClient.createProducer()).andReturn(messageProducer).atLeastOnce();
        expect(soapClient.getActor()).andReturn(null).atLeastOnce();
        messageProducer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                SoapMessage message = (SoapMessage) getCurrentArguments()[0];
                Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
                Assert.assertEquals(message.getAttachments().size(), 2L);
                Assert.assertEquals(message.getAttachments().get(0).getContent(), testAttachment.getContent() + 1);
                Assert.assertEquals(message.getAttachments().get(1).getContent(), testAttachment.getContent() + 2);
                return null;
            }
        }).atLeastOnce();
        replay(soapClient, messageProducer);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                send(new BuilderSupport<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint(soapClient)
                                .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .soap()
                                .attachment(testAttachment.getContentId() + 1, testAttachment.getContentType(), testAttachment.getContent() + 1)
                                .attachment(testAttachment.getContentId() + 2, testAttachment.getContentType(), testAttachment.getContent() + 2);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendSoapMessageAction.class);

        SendSoapMessageAction action = ((SendSoapMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), soapClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);

        Assert.assertEquals(action.getAttachments().size(), 2L);
        Assert.assertNull(action.getAttachments().get(0).getContentResourcePath());
        Assert.assertEquals(action.getAttachments().get(0).getContent(), testAttachment.getContent() + 1);
        Assert.assertEquals(action.getAttachments().get(0).getContentId(), testAttachment.getContentId() + 1);
        Assert.assertEquals(action.getAttachments().get(0).getContentType(), testAttachment.getContentType());
        Assert.assertEquals(action.getAttachments().get(0).getCharsetName(), testAttachment.getCharsetName());
        Assert.assertNull(action.getAttachments().get(1).getContentResourcePath());
        Assert.assertEquals(action.getAttachments().get(1).getContent(), testAttachment.getContent() + 2);
        Assert.assertEquals(action.getAttachments().get(1).getContentId(), testAttachment.getContentId() + 2);
        Assert.assertEquals(action.getAttachments().get(1).getContentType(), testAttachment.getContentType());
        Assert.assertEquals(action.getAttachments().get(1).getCharsetName(), testAttachment.getCharsetName());

        verify(soapClient, messageProducer);
    }
    
    @Test
    public void testSoapAttachmentResource() throws IOException {
        reset(resource, soapClient, messageProducer);
        expect(soapClient.createProducer()).andReturn(messageProducer).once();
        expect(soapClient.getActor()).andReturn(null).atLeastOnce();
        messageProducer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                SoapMessage message = (SoapMessage) getCurrentArguments()[0];
                Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
                Assert.assertEquals(message.getAttachments().size(), 1L);
                Assert.assertEquals(message.getAttachments().get(0).getContent(), "someAttachmentData");
                return null;
            }
        }).once();

        expect(resource.getInputStream()).andReturn(new ByteArrayInputStream("someAttachmentData".getBytes())).once();
        replay(resource, soapClient, messageProducer);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                send(new BuilderSupport<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint(soapClient)
                                .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .soap()
                                .attachment(testAttachment.getContentId(), testAttachment.getContentType(), resource);
                    }
                });
            }
        };
        
        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendSoapMessageAction.class);
        
        SendSoapMessageAction action = ((SendSoapMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");
        
        Assert.assertEquals(action.getEndpoint(), soapClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        
        Assert.assertEquals(action.getAttachments().get(0).getContent(), "someAttachmentData");
        Assert.assertEquals(action.getAttachments().get(0).getContentId(), testAttachment.getContentId());
        Assert.assertEquals(action.getAttachments().get(0).getContentType(), testAttachment.getContentType());
        Assert.assertEquals(action.getAttachments().get(0).getCharsetName(), testAttachment.getCharsetName());
        
        verify(resource, soapClient, messageProducer);
    }
    
    @Test
    public void testSendBuilderWithEndpointName() {
        reset(applicationContextMock, soapClient, messageProducer);
        expect(soapClient.createProducer()).andReturn(messageProducer).times(2);
        expect(soapClient.getActor()).andReturn(null).atLeastOnce();
        messageProducer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                SoapMessage message = (SoapMessage) getCurrentArguments()[0];
                Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
                Assert.assertEquals(message.getAttachments().size(), 1L);
                Assert.assertEquals(message.getAttachments().get(0).getContent(), testAttachment.getContent());
                return null;
            }
        }).once();

        messageProducer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                SoapMessage message = (SoapMessage) getCurrentArguments()[0];
                Assert.assertEquals(message.getPayload(String.class), "<TestRequest><Message>Hello World!</Message></TestRequest>");
                return null;
            }
        }).once();

        expect(applicationContextMock.getBean(TestContext.class)).andReturn(applicationContext.getBean(TestContext.class)).once();
        expect(applicationContextMock.getBean("soapClient", Endpoint.class)).andReturn(soapClient).atLeastOnce();
        expect(applicationContextMock.getBean("otherClient", Endpoint.class)).andReturn(soapClient).atLeastOnce();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();
        replay(applicationContextMock, soapClient, messageProducer);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock) {
            @Override
            public void execute() {
                send(new BuilderSupport<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint("soapClient")
                                .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .header("operation", "soapOperation")
                                .soap()
                                .attachment(testAttachment);
                    }
                });

                send(new BuilderSupport<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint("otherClient")
                                .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .soap();
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendSoapMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), SendSoapMessageAction.class);
        
        SendMessageAction action = ((SendSoapMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");
        Assert.assertEquals(action.getEndpointUri(), "soapClient");
        
        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 1L);
        Assert.assertTrue(messageBuilder.getMessageHeaders().containsKey("operation"));
        
        action = ((SendSoapMessageAction)test.getActions().get(1));
        Assert.assertEquals(action.getName(), "send");
        Assert.assertEquals(action.getEndpointUri(), "otherClient");
        
        verify(applicationContextMock, soapClient, messageProducer);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,
          expectedExceptionsMessageRegExp = "Invalid use of http and soap action builder")
    public void testSendBuilderWithSoapAndHttpMixed() {
        reset(applicationContextMock);
        expect(applicationContextMock.getBean(TestContext.class)).andReturn(applicationContext.getBean(TestContext.class)).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();
        replay(applicationContextMock);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock) {
            @Override
            public void execute() {
                send(new BuilderSupport<SendMessageBuilder>() {
                    @Override
                    public void configure(SendMessageBuilder builder) {
                        builder.endpoint("soapClient")
                                .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .header("operation", "soapOperation")
                                .soap()
                                .attachment(testAttachment)
                                .http();
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendSoapMessageAction.class);

        SendSoapMessageAction action = ((SendSoapMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");
        Assert.assertEquals(action.getEndpointUri(), "soapClient");
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        verify(applicationContextMock);
    }
    
}
