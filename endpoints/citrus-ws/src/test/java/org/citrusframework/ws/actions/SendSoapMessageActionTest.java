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

package org.citrusframework.ws.actions;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.Message;
import org.citrusframework.message.builder.DefaultHeaderDataBuilder;
import org.citrusframework.message.builder.DefaultPayloadBuilder;
import org.citrusframework.message.builder.FileResourceHeaderDataBuilder;
import org.citrusframework.messaging.Producer;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.ws.message.SoapAttachment;
import org.citrusframework.ws.message.SoapMessage;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class SendSoapMessageActionTest extends AbstractTestNGUnitTest {

    private Endpoint webServiceEndpoint = Mockito.mock(Endpoint.class);
    private Producer producer = Mockito.mock(Producer.class);

    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithMtomAttachmentDataTest() throws Exception {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Text>cid:mtomText@citrusframework.org</Text></TestRequest>"));

        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId("mtomText@citrusframework.org");
        attachment.setContentType("text/xml");
        attachment.setContent("<TestAttachment><Message>Hello World!</Message></TestAttachment>");

        reset(webServiceEndpoint, producer);

        when(webServiceEndpoint.createProducer()).thenReturn(producer);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                SoapMessage soapMessage = ((SoapMessage)invocation.getArguments()[0]);
                Assert.assertTrue(soapMessage.isMtomEnabled());
                Assert.assertEquals(soapMessage.getPayload(String.class), "<TestRequest><Text><xop:Include xmlns:xop=\"http://www.w3.org/2004/08/xop/include\" href=\"cid:mtomText%40citrusframework.org\"/></Text></TestRequest>");

                Assert.assertEquals(soapMessage.getAttachments().size(), 1L);
                SoapAttachment constructedAttachment = ((SoapMessage)invocation.getArguments()[0]).getAttachments().get(0);
                Assert.assertEquals(constructedAttachment.getContentId(), "mtomText@citrusframework.org");
                Assert.assertEquals(constructedAttachment.getContentType(), "text/xml");
                Assert.assertEquals(constructedAttachment.getContent(), "<TestAttachment><Message>Hello World!</Message></TestAttachment>");
                Assert.assertEquals(constructedAttachment.getCharsetName(), "UTF-8");
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(webServiceEndpoint.getActor()).thenReturn(null);

        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction.Builder()
                .endpoint(webServiceEndpoint)
                .mtomEnabled(true)
                .message(messageBuilder)
                .attachment(attachment)
                .build();
        soapMessageAction.execute(context);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithMtomInlineBase64BinaryAttachmentDataTest() throws Exception {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Image>cid:mtomImage</Image></TestRequest>"));

        SoapAttachment attachment = new SoapAttachment();
        attachment.setMtomInline(true);
        attachment.setEncodingType(SoapAttachment.ENCODING_BASE64_BINARY);
        attachment.setContentId("mtomImage");
        attachment.setContentType("image/png");
        attachment.setContent("IMAGE_DATA");

        reset(webServiceEndpoint, producer);

        when(webServiceEndpoint.createProducer()).thenReturn(producer);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                SoapMessage soapMessage = ((SoapMessage)invocation.getArguments()[0]);
                Assert.assertTrue(soapMessage.isMtomEnabled());
                Assert.assertEquals(soapMessage.getPayload(String.class), "<TestRequest><Image>SU1BR0VfREFUQQ==</Image></TestRequest>");

                Assert.assertEquals(soapMessage.getAttachments().size(), 0L);
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(webServiceEndpoint.getActor()).thenReturn(null);

        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction.Builder()
                .endpoint(webServiceEndpoint)
                .mtomEnabled(true)
                .message(messageBuilder)
                .attachment(attachment)
                .build();
        soapMessageAction.execute(context);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithMtomInlineHexBinaryAttachmentDataTest() throws Exception {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Image>cid:mtomImage</Image></TestRequest>"));

        SoapAttachment attachment = new SoapAttachment();
        attachment.setMtomInline(true);
        attachment.setEncodingType(SoapAttachment.ENCODING_HEX_BINARY);
        attachment.setContentId("mtomImage");
        attachment.setContentType("image/png");
        attachment.setContent("IMAGE_DATA");

        reset(webServiceEndpoint, producer);

        when(webServiceEndpoint.createProducer()).thenReturn(producer);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                SoapMessage soapMessage = ((SoapMessage)invocation.getArguments()[0]);
                Assert.assertTrue(soapMessage.isMtomEnabled());
                Assert.assertEquals(soapMessage.getPayload(String.class), "<TestRequest><Image>494D4147455F44415441</Image></TestRequest>");

                Assert.assertEquals(soapMessage.getAttachments().size(), 0L);
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(webServiceEndpoint.getActor()).thenReturn(null);

        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction.Builder()
                .endpoint(webServiceEndpoint)
                .mtomEnabled(true)
                .message(messageBuilder)
                .attachment(attachment)
                .build();
        soapMessageAction.execute(context);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithMtomMissingCidAttachmentDataTest() throws Exception {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Text>mtomText</Text></TestRequest>"));

        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId("mtomText");
        attachment.setContentType("text/xml");
        attachment.setContent("<TestAttachment><Message>Hello World!</Message></TestAttachment>");

        reset(webServiceEndpoint, producer);

        when(webServiceEndpoint.createProducer()).thenReturn(producer);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                SoapMessage soapMessage = ((SoapMessage)invocation.getArguments()[0]);
                Assert.assertTrue(soapMessage.isMtomEnabled());
                Assert.assertEquals(soapMessage.getPayload(String.class), "<TestRequest><Text>mtomText</Text></TestRequest>");

                Assert.assertEquals(soapMessage.getAttachments().size(), 1L);
                SoapAttachment constructedAttachment = ((SoapMessage)invocation.getArguments()[0]).getAttachments().get(0);
                Assert.assertEquals(constructedAttachment.getContentId(), "mtomText");
                Assert.assertEquals(constructedAttachment.getContentType(), "text/xml");
                Assert.assertEquals(constructedAttachment.getContent(), "<TestAttachment><Message>Hello World!</Message></TestAttachment>");
                Assert.assertEquals(constructedAttachment.getCharsetName(), "UTF-8");
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(webServiceEndpoint.getActor()).thenReturn(null);

        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction.Builder()
                .endpoint(webServiceEndpoint)
                .mtomEnabled(true)
                .message(messageBuilder)
                .attachment(attachment)
                .build();
        soapMessageAction.execute(context);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithMtomInlineMissingCidAttachmentDataTest() throws Exception {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Image>mtomImage</Image></TestRequest>"));

        SoapAttachment attachment = new SoapAttachment();
        attachment.setMtomInline(true);
        attachment.setEncodingType(SoapAttachment.ENCODING_BASE64_BINARY);
        attachment.setContentId("mtomImage");
        attachment.setContentType("image/png");
        attachment.setContent("IMAGE_DATA");

        reset(webServiceEndpoint, producer);

        when(webServiceEndpoint.createProducer()).thenReturn(producer);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                SoapMessage soapMessage = ((SoapMessage)invocation.getArguments()[0]);
                Assert.assertTrue(soapMessage.isMtomEnabled());
                Assert.assertEquals(soapMessage.getPayload(String.class), "<TestRequest><Image>mtomImage</Image></TestRequest>");

                Assert.assertEquals(soapMessage.getAttachments().size(), 1L);
                SoapAttachment constructedAttachment = ((SoapMessage)invocation.getArguments()[0]).getAttachments().get(0);
                Assert.assertEquals(constructedAttachment.getContentId(), "mtomImage");
                Assert.assertEquals(constructedAttachment.getContentType(), "image/png");
                Assert.assertEquals(constructedAttachment.getContent(), "IMAGE_DATA");
                Assert.assertEquals(constructedAttachment.getCharsetName(), "UTF-8");
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(webServiceEndpoint.getActor()).thenReturn(null);

        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction.Builder()
                .endpoint(webServiceEndpoint)
                .mtomEnabled(true)
                .message(messageBuilder)
                .attachment(attachment)
                .build();
        soapMessageAction.execute(context);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithMtomInlineInvalidEncodingTypeAttachmentDataTest() throws Exception {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Image>cid:mtomImage</Image></TestRequest>"));

        SoapAttachment attachment = new SoapAttachment();
        attachment.setMtomInline(true);
        attachment.setEncodingType("md5");
        attachment.setContentId("mtomImage");
        attachment.setContentType("image/png");
        attachment.setContent("IMAGE_DATA");

        reset(webServiceEndpoint, producer);
        when(webServiceEndpoint.getActor()).thenReturn(null);

        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction.Builder()
                .endpoint(webServiceEndpoint)
                .mtomEnabled(true)
                .message(messageBuilder)
                .attachment(attachment)
                .build();
        try {
            soapMessageAction.execute(context);
            Assert.fail("Missing exception due to invalid attachment encoding type");
        } catch (CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Unsupported encoding type 'md5' for SOAP attachment: cid:mtomImage - choose one of base64Binary or hexBinary");
        }
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithDefaultAttachmentDataTest() throws Exception {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        SoapAttachment attachment = new SoapAttachment();
        attachment.setContent("<TestAttachment><Message>Hello World!</Message></TestAttachment>");

        reset(webServiceEndpoint, producer);

        when(webServiceEndpoint.createProducer()).thenReturn(producer);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Assert.assertEquals(((SoapMessage)invocation.getArguments()[0]).getAttachments().size(), 1L);
                SoapAttachment constructedAttachment = ((SoapMessage)invocation.getArguments()[0]).getAttachments().get(0);
                Assert.assertNull(constructedAttachment.getContentId());
                Assert.assertEquals(constructedAttachment.getContentType(), "text/plain");
                Assert.assertEquals(constructedAttachment.getContent(), "<TestAttachment><Message>Hello World!</Message></TestAttachment>");
                Assert.assertEquals(constructedAttachment.getCharsetName(), "UTF-8");
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(webServiceEndpoint.getActor()).thenReturn(null);

        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction.Builder()
                .endpoint(webServiceEndpoint)
                .message(messageBuilder)
                .attachment(attachment)
                .build();
        soapMessageAction.execute(context);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithAttachmentDataTest() throws Exception {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId("myAttachment");
        attachment.setContentType("text/xml");
        attachment.setContent("<TestAttachment><Message>Hello World!</Message></TestAttachment>");
        attachment.setCharsetName("UTF-16");

        reset(webServiceEndpoint, producer);

        when(webServiceEndpoint.createProducer()).thenReturn(producer);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Assert.assertEquals(((SoapMessage)invocation.getArguments()[0]).getAttachments().size(), 1L);
                SoapAttachment constructedAttachment = ((SoapMessage)invocation.getArguments()[0]).getAttachments().get(0);
                Assert.assertEquals(constructedAttachment.getContentId(), "myAttachment");
                Assert.assertEquals(constructedAttachment.getContentType(), "text/xml");
                Assert.assertEquals(constructedAttachment.getContent(), "<TestAttachment><Message>Hello World!</Message></TestAttachment>");
                Assert.assertEquals(constructedAttachment.getCharsetName(), "UTF-16");
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(webServiceEndpoint.getActor()).thenReturn(null);

        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction.Builder()
                .endpoint(webServiceEndpoint)
                .message(messageBuilder)
                .attachment(attachment)
                .build();
        soapMessageAction.execute(context);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithMultipleAttachmentDataTest() throws Exception {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

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

        reset(webServiceEndpoint, producer);

        when(webServiceEndpoint.createProducer()).thenReturn(producer);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Assert.assertEquals(((SoapMessage)invocation.getArguments()[0]).getAttachments().size(), 2L);
                SoapAttachment constructedAttachment = ((SoapMessage)invocation.getArguments()[0]).getAttachments().get(0);
                Assert.assertEquals(constructedAttachment.getContentId(), "1stAttachment");
                Assert.assertEquals(constructedAttachment.getContentType(), "text/xml");
                Assert.assertEquals(constructedAttachment.getContent(), "<TestAttachment><Message>Hello World1!</Message></TestAttachment>");
                Assert.assertEquals(constructedAttachment.getCharsetName(), "UTF-8");

                constructedAttachment = ((SoapMessage)invocation.getArguments()[0]).getAttachments().get(1);
                Assert.assertEquals(constructedAttachment.getContentId(), "2ndAttachment");
                Assert.assertEquals(constructedAttachment.getContentType(), "text/xml");
                Assert.assertEquals(constructedAttachment.getContent(), "<TestAttachment><Message>Hello World2!</Message></TestAttachment>");
                Assert.assertEquals(constructedAttachment.getCharsetName(), "UTF-16");
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(webServiceEndpoint.getActor()).thenReturn(null);

        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction.Builder()
                .endpoint(webServiceEndpoint)
                .message(messageBuilder)
                .attachment(attachment)
                .attachment(attachment2)
                .build();
        soapMessageAction.execute(context);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithEmptyAttachmentContentTest() throws Exception {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        reset(webServiceEndpoint, producer);

        when(webServiceEndpoint.createProducer()).thenReturn(producer);

        when(webServiceEndpoint.getActor()).thenReturn(null);

        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction.Builder()
                .endpoint(webServiceEndpoint)
                .message(messageBuilder)
                .build();
        soapMessageAction.execute(context);
        verify(producer).send(any(Message.class), any(TestContext.class));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithAttachmentResourceTest() throws Exception {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentResourcePath("classpath:org/citrusframework/ws/actions/test-attachment.xml");

        reset(webServiceEndpoint, producer);

        when(webServiceEndpoint.createProducer()).thenReturn(producer);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Assert.assertEquals(((SoapMessage)invocation.getArguments()[0]).getAttachments().size(), 1L);
                SoapAttachment constructedAttachment = ((SoapMessage)invocation.getArguments()[0]).getAttachments().get(0);
                Assert.assertNull(constructedAttachment.getContentId());
                Assert.assertEquals(constructedAttachment.getContentType(), "text/plain");
                Assert.assertEquals(constructedAttachment.getContent().trim(), "<TestAttachment><Message>Hello World!</Message></TestAttachment>");
                Assert.assertEquals(constructedAttachment.getCharsetName(), "UTF-8");
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(webServiceEndpoint.getActor()).thenReturn(null);

        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction.Builder()
                .endpoint(webServiceEndpoint)
                .message(messageBuilder)
                .attachment(attachment)
                .build();
        soapMessageAction.execute(context);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithAttachmentDataVariableSupportTest() throws Exception {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        context.setVariable("myText", "Hello World!");

        SoapAttachment attachment = new SoapAttachment();
        attachment.setContent("<TestAttachment><Message>${myText}</Message></TestAttachment>");

        reset(webServiceEndpoint, producer);

        when(webServiceEndpoint.createProducer()).thenReturn(producer);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Assert.assertEquals(((SoapMessage)invocation.getArguments()[0]).getAttachments().size(), 1L);
                SoapAttachment constructedAttachment = ((SoapMessage)invocation.getArguments()[0]).getAttachments().get(0);
                Assert.assertNull(constructedAttachment.getContentId());
                Assert.assertEquals(constructedAttachment.getContentType(), "text/plain");
                Assert.assertEquals(constructedAttachment.getContent(), "<TestAttachment><Message>Hello World!</Message></TestAttachment>");
                Assert.assertEquals(constructedAttachment.getCharsetName(), "UTF-8");
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(webServiceEndpoint.getActor()).thenReturn(null);

        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction.Builder()
                .endpoint(webServiceEndpoint)
                .message(messageBuilder)
                .attachment(attachment)
                .build();
        soapMessageAction.execute(context);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithAttachmentResourceVariablesSupportTest() throws Exception {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        context.setVariable("myText", "Hello World!");

        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentResourcePath("classpath:org/citrusframework/ws/actions/test-attachment-with-variables.xml");

        reset(webServiceEndpoint, producer);

        when(webServiceEndpoint.createProducer()).thenReturn(producer);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Assert.assertEquals(((SoapMessage)invocation.getArguments()[0]).getAttachments().size(), 1L);
                SoapAttachment constructedAttachment = ((SoapMessage)invocation.getArguments()[0]).getAttachments().get(0);
                Assert.assertNull(constructedAttachment.getContentId());
                Assert.assertEquals(constructedAttachment.getContentType(), "text/plain");
                Assert.assertEquals(constructedAttachment.getContent().trim(), "<TestAttachment><Message>Hello World!</Message></TestAttachment>");
                Assert.assertEquals(constructedAttachment.getCharsetName(), "UTF-8");
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(webServiceEndpoint.getActor()).thenReturn(null);

        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction.Builder()
                .endpoint(webServiceEndpoint)
                .message(messageBuilder)
                .attachment(attachment)
                .build();
        soapMessageAction.execute(context);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithHeaderContentTest() throws Exception {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        messageBuilder.addHeaderBuilder(new DefaultHeaderDataBuilder("<TestHeader><operation>soapOperation</operation></TestHeader>"));

        reset(webServiceEndpoint, producer);

        when(webServiceEndpoint.createProducer()).thenReturn(producer);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Message constructedMessage = (Message)invocation.getArguments()[0];

                Assert.assertEquals(constructedMessage.getHeaderData().size(), 1L);
                Assert.assertEquals(constructedMessage.getHeaderData().get(0),
                        "<TestHeader><operation>soapOperation</operation></TestHeader>");
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(webServiceEndpoint.getActor()).thenReturn(null);

        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction.Builder()
                .endpoint(webServiceEndpoint)
                .message(messageBuilder)
                .build();
        soapMessageAction.execute(context);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithMultipleHeaderContentTest() throws Exception {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        messageBuilder.addHeaderBuilder(new DefaultHeaderDataBuilder("<TestHeader><operation>soapOperation1</operation></TestHeader>"));
        messageBuilder.addHeaderBuilder(new DefaultHeaderDataBuilder("<TestHeader><operation>soapOperation2</operation></TestHeader>"));

        reset(webServiceEndpoint, producer);

        when(webServiceEndpoint.createProducer()).thenReturn(producer);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Message constructedMessage = (Message)invocation.getArguments()[0];

                Assert.assertEquals(constructedMessage.getHeaderData().size(), 2L);
                Assert.assertEquals(constructedMessage.getHeaderData().get(0),
                        "<TestHeader><operation>soapOperation1</operation></TestHeader>");
                Assert.assertEquals(constructedMessage.getHeaderData().get(1),
                        "<TestHeader><operation>soapOperation2</operation></TestHeader>");
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(webServiceEndpoint.getActor()).thenReturn(null);

        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction.Builder()
                .endpoint(webServiceEndpoint)
                .message(messageBuilder)
                .build();
        soapMessageAction.execute(context);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithHeaderResourceTest() throws Exception {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        messageBuilder.addHeaderBuilder(new FileResourceHeaderDataBuilder("classpath:org/citrusframework/ws/actions/test-header-resource.xml"));

        reset(webServiceEndpoint, producer);

        when(webServiceEndpoint.createProducer()).thenReturn(producer);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Message constructedMessage = (Message)invocation.getArguments()[0];

                Assert.assertEquals(constructedMessage.getHeaderData().size(), 1L);
                Assert.assertEquals(constructedMessage.getHeaderData().get(0).trim(),
                        "<TestHeader><operation>soapOperation</operation></TestHeader>");
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(webServiceEndpoint.getActor()).thenReturn(null);

        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction.Builder()
                .endpoint(webServiceEndpoint)
                .message(messageBuilder)
                .build();
        soapMessageAction.execute(context);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithHeaderContentVariableSupportTest() throws Exception {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        context.setVariable("operation", "soapOperation");

        messageBuilder.addHeaderBuilder(new DefaultHeaderDataBuilder("<TestHeader><operation>${operation}</operation></TestHeader>"));

        reset(webServiceEndpoint, producer);

        when(webServiceEndpoint.createProducer()).thenReturn(producer);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Message constructedMessage = (Message)invocation.getArguments()[0];

                Assert.assertEquals(constructedMessage.getHeaderData().size(), 1L);
                Assert.assertEquals(constructedMessage.getHeaderData().get(0),
                        "<TestHeader><operation>soapOperation</operation></TestHeader>");
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(webServiceEndpoint.getActor()).thenReturn(null);

        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction.Builder()
                .endpoint(webServiceEndpoint)
                .message(messageBuilder)
                .build();
        soapMessageAction.execute(context);
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithHeaderResourceVariableSupportTest() throws Exception {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        context.setVariable("operation", "soapOperation");

        messageBuilder.addHeaderBuilder(new FileResourceHeaderDataBuilder("classpath:org/citrusframework/ws/actions/test-header-resource-with-variables.xml"));

        reset(webServiceEndpoint, producer);

        when(webServiceEndpoint.createProducer()).thenReturn(producer);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Message constructedMessage = (Message)invocation.getArguments()[0];

                Assert.assertEquals(constructedMessage.getHeaderData().size(), 1L);
                Assert.assertEquals(constructedMessage.getHeaderData().get(0).trim(),
                        "<TestHeader><operation>soapOperation</operation></TestHeader>");
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(webServiceEndpoint.getActor()).thenReturn(null);

        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction.Builder()
                .endpoint(webServiceEndpoint)
                .message(messageBuilder)
                .build();
        soapMessageAction.execute(context);
    }

}
