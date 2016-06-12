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
import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.container.SequenceAfterTest;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.dsl.actions.DelegatingTestAction;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.builder.StaticMessageContentBuilder;
import com.consol.citrus.ws.actions.SendSoapMessageAction;
import com.consol.citrus.ws.client.WebServiceClient;
import com.consol.citrus.ws.message.SoapAttachment;
import com.consol.citrus.ws.message.SoapMessageHeaders;
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
public class SendSoapMessageTestDesignerTest extends AbstractTestNGUnitTest {
    
    private WebServiceClient soapClient = Mockito.mock(WebServiceClient.class);

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
    public void testFork() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                send(soapClient)
                    .message(new DefaultMessage("Foo").setHeader("operation", "foo"))
                        .header("additional", "additionalValue");
                
                send(soapClient)
                    .message(new DefaultMessage("Foo").setHeader("operation", "foo"))
                    .fork(true);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), SendMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(1)).getDelegate().getClass(), SendMessageAction.class);
        
        SendMessageAction action = (SendMessageAction) ((DelegatingTestAction)test.getActions().get(0)).getDelegate();
        Assert.assertEquals(action.getName(), "send");
        
        Assert.assertEquals(action.getEndpoint(), soapClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageContentBuilder.class);

        StaticMessageContentBuilder messageBuilder = (StaticMessageContentBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(String.class), "Foo");
        Assert.assertEquals(messageBuilder.getMessage().getHeader("operation"), "foo");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 1L);
        Assert.assertEquals(messageBuilder.getMessageHeaders().get("additional"), "additionalValue");

        Assert.assertFalse(action.isForkMode());
        
        action = (SendMessageAction) ((DelegatingTestAction)test.getActions().get(1)).getDelegate();
        Assert.assertEquals(action.getName(), "send");
        
        Assert.assertEquals(action.getEndpoint(), soapClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageContentBuilder.class);
        
        Assert.assertTrue(action.isForkMode());
    }

    @Test
    public void testSoapAction() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                soap().client(soapClient)
                        .send()
                        .soapAction("TestService/sayHello")
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), SendSoapMessageAction.class);

        SendSoapMessageAction action = (SendSoapMessageAction) ((DelegatingTestAction)test.getActions().get(0)).getDelegate();
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), soapClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageContentBuilder.class);

        StaticMessageContentBuilder messageBuilder = (StaticMessageContentBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessage().getHeaders().size(), 3L);
        Assert.assertEquals(messageBuilder.getMessage().getHeaders().get(SoapMessageHeaders.SOAP_ACTION), "TestService/sayHello");
    }

    @Test
    public void testSoapActionDeprecated() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                send(soapClient)
                        .soap()
                        .soapAction("TestService/sayHello")
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), SendSoapMessageAction.class);

        SendSoapMessageAction action = (SendSoapMessageAction) ((DelegatingTestAction)test.getActions().get(0)).getDelegate();
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), soapClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 1L);
        Assert.assertEquals(messageBuilder.getMessageHeaders().get(SoapMessageHeaders.SOAP_ACTION), "TestService/sayHello");
    }
    
    @Test
    public void testSoapAttachment() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                soap().client(soapClient)
                    .send()
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .attachment(testAttachment);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), SendSoapMessageAction.class);
        
        SendSoapMessageAction action = (SendSoapMessageAction) ((DelegatingTestAction)test.getActions().get(0)).getDelegate();
        Assert.assertEquals(action.getName(), "send");
        
        Assert.assertEquals(action.getEndpoint(), soapClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageContentBuilder.class);

        StaticMessageContentBuilder messageBuilder = (StaticMessageContentBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);

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
                soap().client(soapClient)
                    .send()
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .attachment(testAttachment.getContentId(), testAttachment.getContentType(), testAttachment.getContent());
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), SendSoapMessageAction.class);
        
        SendSoapMessageAction action = (SendSoapMessageAction) ((DelegatingTestAction)test.getActions().get(0)).getDelegate();
        Assert.assertEquals(action.getName(), "send");
        
        Assert.assertEquals(action.getEndpoint(), soapClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageContentBuilder.class);

        StaticMessageContentBuilder messageBuilder = (StaticMessageContentBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);

        Assert.assertEquals(action.getAttachments().size(), 1L);
        Assert.assertNull(action.getAttachments().get(0).getContentResourcePath());
        Assert.assertEquals(action.getAttachments().get(0).getContent(), testAttachment.getContent());
        Assert.assertEquals(action.getAttachments().get(0).getContentId(), testAttachment.getContentId());
        Assert.assertEquals(action.getAttachments().get(0).getContentType(), testAttachment.getContentType());
        Assert.assertEquals(action.getAttachments().get(0).getCharsetName(), testAttachment.getCharsetName());
    }

    @Test
    public void testMultipleSoapAttachmentData() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                soap().client(soapClient)
                        .send()
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .attachment(testAttachment.getContentId() + 1, testAttachment.getContentType(), testAttachment.getContent() + 1)
                        .attachment(testAttachment.getContentId() + 2, testAttachment.getContentType(), testAttachment.getContent() + 2);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), SendSoapMessageAction.class);

        SendSoapMessageAction action = (SendSoapMessageAction) ((DelegatingTestAction)test.getActions().get(0)).getDelegate();
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), soapClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageContentBuilder.class);

        StaticMessageContentBuilder messageBuilder = (StaticMessageContentBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
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
    }
    
    @Test
    public void testSoapAttachmentResource() throws IOException {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                soap().client(soapClient)
                    .send()
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .attachment(testAttachment.getContentId(), testAttachment.getContentType(), resource);
            }
        };
        
        reset(resource);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("someAttachmentData".getBytes()));
        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), SendSoapMessageAction.class);
        
        SendSoapMessageAction action = (SendSoapMessageAction) ((DelegatingTestAction)test.getActions().get(0)).getDelegate();
        Assert.assertEquals(action.getName(), "send");
        
        Assert.assertEquals(action.getEndpoint(), soapClient);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageContentBuilder.class);

        StaticMessageContentBuilder messageBuilder = (StaticMessageContentBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        
        Assert.assertEquals(action.getAttachments().get(0).getContent(), "someAttachmentData");
        Assert.assertEquals(action.getAttachments().get(0).getContentId(), testAttachment.getContentId());
        Assert.assertEquals(action.getAttachments().get(0).getContentType(), testAttachment.getContentType());
        Assert.assertEquals(action.getAttachments().get(0).getCharsetName(), testAttachment.getCharsetName());
    }
    
    @Test
    public void testSendBuilderWithEndpointName() {
        reset(applicationContextMock);
        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());
        MockTestDesigner builder = new MockTestDesigner(applicationContextMock, context) {
            @Override
            public void configure() {
                soap().client("soapClient")
                    .send()
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .header("operation", "soapOperation")
                    .attachment(testAttachment);

                send("otherClient")
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), SendSoapMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(1)).getDelegate().getClass(), SendMessageAction.class);
        
        SendMessageAction action = (SendSoapMessageAction) ((DelegatingTestAction)test.getActions().get(0)).getDelegate();
        Assert.assertEquals(action.getName(), "send");
        Assert.assertEquals(action.getEndpointUri(), "soapClient");
        
        StaticMessageContentBuilder messageBuilder = (StaticMessageContentBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 1L);
        Assert.assertTrue(messageBuilder.getMessageHeaders().containsKey("operation"));
        
        action = (SendMessageAction) ((DelegatingTestAction)test.getActions().get(1)).getDelegate();
        Assert.assertEquals(action.getName(), "send");
        Assert.assertEquals(action.getEndpointUri(), "otherClient");
    }

    @Test
    public void testSendBuilderWithEndpointNameDeprecated() {
        reset(applicationContextMock);
        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());
        MockTestDesigner builder = new MockTestDesigner(applicationContextMock, context) {
            @Override
            public void configure() {
                send("soapClient")
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .header("operation", "soapOperation")
                    .soap()
                    .attachment(testAttachment);

                send("otherClient")
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), SendSoapMessageAction.class);
        Assert.assertEquals(test.getActions().get(1).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(1)).getDelegate().getClass(), SendMessageAction.class);

        SendMessageAction action = (SendSoapMessageAction) ((DelegatingTestAction)test.getActions().get(0)).getDelegate();
        Assert.assertEquals(action.getName(), "send");
        Assert.assertEquals(action.getEndpointUri(), "soapClient");

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 1L);
        Assert.assertTrue(messageBuilder.getMessageHeaders().containsKey("operation"));

        action = (SendMessageAction) ((DelegatingTestAction)test.getActions().get(1)).getDelegate();
        Assert.assertEquals(action.getName(), "send");
        Assert.assertEquals(action.getEndpointUri(), "otherClient");
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,
          expectedExceptionsMessageRegExp = "Invalid use of http and soap action builder")
    public void testSendBuilderWithSoapAndHttpMixed() {
        reset(applicationContextMock);
        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());
        MockTestDesigner builder = new MockTestDesigner(applicationContextMock, context) {
            @Override
            public void configure() {
                send("soapClient")
                        .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                        .header("operation", "soapOperation")
                        .soap()
                        .attachment(testAttachment)
                        .http();
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        SendMessageAction action = ((SendMessageAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");
        Assert.assertEquals(action.getEndpointUri(), "soapClient");
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());

    }
    
}
