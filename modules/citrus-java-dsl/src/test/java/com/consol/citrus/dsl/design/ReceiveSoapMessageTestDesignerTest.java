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

package com.consol.citrus.dsl.design;

import com.consol.citrus.TestCase;
import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.container.SequenceAfterTest;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.dsl.actions.DelegatingTestAction;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.builder.StaticMessageContentBuilder;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import com.consol.citrus.ws.actions.ReceiveSoapMessageAction;
import com.consol.citrus.ws.message.SoapAttachment;
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

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;


/**
 * @author Christoph Deppisch
 */
public class ReceiveSoapMessageTestDesignerTest extends AbstractTestNGUnitTest {
    
    private Endpoint messageEndpoint = Mockito.mock(Endpoint.class);
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
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                soap().server(server)
                    .receive()
                    .message(new DefaultMessage("Foo").setHeader("operation", "foo"))
                    .attachment(testAttachment);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), ReceiveSoapMessageAction.class);

        ReceiveSoapMessageAction action = (ReceiveSoapMessageAction) ((DelegatingTestAction)test.getActions().get(0)).getDelegate();
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), server);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), XmlMessageValidationContext.class);

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
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                soap().server(server)
                        .receive()
                        .message(new DefaultMessage("Foo").setHeader("operation", "foo"))
                        .attachment(testAttachment);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), ReceiveSoapMessageAction.class);

        ReceiveSoapMessageAction action = (ReceiveSoapMessageAction) ((DelegatingTestAction)test.getActions().get(0)).getDelegate();
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), server);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), XmlMessageValidationContext.class);

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
    public void testSoapAttachmentDeprecated() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                    .soap()
                    .message(new DefaultMessage("Foo").setHeader("operation", "foo"))
                    .attachment(testAttachment);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), ReceiveSoapMessageAction.class);
        
        ReceiveSoapMessageAction action = (ReceiveSoapMessageAction) ((DelegatingTestAction)test.getActions().get(0)).getDelegate();
        Assert.assertEquals(action.getName(), "receive");
        
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), XmlMessageValidationContext.class);
        
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
    public void testSoapAttachmentData() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                soap().server(server)
                    .receive()
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .attachment(testAttachment.getContentId(), testAttachment.getContentType(), testAttachment.getContent());
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), ReceiveSoapMessageAction.class);
        
        ReceiveSoapMessageAction action = (ReceiveSoapMessageAction) ((DelegatingTestAction)test.getActions().get(0)).getDelegate();
        Assert.assertEquals(action.getName(), "receive");
        
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), server);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), XmlMessageValidationContext.class);
        
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
    public void testSoapAttachmentDataDeprecated() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .soap()
                    .attachment(testAttachment.getContentId(), testAttachment.getContentType(), testAttachment.getContent());
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), ReceiveSoapMessageAction.class);

        ReceiveSoapMessageAction action = (ReceiveSoapMessageAction) ((DelegatingTestAction)test.getActions().get(0)).getDelegate();
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), XmlMessageValidationContext.class);

        Assert.assertTrue(action.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)action.getMessageBuilder()).getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");

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
        
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                soap().server(server)
                    .receive()
                    .payload(resource)
                    .attachment(testAttachment.getContentId(), testAttachment.getContentType(), attachmentResource);
            }
        };
        
        reset(resource, attachmentResource);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("somePayloadData".getBytes()));
        when(attachmentResource.getInputStream()).thenReturn(new ByteArrayInputStream("someAttachmentData".getBytes()));
        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), ReceiveSoapMessageAction.class);
        
        ReceiveSoapMessageAction action = (ReceiveSoapMessageAction) ((DelegatingTestAction)test.getActions().get(0)).getDelegate();
        Assert.assertEquals(action.getName(), "receive");
        
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), server);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), XmlMessageValidationContext.class);
        
        Assert.assertTrue(action.getMessageBuilder() instanceof StaticMessageContentBuilder);
        Assert.assertEquals(((StaticMessageContentBuilder)action.getMessageBuilder()).getMessage().getPayload(), "somePayloadData");
        
        Assert.assertEquals(action.getAttachments().get(0).getContent(), "someAttachmentData");
        Assert.assertEquals(action.getAttachments().get(0).getContentId(), testAttachment.getContentId());
        Assert.assertEquals(action.getAttachments().get(0).getContentType(), testAttachment.getContentType());
        Assert.assertEquals(action.getAttachments().get(0).getCharsetName(), testAttachment.getCharsetName());
    }

    @Test
    public void testSoapAttachmentResourceDeprecated() throws IOException {
        final Resource attachmentResource = Mockito.mock(Resource.class);

        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                    .payload(resource)
                    .soap()
                    .attachment(testAttachment.getContentId(), testAttachment.getContentType(), attachmentResource);
            }
        };

        reset(resource, attachmentResource);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("somePayloadData".getBytes()));
        when(attachmentResource.getInputStream()).thenReturn(new ByteArrayInputStream("someAttachmentData".getBytes()));
        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), ReceiveSoapMessageAction.class);

        ReceiveSoapMessageAction action = (ReceiveSoapMessageAction) ((DelegatingTestAction)test.getActions().get(0)).getDelegate();
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), XmlMessageValidationContext.class);

        Assert.assertTrue(action.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)action.getMessageBuilder()).getPayloadData(), "somePayloadData");

        Assert.assertEquals(action.getAttachments().get(0).getContent(), "someAttachmentData");
        Assert.assertEquals(action.getAttachments().get(0).getContentId(), testAttachment.getContentId());
        Assert.assertEquals(action.getAttachments().get(0).getContentType(), testAttachment.getContentType());
        Assert.assertEquals(action.getAttachments().get(0).getCharsetName(), testAttachment.getCharsetName());
    }

    @Test
    public void testMultipleSoapAttachmentData() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                soap().server(server)
                        .receive()
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .attachment(testAttachment.getContentId() + 1, testAttachment.getContentType(), testAttachment.getContent() + 1)
                        .attachment(testAttachment.getContentId() + 2, testAttachment.getContentType(), testAttachment.getContent() + 2);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), ReceiveSoapMessageAction.class);

        ReceiveSoapMessageAction action = (ReceiveSoapMessageAction) ((DelegatingTestAction)test.getActions().get(0)).getDelegate();
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), server);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), XmlMessageValidationContext.class);

        Assert.assertTrue(action.getMessageBuilder() instanceof StaticMessageContentBuilder);
        Assert.assertEquals(((StaticMessageContentBuilder)action.getMessageBuilder()).getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");

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
    }

    @Test
    public void testMultipleSoapAttachmentDataDeprecated() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .soap()
                        .attachment(testAttachment.getContentId() + 1, testAttachment.getContentType(), testAttachment.getContent() + 1)
                        .attachment(testAttachment.getContentId() + 2, testAttachment.getContentType(), testAttachment.getContent() + 2);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), ReceiveSoapMessageAction.class);

        ReceiveSoapMessageAction action = (ReceiveSoapMessageAction) ((DelegatingTestAction)test.getActions().get(0)).getDelegate();
        Assert.assertEquals(action.getName(), "receive");

        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), XmlMessageValidationContext.class);

        Assert.assertTrue(action.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)action.getMessageBuilder()).getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");

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
    }
    
    @Test
    public void testReceiveBuilderWithEndpointName() {
        reset(applicationContextMock);
        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());
        MockTestDesigner builder = new MockTestDesigner(applicationContextMock, context) {
            @Override
            public void configure() {
                receive("replyMessageEndpoint")
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");

                receive("fooMessageEndpoint")
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), ReceiveMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(1)).getDelegate().getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = (ReceiveMessageAction) ((DelegatingTestAction)test.getActions().get(0)).getDelegate();
        Assert.assertEquals(action.getName(), "receive");
        Assert.assertEquals(action.getEndpointUri(), "replyMessageEndpoint");
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        
        action = (ReceiveMessageAction) ((DelegatingTestAction)test.getActions().get(1)).getDelegate();
        Assert.assertEquals(action.getName(), "receive");
        Assert.assertEquals(action.getEndpointUri(), "fooMessageEndpoint");
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

    }

    @Test(expectedExceptions = CitrusRuntimeException.class,
            expectedExceptionsMessageRegExp = "Invalid use of http and soap action builder")
    public void testReceiveBuilderWithSoapAndHttpMixed() {
        reset(applicationContextMock);

        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());

        MockTestDesigner builder = new MockTestDesigner(applicationContextMock, context) {
            @Override
            public void configure() {
                receive(messageEndpoint)
                        .soap()
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .header("operation", "soapOperation")
                        .http();
            }
        };

        builder.configure();
    }
    
}
