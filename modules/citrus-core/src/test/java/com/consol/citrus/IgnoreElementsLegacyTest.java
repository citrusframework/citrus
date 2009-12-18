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

import java.util.*;

import org.easymock.EasyMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.message.MessageReceiver;
import com.consol.citrus.testng.AbstractBaseTest;
import com.consol.citrus.validation.MessageValidator;

public class IgnoreElementsLegacyTest extends AbstractBaseTest {
    @Autowired
    MessageValidator validator;
    
    MessageReceiver messageReceiver = EasyMock.createMock(MessageReceiver.class);
    
    ReceiveMessageAction receiveMessageBean;
    
    @Override
    @BeforeMethod
    @SuppressWarnings("unchecked")
    public void setup() {
        super.setup();
        
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
        
        receiveMessageBean = new ReceiveMessageAction();
        receiveMessageBean.setMessageReceiver(messageReceiver);
        receiveMessageBean.setValidator(validator);
    }

    @Test
    public void testIgnoreElements() {
        receiveMessageBean.setMessageData("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                + "<sub-elementA attribute='A'>no validation</sub-elementA>"
                + "<sub-elementB attribute='B'>no validation</sub-elementB>"
                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
            + "</element>" 
            + "</root>");
        
        Set<String> ignoreMessageElements = new HashSet<String>();
        ignoreMessageElements.add("root.element.sub-elementA");
        ignoreMessageElements.add("sub-elementB");
        receiveMessageBean.setIgnoreMessageElements(ignoreMessageElements);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testIgnoreAttributes() {
        receiveMessageBean.setMessageData("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                + "<sub-elementA attribute='no validation'>text-value</sub-elementA>"
                + "<sub-elementB attribute='no validation'>text-value</sub-elementB>"
                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
            + "</element>" 
            + "</root>");
        
        Set<String> ignoreMessageElements = new HashSet<String>();
        ignoreMessageElements.add("root.element.sub-elementA.attribute");
        ignoreMessageElements.add("sub-elementB.attribute");
        receiveMessageBean.setIgnoreMessageElements(ignoreMessageElements);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testIgnoreRootElement() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root>"
                        + "<element>Text</element>" 
                        + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        receiveMessageBean.setMessageData("<root>"
                        + "<element additonal-attribute='some'>Wrong text</element>" 
                        + "</root>");
        
        Set<String> ignoreMessageElements = new HashSet<String>();
        ignoreMessageElements.add("root");
        receiveMessageBean.setIgnoreMessageElements(ignoreMessageElements);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    public void testIgnoreElementsAndValidate() {
        receiveMessageBean.setMessageData("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                + "<sub-elementA attribute='A'>no validation</sub-elementA>"
                + "<sub-elementB attribute='B'>no validation</sub-elementB>"
                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
            + "</element>" 
            + "</root>");
        
        Set<String> ignoreMessageElements = new HashSet<String>();
        ignoreMessageElements.add("root.element.sub-elementA");
        ignoreMessageElements.add("sub-elementB");
        receiveMessageBean.setIgnoreMessageElements(ignoreMessageElements);
        
        Map<String, String> validateElements = new HashMap<String, String>();
        validateElements.put("root.element.sub-elementA", "wrong value");
        validateElements.put("sub-elementB", "wrong value");
        receiveMessageBean.setValidateMessageElements(validateElements);
        
        receiveMessageBean.execute(context);
    }
}
