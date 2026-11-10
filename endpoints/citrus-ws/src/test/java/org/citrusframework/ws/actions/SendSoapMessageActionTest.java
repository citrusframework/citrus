/*
 * Copyright the original author or authors.
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
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.ws.message.SoapAttachment;
import org.citrusframework.ws.message.SoapMessage;
import org.mockito.Mockito;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SendSoapMessageActionTest extends AbstractUnitTest {

    private Endpoint webServiceEndpoint = Mockito.mock(Endpoint.class);
    private Producer producer = Mockito.mock(Producer.class);

    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithMtomAttachmentDataTest() {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Text>cid:mtomText@citrusframework.org</Text></TestRequest>"));

        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId("mtomText@citrusframework.org");
        attachment.setContentType("text/xml");
        attachment.setContent("<TestAttachment><Message>Hello World!</Message></TestAttachment>");

        reset(webServiceEndpoint, producer);

        when(webServiceEndpoint.createProducer()).thenReturn(producer);
        doAnswer(invocation -> {
            SoapMessage soapMessage = ((SoapMessage)invocation.getArguments()[0]);
            Assertions.assertTrue(soapMessage.isMtomEnabled());
            Assertions.assertEquals("<TestRequest><Text><xop:Include xmlns:xop=\"http://www.w3.org/2004/08/xop/include\" href=\"cid:mtomText%40citrusframework.org\"/></Text></TestRequest>", soapMessage.getPayload(String.class));

            Assertions.assertEquals(1L, soapMessage.getAttachments().size());
            SoapAttachment constructedAttachment = ((SoapMessage)invocation.getArguments()[0]).getAttachments().get(0);
            Assertions.assertEquals("mtomText@citrusframework.org", constructedAttachment.getContentId());
            Assertions.assertEquals("text/xml", constructedAttachment.getContentType());
            Assertions.assertEquals("<TestAttachment><Message>Hello World!</Message></TestAttachment>", constructedAttachment.getContent());
            Assertions.assertEquals("UTF-8", constructedAttachment.getCharsetName());
            return null;
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
    public void testSoapMessageWithMtomInlineBase64BinaryAttachmentDataTest() {
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
        doAnswer(invocation -> {
            SoapMessage soapMessage = ((SoapMessage)invocation.getArguments()[0]);
            Assertions.assertTrue(soapMessage.isMtomEnabled());
            Assertions.assertEquals("<TestRequest><Image>SU1BR0VfREFUQQ==</Image></TestRequest>", soapMessage.getPayload(String.class));

            Assertions.assertEquals(0L, soapMessage.getAttachments().size());
            return null;
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
    public void testSoapMessageWithMtomInlineHexBinaryAttachmentDataTest() {
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
        doAnswer(invocation -> {
            SoapMessage soapMessage = ((SoapMessage)invocation.getArguments()[0]);
            Assertions.assertTrue(soapMessage.isMtomEnabled());
            Assertions.assertEquals("<TestRequest><Image>494D4147455F44415441</Image></TestRequest>", soapMessage.getPayload(String.class));

            Assertions.assertEquals(0L, soapMessage.getAttachments().size());
            return null;
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
    public void testSoapMessageWithMtomMissingCidAttachmentDataTest() {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Text>mtomText</Text></TestRequest>"));

        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId("mtomText");
        attachment.setContentType("text/xml");
        attachment.setContent("<TestAttachment><Message>Hello World!</Message></TestAttachment>");

        reset(webServiceEndpoint, producer);

        when(webServiceEndpoint.createProducer()).thenReturn(producer);
        doAnswer(invocation -> {
            SoapMessage soapMessage = ((SoapMessage)invocation.getArguments()[0]);
            Assertions.assertTrue(soapMessage.isMtomEnabled());
            Assertions.assertEquals("<TestRequest><Text>mtomText</Text></TestRequest>", soapMessage.getPayload(String.class));

            Assertions.assertEquals(1L, soapMessage.getAttachments().size());
            SoapAttachment constructedAttachment = ((SoapMessage)invocation.getArguments()[0]).getAttachments().get(0);
            Assertions.assertEquals("mtomText", constructedAttachment.getContentId());
            Assertions.assertEquals("text/xml", constructedAttachment.getContentType());
            Assertions.assertEquals("<TestAttachment><Message>Hello World!</Message></TestAttachment>", constructedAttachment.getContent());
            Assertions.assertEquals("UTF-8", constructedAttachment.getCharsetName());
            return null;
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
    public void testSoapMessageWithMtomInlineMissingCidAttachmentDataTest() {
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
        doAnswer(invocation -> {
            SoapMessage soapMessage = ((SoapMessage)invocation.getArguments()[0]);
            Assertions.assertTrue(soapMessage.isMtomEnabled());
            Assertions.assertEquals("<TestRequest><Image>mtomImage</Image></TestRequest>", soapMessage.getPayload(String.class));

            Assertions.assertEquals(1L, soapMessage.getAttachments().size());
            SoapAttachment constructedAttachment = ((SoapMessage)invocation.getArguments()[0]).getAttachments().get(0);
            Assertions.assertEquals("mtomImage", constructedAttachment.getContentId());
            Assertions.assertEquals("image/png", constructedAttachment.getContentType());
            Assertions.assertEquals("IMAGE_DATA", constructedAttachment.getContent());
            Assertions.assertEquals("UTF-8", constructedAttachment.getCharsetName());
            return null;
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
    public void testSoapMessageWithMtomInlineInvalidEncodingTypeAttachmentDataTest() {
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
            Assertions.fail("Missing exception due to invalid attachment encoding type");
        } catch (CitrusRuntimeException e) {
            Assertions.assertEquals("Unsupported encoding type 'md5' for SOAP attachment: cid:mtomImage - choose one of base64Binary or hexBinary", e.getMessage());
        }
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSoapMessageWithDefaultAttachmentDataTest() {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        SoapAttachment attachment = new SoapAttachment();
        attachment.setContent("<TestAttachment><Message>Hello World!</Message></TestAttachment>");

        reset(webServiceEndpoint, producer);

        when(webServiceEndpoint.createProducer()).thenReturn(producer);
        doAnswer(invocation -> {
            Assertions.assertEquals(1L, ((SoapMessage)invocation.getArguments()[0]).getAttachments().size());
            SoapAttachment constructedAttachment = ((SoapMessage)invocation.getArguments()[0]).getAttachments().get(0);
            Assertions.assertNull(constructedAttachment.getContentId());
            Assertions.assertEquals("text/plain", constructedAttachment.getContentType());
            Assertions.assertEquals("<TestAttachment><Message>Hello World!</Message></TestAttachment>", constructedAttachment.getContent());
            Assertions.assertEquals("UTF-8", constructedAttachment.getCharsetName());
            return null;
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
    public void testSoapMessageWithAttachmentDataTest() {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId("myAttachment");
        attachment.setContentType("text/xml");
        attachment.setContent("<TestAttachment><Message>Hello World!</Message></TestAttachment>");
        attachment.setCharsetName("UTF-16");

        reset(webServiceEndpoint, producer);

        when(webServiceEndpoint.createProducer()).thenReturn(producer);
        doAnswer(invocation -> {
            Assertions.assertEquals(1L, ((SoapMessage)invocation.getArguments()[0]).getAttachments().size());
            SoapAttachment constructedAttachment = ((SoapMessage)invocation.getArguments()[0]).getAttachments().get(0);
            Assertions.assertEquals("myAttachment", constructedAttachment.getContentId());
            Assertions.assertEquals("text/xml", constructedAttachment.getContentType());
            Assertions.assertEquals("<TestAttachment><Message>Hello World!</Message></TestAttachment>", constructedAttachment.getContent());
            Assertions.assertEquals("UTF-16", constructedAttachment.getCharsetName());
            return null;
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
    public void testSoapMessageWithMultipleAttachmentDataTest() {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        List<SoapAttachment> attachments = new ArrayList<>();
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
        doAnswer(invocation -> {
            Assertions.assertEquals(2L, ((SoapMessage)invocation.getArguments()[0]).getAttachments().size());
            SoapAttachment constructedAttachment = ((SoapMessage)invocation.getArguments()[0]).getAttachments().get(0);
            Assertions.assertEquals("1stAttachment", constructedAttachment.getContentId());
            Assertions.assertEquals("text/xml", constructedAttachment.getContentType());
            Assertions.assertEquals("<TestAttachment><Message>Hello World1!</Message></TestAttachment>", constructedAttachment.getContent());
            Assertions.assertEquals("UTF-8", constructedAttachment.getCharsetName());

            constructedAttachment = ((SoapMessage)invocation.getArguments()[0]).getAttachments().get(1);
            Assertions.assertEquals("2ndAttachment", constructedAttachment.getContentId());
            Assertions.assertEquals("text/xml", constructedAttachment.getContentType());
            Assertions.assertEquals("<TestAttachment><Message>Hello World2!</Message></TestAttachment>", constructedAttachment.getContent());
            Assertions.assertEquals("UTF-16", constructedAttachment.getCharsetName());
            return null;
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
    public void testSoapMessageWithEmptyAttachmentContentTest() {
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
    public void testSoapMessageWithAttachmentResourceTest() {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentResourcePath("classpath:org/citrusframework/ws/actions/test-attachment.xml");

        reset(webServiceEndpoint, producer);

        when(webServiceEndpoint.createProducer()).thenReturn(producer);
        doAnswer(invocation -> {
            Assertions.assertEquals(1L, ((SoapMessage)invocation.getArguments()[0]).getAttachments().size());
            SoapAttachment constructedAttachment = ((SoapMessage)invocation.getArguments()[0]).getAttachments().get(0);
            Assertions.assertNull(constructedAttachment.getContentId());
            Assertions.assertEquals("text/plain", constructedAttachment.getContentType());
            Assertions.assertEquals("<TestAttachment><Message>Hello World!</Message></TestAttachment>", constructedAttachment.getContent().trim());
            Assertions.assertEquals("UTF-8", constructedAttachment.getCharsetName());
            return null;
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
    public void testSoapMessageWithAttachmentDataVariableSupportTest() {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        context.setVariable("myText", "Hello World!");

        SoapAttachment attachment = new SoapAttachment();
        attachment.setContent("<TestAttachment><Message>${myText}</Message></TestAttachment>");

        reset(webServiceEndpoint, producer);

        when(webServiceEndpoint.createProducer()).thenReturn(producer);
        doAnswer(invocation -> {
            Assertions.assertEquals(1L, ((SoapMessage)invocation.getArguments()[0]).getAttachments().size());
            SoapAttachment constructedAttachment = ((SoapMessage)invocation.getArguments()[0]).getAttachments().get(0);
            Assertions.assertNull(constructedAttachment.getContentId());
            Assertions.assertEquals("text/plain", constructedAttachment.getContentType());
            Assertions.assertEquals("<TestAttachment><Message>Hello World!</Message></TestAttachment>", constructedAttachment.getContent());
            Assertions.assertEquals("UTF-8", constructedAttachment.getCharsetName());
            return null;
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
    public void testSoapMessageWithAttachmentResourceVariablesSupportTest() {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        context.setVariable("myText", "Hello World!");

        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentResourcePath("classpath:org/citrusframework/ws/actions/test-attachment-with-variables.xml");

        reset(webServiceEndpoint, producer);

        when(webServiceEndpoint.createProducer()).thenReturn(producer);
        doAnswer(invocation -> {
            Assertions.assertEquals(1L, ((SoapMessage)invocation.getArguments()[0]).getAttachments().size());
            SoapAttachment constructedAttachment = ((SoapMessage)invocation.getArguments()[0]).getAttachments().get(0);
            Assertions.assertNull(constructedAttachment.getContentId());
            Assertions.assertEquals("text/plain", constructedAttachment.getContentType());
            Assertions.assertEquals("<TestAttachment><Message>Hello World!</Message></TestAttachment>", constructedAttachment.getContent().trim());
            Assertions.assertEquals("UTF-8", constructedAttachment.getCharsetName());
            return null;
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
    public void testSoapMessageWithHeaderContentTest() {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        messageBuilder.addHeaderBuilder(new DefaultHeaderDataBuilder("<TestHeader><operation>soapOperation</operation></TestHeader>"));

        reset(webServiceEndpoint, producer);

        when(webServiceEndpoint.createProducer()).thenReturn(producer);
        doAnswer(invocation -> {
            Message constructedMessage = (Message)invocation.getArguments()[0];

            Assertions.assertEquals(1L, constructedMessage.getHeaderData().size());
            Assertions.assertEquals(
                    "<TestHeader><operation>soapOperation</operation></TestHeader>", constructedMessage.getHeaderData().get(0));
            return null;
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
    public void testSoapMessageWithMultipleHeaderContentTest() {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        messageBuilder.addHeaderBuilder(new DefaultHeaderDataBuilder("<TestHeader><operation>soapOperation1</operation></TestHeader>"));
        messageBuilder.addHeaderBuilder(new DefaultHeaderDataBuilder("<TestHeader><operation>soapOperation2</operation></TestHeader>"));

        reset(webServiceEndpoint, producer);

        when(webServiceEndpoint.createProducer()).thenReturn(producer);
        doAnswer(invocation -> {
            Message constructedMessage = (Message)invocation.getArguments()[0];

            Assertions.assertEquals(2L, constructedMessage.getHeaderData().size());
            Assertions.assertEquals(
                    "<TestHeader><operation>soapOperation1</operation></TestHeader>", constructedMessage.getHeaderData().get(0));
            Assertions.assertEquals(
                    "<TestHeader><operation>soapOperation2</operation></TestHeader>", constructedMessage.getHeaderData().get(1));
            return null;
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
    public void testSoapMessageWithHeaderResourceTest() {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        messageBuilder.addHeaderBuilder(new FileResourceHeaderDataBuilder("classpath:org/citrusframework/ws/actions/test-header-resource.xml"));

        reset(webServiceEndpoint, producer);

        when(webServiceEndpoint.createProducer()).thenReturn(producer);
        doAnswer(invocation -> {
            Message constructedMessage = (Message)invocation.getArguments()[0];

            Assertions.assertEquals(1L, constructedMessage.getHeaderData().size());
            Assertions.assertEquals(
                    "<TestHeader><operation>soapOperation</operation></TestHeader>", constructedMessage.getHeaderData().get(0).trim());
            return null;
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
    public void testSoapMessageWithHeaderContentVariableSupportTest() {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        context.setVariable("operation", "soapOperation");

        messageBuilder.addHeaderBuilder(new DefaultHeaderDataBuilder("<TestHeader><operation>${operation}</operation></TestHeader>"));

        reset(webServiceEndpoint, producer);

        when(webServiceEndpoint.createProducer()).thenReturn(producer);
        doAnswer(invocation -> {
            Message constructedMessage = (Message)invocation.getArguments()[0];

            Assertions.assertEquals(1L, constructedMessage.getHeaderData().size());
            Assertions.assertEquals(
                    "<TestHeader><operation>soapOperation</operation></TestHeader>", constructedMessage.getHeaderData().get(0));
            return null;
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
    public void testSoapMessageWithHeaderResourceVariableSupportTest() {
        DefaultMessageBuilder messageBuilder = new DefaultMessageBuilder();
        messageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        context.setVariable("operation", "soapOperation");

        messageBuilder.addHeaderBuilder(new FileResourceHeaderDataBuilder("classpath:org/citrusframework/ws/actions/test-header-resource-with-variables.xml"));

        reset(webServiceEndpoint, producer);

        when(webServiceEndpoint.createProducer()).thenReturn(producer);
        doAnswer(invocation -> {
            Message constructedMessage = (Message)invocation.getArguments()[0];

            Assertions.assertEquals(1L, constructedMessage.getHeaderData().size());
            Assertions.assertEquals(
                    "<TestHeader><operation>soapOperation</operation></TestHeader>", constructedMessage.getHeaderData().get(0).trim());
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(webServiceEndpoint.getActor()).thenReturn(null);

        SendSoapMessageAction soapMessageAction = new SendSoapMessageAction.Builder()
                .endpoint(webServiceEndpoint)
                .message(messageBuilder)
                .build();
        soapMessageAction.execute(context);
    }
}
