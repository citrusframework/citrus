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
import java.util.Map;

import org.easymock.EasyMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.core.Message;
import org.springframework.integration.message.MessageBuilder;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.message.MessageReceiver;
import com.consol.citrus.validation.MessageValidator;

public class NamespaceTest extends AbstractBaseTest {
    @Autowired
    MessageValidator validator;
    
    MessageReceiver messageReceiver = EasyMock.createMock(MessageReceiver.class);
    
    ReceiveMessageAction receiveMessageBean;
    
    @Override
    @BeforeMethod
    public void setup() {
        super.setup();
        
        receiveMessageBean = new ReceiveMessageAction();
        receiveMessageBean.setMessageReceiver(messageReceiver);
        receiveMessageBean.setValidator(validator);
        receiveMessageBean.setSchemaValidation(false);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testNamespaces() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<ns1:root xmlns:ns1='http://testsuite'>"
                            + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                            + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                            + "</ns1:element>" 
                        + "</ns1:root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        receiveMessageBean.setMessageData("<ns1:root xmlns:ns1='http://testsuite'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                        + "</ns1:element>" 
                    + "</ns1:root>");
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testDifferentNamespacePrefix() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<ns1:root xmlns:ns1='http://testsuite'>"
                            + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                            + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                            + "</ns1:element>" 
                        + "</ns1:root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        receiveMessageBean.setMessageData("<ns2:root xmlns:ns2='http://testsuite'>"
                        + "<ns2:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns2:sub-element attribute='A'>text-value</ns2:sub-element>"
                        + "</ns2:element>" 
                    + "</ns2:root>");
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testAdditionalNamespace() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<ns1:root xmlns:ns1='http://testsuite'>"
                            + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                            + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                            + "</ns1:element>" 
                        + "</ns1:root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        receiveMessageBean.setMessageData("<ns1:root xmlns:ns1='http://testsuite' xmlns:ns2='http://testsuite/default'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                        + "</ns1:element>" 
                    + "</ns1:root>");
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testMissingNamespaceDeclaration() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<ns1:root xmlns:ns1='http://testsuite' xmlns:ns2='http://testsuite/default'>"
                            + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                            + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                            + "</ns1:element>" 
                        + "</ns1:root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        receiveMessageBean.setMessageData("<ns1:root xmlns:ns1='http://testsuite'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                        + "</ns1:element>" 
                    + "</ns1:root>");
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testDefaultNamespaces() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root xmlns='http://testsuite'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        receiveMessageBean.setMessageData("<root xmlns='http://testsuite'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        receiveMessageBean.execute(context);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDefaultNamespacesInExpectedMessage() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<ns1:root xmlns:ns1='http://testsuite'>"
                            + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                            + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                            + "</ns1:element>" 
                        + "</ns1:root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        receiveMessageBean.setMessageData("<root xmlns='http://testsuite'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testDefaultNamespacesInSourceMessage() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root xmlns='http://testsuite'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        receiveMessageBean.setMessageData("<ns1:root xmlns:ns1='http://testsuite'>"
                    + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                    + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                    + "</ns1:element>" 
                + "</ns1:root>");
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings("unchecked")
    public void testMissingNamespace() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<ns1:root xmlns:ns1='http://testsuite'>"
                            + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                            + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                            + "</ns1:element>" 
                        + "</ns1:root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        receiveMessageBean.setMessageData("<root>"
                            + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                            + "<sub-element attribute='A'>text-value</sub-element>"
                            + "</element>" 
                        + "</root>");
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings("unchecked")
    public void testWrongNamespace() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<ns1:root xmlns:ns1='http://testsuite'>"
                            + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                            + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                            + "</ns1:element>" 
                        + "</ns1:root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        receiveMessageBean.setMessageData("<ns1:root xmlns:ns1='http://testsuite/wrong'>"
                            + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                            + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                            + "</ns1:element>" 
                        + "</ns1:root>");
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testExpectDefaultNamespace() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root xmlns='http://testsuite'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        receiveMessageBean.setMessageData("<root xmlns='http://testsuite'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns", "http://testsuite");
        
        receiveMessageBean.setExpectedNamespaces(expectedNamespaces);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testExpectNamespace() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<ns1:root xmlns:ns1='http://testsuite/ns1'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                        + "</ns1:element>" 
                    + "</ns1:root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        receiveMessageBean.setMessageData("<ns1:root xmlns:ns1='http://testsuite/ns1'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                        + "</ns1:element>" 
                    + "</ns1:root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns:ns1", "http://testsuite/ns1");
        
        receiveMessageBean.setExpectedNamespaces(expectedNamespaces);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testExpectMixedNamespaces() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        receiveMessageBean.setMessageData("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns", "http://testsuite/default");
        expectedNamespaces.put("xmlns:ns1", "http://testsuite/ns1");
        
        receiveMessageBean.setExpectedNamespaces(expectedNamespaces);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testExpectMultipleNamespaces() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        receiveMessageBean.setMessageData("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns", "http://testsuite/default");
        expectedNamespaces.put("xmlns:ns1", "http://testsuite/ns1");
        expectedNamespaces.put("xmlns:ns2", "http://testsuite/ns2");
        
        receiveMessageBean.setExpectedNamespaces(expectedNamespaces);
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings("unchecked")
    public void testExpectDefaultNamespaceError() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root xmlns='http://testsuite'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        receiveMessageBean.setMessageData("<root xmlns='http://testsuite'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns", "http://testsuite/wrong");
        
        receiveMessageBean.setExpectedNamespaces(expectedNamespaces);
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings("unchecked")
    public void testExpectNamespaceError() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<ns1:root xmlns:ns1='http://testsuite/ns1'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                        + "</ns1:element>" 
                    + "</ns1:root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        receiveMessageBean.setMessageData("<ns1:root xmlns:ns1='http://testsuite/ns1'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                        + "</ns1:element>" 
                    + "</ns1:root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns:ns1", "http://testsuite/ns1/wrong");
        
        receiveMessageBean.setExpectedNamespaces(expectedNamespaces);
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings("unchecked")
    public void testExpectMixedNamespacesError() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        receiveMessageBean.setMessageData("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns", "http://testsuite/default/wrong");
        expectedNamespaces.put("xmlns:ns1", "http://testsuite/ns1");
        
        receiveMessageBean.setExpectedNamespaces(expectedNamespaces);
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings("unchecked")
    public void testExpectMultipleNamespacesError() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        receiveMessageBean.setMessageData("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns", "http://testsuite/default");
        expectedNamespaces.put("xmlns:ns1", "http://testsuite/ns1/wrong");
        expectedNamespaces.put("xmlns:ns2", "http://testsuite/ns2");
        
        receiveMessageBean.setExpectedNamespaces(expectedNamespaces);
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings("unchecked")
    public void testExpectWrongNamespacePrefix() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        receiveMessageBean.setMessageData("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns", "http://testsuite/default");
        expectedNamespaces.put("xmlns:nswrong", "http://testsuite/ns1");
        expectedNamespaces.put("xmlns:ns2", "http://testsuite/ns2");
        
        receiveMessageBean.setExpectedNamespaces(expectedNamespaces);
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings("unchecked")
    public void testExpectDefaultNamespaceButNamespace() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<ns0:root xmlns:ns0='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2'>"
                        + "<ns0:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns0:sub-element attribute='A'>text-value</ns0:sub-element>"
                        + "</ns0:element>" 
                    + "</ns0:root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        receiveMessageBean.setMessageData("<ns0:root xmlns:ns0='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2'>"
                        + "<ns0:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns0:sub-element attribute='A'>text-value</ns0:sub-element>"
                        + "</ns0:element>" 
                    + "</ns0:root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns", "http://testsuite/default");
        expectedNamespaces.put("xmlns:ns1", "http://testsuite/ns1");
        expectedNamespaces.put("xmlns:ns2", "http://testsuite/ns2");
        
        receiveMessageBean.setExpectedNamespaces(expectedNamespaces);
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings("unchecked")
    public void testExpectNamespaceButDefaultNamespace() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        receiveMessageBean.setMessageData("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns:ns0", "http://testsuite/default");
        expectedNamespaces.put("xmlns:ns1", "http://testsuite/ns1");
        expectedNamespaces.put("xmlns:ns2", "http://testsuite/ns2");
        
        receiveMessageBean.setExpectedNamespaces(expectedNamespaces);
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings("unchecked")
    public void testExpectAdditionalNamespace() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        receiveMessageBean.setMessageData("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns", "http://testsuite/default");
        expectedNamespaces.put("xmlns:ns1", "http://testsuite/ns1");
        expectedNamespaces.put("xmlns:ns2", "http://testsuite/ns2");
        expectedNamespaces.put("xmlns:ns4", "http://testsuite/ns4");
        
        receiveMessageBean.setExpectedNamespaces(expectedNamespaces);
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings("unchecked")
    public void testExpectNamespaceButNamespaceMissing() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2' xmlns:ns4='http://testsuite/ns4'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>").build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        receiveMessageBean.setMessageData("<root xmlns='http://testsuite/default' xmlns:ns1='http://testsuite/ns1' xmlns:ns2='http://testsuite/ns2' xmlns:ns4='http://testsuite/ns4'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>" 
                    + "</root>");
        
        Map<String, String> expectedNamespaces = new HashMap<String, String>();
        expectedNamespaces.put("xmlns", "http://testsuite/default");
        expectedNamespaces.put("xmlns:ns1", "http://testsuite/ns1");
        expectedNamespaces.put("xmlns:ns2", "http://testsuite/ns2");
        
        receiveMessageBean.setExpectedNamespaces(expectedNamespaces);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testValidateMessageElementsWithAdditionalNamespacePrefix() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root xmlns='http://testsuite/default'>"
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
        
        receiveMessageBean.setValidateMessageElements(validateMessageElements);
        
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("ns1", "http://testsuite/default");
        
        receiveMessageBean.setNamespaces(namespaces);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testValidateMessageElementsWithDifferentNamespacePrefix() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<ns1:root xmlns:ns1='http://testsuite/default'>"
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
        
        receiveMessageBean.setValidateMessageElements(validateMessageElements);
        
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("pfx", "http://testsuite/default");
        
        receiveMessageBean.setNamespaces(namespaces);
        
        receiveMessageBean.execute(context);
    }
    
    @Test(expectedExceptions = {CitrusRuntimeException.class})
    @SuppressWarnings("unchecked")
    public void testWrongNamespaceContext() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<ns1:root xmlns:ns1='http://testsuite/default'>"
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
        
        receiveMessageBean.setValidateMessageElements(validateMessageElements);
        
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("pfx", "http://testsuite/wrong");
        
        receiveMessageBean.setNamespaces(namespaces);
        
        receiveMessageBean.execute(context);
    }
}
