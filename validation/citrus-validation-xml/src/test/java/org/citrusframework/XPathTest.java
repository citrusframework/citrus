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

package org.citrusframework;

import java.util.HashMap;
import java.util.Map;

import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.xml.XmlMessageValidationContext;
import org.citrusframework.validation.xml.XpathMessageValidationContext;
import org.citrusframework.validation.xml.XpathPayloadVariableExtractor;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class XPathTest extends UnitTestSupport {
    private Endpoint endpoint = Mockito.mock(Endpoint.class);
    private Consumer consumer = Mockito.mock(Consumer.class);
    private EndpointConfiguration endpointConfiguration = Mockito.mock(EndpointConfiguration.class);

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testUsingXPath() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<ns1:root xmlns='http://test' xmlns:ns1='http://citrus'>"
                            + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                            + "</element>"
                            + "<ns1:ns-element>namespace</ns1:ns-element>"
                            + "<search-element>search-for</search-element>"
                        + "</ns1:root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        HashMap<String, Object> validateMessageElements = new HashMap<>();
        validateMessageElements.put("//:element/:sub-elementA", "text-value");
        validateMessageElements.put("//:element/:sub-elementA[@attribute='A']", "text-value");
        validateMessageElements.put("//:element/:sub-elementB", "text-value");
        validateMessageElements.put("//:element/:sub-elementB/@attribute", "B");
        validateMessageElements.put("//ns1:ns-element", "namespace");
        validateMessageElements.put("//*[.='search-for']", "search-for");

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext.Builder()
                .schemaValidation(false)
                .expressions(validateMessageElements)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .validate(validationContext)
                .build();
        receiveAction.execute(context);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testUsingXPathWithDefaultNamespace() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root xmlns='http://test'>"
                            + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                            + "</element>"
                            + "<ns-element>namespace</ns-element>"
                            + "<search-element>search-for</search-element>"
                        + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        HashMap<String, Object> validateMessageElements = new HashMap<>();
        validateMessageElements.put("//:element/:sub-elementA", "text-value");
        validateMessageElements.put("//:element/:sub-elementA[@attribute='A']", "text-value");
        validateMessageElements.put("//:element/:sub-elementB", "text-value");
        validateMessageElements.put("//:element/:sub-elementB/@attribute", "B");
        validateMessageElements.put("//:ns-element", "namespace");
        validateMessageElements.put("//*[.='search-for']", "search-for");

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext.Builder()
                .schemaValidation(false)
                .expressions(validateMessageElements)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .validate(validationContext)
                .build();
        receiveAction.execute(context);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testUsingXPathWithExplicitNamespace() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root xmlns='http://test' xmlns:ns1='http://citrus'>"
                            + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                            + "</element>"
                            + "<ns1:ns-element>namespace</ns1:ns-element>"
                            + "<search-element>search-for</search-element>"
                        + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        HashMap<String, Object> validateMessageElements = new HashMap<>();
        validateMessageElements.put("//:element/:sub-elementA", "text-value");
        validateMessageElements.put("//ns1:ns-element", "namespace");

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext.Builder()
                .schemaValidation(false)
                .expressions(validateMessageElements)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .validate(validationContext)
                .build();
        receiveAction.execute(context);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testUsingXPathWithExplicitNamespaceInElementDefinition() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root xmlns='http://test'>"
                            + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                            + "</element>"
                            + "<ns1:ns-element xmlns:ns1='http://citrus'>namespace</ns1:ns-element>"
                            + "<search-element>search-for</search-element>"
                        + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        HashMap<String, Object> validateMessageElements = new HashMap<>();
        validateMessageElements.put("//:element/:sub-elementA", "text-value");
        validateMessageElements.put("//ns1:ns-element", "namespace");

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();

        Map<String, String> namespaces = new HashMap<>();
        namespaces.put("ns1", "http://citrus");

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext.Builder()
                .schemaValidation(false)
                .expressions(validateMessageElements)
                .namespaceContext(namespaces)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .validate(validationContext)
                .build();
        receiveAction.execute(context);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testValidateMessageElementsUsingXPathWithResultTypes() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<ns1:root xmlns='http://test' xmlns:ns1='http://citrus'>"
                            + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                            + "</element>"
                            + "<ns1:ns-element>namespace</ns1:ns-element>"
                            + "<search-element>search-for</search-element>"
                        + "</ns1:root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        HashMap<String, Object> validateMessageElements = new HashMap<>();
        validateMessageElements.put("node://:element/:sub-elementA", "text-value");
        validateMessageElements.put("node://:element/:sub-elementA[@attribute='A']", "text-value");
        validateMessageElements.put("node://:element/:sub-elementB", "text-value");
        validateMessageElements.put("node://:element/:sub-elementB/@attribute", "B");
        validateMessageElements.put("node://ns1:ns-element", "namespace");
        validateMessageElements.put("node://*[.='search-for']", "search-for");
        validateMessageElements.put("number:count(/ns1:root/:element/*)", "3.0");
        validateMessageElements.put("string:concat(/ns1:root/ns1:ns-element, ' is the value')", "namespace is the value");
        validateMessageElements.put("string:local-name(/*)", "root");
        validateMessageElements.put("string:namespace-uri(/*)", "http://citrus");
        validateMessageElements.put("boolean:contains(/ns1:root/:search-element, 'search')", "true");
        validateMessageElements.put("boolean:/ns1:root/:element", "true");
        validateMessageElements.put("boolean:/ns1:root/:element-does-not-exist", "false");

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext.Builder()
                .schemaValidation(false)
                .expressions(validateMessageElements)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .validate(validationContext)
                .build();
        receiveAction.execute(context);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testExtractMessageValuesUsingXPathWithResultTypes() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<ns1:root xmlns='http://test' xmlns:ns1='http://citrus'>"
                            + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                            + "</element>"
                            + "<ns1:ns-element>namespace</ns1:ns-element>"
                            + "<search-element>search-for</search-element>"
                        + "</ns1:root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);
        HashMap<String, Object> extractMessageElements = new HashMap<>();
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

        XpathPayloadVariableExtractor variableExtractor = new XpathPayloadVariableExtractor.Builder()
                .expressions(extractMessageElements)
                .build();

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schemaValidation(false)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .validate(validationContext)
                .process(variableExtractor)
                .build();
        receiveAction.execute(context);

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
        Assert.assertEquals(context.getVariable("namespaceUri"), "http://citrus");
        Assert.assertNotNull(context.getVariable("contains"));
        Assert.assertEquals(context.getVariable("contains"), "true");
        Assert.assertNotNull(context.getVariable("exists"));
        Assert.assertEquals(context.getVariable("exists"), "true");
        Assert.assertNotNull(context.getVariable("existsNot"));
        Assert.assertEquals(context.getVariable("existsNot"), "false");
    }
}
