/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.ws.actions;

import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.EndpointConfiguration;
import com.consol.citrus.messaging.Consumer;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.xml.DomXmlMessageValidator;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import com.consol.citrus.ws.SoapAttachment;
import com.consol.citrus.ws.validation.SoapAttachmentValidator;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ReceiveSoapMessageActionTest extends AbstractTestNGUnitTest {

    private Endpoint endpoint = EasyMock.createMock(Endpoint.class);
    private Consumer consumer = EasyMock.createMock(Consumer.class);
    private EndpointConfiguration endpointConfiguration = EasyMock.createMock(EndpointConfiguration.class);

    private SoapAttachmentValidator attachmentValidator = EasyMock.createMock(SoapAttachmentValidator.class);
    
    @Test
    public void testSoapMessageWithDefaultAttachmentDataTest() throws Exception {
        ReceiveSoapMessageAction soapMessageAction = new ReceiveSoapMessageAction();
        soapMessageAction.setEndpoint(endpoint);
        soapMessageAction.setAttachmentValidator(attachmentValidator);

        soapMessageAction.setValidator(new DomXmlMessageValidator());
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        soapMessageAction.setAttachmentData("TestAttachment!");

        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(controlHeaders)
                                    .build();

        reset(endpoint, consumer, endpointConfiguration, attachmentValidator);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyLong())).andReturn(controlMessage).once();
        
        attachmentValidator.validateAttachment((Message)anyObject(), (SoapAttachment) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                SoapAttachment soapAttachment = (SoapAttachment)EasyMock.getCurrentArguments()[1];
                Assert.assertEquals(soapAttachment.getContent(), "TestAttachment!");
                Assert.assertNull(soapAttachment.getContentId());
                Assert.assertEquals(soapAttachment.getContentType(), "text/plain");
                return null;
            }
        });

        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration, attachmentValidator);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        soapMessageAction.setValidationContexts(validationContexts);
        soapMessageAction.execute(context);

        verify(endpoint, consumer, endpointConfiguration, attachmentValidator);
    }

    @Test
    public void testSoapMessageWithAttachmentDataTest() throws Exception {
        ReceiveSoapMessageAction soapMessageAction = new ReceiveSoapMessageAction();
        soapMessageAction.setEndpoint(endpoint);
        soapMessageAction.setAttachmentValidator(attachmentValidator);

        soapMessageAction.setValidator(new DomXmlMessageValidator());
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        soapMessageAction.setContentId("myAttachment");
        soapMessageAction.setContentType("text/xml");
        soapMessageAction.setAttachmentData("<TestAttachment><Message>Hello World!</Message></TestAttachment>");
        soapMessageAction.setCharsetName("UTF-16");
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(controlHeaders)
                                    .build();

        reset(endpoint, consumer, endpointConfiguration, attachmentValidator);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyLong())).andReturn(controlMessage).once();
        
        attachmentValidator.validateAttachment((Message)anyObject(), (SoapAttachment) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                SoapAttachment soapAttachment = (SoapAttachment)EasyMock.getCurrentArguments()[1];
                Assert.assertEquals(soapAttachment.getContent(), "<TestAttachment><Message>Hello World!</Message></TestAttachment>");
                Assert.assertEquals(soapAttachment.getContentId(), "myAttachment");
                Assert.assertEquals(soapAttachment.getContentType(), "text/xml");
                Assert.assertEquals(soapAttachment.getCharsetName(), "UTF-16");
                return null;
            }
        });

        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration, attachmentValidator);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        soapMessageAction.setValidationContexts(validationContexts);
        soapMessageAction.execute(context);

        verify(endpoint, consumer, endpointConfiguration, attachmentValidator);
    }
    
    @Test
    public void testSoapMessageWithEmptyAttachmentContentTest() throws Exception {
        ReceiveSoapMessageAction soapMessageAction = new ReceiveSoapMessageAction();
        soapMessageAction.setEndpoint(endpoint);
        soapMessageAction.setAttachmentValidator(attachmentValidator);

        soapMessageAction.setValidator(new DomXmlMessageValidator());
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        soapMessageAction.setContentId("myAttachment");
        soapMessageAction.setAttachmentData("");
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(controlHeaders)
                                    .build();

        reset(endpoint, consumer, endpointConfiguration, attachmentValidator);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyLong())).andReturn(controlMessage).once();
        
        attachmentValidator.validateAttachment((Message)anyObject(), (SoapAttachment) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                SoapAttachment soapAttachment = (SoapAttachment)EasyMock.getCurrentArguments()[1];
                Assert.assertEquals(soapAttachment.getContent(), "");
                Assert.assertEquals(soapAttachment.getContentId(), "myAttachment");
                Assert.assertEquals(soapAttachment.getContentType(), "text/plain");
                Assert.assertEquals(soapAttachment.getCharsetName(), "UTF-8");
                return null;
            }
        });

        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration, attachmentValidator);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        soapMessageAction.setValidationContexts(validationContexts);
        soapMessageAction.execute(context);

        verify(endpoint, consumer, endpointConfiguration, attachmentValidator);
    }
    
    @Test
    public void testSoapMessageWithNoAttachmentExpected() throws Exception {
        ReceiveSoapMessageAction soapMessageAction = new ReceiveSoapMessageAction();
        soapMessageAction.setEndpoint(endpoint);
        soapMessageAction.setAttachmentValidator(attachmentValidator);
        
        soapMessageAction.setValidator(new DomXmlMessageValidator());
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(controlHeaders)
                                    .build();

        reset(endpoint, consumer, endpointConfiguration, attachmentValidator);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyLong())).andReturn(controlMessage).once();

        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration, attachmentValidator);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        soapMessageAction.setValidationContexts(validationContexts);
        soapMessageAction.execute(context);

        verify(endpoint, consumer, endpointConfiguration, attachmentValidator);
    }
    
    @Test
    public void testSoapMessageWithAttachmentResourceTest() throws Exception {
        ReceiveSoapMessageAction soapMessageAction = new ReceiveSoapMessageAction();
        soapMessageAction.setEndpoint(endpoint);
        soapMessageAction.setAttachmentValidator(attachmentValidator);

        soapMessageAction.setValidator(new DomXmlMessageValidator());
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        soapMessageAction.setContentId("myAttachment");
        soapMessageAction.setContentType("text/xml");
        soapMessageAction.setAttachmentResourcePath("classpath:com/consol/citrus/ws/actions/test-attachment.xml");
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(controlHeaders)
                                    .build();

        reset(endpoint, consumer, endpointConfiguration, attachmentValidator);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyLong())).andReturn(controlMessage).once();
        
        attachmentValidator.validateAttachment((Message)anyObject(), (SoapAttachment) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                SoapAttachment soapAttachment = (SoapAttachment)EasyMock.getCurrentArguments()[1];
                Assert.assertEquals(soapAttachment.getContent(), "<TestAttachment><Message>Hello World!</Message></TestAttachment>");
                Assert.assertEquals(soapAttachment.getContentId(), "myAttachment");
                Assert.assertEquals(soapAttachment.getContentType(), "text/xml");
                Assert.assertEquals(soapAttachment.getCharsetName(), "UTF-8");
                return null;
            }
        });

        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration, attachmentValidator);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        soapMessageAction.setValidationContexts(validationContexts);
        soapMessageAction.execute(context);

        verify(endpoint, consumer, endpointConfiguration, attachmentValidator);
    }
    
    @Test
    public void testSoapMessageWithAttachmentResourceVariablesSupportTest() throws Exception {
        ReceiveSoapMessageAction soapMessageAction = new ReceiveSoapMessageAction();
        soapMessageAction.setEndpoint(endpoint);
        soapMessageAction.setAttachmentValidator(attachmentValidator);

        soapMessageAction.setValidator(new DomXmlMessageValidator());
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        context.setVariable("myText", "Hello World!");
        
        soapMessageAction.setContentId("myAttachment");
        soapMessageAction.setContentType("text/xml");
        soapMessageAction.setAttachmentResourcePath("classpath:com/consol/citrus/ws/actions/test-attachment-with-variables.xml");
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(controlHeaders)
                                    .build();

        reset(endpoint, consumer, endpointConfiguration, attachmentValidator);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyLong())).andReturn(controlMessage).once();
        
        attachmentValidator.validateAttachment((Message)anyObject(), (SoapAttachment) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                SoapAttachment soapAttachment = (SoapAttachment)EasyMock.getCurrentArguments()[1];
                Assert.assertEquals(soapAttachment.getContent(), "<TestAttachment><Message>Hello World!</Message></TestAttachment>");
                Assert.assertEquals(soapAttachment.getContentId(), "myAttachment");
                Assert.assertEquals(soapAttachment.getContentType(), "text/xml");
                Assert.assertEquals(soapAttachment.getCharsetName(), "UTF-8");
                return null;
            }
        });

        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration, attachmentValidator);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        soapMessageAction.setValidationContexts(validationContexts);
        soapMessageAction.execute(context);

        verify(endpoint, consumer, endpointConfiguration, attachmentValidator);
    }
    
    @Test
    public void testSoapMessageWithAttachmentDataVariablesSupportTest() throws Exception {
        ReceiveSoapMessageAction soapMessageAction = new ReceiveSoapMessageAction();
        soapMessageAction.setEndpoint(endpoint);
        soapMessageAction.setAttachmentValidator(attachmentValidator);

        soapMessageAction.setValidator(new DomXmlMessageValidator());
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        context.setVariable("myText", "Hello World!");
        
        soapMessageAction.setContentId("myAttachment");
        soapMessageAction.setContentType("text/xml");
        soapMessageAction.setAttachmentData("<TestAttachment><Message>${myText}</Message></TestAttachment>");
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        Message controlMessage = MessageBuilder.withPayload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                                    .copyHeaders(controlHeaders)
                                    .build();

        reset(endpoint, consumer, endpointConfiguration, attachmentValidator);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyLong())).andReturn(controlMessage).once();

        attachmentValidator.validateAttachment((Message)anyObject(), (SoapAttachment) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                SoapAttachment soapAttachment = (SoapAttachment)EasyMock.getCurrentArguments()[1];
                Assert.assertEquals(soapAttachment.getContent(), "<TestAttachment><Message>Hello World!</Message></TestAttachment>");
                Assert.assertEquals(soapAttachment.getContentId(), "myAttachment");
                Assert.assertEquals(soapAttachment.getContentType(), "text/xml");
                Assert.assertEquals(soapAttachment.getCharsetName(), "UTF-8");
                return null;
            }
        });

        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration, attachmentValidator);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        soapMessageAction.setValidationContexts(validationContexts);
        soapMessageAction.execute(context);

        verify(endpoint, consumer, endpointConfiguration, attachmentValidator);
    }
}
