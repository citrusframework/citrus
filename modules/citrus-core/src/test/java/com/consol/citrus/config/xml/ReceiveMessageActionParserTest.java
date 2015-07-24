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

import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.validation.json.*;
import com.consol.citrus.validation.script.ScriptValidationContext;
import com.consol.citrus.validation.xml.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.testng.AbstractActionParserTest;
import com.consol.citrus.validation.ControlMessageValidationContext;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.script.GroovyScriptMessageBuilder;
import com.consol.citrus.variable.*;

/**
 * @author Christoph Deppisch
 */
public class ReceiveMessageActionParserTest extends AbstractActionParserTest<ReceiveMessageAction> {

    @Test
    public void testReceiveMessageActionParser() {
        assertActionCount(15);
        assertActionClassAndName(ReceiveMessageAction.class, "receive");
        
        ControlMessageValidationContext validationContext;
        PayloadTemplateMessageBuilder messageBuilder;
        GroovyScriptMessageBuilder groovyMessageBuilder;
        
        // 1st action
        ReceiveMessageAction action = getNextTestActionFromTest();
        Assert.assertTrue(action.getMessageSelector().isEmpty());
        Assert.assertNull(action.getMessageSelectorString());
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class));
        Assert.assertNull(action.getEndpointUri());
        
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof ControlMessageValidationContext);
        validationContext = (ControlMessageValidationContext)action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        messageBuilder = (PayloadTemplateMessageBuilder)validationContext.getMessageBuilder();
        
        Assert.assertNull(messageBuilder.getPayloadResourcePath());
        Assert.assertNotNull(messageBuilder.getPayloadData());
        Assert.assertEquals(messageBuilder.getPayloadData().trim(), "<TestMessage>Hello Citrus</TestMessage>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 1);
        Assert.assertEquals(messageBuilder.getMessageHeaders().get("operation"), "Test");
        Assert.assertEquals(messageBuilder.getMessageInterceptors().size(), 0);

        Assert.assertNull(action.getDataDictionary());

        // 2nd action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getMessageSelector().size(), 1);
        Assert.assertEquals(action.getMessageSelector().get("operation"), "Test");
        Assert.assertNull(action.getMessageSelectorString());
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class));
        Assert.assertNull(action.getEndpointUri());
        
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof ControlMessageValidationContext);
        validationContext = (ControlMessageValidationContext)action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        messageBuilder = (PayloadTemplateMessageBuilder)validationContext.getMessageBuilder();
        
        Assert.assertNotNull(messageBuilder.getPayloadResourcePath());
        Assert.assertEquals(messageBuilder.getPayloadResourcePath(), "classpath:com/consol/citrus/actions/test-request-payload.xml");
        Assert.assertNull(messageBuilder.getPayloadData());
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0);
        Assert.assertEquals(messageBuilder.getMessageInterceptors().size(), 0);
        
        // 3rd action
        action = getNextTestActionFromTest();
        Assert.assertTrue(action.getMessageSelector().isEmpty());
        Assert.assertEquals(action.getMessageSelectorString(), "operation = 'Test'");
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class));
        Assert.assertNull(action.getEndpointUri());
        
        // 4th action
        action = getNextTestActionFromTest();
        Assert.assertTrue(action.getMessageSelector().isEmpty());
        Assert.assertNull(action.getMessageSelectorString());
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class));
        Assert.assertNull(action.getEndpointUri());
        
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof ControlMessageValidationContext);
        validationContext = (ControlMessageValidationContext)action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof GroovyScriptMessageBuilder);
        groovyMessageBuilder = (GroovyScriptMessageBuilder)validationContext.getMessageBuilder();
        
        Assert.assertNull(groovyMessageBuilder.getScriptResourcePath());
        Assert.assertNotNull(groovyMessageBuilder.getScriptData());
        Assert.assertEquals(groovyMessageBuilder.getScriptData().trim(), "println '<TestMessage>Hello Citrus</TestMessage>'");
        Assert.assertEquals(groovyMessageBuilder.getMessageHeaders().size(), 2);
        Assert.assertEquals(groovyMessageBuilder.getMessageHeaders().get("header1"), "Test");
        Assert.assertEquals(groovyMessageBuilder.getMessageHeaders().get("header2"), "Test");
        
        // 5th action
        action = getNextTestActionFromTest();
        Assert.assertTrue(action.getMessageSelector().isEmpty());
        Assert.assertNull(action.getMessageSelectorString());
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class));
        Assert.assertNull(action.getEndpointUri());
        
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof ControlMessageValidationContext);
        validationContext = (ControlMessageValidationContext)action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof GroovyScriptMessageBuilder);
        groovyMessageBuilder = (GroovyScriptMessageBuilder)validationContext.getMessageBuilder();
        
        Assert.assertNotNull(groovyMessageBuilder.getScriptResourcePath());
        Assert.assertEquals(groovyMessageBuilder.getScriptResourcePath(), "classpath:com/consol/citrus/script/example.groovy");
        Assert.assertNull(groovyMessageBuilder.getScriptData());
        
        // 6th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getVariableExtractors().size(), 2);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        MessageHeaderVariableExtractor headerVariableExtractor = (MessageHeaderVariableExtractor)action.getVariableExtractors().get(0);
        Assert.assertTrue(action.getVariableExtractors().get(1) instanceof XpathPayloadVariableExtractor);
        XpathPayloadVariableExtractor variableExtractor = (XpathPayloadVariableExtractor)action.getVariableExtractors().get(1);
        
        Assert.assertEquals(variableExtractor.getNamespaces().size(), 0L);
        Assert.assertEquals(headerVariableExtractor.getHeaderMappings().size(), 1);
        Assert.assertEquals(headerVariableExtractor.getHeaderMappings().get("operation"), "operation");
        Assert.assertEquals(variableExtractor.getXpathExpressions().size(), 1);
        Assert.assertEquals(variableExtractor.getXpathExpressions().get("/TestMessage/text()"), "text");

        Assert.assertNotNull(action.getDataDictionary());

        // 7th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof XmlMessageValidationContext);
        XmlMessageValidationContext xmlValidationContext = (XmlMessageValidationContext)action.getValidationContexts().get(0);

        Assert.assertTrue(xmlValidationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        messageBuilder = (PayloadTemplateMessageBuilder)xmlValidationContext.getMessageBuilder();
        
        Assert.assertNull(messageBuilder.getPayloadResourcePath());
        Assert.assertNotNull(messageBuilder.getPayloadData());
        Assert.assertEquals(messageBuilder.getPayloadData().trim(), "<ns:TestMessage xmlns:ns=\"http://www.consol.com\">Hello Citrus</ns:TestMessage>");
        
        Assert.assertEquals(messageBuilder.getMessageInterceptors().size(), 1);
        Assert.assertTrue(messageBuilder.getMessageInterceptors().get(0) instanceof XpathMessageConstructionInterceptor);
        XpathMessageConstructionInterceptor messageConstructionInterceptor = (XpathMessageConstructionInterceptor)messageBuilder.getMessageInterceptors().get(0);
        
        Assert.assertEquals(messageConstructionInterceptor.getXPathExpressions().size(), 1);
        Assert.assertEquals(messageConstructionInterceptor.getXPathExpressions().get("/ns:TestMessage/"), "newValue");
        
        Assert.assertEquals(xmlValidationContext.isSchemaValidationEnabled(), false);
        
        Assert.assertEquals(xmlValidationContext.getIgnoreExpressions().size(), 1);
        Assert.assertEquals(xmlValidationContext.getIgnoreExpressions().iterator().next(), "/ns:TestMessage/ns:ignore");
        Assert.assertEquals(xmlValidationContext.getNamespaces().size(), 1);
        Assert.assertEquals(xmlValidationContext.getNamespaces().get("ns"), "http://www.consol.com");
        
        // 8th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 2);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XpathMessageValidationContext);
        xmlValidationContext = (XmlMessageValidationContext)action.getValidationContexts().get(0);
        XpathMessageValidationContext xPathValidationContext = (XpathMessageValidationContext)action.getValidationContexts().get(1);
        Assert.assertNull(action.getEndpoint());
        Assert.assertEquals(action.getEndpointUri(), "channel:myMessageEndpoint");

        Assert.assertEquals(xmlValidationContext.isSchemaValidationEnabled(), true);

        Assert.assertEquals(xPathValidationContext.getXpathExpressions().size(), 2);
        Assert.assertEquals(xPathValidationContext.getXpathExpressions().get("/TestMessage/text"), "Hello Citrus");
        Assert.assertEquals(xPathValidationContext.getXpathExpressions().get("boolean:/TestMessage/foo"), "true");

        // 9th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 2);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XpathMessageValidationContext);
        xmlValidationContext = (XmlMessageValidationContext)action.getValidationContexts().get(0);
        xPathValidationContext = (XpathMessageValidationContext)action.getValidationContexts().get(1);
        Assert.assertNull(action.getEndpoint());
        Assert.assertEquals(action.getEndpointUri(), "channel:myMessageEndpoint");

        Assert.assertEquals(xmlValidationContext.isSchemaValidationEnabled(), true);

        Assert.assertEquals(xPathValidationContext.getXpathExpressions().size(), 2);
        Assert.assertEquals(xPathValidationContext.getXpathExpressions().get("/TestMessage/text"), "Hello Citrus");
        Assert.assertEquals(xPathValidationContext.getXpathExpressions().get("boolean:/TestMessage/foo"), "true");

        // 10th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XpathMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof ScriptValidationContext);
        xmlValidationContext = (XmlMessageValidationContext)action.getValidationContexts().get(0);
        xPathValidationContext = (XpathMessageValidationContext)action.getValidationContexts().get(1);
        ScriptValidationContext scriptValidationContext = (ScriptValidationContext)action.getValidationContexts().get(2);
        Assert.assertNull(action.getEndpoint());
        Assert.assertEquals(action.getEndpointUri(), "channel:myMessageEndpoint");

        Assert.assertEquals(xmlValidationContext.isSchemaValidationEnabled(), true);

        Assert.assertEquals(xPathValidationContext.getXpathExpressions().size(), 1);
        Assert.assertEquals(xPathValidationContext.getXpathExpressions().get("boolean:/TestMessage/foo"), "true");

        Assert.assertEquals(scriptValidationContext.getScriptType(), "groovy");
        Assert.assertEquals(scriptValidationContext.getValidationScript().trim(), "assert true");

        // 11th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 2);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof ScriptValidationContext);
        xmlValidationContext = (XmlMessageValidationContext)action.getValidationContexts().get(0);
        scriptValidationContext = (ScriptValidationContext)action.getValidationContexts().get(1);
        Assert.assertNull(action.getEndpoint());
        Assert.assertEquals(action.getEndpointUri(), "channel:myMessageEndpoint");

        Assert.assertEquals(xmlValidationContext.isSchemaValidationEnabled(), true);

        Assert.assertEquals(scriptValidationContext.getScriptType(), "groovy");
        Assert.assertEquals(scriptValidationContext.getValidationScriptResourcePath(), "classpath:com/consol/citrus/actions/test-validation-script.groovy");

        // 12th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 2);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof JsonMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof JsonPathMessageValidationContext);
        JsonMessageValidationContext jsonValidationContext = (JsonMessageValidationContext)action.getValidationContexts().get(0);
        JsonPathMessageValidationContext jsonPathValidationContext = (JsonPathMessageValidationContext)action.getValidationContexts().get(1);
        Assert.assertNull(action.getEndpoint());
        Assert.assertEquals(action.getEndpointUri(), "channel:myMessageEndpoint");

        Assert.assertTrue(jsonValidationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);

        Assert.assertEquals(jsonPathValidationContext.getJsonPathExpressions().size(), 2);
        Assert.assertEquals(jsonPathValidationContext.getJsonPathExpressions().get("$.json.text"), "Hello Citrus");
        Assert.assertEquals(jsonPathValidationContext.getJsonPathExpressions().get("$..foo.bar"), "true");

        // 13th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 2);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof JsonMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof JsonPathMessageValidationContext);
        jsonValidationContext = (JsonMessageValidationContext)action.getValidationContexts().get(0);
        jsonPathValidationContext = (JsonPathMessageValidationContext)action.getValidationContexts().get(1);
        Assert.assertNull(action.getEndpoint());
        Assert.assertEquals(action.getEndpointUri(), "channel:myMessageEndpoint");

        Assert.assertTrue(jsonValidationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);

        Assert.assertEquals(jsonPathValidationContext.getJsonPathExpressions().size(), 2);
        Assert.assertEquals(jsonPathValidationContext.getJsonPathExpressions().get("$.json.text"), "Hello Citrus");
        Assert.assertEquals(jsonPathValidationContext.getJsonPathExpressions().get("$..foo.bar"), "true");

        // 14th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof JsonMessageValidationContext);
        jsonValidationContext = (JsonMessageValidationContext)action.getValidationContexts().get(0);

        Assert.assertTrue(jsonValidationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        messageBuilder = (PayloadTemplateMessageBuilder)jsonValidationContext.getMessageBuilder();

        Assert.assertNull(messageBuilder.getPayloadResourcePath());
        Assert.assertNotNull(messageBuilder.getPayloadData());
        Assert.assertEquals(messageBuilder.getPayloadData().trim(), "{ \"FooMessage\": { \"foo\": \"Hello World!\" }, { \"bar\": \"@ignore@\" }}");

        Assert.assertEquals(messageBuilder.getMessageInterceptors().size(), 1);
        Assert.assertTrue(messageBuilder.getMessageInterceptors().get(0) instanceof JsonPathMessageConstructionInterceptor);
        JsonPathMessageConstructionInterceptor jsonMessageConstructionInterceptor = (JsonPathMessageConstructionInterceptor)messageBuilder.getMessageInterceptors().get(0);

        Assert.assertEquals(jsonMessageConstructionInterceptor.getJsonPathExpressions().size(), 1);
        Assert.assertEquals(jsonMessageConstructionInterceptor.getJsonPathExpressions().get("$.FooMessage.foo"), "newValue");

        Assert.assertEquals(jsonValidationContext.getIgnoreExpressions().size(), 1);
        Assert.assertEquals(jsonValidationContext.getIgnoreExpressions().iterator().next(), "$.FooMessage.bar");

        // 15th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getVariableExtractors().size(), 2);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        headerVariableExtractor = (MessageHeaderVariableExtractor)action.getVariableExtractors().get(0);
        Assert.assertTrue(action.getVariableExtractors().get(1) instanceof JsonPathVariableExtractor);
        JsonPathVariableExtractor jsonVariableExtractor = (JsonPathVariableExtractor)action.getVariableExtractors().get(1);

        Assert.assertEquals(headerVariableExtractor.getHeaderMappings().size(), 1);
        Assert.assertEquals(headerVariableExtractor.getHeaderMappings().get("operation"), "operation");
        Assert.assertEquals(jsonVariableExtractor.getJsonPathExpressions().size(), 1);
        Assert.assertEquals(jsonVariableExtractor.getJsonPathExpressions().get("$.message.text"), "text");
    }
}
