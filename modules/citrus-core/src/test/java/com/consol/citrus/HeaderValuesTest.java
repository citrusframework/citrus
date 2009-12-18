/*
 * Copyright 2006-2009 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus;

import static org.easymock.EasyMock.*;

import java.util.HashMap;

import org.easymock.EasyMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.message.MessageReceiver;
import com.consol.citrus.testng.AbstractBaseTest;
import com.consol.citrus.validation.MessageValidator;

public class HeaderValuesTest extends AbstractBaseTest {
    @Autowired
    MessageValidator validator;
    
    MessageReceiver messageReceiver = EasyMock.createMock(MessageReceiver.class);
    
    ReceiveMessageAction receiveMessageBean;
    
    @Test
    @SuppressWarnings("unchecked")
    public void testValidateHeaderValues() {
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
        
        receiveMessageBean = new ReceiveMessageAction();
        receiveMessageBean.setMessageReceiver(messageReceiver);
        receiveMessageBean.setValidator(validator);
        receiveMessageBean.setMessageData("<root>"
                                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                            + "</element>" 
                            + "</root>");
        
        HashMap<String, Object> validateHeaderValues = new HashMap<String, Object>();
        validateHeaderValues.put("header-valueA", "A");
        
        receiveMessageBean.setHeaderValues(validateHeaderValues);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testValidateHeaderValuesComplete() {
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
        
        receiveMessageBean = new ReceiveMessageAction();
        receiveMessageBean.setMessageReceiver(messageReceiver);
        receiveMessageBean.setValidator(validator);
        receiveMessageBean.setMessageData("<root>"
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
        
        receiveMessageBean.setHeaderValues(validateHeaderValues);
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings("unchecked")
    public void testValidateHeaderValuesWrongExpectedValue() {
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
        
        receiveMessageBean = new ReceiveMessageAction();
        receiveMessageBean.setMessageReceiver(messageReceiver);
        receiveMessageBean.setValidator(validator);
        receiveMessageBean.setMessageData("<root>"
                                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                            + "</element>" 
                            + "</root>");
        
        HashMap<String, Object> validateHeaderValues = new HashMap<String, Object>();
        validateHeaderValues.put("header-valueA", "wrong");
        
        receiveMessageBean.setHeaderValues(validateHeaderValues);
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings("unchecked")
    public void testValidateHeaderValuesForWrongElement() {
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
        
        receiveMessageBean = new ReceiveMessageAction();
        receiveMessageBean.setMessageReceiver(messageReceiver);
        receiveMessageBean.setValidator(validator);
        receiveMessageBean.setMessageData("<root>"
                                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                            + "</element>" 
                            + "</root>");
        
        HashMap<String, Object> validateHeaderValues = new HashMap<String, Object>();
        validateHeaderValues.put("header-wrong", "A");
        
        receiveMessageBean.setHeaderValues(validateHeaderValues);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testValidateEmptyHeaderValues() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>")
                        .setHeader("header-valueA", "")
                        .setHeader("header-valueB", "")
                        .setHeader("header-valueC", "")
                        .build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        receiveMessageBean = new ReceiveMessageAction();
        receiveMessageBean.setMessageReceiver(messageReceiver);
        receiveMessageBean.setValidator(validator);
        receiveMessageBean.setMessageData("<root>"
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
        
        receiveMessageBean.setHeaderValues(validateHeaderValues);
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings("unchecked")
    public void testValidateHeaderValuesNullComparison() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>" 
                        + "</root>")
                        .setHeader("header-valueA", "")
                        .setHeader("header-valueB", "")
                        .setHeader("header-valueC", "")
                        .build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        receiveMessageBean = new ReceiveMessageAction();
        receiveMessageBean.setMessageReceiver(messageReceiver);
        receiveMessageBean.setValidator(validator);
        receiveMessageBean.setMessageData("<root>"
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
        
        receiveMessageBean.setHeaderValues(validateHeaderValues);
        
        receiveMessageBean.execute(context);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExtractHeaderValues() {
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
        
        receiveMessageBean = new ReceiveMessageAction();
        receiveMessageBean.setMessageReceiver(messageReceiver);
        receiveMessageBean.setValidator(validator);
        receiveMessageBean.setMessageData("<root>"
                                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                            + "</element>" 
                            + "</root>");
        
        HashMap<String, String> extractHeaderValues = new HashMap<String, String>();
        extractHeaderValues.put("header-valueA", "${valueA}");
        extractHeaderValues.put("header-valueB", "${valueB}");
        
        receiveMessageBean.setExtractHeaderValues(extractHeaderValues);
        
        receiveMessageBean.execute(context);
        
        Assert.assertTrue(context.getVariables().containsKey("valueA"));
        Assert.assertEquals(context.getVariables().get("valueA"), "A");
        Assert.assertTrue(context.getVariables().containsKey("valueB"));
        Assert.assertEquals(context.getVariables().get("valueB"), "B");
    }
}
