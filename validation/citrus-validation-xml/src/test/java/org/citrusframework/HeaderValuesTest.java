/*
 * Copyright the original author or authors.
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

import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.builder.DefaultHeaderBuilder;
import org.citrusframework.message.builder.DefaultPayloadBuilder;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.variable.MessageHeaderVariableExtractor;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class HeaderValuesTest extends UnitTestSupport {
    private Endpoint endpoint = Mockito.mock(Endpoint.class);
    private Consumer consumer = Mockito.mock(Consumer.class);
    private EndpointConfiguration endpointConfiguration = Mockito.mock(EndpointConfiguration.class);

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testValidateHeaderValues() {
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

        HashMap<String, Object> validateHeaderValues = new HashMap<>();
        validateHeaderValues.put("header-valueA", "A");

        controlMessageBuilder.addHeaderBuilder(new DefaultHeaderBuilder(validateHeaderValues));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testValidateHeaderValuesComplete() {
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

        HashMap<String, Object> validateHeaderValues = new HashMap<>();
        validateHeaderValues.put("header-valueA", "A");
        validateHeaderValues.put("header-valueB", "B");
        validateHeaderValues.put("header-valueC", "C");

        controlMessageBuilder.addHeaderBuilder(new DefaultHeaderBuilder(validateHeaderValues));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .validate(new HeaderValidationContext.Builder().build())
                .build();
        receiveAction.execute(context);
    }

    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testValidateHeaderValuesWrongExpectedValue() {
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

        HashMap<String, Object> validateHeaderValues = new HashMap<>();
        validateHeaderValues.put("header-valueA", "wrong");

        controlMessageBuilder.addHeaderBuilder(new DefaultHeaderBuilder(validateHeaderValues));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);
    }

    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testValidateHeaderValuesForWrongElement() {
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

        HashMap<String, Object> validateHeaderValues = new HashMap<>();
        validateHeaderValues.put("header-wrong", "A");

        controlMessageBuilder.addHeaderBuilder(new DefaultHeaderBuilder(validateHeaderValues));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testValidateEmptyHeaderValues() {
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
                        .setHeader("header-valueA", "")
                        .setHeader("header-valueB", "")
                        .setHeader("header-valueC", "");

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

        HashMap<String, Object> validateHeaderValues = new HashMap<>();
        validateHeaderValues.put("header-valueA", "");
        validateHeaderValues.put("header-valueB", "");
        validateHeaderValues.put("header-valueC", "");

        controlMessageBuilder.addHeaderBuilder(new DefaultHeaderBuilder(validateHeaderValues));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);
    }

    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testValidateHeaderValuesNullComparison() {
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
                        .setHeader("header-valueA", "")
                        .setHeader("header-valueB", "")
                        .setHeader("header-valueC", "");

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

        HashMap<String, Object> validateHeaderValues = new HashMap<>();
        validateHeaderValues.put("header-valueA", "null");
        validateHeaderValues.put("header-valueB", "null");
        validateHeaderValues.put("header-valueC", "null");

        controlMessageBuilder.addHeaderBuilder(new DefaultHeaderBuilder(validateHeaderValues));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testExtractHeaderValues() {
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

        HashMap<String, String> extractHeaderValues = new HashMap<>();
        extractHeaderValues.put("header-valueA", "${valueA}");
        extractHeaderValues.put("header-valueB", "${valueB}");

        MessageHeaderVariableExtractor variableExtractor = new MessageHeaderVariableExtractor.Builder()
                .headers(extractHeaderValues)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .process(variableExtractor)
                .build();
        receiveAction.execute(context);

        Assert.assertTrue(context.getVariables().containsKey("valueA"));
        Assert.assertEquals(context.getVariables().get("valueA"), "A");
        Assert.assertTrue(context.getVariables().containsKey("valueB"));
        Assert.assertEquals(context.getVariables().get("valueB"), "B");
    }
}
