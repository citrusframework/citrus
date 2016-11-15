/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.dsl.design;

import com.consol.citrus.TestCase;
import com.consol.citrus.container.SequenceAfterTest;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.dsl.actions.DelegatingTestAction;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.builder.StaticMessageContentBuilder;
import com.consol.citrus.ws.actions.SendSoapFaultAction;
import com.consol.citrus.ws.server.WebServiceServer;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;


/**
 * @author Christoph Deppisch
 */
public class SendSoapFaultTestDesignerTest extends AbstractTestNGUnitTest {

    public static final String FAULT_STRING = "Something went wrong";
    public static final String FAULT_CODE = "CITRUS-1000";

    private WebServiceServer soapServer = Mockito.mock(WebServiceServer.class);
    private ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);
    private Resource resource = Mockito.mock(Resource.class);

    @Test
    public void testSendSoapFault() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                soap().server(soapServer)
                        .sendFault()
                        .faultActor("faultActor")
                        .faultCode(FAULT_CODE)
                        .faultString(FAULT_STRING);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), SendSoapFaultAction.class);

        SendSoapFaultAction action = (SendSoapFaultAction) ((DelegatingTestAction)test.getActions().get(0)).getDelegate();
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), soapServer);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageContentBuilder.class);

        StaticMessageContentBuilder messageBuilder = (StaticMessageContentBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(), "");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        Assert.assertEquals(action.getFaultActor(), "faultActor");
        Assert.assertEquals(action.getFaultCode(), FAULT_CODE);
        Assert.assertEquals(action.getFaultString(), FAULT_STRING);
    }

    @Test
    public void testSendSoapFaultByEndpointName() {
        reset(applicationContextMock);
        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBean("soapServer", Endpoint.class)).thenReturn(soapServer);
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());
        MockTestDesigner builder = new MockTestDesigner(applicationContextMock, context) {
            @Override
            public void configure() {
                soap().server("soapServer")
                        .sendFault()
                        .faultCode(FAULT_CODE)
                        .faultString(FAULT_STRING);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), SendSoapFaultAction.class);

        SendSoapFaultAction action = (SendSoapFaultAction) ((DelegatingTestAction)test.getActions().get(0)).getDelegate();
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), soapServer);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageContentBuilder.class);

        StaticMessageContentBuilder messageBuilder = (StaticMessageContentBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(), "");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        Assert.assertNull(action.getFaultActor());
        Assert.assertEquals(action.getFaultCode(), FAULT_CODE);
        Assert.assertEquals(action.getFaultString(), FAULT_STRING);
    }

    @Test
    public void testSendSoapFaultWithDetailResource() throws IOException {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                soap().server(soapServer)
                        .sendFault()
                        .faultCode(FAULT_CODE)
                        .faultDetailResource(resource)
                        .faultString(FAULT_STRING);
            }
        };

        reset(resource);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("someDetailData".getBytes()));
        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), SendSoapFaultAction.class);

        SendSoapFaultAction action = (SendSoapFaultAction) ((DelegatingTestAction)test.getActions().get(0)).getDelegate();
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), soapServer);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageContentBuilder.class);

        StaticMessageContentBuilder messageBuilder = (StaticMessageContentBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(), "");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        Assert.assertEquals(action.getFaultDetails().size(), 1L);
        Assert.assertEquals(action.getFaultDetails().get(0), "someDetailData");
        Assert.assertEquals(action.getFaultCode(), FAULT_CODE);
        Assert.assertEquals(action.getFaultString(), FAULT_STRING);
    }

    @Test
    public void testSendSoapFaultWithDetail() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                soap().server(soapServer)
                        .sendFault()
                        .faultCode(FAULT_CODE)
                        .faultDetail("DETAIL")
                        .faultString(FAULT_STRING);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), SendSoapFaultAction.class);

        SendSoapFaultAction action = (SendSoapFaultAction) ((DelegatingTestAction)test.getActions().get(0)).getDelegate();
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), soapServer);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageContentBuilder.class);

        StaticMessageContentBuilder messageBuilder = (StaticMessageContentBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(), "");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        Assert.assertEquals(action.getFaultDetails().size(), 1L);
        Assert.assertEquals(action.getFaultDetails().get(0), "DETAIL");
        Assert.assertEquals(action.getFaultCode(), FAULT_CODE);
        Assert.assertEquals(action.getFaultString(), FAULT_STRING);
    }

    @Test
    public void testSendSoapFaultWithDetailResourcePath() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                soap().server(soapServer)
                        .sendFault()
                        .faultCode(FAULT_CODE)
                        .faultDetailResource("com/consol/citrus/soap/fault.xml")
                        .faultString(FAULT_STRING);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), SendSoapFaultAction.class);

        SendSoapFaultAction action = (SendSoapFaultAction) ((DelegatingTestAction)test.getActions().get(0)).getDelegate();
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), soapServer);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageContentBuilder.class);

        StaticMessageContentBuilder messageBuilder = (StaticMessageContentBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(), "");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        Assert.assertEquals(action.getFaultDetails().size(), 0L);
        Assert.assertEquals(action.getFaultDetailResourcePaths().size(), 1L);
        Assert.assertEquals(action.getFaultDetailResourcePaths().get(0), "com/consol/citrus/soap/fault.xml");
        Assert.assertEquals(action.getFaultCode(), FAULT_CODE);
        Assert.assertEquals(action.getFaultString(), FAULT_STRING);
    }

    @Test
    public void testSendSoapFaultWithMultipleDetail() {
        MockTestDesigner builder = new MockTestDesigner(applicationContext, context) {
            @Override
            public void configure() {
                soap().server(soapServer)
                        .sendFault()
                        .faultCode(FAULT_CODE)
                        .faultDetail("DETAIL1")
                        .faultDetail("DETAIL2")
                        .faultString(FAULT_STRING);
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);
        Assert.assertEquals(((DelegatingTestAction)test.getActions().get(0)).getDelegate().getClass(), SendSoapFaultAction.class);

        SendSoapFaultAction action = (SendSoapFaultAction) ((DelegatingTestAction)test.getActions().get(0)).getDelegate();
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), soapServer);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageContentBuilder.class);

        StaticMessageContentBuilder messageBuilder = (StaticMessageContentBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(), "");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        Assert.assertEquals(action.getFaultDetails().size(), 2L);
        Assert.assertEquals(action.getFaultDetails().get(0), "DETAIL1");
        Assert.assertEquals(action.getFaultDetails().get(1), "DETAIL2");
        Assert.assertEquals(action.getFaultCode(), FAULT_CODE);
        Assert.assertEquals(action.getFaultString(), FAULT_STRING);
    }
}
