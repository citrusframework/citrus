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

import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.message.Message;
import org.citrusframework.message.builder.DefaultPayloadBuilder;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.junit.jupiter.AbstractUnitTest;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.xml.DomXmlMessageValidator;
import org.citrusframework.ws.message.SoapAttachment;
import org.citrusframework.ws.message.SoapMessage;
import org.citrusframework.ws.validation.SoapAttachmentValidator;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@SuppressWarnings({ "unchecked", "rawtypes" })
@ExtendWith(MockitoExtension.class)
public class ReceiveSoapMessageActionTest extends AbstractUnitTest {

    @Mock
    private Endpoint endpoint;
    @Mock
    private Consumer consumer;
    @Mock
    private EndpointConfiguration endpointConfiguration;

    @Mock
    private SoapAttachmentValidator attachmentValidator;

    @Override
    protected TestContextFactory createTestContextFactory() {
        return super.createTestContextFactory();
    }

    @Test
    public void testSoapMessageWithDefaultAttachmentDataTest() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        SoapAttachment attachment = new SoapAttachment();
        attachment.setContent("TestAttachment!");

        Message controlMessage = new SoapMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration, attachmentValidator);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);

        doAnswer(invocation -> {
            Assertions.assertEquals(1L, ((List<SoapAttachment>)invocation.getArguments()[1]).size());
            SoapAttachment soapAttachment = ((List<SoapAttachment>)invocation.getArguments()[1]).get(0);
            Assertions.assertEquals("TestAttachment!", soapAttachment.getContent());
            Assertions.assertNull(soapAttachment.getContentId());
            Assertions.assertEquals("text/plain", soapAttachment.getContentType());
            return null;
        }).when(attachmentValidator).validateAttachment((SoapMessage)any(), any(List.class));

        when(endpoint.getActor()).thenReturn(null);

        ReceiveSoapMessageAction soapMessageAction = new ReceiveSoapMessageAction.Builder()
                .endpoint(endpoint)
                .validator(new DomXmlMessageValidator())
                .message(controlMessageBuilder)
                .attachment(attachment)
                .attachmentValidator(attachmentValidator)
                .build();
        soapMessageAction.execute(context);
    }

    @Test
    public void testSoapMessageWithAttachmentDataTest() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId("myAttachment");
        attachment.setContentType("text/xml");
        attachment.setContent("<TestAttachment><Message>Hello World!</Message></TestAttachment>");
        attachment.setCharsetName("UTF-16");

        Message controlMessage = new SoapMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration, attachmentValidator);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);

        doAnswer(invocation -> {
            Assertions.assertEquals(1L, ((List<SoapAttachment>)invocation.getArguments()[1]).size());
            SoapAttachment soapAttachment = ((List<SoapAttachment>)invocation.getArguments()[1]).get(0);
            Assertions.assertEquals("<TestAttachment><Message>Hello World!</Message></TestAttachment>", soapAttachment.getContent());
            Assertions.assertEquals("myAttachment", soapAttachment.getContentId());
            Assertions.assertEquals("text/xml", soapAttachment.getContentType());
            Assertions.assertEquals("UTF-16", soapAttachment.getCharsetName());
            return null;
        }).when(attachmentValidator).validateAttachment((SoapMessage)any(), (List)any());

        when(endpoint.getActor()).thenReturn(null);

        ReceiveSoapMessageAction soapMessageAction = new ReceiveSoapMessageAction.Builder()
                .endpoint(endpoint)
                .validator(new DomXmlMessageValidator())
                .message(controlMessageBuilder)
                .attachment(attachment)
                .attachmentValidator(attachmentValidator)
                .build();
        soapMessageAction.execute(context);
    }

    @Test
    public void testSoapMessageWithMultipleAttachmentDataTest() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId("1stAttachment");
        attachment.setContentType("text/xml");
        attachment.setContent("<TestAttachment><Message>Hello World1!</Message></TestAttachment>");
        attachment.setCharsetName("UTF-8");

        SoapAttachment attachment2 = new SoapAttachment();
        attachment2.setContentId("2ndAttachment");
        attachment2.setContentType("text/xml");
        attachment2.setContent("<TestAttachment><Message>Hello World2!</Message></TestAttachment>");
        attachment2.setCharsetName("UTF-16");

        Message controlMessage = new SoapMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration, attachmentValidator);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);

        doAnswer(invocation -> {
            Assertions.assertEquals(2L, ((List<SoapAttachment>)invocation.getArguments()[1]).size());

            SoapAttachment soapAttachment = ((List<SoapAttachment>)invocation.getArguments()[1]).get(0);
            Assertions.assertEquals("<TestAttachment><Message>Hello World1!</Message></TestAttachment>", soapAttachment.getContent());
            Assertions.assertEquals("1stAttachment", soapAttachment.getContentId());
            Assertions.assertEquals("text/xml", soapAttachment.getContentType());
            Assertions.assertEquals("UTF-8", soapAttachment.getCharsetName());

            soapAttachment = ((List<SoapAttachment>)invocation.getArguments()[1]).get(1);
            Assertions.assertEquals("<TestAttachment><Message>Hello World2!</Message></TestAttachment>", soapAttachment.getContent());
            Assertions.assertEquals("2ndAttachment", soapAttachment.getContentId());
            Assertions.assertEquals("text/xml", soapAttachment.getContentType());
            Assertions.assertEquals("UTF-16", soapAttachment.getCharsetName());
            return null;
        }).when(attachmentValidator).validateAttachment((SoapMessage)any(), (List)any());

        when(endpoint.getActor()).thenReturn(null);

        ReceiveSoapMessageAction soapMessageAction = new ReceiveSoapMessageAction.Builder()
                .endpoint(endpoint)
                .validator(new DomXmlMessageValidator())
                .message(controlMessageBuilder)
                .attachment(attachment)
                .attachment(attachment2)
                .attachmentValidator(attachmentValidator)
                .build();
        soapMessageAction.execute(context);

    }

    @Test
    public void testSoapMessageWithEmptyAttachmentContentTest() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId("myAttachment");
        attachment.setContent("");

        Message controlMessage = new SoapMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration, attachmentValidator);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);

        doAnswer(invocation -> {
            Assertions.assertEquals(1L, ((List<SoapAttachment>)invocation.getArguments()[1]).size());
            SoapAttachment soapAttachment = ((List<SoapAttachment>)invocation.getArguments()[1]).get(0);
            Assertions.assertEquals("", soapAttachment.getContent());
            Assertions.assertEquals("myAttachment", soapAttachment.getContentId());
            Assertions.assertEquals("text/plain", soapAttachment.getContentType());
            Assertions.assertEquals("UTF-8", soapAttachment.getCharsetName());
            return null;
        }).when(attachmentValidator).validateAttachment(any(SoapMessage.class), any(List.class));

        when(endpoint.getActor()).thenReturn(null);

        ReceiveSoapMessageAction soapMessageAction = new ReceiveSoapMessageAction.Builder()
                .endpoint(endpoint)
                .validator(new DomXmlMessageValidator())
                .message(controlMessageBuilder)
                .attachment(attachment)
                .attachmentValidator(attachmentValidator)
                .build();
        soapMessageAction.execute(context);

    }

    @Test
    public void testSoapMessageWithNoAttachmentExpected() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        Message controlMessage = new SoapMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration, attachmentValidator);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);

        when(endpoint.getActor()).thenReturn(null);

        ReceiveSoapMessageAction soapMessageAction = new ReceiveSoapMessageAction.Builder()
                .endpoint(endpoint)
                .validator(new DomXmlMessageValidator())
                .message(controlMessageBuilder)
                .attachmentValidator(attachmentValidator)
                .build();
        soapMessageAction.execute(context);

    }

    @Test
    public void testSoapMessageWithAttachmentResourceTest() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId("myAttachment");
        attachment.setContentType("text/xml");
        attachment.setContentResourcePath("classpath:org/citrusframework/ws/actions/test-attachment.xml");

        Message controlMessage = new SoapMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration, attachmentValidator);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);

        doAnswer(invocation -> {
            Assertions.assertEquals(1L, ((List<SoapAttachment>)invocation.getArguments()[1]).size());
            SoapAttachment soapAttachment = ((List<SoapAttachment>)invocation.getArguments()[1]).get(0);
            Assertions.assertEquals("<TestAttachment><Message>Hello World!</Message></TestAttachment>", soapAttachment.getContent().trim());
            Assertions.assertEquals("myAttachment", soapAttachment.getContentId());
            Assertions.assertEquals("text/xml", soapAttachment.getContentType());
            Assertions.assertEquals("UTF-8", soapAttachment.getCharsetName());
            return null;
        }).when(attachmentValidator).validateAttachment(any(SoapMessage.class), any(List.class));

        when(endpoint.getActor()).thenReturn(null);

        ReceiveSoapMessageAction soapMessageAction = new ReceiveSoapMessageAction.Builder()
                .endpoint(endpoint)
                .validator(new DomXmlMessageValidator())
                .message(controlMessageBuilder)
                .attachment(attachment)
                .attachmentValidator(attachmentValidator)
                .build();
        soapMessageAction.execute(context);

    }

    @Test
    public void testSoapMessageWithAttachmentResourceVariablesSupportTest() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        context.setVariable("myText", "Hello World!");

        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId("myAttachment");
        attachment.setContentType("text/xml");
        attachment.setContentResourcePath("classpath:org/citrusframework/ws/actions/test-attachment-with-variables.xml");

        Message controlMessage = new SoapMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration, attachmentValidator);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);

        doAnswer(invocation -> {
            Assertions.assertEquals(1L, ((List<SoapAttachment>)invocation.getArguments()[1]).size());
            SoapAttachment soapAttachment = ((List<SoapAttachment>)invocation.getArguments()[1]).get(0);
            Assertions.assertEquals("<TestAttachment><Message>Hello World!</Message></TestAttachment>", soapAttachment.getContent().trim());
            Assertions.assertEquals("myAttachment", soapAttachment.getContentId());
            Assertions.assertEquals("text/xml", soapAttachment.getContentType());
            Assertions.assertEquals("UTF-8", soapAttachment.getCharsetName());
            return null;
        }).when(attachmentValidator).validateAttachment(any(SoapMessage.class), any(List.class));

        when(endpoint.getActor()).thenReturn(null);

        ReceiveSoapMessageAction soapMessageAction = new ReceiveSoapMessageAction.Builder()
                .endpoint(endpoint)
                .validator(new DomXmlMessageValidator())
                .message(controlMessageBuilder)
                .attachment(attachment)
                .attachmentValidator(attachmentValidator)
                .build();
        soapMessageAction.execute(context);

    }

    @Test
    public void testSoapMessageWithAttachmentDataVariablesSupportTest() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<TestRequest><Message>Hello World!</Message></TestRequest>"));

        context.setVariable("myText", "Hello World!");

        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId("myAttachment");
        attachment.setContentType("text/xml");
        attachment.setContent("<TestAttachment><Message>${myText}</Message></TestAttachment>");

        Message controlMessage = new SoapMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration, attachmentValidator);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);

        doAnswer(invocation -> {
            Assertions.assertEquals(1L, ((List<SoapAttachment>)invocation.getArguments()[1]).size());
            SoapAttachment soapAttachment = ((List<SoapAttachment>)invocation.getArguments()[1]).get(0);
            Assertions.assertEquals("<TestAttachment><Message>Hello World!</Message></TestAttachment>", soapAttachment.getContent().trim());
            Assertions.assertEquals("myAttachment", soapAttachment.getContentId());
            Assertions.assertEquals("text/xml", soapAttachment.getContentType());
            Assertions.assertEquals("UTF-8", soapAttachment.getCharsetName());
            return null;
        }).when(attachmentValidator).validateAttachment(any(SoapMessage.class), any(List.class));

        when(endpoint.getActor()).thenReturn(null);

        ReceiveSoapMessageAction soapMessageAction = new ReceiveSoapMessageAction.Builder()
                .endpoint(endpoint)
                .validator(new DomXmlMessageValidator())
                .message(controlMessageBuilder)
                .attachment(attachment)
                .attachmentValidator(attachmentValidator)
                .build();
        soapMessageAction.execute(context);
    }
}
