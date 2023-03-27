/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.citrus.dsl.runner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;

import org.citrusframework.citrus.TestCase;
import org.citrusframework.citrus.container.SequenceAfterTest;
import org.citrusframework.citrus.container.SequenceBeforeTest;
import org.citrusframework.citrus.spi.ReferenceResolver;
import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.endpoint.Endpoint;
import org.citrusframework.citrus.message.Message;
import org.citrusframework.citrus.messaging.Producer;
import org.citrusframework.citrus.report.TestActionListeners;
import org.citrusframework.citrus.dsl.UnitTestSupport;
import org.citrusframework.citrus.validation.builder.StaticMessageBuilder;
import org.citrusframework.citrus.ws.actions.SendSoapFaultAction;
import org.citrusframework.citrus.ws.message.SoapFault;
import org.citrusframework.citrus.ws.server.WebServiceServer;
import org.mockito.Mockito;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class SendSoapFaultTestRunnerTest extends UnitTestSupport {

    public static final String FAULT_STRING = "Something went wrong";
    public static final String FAULT_CODE = "CITRUS-1000";
    public static final String ERROR_DETAIL = "<ErrorDetail><message>Something went wrong</message></ErrorDetail>";

    private WebServiceServer soapServer = Mockito.mock(WebServiceServer.class);
    private Producer messageProducer = Mockito.mock(Producer.class);
    private ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);
    private Resource resource = Mockito.mock(Resource.class);

    @Test
    public void testSendSoapFault() {
        reset(soapServer, messageProducer);
        when(soapServer.createProducer()).thenReturn(messageProducer);
        when(soapServer.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            SoapFault message = (SoapFault) invocation.getArguments()[0];
            Assert.assertEquals(message.getFaultActor(), "faultActor");
            Assert.assertEquals(message.getFaultCode(), FAULT_CODE);
            Assert.assertEquals(message.getFaultString(), FAULT_STRING);
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                soap(builder -> builder.server(soapServer)
                        .sendFault()
                        .faultActor("faultActor")
                        .faultCode(FAULT_CODE)
                        .faultString(FAULT_STRING));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendSoapFaultAction.class);

        SendSoapFaultAction action = (SendSoapFaultAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), soapServer);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageBuilder.class);

        StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(), "");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0L);
        Assert.assertEquals(action.getFaultActor(), "faultActor");
        Assert.assertEquals(action.getFaultCode(), FAULT_CODE);
        Assert.assertEquals(action.getFaultString(), FAULT_STRING);
    }

    @Test
    public void testSendSoapFaultByEndpointName() {
        TestContext context = applicationContext.getBean(TestContext.class);

        reset(referenceResolver, soapServer, messageProducer);
        when(soapServer.createProducer()).thenReturn(messageProducer);
        when(soapServer.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            SoapFault message = (SoapFault) invocation.getArguments()[0];
            Assert.assertEquals(message.getFaultCode(), FAULT_CODE);
            Assert.assertEquals(message.getFaultString(), FAULT_STRING);
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve("soapServer", Endpoint.class)).thenReturn(soapServer);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                soap(builder -> builder.server("soapServer")
                        .sendFault()
                        .faultCode(FAULT_CODE)
                        .faultString(FAULT_STRING));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendSoapFaultAction.class);

        SendSoapFaultAction action = (SendSoapFaultAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpointUri(), "soapServer");
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageBuilder.class);

        StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(), "");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0L);
        Assert.assertNull(action.getFaultActor());
        Assert.assertEquals(action.getFaultCode(), FAULT_CODE);
        Assert.assertEquals(action.getFaultString(), FAULT_STRING);
    }

    @Test
    public void testSendSoapFaultWithDetailResource() throws IOException {
        reset(resource, soapServer, messageProducer);
        when(soapServer.createProducer()).thenReturn(messageProducer);
        when(soapServer.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            SoapFault message = (SoapFault) invocation.getArguments()[0];
            Assert.assertEquals(message.getFaultCode(), FAULT_CODE);
            Assert.assertEquals(message.getFaultString(), FAULT_STRING);
            Assert.assertEquals(message.getFaultDetails().size(), 1L);
            Assert.assertEquals(message.getFaultDetails().get(0), ERROR_DETAIL);
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(ERROR_DETAIL.getBytes()));

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                soap(builder -> builder.server(soapServer)
                        .sendFault()
                        .faultCode(FAULT_CODE)
                        .faultDetailResource(resource)
                        .faultString(FAULT_STRING));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendSoapFaultAction.class);

        SendSoapFaultAction action = (SendSoapFaultAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), soapServer);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageBuilder.class);

        StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(), "");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0L);
        Assert.assertEquals(action.getFaultDetails().size(), 1L);
        Assert.assertEquals(action.getFaultDetails().get(0), ERROR_DETAIL);
        Assert.assertEquals(action.getFaultCode(), FAULT_CODE);
        Assert.assertEquals(action.getFaultString(), FAULT_STRING);
    }

    @Test
    public void testSendSoapFaultWithDetail() {
        reset(soapServer, messageProducer);
        when(soapServer.createProducer()).thenReturn(messageProducer);
        when(soapServer.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            SoapFault message = (SoapFault) invocation.getArguments()[0];
            Assert.assertEquals(message.getFaultCode(), FAULT_CODE);
            Assert.assertEquals(message.getFaultString(), FAULT_STRING);
            Assert.assertEquals(message.getFaultDetails().size(), 1L);
            Assert.assertEquals(message.getFaultDetails().get(0), "DETAIL");
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                soap(builder -> builder.server(soapServer)
                        .sendFault()
                        .faultCode(FAULT_CODE)
                        .faultDetail("DETAIL")
                        .faultString(FAULT_STRING));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendSoapFaultAction.class);

        SendSoapFaultAction action = (SendSoapFaultAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), soapServer);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageBuilder.class);

        StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(), "");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0L);
        Assert.assertEquals(action.getFaultDetails().size(), 1L);
        Assert.assertEquals(action.getFaultDetails().get(0), "DETAIL");
        Assert.assertEquals(action.getFaultCode(), FAULT_CODE);
        Assert.assertEquals(action.getFaultString(), FAULT_STRING);
    }

    @Test
    public void testSendSoapFaultWithDetailResourcePath() {
        reset(soapServer, messageProducer);
        when(soapServer.createProducer()).thenReturn(messageProducer);
        when(soapServer.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            SoapFault message = (SoapFault) invocation.getArguments()[0];
            Assert.assertEquals(message.getFaultCode(), FAULT_CODE);
            Assert.assertEquals(message.getFaultString(), FAULT_STRING);
            Assert.assertEquals(message.getFaultDetails().size(), 1L);
            Assert.assertEquals(message.getFaultDetails().get(0).trim(), ERROR_DETAIL);
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                soap(builder -> builder.server(soapServer)
                        .sendFault()
                        .faultCode(FAULT_CODE)
                        .faultDetailResource("classpath:org/citrusframework/citrus/dsl/runner/soap-fault-detail.xml")
                        .faultString(FAULT_STRING));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendSoapFaultAction.class);

        SendSoapFaultAction action = (SendSoapFaultAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), soapServer);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageBuilder.class);

        StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(), "");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0L);
        Assert.assertEquals(action.getFaultDetails().size(), 0L);
        Assert.assertEquals(action.getFaultDetailResourcePaths().size(), 1L);
        Assert.assertEquals(action.getFaultDetailResourcePaths().get(0), "classpath:org/citrusframework/citrus/dsl/runner/soap-fault-detail.xml");
        Assert.assertEquals(action.getFaultCode(), FAULT_CODE);
        Assert.assertEquals(action.getFaultString(), FAULT_STRING);
    }

    @Test
    public void testSendSoapFaultWithMultipleDetail() {
        reset(soapServer, messageProducer);
        when(soapServer.createProducer()).thenReturn(messageProducer);
        when(soapServer.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            SoapFault message = (SoapFault) invocation.getArguments()[0];
            Assert.assertEquals(message.getFaultCode(), FAULT_CODE);
            Assert.assertEquals(message.getFaultString(), FAULT_STRING);
            Assert.assertEquals(message.getFaultDetails().size(), 2L);
            Assert.assertEquals(message.getFaultDetails().get(0), "DETAIL1");
            Assert.assertEquals(message.getFaultDetails().get(1), "DETAIL2");
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                soap(builder -> builder.server(soapServer)
                        .sendFault()
                        .faultCode(FAULT_CODE)
                        .faultDetail("DETAIL1")
                        .faultDetail("DETAIL2")
                        .faultString(FAULT_STRING));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendSoapFaultAction.class);

        SendSoapFaultAction action = (SendSoapFaultAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), soapServer);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageBuilder.class);

        StaticMessageBuilder messageBuilder = (StaticMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(), "");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0L);
        Assert.assertEquals(action.getFaultDetails().size(), 2L);
        Assert.assertEquals(action.getFaultDetails().get(0), "DETAIL1");
        Assert.assertEquals(action.getFaultDetails().get(1), "DETAIL2");
        Assert.assertEquals(action.getFaultCode(), FAULT_CODE);
        Assert.assertEquals(action.getFaultString(), FAULT_STRING);
    }

}
