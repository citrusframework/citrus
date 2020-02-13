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

package com.consol.citrus.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.consol.citrus.CitrusSettings;
import com.consol.citrus.DefaultTestCase;
import com.consol.citrus.TestActor;
import com.consol.citrus.TestCase;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.EndpointConfiguration;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageQueue;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.messaging.SelectiveConsumer;
import com.consol.citrus.script.ScriptTypes;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.MessageValidator;
import com.consol.citrus.validation.MessageValidatorRegistry;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.validation.json.JsonMessageValidationContext;
import com.consol.citrus.validation.json.JsonPathMessageConstructionInterceptor;
import com.consol.citrus.validation.json.JsonPathMessageValidationContext;
import com.consol.citrus.validation.json.JsonPathVariableExtractor;
import com.consol.citrus.validation.script.GroovyScriptMessageBuilder;
import com.consol.citrus.validation.script.GroovyXmlMessageValidator;
import com.consol.citrus.validation.script.ScriptValidationContext;
import com.consol.citrus.validation.xml.DomXmlMessageValidator;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import com.consol.citrus.validation.xml.XpathMessageConstructionInterceptor;
import com.consol.citrus.validation.xml.XpathMessageValidationContext;
import com.consol.citrus.validation.xml.XpathPayloadVariableExtractor;
import com.consol.citrus.variable.MessageHeaderVariableExtractor;
import com.consol.citrus.variable.VariableExtractor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class ReceiveMessageActionTest extends AbstractTestNGUnitTest {

    private Endpoint endpoint = Mockito.mock(Endpoint.class);
    private SelectiveConsumer consumer = Mockito.mock(SelectiveConsumer.class);
    private EndpointConfiguration endpointConfiguration = Mockito.mock(EndpointConfiguration.class);

    @Autowired
    private MessageQueue mockQueue;

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithEndpointUri() {
        TestActor testActor = new TestActor();
        testActor.setName("TESTACTOR");

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");


        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        when(mockQueue.receive(15000)).thenReturn(controlMessage);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint("direct:mockQueue?timeout=15000")
                .actor(testActor)
                .messageBuilder(controlMessageBuilder)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithVariableEndpointName() {
        context.setVariable("varEndpoint", "direct:mockQueue");
        TestActor testActor = new TestActor();
        testActor.setName("TESTACTOR");

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        when(mockQueue.receive(5000)).thenReturn(controlMessage);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint("${varEndpoint}")
                .actor(testActor)
                .messageBuilder(controlMessageBuilder)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public void testReceiveMessageWithMessagePayloadData() {
		TestActor testActor = new TestActor();
        testActor.setName("TESTACTOR");


		PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .actor(testActor)
                .messageBuilder(controlMessageBuilder)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);
	}

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithMessagePayloadResource() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setPayloadResourcePath("classpath:com/consol/citrus/actions/test-request-payload.xml");

        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithMessageBuilderScriptData() {
        StringBuilder sb = new StringBuilder();
        sb.append("markupBuilder.TestRequest(){\n");
        sb.append("Message('Hello World!')\n");
        sb.append("}");

        GroovyScriptMessageBuilder controlMessageBuilder = new GroovyScriptMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setScriptData(sb.toString());

        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithMessageBuilderScriptDataVariableSupport() {
        context.setVariable("text", "Hello World!");

        StringBuilder sb = new StringBuilder();
        sb.append("markupBuilder.TestRequest(){\n");
        sb.append("Message('${text}')\n");
        sb.append("}");

        GroovyScriptMessageBuilder controlMessageBuilder = new GroovyScriptMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setScriptData(sb.toString());

        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithMessageBuilderScriptResource() {
        GroovyScriptMessageBuilder controlMessageBuilder = new GroovyScriptMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setScriptResourcePath("classpath:com/consol/citrus/actions/test-request-payload.groovy");

        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithMessagePayloadDataVariablesSupport() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setPayloadData("<TestRequest><Message>${myText}</Message></TestRequest>");

        context.setVariable("myText", "Hello World!");

        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithMessagePayloadResourceVariablesSupport() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setPayloadResourcePath("classpath:com/consol/citrus/actions/test-request-payload-with-variables.xml");

        context.setVariable("myText", "Hello World!");

        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithMessagePayloadResourceFunctionsSupport() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setPayloadResourcePath("classpath:com/consol/citrus/actions/test-request-payload-with-functions.xml");

        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageOverwriteMessageElementsXPath() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setPayloadData("<TestRequest><Message>?</Message></TestRequest>");

        Map<String, String> overwriteElements = new HashMap<String, String>();
        overwriteElements.put("/TestRequest/Message", "Hello World!");

        XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(overwriteElements);
        controlMessageBuilder.add(interceptor);

        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageOverwriteMessageElementsJsonPath() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
        controlMessageBuilder.setPayloadData("{ \"TestRequest\": { \"Message\": \"?\" }}");

        Map<String, String> overwriteElements = new HashMap<String, String>();
        overwriteElements.put("$.TestRequest.Message", "Hello World!");

        JsonPathMessageConstructionInterceptor interceptor = new JsonPathMessageConstructionInterceptor(overwriteElements);
        controlMessageBuilder.add(interceptor);

        Message controlMessage = new DefaultMessage("{ \"TestRequest\": { \"Message\": \"Hello World!\" }}");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageType(MessageType.JSON)
                .messageBuilder(controlMessageBuilder)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageOverwriteMessageElementsDotNotation() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setPayloadData("<TestRequest><Message>?</Message></TestRequest>");

        Map<String, String> overwriteElements = new HashMap<String, String>();
        overwriteElements.put("TestRequest.Message", "Hello World!");

        XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(overwriteElements);
        controlMessageBuilder.add(interceptor);

        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageOverwriteMessageElementsXPathWithNamespaces() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setPayloadData("<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\">" +
                "<ns0:Message>?</ns0:Message></ns0:TestRequest>");

        Map<String, String> overwriteElements = new HashMap<String, String>();
        overwriteElements.put("/ns0:TestRequest/ns0:Message", "Hello World!");

        XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(overwriteElements);
        controlMessageBuilder.add(interceptor);

        Message controlMessage = new DefaultMessage("<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\">" +
                "<ns0:Message>Hello World!</ns0:Message></ns0:TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);
        validationContext.setSchemaValidation(false);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageOverwriteMessageElementsXPathWithNestedNamespaces() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setPayloadData("<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\">" +
                "<ns1:Message xmlns:ns1=\"http://citrusframework.org/unittest/message\">?</ns1:Message></ns0:TestRequest>");

        Map<String, String> overwriteElements = new HashMap<String, String>();
        overwriteElements.put("/ns0:TestRequest/ns1:Message", "Hello World!");

        XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(overwriteElements);
        controlMessageBuilder.add(interceptor);

        Message controlMessage = new DefaultMessage("<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\">" +
                "<ns1:Message xmlns:ns1=\"http://citrusframework.org/unittest/message\">Hello World!</ns1:Message></ns0:TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);
        validationContext.setSchemaValidation(false);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageOverwriteMessageElementsXPathWithDefaultNamespaces() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setPayloadData("<TestRequest xmlns=\"http://citrusframework.org/unittest\">" +
                "<Message>?</Message></TestRequest>");

        Map<String, String> overwriteElements = new HashMap<String, String>();
        overwriteElements.put("/:TestRequest/:Message", "Hello World!");

        XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(overwriteElements);
        controlMessageBuilder.add(interceptor);

        Message controlMessage = new DefaultMessage("<TestRequest xmlns=\"http://citrusframework.org/unittest\"><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);
        validationContext.setSchemaValidation(false);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithMessageHeaders() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "sayHello");
        controlMessageBuilder.setMessageHeaders(headers);

        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        controlHeaders.put("Operation", "sayHello");
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", controlHeaders);

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithMessageHeadersVariablesSupport() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        context.setVariable("myOperation", "sayHello");

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "${myOperation}");
        controlMessageBuilder.setMessageHeaders(headers);

        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        controlHeaders.put("Operation", "sayHello");
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", controlHeaders);

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithUnknownVariablesInMessageHeaders() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "${myOperation}");
        controlMessageBuilder.setMessageHeaders(headers);

        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        controlHeaders.put("Operation", "sayHello");
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", controlHeaders);

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .validationContext(validationContext)
                .build();
        try {
            receiveAction.execute(context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Unknown variable 'myOperation'");
        }
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithUnknownVariableInMessagePayload() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setPayloadData("<TestRequest><Message>${myText}</Message></TestRequest>");

        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .validationContext(validationContext)
                .build();
        try {
            receiveAction.execute(context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Unknown variable 'myText'");
        }
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithExtractVariablesFromHeaders() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Operation", "myOperation");

        MessageHeaderVariableExtractor headerVariableExtractor = new MessageHeaderVariableExtractor();
        headerVariableExtractor.setHeaderMappings(headers);
        List<VariableExtractor> variableExtractors = new ArrayList<VariableExtractor>();
        variableExtractors.add(headerVariableExtractor);

        Map<String, Object> controlHeaders = new HashMap<String, Object>();
        controlHeaders.put("Operation", "sayHello");
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", controlHeaders);

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .variableExtractor(headerVariableExtractor)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

        Assert.assertNotNull(context.getVariable("myOperation"));
        Assert.assertEquals(context.getVariable("myOperation"), "sayHello");

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithValidateMessageElementsFromMessageXPath() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XpathMessageValidationContext validationContext = new XpathMessageValidationContext();

        Map<String, Object> messageElements = new HashMap<>();
        messageElements.put("/TestRequest/Message", "Hello World!");
        validationContext.setXpathExpressions(messageElements);

        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithValidateMessageElementsXPathDefaultNamespaceSupport() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XpathMessageValidationContext validationContext = new XpathMessageValidationContext();

        Map<String, Object> messageElements = new HashMap<>();
        messageElements.put("/:TestRequest/:Message", "Hello World!");
        validationContext.setXpathExpressions(messageElements);

        Message controlMessage = new DefaultMessage("<TestRequest  xmlns=\"http://citrusframework.org/unittest\">" +
                "<Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);
        validationContext.setSchemaValidation(false);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithValidateMessageElementsXPathNamespaceSupport() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XpathMessageValidationContext validationContext = new XpathMessageValidationContext();

        Map<String, Object> messageElements = new HashMap<>();
        messageElements.put("/ns0:TestRequest/ns0:Message", "Hello World!");
        validationContext.setXpathExpressions(messageElements);

        Message controlMessage = new DefaultMessage("<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\">" +
                "<ns0:Message>Hello World!</ns0:Message></ns0:TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);
        validationContext.setSchemaValidation(false);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithValidateMessageElementsXPathNestedNamespaceSupport() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XpathMessageValidationContext validationContext = new XpathMessageValidationContext();

        Map<String, Object> messageElements = new HashMap<>();
        messageElements.put("/ns0:TestRequest/ns1:Message", "Hello World!");
        validationContext.setXpathExpressions(messageElements);

        Message controlMessage = new DefaultMessage("<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\">" +
                "<ns1:Message xmlns:ns1=\"http://citrusframework.org/unittest/message\">Hello World!</ns1:Message></ns0:TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);
        validationContext.setSchemaValidation(false);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithValidateMessageElementsXPathNamespaceBindings() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XpathMessageValidationContext validationContext = new XpathMessageValidationContext();

        Map<String, Object> messageElements = new HashMap<>();
        messageElements.put("/pfx:TestRequest/pfx:Message", "Hello World!");
        validationContext.setXpathExpressions(messageElements);

        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("pfx", "http://citrusframework.org/unittest");
        validationContext.setNamespaces(namespaces);

        Message controlMessage = new DefaultMessage("<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\">" +
                "<ns0:Message>Hello World!</ns0:Message></ns0:TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);
        validationContext.setSchemaValidation(false);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithExtractVariablesFromMessageJsonPath() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
        controlMessageBuilder.setPayloadData("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}");

        Map<String, String> extractMessageElements = new HashMap<String, String>();
        extractMessageElements.put("$.text", "messageVar");
        extractMessageElements.put("$.person", "person");

        JsonPathVariableExtractor variableExtractor = new JsonPathVariableExtractor();
        variableExtractor.setJsonPathExpressions(extractMessageElements);

        Message controlMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .variableExtractor(variableExtractor)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

        Assert.assertNotNull(context.getVariable("messageVar"));
        Assert.assertEquals(context.getVariable("messageVar"), "Hello World!");

        Assert.assertNotNull(context.getVariable("person"));
        Assert.assertTrue(context.getVariable("person").contains("\"John\""));

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithExtractVariablesFromMessageXPath() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, String> extractMessageElements = new HashMap<String, String>();
        extractMessageElements.put("/TestRequest/Message", "messageVar");

        XpathPayloadVariableExtractor variableExtractor = new XpathPayloadVariableExtractor();
        variableExtractor.setXpathExpressions(extractMessageElements);

        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .variableExtractor(variableExtractor)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

        Assert.assertNotNull(context.getVariable("messageVar"));
        Assert.assertEquals(context.getVariable("messageVar"), "Hello World!");

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithExtractVariablesFromMessageXPathNodeList() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setPayloadData("<TestRequest>" +
                  "<Message>Hello</Message>" +
                  "<Message>ByeBye</Message>" +
                "</TestRequest>");

        Map<String, String> extractMessageElements = new HashMap<String, String>();
        extractMessageElements.put("node-set://TestRequest/Message", "messageVar");

        XpathPayloadVariableExtractor variableExtractor = new XpathPayloadVariableExtractor();
        variableExtractor.setXpathExpressions(extractMessageElements);

        Message controlMessage = new DefaultMessage("<TestRequest>" +
                  "<Message>Hello</Message>" +
                  "<Message>ByeBye</Message>" +
                "</TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .variableExtractor(variableExtractor)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

        Assert.assertNotNull(context.getVariable("messageVar"));
        Assert.assertEquals(context.getVariable("messageVar"), "Hello,ByeBye");

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithExtractVariablesFromMessageXPathDefaultNamespaceSupport() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setPayloadData("<TestRequest xmlns=\"http://citrusframework.org/unittest\">" +
                "<Message>Hello World!</Message></TestRequest>");

        Map<String, String> extractMessageElements = new HashMap<String, String>();
        extractMessageElements.put("/:TestRequest/:Message", "messageVar");

        XpathPayloadVariableExtractor variableExtractor = new XpathPayloadVariableExtractor();
        variableExtractor.setXpathExpressions(extractMessageElements);

        Message controlMessage = new DefaultMessage("<TestRequest  xmlns=\"http://citrusframework.org/unittest\">" +
                "<Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        validationContext.setSchemaValidation(false);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .variableExtractor(variableExtractor)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

        Assert.assertNotNull(context.getVariable("messageVar"));
        Assert.assertEquals(context.getVariable("messageVar"), "Hello World!");

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithExtractVariablesFromMessageXPathNamespaceSupport() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setPayloadData("<TestRequest xmlns=\"http://citrusframework.org/unittest\">" +
                "<Message>Hello World!</Message></TestRequest>");

        Map<String, String> extractMessageElements = new HashMap<String, String>();
        extractMessageElements.put("/ns0:TestRequest/ns0:Message", "messageVar");

        XpathPayloadVariableExtractor variableExtractor = new XpathPayloadVariableExtractor();
        variableExtractor.setXpathExpressions(extractMessageElements);

        Message controlMessage = new DefaultMessage("<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\">" +
                "<ns0:Message>Hello World!</ns0:Message></ns0:TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        validationContext.setSchemaValidation(false);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .variableExtractor(variableExtractor)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

        Assert.assertNotNull(context.getVariable("messageVar"));
        Assert.assertEquals(context.getVariable("messageVar"), "Hello World!");

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithExtractVariablesFromMessageXPathNestedNamespaceSupport() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setPayloadData("<TestRequest xmlns=\"http://citrusframework.org/unittest\" xmlns:ns1=\"http://citrusframework.org/unittest/message\">" +
                "<ns1:Message>Hello World!</ns1:Message></TestRequest>");

        Map<String, String> extractMessageElements = new HashMap<String, String>();
        extractMessageElements.put("/ns0:TestRequest/ns1:Message", "messageVar");

        XpathPayloadVariableExtractor variableExtractor = new XpathPayloadVariableExtractor();
        variableExtractor.setXpathExpressions(extractMessageElements);

        Message controlMessage = new DefaultMessage("<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\">" +
                "<ns1:Message xmlns:ns1=\"http://citrusframework.org/unittest/message\">Hello World!</ns1:Message></ns0:TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        validationContext.setSchemaValidation(false);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .variableExtractor(variableExtractor)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

        Assert.assertNotNull(context.getVariable("messageVar"));
        Assert.assertEquals(context.getVariable("messageVar"), "Hello World!");

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithExtractVariablesFromMessageXPathNamespaceBindings() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setPayloadData("<TestRequest xmlns=\"http://citrusframework.org/unittest\">" +
                "<Message>Hello World!</Message></TestRequest>");

        Map<String, String> extractMessageElements = new HashMap<String, String>();
        extractMessageElements.put("/pfx:TestRequest/pfx:Message", "messageVar");

        XpathPayloadVariableExtractor variableExtractor = new XpathPayloadVariableExtractor();
        variableExtractor.setXpathExpressions(extractMessageElements);

        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("pfx", "http://citrusframework.org/unittest");
        variableExtractor.setNamespaces(namespaces);

        Message controlMessage = new DefaultMessage("<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\">" +
                "<ns0:Message>Hello World!</ns0:Message></ns0:TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        validationContext.setSchemaValidation(false);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .variableExtractor(variableExtractor)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

        Assert.assertNotNull(context.getVariable("messageVar"));
        Assert.assertEquals(context.getVariable("messageVar"), "Hello World!");

    }

    @Test
    public void testReceiveMessageWithJsonPathValidation() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        JsonPathMessageValidationContext validationContext = new JsonPathMessageValidationContext();

        Map<String, Object> jsonPathExpressions = new HashMap<>();
        jsonPathExpressions.put("$..text", "Hello World!");
        jsonPathExpressions.put("$.person.name", "John");
        jsonPathExpressions.put("$.person.surname", "Doe");
        jsonPathExpressions.put("$.index", "5");
        validationContext.setJsonPathExpressions(jsonPathExpressions);

        Message controlMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .messageType(MessageType.JSON)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

    }

    @Test(expectedExceptions = ValidationException.class)
    public void testReceiveMessageWithJsonPathValidationFailure() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        JsonPathMessageValidationContext validationContext = new JsonPathMessageValidationContext();

        Map<String, Object> jsonPathExpressions = new HashMap<>();
        jsonPathExpressions.put("$..text", "Hello Citrus!");
        validationContext.setJsonPathExpressions(jsonPathExpressions);

        Message controlMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .messageType(MessageType.JSON)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testReceiveMessageWithJsonPathValidationNoPathResult() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        JsonPathMessageValidationContext validationContext = new JsonPathMessageValidationContext();

        Map<String, Object> jsonPathExpressions = new HashMap<>();
        jsonPathExpressions.put("$.person.age", "50");
        validationContext.setJsonPathExpressions(jsonPathExpressions);

        Message controlMessage = new DefaultMessage("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .messageType(MessageType.JSON)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithTimeout() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(context, 3000L)).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .timeout(3000L)
                .messageBuilder(controlMessageBuilder)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveSelectedWithMessageSelector() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        String messageSelector = "Operation = 'sayHello'";

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "sayHello");
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", headers);

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(messageSelector, context, 5000L)).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .selector(messageSelector)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveSelectedWithMessageSelectorAndTimeout() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        String messageSelector = "Operation = 'sayHello'";

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "sayHello");
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", headers);

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(messageSelector, context, 5000L)).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .timeout(5000L)
                .selector(messageSelector)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveSelectedWithMessageSelectorMap() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, String> messageSelector = new HashMap<>();
        messageSelector.put("Operation", "sayHello");

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "sayHello");
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", headers);

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive("Operation = 'sayHello'", context, 5000L)).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .selector(messageSelector)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveSelectedWithMessageSelectorMapAndTimeout() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Map<String, String> messageSelector = new HashMap<>();
        messageSelector.put("Operation", "sayHello");

        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "sayHello");
        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", headers);

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive("Operation = 'sayHello'", context, 5000L)).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .timeout(5000L)
                .messageBuilder(controlMessageBuilder)
                .selector(messageSelector)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

    }

    @Test
    public void testMessageTimeout() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(null);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .validationContext(validationContext)
                .build();
        try {
            receiveAction.execute(context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Failed to receive message - message is not available");
            return;
        }

        Assert.fail("Missing " + CitrusRuntimeException.class + " for receiving no message");
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveEmptyMessagePayloadAsExpected() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setPayloadData("");

        Message controlMessage = new DefaultMessage("");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveEmptyMessagePayloadUnexpected() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Message controlMessage = new DefaultMessage("");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .validationContext(validationContext)
                .build();
        try {
            receiveAction.execute(context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Validation failed: Unable to validate message payload - received message payload was empty, control message payload is not");
            return;
        }

        Assert.fail("Missing " + CitrusRuntimeException.class + " for receiving unexpected empty message payload");
    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithValidationScript() {
        ScriptValidationContext validationContext = new ScriptValidationContext(ScriptTypes.GROOVY);
        validationContext.setValidationScript("assert root.Message.name() == 'Message'\n" +
                "assert root.Message.text() == 'Hello World!'");

        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .validator(new GroovyXmlMessageValidator())
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testReceiveMessageWithValidationScriptResource() {
        ScriptValidationContext validationContext = new ScriptValidationContext(ScriptTypes.GROOVY);
        validationContext.setValidationScriptResourcePath("classpath:com/consol/citrus/actions/test-validation-script.groovy");

        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .validator(new GroovyXmlMessageValidator())
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

    }

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testInjectedMessageValidators() {
        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(consumer.receive(any(TestContext.class), anyLong())).thenReturn(controlMessage);
        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .validationContext(validationContext)
                .build();
        receiveAction.execute(context);

        // now inject multiple validators
        List<MessageValidator<? extends ValidationContext>> validators = new ArrayList<MessageValidator<? extends ValidationContext>>();
        validators.add(new DomXmlMessageValidator());
        validators.add(new GroovyXmlMessageValidator());

        MessageValidatorRegistry messageValidatorRegistry = new MessageValidatorRegistry();
        messageValidatorRegistry.setMessageValidators(validators);
        TestContext newContext = createTestContext();
        newContext.setMessageValidatorRegistry(messageValidatorRegistry);

        ScriptValidationContext scriptValidationContext = new ScriptValidationContext(CitrusSettings.DEFAULT_MESSAGE_TYPE);

        receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .validationContext(validationContext)
                .validationContext(scriptValidationContext)
                .build();
        receiveAction.execute(newContext);

    }

    @Test
    public void testDisabledReceiveMessage() {
        TestCase testCase = new DefaultTestCase();

        TestActor disabledActor = new TestActor();
        disabledActor.setDisabled(true);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(endpoint.getActor()).thenReturn(null);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .actor(disabledActor)
                .messageBuilder(controlMessageBuilder)
                .validationContext(validationContext)
                .build();
        testCase.addTestAction(receiveAction);
        testCase.execute(context);

    }

    @Test
    public void testDisabledReceiveMessageByEndpointActor() {
        TestCase testCase = new DefaultTestCase();

        TestActor disabledActor = new TestActor();
        disabledActor.setDisabled(true);

        PayloadTemplateMessageBuilder controlMessageBuilder = new PayloadTemplateMessageBuilder();
        XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
        controlMessageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, consumer, endpointConfiguration);
        when(endpoint.createConsumer()).thenReturn(consumer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpointConfiguration.getTimeout()).thenReturn(5000L);

        when(endpoint.getActor()).thenReturn(disabledActor);

        ReceiveMessageAction receiveAction = new ReceiveMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(controlMessageBuilder)
                .validationContext(validationContext)
                .build();
        testCase.addTestAction(receiveAction);
        testCase.execute(context);
    }
}
