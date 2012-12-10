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
import org.springframework.core.io.Resource;
import org.springframework.ws.soap.SoapMessageFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.dsl.TestNGCitrusTestBuilder;
import com.consol.citrus.ws.actions.AssertSoapFault;
import com.consol.citrus.ws.validation.SoapFaultValidator;

public class AssertSoapFaultDefinitionTest {
    
    private Resource resource = EasyMock.createMock(Resource.class);
    private SoapFaultValidator soapFaultValidator = EasyMock.createMock(SoapFaultValidator.class);
    private SoapMessageFactory messageFactory = EasyMock.createMock(SoapMessageFactory.class);
    
    @Test
    public void testAssertSoapFaultBuilder() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            public void configure() {
                soapFault(echo("${foo}"))
                    .faultCode("SOAP-ENV:Server")
                    .faultString("Internal server error");
            }
        };
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getName(), AssertSoapFault.class.getSimpleName());
        
        AssertSoapFault container = (AssertSoapFault)(builder.getTestCase().getTestAction(0));
        
        Assert.assertEquals(container.getActions().size(), 1);
        Assert.assertEquals(container.getAction().getClass(), EchoAction.class);
        Assert.assertEquals(container.getFaultCode(), "SOAP-ENV:Server");
        Assert.assertEquals(container.getFaultString(), "Internal server error");
        Assert.assertEquals(((EchoAction)(container.getAction())).getMessage(), "${foo}");
    }
    
    @Test
    public void testFaultDetail() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            public void configure() {
                soapFault(echo("${foo}"))
                    .faultCode("SOAP-ENV:Server")
                    .faultString("Internal server error")
                    .faultDetail("<detail>FooBar</detail>");
            }
        };
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getName(), AssertSoapFault.class.getSimpleName());
        
        AssertSoapFault container = (AssertSoapFault)(builder.getTestCase().getTestAction(0));
        
        Assert.assertEquals(container.getActions().size(), 1);
        Assert.assertEquals(container.getAction().getClass(), EchoAction.class);
        Assert.assertEquals(container.getFaultCode(), "SOAP-ENV:Server");
        Assert.assertEquals(container.getFaultString(), "Internal server error");
        Assert.assertEquals(container.getFaultDetail(), "<detail>FooBar</detail>");
        Assert.assertEquals(((EchoAction)(container.getAction())).getMessage(), "${foo}");
    }
    
    @Test
    public void testFaultDetailResource() throws IOException {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            public void configure() {
                soapFault(echo("${foo}"))
                    .faultCode("SOAP-ENV:Server")
                    .faultString("Internal server error")
                    .faultDetailResource(resource);
            }
        };
        
        reset(resource);
        expect(resource.getInputStream()).andReturn(new ByteArrayInputStream("<detail>FooBar</detail>".getBytes())).once();
        replay(resource);
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getName(), AssertSoapFault.class.getSimpleName());
        
        AssertSoapFault container = (AssertSoapFault)(builder.getTestCase().getTestAction(0));
        
        Assert.assertEquals(container.getActions().size(), 1);
        Assert.assertEquals(container.getAction().getClass(), EchoAction.class);
        Assert.assertEquals(container.getFaultCode(), "SOAP-ENV:Server");
        Assert.assertEquals(container.getFaultString(), "Internal server error");
        Assert.assertEquals(container.getFaultDetail(), "<detail>FooBar</detail>");
        Assert.assertEquals(((EchoAction)(container.getAction())).getMessage(), "${foo}");
        
        verify(resource);
    }
    
    @Test
    public void testAssertSoapFaultBuilderWithValidator() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            public void configure() {
                soapFault(echo("${foo}"))
                    .faultCode("SOAP-ENV:Server")
                    .faultString("Internal server error")
                    .validator(soapFaultValidator);
            }
        };
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getName(), AssertSoapFault.class.getSimpleName());
        
        AssertSoapFault container = (AssertSoapFault)(builder.getTestCase().getTestAction(0));
        
        Assert.assertEquals(container.getActions().size(), 1);
        Assert.assertEquals(container.getAction().getClass(), EchoAction.class);
        Assert.assertEquals(container.getFaultCode(), "SOAP-ENV:Server");
        Assert.assertEquals(container.getFaultString(), "Internal server error");
        Assert.assertEquals(container.getValidator(), soapFaultValidator);
        Assert.assertEquals(((EchoAction)(container.getAction())).getMessage(), "${foo}");
    }
    
    @Test
    public void testAssertSoapFaultBuilderWithMessageFactory() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            public void configure() {
                soapFault(echo("${foo}"))
                    .faultCode("SOAP-ENV:Server")
                    .faultString("Internal server error")
                    .messageFactory(messageFactory);
            }
        };
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), AssertSoapFault.class);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getName(), AssertSoapFault.class.getSimpleName());
        
        AssertSoapFault container = (AssertSoapFault)(builder.getTestCase().getTestAction(0));
        
        Assert.assertEquals(container.getActions().size(), 1);
        Assert.assertEquals(container.getAction().getClass(), EchoAction.class);
        Assert.assertEquals(container.getFaultCode(), "SOAP-ENV:Server");
        Assert.assertEquals(container.getFaultString(), "Internal server error");
        Assert.assertEquals(container.getMessageFactory(), messageFactory);
        Assert.assertEquals(((EchoAction)(container.getAction())).getMessage(), "${foo}");
    }
}
