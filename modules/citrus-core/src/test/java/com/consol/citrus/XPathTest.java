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

/**
 * @author Christoph Deppisch
 */
public class XPathTest extends AbstractBaseTest {
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
    public void testUsingXPath() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<ns1:root xmlns='http://test' xmlns:ns1='http://testsuite'>"
                            + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                            + "</element>"
                            + "<ns1:ns-element>namespace</ns1:ns-element>"
                            + "<search-element>search-for</search-element>"
                        + "</ns1:root>")
                        .build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        HashMap<String, String> validateMessageElements = new HashMap<String, String>();
        validateMessageElements.put("//:element/:sub-elementA", "text-value");
        validateMessageElements.put("//:element/:sub-elementA[@attribute='A']", "text-value");
        validateMessageElements.put("//:element/:sub-elementB", "text-value");
        validateMessageElements.put("//:element/:sub-elementB/@attribute", "B");
        validateMessageElements.put("//ns1:ns-element", "namespace");
        validateMessageElements.put("//*[.='search-for']", "search-for");
        
        receiveMessageBean.setValidateMessageElements(validateMessageElements);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testUsingXPathWithDefaultNamespace() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root xmlns='http://test'>"
                            + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                            + "</element>"
                            + "<ns-element>namespace</ns-element>"
                            + "<search-element>search-for</search-element>"
                        + "</root>")
                        .build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        HashMap<String, String> validateMessageElements = new HashMap<String, String>();
        validateMessageElements.put("//:element/:sub-elementA", "text-value");
        validateMessageElements.put("//:element/:sub-elementA[@attribute='A']", "text-value");
        validateMessageElements.put("//:element/:sub-elementB", "text-value");
        validateMessageElements.put("//:element/:sub-elementB/@attribute", "B");
        validateMessageElements.put("//:ns-element", "namespace");
        validateMessageElements.put("//*[.='search-for']", "search-for");
        
        receiveMessageBean.setValidateMessageElements(validateMessageElements);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testUsingXPathWithExplicitNamespace() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root xmlns='http://test' xmlns:ns1='http://testsuite'>"
                            + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                            + "</element>"
                            + "<ns1:ns-element>namespace</ns1:ns-element>"
                            + "<search-element>search-for</search-element>"
                        + "</root>")
                        .build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        HashMap<String, String> validateMessageElements = new HashMap<String, String>();
        validateMessageElements.put("//:element/:sub-elementA", "text-value");
        validateMessageElements.put("//ns1:ns-element", "namespace");
        
        receiveMessageBean.setValidateMessageElements(validateMessageElements);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testUsingXPathWithExplicitNamespaceInElementDefinition() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<root xmlns='http://test'>"
                            + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                            + "</element>"
                            + "<ns1:ns-element xmlns:ns1='http://testsuite'>namespace</ns1:ns-element>"
                            + "<search-element>search-for</search-element>"
                        + "</root>")
                        .build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        HashMap<String, String> validateMessageElements = new HashMap<String, String>();
        validateMessageElements.put("//:element/:sub-elementA", "text-value");
        validateMessageElements.put("//ns1:ns-element", "namespace");
        
        receiveMessageBean.setValidateMessageElements(validateMessageElements);
        
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("ns1", "http://testsuite");
        
        receiveMessageBean.setNamespaces(namespaces);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testValidateMessageElementsUsingXPathWithResultTypes() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<ns1:root xmlns='http://test' xmlns:ns1='http://testsuite'>"
                            + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                            + "</element>"
                            + "<ns1:ns-element>namespace</ns1:ns-element>"
                            + "<search-element>search-for</search-element>"
                        + "</ns1:root>")
                        .build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        HashMap<String, String> validateMessageElements = new HashMap<String, String>();
        validateMessageElements.put("node://:element/:sub-elementA", "text-value");
        validateMessageElements.put("node://:element/:sub-elementA[@attribute='A']", "text-value");
        validateMessageElements.put("node://:element/:sub-elementB", "text-value");
        validateMessageElements.put("node://:element/:sub-elementB/@attribute", "B");
        validateMessageElements.put("node://ns1:ns-element", "namespace");
        validateMessageElements.put("node://*[.='search-for']", "search-for");
        validateMessageElements.put("number:count(/ns1:root/:element/*)", "3.0");
        validateMessageElements.put("string:concat(/ns1:root/ns1:ns-element, ' is the value')", "namespace is the value");
        validateMessageElements.put("string:local-name(/*)", "root");
        validateMessageElements.put("string:namespace-uri(/*)", "http://testsuite");
        validateMessageElements.put("boolean:contains(/ns1:root/:search-element, 'search')", "true");
        validateMessageElements.put("boolean:/ns1:root/:element", "true");
        validateMessageElements.put("boolean:/ns1:root/:element-does-not-exist", "false");
        
        
        receiveMessageBean.setValidateMessageElements(validateMessageElements);
        
        receiveMessageBean.execute(context);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testExtractMessageValuesUsingXPathWithResultTypes() {
        reset(messageReceiver);
        
        Message message = MessageBuilder.withPayload("<ns1:root xmlns='http://test' xmlns:ns1='http://testsuite'>"
                            + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                            + "</element>"
                            + "<ns1:ns-element>namespace</ns1:ns-element>"
                            + "<search-element>search-for</search-element>"
                        + "</ns1:root>")
                        .build();
        
        expect(messageReceiver.receive()).andReturn(message);
        replay(messageReceiver);
        
        HashMap<String, String> extractMessageElements = new HashMap<String, String>();
        extractMessageElements.put("node://:element/:sub-elementA", "elementA");
        extractMessageElements.put("node://:element/:sub-elementA/@attribute", "elementAttribute");
        extractMessageElements.put("node://*[.='search-for']", "search");
        extractMessageElements.put("number:count(/ns1:root/:element/*)", "count");
        extractMessageElements.put("string:concat(/ns1:root/ns1:ns-element, ' is the value')", "concat");
        extractMessageElements.put("string:local-name(/*)", "localName");
        extractMessageElements.put("string:namespace-uri(/*)", "namespaceUri");
        extractMessageElements.put("boolean:contains(/ns1:root/:search-element, 'search')", "contains");
        extractMessageElements.put("boolean:/ns1:root/:element", "exists");
        extractMessageElements.put("boolean:/ns1:root/:element-does-not-exist", "existsNot");
        
        receiveMessageBean.setExtractMessageElements(extractMessageElements);
        
        receiveMessageBean.execute(context);
        
        Assert.assertNotNull(context.getVariable("elementA"));
        Assert.assertEquals(context.getVariable("elementA"), "text-value");
        Assert.assertNotNull(context.getVariable("elementAttribute"));
        Assert.assertEquals(context.getVariable("elementAttribute"), "A");
        Assert.assertNotNull(context.getVariable("search"));
        Assert.assertEquals(context.getVariable("search"), "search-for");
        Assert.assertNotNull(context.getVariable("count"));
        Assert.assertEquals(context.getVariable("count"), "3.0");
        Assert.assertNotNull(context.getVariable("concat"));
        Assert.assertEquals(context.getVariable("concat"), "namespace is the value");
        Assert.assertNotNull(context.getVariable("localName"));
        Assert.assertEquals(context.getVariable("localName"), "root");
        Assert.assertNotNull(context.getVariable("namespaceUri"));
        Assert.assertEquals(context.getVariable("namespaceUri"), "http://testsuite");
        Assert.assertNotNull(context.getVariable("contains"));
        Assert.assertEquals(context.getVariable("contains"), "true");
        Assert.assertNotNull(context.getVariable("exists"));
        Assert.assertEquals(context.getVariable("exists"), "true");
        Assert.assertNotNull(context.getVariable("existsNot"));
        Assert.assertEquals(context.getVariable("existsNot"), "false");
    }
}
