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

package org.citrusframework.ws.xml;

import java.io.IOException;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.endpoint.AbstractEndpointAdapter;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.direct.DirectEndpointAdapter;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.message.MessageType;
import org.citrusframework.validation.DefaultMessageHeaderValidator;
import org.citrusframework.validation.DefaultTextEqualsMessageValidator;
import org.citrusframework.validation.builder.StaticMessageBuilder;
import org.citrusframework.ws.actions.ReceiveSoapMessageAction;
import org.citrusframework.ws.actions.SendSoapFaultAction;
import org.citrusframework.ws.message.SoapMessage;
import org.citrusframework.ws.server.WebServiceServer;
import org.citrusframework.xml.XmlTestLoader;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.citrusframework.endpoint.direct.DirectEndpoints.direct;
import static org.citrusframework.ws.endpoint.builder.WebServiceEndpoints.soap;

/**
 * @author Christoph Deppisch
 */
public class SendSoapFaultTest extends AbstractXmlActionTest {

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
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/ws/xml/send-soap-fault-test.xml");

        context.getReferenceResolver().bind("soapServer", soapServer);
        context.getReferenceResolver().bind("headerValidator", new DefaultMessageHeaderValidator());
        context.getMessageValidatorRegistry().addMessageValidator("validator", new DefaultTextEqualsMessageValidator());

        endpointAdapter.handleMessage(new SoapMessage("<TestMessage>Hello Citrus</TestMessage>").contentType("application/xml"));
        endpointAdapter.handleMessage(new SoapMessage("<TestMessage>Hello Citrus</TestMessage>").contentType("application/xml"));

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "SendSoapFaultTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 4L);
        Assert.assertEquals(result.getTestAction(0).getClass(), ReceiveSoapMessageAction.class);
        Assert.assertEquals(result.getTestAction(0).getName(), "soap:receive-request");

        Assert.assertEquals(result.getTestAction(1).getClass(), SendSoapFaultAction.class);
        Assert.assertEquals(result.getTestAction(1).getName(), "soap:send-fault");

        int actionIndex = 0;

        ReceiveSoapMessageAction action = (ReceiveSoapMessageAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getMessageBuilder().build(context, MessageType.XML.name()).getPayload(), "<TestMessage>Hello Citrus</TestMessage>");

        SendSoapFaultAction send = (SendSoapFaultAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(send.getEndpointUri(), "soapServer");
        Assert.assertNotNull(send.getMessageBuilder());
        Assert.assertEquals(send.getMessageBuilder().getClass(), StaticMessageBuilder.class);

        StaticMessageBuilder messageBuilder = (StaticMessageBuilder)send.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 1L);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("operation"), "sendFault");
        Assert.assertEquals(send.getFaultCode(), "{http://citrusframework.org/faults}citrus-ns:FAULT-1000");
        Assert.assertEquals(send.getFaultString(), "FaultString");
        Assert.assertEquals(send.getFaultDetails().size(), 1);
        Assert.assertTrue(send.getFaultDetails().get(0).startsWith("<ns0:FaultDetail xmlns:ns0=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">"));
        Assert.assertNull(send.getFaultActor());

        action = (ReceiveSoapMessageAction) result.getTestAction(actionIndex++);
        Assert.assertEquals(action.getMessageBuilder().build(context, MessageType.XML.name()).getPayload(), "<TestMessage>Hello Citrus</TestMessage>");

        send = (SendSoapFaultAction) result.getTestAction(actionIndex);
        Assert.assertEquals(send.getEndpointUri(), "soapServer");
        Assert.assertNotNull(send.getMessageBuilder());
        Assert.assertEquals(send.getMessageBuilder().getClass(), StaticMessageBuilder.class);

        messageBuilder = (StaticMessageBuilder)send.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 1);
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).get("operation"), "sendFault");
        Assert.assertEquals(send.getFaultCode(), "{http://citrusframework.org/faults}citrus-ns:FAULT-1001");
        Assert.assertEquals(send.getFaultString(), "FaultString");
        Assert.assertEquals(send.getFaultDetails().size(), 0);
        Assert.assertEquals(send.getFaultDetailResourcePaths().size(), 1);
        Assert.assertEquals(send.getFaultDetailResourcePaths().get(0), "classpath:org/citrusframework/ws/actions/test-fault-detail.xml");
        Assert.assertEquals(send.getFaultActor(), "FaultActor");
    }
}
