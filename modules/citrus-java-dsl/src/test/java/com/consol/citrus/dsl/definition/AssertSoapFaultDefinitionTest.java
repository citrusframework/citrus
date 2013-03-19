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

import static org.easymock.EasyMock.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.easymock.EasyMock;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.ws.soap.SoapMessageFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.ws.actions.AssertSoapFault;
import com.consol.citrus.ws.validation.SoapFaultValidator;

public class AssertSoapFaultDefinitionTest {
    
    private Resource resource = EasyMock.createMock(Resource.class);
    private SoapFaultValidator soapFaultValidator = EasyMock.createMock(SoapFaultValidator.class);
    private SoapMessageFactory messageFactory = EasyMock.createMock(SoapMessageFactory.class);
    
    private ApplicationContext applicationContext = EasyMock.createMock(ApplicationContext.class);
    
    @Test
    public void testAssertSoapFaultBuilder() {
        MockBuilder builder = new MockBuilder() {
            @Override
            public void configure() {
                assertSoapFault(echo("${foo}"))
                    .faultCode("SOAP-ENV:Server")
                    .faultString("Internal server error");
            }
        };

        builder.setApplicationContext(applicationContext);
        
        reset(applicationContext);
        
        expect(applicationContext.getBean("messageFactory", SoapMessageFactory.class)).andReturn(messageFactory).once();
        expect(applicationContext.getBean("soapFaultValidator", SoapFaultValidator.class)).andReturn(soapFaultValidator).once();
        
        replay(applicationContext);
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(builder.testCase().getActions().get(0).getName(), AssertSoapFault.class.getSimpleName());
        
        AssertSoapFault container = (AssertSoapFault)(builder.testCase().getTestAction(0));
        
        Assert.assertEquals(container.getActions().size(), 1);
        Assert.assertEquals(container.getAction().getClass(), EchoAction.class);
        Assert.assertEquals(container.getFaultCode(), "SOAP-ENV:Server");
        Assert.assertEquals(container.getFaultString(), "Internal server error");
        Assert.assertEquals(((EchoAction)(container.getAction())).getMessage(), "${foo}");
        
        verify(applicationContext);
    }
    
    @Test
    public void testFaultDetail() {
        MockBuilder builder = new MockBuilder() {
            @Override
            public void configure() {
                assertSoapFault(echo("${foo}"))
                    .faultCode("SOAP-ENV:Server")
                    .faultString("Internal server error")
                    .faultDetail("<ErrorDetail><message>FooBar</message></ErrorDetail>");
            }
        };
        
        builder.setApplicationContext(applicationContext);
        
        reset(applicationContext);
        
        expect(applicationContext.getBean("messageFactory", SoapMessageFactory.class)).andReturn(messageFactory).once();
        expect(applicationContext.getBean("soapFaultValidator", SoapFaultValidator.class)).andReturn(soapFaultValidator).once();
        
        replay(applicationContext);
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(builder.testCase().getActions().get(0).getName(), AssertSoapFault.class.getSimpleName());
        
        AssertSoapFault container = (AssertSoapFault)(builder.testCase().getTestAction(0));
        
        Assert.assertEquals(container.getActions().size(), 1);
        Assert.assertEquals(container.getAction().getClass(), EchoAction.class);
        Assert.assertEquals(container.getFaultCode(), "SOAP-ENV:Server");
        Assert.assertEquals(container.getFaultString(), "Internal server error");
        Assert.assertEquals(container.getFaultDetails().size(), 1L);
        Assert.assertEquals(container.getFaultDetails().get(0), "<ErrorDetail><message>FooBar</message></ErrorDetail>");
        Assert.assertEquals(((EchoAction)(container.getAction())).getMessage(), "${foo}");
        
        verify(applicationContext);
    }
    
    @Test
    public void testMultipleFaultDetails() {
        MockBuilder builder = new MockBuilder() {
            @Override
            public void configure() {
                assertSoapFault(echo("${foo}"))
                    .faultCode("SOAP-ENV:Server")
                    .faultString("Internal server error")
                    .faultDetail("<ErrorDetail><code>1001</code></ErrorDetail>")
                    .faultDetail("<MessageDetail><message>FooBar</message></MessageDetail>");
            }
        };
        
        builder.setApplicationContext(applicationContext);
        
        reset(applicationContext);
        
        expect(applicationContext.getBean("messageFactory", SoapMessageFactory.class)).andReturn(messageFactory).once();
        expect(applicationContext.getBean("soapFaultValidator", SoapFaultValidator.class)).andReturn(soapFaultValidator).once();
        
        replay(applicationContext);
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(builder.testCase().getActions().get(0).getName(), AssertSoapFault.class.getSimpleName());
        
        AssertSoapFault container = (AssertSoapFault)(builder.testCase().getTestAction(0));
        
        Assert.assertEquals(container.getActions().size(), 1);
        Assert.assertEquals(container.getAction().getClass(), EchoAction.class);
        Assert.assertEquals(container.getFaultCode(), "SOAP-ENV:Server");
        Assert.assertEquals(container.getFaultString(), "Internal server error");
        Assert.assertEquals(container.getFaultDetails().size(), 2L);
        Assert.assertEquals(container.getFaultDetails().get(0), "<ErrorDetail><code>1001</code></ErrorDetail>");
        Assert.assertEquals(container.getFaultDetails().get(1), "<MessageDetail><message>FooBar</message></MessageDetail>");
        Assert.assertEquals(((EchoAction)(container.getAction())).getMessage(), "${foo}");
        
        verify(applicationContext);
    }
    
    @Test
    public void testFaultDetailResource() throws IOException {
        MockBuilder builder = new MockBuilder() {
            @Override
            public void configure() {
                assertSoapFault(echo("${foo}"))
                    .faultCode("SOAP-ENV:Server")
                    .faultString("Internal server error")
                    .faultDetailResource(resource);
            }
        };
        
        builder.setApplicationContext(applicationContext);
        
        reset(resource, applicationContext);

        expect(resource.getInputStream()).andReturn(new ByteArrayInputStream("<ErrorDetail><message>FooBar</message</ErrorDetail>".getBytes())).once();
        expect(applicationContext.getBean("messageFactory", SoapMessageFactory.class)).andReturn(messageFactory).once();
        expect(applicationContext.getBean("soapFaultValidator", SoapFaultValidator.class)).andReturn(soapFaultValidator).once();
        
        replay(resource, applicationContext);
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(builder.testCase().getActions().get(0).getName(), AssertSoapFault.class.getSimpleName());
        
        AssertSoapFault container = (AssertSoapFault)(builder.testCase().getTestAction(0));
        
        Assert.assertEquals(container.getActions().size(), 1);
        Assert.assertEquals(container.getAction().getClass(), EchoAction.class);
        Assert.assertEquals(container.getFaultCode(), "SOAP-ENV:Server");
        Assert.assertEquals(container.getFaultString(), "Internal server error");
        Assert.assertEquals(container.getFaultDetails().size(), 1L);
        Assert.assertEquals(container.getFaultDetails().get(0), "<ErrorDetail><message>FooBar</message</ErrorDetail>");
        Assert.assertEquals(((EchoAction)(container.getAction())).getMessage(), "${foo}");
        
        verify(resource, applicationContext);
    }
    
    @Test
    public void testMultipleFaultDetailsInlineAndResource() throws IOException {
        MockBuilder builder = new MockBuilder() {
            @Override
            public void configure() {
                assertSoapFault(echo("${foo}"))
                    .faultCode("SOAP-ENV:Server")
                    .faultString("Internal server error")
                    .faultDetail("<ErrorDetail><code>1001</code></ErrorDetail>")
                    .faultDetailResource(resource);
            }
        };
        
        builder.setApplicationContext(applicationContext);
        
        reset(resource, applicationContext);

        expect(resource.getInputStream()).andReturn(new ByteArrayInputStream("<MessageDetail><message>FooBar</message></MessageDetail>".getBytes())).once();
        expect(applicationContext.getBean("messageFactory", SoapMessageFactory.class)).andReturn(messageFactory).once();
        expect(applicationContext.getBean("soapFaultValidator", SoapFaultValidator.class)).andReturn(soapFaultValidator).once();
        
        replay(resource, applicationContext);
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(builder.testCase().getActions().get(0).getName(), AssertSoapFault.class.getSimpleName());
        
        AssertSoapFault container = (AssertSoapFault)(builder.testCase().getTestAction(0));
        
        Assert.assertEquals(container.getActions().size(), 1);
        Assert.assertEquals(container.getAction().getClass(), EchoAction.class);
        Assert.assertEquals(container.getFaultCode(), "SOAP-ENV:Server");
        Assert.assertEquals(container.getFaultString(), "Internal server error");
        Assert.assertEquals(container.getFaultDetails().size(), 2L);
        Assert.assertEquals(container.getFaultDetails().get(0), "<ErrorDetail><code>1001</code></ErrorDetail>");
        Assert.assertEquals(container.getFaultDetails().get(1), "<MessageDetail><message>FooBar</message></MessageDetail>");
        Assert.assertEquals(((EchoAction)(container.getAction())).getMessage(), "${foo}");
        
        verify(resource, applicationContext);
    }
    
    @Test
    public void testAssertSoapFaultBuilderWithValidator() {
        MockBuilder builder = new MockBuilder() {
            @Override
            public void configure() {
                assertSoapFault(echo("${foo}"))
                    .faultCode("SOAP-ENV:Server")
                    .faultString("Internal server error")
                    .validator(soapFaultValidator);
            }
        };
        
        builder.setApplicationContext(applicationContext);
        
        reset(applicationContext);
        
        expect(applicationContext.getBean("messageFactory", SoapMessageFactory.class)).andReturn(messageFactory).once();
        expect(applicationContext.getBean("soapFaultValidator", SoapFaultValidator.class)).andReturn(soapFaultValidator).once();
        
        replay(applicationContext);
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(builder.testCase().getActions().get(0).getName(), AssertSoapFault.class.getSimpleName());
        
        AssertSoapFault container = (AssertSoapFault)(builder.testCase().getTestAction(0));
        
        Assert.assertEquals(container.getActions().size(), 1);
        Assert.assertEquals(container.getAction().getClass(), EchoAction.class);
        Assert.assertEquals(container.getFaultCode(), "SOAP-ENV:Server");
        Assert.assertEquals(container.getFaultString(), "Internal server error");
        Assert.assertEquals(container.getValidator(), soapFaultValidator);
        Assert.assertEquals(((EchoAction)(container.getAction())).getMessage(), "${foo}");
        
        verify(applicationContext);
    }
    
    @Test
    public void testAssertSoapFaultBuilderWithMessageFactory() {
        MockBuilder builder = new MockBuilder() {
            @Override
            public void configure() {
                assertSoapFault(echo("${foo}"))
                    .faultCode("SOAP-ENV:Server")
                    .faultString("Internal server error")
                    .messageFactory(messageFactory);
            }
        };
        
        builder.setApplicationContext(applicationContext);
        
        reset(applicationContext);
        
        expect(applicationContext.getBean("messageFactory", SoapMessageFactory.class)).andReturn(messageFactory).once();
        expect(applicationContext.getBean("soapFaultValidator", SoapFaultValidator.class)).andReturn(soapFaultValidator).once();
        
        replay(applicationContext);
        
        builder.run(null, null);
        
        Assert.assertEquals(builder.testCase().getActions().size(), 1);
        Assert.assertEquals(builder.testCase().getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(builder.testCase().getActions().get(0).getName(), AssertSoapFault.class.getSimpleName());
        
        AssertSoapFault container = (AssertSoapFault)(builder.testCase().getTestAction(0));
        
        Assert.assertEquals(container.getActions().size(), 1);
        Assert.assertEquals(container.getAction().getClass(), EchoAction.class);
        Assert.assertEquals(container.getFaultCode(), "SOAP-ENV:Server");
        Assert.assertEquals(container.getFaultString(), "Internal server error");
        Assert.assertEquals(container.getMessageFactory(), messageFactory);
        Assert.assertEquals(((EchoAction)(container.getAction())).getMessage(), "${foo}");
        
        verify(applicationContext);
    }
}
