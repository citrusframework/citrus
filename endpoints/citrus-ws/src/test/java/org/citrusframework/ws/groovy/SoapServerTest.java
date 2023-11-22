/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.ws.groovy;

import java.io.IOException;
import java.util.Arrays;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.endpoint.AbstractEndpointAdapter;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.direct.DirectEndpointAdapter;
import org.citrusframework.groovy.GroovyTestLoader;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.message.MessageType;
import org.citrusframework.util.FileUtils;
import org.citrusframework.validation.DefaultMessageHeaderValidator;
import org.citrusframework.validation.DefaultTextEqualsMessageValidator;
import org.citrusframework.ws.actions.ReceiveSoapMessageAction;
import org.citrusframework.ws.actions.SendSoapMessageAction;
import org.citrusframework.ws.message.SoapAttachment;
import org.citrusframework.ws.message.SoapMessage;
import org.citrusframework.ws.server.WebServiceServer;
import org.citrusframework.ws.validation.SimpleSoapAttachmentValidator;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.citrusframework.endpoint.direct.DirectEndpoints.direct;
import static org.citrusframework.ws.endpoint.builder.WebServiceEndpoints.soap;

/**
 * @author Christoph Deppisch
 */
public class SoapServerTest extends AbstractGroovyActionDslTest {

    private WebServiceServer soapServer;

    private final MessageQueue inboundQueue = new DefaultMessageQueue("inboundQueue");
    private final EndpointAdapter endpointAdapter = new DirectEndpointAdapter(direct()
            .synchronous()
            .timeout(100L)
            .queue(inboundQueue)
            .build());

    @BeforeClass
    public void setupEndpoints() {
        soapServer = soap().server()
                .timeout(100L)
                .endpointAdapter(endpointAdapter)
                .autoStart(true)
                .name("soapServer")
                .build();
    }

    @BeforeMethod
    @Override
    public void prepareTest() {
        super.prepareTest();
        ((AbstractEndpointAdapter) endpointAdapter).setTestContextFactory(testContextFactory);
    }

    @Test
    public void shouldLoadSoapServerActions() throws IOException {
        GroovyTestLoader testLoader = createTestLoader("classpath:org/citrusframework/ws/groovy/soap-server.test.groovy");

        context.getReferenceResolver().bind("soapServer", soapServer);
        context.getReferenceResolver().bind("headerValidator", new DefaultMessageHeaderValidator());
        context.getMessageValidatorRegistry().addMessageValidator("validator", new DefaultTextEqualsMessageValidator());
        context.getReferenceResolver().bind("soapAttachmentValidator", new SimpleSoapAttachmentValidator());
        context.getReferenceResolver().bind("mySoapAttachmentValidator", new SimpleSoapAttachmentValidator());

        endpointAdapter.handleMessage(createSoapMessage(new SoapAttachment("MySoapAttachment", "text/plain", "This is an attachment!")).soapAction("myAction"));
        endpointAdapter.handleMessage(createSoapMessage(new SoapAttachment("MySoapAttachment", "application/xml",
                FileUtils.readToString(FileUtils.getFileResource("classpath:org/citrusframework/ws/actions/test-attachment.xml")), "UTF-8")));
        endpointAdapter.handleMessage(createSoapMessage(new SoapAttachment("FirstSoapAttachment", "text/plain", "This is an attachment!"),
                new SoapAttachment("SecondSoapAttachment", "application/xml", FileUtils.readToString(FileUtils.getFileResource("classpath:org/citrusframework/ws/actions/test-attachment.xml")), "UTF-8")));

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "SoapServerTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 6L);
        Assert.assertEquals(result.getTestAction(0).getClass(), ReceiveSoapMessageAction.class);
        Assert.assertEquals(result.getTestAction(0).getName(), "soap:receive-request");

        Assert.assertEquals(result.getTestAction(1).getClass(), SendSoapMessageAction.class);
        Assert.assertEquals(result.getTestAction(1).getName(), "soap:send-response");

        int actionIndex = 0;

        ReceiveSoapMessageAction action = (ReceiveSoapMessageAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getMessageBuilder().build(context, MessageType.XML.name()).getPayload(), "<TestMessage>Hello Citrus</TestMessage>");
        Assert.assertEquals(action.getAttachmentValidator(), context.getReferenceResolver().resolve("soapAttachmentValidator"));
        Assert.assertEquals(action.getAttachments().size(), 1L);
        Assert.assertEquals(action.getAttachments().get(0).getContent().trim(), "This is an attachment!");
        Assert.assertNull(action.getAttachments().get(0).getContentResourcePath());
        Assert.assertEquals(action.getAttachments().get(0).getContentId(), "MySoapAttachment");
        Assert.assertEquals(action.getAttachments().get(0).getContentType(), "text/plain");

        SendSoapMessageAction send = (SendSoapMessageAction) result.getTestAction(actionIndex++);
        Assert.assertFalse(send.isForkMode());
        Assert.assertEquals(send.getAttachments().size(), 1L);
        Assert.assertEquals(send.getAttachments().get(0).getContent().trim(), "This is an attachment!");
        Assert.assertNull(send.getAttachments().get(0).getContentResourcePath());
        Assert.assertEquals(send.getAttachments().get(0).getContentId(), "MySoapAttachment");
        Assert.assertEquals(send.getAttachments().get(0).getContentType(), "text/plain");

        action = (ReceiveSoapMessageAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getAttachmentValidator(), context.getReferenceResolver().resolve("mySoapAttachmentValidator"));
        Assert.assertEquals(action.getAttachments().size(), 1L);
        Assert.assertNull(action.getAttachments().get(0).getContentResourcePath());
        Assert.assertNotNull(action.getAttachments().get(0).getContent());
        Assert.assertEquals(action.getAttachments().get(0).getContentId(), "MySoapAttachment");
        Assert.assertEquals(action.getAttachments().get(0).getContentType(), "application/xml");
        Assert.assertEquals(action.getAttachments().get(0).getCharsetName(), "UTF-8");

        send = (SendSoapMessageAction) result.getTestAction(actionIndex++);
        Assert.assertFalse(send.isForkMode());
        Assert.assertEquals(send.getAttachments().size(), 1L);
        Assert.assertEquals(send.getAttachments().get(0).getContent(), FileUtils.readToString(FileUtils.getFileResource("classpath:org/citrusframework/ws/actions/test-attachment.xml")));
        Assert.assertEquals(send.getAttachments().get(0).getContentId(), "MySoapAttachment");
        Assert.assertEquals(send.getAttachments().get(0).getContentType(), "application/xml");
        Assert.assertEquals(send.getAttachments().get(0).getCharsetName(), "UTF-8");

        action = (ReceiveSoapMessageAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getAttachmentValidator(), context.getReferenceResolver().resolve("soapAttachmentValidator"));
        Assert.assertEquals(action.getAttachments().size(), 2L);
        Assert.assertEquals(action.getAttachments().get(0).getContent().trim(), "This is an attachment!");
        Assert.assertNull(action.getAttachments().get(0).getContentResourcePath());
        Assert.assertEquals(action.getAttachments().get(0).getContentId(), "FirstSoapAttachment");
        Assert.assertEquals(action.getAttachments().get(0).getContentType(), "text/plain");
        Assert.assertNull(action.getAttachments().get(1).getContentResourcePath());
        Assert.assertNotNull(action.getAttachments().get(1).getContent());
        Assert.assertEquals(action.getAttachments().get(1).getContentId(), "SecondSoapAttachment");
        Assert.assertEquals(action.getAttachments().get(1).getContentType(), "application/xml");
        Assert.assertEquals(action.getAttachments().get(1).getCharsetName(), "UTF-8");

        send = (SendSoapMessageAction) result.getTestAction(actionIndex);
        Assert.assertFalse(send.isForkMode());
        Assert.assertEquals(send.getAttachments().size(), 2L);
        Assert.assertEquals(send.getAttachments().get(0).getContent().trim(), "This is an attachment!");
        Assert.assertNull(send.getAttachments().get(0).getContentResourcePath());
        Assert.assertEquals(send.getAttachments().get(0).getContentId(), "FirstSoapAttachment");
        Assert.assertEquals(send.getAttachments().get(0).getContentType(), "text/plain");
        Assert.assertNull(send.getAttachments().get(1).getContentResourcePath());
        Assert.assertNotNull(send.getAttachments().get(1).getContent());
        Assert.assertEquals(send.getAttachments().get(1).getContentId(), "SecondSoapAttachment");
        Assert.assertEquals(send.getAttachments().get(1).getContentType(), "application/xml");
        Assert.assertEquals(send.getAttachments().get(1).getCharsetName(), "UTF-8");
    }

    private SoapMessage createSoapMessage(SoapAttachment... attachments) {
        SoapMessage message = new SoapMessage("<TestMessage>Hello Citrus</TestMessage>")
                .contentType("application/xml");

        Arrays.stream(attachments).forEach(message::addAttachment);

        return message;
    }
}
