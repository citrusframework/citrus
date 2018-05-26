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
import com.consol.citrus.dsl.actions.DelegatingTestAction;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.EndpointConfiguration;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.messaging.Consumer;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.builder.StaticMessageContentBuilder;
import com.consol.citrus.validation.context.HeaderValidationContext;
import com.consol.citrus.validation.json.JsonMessageValidationContext;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import com.consol.citrus.ws.actions.ReceiveSoapMessageAction;
import com.consol.citrus.ws.message.SoapAttachment;
import com.consol.citrus.ws.message.SoapMessage;
import com.consol.citrus.ws.server.WebServiceServer;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 */
public class ReceiveSoapMessageTestRunnerTest extends AbstractTestNGUnitTest {
    
    private Consumer messageConsumer = Mockito.mock(Consumer.class);
    private EndpointConfiguration configuration = Mockito.mock(EndpointConfiguration.class);
    private WebServiceServer server = Mockito.mock(WebServiceServer.class);
    private ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);
    private Resource resource = Mockito.mock(Resource.class);
    
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
        when(server.createConsumer()).thenReturn(messageConsumer);
        when(server.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(server.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new SoapMessage("Foo")
                .setHeader("operation","foo")
                .addAttachment(testAttachment));
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                soap(action -> action.server(server)
                        .receive()
                        .messageType(MessageType.PLAINTEXT)
                        .message(new DefaultMessage("Foo").setHeader("operation", "foo"))
                        .attachment(testAttachment));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), ReceiveSoapMessageAction.class);

        ReceiveSoapMessageAction action = ((ReceiveSoapMessageAction)((DelegatingTestAction)test.getActions().get(0)).getDelegate());
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.PLAINTEXT.name());
        Assert.assertEquals(action.getEndpoint(), server);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), HeaderValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(1).getClass(), XmlMessageValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(2).getClass(), JsonMessageValidationContext.class);

        Assert.assertTrue(action.getMessageBuilder() instanceof StaticMessageContentBuilder);
        Assert.assertEquals(((StaticMessageContentBuilder)action.getMessageBuilder()).getMessage().getPayload(), "Foo");
        Assert.assertNotNull(((StaticMessageContentBuilder)action.getMessageBuilder()).getMessage().getHeader("operation"));

        Assert.assertEquals(action.getAttachments().size(), 1L);
        Assert.assertNull(action.getAttachments().get(0).getContentResourcePath());
        Assert.assertEquals(action.getAttachments().get(0).getContent(), testAttachment.getContent());
        Assert.assertEquals(action.getAttachments().get(0).getContentId(), testAttachment.getContentId());
        Assert.assertEquals(action.getAttachments().get(0).getContentType(), testAttachment.getContentType());
        Assert.assertEquals(action.getAttachments().get(0).getCharsetName(), testAttachment.getCharsetName());
    }

    @Test
    public void testSoapAttachment() {
        reset(server, messageConsumer, configuration);
        when(server.createConsumer()).thenReturn(messageConsumer);
        when(server.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(server.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new SoapMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .setHeader("operation","foo")
                .addAttachment(testAttachment));
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                soap(action -> action.server(server)
                        .receive()
                        .message(new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>").setHeader("operation", "foo"))
                        .attachment(testAttachment));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), ReceiveSoapMessageAction.class);

        ReceiveSoapMessageAction action = ((ReceiveSoapMessageAction)((DelegatingTestAction)test.getActions().get(0)).getDelegate());
        Assert.assertEquals(action.getName(), "receive");
        
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), server);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), HeaderValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(1).getClass(), XmlMessageValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(2).getClass(), JsonMessageValidationContext.class);
        
        Assert.assertTrue(action.getMessageBuilder() instanceof StaticMessageContentBuilder);
        Assert.assertEquals(((StaticMessageContentBuilder)action.getMessageBuilder()).getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertNotNull(((StaticMessageContentBuilder)action.getMessageBuilder()).getMessage().getHeader("operation"));

        Assert.assertEquals(action.getAttachments().size(), 1L);
        Assert.assertNull(action.getAttachments().get(0).getContentResourcePath());
        Assert.assertEquals(action.getAttachments().get(0).getContent(), testAttachment.getContent());
        Assert.assertEquals(action.getAttachments().get(0).getContentId(), testAttachment.getContentId());
        Assert.assertEquals(action.getAttachments().get(0).getContentType(), testAttachment.getContentType());
        Assert.assertEquals(action.getAttachments().get(0).getCharsetName(), testAttachment.getCharsetName());
    }

    @Test
    public void testSoapAttachmentData() {
        reset(server, messageConsumer, configuration);
        when(server.createConsumer()).thenReturn(messageConsumer);
        when(server.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(server.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new SoapMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .setHeader("operation","foo")
                .addAttachment(testAttachment));
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                soap(action -> action.server(server)
                        .receive()
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .attachment(testAttachment.getContentId(), testAttachment.getContentType(), testAttachment.getContent()));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), ReceiveSoapMessageAction.class);

        ReceiveSoapMessageAction action = ((ReceiveSoapMessageAction)((DelegatingTestAction)test.getActions().get(0)).getDelegate());
        Assert.assertEquals(action.getName(), "receive");
        
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), server);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), HeaderValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(1).getClass(), XmlMessageValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(2).getClass(), JsonMessageValidationContext.class);
        
        Assert.assertTrue(action.getMessageBuilder() instanceof StaticMessageContentBuilder);
        Assert.assertEquals(((StaticMessageContentBuilder)action.getMessageBuilder()).getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");

        Assert.assertEquals(action.getAttachments().size(), 1L);
        Assert.assertNull(action.getAttachments().get(0).getContentResourcePath());
        Assert.assertEquals(action.getAttachments().get(0).getContent(), testAttachment.getContent());
        Assert.assertEquals(action.getAttachments().get(0).getContentId(), testAttachment.getContentId());
        Assert.assertEquals(action.getAttachments().get(0).getContentType(), testAttachment.getContentType());
        Assert.assertEquals(action.getAttachments().get(0).getCharsetName(), testAttachment.getCharsetName());
    }

    @Test
    public void testSoapAttachmentResource() throws IOException {
        final Resource attachmentResource = Mockito.mock(Resource.class);

        reset(server, messageConsumer, configuration, resource, attachmentResource);
        when(server.createConsumer()).thenReturn(messageConsumer);
        when(server.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(server.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new SoapMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .setHeader("operation","foo")
                .addAttachment(testAttachment));

        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("<TestRequest><Message>Hello World!</Message></TestRequest>".getBytes()));
        when(attachmentResource.getInputStream()).thenReturn(new ByteArrayInputStream("This is an attachment".getBytes()));

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                soap(action -> action.server(server)
                        .receive()
                        .payload(resource)
                        .attachment(testAttachment.getContentId(), testAttachment.getContentType(), attachmentResource));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), ReceiveSoapMessageAction.class);

        ReceiveSoapMessageAction action = ((ReceiveSoapMessageAction)((DelegatingTestAction)test.getActions().get(0)).getDelegate());
        Assert.assertEquals(action.getName(), "receive");
        
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), server);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), HeaderValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(1).getClass(), XmlMessageValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(2).getClass(), JsonMessageValidationContext.class);
        
        Assert.assertTrue(action.getMessageBuilder() instanceof StaticMessageContentBuilder);
        Assert.assertEquals(((StaticMessageContentBuilder)action.getMessageBuilder()).getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        Assert.assertEquals(action.getAttachments().get(0).getContent(), "This is an attachment");
        Assert.assertEquals(action.getAttachments().get(0).getContentId(), testAttachment.getContentId());
        Assert.assertEquals(action.getAttachments().get(0).getContentType(), testAttachment.getContentType());
        Assert.assertEquals(action.getAttachments().get(0).getCharsetName(), testAttachment.getCharsetName());
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
        when(server.createConsumer()).thenReturn(messageConsumer);
        when(server.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(server.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new SoapMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .setHeader("operation","foo")
                .addAttachment(attachment1)
                .addAttachment(attachment2));
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                soap(action -> action.server(server)
                        .receive()
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .attachment(attachment1.getContentId(), attachment1.getContentType(), attachment1.getContent())
                        .attachment(attachment2.getContentId(), attachment2.getContentType(), attachment2.getContent()));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), ReceiveSoapMessageAction.class);

        ReceiveSoapMessageAction action = ((ReceiveSoapMessageAction)((DelegatingTestAction)test.getActions().get(0)).getDelegate());
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), server);
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), HeaderValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(1).getClass(), XmlMessageValidationContext.class);
        Assert.assertEquals(action.getValidationContexts().get(2).getClass(), JsonMessageValidationContext.class);

        Assert.assertTrue(action.getMessageBuilder() instanceof StaticMessageContentBuilder);
        Assert.assertEquals(((StaticMessageContentBuilder)action.getMessageBuilder()).getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");

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
    }

    @Test
    public void testReceiveBuilderWithEndpointName() {
        TestContext context = applicationContext.getBean(TestContext.class);
        context.setApplicationContext(applicationContextMock);

        reset(applicationContextMock);
        reset(server, messageConsumer, configuration);
        when(server.createConsumer()).thenReturn(messageConsumer);
        when(server.getEndpointConfiguration()).thenReturn(configuration);
        when(configuration.getTimeout()).thenReturn(100L);
        when(server.getActor()).thenReturn(null);
        when(messageConsumer.receive(any(TestContext.class), anyLong())).thenReturn(new SoapMessage("<TestRequest><Message>Hello World!</Message></TestRequest>")
                .setHeader("operation","foo"));
        when(applicationContextMock.getBean(TestContext.class)).thenReturn(context);
        when(applicationContextMock.getBean("replyMessageEndpoint", Endpoint.class)).thenReturn(server);
        when(applicationContextMock.getBean("fooMessageEndpoint", Endpoint.class)).thenReturn(server);
        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<>());
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock, context) {
            @Override
            public void execute() {
                soap(action -> action.server("replyMessageEndpoint")
                        .receive()
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>"));

                soap(action -> action.server("fooMessageEndpoint")
                        .receive()
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>"));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), ReceiveSoapMessageAction.class);

        Assert.assertEquals(test.getActions().get(1).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(1)).getDelegate().getClass(), ReceiveSoapMessageAction.class);

        ReceiveSoapMessageAction action = ((ReceiveSoapMessageAction)((DelegatingTestAction)test.getActions().get(0)).getDelegate());
        Assert.assertEquals(action.getName(), "receive");
        Assert.assertEquals(action.getEndpoint(), server);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

        action = ((ReceiveSoapMessageAction)((DelegatingTestAction)test.getActions().get(1)).getDelegate());
        Assert.assertEquals(action.getName(), "receive");
        Assert.assertEquals(action.getEndpoint(), server);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
    }

}
