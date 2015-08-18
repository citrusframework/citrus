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
import org.easymock.EasyMock;
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

import static org.easymock.EasyMock.*;

public class AssertSoapFaultTestRunnerTest extends AbstractTestNGUnitTest {

    public static final String INTERNAL_SERVER_ERROR = "Internal server error";
    public static final String SOAP_FAULT_VALIDATOR = "soapFaultValidator";

    private Resource resource = EasyMock.createMock(Resource.class);
    private SoapFaultValidator soapFaultValidator = EasyMock.createMock(SoapFaultValidator.class);
    private ApplicationContext applicationContextMock = EasyMock.createMock(ApplicationContext.class);

    private SoapMessage soapMessage = EasyMock.createMock(org.springframework.ws.soap.SoapMessage.class);
    private SoapBody soapBody = EasyMock.createMock(SoapBody.class);
    private SoapFault soapFault = EasyMock.createMock(SoapFault.class);
    private SoapFaultDetail soapFaultDetail = EasyMock.createMock(SoapFaultDetail.class);
    private SoapFaultDetailElement soapFaultDetailElement = EasyMock.createMock(SoapFaultDetailElement.class);

    @Test
    public void testAssertSoapFaultBuilder() {
        reset(applicationContextMock, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);

        expect(soapMessage.getSoapBody()).andReturn(soapBody).once();
        expect(soapMessage.getFaultReason()).andReturn(INTERNAL_SERVER_ERROR).once();
        expect(soapBody.getFault()).andReturn(soapFault).once();

        expect(soapFault.getFaultActorOrRole()).andReturn(SoapFaultDefinition.SERVER.getLocalPart()).once();
        expect(soapFault.getFaultCode()).andReturn(SoapFaultDefinition.SERVER).atLeastOnce();
        expect(soapFault.getFaultStringOrReason()).andReturn(INTERNAL_SERVER_ERROR).atLeastOnce();
        expect(soapFault.getFaultDetail()).andReturn(null).once();

        expect(applicationContextMock.getBean(TestContext.class)).andReturn(applicationContext.getBean(TestContext.class)).once();
        expect(applicationContextMock.containsBean(SOAP_FAULT_VALIDATOR)).andReturn(false).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();

        replay(applicationContextMock, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock) {
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

        verify(applicationContextMock, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);
    }

    @Test
    public void testAssertSoapFaultDefaultValidatorBuilder() {
        reset(applicationContextMock, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);

        expect(soapMessage.getSoapBody()).andReturn(soapBody).once();
        expect(soapMessage.getFaultReason()).andReturn(INTERNAL_SERVER_ERROR).once();
        expect(soapBody.getFault()).andReturn(soapFault).once();

        expect(soapFault.getFaultActorOrRole()).andReturn(SoapFaultDefinition.SERVER.getLocalPart()).once();
        expect(soapFault.getFaultCode()).andReturn(SoapFaultDefinition.SERVER).atLeastOnce();
        expect(soapFault.getFaultStringOrReason()).andReturn(INTERNAL_SERVER_ERROR).atLeastOnce();
        expect(soapFault.getFaultDetail()).andReturn(null).once();

        expect(applicationContextMock.getBean(TestContext.class)).andReturn(applicationContext.getBean(TestContext.class)).once();
        expect(applicationContextMock.containsBean(SOAP_FAULT_VALIDATOR)).andReturn(true).once();
        expect(applicationContextMock.getBean(SOAP_FAULT_VALIDATOR, SoapFaultValidator.class)).andReturn(soapFaultValidator).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();

        soapFaultValidator.validateSoapFault(anyObject(com.consol.citrus.ws.message.SoapFault.class), anyObject(com.consol.citrus.ws.message.SoapFault.class),
                anyObject(TestContext.class), anyObject(ValidationContext.class));
        expectLastCall().once();

        replay(applicationContextMock, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock) {
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

        verify(applicationContextMock, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);
    }
    
    @Test
    public void testFaultDetail() {
        reset(applicationContextMock, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);

        expect(soapMessage.getSoapBody()).andReturn(soapBody).once();
        expect(soapMessage.getFaultReason()).andReturn(INTERNAL_SERVER_ERROR).once();
        expect(soapBody.getFault()).andReturn(soapFault).once();

        expect(soapFault.getFaultActorOrRole()).andReturn(SoapFaultDefinition.SERVER.getLocalPart()).once();
        expect(soapFault.getFaultCode()).andReturn(SoapFaultDefinition.SERVER).atLeastOnce();
        expect(soapFault.getFaultStringOrReason()).andReturn(INTERNAL_SERVER_ERROR).atLeastOnce();
        expect(soapFault.getFaultDetail()).andReturn(soapFaultDetail).atLeastOnce();

        expect(soapFaultDetail.getDetailEntries()).andReturn(Collections.singleton(soapFaultDetailElement).iterator()).once();
        expect(soapFaultDetailElement.getSource()).andReturn(new StringSource("<ErrorDetail><message>Something went wrong</message></ErrorDetail>")).once();

        expect(applicationContextMock.getBean(TestContext.class)).andReturn(applicationContext.getBean(TestContext.class)).once();
        expect(applicationContextMock.containsBean(SOAP_FAULT_VALIDATOR)).andReturn(false).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();

        replay(applicationContextMock, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock) {
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

        verify(applicationContextMock, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);
    }
    
    @Test
    public void testMultipleFaultDetails() {
        reset(applicationContextMock, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);

        expect(soapMessage.getSoapBody()).andReturn(soapBody).once();
        expect(soapMessage.getFaultReason()).andReturn(INTERNAL_SERVER_ERROR).once();
        expect(soapBody.getFault()).andReturn(soapFault).once();

        expect(soapFault.getFaultActorOrRole()).andReturn(SoapFaultDefinition.SERVER.getLocalPart()).once();
        expect(soapFault.getFaultCode()).andReturn(SoapFaultDefinition.SERVER).atLeastOnce();
        expect(soapFault.getFaultStringOrReason()).andReturn(INTERNAL_SERVER_ERROR).atLeastOnce();
        expect(soapFault.getFaultDetail()).andReturn(soapFaultDetail).atLeastOnce();

        expect(soapFaultDetail.getDetailEntries()).andReturn(Arrays.asList(soapFaultDetailElement, soapFaultDetailElement).iterator()).once();
        expect(soapFaultDetailElement.getSource()).andReturn(new StringSource("<ErrorDetail><code>1001</code></ErrorDetail>")).once();
        expect(soapFaultDetailElement.getSource()).andReturn(new StringSource("<MessageDetail><message>Something went wrong</message></MessageDetail>")).once();

        expect(applicationContextMock.getBean(TestContext.class)).andReturn(applicationContext.getBean(TestContext.class)).once();
        expect(applicationContextMock.containsBean(SOAP_FAULT_VALIDATOR)).andReturn(false).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();

        replay(applicationContextMock, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock) {
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

        verify(applicationContextMock, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);
    }
    
    @Test
    public void testFaultDetailResource() throws IOException {
        reset(resource, applicationContextMock, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);
        expect(resource.getInputStream()).andReturn(new ByteArrayInputStream("<ErrorDetail><message>Something went wrong</message></ErrorDetail>".getBytes())).once();

        expect(soapMessage.getSoapBody()).andReturn(soapBody).once();
        expect(soapMessage.getFaultReason()).andReturn(INTERNAL_SERVER_ERROR).once();
        expect(soapBody.getFault()).andReturn(soapFault).once();

        expect(soapFault.getFaultActorOrRole()).andReturn(SoapFaultDefinition.SERVER.getLocalPart()).once();
        expect(soapFault.getFaultCode()).andReturn(SoapFaultDefinition.SERVER).atLeastOnce();
        expect(soapFault.getFaultStringOrReason()).andReturn(INTERNAL_SERVER_ERROR).atLeastOnce();
        expect(soapFault.getFaultDetail()).andReturn(soapFaultDetail).atLeastOnce();

        expect(soapFaultDetail.getDetailEntries()).andReturn(Collections.singleton(soapFaultDetailElement).iterator()).once();
        expect(soapFaultDetailElement.getSource()).andReturn(new StringSource("<ErrorDetail><message>Something went wrong</message></ErrorDetail>")).once();

        expect(applicationContextMock.getBean(TestContext.class)).andReturn(applicationContext.getBean(TestContext.class)).once();
        expect(applicationContextMock.containsBean(SOAP_FAULT_VALIDATOR)).andReturn(false).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();

        replay(resource, applicationContextMock, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock) {
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

        verify(resource, applicationContextMock, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);
    }

    @Test
    public void testFaultDetailResourcePath() {
        reset(applicationContextMock, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);

        expect(soapMessage.getSoapBody()).andReturn(soapBody).once();
        expect(soapMessage.getFaultReason()).andReturn(INTERNAL_SERVER_ERROR).once();
        expect(soapBody.getFault()).andReturn(soapFault).once();

        expect(soapFault.getFaultActorOrRole()).andReturn(SoapFaultDefinition.SERVER.getLocalPart()).once();
        expect(soapFault.getFaultCode()).andReturn(SoapFaultDefinition.SERVER).atLeastOnce();
        expect(soapFault.getFaultStringOrReason()).andReturn(INTERNAL_SERVER_ERROR).atLeastOnce();
        expect(soapFault.getFaultDetail()).andReturn(soapFaultDetail).atLeastOnce();

        expect(soapFaultDetail.getDetailEntries()).andReturn(Collections.singleton(soapFaultDetailElement).iterator()).once();
        expect(soapFaultDetailElement.getSource()).andReturn(new StringSource("<ErrorDetail><message>Something went wrong</message></ErrorDetail>")).once();

        expect(applicationContextMock.getBean(TestContext.class)).andReturn(applicationContext.getBean(TestContext.class)).once();
        expect(applicationContextMock.containsBean(SOAP_FAULT_VALIDATOR)).andReturn(false).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();

        replay(applicationContextMock, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock) {
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

        verify(applicationContextMock, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);
    }
    
    @Test
    public void testMultipleFaultDetailsInlineAndResource() throws IOException {
        reset(resource, applicationContextMock, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);
        expect(resource.getInputStream()).andReturn(new ByteArrayInputStream("<MessageDetail><message>Something went wrong</message></MessageDetail>".getBytes())).once();

        expect(soapMessage.getSoapBody()).andReturn(soapBody).once();
        expect(soapMessage.getFaultReason()).andReturn(INTERNAL_SERVER_ERROR).once();
        expect(soapBody.getFault()).andReturn(soapFault).once();

        expect(soapFault.getFaultActorOrRole()).andReturn(SoapFaultDefinition.SERVER.getLocalPart()).once();
        expect(soapFault.getFaultCode()).andReturn(SoapFaultDefinition.SERVER).atLeastOnce();
        expect(soapFault.getFaultStringOrReason()).andReturn(INTERNAL_SERVER_ERROR).atLeastOnce();
        expect(soapFault.getFaultDetail()).andReturn(soapFaultDetail).atLeastOnce();

        expect(soapFaultDetail.getDetailEntries()).andReturn(Arrays.asList(soapFaultDetailElement, soapFaultDetailElement).iterator()).once();
        expect(soapFaultDetailElement.getSource()).andReturn(new StringSource("<ErrorDetail><code>1001</code></ErrorDetail>")).once();
        expect(soapFaultDetailElement.getSource()).andReturn(new StringSource("<MessageDetail><message>Something went wrong</message></MessageDetail>")).once();

        expect(applicationContextMock.getBean(TestContext.class)).andReturn(applicationContext.getBean(TestContext.class)).once();
        expect(applicationContextMock.containsBean(SOAP_FAULT_VALIDATOR)).andReturn(false).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();

        replay(resource, applicationContextMock, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock) {
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

        verify(resource, applicationContextMock, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);
    }
    
    @Test
    public void testAssertSoapFaultBuilderWithValidator() {
        reset(applicationContextMock, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);

        expect(soapMessage.getSoapBody()).andReturn(soapBody).once();
        expect(soapMessage.getFaultReason()).andReturn(INTERNAL_SERVER_ERROR).once();
        expect(soapBody.getFault()).andReturn(soapFault).once();

        expect(soapFault.getFaultActorOrRole()).andReturn(SoapFaultDefinition.SERVER.getLocalPart()).once();
        expect(soapFault.getFaultCode()).andReturn(SoapFaultDefinition.SERVER).atLeastOnce();
        expect(soapFault.getFaultStringOrReason()).andReturn(INTERNAL_SERVER_ERROR).atLeastOnce();
        expect(soapFault.getFaultDetail()).andReturn(null).once();

        expect(applicationContextMock.getBean(TestContext.class)).andReturn(applicationContext.getBean(TestContext.class)).once();
        expect(applicationContextMock.containsBean(SOAP_FAULT_VALIDATOR)).andReturn(true).once();
        expect(applicationContextMock.getBean(SOAP_FAULT_VALIDATOR, SoapFaultValidator.class)).andReturn(soapFaultValidator).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();

        soapFaultValidator.validateSoapFault(anyObject(com.consol.citrus.ws.message.SoapFault.class), anyObject(com.consol.citrus.ws.message.SoapFault.class),
                anyObject(TestContext.class), anyObject(ValidationContext.class));
        expectLastCall().once();

        replay(applicationContextMock, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock) {
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

        verify(applicationContextMock, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);
    }
    
    @Test
    public void testAssertSoapFaultBuilderWithActor() {
        reset(applicationContextMock, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);

        expect(soapMessage.getSoapBody()).andReturn(soapBody).once();
        expect(soapMessage.getFaultReason()).andReturn(INTERNAL_SERVER_ERROR).once();
        expect(soapBody.getFault()).andReturn(soapFault).once();

        expect(soapFault.getFaultActorOrRole()).andReturn("MyActor").once();
        expect(soapFault.getFaultCode()).andReturn(SoapFaultDefinition.SERVER).atLeastOnce();
        expect(soapFault.getFaultStringOrReason()).andReturn(INTERNAL_SERVER_ERROR).atLeastOnce();
        expect(soapFault.getFaultDetail()).andReturn(null).once();

        expect(applicationContextMock.getBean(TestContext.class)).andReturn(applicationContext.getBean(TestContext.class)).once();
        expect(applicationContextMock.containsBean(SOAP_FAULT_VALIDATOR)).andReturn(false).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();

        replay(applicationContextMock, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContextMock) {
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

        verify(applicationContextMock, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);
    }
}
