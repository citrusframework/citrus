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

import java.util.List;

import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.message.Message;
import org.citrusframework.message.builder.DefaultPayloadBuilder;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.xml.DomXmlMessageValidator;
import org.citrusframework.ws.message.SoapAttachment;
import org.citrusframework.ws.message.SoapMessage;
import org.citrusframework.ws.validation.SoapAttachmentValidator;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ReceiveSoapMessageActionTest extends AbstractTestNGUnitTest {

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
        MockitoAnnotations.openMocks(this);
        return super.createTestContextFactory();
    }

    @Test
    public void testSoapMessageWithDefaultAttachmentDataTest() throws Exception {
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

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Assert.assertEquals(((List<SoapAttachment>)invocation.getArguments()[1]).size(), 1L);
                SoapAttachment soapAttachment = ((List<SoapAttachment>)invocation.getArguments()[1]).get(0);
                Assert.assertEquals(soapAttachment.getContent(), "TestAttachment!");
                Assert.assertNull(soapAttachment.getContentId());
                Assert.assertEquals(soapAttachment.getContentType(), "text/plain");
                return null;
            }
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
    public void testSoapMessageWithAttachmentDataTest() throws Exception {
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

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Assert.assertEquals(((List<SoapAttachment>)invocation.getArguments()[1]).size(), 1L);
                SoapAttachment soapAttachment = ((List<SoapAttachment>)invocation.getArguments()[1]).get(0);
                Assert.assertEquals(soapAttachment.getContent(), "<TestAttachment><Message>Hello World!</Message></TestAttachment>");
                Assert.assertEquals(soapAttachment.getContentId(), "myAttachment");
                Assert.assertEquals(soapAttachment.getContentType(), "text/xml");
                Assert.assertEquals(soapAttachment.getCharsetName(), "UTF-16");
                return null;
            }
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
    public void testSoapMessageWithMultipleAttachmentDataTest() throws Exception {
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

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Assert.assertEquals(((List<SoapAttachment>)invocation.getArguments()[1]).size(), 2L);

                SoapAttachment soapAttachment = ((List<SoapAttachment>)invocation.getArguments()[1]).get(0);
                Assert.assertEquals(soapAttachment.getContent(), "<TestAttachment><Message>Hello World1!</Message></TestAttachment>");
                Assert.assertEquals(soapAttachment.getContentId(), "1stAttachment");
                Assert.assertEquals(soapAttachment.getContentType(), "text/xml");
                Assert.assertEquals(soapAttachment.getCharsetName(), "UTF-8");

                soapAttachment = ((List<SoapAttachment>)invocation.getArguments()[1]).get(1);
                Assert.assertEquals(soapAttachment.getContent(), "<TestAttachment><Message>Hello World2!</Message></TestAttachment>");
                Assert.assertEquals(soapAttachment.getContentId(), "2ndAttachment");
                Assert.assertEquals(soapAttachment.getContentType(), "text/xml");
                Assert.assertEquals(soapAttachment.getCharsetName(), "UTF-16");
                return null;
            }
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
    public void testSoapMessageWithEmptyAttachmentContentTest() throws Exception {
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

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Assert.assertEquals(((List<SoapAttachment>)invocation.getArguments()[1]).size(), 1L);
                SoapAttachment soapAttachment = ((List<SoapAttachment>)invocation.getArguments()[1]).get(0);
                Assert.assertEquals(soapAttachment.getContent(), "");
                Assert.assertEquals(soapAttachment.getContentId(), "myAttachment");
                Assert.assertEquals(soapAttachment.getContentType(), "text/plain");
                Assert.assertEquals(soapAttachment.getCharsetName(), "UTF-8");
                return null;
            }
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
    public void testSoapMessageWithNoAttachmentExpected() throws Exception {
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
    public void testSoapMessageWithAttachmentResourceTest() throws Exception {
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

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Assert.assertEquals(((List<SoapAttachment>)invocation.getArguments()[1]).size(), 1L);
                SoapAttachment soapAttachment = ((List<SoapAttachment>)invocation.getArguments()[1]).get(0);
                Assert.assertEquals(soapAttachment.getContent().trim(), "<TestAttachment><Message>Hello World!</Message></TestAttachment>");
                Assert.assertEquals(soapAttachment.getContentId(), "myAttachment");
                Assert.assertEquals(soapAttachment.getContentType(), "text/xml");
                Assert.assertEquals(soapAttachment.getCharsetName(), "UTF-8");
                return null;
            }
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
    public void testSoapMessageWithAttachmentResourceVariablesSupportTest() throws Exception {
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

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Assert.assertEquals(((List<SoapAttachment>)invocation.getArguments()[1]).size(), 1L);
                SoapAttachment soapAttachment = ((List<SoapAttachment>)invocation.getArguments()[1]).get(0);
                Assert.assertEquals(soapAttachment.getContent().trim(), "<TestAttachment><Message>Hello World!</Message></TestAttachment>");
                Assert.assertEquals(soapAttachment.getContentId(), "myAttachment");
                Assert.assertEquals(soapAttachment.getContentType(), "text/xml");
                Assert.assertEquals(soapAttachment.getCharsetName(), "UTF-8");
                return null;
            }
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
    public void testSoapMessageWithAttachmentDataVariablesSupportTest() throws Exception {
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

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Assert.assertEquals(((List<SoapAttachment>)invocation.getArguments()[1]).size(), 1L);
                SoapAttachment soapAttachment = ((List<SoapAttachment>)invocation.getArguments()[1]).get(0);
                Assert.assertEquals(soapAttachment.getContent().trim(), "<TestAttachment><Message>Hello World!</Message></TestAttachment>");
                Assert.assertEquals(soapAttachment.getContentId(), "myAttachment");
                Assert.assertEquals(soapAttachment.getContentType(), "text/xml");
                Assert.assertEquals(soapAttachment.getCharsetName(), "UTF-8");
                return null;
            }
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
