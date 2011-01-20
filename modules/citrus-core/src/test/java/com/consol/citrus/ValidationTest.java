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

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.message.MessageReceiver;
import com.consol.citrus.testng.AbstractBaseTest;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;

/**
 * @author Christoph Deppisch
 */
public class ValidationTest extends AbstractBaseTest {
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
    public void testValidateXMLTree() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
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
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testValidateXMLTreeDifferentAttributeOrder() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                        + "<element attributeB='attribute-value' attributeA='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings("unchecked")
    public void testValidateXMLTreeMissingElement() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                        + "</element>" 
                        + "</root>");
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings("unchecked")
    public void testValidateXMLTreeAdditionalElement() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                            + "<sub-elementD attribute='D'>text-value</sub-elementD>"
                        + "</element>" 
                        + "</root>");
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings("unchecked")
    public void testValidateXMLTreeMissingAttribute() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings("unchecked")
    public void testValidateXMLTreeAdditionalAttribute() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A' attribute-additional='additional'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings("unchecked")
    public void testValidateXMLTreeWrongAttribute() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute-wrong='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings("unchecked")
    public void testValidateXMLTreeWrongElement() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-element-wrong attribute='A'>text-value</sub-element-wrong>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings("unchecked")
    public void testValidateXMLTreeWrongNodeValue() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>wrong-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings("unchecked")
    public void testValidateXMLTreeWrongAttributeValue() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        validationContext.setMessageBuilder(controlMessageBuilder);
        controlMessageBuilder.setPayloadData("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='wrong-value'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>");
        
        List<ValidationContext> validationContexts = new ArrayList<ValidationContext>();
        validationContexts.add(validationContext);
        receiveMessageBean.setValidationContexts(validationContexts);
        receiveMessageBean.execute(context);
    }
}
