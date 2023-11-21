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

package org.citrusframework.ws.actions.dsl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.TestCase;
import org.citrusframework.container.SequenceAfterTest;
import org.citrusframework.container.SequenceBeforeTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.report.TestActionListeners;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.Resource;
import org.citrusframework.ws.UnitTestSupport;
import org.citrusframework.ws.actions.AssertSoapFault;
import org.citrusframework.ws.validation.SoapFaultValidationContext;
import org.citrusframework.ws.validation.SoapFaultValidator;
import org.citrusframework.xml.StringSource;
import org.mockito.Mockito;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.SoapFaultDetailElement;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.DefaultTestActionBuilder.action;
import static org.citrusframework.ws.actions.SoapActionBuilder.soap;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AssertSoapFaultBuilderTest extends UnitTestSupport {

    public static final String INTERNAL_SERVER_ERROR = "Internal server error";
    public static final String SOAP_FAULT_VALIDATOR = "soapFaultValidator";

    private final Resource resource = Mockito.mock(Resource.class);
    private final SoapFaultValidator soapFaultValidator = Mockito.mock(SoapFaultValidator.class);
    private final ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);

    private final SoapMessage soapMessage = Mockito.mock(SoapMessage.class);
    private final SoapBody soapBody = Mockito.mock(SoapBody.class);
    private final SoapFault soapFault = Mockito.mock(SoapFault.class);
    private final SoapFaultDetail soapFaultDetail = Mockito.mock(SoapFaultDetail.class);
    private final SoapFaultDetailElement soapFaultDetailElement = Mockito.mock(SoapFaultDetailElement.class);

    @Test
    public void testAssertSoapFaultBuilder() {
        reset(referenceResolver, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);

        when(soapMessage.getSoapBody()).thenReturn(soapBody);
        when(soapMessage.getFaultReason()).thenReturn(INTERNAL_SERVER_ERROR);
        when(soapBody.getFault()).thenReturn(soapFault);

        when(soapFault.getFaultActorOrRole()).thenReturn(SoapFaultDefinition.SERVER.getLocalPart());
        when(soapFault.getFaultCode()).thenReturn(SoapFaultDefinition.SERVER);
        when(soapFault.getFaultStringOrReason()).thenReturn(INTERNAL_SERVER_ERROR);
        when(soapFault.getFaultDetail()).thenReturn(null);

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.isResolvable(SOAP_FAULT_VALIDATOR)).thenReturn(false);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(soap().client("soapClient")
                        .assertFault()
                            .faultCode(SoapFaultDefinition.SERVER.getLocalPart())
                            .faultString(INTERNAL_SERVER_ERROR)
                        .when(action(context -> {
                            throw new SoapFaultClientException(soapMessage);
                        })));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "soap:assert-fault");

        AssertSoapFault container = (AssertSoapFault)(test.getTestAction(0));

        Assert.assertEquals(container.getActionCount(), 1);
        Assert.assertTrue(container.getAction().getClass().isAnonymousClass());
        Assert.assertEquals(container.getFaultCode(), SoapFaultDefinition.SERVER.getLocalPart());
        Assert.assertEquals(container.getFaultString(), INTERNAL_SERVER_ERROR);

    }

    @Test
    public void testAssertSoapFaultExplicitValidatorBuilder() {
        reset(referenceResolver, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);

        when(soapMessage.getSoapBody()).thenReturn(soapBody);
        when(soapMessage.getFaultReason()).thenReturn(INTERNAL_SERVER_ERROR);
        when(soapBody.getFault()).thenReturn(soapFault);

        when(soapFault.getFaultActorOrRole()).thenReturn(SoapFaultDefinition.SERVER.getLocalPart());
        when(soapFault.getFaultCode()).thenReturn(SoapFaultDefinition.SERVER);
        when(soapFault.getFaultStringOrReason()).thenReturn(INTERNAL_SERVER_ERROR);
        when(soapFault.getFaultDetail()).thenReturn(null);

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(soap().client("soapClient")
                    .assertFault()
                        .faultCode(SoapFaultDefinition.SERVER.getLocalPart())
                        .faultString(INTERNAL_SERVER_ERROR)
                        .validator(soapFaultValidator)
                    .when(action(context -> {
                        throw new SoapFaultClientException(soapMessage);
                    })));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "soap:assert-fault");

        AssertSoapFault container = (AssertSoapFault)(test.getTestAction(0));

        Assert.assertEquals(container.getActionCount(), 1);
        Assert.assertTrue(container.getAction().getClass().isAnonymousClass());
        Assert.assertEquals(container.getFaultCode(), SoapFaultDefinition.SERVER.getLocalPart());
        Assert.assertEquals(container.getFaultString(), INTERNAL_SERVER_ERROR);

        verify(soapFaultValidator).validateSoapFault(any(org.citrusframework.ws.message.SoapFault.class), any(org.citrusframework.ws.message.SoapFault.class),
                any(TestContext.class), any(SoapFaultValidationContext.class));
    }

    @Test
    public void testAssertSoapFaultWithValidatorNameBuilder() {
        reset(referenceResolver, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);

        when(soapMessage.getSoapBody()).thenReturn(soapBody);
        when(soapMessage.getFaultReason()).thenReturn(INTERNAL_SERVER_ERROR);
        when(soapBody.getFault()).thenReturn(soapFault);

        when(soapFault.getFaultActorOrRole()).thenReturn(SoapFaultDefinition.SERVER.getLocalPart());
        when(soapFault.getFaultCode()).thenReturn(SoapFaultDefinition.SERVER);
        when(soapFault.getFaultStringOrReason()).thenReturn(INTERNAL_SERVER_ERROR);
        when(soapFault.getFaultDetail()).thenReturn(null);

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.isResolvable(SOAP_FAULT_VALIDATOR)).thenReturn(true);
        when(referenceResolver.resolve(SOAP_FAULT_VALIDATOR, SoapFaultValidator.class)).thenReturn(soapFaultValidator);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(soap().client("soapClient")
                        .assertFault()
                                .faultCode(SoapFaultDefinition.SERVER.getLocalPart())
                                .faultString(INTERNAL_SERVER_ERROR)
                                .validator(SOAP_FAULT_VALIDATOR)
                        .when(action(context -> {
                            throw new SoapFaultClientException(soapMessage);
                        })));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "soap:assert-fault");

        AssertSoapFault container = (AssertSoapFault)(test.getTestAction(0));

        Assert.assertEquals(container.getActionCount(), 1);
        Assert.assertTrue(container.getAction().getClass().isAnonymousClass());
        Assert.assertEquals(container.getFaultCode(), SoapFaultDefinition.SERVER.getLocalPart());
        Assert.assertEquals(container.getFaultString(), INTERNAL_SERVER_ERROR);

        verify(soapFaultValidator).validateSoapFault(any(org.citrusframework.ws.message.SoapFault.class), any(org.citrusframework.ws.message.SoapFault.class),
                any(TestContext.class), any(SoapFaultValidationContext.class));
    }

    @Test
    public void testAssertSoapFaultDefaultValidatorBuilder() {
        reset(referenceResolver, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);

        when(soapMessage.getSoapBody()).thenReturn(soapBody);
        when(soapMessage.getFaultReason()).thenReturn(INTERNAL_SERVER_ERROR);
        when(soapBody.getFault()).thenReturn(soapFault);

        when(soapFault.getFaultActorOrRole()).thenReturn(SoapFaultDefinition.SERVER.getLocalPart());
        when(soapFault.getFaultCode()).thenReturn(SoapFaultDefinition.SERVER);
        when(soapFault.getFaultStringOrReason()).thenReturn(INTERNAL_SERVER_ERROR);
        when(soapFault.getFaultDetail()).thenReturn(null);

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.isResolvable(SOAP_FAULT_VALIDATOR)).thenReturn(true);
        when(referenceResolver.resolve(SOAP_FAULT_VALIDATOR, SoapFaultValidator.class)).thenReturn(soapFaultValidator);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(soap().client("soapClient")
                        .assertFault()
                                .faultCode(SoapFaultDefinition.SERVER.getLocalPart())
                                .faultString(INTERNAL_SERVER_ERROR)
                        .when(action(context -> {
                            throw new SoapFaultClientException(soapMessage);
                        })));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "soap:assert-fault");

        AssertSoapFault container = (AssertSoapFault)(test.getTestAction(0));

        Assert.assertEquals(container.getActionCount(), 1);
        Assert.assertTrue(container.getAction().getClass().isAnonymousClass());
        Assert.assertEquals(container.getFaultCode(), SoapFaultDefinition.SERVER.getLocalPart());
        Assert.assertEquals(container.getFaultString(), INTERNAL_SERVER_ERROR);

        verify(soapFaultValidator).validateSoapFault(any(org.citrusframework.ws.message.SoapFault.class), any(org.citrusframework.ws.message.SoapFault.class),
                any(TestContext.class), any(SoapFaultValidationContext.class));
    }

    @Test
    public void testFaultDetail() {
        reset(referenceResolver, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);

        when(soapMessage.getSoapBody()).thenReturn(soapBody);
        when(soapMessage.getFaultReason()).thenReturn(INTERNAL_SERVER_ERROR);
        when(soapBody.getFault()).thenReturn(soapFault);

        when(soapFault.getFaultActorOrRole()).thenReturn(SoapFaultDefinition.SERVER.getLocalPart());
        when(soapFault.getFaultCode()).thenReturn(SoapFaultDefinition.SERVER);
        when(soapFault.getFaultStringOrReason()).thenReturn(INTERNAL_SERVER_ERROR);
        when(soapFault.getFaultDetail()).thenReturn(soapFaultDetail);

        when(soapFaultDetail.getDetailEntries()).thenReturn(Collections.singleton(soapFaultDetailElement).iterator());
        when(soapFaultDetailElement.getSource()).thenReturn(new StringSource("<ErrorDetail><message>Something went wrong</message></ErrorDetail>"));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.isResolvable(SOAP_FAULT_VALIDATOR)).thenReturn(false);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(soap().client("soapClient")
                        .assertFault()
                                .faultCode(SoapFaultDefinition.SERVER.getLocalPart())
                                .faultString(INTERNAL_SERVER_ERROR)
                                .faultDetail("<ErrorDetail><message>Something went wrong</message></ErrorDetail>")
                        .when(action(context -> {
                            throw new SoapFaultClientException(soapMessage);
                        })));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "soap:assert-fault");

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
        reset(referenceResolver, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);

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

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.isResolvable(SOAP_FAULT_VALIDATOR)).thenReturn(false);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(soap().client("soapClient")
                        .assertFault()
                                .faultCode(SoapFaultDefinition.SERVER.getLocalPart())
                                .faultString(INTERNAL_SERVER_ERROR)
                                .faultDetail("<ErrorDetail><code>1001</code></ErrorDetail>")
                                .faultDetail("<MessageDetail><message>Something went wrong</message></MessageDetail>")
                        .when(action(context -> {
                            throw new SoapFaultClientException(soapMessage);
                        })));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "soap:assert-fault");

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
        reset(resource, referenceResolver, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);
        when(resource.exists()).thenReturn(true);
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

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.isResolvable(SOAP_FAULT_VALIDATOR)).thenReturn(false);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(soap().client("soapClient")
                        .assertFault()
                                .faultCode(SoapFaultDefinition.SERVER.getLocalPart())
                                .faultString(INTERNAL_SERVER_ERROR)
                                .faultDetailResource(resource)
                        .when(action(context -> {
                            throw new SoapFaultClientException(soapMessage);
                        })));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "soap:assert-fault");

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
        reset(referenceResolver, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);

        when(soapMessage.getSoapBody()).thenReturn(soapBody);
        when(soapMessage.getFaultReason()).thenReturn(INTERNAL_SERVER_ERROR);
        when(soapBody.getFault()).thenReturn(soapFault);

        when(soapFault.getFaultActorOrRole()).thenReturn(SoapFaultDefinition.SERVER.getLocalPart());
        when(soapFault.getFaultCode()).thenReturn(SoapFaultDefinition.SERVER);
        when(soapFault.getFaultStringOrReason()).thenReturn(INTERNAL_SERVER_ERROR);
        when(soapFault.getFaultDetail()).thenReturn(soapFaultDetail);

        when(soapFaultDetail.getDetailEntries()).thenReturn(Collections.singleton(soapFaultDetailElement).iterator());
        when(soapFaultDetailElement.getSource()).thenReturn(new StringSource("<ErrorDetail><message>Something went wrong</message></ErrorDetail>"));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.isResolvable(SOAP_FAULT_VALIDATOR)).thenReturn(false);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(soap().client("soapClient")
                        .assertFault()
                                .faultCode(SoapFaultDefinition.SERVER.getLocalPart())
                                .faultString(INTERNAL_SERVER_ERROR)
                                .faultDetailResource("classpath:org/citrusframework/ws/actions/dsl/soap-fault-detail.xml")
                        .when(action(context -> {
                            throw new SoapFaultClientException(soapMessage);
                        })));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "soap:assert-fault");

        AssertSoapFault container = (AssertSoapFault)(test.getTestAction(0));

        Assert.assertEquals(container.getActionCount(), 1);
        Assert.assertTrue(container.getAction().getClass().isAnonymousClass());
        Assert.assertEquals(container.getFaultCode(), SoapFaultDefinition.SERVER.getLocalPart());
        Assert.assertEquals(container.getFaultString(), INTERNAL_SERVER_ERROR);
        Assert.assertEquals(container.getFaultDetails().size(), 0L);
        Assert.assertEquals(container.getFaultDetailResourcePaths().size(), 1L);
        Assert.assertEquals(container.getFaultDetailResourcePaths().get(0), "classpath:org/citrusframework/ws/actions/dsl/soap-fault-detail.xml");

    }

    @Test
    public void testMultipleFaultDetailsInlineAndResource() throws IOException {
        reset(resource, referenceResolver, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);
        when(resource.exists()).thenReturn(true);
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

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.isResolvable(SOAP_FAULT_VALIDATOR)).thenReturn(false);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(soap().client("soapClient")
                        .assertFault()
                                .faultCode(SoapFaultDefinition.SERVER.getLocalPart())
                                .faultString(INTERNAL_SERVER_ERROR)
                                .faultDetail("<ErrorDetail><code>1001</code></ErrorDetail>")
                                .faultDetailResource(resource)
                        .when(action(context -> {
                            throw new SoapFaultClientException(soapMessage);
                        })));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "soap:assert-fault");

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
        reset(referenceResolver, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);

        when(soapMessage.getSoapBody()).thenReturn(soapBody);
        when(soapMessage.getFaultReason()).thenReturn(INTERNAL_SERVER_ERROR);
        when(soapBody.getFault()).thenReturn(soapFault);

        when(soapFault.getFaultActorOrRole()).thenReturn(SoapFaultDefinition.SERVER.getLocalPart());
        when(soapFault.getFaultCode()).thenReturn(SoapFaultDefinition.SERVER);
        when(soapFault.getFaultStringOrReason()).thenReturn(INTERNAL_SERVER_ERROR);
        when(soapFault.getFaultDetail()).thenReturn(null);

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.isResolvable(SOAP_FAULT_VALIDATOR)).thenReturn(true);
        when(referenceResolver.resolve(SOAP_FAULT_VALIDATOR, SoapFaultValidator.class)).thenReturn(soapFaultValidator);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(soap().client("soapClient")
                        .assertFault()
                                .faultCode(SoapFaultDefinition.SERVER.getLocalPart())
                                .faultString(INTERNAL_SERVER_ERROR)
                                .validator(soapFaultValidator)
                        .when(action(context -> {
                            throw new SoapFaultClientException(soapMessage);
                        })));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "soap:assert-fault");

        AssertSoapFault container = (AssertSoapFault)(test.getTestAction(0));

        Assert.assertEquals(container.getActionCount(), 1);
        Assert.assertTrue(container.getAction().getClass().isAnonymousClass());
        Assert.assertEquals(container.getFaultCode(), SoapFaultDefinition.SERVER.getLocalPart());
        Assert.assertEquals(container.getFaultString(), INTERNAL_SERVER_ERROR);
        Assert.assertEquals(container.getValidator(), soapFaultValidator);

        verify(soapFaultValidator).validateSoapFault(any(org.citrusframework.ws.message.SoapFault.class), any(org.citrusframework.ws.message.SoapFault.class),
                any(TestContext.class), any(SoapFaultValidationContext.class));
    }

    @Test
    public void testAssertSoapFaultBuilderWithActor() {
        reset(referenceResolver, soapMessage, soapFaultValidator, soapBody, soapFault, soapFaultDetail, soapFaultDetailElement);

        when(soapMessage.getSoapBody()).thenReturn(soapBody);
        when(soapMessage.getFaultReason()).thenReturn(INTERNAL_SERVER_ERROR);
        when(soapBody.getFault()).thenReturn(soapFault);

        when(soapFault.getFaultActorOrRole()).thenReturn("MyActor");
        when(soapFault.getFaultCode()).thenReturn(SoapFaultDefinition.SERVER);
        when(soapFault.getFaultStringOrReason()).thenReturn(INTERNAL_SERVER_ERROR);
        when(soapFault.getFaultDetail()).thenReturn(null);

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.isResolvable(SOAP_FAULT_VALIDATOR)).thenReturn(false);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(soap().client("soapClient")
                        .assertFault()
                                .faultCode(SoapFaultDefinition.SERVER.getLocalPart())
                                .faultString(INTERNAL_SERVER_ERROR)
                                .faultActor("MyActor")
                        .when(action(context -> {
                            throw new SoapFaultClientException(soapMessage);
                        })));

        TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "soap:assert-fault");

        AssertSoapFault container = (AssertSoapFault)(test.getTestAction(0));

        Assert.assertEquals(container.getActionCount(), 1);
        Assert.assertTrue(container.getAction().getClass().isAnonymousClass());
        Assert.assertEquals(container.getFaultCode(), SoapFaultDefinition.SERVER.getLocalPart());
        Assert.assertEquals(container.getFaultString(), INTERNAL_SERVER_ERROR);
        Assert.assertEquals(container.getFaultActor(), "MyActor");

    }
}
