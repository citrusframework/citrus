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
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.Message;
import com.consol.citrus.messaging.Producer;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.ws.message.SoapAttachment;
import com.consol.citrus.ws.message.SoapMessage;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class SendSoapMessageActionTest extends AbstractTestNGUnitTest {
    
    private Endpoint webServiceEndpoint = EasyMock.createMock(Endpoint.class);
    private Producer producer = EasyMock.createMock(Producer.class);

    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithMtomAttachmentDataTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setEndpoint(webServiceEndpoint);
        soapMessageAction.setMtomEnabled(true);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Text>cid:mtomText</Text></TestRequest>");

        soapMessageAction.setMessageBuilder(messageBuilder);

        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId("mtomText");
        attachment.setContentType("text/xml");
        attachment.setContent("<TestAttachment><Message>Hello World!</Message></TestAttachment>");
        soapMessageAction.setAttachments(Collections.singletonList(attachment));

        reset(webServiceEndpoint, producer);

        expect(webServiceEndpoint.createProducer()).andReturn(producer).once();
        producer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                SoapMessage soapMessage = ((SoapMessage)EasyMock.getCurrentArguments()[0]);
                Assert.assertTrue(soapMessage.isMtomEnabled());
                Assert.assertEquals(soapMessage.getPayload(String.class), "<TestRequest><Text><xop:Include xmlns:xop=\"http://www.w3.org/2004/08/xop/include\" href=\"cid:mtomText\"/></Text></TestRequest>");

                Assert.assertEquals(soapMessage.getAttachments().size(), 1L);
                SoapAttachment constructedAttachment = ((SoapMessage)EasyMock.getCurrentArguments()[0]).getAttachments().get(0);
                Assert.assertEquals(constructedAttachment.getContentId(), "mtomText");
                Assert.assertEquals(constructedAttachment.getContentType(), "text/xml");
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
    public void testSoapMessageWithMtomInlineBase64BinaryAttachmentDataTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setEndpoint(webServiceEndpoint);
        soapMessageAction.setMtomEnabled(true);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Image>cid:mtomImage</Image></TestRequest>");

        soapMessageAction.setMessageBuilder(messageBuilder);

        SoapAttachment attachment = new SoapAttachment();
        attachment.setMtomInline(true);
        attachment.setEncodingType(SoapAttachment.ENCODING_BASE64_BINARY);
        attachment.setContentId("mtomImage");
        attachment.setContentType("image/png");
        attachment.setContent("IMAGE_DATA");
        soapMessageAction.setAttachments(Collections.singletonList(attachment));

        reset(webServiceEndpoint, producer);

        expect(webServiceEndpoint.createProducer()).andReturn(producer).once();
        producer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                SoapMessage soapMessage = ((SoapMessage)EasyMock.getCurrentArguments()[0]);
                Assert.assertTrue(soapMessage.isMtomEnabled());
                Assert.assertEquals(soapMessage.getPayload(String.class), "<TestRequest><Image>SU1BR0VfREFUQQ==</Image></TestRequest>");

                Assert.assertEquals(soapMessage.getAttachments().size(), 0L);
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
    public void testSoapMessageWithMtomInlineHexBinaryAttachmentDataTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setEndpoint(webServiceEndpoint);
        soapMessageAction.setMtomEnabled(true);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Image>cid:mtomImage</Image></TestRequest>");

        soapMessageAction.setMessageBuilder(messageBuilder);

        SoapAttachment attachment = new SoapAttachment();
        attachment.setMtomInline(true);
        attachment.setEncodingType(SoapAttachment.ENCODING_HEX_BINARY);
        attachment.setContentId("mtomImage");
        attachment.setContentType("image/png");
        attachment.setContent("IMAGE_DATA");
        soapMessageAction.setAttachments(Collections.singletonList(attachment));

        reset(webServiceEndpoint, producer);

        expect(webServiceEndpoint.createProducer()).andReturn(producer).once();
        producer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                SoapMessage soapMessage = ((SoapMessage)EasyMock.getCurrentArguments()[0]);
                Assert.assertTrue(soapMessage.isMtomEnabled());
                Assert.assertEquals(soapMessage.getPayload(String.class), "<TestRequest><Image>494D4147455F44415441</Image></TestRequest>");

                Assert.assertEquals(soapMessage.getAttachments().size(), 0L);
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
    public void testSoapMessageWithMtomMissingCidAttachmentDataTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setEndpoint(webServiceEndpoint);
        soapMessageAction.setMtomEnabled(true);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Text>mtomText</Text></TestRequest>");

        soapMessageAction.setMessageBuilder(messageBuilder);

        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId("mtomText");
        attachment.setContentType("text/xml");
        attachment.setContent("<TestAttachment><Message>Hello World!</Message></TestAttachment>");
        soapMessageAction.setAttachments(Collections.singletonList(attachment));

        reset(webServiceEndpoint, producer);

        expect(webServiceEndpoint.createProducer()).andReturn(producer).once();
        producer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                SoapMessage soapMessage = ((SoapMessage)EasyMock.getCurrentArguments()[0]);
                Assert.assertTrue(soapMessage.isMtomEnabled());
                Assert.assertEquals(soapMessage.getPayload(String.class), "<TestRequest><Text>mtomText</Text></TestRequest>");

                Assert.assertEquals(soapMessage.getAttachments().size(), 1L);
                SoapAttachment constructedAttachment = ((SoapMessage)EasyMock.getCurrentArguments()[0]).getAttachments().get(0);
                Assert.assertEquals(constructedAttachment.getContentId(), "mtomText");
                Assert.assertEquals(constructedAttachment.getContentType(), "text/xml");
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
    public void testSoapMessageWithMtomInlineMissingCidAttachmentDataTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setEndpoint(webServiceEndpoint);
        soapMessageAction.setMtomEnabled(true);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Image>mtomImage</Image></TestRequest>");

        soapMessageAction.setMessageBuilder(messageBuilder);

        SoapAttachment attachment = new SoapAttachment();
        attachment.setMtomInline(true);
        attachment.setEncodingType(SoapAttachment.ENCODING_BASE64_BINARY);
        attachment.setContentId("mtomImage");
        attachment.setContentType("image/png");
        attachment.setContent("IMAGE_DATA");
        soapMessageAction.setAttachments(Collections.singletonList(attachment));

        reset(webServiceEndpoint, producer);

        expect(webServiceEndpoint.createProducer()).andReturn(producer).once();
        producer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                SoapMessage soapMessage = ((SoapMessage)EasyMock.getCurrentArguments()[0]);
                Assert.assertTrue(soapMessage.isMtomEnabled());
                Assert.assertEquals(soapMessage.getPayload(String.class), "<TestRequest><Image>mtomImage</Image></TestRequest>");

                Assert.assertEquals(soapMessage.getAttachments().size(), 1L);
                SoapAttachment constructedAttachment = ((SoapMessage)EasyMock.getCurrentArguments()[0]).getAttachments().get(0);
                Assert.assertEquals(constructedAttachment.getContentId(), "mtomImage");
                Assert.assertEquals(constructedAttachment.getContentType(), "image/png");
                Assert.assertEquals(constructedAttachment.getContent(), "IMAGE_DATA");
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
    public void testSoapMessageWithMtomInlineInvalidEncodingTypeAttachmentDataTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setEndpoint(webServiceEndpoint);
        soapMessageAction.setMtomEnabled(true);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Image>cid:mtomImage</Image></TestRequest>");

        soapMessageAction.setMessageBuilder(messageBuilder);

        SoapAttachment attachment = new SoapAttachment();
        attachment.setMtomInline(true);
        attachment.setEncodingType("md5");
        attachment.setContentId("mtomImage");
        attachment.setContentType("image/png");
        attachment.setContent("IMAGE_DATA");
        soapMessageAction.setAttachments(Collections.singletonList(attachment));

        reset(webServiceEndpoint, producer);
        expect(webServiceEndpoint.getActor()).andReturn(null).anyTimes();
        replay(webServiceEndpoint, producer);

        try {
            soapMessageAction.execute(context);
            Assert.fail("Missing exception due to invalid attachment encoding type");
        } catch (CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Unsupported encoding type 'md5' for SOAP attachment: cid:mtomImage - choose one of base64Binary or hexBinary");
            verify(webServiceEndpoint, producer);
        }
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithDefaultAttachmentDataTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setEndpoint(webServiceEndpoint);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        SoapAttachment attachment = new SoapAttachment();
        attachment.setContent("<TestAttachment><Message>Hello World!</Message></TestAttachment>");
        soapMessageAction.setAttachments(Collections.singletonList(attachment));
        
        soapMessageAction.setMessageBuilder(messageBuilder);
        
        reset(webServiceEndpoint, producer);

        expect(webServiceEndpoint.createProducer()).andReturn(producer).once();
        producer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Assert.assertEquals(((SoapMessage)EasyMock.getCurrentArguments()[0]).getAttachments().size(), 1L);
                SoapAttachment constructedAttachment = ((SoapMessage)EasyMock.getCurrentArguments()[0]).getAttachments().get(0);
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

        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId("myAttachment");
        attachment.setContentType("text/xml");
        attachment.setContent("<TestAttachment><Message>Hello World!</Message></TestAttachment>");
        attachment.setCharsetName("UTF-16");
        soapMessageAction.setAttachments(Collections.singletonList(attachment));

        reset(webServiceEndpoint, producer);

        expect(webServiceEndpoint.createProducer()).andReturn(producer).once();
        producer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Assert.assertEquals(((SoapMessage)EasyMock.getCurrentArguments()[0]).getAttachments().size(), 1L);
                SoapAttachment constructedAttachment = ((SoapMessage)EasyMock.getCurrentArguments()[0]).getAttachments().get(0);
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
    public void testSoapMessageWithMultipleAttachmentDataTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setEndpoint(webServiceEndpoint);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        soapMessageAction.setMessageBuilder(messageBuilder);

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

        reset(webServiceEndpoint, producer);

        expect(webServiceEndpoint.createProducer()).andReturn(producer).once();
        producer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Assert.assertEquals(((SoapMessage)EasyMock.getCurrentArguments()[0]).getAttachments().size(), 2L);
                SoapAttachment constructedAttachment = ((SoapMessage)EasyMock.getCurrentArguments()[0]).getAttachments().get(0);
                Assert.assertEquals(constructedAttachment.getContentId(), "1stAttachment");
                Assert.assertEquals(constructedAttachment.getContentType(), "text/xml");
                Assert.assertEquals(constructedAttachment.getContent(), "<TestAttachment><Message>Hello World1!</Message></TestAttachment>");
                Assert.assertEquals(constructedAttachment.getCharsetName(), "UTF-8");

                constructedAttachment = ((SoapMessage)EasyMock.getCurrentArguments()[0]).getAttachments().get(1);
                Assert.assertEquals(constructedAttachment.getContentId(), "2ndAttachment");
                Assert.assertEquals(constructedAttachment.getContentType(), "text/xml");
                Assert.assertEquals(constructedAttachment.getContent(), "<TestAttachment><Message>Hello World2!</Message></TestAttachment>");
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
        producer.send(anyObject(Message.class), anyObject(TestContext.class));
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

        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentResourcePath("classpath:com/consol/citrus/ws/actions/test-attachment.xml");
        soapMessageAction.setAttachments(Collections.singletonList(attachment));

        soapMessageAction.setMessageBuilder(messageBuilder);
        
        reset(webServiceEndpoint, producer);

        expect(webServiceEndpoint.createProducer()).andReturn(producer).once();
        producer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Assert.assertEquals(((SoapMessage)EasyMock.getCurrentArguments()[0]).getAttachments().size(), 1L);
                SoapAttachment constructedAttachment = ((SoapMessage)EasyMock.getCurrentArguments()[0]).getAttachments().get(0);
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

        SoapAttachment attachment = new SoapAttachment();
        attachment.setContent("<TestAttachment><Message>${myText}</Message></TestAttachment>");
        soapMessageAction.setAttachments(Collections.singletonList(attachment));

        soapMessageAction.setMessageBuilder(messageBuilder);
        
        reset(webServiceEndpoint, producer);

        expect(webServiceEndpoint.createProducer()).andReturn(producer).once();
        producer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Assert.assertEquals(((SoapMessage)EasyMock.getCurrentArguments()[0]).getAttachments().size(), 1L);
                SoapAttachment constructedAttachment = ((SoapMessage)EasyMock.getCurrentArguments()[0]).getAttachments().get(0);
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

        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentResourcePath("classpath:com/consol/citrus/ws/actions/test-attachment-with-variables.xml");
        soapMessageAction.setAttachments(Collections.singletonList(attachment));

        soapMessageAction.setMessageBuilder(messageBuilder);
        
        reset(webServiceEndpoint, producer);

        expect(webServiceEndpoint.createProducer()).andReturn(producer).once();
        producer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Assert.assertEquals(((SoapMessage)EasyMock.getCurrentArguments()[0]).getAttachments().size(), 1L);
                SoapAttachment constructedAttachment = ((SoapMessage)EasyMock.getCurrentArguments()[0]).getAttachments().get(0);
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
        
        messageBuilder.getHeaderData().add("<TestHeader><operation>soapOperation</operation></TestHeader>");
        
        soapMessageAction.setMessageBuilder(messageBuilder);

        reset(webServiceEndpoint, producer);

        expect(webServiceEndpoint.createProducer()).andReturn(producer).once();
        producer.send(anyObject(Message.class), anyObject(TestContext.class));
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
    public void testSoapMessageWithMultipleHeaderContentTest() throws Exception {
        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction();
        soapMessageAction.setEndpoint(webServiceEndpoint);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        messageBuilder.getHeaderData().add("<TestHeader><operation>soapOperation1</operation></TestHeader>");
        messageBuilder.getHeaderData().add("<TestHeader><operation>soapOperation2</operation></TestHeader>");

        soapMessageAction.setMessageBuilder(messageBuilder);

        reset(webServiceEndpoint, producer);

        expect(webServiceEndpoint.createProducer()).andReturn(producer).once();
        producer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Message constructedMessage = (Message)EasyMock.getCurrentArguments()[0];

                Assert.assertEquals(constructedMessage.getHeaderData().size(), 2L);
                Assert.assertEquals(constructedMessage.getHeaderData().get(0),
                        "<TestHeader><operation>soapOperation1</operation></TestHeader>");
                Assert.assertEquals(constructedMessage.getHeaderData().get(1),
                        "<TestHeader><operation>soapOperation2</operation></TestHeader>");

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
        
        messageBuilder.getHeaderResources().add("classpath:com/consol/citrus/ws/actions/test-header-resource.xml");
        
        soapMessageAction.setMessageBuilder(messageBuilder);
        
        reset(webServiceEndpoint, producer);

        expect(webServiceEndpoint.createProducer()).andReturn(producer).once();
        producer.send(anyObject(Message.class), anyObject(TestContext.class));
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
        
        messageBuilder.getHeaderData().add("<TestHeader><operation>${operation}</operation></TestHeader>");
        
        soapMessageAction.setMessageBuilder(messageBuilder);

        reset(webServiceEndpoint, producer);

        expect(webServiceEndpoint.createProducer()).andReturn(producer).once();
        producer.send(anyObject(Message.class), anyObject(TestContext.class));
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
        
        messageBuilder.getHeaderResources().add("classpath:com/consol/citrus/ws/actions/test-header-resource-with-variables.xml");
        
        soapMessageAction.setMessageBuilder(messageBuilder);

        reset(webServiceEndpoint, producer);

        expect(webServiceEndpoint.createProducer()).andReturn(producer).once();
        producer.send(anyObject(Message.class), anyObject(TestContext.class));
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
