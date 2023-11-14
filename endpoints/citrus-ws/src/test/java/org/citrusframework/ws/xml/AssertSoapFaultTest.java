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
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.direct.DirectEndpointAdapter;
import org.citrusframework.endpoint.direct.DirectSyncEndpointConfiguration;
import org.citrusframework.message.Message;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.SocketUtils;
import org.citrusframework.validation.DefaultMessageHeaderValidator;
import org.citrusframework.validation.DefaultTextEqualsMessageValidator;
import org.citrusframework.ws.actions.AssertSoapFault;
import org.citrusframework.ws.client.WebServiceClient;
import org.citrusframework.ws.message.SoapFault;
import org.citrusframework.ws.message.SoapMessage;
import org.citrusframework.ws.server.WebServiceServer;
import org.citrusframework.ws.validation.SimpleSoapFaultValidator;
import org.citrusframework.ws.validation.SoapFaultValidator;
import org.citrusframework.xml.XmlTestLoader;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.citrusframework.ws.endpoint.builder.WebServiceEndpoints.soap;

/**
 * @author Christoph Deppisch
 */
public class AssertSoapFaultTest extends AbstractXmlActionTest {

    private final int port = SocketUtils.findAvailableTcpPort(8080);
    private final String uri = "http://localhost:" + port + "/test";

    private WebServiceServer soapServer;
    private WebServiceClient soapClient;

    private final Queue<SoapMessage> responses = new ArrayBlockingQueue<>(4);

    @BeforeClass
    public void setupEndpoints() {
        EndpointAdapter endpointAdapter = new DirectEndpointAdapter(new DirectSyncEndpointConfiguration()) {
            @Override
            public Message handleMessageInternal(Message request) {
                return responses.isEmpty() ? new SoapMessage() : responses.remove();
            }
        };

        soapServer = soap().server()
                .port(port)
                .timeout(500L)
                .endpointAdapter(endpointAdapter)
                .autoStart(true)
                .name("soapServer")
                .build();
        soapServer.initialize();

        soapClient = soap().client()
                .defaultUri(uri)
                .name("soapClient")
                .build();
    }

    @AfterClass(alwaysRun = true)
    public void cleanupEndpoints() {
        if (soapServer != null) {
            soapServer.stop();
        }
    }

    @Test
    public void shouldLoadSoapClientActions() throws IOException {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/ws/xml/assert-soap-fault-test.xml");

        context.setVariable("port", port);

        context.getReferenceResolver().bind("soapClient", soapClient);
        context.getReferenceResolver().bind("soapServer", soapServer);

        context.getReferenceResolver().bind("headerValidator", new DefaultMessageHeaderValidator());
        context.getMessageValidatorRegistry().addMessageValidator("validator", new DefaultTextEqualsMessageValidator());
        context.getReferenceResolver().bind("soapFaultValidator", new SimpleSoapFaultValidator());
        context.getReferenceResolver().bind("customSoapFaultValidator", new SimpleSoapFaultValidator());

        responses.add(createSoapFault("FAULT-1001"));
        responses.add(createSoapFault("FAULT-1002"));
        responses.add(createSoapFault("FAULT-1003").addFaultDetail("<ns0:FaultDetail xmlns:ns0=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                    "<ns0:DetailId>1000</ns0:DetailId>" +
                "</ns0:FaultDetail>"));
        responses.add(createSoapFault("FAULT-1004").addFaultDetail(FileUtils.readToString(
                FileUtils.getFileResource("classpath:org/citrusframework/ws/actions/test-fault-detail.xml"))));

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "AssertSoapFaultTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 4L);
        Assert.assertEquals(result.getTestAction(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(result.getTestAction(0).getName(), "soap:assert-fault");

        int actionIndex = 0;

        AssertSoapFault action = (AssertSoapFault) result.getTestAction(actionIndex++);
        Assert.assertNotNull(action.getAction());
        Assert.assertEquals(action.getValidator(), context.getReferenceResolver().resolve("soapFaultValidator", SoapFaultValidator.class));
        Assert.assertEquals(action.getFaultCode(), "{http://citrusframework.org/faults}FAULT-1001");
        Assert.assertNull(action.getFaultString());
        Assert.assertEquals(action.getFaultDetails().size(), 0L);
        Assert.assertNotNull(action.getValidationContext());

        action = (AssertSoapFault) result.getTestAction(actionIndex++);
        Assert.assertNotNull(action.getAction());
        Assert.assertEquals(action.getValidator(), context.getReferenceResolver().resolve("soapFaultValidator", SoapFaultValidator.class));
        Assert.assertEquals(action.getFaultCode(), "{http://citrusframework.org/faults}FAULT-1002");
        Assert.assertEquals(action.getFaultString(), "FaultString");
        Assert.assertEquals(action.getFaultDetails().size(), 0L);
        Assert.assertNotNull(action.getValidationContext());

        action = (AssertSoapFault) result.getTestAction(actionIndex++);
        Assert.assertNotNull(action.getAction());
        Assert.assertEquals(action.getValidator(), context.getReferenceResolver().resolve("soapFaultValidator", SoapFaultValidator.class));
        Assert.assertEquals(action.getFaultCode(), "{http://citrusframework.org/faults}FAULT-1003");
        Assert.assertEquals(action.getFaultString(), "FaultString");
        Assert.assertEquals(action.getFaultActor(), "FaultActor");
        Assert.assertEquals(action.getFaultDetails().size(), 1L);
        Assert.assertEquals(action.getFaultDetails().get(0), "<ns0:FaultDetail xmlns:ns0=\"http://citrusframework.org/schemas/samples/HelloService.xsd\">" +
                    "<ns0:DetailId>1000</ns0:DetailId>" +
                "</ns0:FaultDetail>");
        Assert.assertEquals(action.getValidationContext().getValidationContexts().size(), 1L);

        action = (AssertSoapFault) result.getTestAction(actionIndex);
        Assert.assertNotNull(action.getAction());
        Assert.assertEquals(action.getValidator(), context.getReferenceResolver().resolve("customSoapFaultValidator", SoapFaultValidator.class));
        Assert.assertEquals(action.getFaultCode(), "{http://citrusframework.org/faults}FAULT-1004");
        Assert.assertEquals(action.getFaultString(), "FaultString");
        Assert.assertEquals(action.getFaultDetails().size(), 0L);
        Assert.assertEquals(action.getFaultDetailResourcePaths().size(), 1L);
        Assert.assertEquals(action.getFaultDetailResourcePaths().get(0), "classpath:org/citrusframework/ws/actions/test-fault-detail.xml");
        Assert.assertEquals(action.getValidationContext().getValidationContexts().size(), 1L);
    }

    private SoapFault createSoapFault(String faultCode) {
        SoapFault fault = new SoapFault("<TestResponse>Hello User</TestResponse>");

        fault.faultCode(String.format("{http://citrusframework.org/faults}%s", faultCode));
        fault.faultString("FaultString");
        fault.faultActor("FaultActor");

        return fault;
    }
}
