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
import org.citrusframework.message.builder.DefaultHeaderBuilder;
import org.citrusframework.message.builder.DefaultPayloadBuilder;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.xml.XpathMessageValidationContext;
import org.citrusframework.validation.xml.XpathPayloadVariableExtractor;
import org.citrusframework.variable.MessageHeaderVariableExtractor;
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
public class VariableSupportTest extends UnitTestSupport {
    private Endpoint endpoint = Mockito.mock(Endpoint.class);
    private Consumer consumer = Mockito.mock(Consumer.class);
    private EndpointConfiguration endpointConfiguration = Mockito.mock(EndpointConfiguration.class);

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testValidateMessageElementsVariablesSupport() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>"
                        + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        context.getVariables().put("variable", "text-value");

        Map<String, Object> validateMessageElements = new HashMap<>();
        validateMessageElements.put("//root/element/sub-elementA", "${variable}");
        validateMessageElements.put("//sub-elementB", "${variable}");

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        XpathMessageValidationContext validationContext = new XpathMessageValidationContext.Builder()
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
    public void testValidateMessageElementsFunctionSupport() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>"
                + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        context.getVariables().put("variable", "text-value");
        context.getVariables().put("text", "text");

        Map<String, Object> validateMessageElements = new HashMap<>();
        validateMessageElements.put("//root/element/sub-elementA", "citrus:concat('text', '-', 'value')");
        validateMessageElements.put("//sub-elementB", "citrus:concat(${text}, '-', 'value')");

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        XpathMessageValidationContext validationContext = new XpathMessageValidationContext.Builder()
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
    public void testValidateMessageElementsVariableSupportInExpression() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>"
                + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        context.getVariables().put("expression", "//root/element/sub-elementA");

        Map<String, Object> validateMessageElements = new HashMap<>();
        validateMessageElements.put("${expression}", "text-value");

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        XpathMessageValidationContext validationContext = new XpathMessageValidationContext.Builder()
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
    public void testValidateMessageElementsFunctionSupportInExpression() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>"
                + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        context.getVariables().put("variable", "B");

        Map<String, Object> validateMessageElements = new HashMap<>();
        validateMessageElements.put("citrus:concat('//root/', 'element/sub-elementA')", "text-value");
        validateMessageElements.put("citrus:concat('//sub-element', ${variable})", "text-value");

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        XpathMessageValidationContext validationContext = new XpathMessageValidationContext.Builder()
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
    public void testValidateHeaderValuesVariablesSupport() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>"
                + "</root>")
                        .setHeader("header-valueA", "A")
                        .setHeader("header-valueB", "B")
                        .setHeader("header-valueC", "C");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                    + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                    + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                    + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>"
                + "</root>"));

        context.getVariables().put("variableA", "A");
        context.getVariables().put("variableB", "B");
        context.getVariables().put("variableC", "C");

        HashMap<String, Object> validateHeaderValues = new HashMap<String, Object>();
        validateHeaderValues.put("header-valueA", "${variableA}");
        validateHeaderValues.put("header-valueB", "${variableB}");
        validateHeaderValues.put("header-valueC", "${variableC}");

        controlMessageBuilder.addHeaderBuilder(new DefaultHeaderBuilder(validateHeaderValues));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testValidateHeaderValuesFunctionSupport() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>"
                + "</root>")
                        .setHeader("header-valueA", "A")
                        .setHeader("header-valueB", "B")
                        .setHeader("header-valueC", "C");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                    + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                    + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                    + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>"
                + "</root>"));

        context.getVariables().put("variableC", "c");

        HashMap<String, Object> validateHeaderValues = new HashMap<String, Object>();
        validateHeaderValues.put("header-valueA", "citrus:upperCase('a')");
        validateHeaderValues.put("header-valueB", "citrus:upperCase('b')");
        validateHeaderValues.put("header-valueC", "citrus:upperCase(${variableC})");

        controlMessageBuilder.addHeaderBuilder(new DefaultHeaderBuilder(validateHeaderValues));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testHeaderNameVariablesSupport() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>"
                + "</root>")
                        .setHeader("header-valueA", "A")
                        .setHeader("header-valueB", "B")
                        .setHeader("header-valueC", "C");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                    + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                    + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                    + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>"
                + "</root>"));

        context.getVariables().put("variableA", "header-valueA");
        context.getVariables().put("variableB", "header-valueB");
        context.getVariables().put("variableC", "header-valueC");

        HashMap<String, Object> validateHeaderValues = new HashMap<String, Object>();
        validateHeaderValues.put("${variableA}", "A");
        validateHeaderValues.put("${variableB}", "B");
        validateHeaderValues.put("${variableC}", "C");

        controlMessageBuilder.addHeaderBuilder(new DefaultHeaderBuilder(validateHeaderValues));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testHeaderNameFunctionSupport() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>"
                + "</root>")
                        .setHeader("header-valueA", "A")
                        .setHeader("header-valueB", "B")
                        .setHeader("header-valueC", "C");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                    + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                    + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                    + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>"
                + "</root>"));

        HashMap<String, Object> validateHeaderValues = new HashMap<String, Object>();
        validateHeaderValues.put("citrus:concat('header', '-', 'valueA')", "A");
        validateHeaderValues.put("citrus:concat('header', '-', 'valueB')", "B");
        validateHeaderValues.put("citrus:concat('header', '-', 'valueC')", "C");

        controlMessageBuilder.addHeaderBuilder(new DefaultHeaderBuilder(validateHeaderValues));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testExtractMessageElementsVariablesSupport() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>"
                + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        context.getVariables().put("variableA", "initial");
        context.getVariables().put("variableB", "initial");

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                    + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                    + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                    + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>"
                + "</root>"));

        HashMap<String, Object> extractMessageElements = new HashMap<>();
        extractMessageElements.put("//root/element/sub-elementA", "${variableA}");
        extractMessageElements.put("//root/element/sub-elementB", "${variableB}");

        XpathPayloadVariableExtractor variableExtractor = new XpathPayloadVariableExtractor.Builder()
                .expressions(extractMessageElements)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .process(variableExtractor)
                .build();
        receiveAction.execute(context);

        Assert.assertTrue(context.getVariables().containsKey("variableA"));
        Assert.assertEquals(context.getVariables().get("variableA"), "text-value");
        Assert.assertTrue(context.getVariables().containsKey("variableB"));
        Assert.assertEquals(context.getVariables().get("variableB"), "text-value");
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testExtractHeaderValuesVariablesSupport() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>"
                + "</root>")
                        .setHeader("header-valueA", "A")
                        .setHeader("header-valueB", "B")
                        .setHeader("header-valueC", "C");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        context.getVariables().put("variableA", "initial");
        context.getVariables().put("variableB", "initial");

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                    + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                    + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                    + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>"
                + "</root>"));

        HashMap<String, String> extractHeaderValues = new HashMap<>();
        extractHeaderValues.put("header-valueA", "${variableA}");
        extractHeaderValues.put("header-valueB", "${variableB}");

        MessageHeaderVariableExtractor variableExtractor = new MessageHeaderVariableExtractor.Builder()
                .headers(extractHeaderValues)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .process(variableExtractor)
                .build();
        receiveAction.execute(context);

        Assert.assertTrue(context.getVariables().containsKey("variableA"));
        Assert.assertEquals(context.getVariables().get("variableA"), "A");
        Assert.assertTrue(context.getVariables().containsKey("variableB"));
        Assert.assertEquals(context.getVariables().get("variableB"), "B");
    }
}
