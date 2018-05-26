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

import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.testng.AbstractActionParserTest;
import com.consol.citrus.validation.*;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.context.HeaderValidationContext;
import com.consol.citrus.validation.json.*;
import com.consol.citrus.validation.script.GroovyScriptMessageBuilder;
import com.consol.citrus.validation.script.ScriptValidationContext;
import com.consol.citrus.validation.xml.*;
import com.consol.citrus.variable.MessageHeaderVariableExtractor;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class ReceiveMessageActionParserTest extends AbstractActionParserTest<ReceiveMessageAction> {

    @Test
    public void testReceiveMessageActionParser() {
        assertActionCount(18);
        assertActionClassAndName(ReceiveMessageAction.class, "receive");
        
        PayloadTemplateMessageBuilder messageBuilder;
        GroovyScriptMessageBuilder groovyMessageBuilder;
        
        // 1st action
        ReceiveMessageAction action = getNextTestActionFromTest();
        Assert.assertTrue(action.getMessageSelectorMap().isEmpty());
        Assert.assertNull(action.getMessageSelector());
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class));
        Assert.assertNull(action.getEndpointUri());
        
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof JsonMessageValidationContext);

        Assert.assertTrue(action.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        messageBuilder = (PayloadTemplateMessageBuilder)action.getMessageBuilder();
        
        Assert.assertNull(messageBuilder.getPayloadResourcePath());
        Assert.assertNotNull(messageBuilder.getPayloadData());
        Assert.assertEquals(messageBuilder.getPayloadData().trim(), "<TestMessage>Hello Citrus</TestMessage>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 1);
        Assert.assertEquals(messageBuilder.getMessageHeaders().get("operation"), "Test");
        Assert.assertEquals(messageBuilder.getMessageInterceptors().size(), 0);

        Assert.assertNull(action.getDataDictionary());
        Assert.assertEquals(action.getVariableExtractors().size(), 0);

        // 2nd action
        action = getNextTestActionFromTest();
        Assert.assertTrue(action.getMessageSelectorMap().isEmpty());
        Assert.assertNull(action.getMessageSelector());
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class));
        Assert.assertNull(action.getEndpointUri());

        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof JsonMessageValidationContext);

        Assert.assertTrue(action.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        messageBuilder = (PayloadTemplateMessageBuilder)action.getMessageBuilder();

        Assert.assertNull(messageBuilder.getPayloadResourcePath());
        Assert.assertNotNull(messageBuilder.getPayloadData());
        Assert.assertEquals(messageBuilder.getPayloadData().trim(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestMessage xmlns=\"http://citrusframework.org/test\">Hello Citrus</TestMessage>");
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 1);
        Assert.assertEquals(messageBuilder.getMessageHeaders().get("operation"), "Test");
        Assert.assertEquals(messageBuilder.getHeaderData().size(), 1);
        Assert.assertEquals(messageBuilder.getHeaderData().get(0).trim(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Header xmlns=\"http://citrusframework.org/test\">\n   <operation>hello</operation>\n</Header>");
        Assert.assertEquals(messageBuilder.getMessageInterceptors().size(), 0);

        Assert.assertNull(action.getDataDictionary());
        Assert.assertEquals(action.getVariableExtractors().size(), 0);

        // 3rd action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getMessageSelectorMap().size(), 1);
        Assert.assertEquals(action.getMessageSelectorMap().get("operation"), "Test");
        Assert.assertNull(action.getMessageSelector());
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class));
        Assert.assertNull(action.getEndpointUri());
        
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof JsonMessageValidationContext);

        Assert.assertTrue(action.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        messageBuilder = (PayloadTemplateMessageBuilder)action.getMessageBuilder();
        
        Assert.assertNotNull(messageBuilder.getPayloadResourcePath());
        Assert.assertEquals(messageBuilder.getPayloadResourcePath(), "classpath:com/consol/citrus/actions/test-request-payload.xml");
        Assert.assertNull(messageBuilder.getPayloadData());
        Assert.assertEquals(messageBuilder.getMessageHeaders().size(), 0);
        Assert.assertEquals(messageBuilder.getMessageInterceptors().size(), 0);
        
        // 4th action
        action = getNextTestActionFromTest();
        Assert.assertTrue(action.getMessageSelectorMap().isEmpty());
        Assert.assertEquals(action.getMessageSelector(), "operation = 'Test'");
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class));
        Assert.assertNull(action.getEndpointUri());
        
        // 5th action
        action = getNextTestActionFromTest();
        Assert.assertTrue(action.getMessageSelectorMap().isEmpty());
        Assert.assertNull(action.getMessageSelector());
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class));
        Assert.assertNull(action.getEndpointUri());
        
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof JsonMessageValidationContext);

        Assert.assertTrue(action.getMessageBuilder() instanceof GroovyScriptMessageBuilder);
        groovyMessageBuilder = (GroovyScriptMessageBuilder)action.getMessageBuilder();
        
        Assert.assertNull(groovyMessageBuilder.getScriptResourcePath());
        Assert.assertNotNull(groovyMessageBuilder.getScriptData());
        Assert.assertEquals(groovyMessageBuilder.getScriptData().trim(), "println '<TestMessage>Hello Citrus</TestMessage>'");
        Assert.assertEquals(groovyMessageBuilder.getMessageHeaders().size(), 2);
        Assert.assertEquals(groovyMessageBuilder.getMessageHeaders().get("header1"), "Test");
        Assert.assertEquals(groovyMessageBuilder.getMessageHeaders().get("header2"), "Test");
        
        // 6th action
        action = getNextTestActionFromTest();
        Assert.assertTrue(action.getMessageSelectorMap().isEmpty());
        Assert.assertNull(action.getMessageSelector());
        Assert.assertEquals(action.getEndpoint(), beanDefinitionContext.getBean("myMessageEndpoint", Endpoint.class));
        Assert.assertNull(action.getEndpointUri());
        
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof JsonMessageValidationContext);

        Assert.assertTrue(action.getMessageBuilder() instanceof GroovyScriptMessageBuilder);
        groovyMessageBuilder = (GroovyScriptMessageBuilder)action.getMessageBuilder();
        
        Assert.assertNotNull(groovyMessageBuilder.getScriptResourcePath());
        Assert.assertEquals(groovyMessageBuilder.getScriptResourcePath(), "classpath:com/consol/citrus/script/example.groovy");
        Assert.assertNull(groovyMessageBuilder.getScriptData());

        // 7th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getVariableExtractors().size(), 2);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        MessageHeaderVariableExtractor headerVariableExtractor = (MessageHeaderVariableExtractor)action.getVariableExtractors().get(0);
        Assert.assertTrue(action.getVariableExtractors().get(1) instanceof DefaultPayloadVariableExtractor);
        DefaultPayloadVariableExtractor variableExtractor = (DefaultPayloadVariableExtractor)action.getVariableExtractors().get(1);
        
        Assert.assertEquals(variableExtractor.getNamespaces().size(), 0L);
        Assert.assertEquals(headerVariableExtractor.getHeaderMappings().size(), 1);
        Assert.assertEquals(headerVariableExtractor.getHeaderMappings().get("operation"), "operation");
        Assert.assertEquals(variableExtractor.getPathExpressions().size(), 1);
        Assert.assertEquals(variableExtractor.getPathExpressions().get("/TestMessage/text()"), "text");

        Assert.assertNotNull(action.getDataDictionary());

        // 8th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof JsonMessageValidationContext);

        XmlMessageValidationContext xmlValidationContext = (XmlMessageValidationContext)action.getValidationContexts().get(1);
        Assert.assertTrue(action.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        messageBuilder = (PayloadTemplateMessageBuilder)action.getMessageBuilder();
        
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
        
        // 9th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 4);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof XpathMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(3) instanceof JsonMessageValidationContext);
        xmlValidationContext = (XmlMessageValidationContext)action.getValidationContexts().get(1);
        XpathMessageValidationContext xPathValidationContext = (XpathMessageValidationContext)action.getValidationContexts().get(2);
        Assert.assertNull(action.getEndpoint());
        Assert.assertEquals(action.getEndpointUri(), "channel:myMessageEndpoint");

        Assert.assertEquals(xmlValidationContext.isSchemaValidationEnabled(), true);

        Assert.assertEquals(xPathValidationContext.getXpathExpressions().size(), 2);
        Assert.assertEquals(xPathValidationContext.getXpathExpressions().get("/TestMessage/text"), "Hello Citrus");
        Assert.assertEquals(xPathValidationContext.getXpathExpressions().get("boolean:/TestMessage/foo"), "true");

        // 10th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 4);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof XpathMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(3) instanceof JsonMessageValidationContext);
        xmlValidationContext = (XmlMessageValidationContext)action.getValidationContexts().get(1);
        xPathValidationContext = (XpathMessageValidationContext)action.getValidationContexts().get(2);
        Assert.assertNull(action.getEndpoint());
        Assert.assertEquals(action.getEndpointUri(), "channel:myMessageEndpoint");

        Assert.assertEquals(xmlValidationContext.isSchemaValidationEnabled(), true);

        Assert.assertEquals(xPathValidationContext.getXpathExpressions().size(), 2);
        Assert.assertEquals(xPathValidationContext.getXpathExpressions().get("/TestMessage/text"), "Hello Citrus");
        Assert.assertEquals(xPathValidationContext.getXpathExpressions().get("boolean:/TestMessage/foo"), "true");

        // 11th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 5);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof XpathMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(3) instanceof JsonMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(4) instanceof ScriptValidationContext);
        xmlValidationContext = (XmlMessageValidationContext)action.getValidationContexts().get(1);
        xPathValidationContext = (XpathMessageValidationContext)action.getValidationContexts().get(2);
        ScriptValidationContext scriptValidationContext = (ScriptValidationContext)action.getValidationContexts().get(4);
        Assert.assertNull(action.getEndpoint());
        Assert.assertEquals(action.getEndpointUri(), "channel:myMessageEndpoint");

        Assert.assertEquals(xmlValidationContext.isSchemaValidationEnabled(), true);

        Assert.assertEquals(xPathValidationContext.getXpathExpressions().size(), 1);
        Assert.assertEquals(xPathValidationContext.getXpathExpressions().get("boolean:/TestMessage/foo"), "true");

        Assert.assertEquals(scriptValidationContext.getScriptType(), "groovy");
        Assert.assertEquals(scriptValidationContext.getValidationScript().trim(), "assert true");

        // 12th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 4);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof JsonMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(3) instanceof ScriptValidationContext);
        xmlValidationContext = (XmlMessageValidationContext)action.getValidationContexts().get(1);
        scriptValidationContext = (ScriptValidationContext)action.getValidationContexts().get(3);
        Assert.assertNull(action.getEndpoint());
        Assert.assertEquals(action.getEndpointUri(), "channel:myMessageEndpoint");

        Assert.assertEquals(xmlValidationContext.isSchemaValidationEnabled(), true);

        Assert.assertEquals(scriptValidationContext.getScriptType(), "groovy");
        Assert.assertEquals(scriptValidationContext.getValidationScriptResourcePath(), "classpath:com/consol/citrus/actions/test-validation-script.groovy");

        // 13th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 4);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof JsonMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(3) instanceof JsonPathMessageValidationContext);
        JsonPathMessageValidationContext jsonPathValidationContext = (JsonPathMessageValidationContext)action.getValidationContexts().get(3);
        Assert.assertNull(action.getEndpoint());
        Assert.assertEquals(action.getEndpointUri(), "channel:myMessageEndpoint");

        Assert.assertTrue(action.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);

        Assert.assertEquals(jsonPathValidationContext.getJsonPathExpressions().size(), 2);
        Assert.assertEquals(jsonPathValidationContext.getJsonPathExpressions().get("$.json.text"), "Hello Citrus");
        Assert.assertEquals(jsonPathValidationContext.getJsonPathExpressions().get("$..foo.bar"), "true");

        // 14th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 4);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof JsonMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(3) instanceof JsonPathMessageValidationContext);
        jsonPathValidationContext = (JsonPathMessageValidationContext)action.getValidationContexts().get(3);
        Assert.assertNull(action.getEndpoint());
        Assert.assertEquals(action.getEndpointUri(), "channel:myMessageEndpoint");

        Assert.assertTrue(action.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);

        Assert.assertEquals(jsonPathValidationContext.getJsonPathExpressions().size(), 2);
        Assert.assertEquals(jsonPathValidationContext.getJsonPathExpressions().get("$.json.text"), "Hello Citrus");
        Assert.assertEquals(jsonPathValidationContext.getJsonPathExpressions().get("$..foo.bar"), "true");

        // 15th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidationContexts().size(), 3);
        Assert.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(1) instanceof XmlMessageValidationContext);
        Assert.assertTrue(action.getValidationContexts().get(2) instanceof JsonMessageValidationContext);
        JsonMessageValidationContext jsonValidationContext = (JsonMessageValidationContext)action.getValidationContexts().get(2);

        Assert.assertTrue(action.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        messageBuilder = (PayloadTemplateMessageBuilder)action.getMessageBuilder();

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

        // 16th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getVariableExtractors().size(), 2);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        headerVariableExtractor = (MessageHeaderVariableExtractor)action.getVariableExtractors().get(0);
        Assert.assertTrue(action.getVariableExtractors().get(1) instanceof DefaultPayloadVariableExtractor);
        DefaultPayloadVariableExtractor jsonVariableExtractor = (DefaultPayloadVariableExtractor) action.getVariableExtractors().get(1);

        Assert.assertEquals(headerVariableExtractor.getHeaderMappings().size(), 1);
        Assert.assertEquals(headerVariableExtractor.getHeaderMappings().get("operation"), "operation");
        Assert.assertEquals(jsonVariableExtractor.getPathExpressions().size(), 1);
        Assert.assertEquals(jsonVariableExtractor.getPathExpressions().get("$.message.text"), "text");

        // 17th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidators().size(), 1);
        Assert.assertEquals(action.getValidators().get(0), beanDefinitionContext.getBean("myValidator", MessageValidator.class));
        HeaderValidationContext headerValidationContext = (HeaderValidationContext) action.getValidationContexts().get(0);
        Assert.assertEquals(headerValidationContext.getValidatorNames().size(), 1);
        Assert.assertEquals(headerValidationContext.getValidatorNames().get(0), "myHeaderValidator");

        // 18th action
        action = getNextTestActionFromTest();
        Assert.assertEquals(action.getValidators().size(), 2);
        Assert.assertEquals(action.getValidators().get(0), beanDefinitionContext.getBean("myValidator", MessageValidator.class));
        Assert.assertEquals(action.getValidators().get(1), beanDefinitionContext.getBean("defaultPlaintextMessageValidator", MessageValidator.class));
        headerValidationContext = (HeaderValidationContext) action.getValidationContexts().get(0);
        Assert.assertEquals(headerValidationContext.getValidatorNames().size(), 2);
        Assert.assertEquals(headerValidationContext.getValidatorNames().get(0), "myHeaderValidator");
        Assert.assertEquals(headerValidationContext.getValidatorNames().get(1), "defaultHeaderValidator");
    }
}
