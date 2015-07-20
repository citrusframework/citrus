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
import com.consol.citrus.container.SequenceAfterTest;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.builder.BuilderSupport;
import com.consol.citrus.dsl.builder.ReceiveMessageBuilder;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.EndpointConfiguration;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.messaging.Consumer;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.ControlMessageValidationContext;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.builder.StaticMessageContentBuilder;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import com.consol.citrus.ws.actions.ReceiveSoapMessageAction;
import com.consol.citrus.ws.message.SoapAttachment;
import com.consol.citrus.ws.message.SoapMessage;
import com.consol.citrus.ws.server.WebServiceServer;
import org.easymock.EasyMock;
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
public class ReceiveSoapMessageTestRunnerTest extends AbstractTestNGUnitTest {
    
    private Consumer messageConsumer = EasyMock.createMock(Consumer.class);
    private EndpointConfiguration configuration = EasyMock.createMock(EndpointConfiguration.class);
    private WebServiceServer server = EasyMock.createMock(WebServiceServer.class);
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
    public void testWebServiceServerReceive() {
        reset(server, messageConsumer, configuration);
        expect(server.createConsumer()).andReturn(messageConsumer).once();
        expect(server.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(server.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(new SoapMessage("Foo")
                .setHeader("operation","foo")
                .addAttachment(testAttachment)).atLeastOnce();
        replay(server, messageConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(server)
                                .messageType(MessageType.PLAINTEXT)
                                .message(new DefaultMessage("Foo").setHeader("operation", "foo"))
                                .soap()
                                .attachment(testAttachment);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveSoapMessageAction.class);

        ReceiveSoapMessageAction action = ((ReceiveSoapMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.PLAINTEXT.name());
        Assert.assertEquals(action.getEndpoint(), server);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), ControlMessageValidationContext.class);

        ControlMessageValidationContext validationContext = (ControlMessageValidationContext) action.getValidationContexts().get(0);

        Assert.assertTrue(validationContext.getMessageBuilder() instanceof StaticMessageContentBuilder);
        Assert.assertEquals(((StaticMessageContentBuilder)validationContext.getMessageBuilder()).getMessage().getPayload(), "Foo");
        Assert.assertNotNull(((StaticMessageContentBuilder)validationContext.getMessageBuilder()).getMessage().getHeader("operation"));

        Assert.assertEquals(action.getAttachments().size(), 1L);
        Assert.assertNull(action.getAttachments().get(0).getContentResourcePath());
        Assert.assertEquals(action.getAttachments().get(0).getContent(), testAttachment.getContent());
        Assert.assertEquals(action.getAttachments().get(0).getContentId(), testAttachment.getContentId());
        Assert.assertEquals(action.getAttachments().get(0).getContentType(), testAttachment.getContentType());
        Assert.assertEquals(action.getAttachments().get(0).getCharsetName(), testAttachment.getCharsetName());

        verify(server, messageConsumer, configuration);
    }
    
    @Test
    public void testSoapAttachment() {
        reset(server, messageConsumer, configuration);
        expect(server.createConsumer()).andReturn(messageConsumer).once();
        expect(server.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(server.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(new SoapMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .setHeader("operation","foo")
                .addAttachment(testAttachment)).atLeastOnce();
        replay(server, messageConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(server)
                                .soap()
                                .message(new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>").setHeader("operation", "foo"))
                                .attachment(testAttachment);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveSoapMessageAction.class);
        
        ReceiveSoapMessageAction action = ((ReceiveSoapMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");
        
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), server);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), XmlMessageValidationContext.class);
        
        XmlMessageValidationContext validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof StaticMessageContentBuilder);
        Assert.assertEquals(((StaticMessageContentBuilder)validationContext.getMessageBuilder()).getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertNotNull(((StaticMessageContentBuilder)validationContext.getMessageBuilder()).getMessage().getHeader("operation"));

        Assert.assertEquals(action.getAttachments().size(), 1L);
        Assert.assertNull(action.getAttachments().get(0).getContentResourcePath());
        Assert.assertEquals(action.getAttachments().get(0).getContent(), testAttachment.getContent());
        Assert.assertEquals(action.getAttachments().get(0).getContentId(), testAttachment.getContentId());
        Assert.assertEquals(action.getAttachments().get(0).getContentType(), testAttachment.getContentType());
        Assert.assertEquals(action.getAttachments().get(0).getCharsetName(), testAttachment.getCharsetName());

        verify(server, messageConsumer, configuration);
    }
    
    @Test
    public void testSoapAttachmentData() {
        reset(server, messageConsumer, configuration);
        expect(server.createConsumer()).andReturn(messageConsumer).once();
        expect(server.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(server.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(new SoapMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .setHeader("operation","foo")
                .addAttachment(testAttachment)).atLeastOnce();
        replay(server, messageConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(server)
                                .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .soap()
                                .attachment(testAttachment.getContentId(), testAttachment.getContentType(), testAttachment.getContent());
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveSoapMessageAction.class);
        
        ReceiveSoapMessageAction action = ((ReceiveSoapMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");
        
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), server);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), XmlMessageValidationContext.class);
        
        XmlMessageValidationContext validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");

        Assert.assertEquals(action.getAttachments().size(), 1L);
        Assert.assertNull(action.getAttachments().get(0).getContentResourcePath());
        Assert.assertEquals(action.getAttachments().get(0).getContent(), testAttachment.getContent());
        Assert.assertEquals(action.getAttachments().get(0).getContentId(), testAttachment.getContentId());
        Assert.assertEquals(action.getAttachments().get(0).getContentType(), testAttachment.getContentType());
        Assert.assertEquals(action.getAttachments().get(0).getCharsetName(), testAttachment.getCharsetName());

        verify(server, messageConsumer, configuration);
    }
    
    @Test
    public void testSoapAttachmentResource() throws IOException {
        final Resource attachmentResource = EasyMock.createMock(Resource.class);

        reset(server, messageConsumer, configuration, resource, attachmentResource);
        expect(server.createConsumer()).andReturn(messageConsumer).once();
        expect(server.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(server.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(new SoapMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .setHeader("operation","foo")
                .addAttachment(testAttachment)).atLeastOnce();

        expect(resource.getInputStream()).andReturn(new ByteArrayInputStream("<TestRequest><Message>Hello World!</Message></TestRequest>".getBytes())).once();
        expect(attachmentResource.getInputStream()).andReturn(new ByteArrayInputStream("This is an attachment".getBytes())).once();

        replay(server, messageConsumer, configuration, resource, attachmentResource);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(server)
                                .payload(resource)
                            .soap()
                                .attachment(testAttachment.getContentId(), testAttachment.getContentType(), attachmentResource);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveSoapMessageAction.class);
        
        ReceiveSoapMessageAction action = ((ReceiveSoapMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");
        
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), server);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), XmlMessageValidationContext.class);
        
        XmlMessageValidationContext validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        Assert.assertEquals(action.getAttachments().get(0).getContent(), "This is an attachment");
        Assert.assertEquals(action.getAttachments().get(0).getContentId(), testAttachment.getContentId());
        Assert.assertEquals(action.getAttachments().get(0).getContentType(), testAttachment.getContentType());
        Assert.assertEquals(action.getAttachments().get(0).getCharsetName(), testAttachment.getCharsetName());
        
        verify(server, messageConsumer, configuration, resource, attachmentResource);
    }

    @Test
    public void testMultipleSoapAttachmentData() {
        final SoapAttachment attachment1 = new SoapAttachment();
        attachment1.setContentId("attachment01");
        attachment1.setContent("This is an attachment");
        attachment1.setContentType("text/plain");
        attachment1.setCharsetName("UTF-8");

        final SoapAttachment attachment2 = new SoapAttachment();
        attachment2.setContentId("attachment02");
        attachment2.setContent("This is an attachment");
        attachment2.setContentType("text/plain");
        attachment2.setCharsetName("UTF-8");

        reset(server, messageConsumer, configuration);
        expect(server.createConsumer()).andReturn(messageConsumer).once();
        expect(server.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(server.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(new SoapMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .setHeader("operation","foo")
                .addAttachment(attachment1)
                .addAttachment(attachment2)).atLeastOnce();
        replay(server, messageConsumer, configuration);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(server)
                                .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .soap()
                                .attachment(attachment1.getContentId(), attachment1.getContentType(), attachment1.getContent())
                                .attachment(attachment2.getContentId(), attachment2.getContentType(), attachment2.getContent());
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveSoapMessageAction.class);

        ReceiveSoapMessageAction action = ((ReceiveSoapMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), server);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), XmlMessageValidationContext.class);

        XmlMessageValidationContext validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);

        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");

        Assert.assertEquals(action.getAttachments().size(), 2L);
        Assert.assertNull(action.getAttachments().get(0).getContentResourcePath());
        Assert.assertEquals(action.getAttachments().get(0).getContent(), attachment1.getContent());
        Assert.assertEquals(action.getAttachments().get(0).getContentId(), attachment1.getContentId());
        Assert.assertEquals(action.getAttachments().get(0).getContentType(), attachment1.getContentType());
        Assert.assertEquals(action.getAttachments().get(0).getCharsetName(), attachment1.getCharsetName());

        Assert.assertNull(action.getAttachments().get(1).getContentResourcePath());
        Assert.assertEquals(action.getAttachments().get(1).getContent(), attachment2.getContent());
        Assert.assertEquals(action.getAttachments().get(1).getContentId(), attachment2.getContentId());
        Assert.assertEquals(action.getAttachments().get(1).getContentType(), attachment2.getContentType());
        Assert.assertEquals(action.getAttachments().get(1).getCharsetName(), attachment2.getCharsetName());

        verify(server, messageConsumer, configuration);
    }
    
    @Test
    public void testReceiveBuilderWithEndpointName() {
        reset(applicationContextMock);
        reset(server, messageConsumer, configuration);
        expect(server.createConsumer()).andReturn(messageConsumer).times(2);
        expect(server.getEndpointConfiguration()).andReturn(configuration).atLeastOnce();
        expect(configuration.getTimeout()).andReturn(100L).atLeastOnce();
        expect(server.getActor()).andReturn(null).atLeastOnce();
        expect(messageConsumer.receive(anyObject(TestContext.class), anyLong())).andReturn(new SoapMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .setHeader("operation","foo")).atLeastOnce();
        replay(server, messageConsumer, configuration);

        expect(applicationContextMock.getBean(TestContext.class)).andReturn(applicationContext.getBean(TestContext.class)).once();
        expect(applicationContextMock.getBean("replyMessageEndpoint", Endpoint.class)).andReturn(server).atLeastOnce();
        expect(applicationContextMock.getBean("fooMessageEndpoint", Endpoint.class)).andReturn(server).atLeastOnce();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();
        replay(applicationContextMock);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint("replyMessageEndpoint")
                                .soap()
                                .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
                    }
                });

                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint("fooMessageEndpoint")
                                .soap()
                                .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), ReceiveSoapMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), ReceiveSoapMessageAction.class);

        ReceiveSoapMessageAction action = ((ReceiveSoapMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "receive");
        Assert.assertEquals(action.getEndpointUri(), "replyMessageEndpoint");
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        
        action = ((ReceiveSoapMessageAction)test.getActions().get(1));
        Assert.assertEquals(action.getName(), "receive");
        Assert.assertEquals(action.getEndpointUri(), "fooMessageEndpoint");
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        
        verify(applicationContextMock);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,
            expectedExceptionsMessageRegExp = "Invalid use of http and soap action builder")
    public void testReceiveBuilderWithSoapAndHttpMixed() {
        reset(applicationContextMock);

        expect(applicationContextMock.getBean(TestContext.class)).andReturn(applicationContext.getBean(TestContext.class)).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();

        replay(applicationContextMock);

        new MockTestRunner(getClass().getSimpleName(), applicationContextMock) {
            @Override
            public void execute() {
                receive(new BuilderSupport<ReceiveMessageBuilder>() {
                    @Override
                    public void configure(ReceiveMessageBuilder builder) {
                        builder.endpoint(server)
                                .soap()
                                .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                .header("operation", "soapOperation")
                                .http();
                    }
                });
            }
        };
    }
    
}
