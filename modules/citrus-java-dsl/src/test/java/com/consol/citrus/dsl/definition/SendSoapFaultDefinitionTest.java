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

package com.consol.citrus.dsl.definition;

import com.consol.citrus.CitrusConstants;
import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.resolver.EndpointResolver;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.report.TestListeners;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.ws.message.builder.SoapFaultAwareMessageBuilder;
import org.easymock.EasyMock;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class SendSoapFaultDefinitionTest extends AbstractTestNGUnitTest {

    private Endpoint soapEndpoint = EasyMock.createMock(Endpoint.class);
    private ApplicationContext applicationContextMock = EasyMock.createMock(ApplicationContext.class);
    private EndpointResolver endpointResolver = EasyMock.createMock(EndpointResolver.class);
    private Resource resource = EasyMock.createMock(Resource.class);

    @Test
    public void testSendSoapFault() {
        MockBuilder builder = new MockBuilder(applicationContext) {
            @Override
            public void configure() {
                sendSoapFault(soapEndpoint)
                        .faultActor("faultActor")
                        .faultCode("CITRUS-1000")
                        .faultString("Something went wrong");
            }
        };

        builder.execute();

        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), SendMessageAction.class);

        SendMessageAction action = ((SendMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), soapEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), SoapFaultAwareMessageBuilder.class);

        SoapFaultAwareMessageBuilder messageBuilder = (SoapFaultAwareMessageBuilder) action.getMessageBuilder();
        Assert.assertNull(messageBuilder.getPayloadData());
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        Assert.assertEquals(messageBuilder.getFaultActor(), "faultActor");
        Assert.assertEquals(messageBuilder.getFaultCode(), "CITRUS-1000");
        Assert.assertEquals(messageBuilder.getFaultString(), "Something went wrong");
    }

    @Test
    public void testSendSoapFaultByEndpointName() {
        reset(applicationContextMock, endpointResolver);

        expect(applicationContextMock.getBean(CitrusConstants.ENDPOINT_RESOLVER_BEAN, EndpointResolver.class)).andReturn(endpointResolver).once();
        expect(endpointResolver.resolve("soapEndpoint", applicationContextMock)).andReturn(soapEndpoint).once();
        expect(applicationContextMock.getBean(TestListeners.class)).andReturn(new TestListeners()).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();

        replay(applicationContextMock, endpointResolver);

        MockBuilder builder = new MockBuilder(applicationContextMock) {
            @Override
            public void configure() {
                sendSoapFault("soapEndpoint")
                        .faultCode("CITRUS-1000")
                        .faultString("Something went wrong");
            }
        };

        builder.execute();

        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), SendMessageAction.class);

        SendMessageAction action = ((SendMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), soapEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), SoapFaultAwareMessageBuilder.class);

        SoapFaultAwareMessageBuilder messageBuilder = (SoapFaultAwareMessageBuilder) action.getMessageBuilder();
        Assert.assertNull(messageBuilder.getPayloadData());
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        Assert.assertNull(messageBuilder.getFaultActor());
        Assert.assertEquals(messageBuilder.getFaultCode(), "CITRUS-1000");
        Assert.assertEquals(messageBuilder.getFaultString(), "Something went wrong");

        verify(applicationContextMock, endpointResolver);
    }

    @Test
    public void testSendSoapFaultWithDetailResource() throws IOException {
        MockBuilder builder = new MockBuilder(applicationContext) {
            @Override
            public void configure() {
                sendSoapFault(soapEndpoint)
                        .faultCode("CITRUS-1000")
                        .faultDetailResource(resource)
                        .faultString("Something went wrong");
            }
        };

        reset(resource);
        expect(resource.getInputStream()).andReturn(new ByteArrayInputStream("someDetailData".getBytes())).once();
        replay(resource);

        builder.execute();

        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), SendMessageAction.class);

        SendMessageAction action = ((SendMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), soapEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), SoapFaultAwareMessageBuilder.class);

        SoapFaultAwareMessageBuilder messageBuilder = (SoapFaultAwareMessageBuilder) action.getMessageBuilder();
        Assert.assertNull(messageBuilder.getPayloadData());
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        Assert.assertEquals(messageBuilder.getFaultDetails().size(), 1L);
        Assert.assertEquals(messageBuilder.getFaultDetails().get(0), "someDetailData");
        Assert.assertEquals(messageBuilder.getFaultCode(), "CITRUS-1000");
        Assert.assertEquals(messageBuilder.getFaultString(), "Something went wrong");
    }

    @Test
    public void testSendSoapFaultWithDetail() {
        MockBuilder builder = new MockBuilder(applicationContext) {
            @Override
            public void configure() {
                sendSoapFault(soapEndpoint)
                        .faultCode("CITRUS-1000")
                        .faultDetail("DETAIL")
                        .faultString("Something went wrong");
            }
        };

        builder.execute();

        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), SendMessageAction.class);

        SendMessageAction action = ((SendMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), soapEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), SoapFaultAwareMessageBuilder.class);

        SoapFaultAwareMessageBuilder messageBuilder = (SoapFaultAwareMessageBuilder) action.getMessageBuilder();
        Assert.assertNull(messageBuilder.getPayloadData());
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        Assert.assertEquals(messageBuilder.getFaultDetails().size(), 1L);
        Assert.assertEquals(messageBuilder.getFaultDetails().get(0), "DETAIL");
        Assert.assertEquals(messageBuilder.getFaultCode(), "CITRUS-1000");
        Assert.assertEquals(messageBuilder.getFaultString(), "Something went wrong");
    }

    @Test
    public void testSendSoapFaultWithMultipleDetail() {
        MockBuilder builder = new MockBuilder(applicationContext) {
            @Override
            public void configure() {
                sendSoapFault(soapEndpoint)
                        .faultCode("CITRUS-1000")
                        .faultDetail("DETAIL1")
                        .faultDetail("DETAIL2")
                        .faultString("Something went wrong");
            }
        };

        builder.execute();

        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), SendMessageAction.class);

        SendMessageAction action = ((SendMessageAction)builder.testCase().getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), soapEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), SoapFaultAwareMessageBuilder.class);

        SoapFaultAwareMessageBuilder messageBuilder = (SoapFaultAwareMessageBuilder) action.getMessageBuilder();
        Assert.assertNull(messageBuilder.getPayloadData());
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        Assert.assertEquals(messageBuilder.getFaultDetails().size(), 2L);
        Assert.assertEquals(messageBuilder.getFaultDetails().get(0), "DETAIL1");
        Assert.assertEquals(messageBuilder.getFaultDetails().get(1), "DETAIL2");
        Assert.assertEquals(messageBuilder.getFaultCode(), "CITRUS-1000");
        Assert.assertEquals(messageBuilder.getFaultString(), "Something went wrong");
    }
}
