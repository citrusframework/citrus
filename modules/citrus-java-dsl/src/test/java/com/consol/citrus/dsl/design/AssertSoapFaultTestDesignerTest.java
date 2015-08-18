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
import org.easymock.EasyMock;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;

import static org.easymock.EasyMock.*;

public class AssertSoapFaultTestDesignerTest extends AbstractTestNGUnitTest {

    public static final String SOAP_FAULT_VALIDATOR = "soapFaultValidator";
    public static final String INTERNAL_SERVER_ERROR = "Internal server error";
    public static final String SOAP_ENV_SERVER_ERROR = "SOAP-ENV:Server";

    private Resource resource = EasyMock.createMock(Resource.class);
    private SoapFaultValidator soapFaultValidator = EasyMock.createMock(SoapFaultValidator.class);
    private ApplicationContext applicationContextMock = EasyMock.createMock(ApplicationContext.class);

    @Test
    public void testAssertSoapFaultBuilderNested() {
        reset(applicationContextMock);

        expect(applicationContextMock.containsBean(SOAP_FAULT_VALIDATOR)).andReturn(true).once();
        expect(applicationContextMock.getBean(SOAP_FAULT_VALIDATOR, SoapFaultValidator.class)).andReturn(soapFaultValidator).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();

        replay(applicationContextMock);

        MockTestDesigner builder = new MockTestDesigner(applicationContextMock) {
            @Override
            public void configure() {
                assertSoapFault(echo("${foo}"))
                        .faultCode(SOAP_ENV_SERVER_ERROR)
                        .faultString(INTERNAL_SERVER_ERROR);
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

        verify(applicationContextMock);
    }

    @Test
    public void testAssertSoapFaultBuilder() {
        reset(applicationContextMock);

        expect(applicationContextMock.containsBean(SOAP_FAULT_VALIDATOR)).andReturn(true).once();
        expect(applicationContextMock.getBean(SOAP_FAULT_VALIDATOR, SoapFaultValidator.class)).andReturn(soapFaultValidator).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();

        replay(applicationContextMock);

        MockTestDesigner builder = new MockTestDesigner(applicationContextMock) {
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
        
        verify(applicationContextMock);
    }
    
    @Test
    public void testFaultDetail() {
        reset(applicationContextMock);

        expect(applicationContextMock.containsBean(SOAP_FAULT_VALIDATOR)).andReturn(true).once();
        expect(applicationContextMock.getBean(SOAP_FAULT_VALIDATOR, SoapFaultValidator.class)).andReturn(soapFaultValidator).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();

        replay(applicationContextMock);

        MockTestDesigner builder = new MockTestDesigner(applicationContextMock) {
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
        
        verify(applicationContextMock);
    }
    
    @Test
    public void testMultipleFaultDetails() {
        reset(applicationContextMock);

        expect(applicationContextMock.containsBean(SOAP_FAULT_VALIDATOR)).andReturn(true).once();
        expect(applicationContextMock.getBean(SOAP_FAULT_VALIDATOR, SoapFaultValidator.class)).andReturn(soapFaultValidator).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();

        replay(applicationContextMock);

        MockTestDesigner builder = new MockTestDesigner(applicationContextMock) {
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
        
        verify(applicationContextMock);
    }
    
    @Test
    public void testFaultDetailResource() throws IOException {
        reset(resource, applicationContextMock);

        expect(resource.getInputStream()).andReturn(new ByteArrayInputStream("<ErrorDetail><message>FooBar</message></ErrorDetail>".getBytes())).once();
        expect(applicationContextMock.containsBean(SOAP_FAULT_VALIDATOR)).andReturn(true).once();
        expect(applicationContextMock.getBean(SOAP_FAULT_VALIDATOR, SoapFaultValidator.class)).andReturn(soapFaultValidator).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();

        replay(resource, applicationContextMock);

        MockTestDesigner builder = new MockTestDesigner(applicationContextMock) {
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
        
        verify(resource, applicationContextMock);
    }

    @Test
    public void testFaultDetailResourcePath() {
        reset(applicationContextMock);

        expect(applicationContextMock.containsBean(SOAP_FAULT_VALIDATOR)).andReturn(true).once();
        expect(applicationContextMock.getBean(SOAP_FAULT_VALIDATOR, SoapFaultValidator.class)).andReturn(soapFaultValidator).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();

        replay(applicationContextMock);

        MockTestDesigner builder = new MockTestDesigner(applicationContextMock) {
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

        verify(applicationContextMock);
    }
    
    @Test
    public void testMultipleFaultDetailsInlineAndResource() throws IOException {
        reset(resource, applicationContextMock);

        expect(resource.getInputStream()).andReturn(new ByteArrayInputStream("<MessageDetail><message>FooBar</message></MessageDetail>".getBytes())).once();
        expect(applicationContextMock.containsBean(SOAP_FAULT_VALIDATOR)).andReturn(true).once();
        expect(applicationContextMock.getBean(SOAP_FAULT_VALIDATOR, SoapFaultValidator.class)).andReturn(soapFaultValidator).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();

        replay(resource, applicationContextMock);

        MockTestDesigner builder = new MockTestDesigner(applicationContextMock) {
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
        
        verify(resource, applicationContextMock);
    }
    
    @Test
    public void testAssertSoapFaultBuilderWithValidator() {
        reset(applicationContextMock);

        expect(applicationContextMock.containsBean(SOAP_FAULT_VALIDATOR)).andReturn(true).once();
        expect(applicationContextMock.getBean(SOAP_FAULT_VALIDATOR, SoapFaultValidator.class)).andReturn(soapFaultValidator).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();

        replay(applicationContextMock);

        MockTestDesigner builder = new MockTestDesigner(applicationContextMock) {
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
        
        verify(applicationContextMock);
    }
    
    @Test
    public void testAssertSoapFaultBuilderWithActor() {
        reset(applicationContextMock);

        expect(applicationContextMock.containsBean(SOAP_FAULT_VALIDATOR)).andReturn(true).once();
        expect(applicationContextMock.getBean(SOAP_FAULT_VALIDATOR, SoapFaultValidator.class)).andReturn(soapFaultValidator).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();

        replay(applicationContextMock);

        MockTestDesigner builder = new MockTestDesigner(applicationContextMock) {
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
        
        verify(applicationContextMock);
    }
}
