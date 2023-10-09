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
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.message.builder.DefaultPayloadBuilder;
import org.citrusframework.messaging.Consumer;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.xml.XmlMessageValidationContext;
import org.citrusframework.validation.xml.XpathMessageValidationContext;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class NamespaceTest extends UnitTestSupport {
    private final Endpoint endpoint = Mockito.mock(Endpoint.class);
    private final Consumer consumer = Mockito.mock(Consumer.class);
    private final EndpointConfiguration endpointConfiguration = Mockito.mock(EndpointConfiguration.class);

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testNamespaces() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<ns1:root xmlns:ns1='http://citrus'>"
                            + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                            + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                            + "</ns1:element>"
                        + "</ns1:root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<ns1:root xmlns:ns1='http://citrus'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                        + "</ns1:element>"
                    + "</ns1:root>"));

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schemaValidation(false)
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
    public void testDifferentNamespacePrefix() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<ns1:root xmlns:ns1='http://citrus'>"
                            + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                            + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                            + "</ns1:element>"
                        + "</ns1:root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<ns2:root xmlns:ns2='http://citrus'>"
                        + "<ns2:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns2:sub-element attribute='A'>text-value</ns2:sub-element>"
                        + "</ns2:element>"
                    + "</ns2:root>"));

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schemaValidation(false)
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
    public void testAdditionalNamespace() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<ns1:root xmlns:ns1='http://citrus'>"
                            + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                            + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                            + "</ns1:element>"
                        + "</ns1:root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<ns1:root xmlns:ns1='http://citrus' xmlns:ns2='http://citrus/default'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                        + "</ns1:element>"
                    + "</ns1:root>"));

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schemaValidation(false)
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
    public void testMissingNamespaceDeclaration() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<ns1:root xmlns:ns1='http://citrus' xmlns:ns2='http://citrus/default'>"
                            + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                            + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                            + "</ns1:element>"
                        + "</ns1:root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<ns1:root xmlns:ns1='http://citrus'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                        + "</ns1:element>"
                    + "</ns1:root>"));

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schemaValidation(false)
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
    public void testDefaultNamespaces() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root xmlns='http://citrus'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root xmlns='http://citrus'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>"));

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schemaValidation(false)
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
    public void testDefaultNamespacesInExpectedMessage() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<ns1:root xmlns:ns1='http://citrus'>"
                            + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                            + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                            + "</ns1:element>"
                        + "</ns1:root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root xmlns='http://citrus'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>"));

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schemaValidation(false)
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
    public void testDefaultNamespacesInSourceMessage() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root xmlns='http://citrus'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<ns1:root xmlns:ns1='http://citrus'>"
                    + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                    + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                    + "</ns1:element>"
                + "</ns1:root>"));

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schemaValidation(false)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .validate(validationContext)
                .build();
        receiveAction.execute(context);
    }

    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testMissingNamespace() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<ns1:root xmlns:ns1='http://citrus'>"
                            + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                            + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                            + "</ns1:element>"
                        + "</ns1:root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root>"
                            + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                            + "<sub-element attribute='A'>text-value</sub-element>"
                            + "</element>"
                        + "</root>"));

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schemaValidation(false)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .validate(validationContext)
                .build();
        receiveAction.execute(context);
    }

    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testWrongNamespace() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<ns1:root xmlns:ns1='http://citrus'>"
                            + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                            + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                            + "</ns1:element>"
                        + "</ns1:root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<ns1:root xmlns:ns1='http://citrus/wrong'>"
                            + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                            + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                            + "</ns1:element>"
                        + "</ns1:root>"));

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schemaValidation(false)
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
    public void testExpectDefaultNamespace() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root xmlns='http://citrus'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root xmlns='http://citrus'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>"));

        Map<String, String> expectedNamespaces = new HashMap<>();
        expectedNamespaces.put("", "http://citrus");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schemaValidation(false)
                .namespaces(expectedNamespaces)
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
    public void testExpectNamespace() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<ns1:root xmlns:ns1='http://citrus/ns1'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                        + "</ns1:element>"
                    + "</ns1:root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<ns1:root xmlns:ns1='http://citrus/ns1'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                        + "</ns1:element>"
                    + "</ns1:root>"));

        Map<String, String> expectedNamespaces = new HashMap<>();
        expectedNamespaces.put("ns1", "http://citrus/ns1");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schemaValidation(false)
                .namespaces(expectedNamespaces)
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
    public void testExpectMixedNamespaces() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>"));

        Map<String, String> expectedNamespaces = new HashMap<>();
        expectedNamespaces.put("", "http://citrus/default");
        expectedNamespaces.put("ns1", "http://citrus/ns1");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schemaValidation(false)
                .namespaces(expectedNamespaces)
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
    public void testExpectMultipleNamespaces() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>"));

        Map<String, String> expectedNamespaces = new HashMap<>();
        expectedNamespaces.put("", "http://citrus/default");
        expectedNamespaces.put("ns1", "http://citrus/ns1");
        expectedNamespaces.put("ns2", "http://citrus/ns2");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schemaValidation(false)
                .namespaces(expectedNamespaces)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .validate(validationContext)
                .build();
        receiveAction.execute(context);
    }

    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testExpectDefaultNamespaceError() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root xmlns='http://citrus'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root xmlns='http://citrus'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>"));

        Map<String, String> expectedNamespaces = new HashMap<>();
        expectedNamespaces.put("", "http://citrus/wrong");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schemaValidation(false)
                .namespaces(expectedNamespaces)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .validate(validationContext)
                .build();
        receiveAction.execute(context);
    }

    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testExpectNamespaceError() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<ns1:root xmlns:ns1='http://citrus/ns1'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                        + "</ns1:element>"
                    + "</ns1:root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<ns1:root xmlns:ns1='http://citrus/ns1'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns1:sub-element attribute='A'>text-value</ns1:sub-element>"
                        + "</ns1:element>"
                    + "</ns1:root>"));

        Map<String, String> expectedNamespaces = new HashMap<>();
        expectedNamespaces.put("ns1", "http://citrus/ns1/wrong");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schemaValidation(false)
                .namespaces(expectedNamespaces)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .validate(validationContext)
                .build();
        receiveAction.execute(context);
    }

    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testExpectMixedNamespacesError() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>"));

        Map<String, String> expectedNamespaces = new HashMap<>();
        expectedNamespaces.put("", "http://citrus/default/wrong");
        expectedNamespaces.put("ns1", "http://citrus/ns1");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schemaValidation(false)
                .namespaces(expectedNamespaces)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .validate(validationContext)
                .build();
        receiveAction.execute(context);
    }

    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testExpectMultipleNamespacesError() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>"));

        Map<String, String> expectedNamespaces = new HashMap<>();
        expectedNamespaces.put("", "http://citrus/default");
        expectedNamespaces.put("ns1", "http://citrus/ns1/wrong");
        expectedNamespaces.put("ns2", "http://citrus/ns2");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schemaValidation(false)
                .namespaces(expectedNamespaces)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .validate(validationContext)
                .build();
        receiveAction.execute(context);
    }

    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testExpectWrongNamespacePrefix() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>"));

        Map<String, String> expectedNamespaces = new HashMap<>();
        expectedNamespaces.put("", "http://citrus/default");
        expectedNamespaces.put("nswrong", "http://citrus/ns1");
        expectedNamespaces.put("ns2", "http://citrus/ns2");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schemaValidation(false)
                .namespaces(expectedNamespaces)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .validate(validationContext)
                .build();
        receiveAction.execute(context);
    }

    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testExpectDefaultNamespaceButNamespace() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<ns0:root xmlns:ns0='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2'>"
                        + "<ns0:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns0:sub-element attribute='A'>text-value</ns0:sub-element>"
                        + "</ns0:element>"
                    + "</ns0:root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<ns0:root xmlns:ns0='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2'>"
                        + "<ns0:element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<ns0:sub-element attribute='A'>text-value</ns0:sub-element>"
                        + "</ns0:element>"
                    + "</ns0:root>"));

        Map<String, String> expectedNamespaces = new HashMap<>();
        expectedNamespaces.put("", "http://citrus/default");
        expectedNamespaces.put("ns1", "http://citrus/ns1");
        expectedNamespaces.put("ns2", "http://citrus/ns2");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schemaValidation(false)
                .namespaces(expectedNamespaces)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .validate(validationContext)
                .build();
        receiveAction.execute(context);
    }

    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testExpectNamespaceButDefaultNamespace() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>"));

        Map<String, String> expectedNamespaces = new HashMap<>();
        expectedNamespaces.put("ns0", "http://citrus/default");
        expectedNamespaces.put("ns1", "http://citrus/ns1");
        expectedNamespaces.put("ns2", "http://citrus/ns2");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schemaValidation(false)
                .namespaces(expectedNamespaces)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .validate(validationContext)
                .build();
        receiveAction.execute(context);
    }

    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testExpectAdditionalNamespace() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>"));

        Map<String, String> expectedNamespaces = new HashMap<>();
        expectedNamespaces.put("", "http://citrus/default");
        expectedNamespaces.put("ns1", "http://citrus/ns1");
        expectedNamespaces.put("ns2", "http://citrus/ns2");
        expectedNamespaces.put("ns4", "http://citrus/ns4");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schemaValidation(false)
                .namespaces(expectedNamespaces)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .validate(validationContext)
                .build();
        receiveAction.execute(context);
    }

    @Test(expectedExceptions = {ValidationException.class})
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testExpectNamespaceButNamespaceMissing() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2' xmlns:ns4='http://citrus/ns4'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root xmlns='http://citrus/default' xmlns:ns1='http://citrus/ns1' xmlns:ns2='http://citrus/ns2' xmlns:ns4='http://citrus/ns4'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value'>"
                        + "<sub-element attribute='A'>text-value</sub-element>"
                        + "</element>"
                    + "</root>"));

        Map<String, String> expectedNamespaces = new HashMap<>();
        expectedNamespaces.put("", "http://citrus/default");
        expectedNamespaces.put("ns1", "http://citrus/ns1");
        expectedNamespaces.put("ns2", "http://citrus/ns2");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .schemaValidation(false)
                .namespaces(expectedNamespaces)
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
    public void testValidateMessageElementsWithAdditionalNamespacePrefix() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root xmlns='http://citrus/default'>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                            + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                            + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                        + "</element>"
                        + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        HashMap<String, Object> validateMessageElements = new HashMap<>();
        validateMessageElements.put("//ns1:root/ns1:element/ns1:sub-elementA", "text-value");
        validateMessageElements.put("//ns1:sub-elementB", "text-value");

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();

        Map<String, String> namespaces = new HashMap<>();
        namespaces.put("ns1", "http://citrus/default");

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
    public void testValidateMessageElementsWithDifferentNamespacePrefix() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<ns1:root xmlns:ns1='http://citrus/default'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<ns1:sub-elementA attribute='A'>text-value</ns1:sub-elementA>"
                            + "<ns1:sub-elementB attribute='B'>text-value</ns1:sub-elementB>"
                            + "<ns1:sub-elementC attribute='C'>text-value</ns1:sub-elementC>"
                        + "</ns1:element>"
                        + "</ns1:root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        HashMap<String, Object> validateMessageElements = new HashMap<>();
        validateMessageElements.put("//pfx:root/pfx:element/pfx:sub-elementA", "text-value");
        validateMessageElements.put("//pfx:sub-elementB", "text-value");

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();

        Map<String, String> namespaces = new HashMap<>();
        namespaces.put("pfx", "http://citrus/default");

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

    @Test(expectedExceptions = {CitrusRuntimeException.class})
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testWrongNamespaceContext() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<ns1:root xmlns:ns1='http://citrus/default'>"
                        + "<ns1:element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<ns1:sub-elementA attribute='A'>text-value</ns1:sub-elementA>"
                            + "<ns1:sub-elementB attribute='B'>text-value</ns1:sub-elementB>"
                            + "<ns1:sub-elementC attribute='C'>text-value</ns1:sub-elementC>"
                        + "</ns1:element>"
                        + "</ns1:root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        HashMap<String, Object> validateMessageElements = new HashMap<>();
        validateMessageElements.put("//pfx:root/ns1:element/pfx:sub-elementA", "text-value");
        validateMessageElements.put("//pfx:sub-elementB", "text-value");

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();

        Map<String, String> namespaces = new HashMap<>();
        namespaces.put("pfx", "http://citrus/wrong");

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
}
