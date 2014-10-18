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

package com.consol.citrus.dsl.definition;

import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.container.SequenceAfterTest;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.report.TestListeners;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.ws.actions.AssertSoapFault;
import com.consol.citrus.ws.validation.SoapFaultValidator;
import org.easymock.EasyMock;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.ws.soap.SoapMessageFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;

import static org.easymock.EasyMock.*;

public class AssertSoapFaultDefinitionTest extends AbstractTestNGUnitTest {
    
    private Resource resource = EasyMock.createMock(Resource.class);
    private SoapFaultValidator soapFaultValidator = EasyMock.createMock(SoapFaultValidator.class);
    private SoapMessageFactory messageFactory = EasyMock.createMock(SoapMessageFactory.class);
    
    private ApplicationContext applicationContextMock = EasyMock.createMock(ApplicationContext.class);
    
    @Test
    public void testAssertSoapFaultBuilder() {
        reset(applicationContextMock);

        expect(applicationContextMock.getBean("soapFaultValidator", SoapFaultValidator.class)).andReturn(soapFaultValidator).once();
        expect(applicationContextMock.getBean(TestListeners.class)).andReturn(new TestListeners()).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();

        replay(applicationContextMock);

        MockBuilder builder = new MockBuilder(applicationContextMock) {
            @Override
            public void configure() {
                assertSoapFault(echo("${foo}"))
                    .faultCode("SOAP-ENV:Server")
                    .faultString("Internal server error");
            }
        };

        builder.execute();
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(builder.testCase().getActions().get(0).getName(), "soap-fault");
        
        AssertSoapFault container = (AssertSoapFault)(builder.testCase().getTestAction(0));
        
        Assert.assertEquals(container.getActions().size(), 1);
        Assert.assertEquals(container.getAction().getClass(), EchoAction.class);
        Assert.assertEquals(container.getFaultCode(), "SOAP-ENV:Server");
        Assert.assertEquals(container.getFaultString(), "Internal server error");
        Assert.assertEquals(((EchoAction)(container.getAction())).getMessage(), "${foo}");
        
        verify(applicationContextMock);
    }
    
    @Test
    public void testFaultDetail() {
        reset(applicationContextMock);

        expect(applicationContextMock.getBean("soapFaultValidator", SoapFaultValidator.class)).andReturn(soapFaultValidator).once();
        expect(applicationContextMock.getBean(TestListeners.class)).andReturn(new TestListeners()).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();

        replay(applicationContextMock);

        MockBuilder builder = new MockBuilder(applicationContextMock) {
            @Override
            public void configure() {
                assertSoapFault(echo("${foo}"))
                    .faultCode("SOAP-ENV:Server")
                    .faultString("Internal server error")
                    .faultDetail("<ErrorDetail><message>FooBar</message></ErrorDetail>");
            }
        };

        builder.execute();
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(builder.testCase().getActions().get(0).getName(), "soap-fault");
        
        AssertSoapFault container = (AssertSoapFault)(builder.testCase().getTestAction(0));
        
        Assert.assertEquals(container.getActions().size(), 1);
        Assert.assertEquals(container.getAction().getClass(), EchoAction.class);
        Assert.assertEquals(container.getFaultCode(), "SOAP-ENV:Server");
        Assert.assertEquals(container.getFaultString(), "Internal server error");
        Assert.assertEquals(container.getFaultDetails().size(), 1L);
        Assert.assertEquals(container.getFaultDetails().get(0), "<ErrorDetail><message>FooBar</message></ErrorDetail>");
        Assert.assertEquals(((EchoAction)(container.getAction())).getMessage(), "${foo}");
        
        verify(applicationContextMock);
    }
    
    @Test
    public void testMultipleFaultDetails() {
        reset(applicationContextMock);

        expect(applicationContextMock.getBean("soapFaultValidator", SoapFaultValidator.class)).andReturn(soapFaultValidator).once();
        expect(applicationContextMock.getBean(TestListeners.class)).andReturn(new TestListeners()).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();

        replay(applicationContextMock);

        MockBuilder builder = new MockBuilder(applicationContextMock) {
            @Override
            public void configure() {
                assertSoapFault(echo("${foo}"))
                    .faultCode("SOAP-ENV:Server")
                    .faultString("Internal server error")
                    .faultDetail("<ErrorDetail><code>1001</code></ErrorDetail>")
                    .faultDetail("<MessageDetail><message>FooBar</message></MessageDetail>");
            }
        };

        builder.execute();
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(builder.testCase().getActions().get(0).getName(), "soap-fault");
        
        AssertSoapFault container = (AssertSoapFault)(builder.testCase().getTestAction(0));
        
        Assert.assertEquals(container.getActions().size(), 1);
        Assert.assertEquals(container.getAction().getClass(), EchoAction.class);
        Assert.assertEquals(container.getFaultCode(), "SOAP-ENV:Server");
        Assert.assertEquals(container.getFaultString(), "Internal server error");
        Assert.assertEquals(container.getFaultDetails().size(), 2L);
        Assert.assertEquals(container.getFaultDetails().get(0), "<ErrorDetail><code>1001</code></ErrorDetail>");
        Assert.assertEquals(container.getFaultDetails().get(1), "<MessageDetail><message>FooBar</message></MessageDetail>");
        Assert.assertEquals(((EchoAction)(container.getAction())).getMessage(), "${foo}");
        
        verify(applicationContextMock);
    }
    
    @Test
    public void testFaultDetailResource() throws IOException {
        reset(resource, applicationContextMock);

        expect(resource.getInputStream()).andReturn(new ByteArrayInputStream("<ErrorDetail><message>FooBar</message</ErrorDetail>".getBytes())).once();
        expect(applicationContextMock.getBean("soapFaultValidator", SoapFaultValidator.class)).andReturn(soapFaultValidator).once();
        expect(applicationContextMock.getBean(TestListeners.class)).andReturn(new TestListeners()).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();

        replay(resource, applicationContextMock);

        MockBuilder builder = new MockBuilder(applicationContextMock) {
            @Override
            public void configure() {
                assertSoapFault(echo("${foo}"))
                    .faultCode("SOAP-ENV:Server")
                    .faultString("Internal server error")
                    .faultDetailResource(resource);
            }
        };

        builder.execute();
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(builder.testCase().getActions().get(0).getName(), "soap-fault");
        
        AssertSoapFault container = (AssertSoapFault)(builder.testCase().getTestAction(0));
        
        Assert.assertEquals(container.getActions().size(), 1);
        Assert.assertEquals(container.getAction().getClass(), EchoAction.class);
        Assert.assertEquals(container.getFaultCode(), "SOAP-ENV:Server");
        Assert.assertEquals(container.getFaultString(), "Internal server error");
        Assert.assertEquals(container.getFaultDetails().size(), 1L);
        Assert.assertEquals(container.getFaultDetails().get(0), "<ErrorDetail><message>FooBar</message</ErrorDetail>");
        Assert.assertEquals(((EchoAction)(container.getAction())).getMessage(), "${foo}");
        
        verify(resource, applicationContextMock);
    }

    @Test
    public void testFaultDetailResourcePath() {
        reset(applicationContextMock);

        expect(applicationContextMock.getBean("soapFaultValidator", SoapFaultValidator.class)).andReturn(soapFaultValidator).once();
        expect(applicationContextMock.getBean(TestListeners.class)).andReturn(new TestListeners()).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();

        replay(applicationContextMock);

        MockBuilder builder = new MockBuilder(applicationContextMock) {
            @Override
            public void configure() {
                assertSoapFault(echo("${foo}"))
                        .faultCode("SOAP-ENV:Server")
                        .faultString("Internal server error")
                        .faultDetailResource("com/consol/citrus/soap/fault.xml");
            }
        };

        builder.execute();

        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(builder.testCase().getActions().get(0).getName(), "soap-fault");

        AssertSoapFault container = (AssertSoapFault)(builder.testCase().getTestAction(0));

        Assert.assertEquals(container.getActions().size(), 1);
        Assert.assertEquals(container.getAction().getClass(), EchoAction.class);
        Assert.assertEquals(container.getFaultCode(), "SOAP-ENV:Server");
        Assert.assertEquals(container.getFaultString(), "Internal server error");
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
        expect(applicationContextMock.getBean("soapFaultValidator", SoapFaultValidator.class)).andReturn(soapFaultValidator).once();
        expect(applicationContextMock.getBean(TestListeners.class)).andReturn(new TestListeners()).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();

        replay(resource, applicationContextMock);

        MockBuilder builder = new MockBuilder(applicationContextMock) {
            @Override
            public void configure() {
                assertSoapFault(echo("${foo}"))
                    .faultCode("SOAP-ENV:Server")
                    .faultString("Internal server error")
                    .faultDetail("<ErrorDetail><code>1001</code></ErrorDetail>")
                    .faultDetailResource(resource);
            }
        };

        builder.execute();
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(builder.testCase().getActions().get(0).getName(), "soap-fault");
        
        AssertSoapFault container = (AssertSoapFault)(builder.testCase().getTestAction(0));
        
        Assert.assertEquals(container.getActions().size(), 1);
        Assert.assertEquals(container.getAction().getClass(), EchoAction.class);
        Assert.assertEquals(container.getFaultCode(), "SOAP-ENV:Server");
        Assert.assertEquals(container.getFaultString(), "Internal server error");
        Assert.assertEquals(container.getFaultDetails().size(), 2L);
        Assert.assertEquals(container.getFaultDetails().get(0), "<ErrorDetail><code>1001</code></ErrorDetail>");
        Assert.assertEquals(container.getFaultDetails().get(1), "<MessageDetail><message>FooBar</message></MessageDetail>");
        Assert.assertEquals(((EchoAction)(container.getAction())).getMessage(), "${foo}");
        
        verify(resource, applicationContextMock);
    }
    
    @Test
    public void testAssertSoapFaultBuilderWithValidator() {
        reset(applicationContextMock);

        expect(applicationContextMock.getBean("soapFaultValidator", SoapFaultValidator.class)).andReturn(soapFaultValidator).once();
        expect(applicationContextMock.getBean(TestListeners.class)).andReturn(new TestListeners()).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();

        replay(applicationContextMock);

        MockBuilder builder = new MockBuilder(applicationContextMock) {
            @Override
            public void configure() {
                assertSoapFault(echo("${foo}"))
                    .faultCode("SOAP-ENV:Server")
                    .faultString("Internal server error")
                    .validator(soapFaultValidator);
            }
        };

        builder.execute();
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(builder.testCase().getActions().get(0).getName(), "soap-fault");
        
        AssertSoapFault container = (AssertSoapFault)(builder.testCase().getTestAction(0));
        
        Assert.assertEquals(container.getActions().size(), 1);
        Assert.assertEquals(container.getAction().getClass(), EchoAction.class);
        Assert.assertEquals(container.getFaultCode(), "SOAP-ENV:Server");
        Assert.assertEquals(container.getFaultString(), "Internal server error");
        Assert.assertEquals(container.getValidator(), soapFaultValidator);
        Assert.assertEquals(((EchoAction)(container.getAction())).getMessage(), "${foo}");
        
        verify(applicationContextMock);
    }
    
    @Test
    public void testAssertSoapFaultBuilderWithMessageFactory() {
        reset(applicationContextMock);

        expect(applicationContextMock.getBean("soapFaultValidator", SoapFaultValidator.class)).andReturn(soapFaultValidator).once();
        expect(applicationContextMock.getBean(TestListeners.class)).andReturn(new TestListeners()).once();
        expect(applicationContextMock.getBean(TestActionListeners.class)).andReturn(new TestActionListeners()).once();
        expect(applicationContextMock.getBeansOfType(SequenceBeforeTest.class)).andReturn(new HashMap<String, SequenceBeforeTest>()).once();
        expect(applicationContextMock.getBeansOfType(SequenceAfterTest.class)).andReturn(new HashMap<String, SequenceAfterTest>()).once();

        replay(applicationContextMock);

        MockBuilder builder = new MockBuilder(applicationContextMock) {
            @Override
            public void configure() {
                assertSoapFault(echo("${foo}"))
                    .faultCode("SOAP-ENV:Server")
                    .faultString("Internal server error")
                    .faultActor("MyActor");
            }
        };

        builder.execute();
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(builder.testCase().getActions().get(0).getName(), "soap-fault");
        
        AssertSoapFault container = (AssertSoapFault)(builder.testCase().getTestAction(0));
        
        Assert.assertEquals(container.getActions().size(), 1);
        Assert.assertEquals(container.getAction().getClass(), EchoAction.class);
        Assert.assertEquals(container.getFaultCode(), "SOAP-ENV:Server");
        Assert.assertEquals(container.getFaultString(), "Internal server error");
        Assert.assertEquals(container.getFaultActor(), "MyActor");
        Assert.assertEquals(((EchoAction)(container.getAction())).getMessage(), "${foo}");
        
        verify(applicationContextMock);
    }
}
