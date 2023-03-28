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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.exceptions.CitrusRuntimeException;
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
public class IgnoreElementsTest extends UnitTestSupport {
    private Endpoint endpoint = Mockito.mock(Endpoint.class);
    private Consumer consumer = Mockito.mock(Consumer.class);
    private EndpointConfiguration endpointConfiguration = Mockito.mock(EndpointConfiguration.class);

    @Override
    protected TestContext createTestContext() {
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

        return super.createTestContext();
    }

    @Test
    public void testIgnoreElements() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                    + "<sub-elementA attribute='A'>no validation</sub-elementA>"
                    + "<sub-elementB attribute='B'>no validation</sub-elementB>"
                    + "<sub-elementC attribute='C'>text-value</sub-elementC>"
            + "</element>"
            + "</root>"));

        Set<String> ignoreMessageElements = new HashSet<String>();
        ignoreMessageElements.add("//root/element/sub-elementA");
        ignoreMessageElements.add("//sub-elementB");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .ignore(ignoreMessageElements)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .validate(validationContext)
                .build();
        receiveAction.execute(context);
    }

    @Test
    public void testIgnoreNodeListElements() {
        reset(consumer);
        Message message = new DefaultMessage("<root>"
                + "<element>"
                    + "<sub-element attribute='A'>text-value</sub-element>"
                    + "<sub-element attribute='B'>text-value</sub-element>"
                    + "<sub-element attribute='C'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root>"
                + "<element>"
                    + "<sub-element attribute='A'>no validation</sub-element>"
                    + "<sub-element attribute='B'>no validation</sub-element>"
                    + "<sub-element attribute='C'>no validation</sub-element>"
                + "</element>"
                + "</root>"));

        Set<String> ignoreMessageElements = new HashSet<String>();
        ignoreMessageElements.add("//sub-element");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .ignore(ignoreMessageElements)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .validate(validationContext)
                .build();
        receiveAction.execute(context);
    }

    @Test
    public void testIgnoreMultipleElements() {
        reset(consumer);
        Message message = new DefaultMessage("<root>"
                + "<element>"
                    + "<sub-element attribute='A'>text-value</sub-element>"
                    + "<sub-element attribute='B'>text-value</sub-element>"
                    + "<sub-element attribute='C'>text-value</sub-element>"
                + "</element>"
                + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root>"
                + "<element>"
                    + "<sub-element attribute='wrong'>no validation</sub-element>"
                    + "<sub-element attribute='B'>text-value</sub-element>"
                    + "<sub-element attribute='wrong'>no validation</sub-element>"
                + "</element>"
                + "</root>"));

        Set<String> ignoreMessageElements = new HashSet<String>();
        ignoreMessageElements.add("//sub-element[1]");
        ignoreMessageElements.add("//sub-element[3]");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .ignore(ignoreMessageElements)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .validate(validationContext)
                .build();
        receiveAction.execute(context);
    }

    @Test
    public void testIgnoreAllElements() {
        reset(consumer);
        Message message = new DefaultMessage("<root>"
                + "<element>"
                    + "<another-element attribute='Z'>text-value</another-element>"
                    + "<sub-element attribute='A'>text-value</sub-element>"
                    + "<sub-element attribute='B'>text-value</sub-element>"
                    + "<sub-element attribute='C'>text-value</sub-element>"
                    + "<another-element attribute='Z'>text-value</another-element>"
                + "</element>"
                + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root>"
                + "<element>"
                    + "<another-element>no validation</another-element>"
                    + "<sub-element attribute='wrong'>no validation</sub-element>"
                    + "<sub-element attribute='wrong'>no validation</sub-element>"
                    + "<sub-element attribute='wrong'>no validation</sub-element>"
                + "</element>"
                + "</root>"));

        Set<String> ignoreMessageElements = new HashSet<String>();
        ignoreMessageElements.add("/*");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .ignore(ignoreMessageElements)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .validate(validationContext)
                .build();
        receiveAction.execute(context);
    }

    @Test
    public void testIgnoreAttributes() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                    + "<sub-elementA attribute='no validation'>text-value</sub-elementA>"
                    + "<sub-elementB attribute='no validation'>text-value</sub-elementB>"
                    + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>"
                + "</root>"));

        Set<String> ignoreMessageElements = new HashSet<String>();
        ignoreMessageElements.add("//root/element/sub-elementA/@attribute");
        ignoreMessageElements.add("//sub-elementB/@attribute");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .ignore(ignoreMessageElements)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .validate(validationContext)
                .build();
        receiveAction.execute(context);
    }

    @Test
    public void testIgnoreAttributesAll() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                    + "<sub-elementA attribute='no validation'>text-value</sub-elementA>"
                    + "<sub-elementB attribute='B'>text-value</sub-elementB>" //TODO fix this
                    + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>"
                + "</root>"));

        Set<String> ignoreMessageElements = new HashSet<String>();
        ignoreMessageElements.add("//@attribute");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .ignore(ignoreMessageElements)
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
    public void testIgnoreAttributesUsingArrays() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root>"
                        + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                            + "<sub-element attribute='A'>text-value</sub-element>"
                            + "<sub-element attribute='B'>text-value</sub-element>"
                            + "<sub-element attribute='C'>text-value</sub-element>"
                        + "</element>"
                        + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                    + "<sub-element attribute='no validation'>text-value</sub-element>"
                    + "<sub-element attribute='no validation'>text-value</sub-element>"
                    + "<sub-element attribute='C'>text-value</sub-element>"
                + "</element>"
                + "</root>"));

        Set<String> ignoreMessageElements = new HashSet<String>();
        ignoreMessageElements.add("//sub-element[1]/@attribute");
        ignoreMessageElements.add("//sub-element[2]/@attribute");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .ignore(ignoreMessageElements)
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
    public void testIgnoreRootElement() {
        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        Message message = new DefaultMessage("<root>"
                + "<element>Text</element>"
                + "</root>");

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(message);
        when(endpoint.getActor()).thenReturn(null);

        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root>"
                        + "<element additonal-attribute='some'>Wrong text</element>"
                        + "</root>"));

        Set<String> ignoreMessageElements = new HashSet<String>();
        ignoreMessageElements.add("//root");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .ignore(ignoreMessageElements)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .validate(validationContext)
                .build();
        receiveAction.execute(context);
    }

    @Test
    public void testIgnoreElementsAndValidate() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                    + "<sub-elementA attribute='A'>no validation</sub-elementA>"
                    + "<sub-elementB attribute='B'>no validation</sub-elementB>"
                    + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>"
                + "</root>"));

        Set<String> ignoreMessageElements = new HashSet<String>();
        ignoreMessageElements.add("//root/element/sub-elementA");
        ignoreMessageElements.add("//sub-elementB");

        Map<String, Object> validateElements = new HashMap<>();
        validateElements.put("//root/element/sub-elementA", "wrong value");
        validateElements.put("//sub-elementB", "wrong value");

        XpathMessageValidationContext validationContext = new XpathMessageValidationContext.Builder()
                .ignore(ignoreMessageElements)
                .expressions(validateElements)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .validate(validationContext)
                .build();
        receiveAction.execute(context);
    }

    @Test
    public void testIgnoreElementsByPlaceholder() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                    + "<sub-elementA attribute='A'>@ignore@</sub-elementA>"
                    + "<sub-elementB attribute='B'> @ignore@ </sub-elementB>"
                    + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>"
                + "</root>"));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);
    }

    @Test
    public void testIgnoreSubElementsByPlaceholder() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >@ignore@</element>"
            + "</root>"));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);
    }

    @Test
    public void testIgnoreAttributesByPlaceholder() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                    + "<sub-elementA attribute='@ignore@'>text-value</sub-elementA>"
                    + "<sub-elementB attribute=' @ignore@ '>text-value</sub-elementB>"
                    + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>"
                + "</root>"));

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .build();
        receiveAction.execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class, expectedExceptionsMessageRegExp = "No result for XPath expression: '//something-else'")
    public void testIgnoreElementsNoMatch() {
        DefaultMessageBuilder controlMessageBuilder = new DefaultMessageBuilder();
        controlMessageBuilder.setPayloadBuilder(new DefaultPayloadBuilder("<root>"
                + "<element attributeA='attribute-value' attributeB='attribute-value' >"
                + "<sub-elementA attribute='A'>text-value</sub-elementA>"
                + "<sub-elementB attribute='B'>text-value</sub-elementB>"
                + "<sub-elementC attribute='C'>text-value</sub-elementC>"
                + "</element>"
                + "</root>"));

        Set<String> ignoreMessageElements = new HashSet<String>();
        ignoreMessageElements.add("//something-else");

        XmlMessageValidationContext validationContext = new XmlMessageValidationContext.Builder()
                .ignore(ignoreMessageElements)
                .build();

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .message(controlMessageBuilder)
                .validate(validationContext)
                .build();
        receiveAction.execute(context);
    }
}
