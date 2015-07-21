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

package com.consol.citrus;

import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.EndpointConfiguration;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.messaging.Consumer;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import com.consol.citrus.variable.MessageHeaderVariableExtractor;
import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class HeaderValuesTest extends AbstractTestNGUnitTest {
    private Endpoint endpoint = EasyMock.createMock(Endpoint.class);
    private Consumer consumer = EasyMock.createMock(Consumer.class);
    private EndpointConfiguration endpointConfiguration = EasyMock.createMock(EndpointConfiguration.class);
    
    private ReceiveMessageAction receiveMessageBean;
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testValidateHeaderValues() {
        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();
        
        Message message = new DefaultMessage("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>")
                        .setHeader("header-valueA", "A")
                        .setHeader("header-valueB", "B")
                        .setHeader("header-valueC", "C");

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(message);
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        receiveMessageBean = new ReceiveMessageAction();
        receiveMessageBean.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                            + "</element>" 
                            + "</root>");
        
        HashMap<String, Object> validateHeaderValues = new HashMap<String, Object>();
        validateHeaderValues.put("header-valueA", "A");
        
        controlMessageBuilder.setMessageHeaders(validateHeaderValues);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testValidateHeaderValuesComplete() {
        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();
        
        Message message = new DefaultMessage("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>")
                        .setHeader("header-valueA", "A")
                        .setHeader("header-valueB", "B")
                        .setHeader("header-valueC", "C");

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(message);
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        receiveMessageBean = new ReceiveMessageAction();
        receiveMessageBean.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                            + "</element>" 
                            + "</root>");
        
        HashMap<String, Object> validateHeaderValues = new HashMap<String, Object>();
        validateHeaderValues.put("header-valueA", "A");
        validateHeaderValues.put("header-valueB", "B");
        validateHeaderValues.put("header-valueC", "C");
        
        controlMessageBuilder.setMessageHeaders(validateHeaderValues);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testValidateHeaderValuesWrongExpectedValue() {
        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();
        
        Message message = new DefaultMessage("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>")
                        .setHeader("header-valueA", "A")
                        .setHeader("header-valueB", "B")
                        .setHeader("header-valueC", "C");

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(message);
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        receiveMessageBean = new ReceiveMessageAction();
        receiveMessageBean.setEndpoint(endpoint);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                            + "</element>" 
                            + "</root>");
        
        HashMap<String, Object> validateHeaderValues = new HashMap<String, Object>();
        validateHeaderValues.put("header-valueA", "wrong");
        
        controlMessageBuilder.setMessageHeaders(validateHeaderValues);
        
        List<ValidationContext> validationContexts = 
            new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testValidateHeaderValuesForWrongElement() {
        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();
        
        Message message = new DefaultMessage("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>")
                        .setHeader("header-valueA", "A")
                        .setHeader("header-valueB", "B")
                        .setHeader("header-valueC", "C");

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(message);
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        receiveMessageBean = new ReceiveMessageAction();
        receiveMessageBean.setEndpoint(endpoint);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                            + "</element>" 
                            + "</root>");
        
        HashMap<String, Object> validateHeaderValues = new HashMap<String, Object>();
        validateHeaderValues.put("header-wrong", "A");
        
        controlMessageBuilder.setMessageHeaders(validateHeaderValues);
        
        List<ValidationContext> validationContexts = 
            new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testValidateEmptyHeaderValues() {
        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();
        
        Message message = new DefaultMessage("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>")
                        .setHeader("header-valueA", "")
                        .setHeader("header-valueB", "")
                        .setHeader("header-valueC", "");

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(message);
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        receiveMessageBean = new ReceiveMessageAction();
        receiveMessageBean.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                            + "</element>" 
                            + "</root>");
        
        HashMap<String, Object> validateHeaderValues = new HashMap<String, Object>();
        validateHeaderValues.put("header-valueA", "");
        validateHeaderValues.put("header-valueB", "");
        validateHeaderValues.put("header-valueC", "");
        
        controlMessageBuilder.setMessageHeaders(validateHeaderValues);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testValidateHeaderValuesNullComparison() {
        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();
        
        Message message = new DefaultMessage("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>")
                        .setHeader("header-valueA", "")
                        .setHeader("header-valueB", "")
                        .setHeader("header-valueC", "");

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(message);
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        receiveMessageBean = new ReceiveMessageAction();
        receiveMessageBean.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                            + "</element>" 
                            + "</root>");
        
        HashMap<String, Object> validateHeaderValues = new HashMap<String, Object>();
        validateHeaderValues.put("header-valueA", "null");
        validateHeaderValues.put("header-valueB", "null");
        validateHeaderValues.put("header-valueC", "null");
        
        controlMessageBuilder.setMessageHeaders(validateHeaderValues);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testExtractHeaderValues() {
        reset(endpoint, consumer, endpointConfiguration);
        expect(endpoint.createConsumer()).andReturn(consumer).anyTimes();
        expect(endpoint.getEndpointConfiguration()).andReturn(endpointConfiguration).anyTimes();
        expect(endpointConfiguration.getTimeout()).andReturn(5000L).anyTimes();
        
        Message message = new DefaultMessage("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>")
                        .setHeader("header-valueA", "A")
                        .setHeader("header-valueB", "B")
                        .setHeader("header-valueC", "C");

        expect(consumer.receive(anyObject(TestContext.class), anyLong())).andReturn(message);
        expect(endpoint.getActor()).andReturn(null).anyTimes();
        replay(endpoint, consumer, endpointConfiguration);
        
        receiveMessageBean = new ReceiveMessageAction();
        receiveMessageBean.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                            + "</element>" 
                            + "</root>");
        
        HashMap<String, String> extractHeaderValues = new HashMap<String, String>();
        extractHeaderValues.put("header-valueA", "${valueA}");
        extractHeaderValues.put("header-valueB", "${valueB}");
        
        MessageHeaderVariableExtractor variableExtractor = new MessageHeaderVariableExtractor();
        variableExtractor.setHeaderMappings(extractHeaderValues);
        
        receiveMessageBean.addVariableExtractors(variableExtractor);
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        
        receiveMessageBean.execute(context);
        
        Assert.assertTrue(context.getVariables().containsKey("valueA"));
        Assert.assertEquals(context.getVariables().get("valueA"), "A");
        Assert.assertTrue(context.getVariables().containsKey("valueB"));
        Assert.assertEquals(context.getVariables().get("valueB"), "B");
    }
}
