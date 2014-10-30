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

import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.EndpointConfiguration;
import com.consol.citrus.message.Message;
import com.consol.citrus.messaging.Consumer;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.xml.DomXmlMessageValidator;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import com.consol.citrus.ws.message.SoapAttachment;
import com.consol.citrus.ws.message.SoapMessage;
import com.consol.citrus.ws.validation.SoapAttachmentValidator;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
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

        SoapAttachment attachment = new SoapAttachment();
        attachment.setContent("TestAttachment!");
        soapMessageAction.setAttachments(Collections.singletonList(attachment));

        Message controlMessage = new SoapMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration, attachmentValidator);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        
        attachmentValidator.validateAttachment((SoapMessage)anyObject(), anyObject(List.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Assert.assertEquals(((List<SoapAttachment>)EasyMock.getCurrentArguments()[1]).size(), 1L);
                SoapAttachment soapAttachment = ((List<SoapAttachment>)EasyMock.getCurrentArguments()[1]).get(0);
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

        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId("myAttachment");
        attachment.setContentType("text/xml");
        attachment.setContent("<TestAttachment><Message>Hello World!</Message></TestAttachment>");
        attachment.setCharsetName("UTF-16");
        soapMessageAction.setAttachments(Collections.singletonList(attachment));

        Message controlMessage = new SoapMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration, attachmentValidator);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        
        attachmentValidator.validateAttachment((SoapMessage)anyObject(), (List)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Assert.assertEquals(((List<SoapAttachment>)EasyMock.getCurrentArguments()[1]).size(), 1L);
                SoapAttachment soapAttachment = ((List<SoapAttachment>)EasyMock.getCurrentArguments()[1]).get(0);
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
    public void testSoapMessageWithMultipleAttachmentDataTest() throws Exception {
        ReceiveSoapMessageAction soapMessageAction = new ReceiveSoapMessageAction();
        soapMessageAction.setEndpoint(endpoint);
        soapMessageAction.setAttachmentValidator(attachmentValidator);

        soapMessageAction.setValidator(new DomXmlMessageValidator());
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        List<SoapAttachment> attachments = new ArrayList<SoapAttachment>();
        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId("1stAttachment");
        attachment.setContentType("text/xml");
        attachment.setContent("<TestAttachment><Message>Hello World1!</Message></TestAttachment>");
        attachment.setCharsetName("UTF-8");
        attachments.add(attachment);

        SoapAttachment attachment2 = new SoapAttachment();
        attachment2.setContentId("2ndAttachment");
        attachment2.setContentType("text/xml");
        attachment2.setContent("<TestAttachment><Message>Hello World2!</Message></TestAttachment>");
        attachment2.setCharsetName("UTF-16");
        attachments.add(attachment2);
        soapMessageAction.setAttachments(attachments);

        Message controlMessage = new SoapMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration, attachmentValidator);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();

        attachmentValidator.validateAttachment((SoapMessage)anyObject(), (List)anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Assert.assertEquals(((List<SoapAttachment>)EasyMock.getCurrentArguments()[1]).size(), 2L);

                SoapAttachment soapAttachment = ((List<SoapAttachment>)EasyMock.getCurrentArguments()[1]).get(0);
                Assert.assertEquals(soapAttachment.getContent(), "<TestAttachment><Message>Hello World1!</Message></TestAttachment>");
                Assert.assertEquals(soapAttachment.getContentId(), "1stAttachment");
                Assert.assertEquals(soapAttachment.getContentType(), "text/xml");
                Assert.assertEquals(soapAttachment.getCharsetName(), "UTF-8");

                soapAttachment = ((List<SoapAttachment>)EasyMock.getCurrentArguments()[1]).get(1);
                Assert.assertEquals(soapAttachment.getContent(), "<TestAttachment><Message>Hello World2!</Message></TestAttachment>");
                Assert.assertEquals(soapAttachment.getContentId(), "2ndAttachment");
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

        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId("myAttachment");
        attachment.setContent("");
        soapMessageAction.setAttachments(Collections.singletonList(attachment));

        Message controlMessage = new SoapMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration, attachmentValidator);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        
        attachmentValidator.validateAttachment((SoapMessage)anyObject(), (List) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Assert.assertEquals(((List<SoapAttachment>)EasyMock.getCurrentArguments()[1]).size(), 1L);
                SoapAttachment soapAttachment = ((List<SoapAttachment>)EasyMock.getCurrentArguments()[1]).get(0);
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
        
        Message controlMessage = new SoapMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration, attachmentValidator);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();

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

        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId("myAttachment");
        attachment.setContentType("text/xml");
        attachment.setContentResourcePath("classpath:com/consol/citrus/ws/actions/test-attachment.xml");
        soapMessageAction.setAttachments(Collections.singletonList(attachment));

        Message controlMessage = new SoapMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration, attachmentValidator);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        
        attachmentValidator.validateAttachment((SoapMessage)anyObject(), (List) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Assert.assertEquals(((List<SoapAttachment>)EasyMock.getCurrentArguments()[1]).size(), 1L);
                SoapAttachment soapAttachment = ((List<SoapAttachment>)EasyMock.getCurrentArguments()[1]).get(0);
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

        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId("myAttachment");
        attachment.setContentType("text/xml");
        attachment.setContentResourcePath("classpath:com/consol/citrus/ws/actions/test-attachment-with-variables.xml");
        soapMessageAction.setAttachments(Collections.singletonList(attachment));

        Message controlMessage = new SoapMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration, attachmentValidator);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        
        attachmentValidator.validateAttachment((SoapMessage)anyObject(), (List) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Assert.assertEquals(((List<SoapAttachment>)EasyMock.getCurrentArguments()[1]).size(), 1L);
                SoapAttachment soapAttachment = ((List<SoapAttachment>)EasyMock.getCurrentArguments()[1]).get(0);
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

        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId("myAttachment");
        attachment.setContentType("text/xml");
        attachment.setContent("<TestAttachment><Message>${myText}</Message></TestAttachment>");
        soapMessageAction.setAttachments(Collections.singletonList(attachment));

        Message controlMessage = new SoapMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration, attachmentValidator);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();

        attachmentValidator.validateAttachment((SoapMessage)anyObject(), (List) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Assert.assertEquals(((List<SoapAttachment>)EasyMock.getCurrentArguments()[1]).size(), 1L);
                SoapAttachment soapAttachment = ((List<SoapAttachment>)EasyMock.getCurrentArguments()[1]).get(0);
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
