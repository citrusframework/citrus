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

import java.util.*;

import org.easymock.EasyMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.message.MessageReceiver;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;

/**
 * @author Christoph Deppisch
 */
public class NamespaceTest extends AbstractTestNGUnitTest {
    @Autowired
    MessageValidator<ValidationContext> validator;
    
    MessageReceiver messageReceiver = EasyMock.createMock(MessageReceiver.class);
    
    ReceiveMessageAction receiveMessageBean;
    
    @Override
    @BeforeMethod
    public void prepareTest() {
        super.prepareTest();
        
        receiveMessageBean = new ReceiveMessageAction();
        receiveMessageBean.setMessageReceiver(messageReceiver);

        receiveMessageBean.setValidator(validator);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testNamespaces() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<ns1:root xmlns:ns1='http://citrus'>"
                            + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                            + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                            + "</ns1:element>" 
                        + "</ns1:root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<ns1:root xmlns:ns1='http://citrus'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                        + "</ns1:element>" 
                    + "</ns1:root>");
        
        validationContext.setSchemaValidation(false);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testDifferentNamespacePrefix() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<ns1:root xmlns:ns1='http://citrus'>"
                            + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                            + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                            + "</ns1:element>" 
                        + "</ns1:root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<ns2:root xmlns:ns2='http://citrus'>"
                        + "<ns2:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns2:sub-element attribute='A'>text-value</ns2:sub-element>"
                        + "</ns2:element>" 
                    + "</ns2:root>");
        
        validationContext.setSchemaValidation(false);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testAdditionalNamespace() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<ns1:root xmlns:ns1='http://citrus'>"
                            + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                            + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                            + "</ns1:element>" 
                        + "</ns1:root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<ns1:root xmlns:ns1='http://citrus' xmlns:ns2='http://citrus/default'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                        + "</ns1:element>" 
                    + "</ns1:root>");
        
        validationContext.setSchemaValidation(false);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testMissingNamespaceDeclaration() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<ns1:root xmlns:ns1='http://citrus' xmlns:ns2='http://citrus/default'>"
                            + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                            + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                            + "</ns1:element>" 
                        + "</ns1:root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<ns1:root xmlns:ns1='http://citrus'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                        + "</ns1:element>" 
                    + "</ns1:root>");
        
        validationContext.setSchemaValidation(false);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testDefaultNamespaces() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root xmlns='http://citrus'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root xmlns='http://citrus'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        validationContext.setSchemaValidation(false);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testDefaultNamespacesInExpectedMessage() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<ns1:root xmlns:ns1='http://citrus'>"
                            + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                            + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                            + "</ns1:element>" 
                        + "</ns1:root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root xmlns='http://citrus'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        validationContext.setSchemaValidation(false);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testDefaultNamespacesInSourceMessage() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root xmlns='http://citrus'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<ns1:root xmlns:ns1='http://citrus'>"
                    + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                    + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                    + "</ns1:element>" 
                + "</ns1:root>");
        
        validationContext.setSchemaValidation(false);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testMissingNamespace() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<ns1:root xmlns:ns1='http://citrus'>"
                            + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                            + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                            + "</ns1:element>" 
                        + "</ns1:root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                            + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                            + "<sub-element attribute='A'>text-value</sub-element>"
                            + "</element>" 
                        + "</root>");
        
        validationContext.setSchemaValidation(false);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testWrongNamespace() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<ns1:root xmlns:ns1='http://citrus'>"
                            + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                            + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                            + "</ns1:element>" 
                        + "</ns1:root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<ns1:root xmlns:ns1='http://citrus/wrong'>"
                            + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                            + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                            + "</ns1:element>" 
                        + "</ns1:root>");
        
        validationContext.setSchemaValidation(false);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testExpectDefaultNamespace() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root xmlns='http://citrus'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root xmlns='http://citrus'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://citrus");
        
        validationContext.setControlNamespaces(expectedNamespaces);
        
        validationContext.setSchemaValidation(false);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testExpectNamespace() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<ns1:root xmlns:ns1='http://citrus/ns1'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                        + "</ns1:element>" 
                    + "</ns1:root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<ns1:root xmlns:ns1='http://citrus/ns1'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                        + "</ns1:element>" 
                    + "</ns1:root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("ns1", "http://citrus/ns1");
        
        validationContext.setControlNamespaces(expectedNamespaces);
        
        validationContext.setSchemaValidation(false);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testExpectMixedNamespaces() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://citrus/default");
        expectedNamespaces.put("ns1", "http://citrus/ns1");
        
        validationContext.setControlNamespaces(expectedNamespaces);
        
        validationContext.setSchemaValidation(false);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testExpectMultipleNamespaces() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://citrus/default");
        expectedNamespaces.put("ns1", "http://citrus/ns1");
        expectedNamespaces.put("ns2", "http://citrus/ns2");
        
        validationContext.setControlNamespaces(expectedNamespaces);
        
        validationContext.setSchemaValidation(false);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testExpectDefaultNamespaceError() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root xmlns='http://citrus'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root xmlns='http://citrus'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://citrus/wrong");
        
        validationContext.setControlNamespaces(expectedNamespaces);
        
        validationContext.setSchemaValidation(false);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testExpectNamespaceError() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<ns1:root xmlns:ns1='http://citrus/ns1'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                        + "</ns1:element>" 
                    + "</ns1:root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<ns1:root xmlns:ns1='http://citrus/ns1'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                        + "</ns1:element>" 
                    + "</ns1:root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("ns1", "http://citrus/ns1/wrong");
        
        validationContext.setControlNamespaces(expectedNamespaces);
        
        validationContext.setSchemaValidation(false);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testExpectMixedNamespacesError() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://citrus/default/wrong");
        expectedNamespaces.put("ns1", "http://citrus/ns1");
        
        validationContext.setControlNamespaces(expectedNamespaces);
        
        validationContext.setSchemaValidation(false);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testExpectMultipleNamespacesError() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://citrus/default");
        expectedNamespaces.put("ns1", "http://citrus/ns1/wrong");
        expectedNamespaces.put("ns2", "http://citrus/ns2");
        
        validationContext.setControlNamespaces(expectedNamespaces);
        
        validationContext.setSchemaValidation(false);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testExpectWrongNamespacePrefix() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://citrus/default");
        expectedNamespaces.put("nswrong", "http://citrus/ns1");
        expectedNamespaces.put("ns2", "http://citrus/ns2");
        
        validationContext.setControlNamespaces(expectedNamespaces);
        
        validationContext.setSchemaValidation(false);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testExpectDefaultNamespaceButNamespace() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<ns0:root xmlns:ns0='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2'>"
                        + "<ns0:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns0:sub-element attribute='A'>text-value</ns0:sub-element>"
                        + "</ns0:element>" 
                    + "</ns0:root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<ns0:root xmlns:ns0='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2'>"
                        + "<ns0:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns0:sub-element attribute='A'>text-value</ns0:sub-element>"
                        + "</ns0:element>" 
                    + "</ns0:root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://citrus/default");
        expectedNamespaces.put("ns1", "http://citrus/ns1");
        expectedNamespaces.put("ns2", "http://citrus/ns2");
        
        validationContext.setControlNamespaces(expectedNamespaces);
        
        validationContext.setSchemaValidation(false);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testExpectNamespaceButDefaultNamespace() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("ns0", "http://citrus/default");
        expectedNamespaces.put("ns1", "http://citrus/ns1");
        expectedNamespaces.put("ns2", "http://citrus/ns2");
        
        validationContext.setControlNamespaces(expectedNamespaces);
        
        validationContext.setSchemaValidation(false);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testExpectAdditionalNamespace() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://citrus/default");
        expectedNamespaces.put("ns1", "http://citrus/ns1");
        expectedNamespaces.put("ns2", "http://citrus/ns2");
        expectedNamespaces.put("ns4", "http://citrus/ns4");
        
        validationContext.setControlNamespaces(expectedNamespaces);
        
        validationContext.setSchemaValidation(false);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testExpectNamespaceButNamespaceMissing() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2' xmlns:ns4='http://citrus/ns4'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2' xmlns:ns4='http://citrus/ns4'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("", "http://citrus/default");
        expectedNamespaces.put("ns1", "http://citrus/ns1");
        expectedNamespaces.put("ns2", "http://citrus/ns2");
        
        validationContext.setControlNamespaces(expectedNamespaces);
        
        validationContext.setSchemaValidation(false);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testValidateMessageElementsWithAdditionalNamespacePrefix() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root xmlns='http://citrus/default'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        HashMap<String, String> validateMessageElements = new HashMap<String, String>();
        validateMessageElements.put("//ns1:root/ns1:element/ns1:sub-elementA", "text-value");
        validateMessageElements.put("//ns1:sub-elementB", "text-value");
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        validationContext.setPathValidationExpressions(validateMessageElements);
        
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("ns1", "http://citrus/default");
        
        validationContext.setNamespaces(namespaces);
        
        validationContext.setSchemaValidation(false);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testValidateMessageElementsWithDifferentNamespacePrefix() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<ns1:root xmlns:ns1='http://citrus/default'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<ns1:sub-elementA attribute='A'>text-value</ns1:sub-elementA>"
                            + "<ns1:sub-elementB attribute='B'>text-value</ns1:sub-elementB>"
                            + "<ns1:sub-elementC attribute='C'>text-value</ns1:sub-elementC>"
                        + "</ns1:element>" 
                        + "</ns1:root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        HashMap<String, String> validateMessageElements = new HashMap<String, String>();
        validateMessageElements.put("//pfx:root/pfx:element/pfx:sub-elementA", "text-value");
        validateMessageElements.put("//pfx:sub-elementB", "text-value");
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        validationContext.setPathValidationExpressions(validateMessageElements);
        
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("pfx", "http://citrus/default");
        
        validationContext.setNamespaces(namespaces);
        
        validationContext.setSchemaValidation(false);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {CitrusRuntimeException.class})
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testWrongNamespaceContext() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<ns1:root xmlns:ns1='http://citrus/default'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<ns1:sub-elementA attribute='A'>text-value</ns1:sub-elementA>"
                            + "<ns1:sub-elementB attribute='B'>text-value</ns1:sub-elementB>"
                            + "<ns1:sub-elementC attribute='C'>text-value</ns1:sub-elementC>"
                        + "</ns1:element>" 
                        + "</ns1:root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        HashMap<String, String> validateMessageElements = new HashMap<String, String>();
        validateMessageElements.put("//pfx:root/ns1:element/pfx:sub-elementA", "text-value");
        validateMessageElements.put("//pfx:sub-elementB", "text-value");
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        validationContext.setPathValidationExpressions(validateMessageElements);
        
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("pfx", "http://citrus/wrong");
        
        validationContext.setNamespaces(namespaces);
        
        validationContext.setSchemaValidation(false);
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
}
