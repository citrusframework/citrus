/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.actions;

import com.consol.citrus.*;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.EndpointConfiguration;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.message.*;
import com.consol.citrus.messaging.SelectiveConsumer;
import com.consol.citrus.script.ScriptTypes;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.*;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.json.*;
import com.consol.citrus.validation.xml.XpathMessageConstructionInterceptor;
import com.consol.citrus.validation.script.*;
import com.consol.citrus.validation.xml.*;
import com.consol.citrus.variable.*;
import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class ReceiveMessageActionTest extends AbstractTestNGUnitTest {

    private Endpoint endpoint = EasyMock.createMock(Endpoint.class);
    private SelectiveConsumer consumer = EasyMock.createMock(SelectiveConsumer.class);
    private EndpointConfiguration endpointConfiguration = EasyMock.createMock(EndpointConfiguration.class);
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public void testReceiveMessageWithMessagePayloadData() {
		ReceiveMessageAction receiveAction = new ReceiveMessageAction();
		receiveAction.setEndpoint(endpoint);
		
		TestActor testActor = new TestActor();
        testActor.setName("TESTACTOR");
        
        receiveAction.setActor(testActor);
        
		PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
		
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
		
		List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);
		
		verify(endpoint, consumer, endpointConfiguration);
	}
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithMessagePayloadResource() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadResourcePath("classpath:com/consol/citrus/actions/test-request-payload.xml");
        
        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);

        verify(endpoint, consumer, endpointConfiguration);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithMessageBuilderScriptData() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);

        StringBuilder sb = new StringBuilder();
        sb.append("markupBuilder.TestRequest(){\n");
        sb.append("Message('Hello World!')\n");
        sb.append("}");
        
        GroovyScriptMessageBuilder controlMessageBuilder = new GroovyScriptMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setScriptData(sb.toString());
        
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);

        verify(endpoint, consumer, endpointConfiguration);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithMessageBuilderScriptDataVariableSupport() {
        context.setVariable("text", "Hello World!");
        
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);

        StringBuilder sb = new StringBuilder();
        sb.append("markupBuilder.TestRequest(){\n");
        sb.append("Message('${text}')\n");
        sb.append("}");
        
        GroovyScriptMessageBuilder controlMessageBuilder = new GroovyScriptMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setScriptData(sb.toString());
        
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);

        verify(endpoint, consumer, endpointConfiguration);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithMessageBuilderScriptResource() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);

        GroovyScriptMessageBuilder controlMessageBuilder = new GroovyScriptMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setScriptResourcePath("classpath:com/consol/citrus/actions/test-request-payload.groovy");
        
        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);

        verify(endpoint, consumer, endpointConfiguration);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithMessagePayloadDataVariablesSupport() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<TestRequest><Message>${myText}</Message></TestRequest>");
        
        context.setVariable("myText", "Hello World!");
        
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);

        verify(endpoint, consumer, endpointConfiguration);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithMessagePayloadResourceVariablesSupport() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadResourcePath("classpath:com/consol/citrus/actions/test-request-payload-with-variables.xml");
        
        context.setVariable("myText", "Hello World!");
        
        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);

        verify(endpoint, consumer, endpointConfiguration);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithMessagePayloadResourceFunctionsSupport() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadResourcePath("classpath:com/consol/citrus/actions/test-request-payload-with-functions.xml");
        
        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);

        verify(endpoint, consumer, endpointConfiguration);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageOverwriteMessageElementsXPath() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<TestRequest><Message>?</Message></TestRequest>");
        
        Map<String, String> overwriteElements = new HashMap<String, String>();
        overwriteElements.put("/TestRequest/Message", "Hello World!");
        
        XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(overwriteElements);
        controlMessageBuilder.add(interceptor);
        
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);

        verify(endpoint, consumer, endpointConfiguration);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageOverwriteMessageElementsJsonPath() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setMessageType(MessageType.JSON.toString());
        receiveAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("{ \"TestRequest\": { \"Message\": \"?\" }}");

        Map<String, String> overwriteElements = new HashMap<String, String>();
        overwriteElements.put("$.TestRequest.Message", "Hello World!");

        JsonPathMessageConstructionInterceptor interceptor = new JsonPathMessageConstructionInterceptor(overwriteElements);
        controlMessageBuilder.add(interceptor);

        Message controlMessage = new DefaultMessage("{ \"TestRequest\": { \"Message\": \"Hello World!\" }}");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);

        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);

        verify(endpoint, consumer, endpointConfiguration);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageOverwriteMessageElementsDotNotation() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<TestRequest><Message>?</Message></TestRequest>");
        
        Map<String, String> overwriteElements = new HashMap<String, String>();
        overwriteElements.put("TestRequest.Message", "Hello World!");
        
        XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(overwriteElements);
        controlMessageBuilder.add(interceptor);
        
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);

        verify(endpoint, consumer, endpointConfiguration);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageOverwriteMessageElementsXPathWithNamespaces() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);


        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\">" +
                "<ns0:Message>?</ns0:Message></ns0:TestRequest>");
        
        Map<String, String> overwriteElements = new HashMap<String, String>();
        overwriteElements.put("/ns0:TestRequest/ns0:Message", "Hello World!");

        XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(overwriteElements);
        controlMessageBuilder.add(interceptor);
        
        Message controlMessage = new DefaultMessage("<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\">" +
                "<ns0:Message>Hello World!</ns0:Message></ns0:TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);

        validationContext.setSchemaValidation(false);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);

        verify(endpoint, consumer, endpointConfiguration);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageOverwriteMessageElementsXPathWithNestedNamespaces() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\">" +
                "<ns1:Message xmlns:ns1=\"http://citrusframework.org/unittest/message\">?</ns1:Message></ns0:TestRequest>");
        
        Map<String, String> overwriteElements = new HashMap<String, String>();
        overwriteElements.put("/ns0:TestRequest/ns1:Message", "Hello World!");

        XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(overwriteElements);
        controlMessageBuilder.add(interceptor);
        
        Message controlMessage = new DefaultMessage("<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\">" +
                "<ns1:Message xmlns:ns1=\"http://citrusframework.org/unittest/message\">Hello World!</ns1:Message></ns0:TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);

        validationContext.setSchemaValidation(false);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);

        verify(endpoint, consumer, endpointConfiguration);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageOverwriteMessageElementsXPathWithDefaultNamespaces() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<TestRequest xmlns=\"http://citrusframework.org/unittest\">" +
                "<Message>?</Message></TestRequest>");
        
        Map<String, String> overwriteElements = new HashMap<String, String>();
        overwriteElements.put("/:TestRequest/:Message", "Hello World!");

        XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(overwriteElements);
        controlMessageBuilder.add(interceptor);
        
        Message controlMessage = new DefaultMessage("<TestRequest xmlns=\"http://citrusframework.org/unittest\"><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);

        validationContext.setSchemaValidation(false);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);

        verify(endpoint, consumer, endpointConfiguration);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithMessageHeaders() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "sayHello");
        controlMessageBuilder.setMessageHeaders(headers);

        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        controlHeaders.put("Operation", "sayHello");
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", controlHeaders);

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);

        verify(endpoint, consumer, endpointConfiguration);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithMessageHeadersVariablesSupport() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        context.setVariable("myOperation", "sayHello");
        
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "${myOperation}");
        controlMessageBuilder.setMessageHeaders(headers);
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        controlHeaders.put("Operation", "sayHello");
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", controlHeaders);

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);

        verify(endpoint, consumer, endpointConfiguration);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithUnknownVariablesInMessageHeaders() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "${myOperation}");
        controlMessageBuilder.setMessageHeaders(headers);
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        controlHeaders.put("Operation", "sayHello");
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", controlHeaders);

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        try {
            receiveAction.execute(context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Unknown variable 'myOperation'");
            return;
        }

        verify(endpoint, consumer, endpointConfiguration);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithUnknownVariableInMessagePayload() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<TestRequest><Message>${myText}</Message></TestRequest>");
        
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        try {
            receiveAction.execute(context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Unknown variable 'myText'");
            return;
        }

        verify(endpoint, consumer, endpointConfiguration);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithExtractVariablesFromHeaders() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Operation", "myOperation");
        
        MessageHeaderVariableExtractor headerVariableExtractor = new MessageHeaderVariableExtractor();
        headerVariableExtractor.setHeaderMappings(headers);
        List<VariableExtractor> variableExtractors = new ArrayList<VariableExtractor>();
        variableExtractors.add(headerVariableExtractor);
        receiveAction.addVariableExtractors(headerVariableExtractor);
        
        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        controlHeaders.put("Operation", "sayHello");
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", controlHeaders);

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);
        
        Assert.assertNotNull(context.getVariable("myOperation"));
        Assert.assertEquals(context.getVariable("myOperation"), "sayHello");

        verify(endpoint, consumer, endpointConfiguration);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithValidateMessageElementsFromMessageXPath() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XpathMessageValidationContext validationContext = new XpathMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);

        Map<String, String> messageElements = new HashMap<String, String>();
        messageElements.put("/TestRequest/Message", "Hello World!");
        validationContext.setXpathExpressions(messageElements);
        
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);

        verify(endpoint, consumer, endpointConfiguration);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithValidateMessageElementsXPathDefaultNamespaceSupport() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XpathMessageValidationContext validationContext = new XpathMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);

        Map<String, String> messageElements = new HashMap<String, String>();
        messageElements.put("/:TestRequest/:Message", "Hello World!");
        validationContext.setXpathExpressions(messageElements);
        
        Message controlMessage = new DefaultMessage("<TestRequest  xmlns=\"http://citrusframework.org/unittest\">" +
                "<Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);

        validationContext.setSchemaValidation(false);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);

        verify(endpoint, consumer, endpointConfiguration);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithValidateMessageElementsXPathNamespaceSupport() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XpathMessageValidationContext validationContext = new XpathMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);

        Map<String, String> messageElements = new HashMap<String, String>();
        messageElements.put("/ns0:TestRequest/ns0:Message", "Hello World!");
        validationContext.setXpathExpressions(messageElements);
        
        Message controlMessage = new DefaultMessage("<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\">" +
                "<ns0:Message>Hello World!</ns0:Message></ns0:TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);

        validationContext.setSchemaValidation(false);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);

        verify(endpoint, consumer, endpointConfiguration);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithValidateMessageElementsXPathNestedNamespaceSupport() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XpathMessageValidationContext validationContext = new XpathMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);

        Map<String, String> messageElements = new HashMap<String, String>();
        messageElements.put("/ns0:TestRequest/ns1:Message", "Hello World!");
        validationContext.setXpathExpressions(messageElements);
        
        Message controlMessage = new DefaultMessage("<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\">" +
                "<ns1:Message xmlns:ns1=\"http://citrusframework.org/unittest/message\">Hello World!</ns1:Message></ns0:TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);

        validationContext.setSchemaValidation(false);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);

        verify(endpoint, consumer, endpointConfiguration);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithValidateMessageElementsXPathNamespaceBindings() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XpathMessageValidationContext validationContext = new XpathMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        
        Map<String, String> messageElements = new HashMap<String, String>();
        messageElements.put("/pfx:TestRequest/pfx:Message", "Hello World!");
        validationContext.setXpathExpressions(messageElements);
        
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("pfx", "http://citrusframework.org/unittest");
        validationContext.setNamespaces(namespaces);
        
        Message controlMessage = new DefaultMessage("<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\">" +
                "<ns0:Message>Hello World!</ns0:Message></ns0:TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);

        validationContext.setSchemaValidation(false);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);

        verify(endpoint, consumer, endpointConfiguration);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithExtractVariablesFromMessageJsonPath() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}");

        Map<String, String> extractMessageElements = new HashMap<String, String>();
        extractMessageElements.put("$.text", "messageVar");
        extractMessageElements.put("$.person", "person");

        JsonPathVariableExtractor variableExtractor = new JsonPathVariableExtractor();
        variableExtractor.setJsonPathExpressions(extractMessageElements);
        receiveAction.addVariableExtractors(variableExtractor);

        Message controlMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);

        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);

        Assert.assertNotNull(context.getVariable("messageVar"));
        Assert.assertEquals(context.getVariable("messageVar"), "Hello World!");

        Assert.assertNotNull(context.getVariable("person"));
        Assert.assertTrue(context.getVariable("person").contains("\"John\""));

        verify(endpoint, consumer, endpointConfiguration);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithExtractVariablesFromMessageXPath() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, String> extractMessageElements = new HashMap<String, String>();
        extractMessageElements.put("/TestRequest/Message", "messageVar");
        
        XpathPayloadVariableExtractor variableExtractor = new XpathPayloadVariableExtractor();
        variableExtractor.setXpathExpressions(extractMessageElements);
        receiveAction.addVariableExtractors(variableExtractor);
        
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);
        
        Assert.assertNotNull(context.getVariable("messageVar"));
        Assert.assertEquals(context.getVariable("messageVar"), "Hello World!");

        verify(endpoint, consumer, endpointConfiguration);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithExtractVariablesFromMessageXPathDefaultNamespaceSupport() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<TestRequest xmlns=\"http://citrusframework.org/unittest\">" +
                "<Message>Hello World!</Message></TestRequest>");

        Map<String, String> extractMessageElements = new HashMap<String, String>();
        extractMessageElements.put("/:TestRequest/:Message", "messageVar");
        
        XpathPayloadVariableExtractor variableExtractor = new XpathPayloadVariableExtractor();
        variableExtractor.setXpathExpressions(extractMessageElements);
        receiveAction.addVariableExtractors(variableExtractor);
        
        Message controlMessage = new DefaultMessage("<TestRequest  xmlns=\"http://citrusframework.org/unittest\">" +
                "<Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        validationContext.setSchemaValidation(false);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);
        
        Assert.assertNotNull(context.getVariable("messageVar"));
        Assert.assertEquals(context.getVariable("messageVar"), "Hello World!");

        verify(endpoint, consumer, endpointConfiguration);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithExtractVariablesFromMessageXPathNamespaceSupport() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<TestRequest xmlns=\"http://citrusframework.org/unittest\">" +
                "<Message>Hello World!</Message></TestRequest>");
        
        Map<String, String> extractMessageElements = new HashMap<String, String>();
        extractMessageElements.put("/ns0:TestRequest/ns0:Message", "messageVar");

        XpathPayloadVariableExtractor variableExtractor = new XpathPayloadVariableExtractor();
        variableExtractor.setXpathExpressions(extractMessageElements);
        receiveAction.addVariableExtractors(variableExtractor);
        
        Message controlMessage = new DefaultMessage("<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\">" +
                "<ns0:Message>Hello World!</ns0:Message></ns0:TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        validationContext.setSchemaValidation(false);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);
        
        Assert.assertNotNull(context.getVariable("messageVar"));
        Assert.assertEquals(context.getVariable("messageVar"), "Hello World!");

        verify(endpoint, consumer, endpointConfiguration);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithExtractVariablesFromMessageXPathNestedNamespaceSupport() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<TestRequest xmlns=\"http://citrusframework.org/unittest\" xmlns:ns1=\"http://citrusframework.org/unittest/message\">" +
                "<ns1:Message>Hello World!</ns1:Message></TestRequest>");

        Map<String, String> extractMessageElements = new HashMap<String, String>();
        extractMessageElements.put("/ns0:TestRequest/ns1:Message", "messageVar");
        
        XpathPayloadVariableExtractor variableExtractor = new XpathPayloadVariableExtractor();
        variableExtractor.setXpathExpressions(extractMessageElements);
        receiveAction.addVariableExtractors(variableExtractor);
        
        Message controlMessage = new DefaultMessage("<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\">" +
                "<ns1:Message xmlns:ns1=\"http://citrusframework.org/unittest/message\">Hello World!</ns1:Message></ns0:TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        validationContext.setSchemaValidation(false);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);
        
        Assert.assertNotNull(context.getVariable("messageVar"));
        Assert.assertEquals(context.getVariable("messageVar"), "Hello World!");

        verify(endpoint, consumer, endpointConfiguration);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithExtractVariablesFromMessageXPathNamespaceBindings() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<TestRequest xmlns=\"http://citrusframework.org/unittest\">" +
                "<Message>Hello World!</Message></TestRequest>");

        Map<String, String> extractMessageElements = new HashMap<String, String>();
        extractMessageElements.put("/pfx:TestRequest/pfx:Message", "messageVar");
        
        XpathPayloadVariableExtractor variableExtractor = new XpathPayloadVariableExtractor();
        variableExtractor.setXpathExpressions(extractMessageElements);
        receiveAction.addVariableExtractors(variableExtractor);
        
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("pfx", "http://citrusframework.org/unittest");
        variableExtractor.setNamespaces(namespaces);
        
        Message controlMessage = new DefaultMessage("<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\">" +
                "<ns0:Message>Hello World!</ns0:Message></ns0:TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        validationContext.setSchemaValidation(false);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);
        
        Assert.assertNotNull(context.getVariable("messageVar"));
        Assert.assertEquals(context.getVariable("messageVar"), "Hello World!");

        verify(endpoint, consumer, endpointConfiguration);
    }

    @Test
    public void testReceiveMessageWithJsonPathValidation() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);
        receiveAction.setMessageType(MessageType.JSON.toString());

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        JsonPathMessageValidationContext validationContext = new JsonPathMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);

        Map<String, String> jsonPathExpressions = new HashMap<String, String>();
        jsonPathExpressions.put("$..text", "Hello World!");
        jsonPathExpressions.put("$.person.name", "John");
        jsonPathExpressions.put("$.person.surname", "Doe");
        jsonPathExpressions.put("$.index", "5");
        validationContext.setJsonPathExpressions(jsonPathExpressions);

        Message controlMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);

        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);

        verify(endpoint, consumer, endpointConfiguration);
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testReceiveMessageWithJsonPathValidationFailure() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);
        receiveAction.setMessageType(MessageType.JSON.toString());

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        JsonPathMessageValidationContext validationContext = new JsonPathMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);

        Map<String, String> jsonPathExpressions = new HashMap<String, String>();
        jsonPathExpressions.put("$..text", "Hello Citrus!");
        validationContext.setJsonPathExpressions(jsonPathExpressions);

        Message controlMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);

        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);

        try {
            receiveAction.execute(context);
        } finally {
            verify(endpoint, consumer, endpointConfiguration);
        }
    }

    @Test(expectedExceptions = ValidationException.class)
    public void testReceiveMessageWithJsonPathValidationNoPathResult() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);
        receiveAction.setMessageType(MessageType.JSON.toString());

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        JsonPathMessageValidationContext validationContext = new JsonPathMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);

        Map<String, String> jsonPathExpressions = new HashMap<String, String>();
        jsonPathExpressions.put("$.person.age", "50");
        validationContext.setJsonPathExpressions(jsonPathExpressions);

        Message controlMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);

        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);

        try {
            receiveAction.execute(context);
        } finally {
            verify(endpoint, consumer, endpointConfiguration);
        }
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithTimeout() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        receiveAction.setReceiveTimeout(3000L);
        
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(context, 3000L)).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);

        verify(endpoint, consumer, endpointConfiguration);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveSelectedWithMessageSelectorString() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        String messageSelectorString = "Operation = 'sayHello'";
        receiveAction.setMessageSelectorString(messageSelectorString);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "sayHello");
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", headers);

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(messageSelectorString, context, 5000L)).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);

        verify(endpoint, consumer, endpointConfiguration);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveSelectedWithMessageSelectorStringAndTimeout() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        receiveAction.setReceiveTimeout(5000L);
        
        String messageSelectorString = "Operation = 'sayHello'";
        receiveAction.setMessageSelectorString(messageSelectorString);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "sayHello");
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", headers);

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(messageSelectorString, context, 5000L)).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);

        verify(endpoint, consumer, endpointConfiguration);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveSelectedWithMessageSelectorMap() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        Map<String, Object> messageSelector = new HashMap<>();
        messageSelector.put("Operation", "sayHello");
        receiveAction.setMessageSelector(messageSelector);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "sayHello");
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", headers);

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive("Operation = 'sayHello'", context, 5000L)).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);

        verify(endpoint, consumer, endpointConfiguration);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveSelectedWithMessageSelectorMapAndTimeout() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        receiveAction.setReceiveTimeout(5000L);
        
        Map<String, Object> messageSelector = new HashMap<>();
        messageSelector.put("Operation", "sayHello");
        receiveAction.setMessageSelector(messageSelector);
        
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "sayHello");
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", headers);

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive("Operation = 'sayHello'", context, 5000L)).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);

        verify(endpoint, consumer, endpointConfiguration);
    }
    
    @Test
    public void testMessageTimeout() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(null).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);

        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        try {
            receiveAction.execute(context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Failed to receive message - message is not available");
            verify(endpoint, consumer, endpointConfiguration);
            return;
        }
        
        Assert.fail("Missing " + CitrusRuntimeException.class + " for receiving no message");
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveEmptyMessagePayloadAsExpected() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("");
        
        Message controlMessage = new DefaultMessage("");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);

        verify(endpoint, consumer, endpointConfiguration);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveEmptyMessagePayloadUnexpected() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        Message controlMessage = new DefaultMessage("");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        try {
            receiveAction.execute(context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Validation failed: Unable to validate message payload - received message payload was empty, control message payload is not");
            verify(endpoint, consumer, endpointConfiguration);
            return;
        }
        
        Assert.fail("Missing " + CitrusRuntimeException.class + " for receiving unexpected empty message payload");
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithValidationScript() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);

        receiveAction.setValidator(new GroovyXmlMessageValidator());
        
        ScriptValidationContext validationContext = new ScriptValidationContext(ScriptTypes.GROOVY);
        validationContext.setValidationScript("assert root.Message.name() == 'Message'\n" + 
                "assert root.Message.text() == 'Hello World!'");
        
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);

        verify(endpoint, consumer, endpointConfiguration);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithValidationScriptResource() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);

        receiveAction.setValidator(new GroovyXmlMessageValidator());
        ScriptValidationContext validationContext = new ScriptValidationContext(ScriptTypes.GROOVY);
        validationContext.setValidationScriptResourcePath("classpath:com/consol/citrus/actions/test-validation-script.groovy");
        
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).once();
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);

        verify(endpoint, consumer, endpointConfiguration);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testInjectedMessageValidators() {
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(controlMessage).times(2);
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        
        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(context);
        
        // now inject multiple validators
        List<MessageValidator<? extends ValidationContext>> validators = new ArrayList<MessageValidator<? extends ValidationContext>>();
        validators.add(new DomXmlMessageValidator());
        validators.add(new GroovyXmlMessageValidator());
        
        MessageValidatorRegistry messageValidatorRegistry = new MessageValidatorRegistry();
        messageValidatorRegistry.setMessageValidators(validators);
        TestContext newContext = createTestContext();
        newContext.setMessageValidatorRegistry(messageValidatorRegistry);
        
        validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        ScriptValidationContext scriptValidationContext = new ScriptValidationContext(CitrusConstants.DEFAULT_MESSAGE_TYPE);
        validationContexts.add(scriptValidationContext);

        receiveAction.setValidationContexts(validationContexts);
        receiveAction.execute(newContext);

        verify(endpoint, consumer, endpointConfiguration);
    }
    
    @Test
    public void testDisabledReceiveMessage() {
        TestCase testCase = new TestCase();
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);
        

        TestActor disabledActor = new TestActor();
        disabledActor.setDisabled(true);
        receiveAction.setActor(disabledActor);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);

        testCase.addTestAction(receiveAction);
        testCase.execute(context);

        verify(endpoint, consumer, endpointConfiguration);
    }
    
    @Test
    public void testDisabledReceiveMessageByEndpointActor() {
        TestCase testCase = new TestCase();
        ReceiveMessageAction receiveAction = new ReceiveMessageAction();
        receiveAction.setEndpoint(endpoint);
        
        TestActor disabledActor = new TestActor();
        disabledActor.setDisabled(true);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();

        expect(endpoint.getActor()).andReturn(disabledActor).times(2);
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>(); 
        validationContexts.add(validationContext);
        receiveAction.setValidationContexts(validationContexts);

        testCase.addTestAction(receiveAction);
        testCase.execute(context);

        verify(endpoint, consumer, endpointConfiguration);
    }
}
