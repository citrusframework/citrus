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
import com.consol.citrus.message.Message;
import com.consol.citrus.messaging.Producer;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.ws.SoapAttachment;
import com.consol.citrus.ws.message.SoapMessage;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class SendSoapMessageActionTest extends AbstractTestNGUnitTest {
    
    private Endpoint webServiceEndpoint = EasyMock.createMock(Endpoint.class);
    private Producer producer = EasyMock.createMock(Producer.class);
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithDefaultAttachmentDataTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setEndpoint(webServiceEndpoint);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        soapMessageAction.setAttachmentData("<TestAttachment><Message>Hello World!</Message></TestAttachment>");
        
        soapMessageAction.setMessageBuilder(messageBuilder);
        
        reset(webServiceEndpoint, producer);

        expect(webServiceEndpoint.createProducer()).andReturn(producer).once();
        producer.send((Message) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                SoapAttachment constructedAttachment = (SoapAttachment)((SoapMessage)EasyMock.getCurrentArguments()[0]).getAttachments().get(0);
                Assert.assertNull(constructedAttachment.getContentId());
                Assert.assertEquals(constructedAttachment.getContentType(), "text/plain");
                Assert.assertEquals(constructedAttachment.getContent(), "<TestAttachment><Message>Hello World!</Message></TestAttachment>");
                Assert.assertEquals(constructedAttachment.getCharsetName(), "UTF-8");
                
                return null;
            }
        }).once();
        
        expect(webServiceEndpoint.getActor()).andReturn(null).anyTimes();
        
        replay(webServiceEndpoint, producer);
        
        soapMessageAction.execute(context);
        
        verify(webServiceEndpoint, producer);
    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithAttachmentDataTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setEndpoint(webServiceEndpoint);
        
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        soapMessageAction.setMessageBuilder(messageBuilder);
        
        soapMessageAction.setContentId("myAttachment");
        soapMessageAction.setContentType("text/xml");
        soapMessageAction.setAttachmentData("<TestAttachment><Message>Hello World!</Message></TestAttachment>");
        soapMessageAction.setCharsetName("UTF-16");

        reset(webServiceEndpoint, producer);

        expect(webServiceEndpoint.createProducer()).andReturn(producer).once();
        producer.send((Message) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                SoapAttachment constructedAttachment = (SoapAttachment)((SoapMessage)EasyMock.getCurrentArguments()[0]).getAttachments().get(0);
                Assert.assertEquals(constructedAttachment.getContentId(), "myAttachment");
                Assert.assertEquals(constructedAttachment.getContentType(), "text/xml");
                Assert.assertEquals(constructedAttachment.getContent(), "<TestAttachment><Message>Hello World!</Message></TestAttachment>");
                Assert.assertEquals(constructedAttachment.getCharsetName(), "UTF-16");
                
                return null;
            }
        }).once();
        
        expect(webServiceEndpoint.getActor()).andReturn(null).anyTimes();
        
        replay(webServiceEndpoint, producer);
        
        soapMessageAction.execute(context);
        
        verify(webServiceEndpoint, producer);
    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithEmptyAttachmentContentTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setEndpoint(webServiceEndpoint);
        
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        soapMessageAction.setMessageBuilder(messageBuilder);
        
        reset(webServiceEndpoint, producer);

        expect(webServiceEndpoint.createProducer()).andReturn(producer).once();
        producer.send((Message) anyObject());
        expectLastCall().once();
        
        expect(webServiceEndpoint.getActor()).andReturn(null).anyTimes();
        
        replay(webServiceEndpoint, producer);
        
        soapMessageAction.execute(context);
        
        verify(webServiceEndpoint, producer);
    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithAttachmentResourceTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setEndpoint(webServiceEndpoint);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        soapMessageAction.setAttachmentResourcePath("classpath:com/consol/citrus/ws/actions/test-attachment.xml");

        soapMessageAction.setMessageBuilder(messageBuilder);
        
        reset(webServiceEndpoint, producer);

        expect(webServiceEndpoint.createProducer()).andReturn(producer).once();
        producer.send((Message) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                SoapAttachment constructedAttachment = (SoapAttachment)((SoapMessage)EasyMock.getCurrentArguments()[0]).getAttachments().get(0);
                Assert.assertNull(constructedAttachment.getContentId());
                Assert.assertEquals(constructedAttachment.getContentType(), "text/plain");
                Assert.assertEquals(constructedAttachment.getContent(), "<TestAttachment><Message>Hello World!</Message></TestAttachment>");
                Assert.assertEquals(constructedAttachment.getCharsetName(), "UTF-8");
                
                return null;
            }
        }).once();
        
        expect(webServiceEndpoint.getActor()).andReturn(null).anyTimes();
        
        replay(webServiceEndpoint, producer);
        
        soapMessageAction.execute(context);
        
        verify(webServiceEndpoint, producer);
    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithAttachmentDataVariableSupportTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setEndpoint(webServiceEndpoint);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        context.setVariable("myText", "Hello World!");
        
        soapMessageAction.setAttachmentData("<TestAttachment><Message>${myText}</Message></TestAttachment>");

        soapMessageAction.setMessageBuilder(messageBuilder);
        
        reset(webServiceEndpoint, producer);

        expect(webServiceEndpoint.createProducer()).andReturn(producer).once();
        producer.send((Message) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                SoapAttachment constructedAttachment = (SoapAttachment)((SoapMessage)EasyMock.getCurrentArguments()[0]).getAttachments().get(0);
                Assert.assertNull(constructedAttachment.getContentId());
                Assert.assertEquals(constructedAttachment.getContentType(), "text/plain");
                Assert.assertEquals(constructedAttachment.getContent(), "<TestAttachment><Message>Hello World!</Message></TestAttachment>");
                Assert.assertEquals(constructedAttachment.getCharsetName(), "UTF-8");
                
                return null;
            }
        }).once();
        
        expect(webServiceEndpoint.getActor()).andReturn(null).anyTimes();
        
        replay(webServiceEndpoint, producer);
        
        soapMessageAction.execute(context);
        
        verify(webServiceEndpoint, producer);
    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithAttachmentResourceVariablesSupportTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setEndpoint(webServiceEndpoint);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        context.setVariable("myText", "Hello World!");
        
        soapMessageAction.setAttachmentResourcePath("classpath:com/consol/citrus/ws/actions/test-attachment-with-variables.xml");

        soapMessageAction.setMessageBuilder(messageBuilder);
        
        reset(webServiceEndpoint, producer);

        expect(webServiceEndpoint.createProducer()).andReturn(producer).once();
        producer.send((Message) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                SoapAttachment constructedAttachment = (SoapAttachment)((SoapMessage)EasyMock.getCurrentArguments()[0]).getAttachments().get(0);
                Assert.assertNull(constructedAttachment.getContentId());
                Assert.assertEquals(constructedAttachment.getContentType(), "text/plain");
                Assert.assertEquals(constructedAttachment.getContent(), "<TestAttachment><Message>Hello World!</Message></TestAttachment>");
                Assert.assertEquals(constructedAttachment.getCharsetName(), "UTF-8");
                
                return null;
            }
        }).once();
        
        expect(webServiceEndpoint.getActor()).andReturn(null).anyTimes();
        
        replay(webServiceEndpoint, producer);
        
        soapMessageAction.execute(context);
        
        verify(webServiceEndpoint, producer);
    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithHeaderContentTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setEndpoint(webServiceEndpoint);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        messageBuilder.setMessageHeaderData("<TestHeader><operation>soapOperation</operation></TestHeader>");
        
        soapMessageAction.setMessageBuilder(messageBuilder);

        reset(webServiceEndpoint, producer);

        expect(webServiceEndpoint.createProducer()).andReturn(producer).once();
        producer.send((Message) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Message constructedMessage = (Message)EasyMock.getCurrentArguments()[0];

                Assert.assertEquals(constructedMessage.getHeaderData().size(), 1L);
                Assert.assertEquals(constructedMessage.getHeaderData().get(0),
                        "<TestHeader><operation>soapOperation</operation></TestHeader>");
                
                return null;
            }
        }).once();
        
        expect(webServiceEndpoint.getActor()).andReturn(null).anyTimes();
        
        replay(webServiceEndpoint, producer);
        
        soapMessageAction.execute(context);
        
        verify(webServiceEndpoint, producer);
    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithHeaderResourceTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setEndpoint(webServiceEndpoint);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        messageBuilder.setMessageHeaderResourcePath("classpath:com/consol/citrus/ws/actions/test-header-resource.xml");
        
        soapMessageAction.setMessageBuilder(messageBuilder);
        
        reset(webServiceEndpoint, producer);

        expect(webServiceEndpoint.createProducer()).andReturn(producer).once();
        producer.send((Message) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Message constructedMessage = (Message)EasyMock.getCurrentArguments()[0];

                Assert.assertEquals(constructedMessage.getHeaderData().size(), 1L);
                Assert.assertEquals(constructedMessage.getHeaderData().get(0),
                        "<TestHeader><operation>soapOperation</operation></TestHeader>");
                
                return null;
            }
        }).once();
        
        expect(webServiceEndpoint.getActor()).andReturn(null).anyTimes();
        
        replay(webServiceEndpoint, producer);
        
        soapMessageAction.execute(context);
        
        verify(webServiceEndpoint, producer);
    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithHeaderContentVariableSupportTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setEndpoint(webServiceEndpoint);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        context.setVariable("operation", "soapOperation");
        
        messageBuilder.setMessageHeaderData("<TestHeader><operation>${operation}</operation></TestHeader>");
        
        soapMessageAction.setMessageBuilder(messageBuilder);

        reset(webServiceEndpoint, producer);

        expect(webServiceEndpoint.createProducer()).andReturn(producer).once();
        producer.send((Message) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Message constructedMessage = (Message)EasyMock.getCurrentArguments()[0];

                Assert.assertEquals(constructedMessage.getHeaderData().size(), 1L);
                Assert.assertEquals(constructedMessage.getHeaderData().get(0),
                        "<TestHeader><operation>soapOperation</operation></TestHeader>");
                
                return null;
            }
        }).once();
        
        expect(webServiceEndpoint.getActor()).andReturn(null).anyTimes();
        
        replay(webServiceEndpoint, producer);
        
        soapMessageAction.execute(context);
        
        verify(webServiceEndpoint, producer);
    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithHeaderResourceVariableSupportTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setEndpoint(webServiceEndpoint);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        context.setVariable("operation", "soapOperation");
        
        messageBuilder.setMessageHeaderResourcePath("classpath:com/consol/citrus/ws/actions/test-header-resource-with-variables.xml");
        
        soapMessageAction.setMessageBuilder(messageBuilder);

        reset(webServiceEndpoint, producer);

        expect(webServiceEndpoint.createProducer()).andReturn(producer).once();
        producer.send((Message) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Message constructedMessage = (Message)EasyMock.getCurrentArguments()[0];

                Assert.assertEquals(constructedMessage.getHeaderData().size(), 1L);
                Assert.assertEquals(constructedMessage.getHeaderData().get(0),
                        "<TestHeader><operation>soapOperation</operation></TestHeader>");
                
                return null;
            }
        }).once();
        
        expect(webServiceEndpoint.getActor()).andReturn(null).anyTimes();
        
        replay(webServiceEndpoint, producer);
        
        soapMessageAction.execute(context);
        
        verify(webServiceEndpoint, producer);
    }

}
