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
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.container.SequenceAfterTest;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.ws.actions.AssertSoapFault;
import com.consol.citrus.ws.validation.SoapFaultValidator;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.ws.soap.*;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;
import org.springframework.xml.transform.StringSource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

import static org.mockito.Mockito.*;

public class AssertSoapFaultTestRunnerTest extends AbstractTestNGUnitTest {

    public static final String INTERNAL_SERVER_ERROR = "Internal server error";
    public static final String SOAP_FAULT_VALIDATOR = "soapFaultValidator";

    private Resource resource = Mockito.mock(Resource.class);
    private SoapFaultValidator soapFaultValidator = Mockito.mock(SoapFaultValidator.class);
    private ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    private SoapMessage soapMessage = Mockito.mock(org.springframework.ws.soap.SoapMessage.class);
    private SoapBody soapBody = Mockito.mock(SoapBody.class);
    private SoapFault soapFault = Mockito.mock(SoapFault.class);
    private SoapFaultDetail soapFaultDetail = Mockito.mock(SoapFaultDetail.class);
    private SoapFaultDetailElement soapFaultDetailElement = Mockito.mock(SoapFaultDetailElement.class);

    @Test
    public void testAssertSoapFaultBuilder() {
        reset(applicationContextMock, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);

        when(soapMessage.getSoapBody()).thenReturn(soapBody);
        when(soapMessage.getFaultReason()).thenReturn(INTERNAL_SERVER_ERROR);
        when(soapBody.getFault()).thenReturn(soapFault);

        when(soapFault.getFaultActorOrRole()).thenReturn(SoapFaultDefinition.SERVER.getLocalPart());
        when(soapFault.getFaultCode()).thenReturn(SoapFaultDefinition.SERVER);
        when(soapFault.getFaultStringOrReason()).thenReturn(INTERNAL_SERVER_ERROR);
        when(soapFault.getFaultDetail()).thenReturn(null);

        when(applicationContextMock.getBean(TestContext.class)).thenReturn(applicationContext.getBean(TestContext.class));
        when(applicationContextMock.containsBean(SOAP_FAULT_VALIDATOR)).thenReturn(false);
        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock, context) {
            @Override
            public void execute() {
                assertSoapFault().faultCode(SoapFaultDefinition.SERVER.getLocalPart())
                                .faultString(INTERNAL_SERVER_ERROR)
                        .when(new AbstractTestAction() {
                            @Override
                            public void doExecute(TestContext context) {
                                throw new SoapFaultClientException(soapMessage);
                            }
                        });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "soap-fault");
        
        AssertSoapFault container = (AssertSoapFault)(test.getTestAction(0));
        
        Assert.assertEquals(container.getActionCount(), 1);
        Assert.assertTrue(container.getAction().getClass().isAnonymousClass());
        Assert.assertEquals(container.getFaultCode(), SoapFaultDefinition.SERVER.getLocalPart());
        Assert.assertEquals(container.getFaultString(), INTERNAL_SERVER_ERROR);

    }

    @Test
    public void testAssertSoapFaultDefaultValidatorBuilder() {
        reset(applicationContextMock, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);

        when(soapMessage.getSoapBody()).thenReturn(soapBody);
        when(soapMessage.getFaultReason()).thenReturn(INTERNAL_SERVER_ERROR);
        when(soapBody.getFault()).thenReturn(soapFault);

        when(soapFault.getFaultActorOrRole()).thenReturn(SoapFaultDefinition.SERVER.getLocalPart());
        when(soapFault.getFaultCode()).thenReturn(SoapFaultDefinition.SERVER);
        when(soapFault.getFaultStringOrReason()).thenReturn(INTERNAL_SERVER_ERROR);
        when(soapFault.getFaultDetail()).thenReturn(null);

        when(applicationContextMock.getBean(TestContext.class)).thenReturn(applicationContext.getBean(TestContext.class));
        when(applicationContextMock.containsBean(SOAP_FAULT_VALIDATOR)).thenReturn(true);
        when(applicationContextMock.getBean(SOAP_FAULT_VALIDATOR, SoapFaultValidator.class)).thenReturn(soapFaultValidator);
        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock, context) {
            @Override
            public void execute() {
                assertSoapFault().faultCode(SoapFaultDefinition.SERVER.getLocalPart())
                                .faultString(INTERNAL_SERVER_ERROR)
                        .when(new AbstractTestAction() {
                            @Override
                            public void doExecute(TestContext context) {
                                throw new SoapFaultClientException(soapMessage);
                            }
                        });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "soap-fault");

        AssertSoapFault container = (AssertSoapFault)(test.getTestAction(0));

        Assert.assertEquals(container.getActionCount(), 1);
        Assert.assertTrue(container.getAction().getClass().isAnonymousClass());
        Assert.assertEquals(container.getFaultCode(), SoapFaultDefinition.SERVER.getLocalPart());
        Assert.assertEquals(container.getFaultString(), INTERNAL_SERVER_ERROR);

        verify(soapFaultValidator).validateSoapFault(any(com.consol.citrus.ws.message.SoapFault.class), any(com.consol.citrus.ws.message.SoapFault.class),
                any(TestContext.class), any(ValidationContext.class));
    }

    @Test
    public void testFaultDetail() {
        reset(applicationContextMock, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);

        when(soapMessage.getSoapBody()).thenReturn(soapBody);
        when(soapMessage.getFaultReason()).thenReturn(INTERNAL_SERVER_ERROR);
        when(soapBody.getFault()).thenReturn(soapFault);

        when(soapFault.getFaultActorOrRole()).thenReturn(SoapFaultDefinition.SERVER.getLocalPart());
        when(soapFault.getFaultCode()).thenReturn(SoapFaultDefinition.SERVER);
        when(soapFault.getFaultStringOrReason()).thenReturn(INTERNAL_SERVER_ERROR);
        when(soapFault.getFaultDetail()).thenReturn(soapFaultDetail);

        when(soapFaultDetail.getDetailEntries()).thenReturn(Collections.singleton(soapFaultDetailElement).iterator());
        when(soapFaultDetailElement.getSource()).thenReturn(new StringSource("<ErrorDetail><message>Something went wrong</message></ErrorDetail>"));

        when(applicationContextMock.getBean(TestContext.class)).thenReturn(applicationContext.getBean(TestContext.class));
        when(applicationContextMock.containsBean(SOAP_FAULT_VALIDATOR)).thenReturn(false);
        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock, context) {
            @Override
            public void execute() {
                assertSoapFault().faultCode(SoapFaultDefinition.SERVER.getLocalPart())
                                .faultString(INTERNAL_SERVER_ERROR)
                                .faultDetail("<ErrorDetail><message>Something went wrong</message></ErrorDetail>")
                        .when(new AbstractTestAction() {
                            @Override
                            public void doExecute(TestContext context) {
                                throw new SoapFaultClientException(soapMessage);
                            }
                        });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "soap-fault");
        
        AssertSoapFault container = (AssertSoapFault)(test.getTestAction(0));
        
        Assert.assertEquals(container.getActionCount(), 1);
        Assert.assertTrue(container.getAction().getClass().isAnonymousClass());
        Assert.assertEquals(container.getFaultCode(), SoapFaultDefinition.SERVER.getLocalPart());
        Assert.assertEquals(container.getFaultString(), INTERNAL_SERVER_ERROR);
        Assert.assertEquals(container.getFaultDetails().size(), 1L);
        Assert.assertEquals(container.getFaultDetails().get(0), "<ErrorDetail><message>Something went wrong</message></ErrorDetail>");

    }
    
    @Test
    public void testMultipleFaultDetails() {
        reset(applicationContextMock, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);

        when(soapMessage.getSoapBody()).thenReturn(soapBody);
        when(soapMessage.getFaultReason()).thenReturn(INTERNAL_SERVER_ERROR);
        when(soapBody.getFault()).thenReturn(soapFault);

        when(soapFault.getFaultActorOrRole()).thenReturn(SoapFaultDefinition.SERVER.getLocalPart());
        when(soapFault.getFaultCode()).thenReturn(SoapFaultDefinition.SERVER);
        when(soapFault.getFaultStringOrReason()).thenReturn(INTERNAL_SERVER_ERROR);
        when(soapFault.getFaultDetail()).thenReturn(soapFaultDetail);

        when(soapFaultDetail.getDetailEntries()).thenReturn(Arrays.asList(soapFaultDetailElement, soapFaultDetailElement).iterator());
        when(soapFaultDetailElement.getSource()).thenReturn(new StringSource("<ErrorDetail><code>1001</code></ErrorDetail>"))
                                                .thenReturn(new StringSource("<MessageDetail><message>Something went wrong</message></MessageDetail>"));

        when(applicationContextMock.getBean(TestContext.class)).thenReturn(applicationContext.getBean(TestContext.class));
        when(applicationContextMock.containsBean(SOAP_FAULT_VALIDATOR)).thenReturn(false);
        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock, context) {
            @Override
            public void execute() {
                assertSoapFault().faultCode(SoapFaultDefinition.SERVER.getLocalPart())
                                .faultString(INTERNAL_SERVER_ERROR)
                                .faultDetail("<ErrorDetail><code>1001</code></ErrorDetail>")
                                .faultDetail("<MessageDetail><message>Something went wrong</message></MessageDetail>")
                        .when(new AbstractTestAction() {
                            @Override
                            public void doExecute(TestContext context) {
                                throw new SoapFaultClientException(soapMessage);
                            }
                        });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "soap-fault");
        
        AssertSoapFault container = (AssertSoapFault)(test.getTestAction(0));
        
        Assert.assertEquals(container.getActionCount(), 1);
        Assert.assertTrue(container.getAction().getClass().isAnonymousClass());
        Assert.assertEquals(container.getFaultCode(), SoapFaultDefinition.SERVER.getLocalPart());
        Assert.assertEquals(container.getFaultString(), INTERNAL_SERVER_ERROR);
        Assert.assertEquals(container.getFaultDetails().size(), 2L);
        Assert.assertEquals(container.getFaultDetails().get(0), "<ErrorDetail><code>1001</code></ErrorDetail>");
        Assert.assertEquals(container.getFaultDetails().get(1), "<MessageDetail><message>Something went wrong</message></MessageDetail>");

    }
    
    @Test
    public void testFaultDetailResource() throws IOException {
        reset(resource, applicationContextMock, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("<ErrorDetail><message>Something went wrong</message></ErrorDetail>".getBytes()));

        when(soapMessage.getSoapBody()).thenReturn(soapBody);
        when(soapMessage.getFaultReason()).thenReturn(INTERNAL_SERVER_ERROR);
        when(soapBody.getFault()).thenReturn(soapFault);

        when(soapFault.getFaultActorOrRole()).thenReturn(SoapFaultDefinition.SERVER.getLocalPart());
        when(soapFault.getFaultCode()).thenReturn(SoapFaultDefinition.SERVER);
        when(soapFault.getFaultStringOrReason()).thenReturn(INTERNAL_SERVER_ERROR);
        when(soapFault.getFaultDetail()).thenReturn(soapFaultDetail);

        when(soapFaultDetail.getDetailEntries()).thenReturn(Collections.singleton(soapFaultDetailElement).iterator());
        when(soapFaultDetailElement.getSource()).thenReturn(new StringSource("<ErrorDetail><message>Something went wrong</message></ErrorDetail>"));

        when(applicationContextMock.getBean(TestContext.class)).thenReturn(applicationContext.getBean(TestContext.class));
        when(applicationContextMock.containsBean(SOAP_FAULT_VALIDATOR)).thenReturn(false);
        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock, context) {
            @Override
            public void execute() {
                assertSoapFault().faultCode(SoapFaultDefinition.SERVER.getLocalPart())
                                .faultString(INTERNAL_SERVER_ERROR)
                                .faultDetailResource(resource)
                        .when(new AbstractTestAction() {
                            @Override
                            public void doExecute(TestContext context) {
                                throw new SoapFaultClientException(soapMessage);
                            }
                        });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "soap-fault");
        
        AssertSoapFault container = (AssertSoapFault)(test.getTestAction(0));
        
        Assert.assertEquals(container.getActionCount(), 1);
        Assert.assertTrue(container.getAction().getClass().isAnonymousClass());
        Assert.assertEquals(container.getFaultCode(), SoapFaultDefinition.SERVER.getLocalPart());
        Assert.assertEquals(container.getFaultString(), INTERNAL_SERVER_ERROR);
        Assert.assertEquals(container.getFaultDetails().size(), 1L);
        Assert.assertEquals(container.getFaultDetails().get(0), "<ErrorDetail><message>Something went wrong</message></ErrorDetail>");

    }

    @Test
    public void testFaultDetailResourcePath() {
        reset(applicationContextMock, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);

        when(soapMessage.getSoapBody()).thenReturn(soapBody);
        when(soapMessage.getFaultReason()).thenReturn(INTERNAL_SERVER_ERROR);
        when(soapBody.getFault()).thenReturn(soapFault);

        when(soapFault.getFaultActorOrRole()).thenReturn(SoapFaultDefinition.SERVER.getLocalPart());
        when(soapFault.getFaultCode()).thenReturn(SoapFaultDefinition.SERVER);
        when(soapFault.getFaultStringOrReason()).thenReturn(INTERNAL_SERVER_ERROR);
        when(soapFault.getFaultDetail()).thenReturn(soapFaultDetail);

        when(soapFaultDetail.getDetailEntries()).thenReturn(Collections.singleton(soapFaultDetailElement).iterator());
        when(soapFaultDetailElement.getSource()).thenReturn(new StringSource("<ErrorDetail><message>Something went wrong</message></ErrorDetail>"));

        when(applicationContextMock.getBean(TestContext.class)).thenReturn(applicationContext.getBean(TestContext.class));
        when(applicationContextMock.containsBean(SOAP_FAULT_VALIDATOR)).thenReturn(false);
        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock, context) {
            @Override
            public void execute() {
                assertSoapFault().faultCode(SoapFaultDefinition.SERVER.getLocalPart())
                                .faultString(INTERNAL_SERVER_ERROR)
                                .faultDetailResource("classpath:com/consol/citrus/dsl/runner/soap-fault-detail.xml")
                        .when(new AbstractTestAction() {
                            @Override
                            public void doExecute(TestContext context) {
                                throw new SoapFaultClientException(soapMessage);
                            }
                        });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "soap-fault");

        AssertSoapFault container = (AssertSoapFault)(test.getTestAction(0));

        Assert.assertEquals(container.getActionCount(), 1);
        Assert.assertTrue(container.getAction().getClass().isAnonymousClass());
        Assert.assertEquals(container.getFaultCode(), SoapFaultDefinition.SERVER.getLocalPart());
        Assert.assertEquals(container.getFaultString(), INTERNAL_SERVER_ERROR);
        Assert.assertEquals(container.getFaultDetails().size(), 0L);
        Assert.assertEquals(container.getFaultDetailResourcePaths().size(), 1L);
        Assert.assertEquals(container.getFaultDetailResourcePaths().get(0), "classpath:com/consol/citrus/dsl/runner/soap-fault-detail.xml");

    }
    
    @Test
    public void testMultipleFaultDetailsInlineAndResource() throws IOException {
        reset(resource, applicationContextMock, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);
        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("<MessageDetail><message>Something went wrong</message></MessageDetail>".getBytes()));

        when(soapMessage.getSoapBody()).thenReturn(soapBody);
        when(soapMessage.getFaultReason()).thenReturn(INTERNAL_SERVER_ERROR);
        when(soapBody.getFault()).thenReturn(soapFault);

        when(soapFault.getFaultActorOrRole()).thenReturn(SoapFaultDefinition.SERVER.getLocalPart());
        when(soapFault.getFaultCode()).thenReturn(SoapFaultDefinition.SERVER);
        when(soapFault.getFaultStringOrReason()).thenReturn(INTERNAL_SERVER_ERROR);
        when(soapFault.getFaultDetail()).thenReturn(soapFaultDetail);

        when(soapFaultDetail.getDetailEntries()).thenReturn(Arrays.asList(soapFaultDetailElement, soapFaultDetailElement).iterator());
        when(soapFaultDetailElement.getSource()).thenReturn(new StringSource("<ErrorDetail><code>1001</code></ErrorDetail>"))
                                                .thenReturn(new StringSource("<MessageDetail><message>Something went wrong</message></MessageDetail>"));

        when(applicationContextMock.getBean(TestContext.class)).thenReturn(applicationContext.getBean(TestContext.class));
        when(applicationContextMock.containsBean(SOAP_FAULT_VALIDATOR)).thenReturn(false);
        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock, context) {
            @Override
            public void execute() {
                assertSoapFault().faultCode(SoapFaultDefinition.SERVER.getLocalPart())
                                .faultString(INTERNAL_SERVER_ERROR)
                                .faultDetail("<ErrorDetail><code>1001</code></ErrorDetail>")
                                .faultDetailResource(resource)
                        .when(new AbstractTestAction() {
                            @Override
                            public void doExecute(TestContext context) {
                                throw new SoapFaultClientException(soapMessage);
                            }
                        });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "soap-fault");
        
        AssertSoapFault container = (AssertSoapFault)(test.getTestAction(0));
        
        Assert.assertEquals(container.getActionCount(), 1);
        Assert.assertTrue(container.getAction().getClass().isAnonymousClass());
        Assert.assertEquals(container.getFaultCode(), SoapFaultDefinition.SERVER.getLocalPart());
        Assert.assertEquals(container.getFaultString(), INTERNAL_SERVER_ERROR);
        Assert.assertEquals(container.getFaultDetails().size(), 2L);
        Assert.assertEquals(container.getFaultDetails().get(0), "<ErrorDetail><code>1001</code></ErrorDetail>");
        Assert.assertEquals(container.getFaultDetails().get(1), "<MessageDetail><message>Something went wrong</message></MessageDetail>");

    }
    
    @Test
    public void testAssertSoapFaultBuilderWithValidator() {
        reset(applicationContextMock, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);

        when(soapMessage.getSoapBody()).thenReturn(soapBody);
        when(soapMessage.getFaultReason()).thenReturn(INTERNAL_SERVER_ERROR);
        when(soapBody.getFault()).thenReturn(soapFault);

        when(soapFault.getFaultActorOrRole()).thenReturn(SoapFaultDefinition.SERVER.getLocalPart());
        when(soapFault.getFaultCode()).thenReturn(SoapFaultDefinition.SERVER);
        when(soapFault.getFaultStringOrReason()).thenReturn(INTERNAL_SERVER_ERROR);
        when(soapFault.getFaultDetail()).thenReturn(null);

        when(applicationContextMock.getBean(TestContext.class)).thenReturn(applicationContext.getBean(TestContext.class));
        when(applicationContextMock.containsBean(SOAP_FAULT_VALIDATOR)).thenReturn(true);
        when(applicationContextMock.getBean(SOAP_FAULT_VALIDATOR, SoapFaultValidator.class)).thenReturn(soapFaultValidator);
        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock, context) {
            @Override
            public void execute() {
                assertSoapFault().faultCode(SoapFaultDefinition.SERVER.getLocalPart())
                                .faultString(INTERNAL_SERVER_ERROR)
                                .validator(soapFaultValidator)
                        .when(new AbstractTestAction() {
                            @Override
                            public void doExecute(TestContext context) {
                                throw new SoapFaultClientException(soapMessage);
                            }
                        });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "soap-fault");

        AssertSoapFault container = (AssertSoapFault)(test.getTestAction(0));

        Assert.assertEquals(container.getActionCount(), 1);
        Assert.assertTrue(container.getAction().getClass().isAnonymousClass());
        Assert.assertEquals(container.getFaultCode(), SoapFaultDefinition.SERVER.getLocalPart());
        Assert.assertEquals(container.getFaultString(), INTERNAL_SERVER_ERROR);
        Assert.assertEquals(container.getValidator(), soapFaultValidator);

        verify(soapFaultValidator).validateSoapFault(any(com.consol.citrus.ws.message.SoapFault.class), any(com.consol.citrus.ws.message.SoapFault.class),
                any(TestContext.class), any(ValidationContext.class));
    }

    @Test
    public void testAssertSoapFaultBuilderWithActor() {
        reset(applicationContextMock, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);

        when(soapMessage.getSoapBody()).thenReturn(soapBody);
        when(soapMessage.getFaultReason()).thenReturn(INTERNAL_SERVER_ERROR);
        when(soapBody.getFault()).thenReturn(soapFault);

        when(soapFault.getFaultActorOrRole()).thenReturn("MyActor");
        when(soapFault.getFaultCode()).thenReturn(SoapFaultDefinition.SERVER);
        when(soapFault.getFaultStringOrReason()).thenReturn(INTERNAL_SERVER_ERROR);
        when(soapFault.getFaultDetail()).thenReturn(null);

        when(applicationContextMock.getBean(TestContext.class)).thenReturn(applicationContext.getBean(TestContext.class));
        when(applicationContextMock.containsBean(SOAP_FAULT_VALIDATOR)).thenReturn(false);
        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock, context) {
            @Override
            public void execute() {
                assertSoapFault().faultCode(SoapFaultDefinition.SERVER.getLocalPart())
                                .faultString(INTERNAL_SERVER_ERROR)
                                .faultActor("MyActor")
                        .when(new AbstractTestAction() {
                            @Override
                            public void doExecute(TestContext context) {
                                throw new SoapFaultClientException(soapMessage);
                            }
                        });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "soap-fault");
        
        AssertSoapFault container = (AssertSoapFault)(test.getTestAction(0));
        
        Assert.assertEquals(container.getActionCount(), 1);
        Assert.assertTrue(container.getAction().getClass().isAnonymousClass());
        Assert.assertEquals(container.getFaultCode(), SoapFaultDefinition.SERVER.getLocalPart());
        Assert.assertEquals(container.getFaultString(), INTERNAL_SERVER_ERROR);
        Assert.assertEquals(container.getFaultActor(), "MyActor");

    }
}
