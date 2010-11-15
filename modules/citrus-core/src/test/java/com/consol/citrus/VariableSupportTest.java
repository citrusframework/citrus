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

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;

import java.util.HashMap;
import java.util.Map;

import org.easymock.EasyMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.message.MessageReceiver;
import com.consol.citrus.testng.AbstractBaseTest;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.xml.XmlMessageValidationContextBuilder;
import com.consol.citrus.variable.MessageHeaderVariableExtractor;
import com.consol.citrus.variable.XpathPayloadVariableExtractor;

/**
 * @author Christoph Deppisch
 */
public class VariableSupportTest extends AbstractBaseTest {
    @Autowired
    MessageValidator<ValidationContext> validator;
    
    MessageReceiver messageReceiver = EasyMock.createMock(MessageReceiver.class);
    
    ReceiveMessageAction receiveMessageBean;
    
    @Override
    @BeforeMethod
    public void setup() {
        super.setup();
        
        receiveMessageBean = new ReceiveMessageAction();
        receiveMessageBean.setMessageReceiver(messageReceiver);

        receiveMessageBean.setValidator(validator);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testValidateMessageElementsVariablesSupport() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>")
                        .build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        context.getVariables().put("variable", "text-value");
        
        Map<String, String> validateMessageElements = new HashMap<String, String>();
        validateMessageElements.put("//root/element/sub-elementA", "${variable}");
        validateMessageElements.put("//sub-elementB", "${variable}");
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContextBuilder contextBuilder = new XmlMessageValidationContextBuilder();
        contextBuilder.setMessageBuilder(controlMessageBuilder);
        contextBuilder.setPathValidationExpressions(validateMessageElements);
        
        receiveMessageBean.setXmlMessageValidationContextBuilder(contextBuilder);
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testValidateMessageElementsFunctionSupport() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>")
                        .build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        context.getVariables().put("variable", "text-value");
        context.getVariables().put("text", "text");
        
        Map<String, String> validateMessageElements = new HashMap<String, String>();
        validateMessageElements.put("//root/element/sub-elementA", "citrus:concat('text', '-', 'value')");
        validateMessageElements.put("//sub-elementB", "citrus:concat(${text}, '-', 'value')");
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContextBuilder contextBuilder = new XmlMessageValidationContextBuilder();
        contextBuilder.setMessageBuilder(controlMessageBuilder);
        contextBuilder.setPathValidationExpressions(validateMessageElements);
        
        receiveMessageBean.setXmlMessageValidationContextBuilder(contextBuilder);
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testValidateMessageElementsVariableSupportInExpression() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>")
                        .build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        context.getVariables().put("expression", "//root/element/sub-elementA");
        
        Map<String, String> validateMessageElements = new HashMap<String, String>();
        validateMessageElements.put("${expression}", "text-value");
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContextBuilder contextBuilder = new XmlMessageValidationContextBuilder();
        contextBuilder.setMessageBuilder(controlMessageBuilder);
        contextBuilder.setPathValidationExpressions(validateMessageElements);
        
        receiveMessageBean.setXmlMessageValidationContextBuilder(contextBuilder);
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testValidateMessageElementsFunctionSupportInExpression() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>")
                        .build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        context.getVariables().put("variable", "B");
        
        Map<String, String> validateMessageElements = new HashMap<String, String>();
        validateMessageElements.put("citrus:concat('//root/', 'element/sub-elementA')", "text-value");
        validateMessageElements.put("citrus:concat('//sub-element', ${variable})", "text-value");
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContextBuilder contextBuilder = new XmlMessageValidationContextBuilder();
        contextBuilder.setMessageBuilder(controlMessageBuilder);
        contextBuilder.setPathValidationExpressions(validateMessageElements);
        
        receiveMessageBean.setXmlMessageValidationContextBuilder(contextBuilder);
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testValidateHeaderValuesVariablesSupport() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>")
                        .setHeader("header-valueA", "A")
                        .setHeader("header-valueB", "B")
                        .setHeader("header-valueC", "C")
                        .build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContextBuilder contextBuilder = new XmlMessageValidationContextBuilder();
        contextBuilder.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                    + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                    + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                    + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>" 
                + "</root>");
        
        context.getVariables().put("variableA", "A");
        context.getVariables().put("variableB", "B");
        context.getVariables().put("variableC", "C");
        
        HashMap<String, Object> validateHeaderValues = new HashMap<String, Object>();
        validateHeaderValues.put("header-valueA", "${variableA}");
        validateHeaderValues.put("header-valueB", "${variableB}");
        validateHeaderValues.put("header-valueC", "${variableC}");
        
        controlMessageBuilder.setMessageHeaders(validateHeaderValues);
        
        receiveMessageBean.setXmlMessageValidationContextBuilder(contextBuilder);
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testValidateHeaderValuesFunctionSupport() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>")
                        .setHeader("header-valueA", "A")
                        .setHeader("header-valueB", "B")
                        .setHeader("header-valueC", "C")
                        .build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContextBuilder contextBuilder = new XmlMessageValidationContextBuilder();
        contextBuilder.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                    + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                    + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                    + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>" 
                + "</root>");
        
        context.getVariables().put("variableC", "c");
        
        HashMap<String, Object> validateHeaderValues = new HashMap<String, Object>();
        validateHeaderValues.put("header-valueA", "citrus:upperCase('a')");
        validateHeaderValues.put("header-valueB", "citrus:upperCase('b')");
        validateHeaderValues.put("header-valueC", "citrus:upperCase(${variableC})");
        
        controlMessageBuilder.setMessageHeaders(validateHeaderValues);
        
        receiveMessageBean.setXmlMessageValidationContextBuilder(contextBuilder);
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testHeaderNameVariablesSupport() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>")
                        .setHeader("header-valueA", "A")
                        .setHeader("header-valueB", "B")
                        .setHeader("header-valueC", "C")
                        .build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContextBuilder contextBuilder = new XmlMessageValidationContextBuilder();
        contextBuilder.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                    + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                    + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                    + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>" 
                + "</root>");
        
        context.getVariables().put("variableA", "header-valueA");
        context.getVariables().put("variableB", "header-valueB");
        context.getVariables().put("variableC", "header-valueC");
        
        HashMap<String, Object> validateHeaderValues = new HashMap<String, Object>();
        validateHeaderValues.put("${variableA}", "A");
        validateHeaderValues.put("${variableB}", "B");
        validateHeaderValues.put("${variableC}", "C");
        
        controlMessageBuilder.setMessageHeaders(validateHeaderValues);
        
        receiveMessageBean.setXmlMessageValidationContextBuilder(contextBuilder);
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testHeaderNameFunctionSupport() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>")
                        .setHeader("header-valueA", "A")
                        .setHeader("header-valueB", "B")
                        .setHeader("header-valueC", "C")
                        .build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContextBuilder contextBuilder = new XmlMessageValidationContextBuilder();
        contextBuilder.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                    + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                    + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                    + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>" 
                + "</root>");
        
        HashMap<String, Object> validateHeaderValues = new HashMap<String, Object>();
        validateHeaderValues.put("citrus:concat('header', '-', 'valueA')", "A");
        validateHeaderValues.put("citrus:concat('header', '-', 'valueB')", "B");
        validateHeaderValues.put("citrus:concat('header', '-', 'valueC')", "C");
        
        controlMessageBuilder.setMessageHeaders(validateHeaderValues);
        
        receiveMessageBean.setXmlMessageValidationContextBuilder(contextBuilder);
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testExtractMessageElementsVariablesSupport() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>")
                        .build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        context.getVariables().put("variableA", "initial");
        context.getVariables().put("variableB", "initial");
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContextBuilder contextBuilder = new XmlMessageValidationContextBuilder();
        contextBuilder.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                    + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                    + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                    + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>" 
                + "</root>");
        
        HashMap<String, String> extractMessageElements = new HashMap<String, String>();
        extractMessageElements.put("//root/element/sub-elementA", "${variableA}");
        extractMessageElements.put("//root/element/sub-elementB", "${variableB}");
        
        XpathPayloadVariableExtractor variableExtractor = new XpathPayloadVariableExtractor();
        variableExtractor.setxPathExpressions(extractMessageElements);
        
        receiveMessageBean.addVariableExtractors(variableExtractor);
        
        receiveMessageBean.setXmlMessageValidationContextBuilder(contextBuilder);
        receiveMessageBean.execute(context);
        
        Assert.assertTrue(context.getVariables().containsKey("variableA"));
        Assert.assertEquals(context.getVariables().get("variableA"), "text-value");
        Assert.assertTrue(context.getVariables().containsKey("variableB"));
        Assert.assertEquals(context.getVariables().get("variableB"), "text-value");
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testExtractHeaderValuesVariablesSupport() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>")
                        .setHeader("header-valueA", "A")
                        .setHeader("header-valueB", "B")
                        .setHeader("header-valueC", "C")
                        .build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        context.getVariables().put("variableA", "initial");
        context.getVariables().put("variableB", "initial");
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContextBuilder contextBuilder = new XmlMessageValidationContextBuilder();
        contextBuilder.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                    + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                    + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                    + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>" 
                + "</root>");
        
        HashMap<String, String> extractHeaderValues = new HashMap<String, String>();
        extractHeaderValues.put("header-valueA", "${variableA}");
        extractHeaderValues.put("header-valueB", "${variableB}");
        
        MessageHeaderVariableExtractor variableExtractor = new MessageHeaderVariableExtractor();
        variableExtractor.setHeaderMappings(extractHeaderValues);
        
        receiveMessageBean.addVariableExtractors(variableExtractor);
        
        receiveMessageBean.setXmlMessageValidationContextBuilder(contextBuilder);
        receiveMessageBean.execute(context);
        
        Assert.assertTrue(context.getVariables().containsKey("variableA"));
        Assert.assertEquals(context.getVariables().get("variableA"), "A");
        Assert.assertTrue(context.getVariables().containsKey("variableB"));
        Assert.assertEquals(context.getVariables().get("variableB"), "B");
    }
}
