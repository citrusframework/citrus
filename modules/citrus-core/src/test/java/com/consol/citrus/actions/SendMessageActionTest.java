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

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.consol.citrus.DefaultTestCase;
import com.consol.citrus.TestActor;
import com.consol.citrus.TestCase;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.EndpointConfiguration;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.functions.DefaultFunctionLibrary;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageHeaders;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.messaging.Producer;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.json.JsonMessageValidationContext;
import com.consol.citrus.validation.json.JsonPathMessageConstructionInterceptor;
import com.consol.citrus.validation.json.JsonTextMessageValidator;
import com.consol.citrus.validation.matcher.DefaultValidationMatcherLibrary;
import com.consol.citrus.validation.script.GroovyScriptMessageBuilder;
import com.consol.citrus.validation.xml.DomXmlMessageValidator;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import com.consol.citrus.validation.xml.XpathMessageConstructionInterceptor;
import com.consol.citrus.variable.MessageHeaderVariableExtractor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class SendMessageActionTest extends AbstractTestNGUnitTest {

    private Endpoint endpoint = Mockito.mock(Endpoint.class);
    private Producer producer = Mockito.mock(Producer.class);
    private EndpointConfiguration endpointConfiguration = Mockito.mock(EndpointConfiguration.class);

    @Override
    protected TestContextFactory createTestContextFactory() {
        TestContextFactory factory = super.createTestContextFactory();
        factory.getFunctionRegistry().getFunctionLibraries().add(new DefaultFunctionLibrary());
        factory.getValidationMatcherRegistry().getValidationMatcherLibraries().add(new DefaultValidationMatcherLibrary());
        return factory;
    }

    @Test
    @SuppressWarnings("rawtypes")
	public void testSendMessageWithMessagePayloadData() {
		TestActor testActor = new TestActor();
        testActor.setName("TESTACTOR");

		PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
		messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

		final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                DomXmlMessageValidator validator = new DomXmlMessageValidator();
                XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
                validator.validateMessage(((Message)invocation.getArguments()[0]), controlMessage, context, validationContext);
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .actor(testActor)
                .messageBuilder(messageBuilder)
                .build();
		sendAction.execute(context);

	}

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithMessagePayloadResource() {
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadResourcePath("classpath:com/consol/citrus/actions/test-request-payload.xml");

        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                DomXmlMessageValidator validator = new DomXmlMessageValidator();
                XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
                validator.validateMessage(((Message)invocation.getArguments()[0]), controlMessage, context, validationContext);
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(messageBuilder)
                .build();
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
	public void testSendMessageWithMessageBuilderScriptData() {
		StringBuilder sb = new StringBuilder();
		sb.append("markupBuilder.TestRequest(){\n");
		sb.append("Message('Hello World!')\n");
		sb.append("}");

		GroovyScriptMessageBuilder scriptMessageBuidler = new GroovyScriptMessageBuilder();
		scriptMessageBuidler.setScriptData(sb.toString());

		final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                DomXmlMessageValidator validator = new DomXmlMessageValidator();
                XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
                validator.validateMessage(((Message)invocation.getArguments()[0]), controlMessage, context, validationContext);
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(scriptMessageBuidler)
                .build();
		sendAction.execute(context);

	}

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithMessageBuilderScriptDataVariableSupport() {
        context.setVariable("text", "Hello World!");

        StringBuilder sb = new StringBuilder();
        sb.append("markupBuilder.TestRequest(){\n");
        sb.append("Message('${text}')\n");
        sb.append("}");

        GroovyScriptMessageBuilder scriptMessageBuidler = new GroovyScriptMessageBuilder();
        scriptMessageBuidler.setScriptData(sb.toString());

        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                DomXmlMessageValidator validator = new DomXmlMessageValidator();
                XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
                validator.validateMessage(((Message)invocation.getArguments()[0]), controlMessage, context, validationContext);
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(scriptMessageBuidler)
                .build();
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithMessageBuilderScriptResource() {
        GroovyScriptMessageBuilder scriptMessageBuidler = new GroovyScriptMessageBuilder();
        scriptMessageBuidler.setScriptResourcePath("classpath:com/consol/citrus/actions/test-request-payload.groovy");

        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                DomXmlMessageValidator validator = new DomXmlMessageValidator();
                XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
                validator.validateMessage(((Message)invocation.getArguments()[0]), controlMessage, context, validationContext);
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(scriptMessageBuidler)
                .build();
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithMessagePayloadDataVariablesSupport() {
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>${myText}</Message></TestRequest>");

        context.setVariable("myText", "Hello World!");

        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                DomXmlMessageValidator validator = new DomXmlMessageValidator();
                XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
                validator.validateMessage(((Message)invocation.getArguments()[0]), controlMessage, context, validationContext);
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(messageBuilder)
                .build();
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithMessagePayloadResourceVariablesSupport() {
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadResourcePath("classpath:com/consol/citrus/actions/test-request-payload-with-variables.xml");

        context.setVariable("myText", "Hello World!");

        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                DomXmlMessageValidator validator = new DomXmlMessageValidator();
                XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
                validator.validateMessage(((Message)invocation.getArguments()[0]), controlMessage, context, validationContext);
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(messageBuilder)
                .build();
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithMessagePayloadResourceFunctionsSupport() {
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadResourcePath("classpath:com/consol/citrus/actions/test-request-payload-with-functions.xml");

        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                DomXmlMessageValidator validator = new DomXmlMessageValidator();
                XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
                validator.validateMessage(((Message)invocation.getArguments()[0]), controlMessage, context, validationContext);
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(messageBuilder)
                .build();
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageOverwriteMessageElementsXPath() {
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>?</Message></TestRequest>");

        Map<String, String> overwriteElements = new HashMap<String, String>();
        overwriteElements.put("/TestRequest/Message", "Hello World!");

        XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(overwriteElements);
        messageBuilder.add(interceptor);

        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                DomXmlMessageValidator validator = new DomXmlMessageValidator();
                XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
                validator.validateMessage(((Message)invocation.getArguments()[0]), controlMessage, context, validationContext);
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(messageBuilder)
                .build();
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageOverwriteMessageElementsJsonPath() {
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("{ \"TestRequest\": { \"Message\": \"?\" }}");

        Map<String, String> overwriteElements = new HashMap<String, String>();
        overwriteElements.put("$.TestRequest.Message", "Hello World!");

        JsonPathMessageConstructionInterceptor interceptor = new JsonPathMessageConstructionInterceptor(overwriteElements);
        messageBuilder.add(interceptor);

        final Message controlMessage = new DefaultMessage("{ \"TestRequest\": { \"Message\": \"Hello World!\" }}");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                JsonTextMessageValidator validator = new JsonTextMessageValidator();
                JsonMessageValidationContext validationContext = new JsonMessageValidationContext();
                validator.validateMessage(((Message)invocation.getArguments()[0]), controlMessage, context, validationContext);
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageType(MessageType.JSON)
                .messageBuilder(messageBuilder)
                .build();
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageOverwriteMessageElementsDotNotation() {
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>?</Message></TestRequest>");

        Map<String, String> overwriteElements = new HashMap<String, String>();
        overwriteElements.put("TestRequest.Message", "Hello World!");

        XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(overwriteElements);
        messageBuilder.add(interceptor);

        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                DomXmlMessageValidator validator = new DomXmlMessageValidator();
                XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
                validator.validateMessage(((Message)invocation.getArguments()[0]), controlMessage, context, validationContext);
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(messageBuilder)
                .build();
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageOverwriteMessageElementsXPathWithNamespace() {
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\">" +
                "<ns0:Message>?</ns0:Message></ns0:TestRequest>");

        Map<String, String> overwriteElements = new HashMap<String, String>();
        overwriteElements.put("/ns0:TestRequest/ns0:Message", "Hello World!");

        XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(overwriteElements);
        messageBuilder.add(interceptor);

        final Message controlMessage = new DefaultMessage("<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\"><ns0:Message>Hello World!</ns0:Message></ns0:TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                DomXmlMessageValidator validator = new DomXmlMessageValidator();
                XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
                validationContext.setSchemaValidation(false);

                validator.validateMessage(((Message)invocation.getArguments()[0]), controlMessage, context, validationContext);
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(messageBuilder)
                .build();
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageOverwriteMessageElementsXPathWithDefaultNamespace() {
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest xmlns=\"http://citrusframework.org/unittest\">" +
                "<Message>?</Message></TestRequest>");

        Map<String, String> overwriteElements = new HashMap<String, String>();
        overwriteElements.put("/:TestRequest/:Message", "Hello World!");

        XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(overwriteElements);
        messageBuilder.add(interceptor);

        final Message controlMessage = new DefaultMessage("<TestRequest xmlns=\"http://citrusframework.org/unittest\"><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                DomXmlMessageValidator validator = new DomXmlMessageValidator();
                XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
                validationContext.setSchemaValidation(false);

                validator.validateMessage(((Message)invocation.getArguments()[0]), controlMessage, context, validationContext);
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(messageBuilder)
                .build();
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithMessageHeaders() {
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        final Map<String, Object> controlHeaders = new HashMap<String, Object>();
        controlHeaders.put("Operation", "sayHello");
        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", controlHeaders);

        final Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "sayHello");
        messageBuilder.setMessageHeaders(headers);

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                DomXmlMessageValidator validator = new DomXmlMessageValidator();
                XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
                validator.validateMessage(((Message)invocation.getArguments()[0]), controlMessage, context, validationContext);
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(messageBuilder)
                .build();
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithHeaderValuesVariableSupport() {
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        context.setVariable("myOperation", "sayHello");

        final Map<String, Object> controlHeaders = new HashMap<String, Object>();
        controlHeaders.put("Operation", "sayHello");
        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", controlHeaders);

        final Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "${myOperation}");
        messageBuilder.setMessageHeaders(headers);

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                DomXmlMessageValidator validator = new DomXmlMessageValidator();
                XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
                validator.validateMessage(((Message)invocation.getArguments()[0]), controlMessage, context, validationContext);
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(messageBuilder)
                .build();
        sendAction.execute(context);

    }

    @Test
    public void testSendMessageWithUnknwonVariableInMessagePayload() {
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>${myText}</Message></TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(messageBuilder)
                .build();
        try {
            sendAction.execute(context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Unknown variable 'myText'");
            return;
        }

        Assert.fail("Missing " + CitrusRuntimeException.class + " with unknown variable error message");
    }

    @Test
    public void testSendMessageWithUnknwonVariableInHeaders() {
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        final Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "${myOperation}");
        messageBuilder.setMessageHeaders(headers);

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(messageBuilder)
                .build();
        try {
            sendAction.execute(context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Unknown variable 'myOperation'");
            return;
        }

        Assert.fail("Missing " + CitrusRuntimeException.class + " with unknown variable error message");
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithExtractHeaderValues() {
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        final Map<String, Object> controlHeaders = new HashMap<String, Object>();
        controlHeaders.put("Operation", "sayHello");
        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", controlHeaders);

        final Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "sayHello");
        messageBuilder.setMessageHeaders(headers);

        Map<String, String> extractVars = new HashMap<String, String>();
        extractVars.put("Operation", "myOperation");
        extractVars.put(MessageHeaders.ID, "correlationId");

        MessageHeaderVariableExtractor variableExtractor = new MessageHeaderVariableExtractor();
        variableExtractor.setHeaderMappings(extractVars);

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                DomXmlMessageValidator validator = new DomXmlMessageValidator();
                XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
                validator.validateMessage(((Message)invocation.getArguments()[0]), controlMessage, context, validationContext);
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .variableExtractor(variableExtractor)
                .messageBuilder(messageBuilder)
                .build();
        sendAction.execute(context);

        Assert.assertNotNull(context.getVariable("myOperation"));
        Assert.assertNotNull(context.getVariable("correlationId"));

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testMissingMessagePayload() {
        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                DomXmlMessageValidator validator = new DomXmlMessageValidator();
                XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
                validator.validateMessage(((Message)invocation.getArguments()[0]), new DefaultMessage(""), context, validationContext);
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .build();
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithXmlDeclaration() {
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>");

        final Message controlMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                DomXmlMessageValidator validator = new DomXmlMessageValidator();
                XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
                validator.validateMessage(((Message)invocation.getArguments()[0]), controlMessage, context, validationContext);
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(messageBuilder)
                .build();
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithUTF16Encoding() {
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<?xml version=\"1.0\" encoding=\"UTF-16\"?><TestRequest><Message>Hello World!</Message></TestRequest>");

        final Message controlMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-16\"?><TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                DomXmlMessageValidator validator = new DomXmlMessageValidator();
                XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
                validator.validateMessage(((Message)invocation.getArguments()[0]), controlMessage, context, validationContext);
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(messageBuilder)
                .build();
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithISOEncoding() {
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><TestRequest><Message>Hello World!</Message></TestRequest>");

        final Message controlMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                DomXmlMessageValidator validator = new DomXmlMessageValidator();
                XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
                validator.validateMessage(((Message)invocation.getArguments()[0]), controlMessage, context, validationContext);
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(messageBuilder)
                .build();
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithUnsupportedEncoding() {
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<?xml version=\"1.0\" encoding=\"MyUnsupportedEncoding\"?><TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(messageBuilder)
                .build();
        try {
            sendAction.execute(context);
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getCause() instanceof UnsupportedEncodingException);
        }

        verify(producer).send(any(Message.class), any(TestContext.class));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithMessagePayloadResourceISOEncoding() {
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadResourcePath("classpath:com/consol/citrus/actions/test-request-iso-encoding.xml");

        final Message controlMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                DomXmlMessageValidator validator = new DomXmlMessageValidator();
                XmlMessageValidationContext validationContext = new XmlMessageValidationContext();
                validator.validateMessage(((Message)invocation.getArguments()[0]), controlMessage, context, validationContext);
                return null;
            }
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(messageBuilder)
                .build();
        sendAction.execute(context);

    }

    @Test
    public void testDisabledSendMessage() {
        TestCase testCase = new DefaultTestCase();

        TestActor disabledActor = new TestActor();
        disabledActor.setDisabled(true);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .actor(disabledActor)
                .messageBuilder(messageBuilder)
                .build();
        testCase.addTestAction(sendAction);
        testCase.execute(context);

    }

    @Test
    public void testDisabledSendMessageByEndpointActor() {
        TestCase testCase = new DefaultTestCase();

        TestActor disabledActor = new TestActor();
        disabledActor.setDisabled(true);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpoint.getActor()).thenReturn(disabledActor);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(messageBuilder)
                .build();
        testCase.addTestAction(sendAction);
        testCase.execute(context);

    }

}
