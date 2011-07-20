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

package com.consol.citrus.config.xml;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.testng.AbstractBeanDefinitionParserBaseTest;
import com.consol.citrus.validation.ControlMessageValidationContext;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.interceptor.XpathMessageConstructionInterceptor;
import com.consol.citrus.validation.script.GroovyScriptMessageBuilder;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import com.consol.citrus.variable.*;

/**
 * @author Christoph Deppisch
 */
public class ReceiveMessageActionParserTest extends AbstractBeanDefinitionParserBaseTest<ReceiveMessageAction> {

    @Test
    public void testReceiveMessageActionParser() {
        assertActionCount(8);
        assertActionClassAndName(ReceiveMessageAction.class, "receive");
        
        ControlMessageValidationContext validationContext;
        PayloadTemplateMessageBuilder messageBuilder;
        GroovyScriptMessageBuilder groovyMessageBuilder;
        
        // 1st action
        ReceiveMessageAction action = getNextTestActionFromTest();
        Assert.assertTrue(action.getMessageSelector().isEmpty());
        Assert.assertNull(action.getMessageSelectorString());
        
        Assert.assertEquals(action.getValidationContexts().size(), 2);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof ControlMessageValidationContext);
        validationContext = (ControlMessageValidationContext)action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        messageBuilder = (PayloadTemplateMessageBuilder)validationContext.getMessageBuilder();
        
        Assert.assertNull(messageBuilder.getPayloadResource());
        Assert.assertNotNull(messageBuilder.getPayloadData());
        Assert.assertEquals(messageBuilder.getPayloadData().trim(), "<TestMessage>Hello Citrus</TestMessage>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 1);
        Assert.assertEquals(messageBuilder.getMessageHeaders().get("operation"), "Test");
        Assert.assertEquals(messageBuilder.getMessageInterceptors().size(), 0);
        
        // 2nd action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getMessageSelector().size(), 1);
        Assert.assertEquals(action.getMessageSelector().get("operation"), "Test");
        Assert.assertNull(action.getMessageSelectorString());
        
        Assert.assertEquals(action.getValidationContexts().size(), 2);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof ControlMessageValidationContext);
        validationContext = (ControlMessageValidationContext)action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        messageBuilder = (PayloadTemplateMessageBuilder)validationContext.getMessageBuilder();
        
        Assert.assertNotNull(messageBuilder.getPayloadResource());
        Assert.assertEquals(messageBuilder.getPayloadResource().getFilename(), "test-request-payload.xml");
        Assert.assertNull(messageBuilder.getPayloadData());
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0);
        Assert.assertEquals(messageBuilder.getMessageInterceptors().size(), 0);
        
        // 3rd action
        action = getNextTestActionFromTest();
        Assert.assertTrue(action.getMessageSelector().isEmpty());
        Assert.assertEquals(action.getMessageSelectorString(), "operation = 'Test'");
        
        // 4th action
        action = getNextTestActionFromTest();
        Assert.assertTrue(action.getMessageSelector().isEmpty());
        Assert.assertNull(action.getMessageSelectorString());
        
        Assert.assertEquals(action.getValidationContexts().size(), 2);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof ControlMessageValidationContext);
        validationContext = (ControlMessageValidationContext)action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof GroovyScriptMessageBuilder);
        groovyMessageBuilder = (GroovyScriptMessageBuilder)validationContext.getMessageBuilder();
        
        Assert.assertNull(groovyMessageBuilder.getScriptResource());
        Assert.assertNotNull(groovyMessageBuilder.getScriptData());
        Assert.assertEquals(groovyMessageBuilder.getScriptData().trim(), "println '<TestMessage>Hello Citrus</TestMessage>'");
        Assert.assertEquals(groovyMessageBuilder.getMessageHeaders().size(), 2);
        Assert.assertEquals(groovyMessageBuilder.getMessageHeaders().get("header1"), "Test");
        Assert.assertEquals(groovyMessageBuilder.getMessageHeaders().get("header2"), "Test");
        
        // 5th action
        action = getNextTestActionFromTest();
        Assert.assertTrue(action.getMessageSelector().isEmpty());
        Assert.assertNull(action.getMessageSelectorString());
        
        Assert.assertEquals(action.getValidationContexts().size(), 2);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof ControlMessageValidationContext);
        validationContext = (ControlMessageValidationContext)action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof GroovyScriptMessageBuilder);
        groovyMessageBuilder = (GroovyScriptMessageBuilder)validationContext.getMessageBuilder();
        
        Assert.assertNotNull(groovyMessageBuilder.getScriptResource());
        Assert.assertEquals(groovyMessageBuilder.getScriptResource().getFilename(), "example.groovy");
        Assert.assertNull(groovyMessageBuilder.getScriptData());
        
        // 6th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getVariableExtractors().size(), 2);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        MessageHeaderVariableExtractor headerVariableExtractor = (MessageHeaderVariableExtractor)action.getVariableExtractors().get(0);
        Assert.assertTrue(action.getVariableExtractors().get(1) instanceof XpathPayloadVariableExtractor);
        XpathPayloadVariableExtractor variableExtractor = (XpathPayloadVariableExtractor)action.getVariableExtractors().get(1);
        
        Assert.assertNull(variableExtractor.getNamespaces());
        Assert.assertEquals(headerVariableExtractor.getHeaderMappings().size(), 1);
        Assert.assertEquals(headerVariableExtractor.getHeaderMappings().get("operation"), "operation");
        Assert.assertEquals(variableExtractor.getxPathExpressions().size(), 1);
        Assert.assertEquals(variableExtractor.getxPathExpressions().get("/TestMessage/text()"), "text");
        
        // 7th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 2);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof XmlMessageValidationContext);
        XmlMessageValidationContext xmlValidationContext = (XmlMessageValidationContext)action.getValidationContexts().get(0);
        
        Assert.assertTrue(xmlValidationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        messageBuilder = (PayloadTemplateMessageBuilder)xmlValidationContext.getMessageBuilder();
        
        Assert.assertNull(messageBuilder.getPayloadResource());
        Assert.assertNotNull(messageBuilder.getPayloadData());
        Assert.assertEquals(messageBuilder.getPayloadData().trim(), "<ns:TestMessage xmlns:ns=\"http://www.consol.com\">Hello Citrus</ns:TestMessage>");
        
        Assert.assertEquals(messageBuilder.getMessageInterceptors().size(), 1);
        Assert.assertTrue(messageBuilder.getMessageInterceptors().get(0) instanceof XpathMessageConstructionInterceptor);
        XpathMessageConstructionInterceptor messageConstructionInterceptor = (XpathMessageConstructionInterceptor)messageBuilder.getMessageInterceptors().get(0);
        
        Assert.assertEquals(messageConstructionInterceptor.getxPathExpressions().size(), 1);
        Assert.assertEquals(messageConstructionInterceptor.getxPathExpressions().get("/ns:TestMessage/"), "newValue");
        
        Assert.assertEquals(xmlValidationContext.isSchemaValidationEnabled(), false);
        
        Assert.assertNull(xmlValidationContext.getPathValidationExpressions());
        Assert.assertEquals(xmlValidationContext.getIgnoreExpressions().size(), 1);
        Assert.assertEquals(xmlValidationContext.getIgnoreExpressions().iterator().next(), "/ns:TestMessage/ns:ignore");
        Assert.assertEquals(xmlValidationContext.getNamespaces().size(), 1);
        Assert.assertEquals(xmlValidationContext.getNamespaces().get("ns"), "http://www.consol.com");
        
        // 8th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 2);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof XmlMessageValidationContext);
        xmlValidationContext = (XmlMessageValidationContext)action.getValidationContexts().get(0);
        
        Assert.assertEquals(xmlValidationContext.isSchemaValidationEnabled(), true);
        
        Assert.assertEquals(xmlValidationContext.getPathValidationExpressions().size(), 2);
        Assert.assertEquals(xmlValidationContext.getPathValidationExpressions().get("/TestMessage/text"), "Hello Citrus");
        Assert.assertEquals(xmlValidationContext.getPathValidationExpressions().get("boolean:/TestMessage/foo"), "true");
    }
}
