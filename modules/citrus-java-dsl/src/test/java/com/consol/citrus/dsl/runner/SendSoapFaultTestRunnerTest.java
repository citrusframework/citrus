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

package com.consol.citrus.dsl.runner;

import com.consol.citrus.TestCase;
import com.consol.citrus.container.SequenceAfterTest;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.builder.BuilderSupport;
import com.consol.citrus.dsl.builder.SendSoapFaultBuilder;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.message.Message;
import com.consol.citrus.messaging.Producer;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.ws.actions.SendSoapFaultAction;
import com.consol.citrus.ws.message.SoapFault;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
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
public class SendSoapFaultTestRunnerTest extends AbstractTestNGUnitTest {

    public static final String FAULT_STRING = "Something went wrong";
    public static final String FAULT_CODE = "CITRUS-1000";
    public static final String ERROR_DETAIL = "<ErrorDetail><message>Something went wrong</message></ErrorDetail>";

    private Endpoint soapEndpoint = EasyMock.createMock(Endpoint.class);
    private Producer messageProducer = EasyMock.createMock(Producer.class);
    private ApplicationContext applicationContextMock = EasyMock.createMock(ApplicationContext.class);
    private Resource resource = EasyMock.createMock(Resource.class);

    @Test
    public void testSendSoapFault() {
        reset(soapEndpoint, messageProducer);
        expect(soapEndpoint.createProducer()).andReturn(messageProducer).once();
        expect(soapEndpoint.getActor()).andReturn(null).atLeastOnce();
        messageProducer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                SoapFault message = (SoapFault) getCurrentArguments()[0];
                Assert.assertEquals(message.getFaultActor(), "faultActor");
                Assert.assertEquals(message.getFaultCode(), FAULT_CODE);
                Assert.assertEquals(message.getFaultString(), FAULT_STRING);
                return null;
            }
        }).atLeastOnce();
        replay(soapEndpoint, messageProducer);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                sendSoapFault(new BuilderSupport<SendSoapFaultBuilder>() {
                    @Override
                    public void configure(SendSoapFaultBuilder builder) {
                        builder.endpoint(soapEndpoint)
                                .faultActor("faultActor")
                                .faultCode(FAULT_CODE)
                                .faultString(FAULT_STRING);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendSoapFaultAction.class);

        SendSoapFaultAction action = ((SendSoapFaultAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), soapEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertNull(messageBuilder.getPayloadData());
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        Assert.assertEquals(action.getFaultActor(), "faultActor");
        Assert.assertEquals(action.getFaultCode(), FAULT_CODE);
        Assert.assertEquals(action.getFaultString(), FAULT_STRING);

        verify(soapEndpoint, messageProducer);
    }

    @Test
    public void testSendSoapFaultByEndpointName() {
        reset(applicationContextMock, soapEndpoint, messageProducer);
        expect(soapEndpoint.createProducer()).andReturn(messageProducer).once();
        expect(soapEndpoint.getActor()).andReturn(null).atLeastOnce();
        messageProducer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                SoapFault message = (SoapFault) getCurrentArguments()[0];
                Assert.assertEquals(message.getFaultCode(), FAULT_CODE);
                Assert.assertEquals(message.getFaultString(), FAULT_STRING);
                return null;
            }
        }).atLeastOnce();

        expect(applicationContextMock.getBean(TestContext.class)).andReturn(applicationContext.getBean(TestContext.class)).once();
        expect(applicationContextMock.getBean("soapEndpoint", Endpoint.class)).andReturn(soapEndpoint).atLeastOnce();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();
        replay(applicationContextMock, soapEndpoint, messageProducer);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock) {
            @Override
            public void execute() {
                sendSoapFault(new BuilderSupport<SendSoapFaultBuilder>() {
                    @Override
                    public void configure(SendSoapFaultBuilder builder) {
                        builder.endpoint("soapEndpoint")
                                .faultCode(FAULT_CODE)
                                .faultString(FAULT_STRING);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendSoapFaultAction.class);

        SendSoapFaultAction action = ((SendSoapFaultAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpointUri(), "soapEndpoint");
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertNull(messageBuilder.getPayloadData());
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        Assert.assertNull(action.getFaultActor());
        Assert.assertEquals(action.getFaultCode(), FAULT_CODE);
        Assert.assertEquals(action.getFaultString(), FAULT_STRING);

        verify(applicationContextMock, soapEndpoint, messageProducer);
    }

    @Test
    public void testSendSoapFaultWithDetailResource() throws IOException {
        reset(resource, soapEndpoint, messageProducer);
        expect(soapEndpoint.createProducer()).andReturn(messageProducer).once();
        expect(soapEndpoint.getActor()).andReturn(null).atLeastOnce();
        messageProducer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                SoapFault message = (SoapFault) getCurrentArguments()[0];
                Assert.assertEquals(message.getFaultCode(), FAULT_CODE);
                Assert.assertEquals(message.getFaultString(), FAULT_STRING);
                Assert.assertEquals(message.getFaultDetails().size(), 1L);
                Assert.assertEquals(message.getFaultDetails().get(0), ERROR_DETAIL);
                return null;
            }
        }).atLeastOnce();

        expect(resource.getInputStream()).andReturn(new ByteArrayInputStream(ERROR_DETAIL.getBytes())).once();
        replay(resource, soapEndpoint, messageProducer);


        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                sendSoapFault(new BuilderSupport<SendSoapFaultBuilder>() {
                    @Override
                    public void configure(SendSoapFaultBuilder builder) {
                        builder.endpoint(soapEndpoint)
                                .faultCode(FAULT_CODE)
                                .faultDetailResource(resource)
                                .faultString(FAULT_STRING);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendSoapFaultAction.class);

        SendSoapFaultAction action = ((SendSoapFaultAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), soapEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertNull(messageBuilder.getPayloadData());
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        Assert.assertEquals(action.getFaultDetails().size(), 1L);
        Assert.assertEquals(action.getFaultDetails().get(0), ERROR_DETAIL);
        Assert.assertEquals(action.getFaultCode(), FAULT_CODE);
        Assert.assertEquals(action.getFaultString(), FAULT_STRING);

        verify(resource, soapEndpoint, messageProducer);

    }

    @Test
    public void testSendSoapFaultWithDetail() {
        reset(soapEndpoint, messageProducer);
        expect(soapEndpoint.createProducer()).andReturn(messageProducer).once();
        expect(soapEndpoint.getActor()).andReturn(null).atLeastOnce();
        messageProducer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                SoapFault message = (SoapFault) getCurrentArguments()[0];
                Assert.assertEquals(message.getFaultCode(), FAULT_CODE);
                Assert.assertEquals(message.getFaultString(), FAULT_STRING);
                Assert.assertEquals(message.getFaultDetails().size(), 1L);
                Assert.assertEquals(message.getFaultDetails().get(0), "DETAIL");
                return null;
            }
        }).atLeastOnce();
        replay(soapEndpoint, messageProducer);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                sendSoapFault(new BuilderSupport<SendSoapFaultBuilder>() {
                    @Override
                    public void configure(SendSoapFaultBuilder builder) {
                        builder.endpoint(soapEndpoint)
                                .faultCode(FAULT_CODE)
                                .faultDetail("DETAIL")
                                .faultString(FAULT_STRING);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendSoapFaultAction.class);

        SendSoapFaultAction action = ((SendSoapFaultAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), soapEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertNull(messageBuilder.getPayloadData());
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        Assert.assertEquals(action.getFaultDetails().size(), 1L);
        Assert.assertEquals(action.getFaultDetails().get(0), "DETAIL");
        Assert.assertEquals(action.getFaultCode(), FAULT_CODE);
        Assert.assertEquals(action.getFaultString(), FAULT_STRING);

        verify(soapEndpoint, messageProducer);
    }

    @Test
    public void testSendSoapFaultWithDetailResourcePath() {
        reset(soapEndpoint, messageProducer);
        expect(soapEndpoint.createProducer()).andReturn(messageProducer).once();
        expect(soapEndpoint.getActor()).andReturn(null).atLeastOnce();
        messageProducer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                SoapFault message = (SoapFault) getCurrentArguments()[0];
                Assert.assertEquals(message.getFaultCode(), FAULT_CODE);
                Assert.assertEquals(message.getFaultString(), FAULT_STRING);
                Assert.assertEquals(message.getFaultDetails().size(), 1L);
                Assert.assertEquals(message.getFaultDetails().get(0), ERROR_DETAIL);
                return null;
            }
        }).atLeastOnce();
        replay(soapEndpoint, messageProducer);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                sendSoapFault(new BuilderSupport<SendSoapFaultBuilder>() {
                    @Override
                    public void configure(SendSoapFaultBuilder builder) {
                        builder.endpoint(soapEndpoint)
                                .faultCode(FAULT_CODE)
                                .faultDetailResource("classpath:com/consol/citrus/dsl/runner/soap-fault-detail.xml")
                                .faultString(FAULT_STRING);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendSoapFaultAction.class);

        SendSoapFaultAction action = ((SendSoapFaultAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), soapEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertNull(messageBuilder.getPayloadData());
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        Assert.assertEquals(action.getFaultDetails().size(), 0L);
        Assert.assertEquals(action.getFaultDetailResourcePaths().size(), 1L);
        Assert.assertEquals(action.getFaultDetailResourcePaths().get(0), "classpath:com/consol/citrus/dsl/runner/soap-fault-detail.xml");
        Assert.assertEquals(action.getFaultCode(), FAULT_CODE);
        Assert.assertEquals(action.getFaultString(), FAULT_STRING);

        verify(soapEndpoint, messageProducer);
    }

    @Test
    public void testSendSoapFaultWithMultipleDetail() {
        reset(soapEndpoint, messageProducer);
        expect(soapEndpoint.createProducer()).andReturn(messageProducer).once();
        expect(soapEndpoint.getActor()).andReturn(null).atLeastOnce();
        messageProducer.send(anyObject(Message.class), anyObject(TestContext.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() throws Throwable {
                SoapFault message = (SoapFault) getCurrentArguments()[0];
                Assert.assertEquals(message.getFaultCode(), FAULT_CODE);
                Assert.assertEquals(message.getFaultString(), FAULT_STRING);
                Assert.assertEquals(message.getFaultDetails().size(), 2L);
                Assert.assertEquals(message.getFaultDetails().get(0), "DETAIL1");
                Assert.assertEquals(message.getFaultDetails().get(1), "DETAIL2");
                return null;
            }
        }).atLeastOnce();
        replay(soapEndpoint, messageProducer);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext) {
            @Override
            public void execute() {
                sendSoapFault(new BuilderSupport<SendSoapFaultBuilder>() {
                    @Override
                    public void configure(SendSoapFaultBuilder builder) {
                        builder.endpoint(soapEndpoint)
                                .faultCode(FAULT_CODE)
                                .faultDetail("DETAIL1")
                                .faultDetail("DETAIL2")
                                .faultString(FAULT_STRING);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendSoapFaultAction.class);

        SendSoapFaultAction action = ((SendSoapFaultAction)test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), soapEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertNull(messageBuilder.getPayloadData());
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        Assert.assertEquals(action.getFaultDetails().size(), 2L);
        Assert.assertEquals(action.getFaultDetails().get(0), "DETAIL1");
        Assert.assertEquals(action.getFaultDetails().get(1), "DETAIL2");
        Assert.assertEquals(action.getFaultCode(), FAULT_CODE);
        Assert.assertEquals(action.getFaultString(), FAULT_STRING);

        verify(soapEndpoint, messageProducer);
    }
}
