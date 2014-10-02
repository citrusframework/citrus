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

import com.consol.citrus.channel.ChannelEndpoint;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.MessageHeaders;
import com.consol.citrus.message.Message;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.ws.SoapAttachment;
import com.consol.citrus.ws.client.WebServiceClient;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.springframework.ws.mime.Attachment;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class SendSoapMessageActionTest extends AbstractTestNGUnitTest {
    
    private WebServiceClient webServiceClient = EasyMock.createMock(WebServiceClient.class);
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithDefaultAttachmentDataTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setEndpoint(webServiceClient);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        soapMessageAction.setAttachmentData("<TestAttachment><Message>Hello World!</Message></TestAttachment>");
        
        soapMessageAction.setMessageBuilder(messageBuilder);
        
        reset(webServiceClient);
        
        webServiceClient.send((Message) anyObject(), (Attachment) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                SoapAttachment constructedAttachment = (SoapAttachment)EasyMock.getCurrentArguments()[1];
                Assert.assertNull(constructedAttachment.getContentId());
                Assert.assertEquals(constructedAttachment.getContentType(), "text/plain");
                Assert.assertEquals(constructedAttachment.getContent(), "<TestAttachment><Message>Hello World!</Message></TestAttachment>");
                Assert.assertEquals(constructedAttachment.getCharsetName(), "UTF-8");
                
                return null;
            }
        }).once();
        
        expect(webServiceClient.getActor()).andReturn(null).anyTimes();
        
        replay(webServiceClient);
        
        soapMessageAction.execute(context);
        
        verify(webServiceClient);
    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithAttachmentDataTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setEndpoint(webServiceClient);
        
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        soapMessageAction.setMessageBuilder(messageBuilder);
        
        soapMessageAction.setContentId("myAttachment");
        soapMessageAction.setContentType("text/xml");
        soapMessageAction.setAttachmentData("<TestAttachment><Message>Hello World!</Message></TestAttachment>");
        soapMessageAction.setCharsetName("UTF-16");

        reset(webServiceClient);
        
        webServiceClient.send((Message) anyObject(), (Attachment) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                SoapAttachment constructedAttachment = (SoapAttachment)EasyMock.getCurrentArguments()[1];
                Assert.assertEquals(constructedAttachment.getContentId(), "myAttachment");
                Assert.assertEquals(constructedAttachment.getContentType(), "text/xml");
                Assert.assertEquals(constructedAttachment.getContent(), "<TestAttachment><Message>Hello World!</Message></TestAttachment>");
                Assert.assertEquals(constructedAttachment.getCharsetName(), "UTF-16");
                
                return null;
            }
        }).once();
        
        expect(webServiceClient.getActor()).andReturn(null).anyTimes();
        
        replay(webServiceClient);
        
        soapMessageAction.execute(context);
        
        verify(webServiceClient);
    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithEmptyAttachmentContentTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setEndpoint(webServiceClient);
        
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        soapMessageAction.setMessageBuilder(messageBuilder);
        
        reset(webServiceClient);
        
        webServiceClient.send((Message) anyObject());
        expectLastCall().once();
        
        expect(webServiceClient.getActor()).andReturn(null).anyTimes();
        
        replay(webServiceClient);
        
        soapMessageAction.execute(context);
        
        verify(webServiceClient);
    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithAttachmentResourceTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setEndpoint(webServiceClient);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        soapMessageAction.setAttachmentResourcePath("classpath:com/consol/citrus/ws/actions/test-attachment.xml");

        soapMessageAction.setMessageBuilder(messageBuilder);
        
        reset(webServiceClient);
        
        webServiceClient.send((Message) anyObject(), (Attachment) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                SoapAttachment constructedAttachment = (SoapAttachment)EasyMock.getCurrentArguments()[1];
                Assert.assertNull(constructedAttachment.getContentId());
                Assert.assertEquals(constructedAttachment.getContentType(), "text/plain");
                Assert.assertEquals(constructedAttachment.getContent(), "<TestAttachment><Message>Hello World!</Message></TestAttachment>");
                Assert.assertEquals(constructedAttachment.getCharsetName(), "UTF-8");
                
                return null;
            }
        }).once();
        
        expect(webServiceClient.getActor()).andReturn(null).anyTimes();
        
        replay(webServiceClient);
        
        soapMessageAction.execute(context);
        
        verify(webServiceClient);
    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithAttachmentDataVariableSupportTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setEndpoint(webServiceClient);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        context.setVariable("myText", "Hello World!");
        
        soapMessageAction.setAttachmentData("<TestAttachment><Message>${myText}</Message></TestAttachment>");

        soapMessageAction.setMessageBuilder(messageBuilder);
        
        reset(webServiceClient);
        
        webServiceClient.send((Message) anyObject(), (Attachment) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                SoapAttachment constructedAttachment = (SoapAttachment)EasyMock.getCurrentArguments()[1];
                Assert.assertNull(constructedAttachment.getContentId());
                Assert.assertEquals(constructedAttachment.getContentType(), "text/plain");
                Assert.assertEquals(constructedAttachment.getContent(), "<TestAttachment><Message>Hello World!</Message></TestAttachment>");
                Assert.assertEquals(constructedAttachment.getCharsetName(), "UTF-8");
                
                return null;
            }
        }).once();
        
        expect(webServiceClient.getActor()).andReturn(null).anyTimes();
        
        replay(webServiceClient);
        
        soapMessageAction.execute(context);
        
        verify(webServiceClient);
    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithAttachmentResourceVariablesSupportTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setEndpoint(webServiceClient);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        context.setVariable("myText", "Hello World!");
        
        soapMessageAction.setAttachmentResourcePath("classpath:com/consol/citrus/ws/actions/test-attachment-with-variables.xml");

        soapMessageAction.setMessageBuilder(messageBuilder);
        
        reset(webServiceClient);
        
        webServiceClient.send((Message) anyObject(), (Attachment) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                SoapAttachment constructedAttachment = (SoapAttachment)EasyMock.getCurrentArguments()[1];
                Assert.assertNull(constructedAttachment.getContentId());
                Assert.assertEquals(constructedAttachment.getContentType(), "text/plain");
                Assert.assertEquals(constructedAttachment.getContent(), "<TestAttachment><Message>Hello World!</Message></TestAttachment>");
                Assert.assertEquals(constructedAttachment.getCharsetName(), "UTF-8");
                
                return null;
            }
        }).once();
        
        expect(webServiceClient.getActor()).andReturn(null).anyTimes();
        
        replay(webServiceClient);
        
        soapMessageAction.execute(context);
        
        verify(webServiceClient);
    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithHeaderContentTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setEndpoint(webServiceClient);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        messageBuilder.setMessageHeaderData("<TestHeader><operation>soapOperation</operation></TestHeader>");
        
        soapMessageAction.setMessageBuilder(messageBuilder);

        reset(webServiceClient);
        
        webServiceClient.send((Message) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Message constructedMessage = (Message)EasyMock.getCurrentArguments()[0];

                Assert.assertNotNull(constructedMessage.getHeader(MessageHeaders.HEADER_CONTENT));
                Assert.assertEquals(constructedMessage.getHeader(MessageHeaders.HEADER_CONTENT),
                        "<TestHeader><operation>soapOperation</operation></TestHeader>");
                
                return null;
            }
        }).once();
        
        expect(webServiceClient.getActor()).andReturn(null).anyTimes();
        
        replay(webServiceClient);
        
        soapMessageAction.execute(context);
        
        verify(webServiceClient);
    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithHeaderResourceTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setEndpoint(webServiceClient);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        messageBuilder.setMessageHeaderResourcePath("classpath:com/consol/citrus/ws/actions/test-header-resource.xml");
        
        soapMessageAction.setMessageBuilder(messageBuilder);
        
        reset(webServiceClient);
        
        webServiceClient.send((Message) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Message constructedMessage = (Message)EasyMock.getCurrentArguments()[0];

                Assert.assertNotNull(constructedMessage.getHeader(MessageHeaders.HEADER_CONTENT));
                Assert.assertEquals(constructedMessage.getHeader(MessageHeaders.HEADER_CONTENT),
                        "<TestHeader><operation>soapOperation</operation></TestHeader>");
                
                return null;
            }
        }).once();
        
        expect(webServiceClient.getActor()).andReturn(null).anyTimes();
        
        replay(webServiceClient);
        
        soapMessageAction.execute(context);
        
        verify(webServiceClient);
    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithHeaderContentVariableSupportTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setEndpoint(webServiceClient);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        context.setVariable("operation", "soapOperation");
        
        messageBuilder.setMessageHeaderData("<TestHeader><operation>${operation}</operation></TestHeader>");
        
        soapMessageAction.setMessageBuilder(messageBuilder);

        reset(webServiceClient);
        
        webServiceClient.send((Message) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Message constructedMessage = (Message)EasyMock.getCurrentArguments()[0];

                Assert.assertNotNull(constructedMessage.getHeader(MessageHeaders.HEADER_CONTENT));
                Assert.assertEquals(constructedMessage.getHeader(MessageHeaders.HEADER_CONTENT),
                        "<TestHeader><operation>soapOperation</operation></TestHeader>");
                
                return null;
            }
        }).once();
        
        expect(webServiceClient.getActor()).andReturn(null).anyTimes();
        
        replay(webServiceClient);
        
        soapMessageAction.execute(context);
        
        verify(webServiceClient);
    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithHeaderResourceVariableSupportTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setEndpoint(webServiceClient);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        context.setVariable("operation", "soapOperation");
        
        messageBuilder.setMessageHeaderResourcePath("classpath:com/consol/citrus/ws/actions/test-header-resource-with-variables.xml");
        
        soapMessageAction.setMessageBuilder(messageBuilder);

        reset(webServiceClient);
        
        webServiceClient.send((Message) anyObject());
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Message constructedMessage = (Message)EasyMock.getCurrentArguments()[0];

                Assert.assertNotNull(constructedMessage.getHeader(MessageHeaders.HEADER_CONTENT));
                Assert.assertEquals(constructedMessage.getHeader(MessageHeaders.HEADER_CONTENT),
                        "<TestHeader><operation>soapOperation</operation></TestHeader>");
                
                return null;
            }
        }).once();
        
        expect(webServiceClient.getActor()).andReturn(null).anyTimes();
        
        replay(webServiceClient);
        
        soapMessageAction.execute(context);
        
        verify(webServiceClient);
    }
    
    @Test
    public void testWrongEndpointImplementationTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        Endpoint channelEndpoint = new ChannelEndpoint();
        soapMessageAction.setEndpoint(channelEndpoint);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        soapMessageAction.setMessageBuilder(messageBuilder);
        
        try {
            soapMessageAction.execute(context);
        } catch (CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Sending SOAP messages requires a " +
                    "'com.consol.citrus.ws.client.WebServiceClient' but was 'com.consol.citrus.channel.ChannelEndpoint'");
            return;
        }
        
        Assert.fail("Missing exception because of unsupported endpoint implementation");
    }
}
