/*
 * Copyright 2006-2012 the original author or authors.
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
import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.container.SequenceAfterTest;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.ws.actions.AssertSoapFault;
import com.consol.citrus.ws.validation.SoapFaultValidator;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;

import static org.mockito.Mockito.*;


public class AssertSoapFaultTestDesignerTest extends AbstractTestNGUnitTest {

    public static final String SOAP_FAULT_VALIDATOR = "soapFaultValidator";
    public static final String INTERNAL_SERVER_ERROR = "Internal server error";
    public static final String SOAP_ENV_SERVER_ERROR = "SOAP-ENV:Server";

    private Resource resource = Mockito.mock(Resource.class);
    private SoapFaultValidator soapFaultValidator = Mockito.mock(SoapFaultValidator.class);
    private ApplicationContext applicationContextMock = Mockito.mock(ApplicationContext.class);

    @Test
    public void testAssertSoapFaultBuilder() {
        reset(applicationContextMock);

        when(applicationContextMock.containsBean(SOAP_FAULT_VALIDATOR)).thenReturn(true);
        when(applicationContextMock.getBean(SOAP_FAULT_VALIDATOR, SoapFaultValidator.class)).thenReturn(soapFaultValidator);
        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());

        MockTestDesigner builder = new MockTestDesigner(applicationContextMock, context) {
            @Override
            public void configure() {
                assertSoapFault()
                    .faultCode(SOAP_ENV_SERVER_ERROR)
                    .faultString(INTERNAL_SERVER_ERROR)
                .when(echo("${foo}"));
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "soap-fault");
        
        AssertSoapFault container = (AssertSoapFault)(test.getTestAction(0));
        
        Assert.assertEquals(container.getActionCount(), 1);
        Assert.assertEquals(container.getAction().getClass(), EchoAction.class);
        Assert.assertEquals(container.getFaultCode(), SOAP_ENV_SERVER_ERROR);
        Assert.assertEquals(container.getFaultString(), INTERNAL_SERVER_ERROR);
        Assert.assertEquals(((EchoAction)(container.getAction())).getMessage(), "${foo}");

    }
    
    @Test
    public void testFaultDetail() {
        reset(applicationContextMock);

        when(applicationContextMock.containsBean(SOAP_FAULT_VALIDATOR)).thenReturn(true);
        when(applicationContextMock.getBean(SOAP_FAULT_VALIDATOR, SoapFaultValidator.class)).thenReturn(soapFaultValidator);
        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());

        MockTestDesigner builder = new MockTestDesigner(applicationContextMock, context) {
            @Override
            public void configure() {
                assertSoapFault()
                    .faultCode(SOAP_ENV_SERVER_ERROR)
                    .faultString(INTERNAL_SERVER_ERROR)
                    .faultDetail("<ErrorDetail><message>FooBar</message></ErrorDetail>")
                .when(echo("${foo}"));
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "soap-fault");
        
        AssertSoapFault container = (AssertSoapFault)(test.getTestAction(0));
        
        Assert.assertEquals(container.getActionCount(), 1);
        Assert.assertEquals(container.getAction().getClass(), EchoAction.class);
        Assert.assertEquals(container.getFaultCode(), SOAP_ENV_SERVER_ERROR);
        Assert.assertEquals(container.getFaultString(), INTERNAL_SERVER_ERROR);
        Assert.assertEquals(container.getFaultDetails().size(), 1L);
        Assert.assertEquals(container.getFaultDetails().get(0), "<ErrorDetail><message>FooBar</message></ErrorDetail>");
        Assert.assertEquals(((EchoAction)(container.getAction())).getMessage(), "${foo}");

    }
    
    @Test
    public void testMultipleFaultDetails() {
        reset(applicationContextMock);

        when(applicationContextMock.containsBean(SOAP_FAULT_VALIDATOR)).thenReturn(true);
        when(applicationContextMock.getBean(SOAP_FAULT_VALIDATOR, SoapFaultValidator.class)).thenReturn(soapFaultValidator);
        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());

        MockTestDesigner builder = new MockTestDesigner(applicationContextMock, context) {
            @Override
            public void configure() {
                assertSoapFault()
                    .faultCode(SOAP_ENV_SERVER_ERROR)
                    .faultString(INTERNAL_SERVER_ERROR)
                    .faultDetail("<ErrorDetail><code>1001</code></ErrorDetail>")
                    .faultDetail("<MessageDetail><message>FooBar</message></MessageDetail>")
                .when(echo("${foo}"));
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "soap-fault");
        
        AssertSoapFault container = (AssertSoapFault)(test.getTestAction(0));
        
        Assert.assertEquals(container.getActionCount(), 1);
        Assert.assertEquals(container.getAction().getClass(), EchoAction.class);
        Assert.assertEquals(container.getFaultCode(), SOAP_ENV_SERVER_ERROR);
        Assert.assertEquals(container.getFaultString(), INTERNAL_SERVER_ERROR);
        Assert.assertEquals(container.getFaultDetails().size(), 2L);
        Assert.assertEquals(container.getFaultDetails().get(0), "<ErrorDetail><code>1001</code></ErrorDetail>");
        Assert.assertEquals(container.getFaultDetails().get(1), "<MessageDetail><message>FooBar</message></MessageDetail>");
        Assert.assertEquals(((EchoAction)(container.getAction())).getMessage(), "${foo}");

    }
    
    @Test
    public void testFaultDetailResource() throws IOException {
        reset(resource, applicationContextMock);

        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("<ErrorDetail><message>FooBar</message></ErrorDetail>".getBytes()));
        when(applicationContextMock.containsBean(SOAP_FAULT_VALIDATOR)).thenReturn(true);
        when(applicationContextMock.getBean(SOAP_FAULT_VALIDATOR, SoapFaultValidator.class)).thenReturn(soapFaultValidator);
        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());

        MockTestDesigner builder = new MockTestDesigner(applicationContextMock, context) {
            @Override
            public void configure() {
                assertSoapFault()
                    .faultCode(SOAP_ENV_SERVER_ERROR)
                    .faultString(INTERNAL_SERVER_ERROR)
                    .faultDetailResource(resource)
                .when(echo("${foo}"));
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "soap-fault");
        
        AssertSoapFault container = (AssertSoapFault)(test.getTestAction(0));
        
        Assert.assertEquals(container.getActionCount(), 1);
        Assert.assertEquals(container.getAction().getClass(), EchoAction.class);
        Assert.assertEquals(container.getFaultCode(), SOAP_ENV_SERVER_ERROR);
        Assert.assertEquals(container.getFaultString(), INTERNAL_SERVER_ERROR);
        Assert.assertEquals(container.getFaultDetails().size(), 1L);
        Assert.assertEquals(container.getFaultDetails().get(0), "<ErrorDetail><message>FooBar</message></ErrorDetail>");
        Assert.assertEquals(((EchoAction)(container.getAction())).getMessage(), "${foo}");

    }

    @Test
    public void testFaultDetailResourcePath() {
        reset(applicationContextMock);

        when(applicationContextMock.containsBean(SOAP_FAULT_VALIDATOR)).thenReturn(true);
        when(applicationContextMock.getBean(SOAP_FAULT_VALIDATOR, SoapFaultValidator.class)).thenReturn(soapFaultValidator);
        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());

        MockTestDesigner builder = new MockTestDesigner(applicationContextMock, context) {
            @Override
            public void configure() {
                assertSoapFault()
                        .faultCode(SOAP_ENV_SERVER_ERROR)
                        .faultString(INTERNAL_SERVER_ERROR)
                        .faultDetailResource("com/consol/citrus/soap/fault.xml")
                .when(echo("${foo}"));
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "soap-fault");

        AssertSoapFault container = (AssertSoapFault)(test.getTestAction(0));

        Assert.assertEquals(container.getActionCount(), 1);
        Assert.assertEquals(container.getAction().getClass(), EchoAction.class);
        Assert.assertEquals(container.getFaultCode(), SOAP_ENV_SERVER_ERROR);
        Assert.assertEquals(container.getFaultString(), INTERNAL_SERVER_ERROR);
        Assert.assertEquals(container.getFaultDetails().size(), 0L);
        Assert.assertEquals(container.getFaultDetailResourcePaths().size(), 1L);
        Assert.assertEquals(container.getFaultDetailResourcePaths().get(0), "com/consol/citrus/soap/fault.xml");
        Assert.assertEquals(((EchoAction)(container.getAction())).getMessage(), "${foo}");

    }
    
    @Test
    public void testMultipleFaultDetailsInlineAndResource() throws IOException {
        reset(resource, applicationContextMock);

        when(resource.getInputStream()).thenReturn(new ByteArrayInputStream("<MessageDetail><message>FooBar</message></MessageDetail>".getBytes()));
        when(applicationContextMock.containsBean(SOAP_FAULT_VALIDATOR)).thenReturn(true);
        when(applicationContextMock.getBean(SOAP_FAULT_VALIDATOR, SoapFaultValidator.class)).thenReturn(soapFaultValidator);
        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());

        MockTestDesigner builder = new MockTestDesigner(applicationContextMock, context) {
            @Override
            public void configure() {
                assertSoapFault()
                    .faultCode(SOAP_ENV_SERVER_ERROR)
                    .faultString(INTERNAL_SERVER_ERROR)
                    .faultDetail("<ErrorDetail><code>1001</code></ErrorDetail>")
                    .faultDetailResource(resource)
                .when(echo("${foo}"));
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "soap-fault");
        
        AssertSoapFault container = (AssertSoapFault)(test.getTestAction(0));
        
        Assert.assertEquals(container.getActionCount(), 1);
        Assert.assertEquals(container.getAction().getClass(), EchoAction.class);
        Assert.assertEquals(container.getFaultCode(), SOAP_ENV_SERVER_ERROR);
        Assert.assertEquals(container.getFaultString(), INTERNAL_SERVER_ERROR);
        Assert.assertEquals(container.getFaultDetails().size(), 2L);
        Assert.assertEquals(container.getFaultDetails().get(0), "<ErrorDetail><code>1001</code></ErrorDetail>");
        Assert.assertEquals(container.getFaultDetails().get(1), "<MessageDetail><message>FooBar</message></MessageDetail>");
        Assert.assertEquals(((EchoAction)(container.getAction())).getMessage(), "${foo}");

    }
    
    @Test
    public void testAssertSoapFaultBuilderWithValidator() {
        reset(applicationContextMock);

        when(applicationContextMock.containsBean(SOAP_FAULT_VALIDATOR)).thenReturn(true);
        when(applicationContextMock.getBean(SOAP_FAULT_VALIDATOR, SoapFaultValidator.class)).thenReturn(soapFaultValidator);
        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());

        MockTestDesigner builder = new MockTestDesigner(applicationContextMock, context) {
            @Override
            public void configure() {
                assertSoapFault()
                    .faultCode(SOAP_ENV_SERVER_ERROR)
                    .faultString(INTERNAL_SERVER_ERROR)
                    .validator(soapFaultValidator)
                .when(echo("${foo}"));
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "soap-fault");
        
        AssertSoapFault container = (AssertSoapFault)(test.getTestAction(0));
        
        Assert.assertEquals(container.getActionCount(), 1);
        Assert.assertEquals(container.getAction().getClass(), EchoAction.class);
        Assert.assertEquals(container.getFaultCode(), SOAP_ENV_SERVER_ERROR);
        Assert.assertEquals(container.getFaultString(), INTERNAL_SERVER_ERROR);
        Assert.assertEquals(container.getValidator(), soapFaultValidator);
        Assert.assertEquals(((EchoAction)(container.getAction())).getMessage(), "${foo}");

    }
    
    @Test
    public void testAssertSoapFaultBuilderWithActor() {
        reset(applicationContextMock);

        when(applicationContextMock.containsBean(SOAP_FAULT_VALIDATOR)).thenReturn(true);
        when(applicationContextMock.getBean(SOAP_FAULT_VALIDATOR, SoapFaultValidator.class)).thenReturn(soapFaultValidator);
        when(applicationContextMock.getBean(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).thenReturn(new HashMap<String, SequenceBeforeTest>());
        when(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).thenReturn(new HashMap<String, SequenceAfterTest>());

        MockTestDesigner builder = new MockTestDesigner(applicationContextMock, context) {
            @Override
            public void configure() {
                assertSoapFault()
                    .faultCode(SOAP_ENV_SERVER_ERROR)
                    .faultString(INTERNAL_SERVER_ERROR)
                    .faultActor("MyActor")
                .when(echo("${foo}"));
            }
        };

        builder.configure();

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "soap-fault");
        
        AssertSoapFault container = (AssertSoapFault)(test.getTestAction(0));
        
        Assert.assertEquals(container.getActionCount(), 1);
        Assert.assertEquals(container.getAction().getClass(), EchoAction.class);
        Assert.assertEquals(container.getFaultCode(), SOAP_ENV_SERVER_ERROR);
        Assert.assertEquals(container.getFaultString(), INTERNAL_SERVER_ERROR);
        Assert.assertEquals(container.getFaultActor(), "MyActor");
        Assert.assertEquals(((EchoAction)(container.getAction())).getMessage(), "${foo}");

    }
}
