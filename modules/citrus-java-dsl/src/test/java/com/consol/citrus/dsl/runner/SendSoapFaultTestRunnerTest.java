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
import com.consol.citrus.dsl.actions.DelegatingTestAction;
import com.consol.citrus.dsl.builder.*;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.message.Message;
import com.consol.citrus.messaging.Producer;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.builder.StaticMessageContentBuilder;
import com.consol.citrus.ws.actions.SendSoapFaultAction;
import com.consol.citrus.ws.message.SoapFault;
import com.consol.citrus.ws.server.WebServiceServer;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 */
public class SendSoapFaultTestRunnerTest extends AbstractTestNGUnitTest {

    public static final String FAULT_STRING = "Something went wrong";
    public static final String FAULT_CODE = "CITRUS-1000";
    public static final String ERROR_DETAIL = "<ErrorDetail><message>Something went wrong</message></ErrorDetail>";

    private WebServiceServer soapServer = Mockito.mock(WebServiceServer.class);
    private Producer messageProducer = Mockito.mock(Producer.class);
    private ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);
    private Resource resource = Mockito.mock(Resource.class);

    @Test
    public void testSendSoapFault() {
        reset(soapServer, messageProducer);
        when(soapServer.createProducer()).thenReturn(messageProducer);
        when(soapServer.getActor()).thenReturn(null);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                SoapFault message = (SoapFault) invocation.getArguments()[0];
                Assert.assertEquals(message.getFaultActor(), "faultActor");
                Assert.assertEquals(message.getFaultCode(), FAULT_CODE);
                Assert.assertEquals(message.getFaultString(), FAULT_STRING);
                return null;
            }
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                soap(new BuilderSupport<SoapActionBuilder>() {
                    @Override
                    public void configure(SoapActionBuilder builder) {
                        builder.server(soapServer)
                                .sendFault()
                                .faultActor("faultActor")
                                .faultCode(FAULT_CODE)
                                .faultString(FAULT_STRING);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);

        SendSoapFaultAction action = (SendSoapFaultAction)((DelegatingTestAction)test.getActions().get(0)).getDelegate();
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
    public void testSendSoapFaultDeprecated() {
        reset(soapServer, messageProducer);
        when(soapServer.createProducer()).thenReturn(messageProducer);
        when(soapServer.getActor()).thenReturn(null);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                SoapFault message = (SoapFault) invocation.getArguments()[0];
                Assert.assertEquals(message.getFaultActor(), "faultActor");
                Assert.assertEquals(message.getFaultCode(), FAULT_CODE);
                Assert.assertEquals(message.getFaultString(), FAULT_STRING);
                return null;
            }
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                sendSoapFault(new BuilderSupport<SendSoapFaultBuilder>() {
                    @Override
                    public void configure(SendSoapFaultBuilder builder) {
                        builder.endpoint(soapServer)
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

        Assert.assertEquals(action.getEndpoint(), soapServer);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertNull(messageBuilder.getPayloadData());
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        Assert.assertEquals(action.getFaultActor(), "faultActor");
        Assert.assertEquals(action.getFaultCode(), FAULT_CODE);
        Assert.assertEquals(action.getFaultString(), FAULT_STRING);
    }

    @Test
    public void testSendSoapFaultByEndpointName() {
        TestContext context = applicationContext.getBean(TestContext.class);
        context.setApplicationContext(applicationContextMock);

        reset(applicationContextMock, soapServer, messageProducer);
        when(soapServer.createProducer()).thenReturn(messageProducer);
        when(soapServer.getActor()).thenReturn(null);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                SoapFault message = (SoapFault) invocation.getArguments()[0];
                Assert.assertEquals(message.getFaultCode(), FAULT_CODE);
                Assert.assertEquals(message.getFaultString(), FAULT_STRING);
                return null;
            }
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        when(applicationContextMock.getBean(TestContext.class)).thenReturn(context);
        when(applicationContextMock.getBean("soapServer", Endpoint.class)).thenReturn(soapServer);
        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock, context) {
            @Override
            public void execute() {
                soap(new BuilderSupport<SoapActionBuilder>() {
                    @Override
                    public void configure(SoapActionBuilder builder) {
                        builder.server("soapServer")
                                .sendFault()
                                .faultCode(FAULT_CODE)
                                .faultString(FAULT_STRING);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);

        SendSoapFaultAction action = (SendSoapFaultAction)((DelegatingTestAction)test.getActions().get(0)).getDelegate();
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
    public void testSendSoapFaultByEndpointNameDeprecated() {
        TestContext context = applicationContext.getBean(TestContext.class);
        context.setApplicationContext(applicationContextMock);

        reset(applicationContextMock, soapServer, messageProducer);
        when(soapServer.createProducer()).thenReturn(messageProducer);
        when(soapServer.getActor()).thenReturn(null);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                SoapFault message = (SoapFault) invocation.getArguments()[0];
                Assert.assertEquals(message.getFaultCode(), FAULT_CODE);
                Assert.assertEquals(message.getFaultString(), FAULT_STRING);
                return null;
            }
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        when(applicationContextMock.getBean(TestContext.class)).thenReturn(context);
        when(applicationContextMock.getBean("soapServer", Endpoint.class)).thenReturn(soapServer);
        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock, context) {
            @Override
            public void execute() {
                sendSoapFault(new BuilderSupport<SendSoapFaultBuilder>() {
                    @Override
                    public void configure(SendSoapFaultBuilder builder) {
                        builder.endpoint("soapServer")
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

        Assert.assertEquals(action.getEndpointUri(), "soapServer");
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertNull(messageBuilder.getPayloadData());
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        Assert.assertNull(action.getFaultActor());
        Assert.assertEquals(action.getFaultCode(), FAULT_CODE);
        Assert.assertEquals(action.getFaultString(), FAULT_STRING);
    }

    @Test
    public void testSendSoapFaultWithDetailResource() throws IOException {
        reset(resource, soapServer, messageProducer);
        when(soapServer.createProducer()).thenReturn(messageProducer);
        when(soapServer.getActor()).thenReturn(null);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                SoapFault message = (SoapFault) invocation.getArguments()[0];
                Assert.assertEquals(message.getFaultCode(), FAULT_CODE);
                Assert.assertEquals(message.getFaultString(), FAULT_STRING);
                Assert.assertEquals(message.getFaultDetails().size(), 1L);
                Assert.assertEquals(message.getFaultDetails().get(0), ERROR_DETAIL);
                return null;
            }
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(ERROR_DETAIL.getBytes()));

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                soap(new BuilderSupport<SoapActionBuilder>() {
                    @Override
                    public void configure(SoapActionBuilder builder) {
                        builder.server(soapServer)
                                .sendFault()
                                .faultCode(FAULT_CODE)
                                .faultDetailResource(resource)
                                .faultString(FAULT_STRING);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);

        SendSoapFaultAction action = (SendSoapFaultAction)((DelegatingTestAction)test.getActions().get(0)).getDelegate();
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), soapServer);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageContentBuilder.class);

        StaticMessageContentBuilder messageBuilder = (StaticMessageContentBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(), "");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        Assert.assertEquals(action.getFaultDetails().size(), 1L);
        Assert.assertEquals(action.getFaultDetails().get(0), ERROR_DETAIL);
        Assert.assertEquals(action.getFaultCode(), FAULT_CODE);
        Assert.assertEquals(action.getFaultString(), FAULT_STRING);
    }

    @Test
    public void testSendSoapFaultWithDetailResourceDeprecated() throws IOException {
        reset(resource, soapServer, messageProducer);
        when(soapServer.createProducer()).thenReturn(messageProducer);
        when(soapServer.getActor()).thenReturn(null);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                SoapFault message = (SoapFault) invocation.getArguments()[0];
                Assert.assertEquals(message.getFaultCode(), FAULT_CODE);
                Assert.assertEquals(message.getFaultString(), FAULT_STRING);
                Assert.assertEquals(message.getFaultDetails().size(), 1L);
                Assert.assertEquals(message.getFaultDetails().get(0), ERROR_DETAIL);
                return null;
            }
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream(ERROR_DETAIL.getBytes()));

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                sendSoapFault(new BuilderSupport<SendSoapFaultBuilder>() {
                    @Override
                    public void configure(SendSoapFaultBuilder builder) {
                        builder.endpoint(soapServer)
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

        Assert.assertEquals(action.getEndpoint(), soapServer);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertNull(messageBuilder.getPayloadData());
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
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
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                SoapFault message = (SoapFault) invocation.getArguments()[0];
                Assert.assertEquals(message.getFaultCode(), FAULT_CODE);
                Assert.assertEquals(message.getFaultString(), FAULT_STRING);
                Assert.assertEquals(message.getFaultDetails().size(), 1L);
                Assert.assertEquals(message.getFaultDetails().get(0), "DETAIL");
                return null;
            }
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                soap(new BuilderSupport<SoapActionBuilder>() {
                    @Override
                    public void configure(SoapActionBuilder builder) {
                        builder.server(soapServer)
                                .sendFault()
                                .faultCode(FAULT_CODE)
                                .faultDetail("DETAIL")
                                .faultString(FAULT_STRING);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);

        SendSoapFaultAction action = (SendSoapFaultAction)((DelegatingTestAction)test.getActions().get(0)).getDelegate();
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
    public void testSendSoapFaultWithDetailDeprecated() {
        reset(soapServer, messageProducer);
        when(soapServer.createProducer()).thenReturn(messageProducer);
        when(soapServer.getActor()).thenReturn(null);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                SoapFault message = (SoapFault) invocation.getArguments()[0];
                Assert.assertEquals(message.getFaultCode(), FAULT_CODE);
                Assert.assertEquals(message.getFaultString(), FAULT_STRING);
                Assert.assertEquals(message.getFaultDetails().size(), 1L);
                Assert.assertEquals(message.getFaultDetails().get(0), "DETAIL");
                return null;
            }
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                soap(new BuilderSupport<SoapActionBuilder>() {
                    @Override
                    public void configure(SoapActionBuilder builder) {
                        builder.server(soapServer)
                                .sendFault()
                                .faultCode(FAULT_CODE)
                                .faultDetail("DETAIL")
                                .faultString(FAULT_STRING);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);

        SendSoapFaultAction action = (SendSoapFaultAction)((DelegatingTestAction)test.getActions().get(0)).getDelegate();
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
        reset(soapServer, messageProducer);
        when(soapServer.createProducer()).thenReturn(messageProducer);
        when(soapServer.getActor()).thenReturn(null);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                SoapFault message = (SoapFault) invocation.getArguments()[0];
                Assert.assertEquals(message.getFaultCode(), FAULT_CODE);
                Assert.assertEquals(message.getFaultString(), FAULT_STRING);
                Assert.assertEquals(message.getFaultDetails().size(), 1L);
                Assert.assertEquals(message.getFaultDetails().get(0), ERROR_DETAIL);
                return null;
            }
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                soap(new BuilderSupport<SoapActionBuilder>() {
                    @Override
                    public void configure(SoapActionBuilder builder) {
                        builder.server(soapServer)
                                .sendFault()
                                .faultCode(FAULT_CODE)
                                .faultDetailResource("classpath:com/consol/citrus/dsl/runner/soap-fault-detail.xml")
                                .faultString(FAULT_STRING);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);

        SendSoapFaultAction action = (SendSoapFaultAction)((DelegatingTestAction)test.getActions().get(0)).getDelegate();
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), soapServer);
        Assert.assertEquals(action.getMessageBuilder().getClass(), StaticMessageContentBuilder.class);

        StaticMessageContentBuilder messageBuilder = (StaticMessageContentBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.getMessage().getPayload(), "");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        Assert.assertEquals(action.getFaultDetails().size(), 0L);
        Assert.assertEquals(action.getFaultDetailResourcePaths().size(), 1L);
        Assert.assertEquals(action.getFaultDetailResourcePaths().get(0), "classpath:com/consol/citrus/dsl/runner/soap-fault-detail.xml");
        Assert.assertEquals(action.getFaultCode(), FAULT_CODE);
        Assert.assertEquals(action.getFaultString(), FAULT_STRING);
    }

    @Test
    public void testSendSoapFaultWithDetailResourcePathDeprecated() {
        reset(soapServer, messageProducer);
        when(soapServer.createProducer()).thenReturn(messageProducer);
        when(soapServer.getActor()).thenReturn(null);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                SoapFault message = (SoapFault) invocation.getArguments()[0];
                Assert.assertEquals(message.getFaultCode(), FAULT_CODE);
                Assert.assertEquals(message.getFaultString(), FAULT_STRING);
                Assert.assertEquals(message.getFaultDetails().size(), 1L);
                Assert.assertEquals(message.getFaultDetails().get(0), ERROR_DETAIL);
                return null;
            }
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                sendSoapFault(new BuilderSupport<SendSoapFaultBuilder>() {
                    @Override
                    public void configure(SendSoapFaultBuilder builder) {
                        builder.endpoint(soapServer)
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

        Assert.assertEquals(action.getEndpoint(), soapServer);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertNull(messageBuilder.getPayloadData());
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        Assert.assertEquals(action.getFaultDetails().size(), 0L);
        Assert.assertEquals(action.getFaultDetailResourcePaths().size(), 1L);
        Assert.assertEquals(action.getFaultDetailResourcePaths().get(0), "classpath:com/consol/citrus/dsl/runner/soap-fault-detail.xml");
        Assert.assertEquals(action.getFaultCode(), FAULT_CODE);
        Assert.assertEquals(action.getFaultString(), FAULT_STRING);
    }

    @Test
    public void testSendSoapFaultWithMultipleDetail() {
        reset(soapServer, messageProducer);
        when(soapServer.createProducer()).thenReturn(messageProducer);
        when(soapServer.getActor()).thenReturn(null);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                SoapFault message = (SoapFault) invocation.getArguments()[0];
                Assert.assertEquals(message.getFaultCode(), FAULT_CODE);
                Assert.assertEquals(message.getFaultString(), FAULT_STRING);
                Assert.assertEquals(message.getFaultDetails().size(), 2L);
                Assert.assertEquals(message.getFaultDetails().get(0), "DETAIL1");
                Assert.assertEquals(message.getFaultDetails().get(1), "DETAIL2");
                return null;
            }
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                soap(new BuilderSupport<SoapActionBuilder>() {
                    @Override
                    public void configure(SoapActionBuilder builder) {
                        builder.server(soapServer)
                                .sendFault()
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
        Assert.assertEquals(test.getActions().get(0).getClass(), DelegatingTestAction.class);

        SendSoapFaultAction action = (SendSoapFaultAction)((DelegatingTestAction)test.getActions().get(0)).getDelegate();
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

    @Test
    public void testSendSoapFaultWithMultipleDetailDeprecated() {
        reset(soapServer, messageProducer);
        when(soapServer.createProducer()).thenReturn(messageProducer);
        when(soapServer.getActor()).thenReturn(null);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                SoapFault message = (SoapFault) invocation.getArguments()[0];
                Assert.assertEquals(message.getFaultCode(), FAULT_CODE);
                Assert.assertEquals(message.getFaultString(), FAULT_STRING);
                Assert.assertEquals(message.getFaultDetails().size(), 2L);
                Assert.assertEquals(message.getFaultDetails().get(0), "DETAIL1");
                Assert.assertEquals(message.getFaultDetails().get(1), "DETAIL2");
                return null;
            }
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                sendSoapFault(new BuilderSupport<SendSoapFaultBuilder>() {
                    @Override
                    public void configure(SendSoapFaultBuilder builder) {
                        builder.endpoint(soapServer)
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

        Assert.assertEquals(action.getEndpoint(), soapServer);
        Assert.assertEquals(action.getMessageBuilder().getClass(), PayloadTemplateMessageBuilder.class);

        PayloadTemplateMessageBuilder messageBuilder = (PayloadTemplateMessageBuilder) action.getMessageBuilder();
        Assert.assertNull(messageBuilder.getPayloadData());
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0L);
        Assert.assertEquals(action.getFaultDetails().size(), 2L);
        Assert.assertEquals(action.getFaultDetails().get(0), "DETAIL1");
        Assert.assertEquals(action.getFaultDetails().get(1), "DETAIL2");
        Assert.assertEquals(action.getFaultCode(), FAULT_CODE);
        Assert.assertEquals(action.getFaultString(), FAULT_STRING);
    }
}
