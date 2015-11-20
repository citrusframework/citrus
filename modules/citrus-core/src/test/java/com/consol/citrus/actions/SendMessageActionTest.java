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

import com.consol.citrus.TestActor;
import com.consol.citrus.TestCase;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.EndpointConfiguration;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.*;
import com.consol.citrus.messaging.Producer;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.json.*;
import com.consol.citrus.validation.script.GroovyScriptMessageBuilder;
import com.consol.citrus.validation.xml.*;
import com.consol.citrus.variable.MessageHeaderVariableExtractor;
import com.consol.citrus.variable.VariableExtractor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;
import java.util.*;

import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 */
public class SendMessageActionTest extends AbstractTestNGUnitTest {

    private Endpoint endpoint = Mockito.mock(Endpoint.class);
    private Producer producer = Mockito.mock(Producer.class);
    private EndpointConfiguration endpointConfiguration = Mockito.mock(EndpointConfiguration.class);
    
    @Test
    @SuppressWarnings("rawtypes")
	public void testSendMessageWithMessagePayloadData() {
		SendMessageAction sendAction = new SendMessageAction();
		sendAction.setEndpoint(endpoint);
		
		TestActor testActor = new TestActor();
        testActor.setName("TESTACTOR");
        
        sendAction.setActor(testActor);
        
		PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
		messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
		
		sendAction.setMessageBuilder(messageBuilder);
		
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

		
		sendAction.execute(context);

	}
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithMessagePayloadResource() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setEndpoint(endpoint);
        
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadResourcePath("classpath:com/consol/citrus/actions/test-request-payload.xml");
        
        sendAction.setMessageBuilder(messageBuilder);
        
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

        
        sendAction.execute(context);

    }
    
    @Test
    @SuppressWarnings("rawtypes")
	public void testSendMessageWithMessageBuilderScriptData() {
		SendMessageAction sendAction = new SendMessageAction();
		sendAction.setEndpoint(endpoint);
		StringBuilder sb = new StringBuilder();
		sb.append("markupBuilder.TestRequest(){\n");
		sb.append("Message('Hello World!')\n");
		sb.append("}");
		
		GroovyScriptMessageBuilder scriptMessageBuidler = new GroovyScriptMessageBuilder();
		scriptMessageBuidler.setScriptData(sb.toString());
		
		sendAction.setMessageBuilder(scriptMessageBuidler);
		
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

		
		sendAction.execute(context);

	}
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithMessageBuilderScriptDataVariableSupport() {
        context.setVariable("text", "Hello World!");
        
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setEndpoint(endpoint);
        StringBuilder sb = new StringBuilder();
        sb.append("markupBuilder.TestRequest(){\n");
        sb.append("Message('${text}')\n");
        sb.append("}");
        
        GroovyScriptMessageBuilder scriptMessageBuidler = new GroovyScriptMessageBuilder();
        scriptMessageBuidler.setScriptData(sb.toString());
        
        sendAction.setMessageBuilder(scriptMessageBuidler);
        
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

        
        sendAction.execute(context);

    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithMessageBuilderScriptResource() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setEndpoint(endpoint);
        
        GroovyScriptMessageBuilder scriptMessageBuidler = new GroovyScriptMessageBuilder();
        scriptMessageBuidler.setScriptResourcePath("classpath:com/consol/citrus/actions/test-request-payload.groovy");
        
        sendAction.setMessageBuilder(scriptMessageBuidler);
        
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

        
        sendAction.execute(context);

    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithMessagePayloadDataVariablesSupport() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setEndpoint(endpoint);
        
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>${myText}</Message></TestRequest>");
        
        sendAction.setMessageBuilder(messageBuilder);
        
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

        
        sendAction.execute(context);

    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithMessagePayloadResourceVariablesSupport() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadResourcePath("classpath:com/consol/citrus/actions/test-request-payload-with-variables.xml");
        
        sendAction.setMessageBuilder(messageBuilder);
        
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

        
        sendAction.execute(context);

    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithMessagePayloadResourceFunctionsSupport() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadResourcePath("classpath:com/consol/citrus/actions/test-request-payload-with-functions.xml");
        
        sendAction.setMessageBuilder(messageBuilder);
        
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

        
        sendAction.execute(context);

    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageOverwriteMessageElementsXPath() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>?</Message></TestRequest>");
        
        Map<String, String> overwriteElements = new HashMap<String, String>();
        overwriteElements.put("/TestRequest/Message", "Hello World!");
        
        XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(overwriteElements);
        messageBuilder.add(interceptor);
        
        sendAction.setMessageBuilder(messageBuilder);
        
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

        
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageOverwriteMessageElementsJsonPath() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setMessageType(MessageType.JSON.toString());
        sendAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("{ \"TestRequest\": { \"Message\": \"?\" }}");

        Map<String, String> overwriteElements = new HashMap<String, String>();
        overwriteElements.put("$.TestRequest.Message", "Hello World!");

        JsonPathMessageConstructionInterceptor interceptor = new JsonPathMessageConstructionInterceptor(overwriteElements);
        messageBuilder.add(interceptor);

        sendAction.setMessageBuilder(messageBuilder);

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

        sendAction.execute(context);

    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageOverwriteMessageElementsDotNotation() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>?</Message></TestRequest>");
        
        Map<String, String> overwriteElements = new HashMap<String, String>();
        overwriteElements.put("TestRequest.Message", "Hello World!");
        
        XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(overwriteElements);
        messageBuilder.add(interceptor);
        
        sendAction.setMessageBuilder(messageBuilder);
        
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

        
        sendAction.execute(context);

    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageOverwriteMessageElementsXPathWithNamespace() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<ns0:TestRequest xmlns:ns0=\"http://citrusframework.org/unittest\">" +
                "<ns0:Message>?</ns0:Message></ns0:TestRequest>");
        
        Map<String, String> overwriteElements = new HashMap<String, String>();
        overwriteElements.put("/ns0:TestRequest/ns0:Message", "Hello World!");

        XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(overwriteElements);
        messageBuilder.add(interceptor);
        
        sendAction.setMessageBuilder(messageBuilder);
        
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

        
        sendAction.execute(context);

    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageOverwriteMessageElementsXPathWithDefaultNamespace() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest xmlns=\"http://citrusframework.org/unittest\">" +
                "<Message>?</Message></TestRequest>");
        
        Map<String, String> overwriteElements = new HashMap<String, String>();
        overwriteElements.put("/:TestRequest/:Message", "Hello World!");

        XpathMessageConstructionInterceptor interceptor = new XpathMessageConstructionInterceptor(overwriteElements);
        messageBuilder.add(interceptor);
        
        sendAction.setMessageBuilder(messageBuilder);
        
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

        
        sendAction.execute(context);

    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithMessageHeaders() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        final Map<String, Object> controlHeaders = new HashMap<String, Object>();
        controlHeaders.put("Operation", "sayHello");
        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", controlHeaders);

        final Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "sayHello");
        messageBuilder.setMessageHeaders(headers);
        
        sendAction.setMessageBuilder(messageBuilder);

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

        
        sendAction.execute(context);

    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithHeaderValuesVariableSupport() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        context.setVariable("myOperation", "sayHello");
        
        final Map<String, Object> controlHeaders = new HashMap<String, Object>();
        controlHeaders.put("Operation", "sayHello");
        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", controlHeaders);

        final Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "${myOperation}");
        messageBuilder.setMessageHeaders(headers);
        
        sendAction.setMessageBuilder(messageBuilder);

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

        
        sendAction.execute(context);

    }
    
    @Test
    public void testSendMessageWithUnknwonVariableInMessagePayload() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>${myText}</Message></TestRequest>");
        
        sendAction.setMessageBuilder(messageBuilder);

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpoint.getActor()).thenReturn(null);

        
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
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        final Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "${myOperation}");
        messageBuilder.setMessageHeaders(headers);
        
        sendAction.setMessageBuilder(messageBuilder);

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpoint.getActor()).thenReturn(null);

        
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
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        final Map<String, Object> controlHeaders = new HashMap<String, Object>();
        controlHeaders.put("Operation", "sayHello");
        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", controlHeaders);

        final Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "sayHello");
        messageBuilder.setMessageHeaders(headers);
        
        sendAction.setMessageBuilder(messageBuilder);
        
        Map<String, String> extractVars = new HashMap<String, String>();
        extractVars.put("Operation", "myOperation");
        extractVars.put(MessageHeaders.ID, "correlationId");
        
        List<VariableExtractor> variableExtractors = new ArrayList<VariableExtractor>();
        MessageHeaderVariableExtractor variableExtractor = new MessageHeaderVariableExtractor();
        variableExtractor.setHeaderMappings(extractVars);
        
        variableExtractors.add(variableExtractor);
        sendAction.setVariableExtractors(variableExtractors);

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

        
        sendAction.execute(context);
        
        Assert.assertNotNull(context.getVariable("myOperation"));
        Assert.assertNotNull(context.getVariable("correlationId"));

    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testMissingMessagePayload() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setEndpoint(endpoint);

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

        
        sendAction.execute(context);

    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithXmlDeclaration() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestRequest><Message>Hello World!</Message></TestRequest>");
        
        sendAction.setMessageBuilder(messageBuilder);
        
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

        
        sendAction.execute(context);

    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithUTF16Encoding() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<?xml version=\"1.0\" encoding=\"UTF-16\"?><TestRequest><Message>Hello World!</Message></TestRequest>");
        
        sendAction.setMessageBuilder(messageBuilder);
        
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

        
        sendAction.execute(context);

    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithISOEncoding() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><TestRequest><Message>Hello World!</Message></TestRequest>");
        
        sendAction.setMessageBuilder(messageBuilder);
        
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

        
        sendAction.execute(context);

    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithUnsupportedEncoding() {
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<?xml version=\"1.0\" encoding=\"MyUnsupportedEncoding\"?><TestRequest><Message>Hello World!</Message></TestRequest>");
        
        sendAction.setMessageBuilder(messageBuilder);

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        when(endpoint.getActor()).thenReturn(null);

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
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setEndpoint(endpoint);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadResourcePath("classpath:com/consol/citrus/actions/test-request-iso-encoding.xml");
        
        sendAction.setMessageBuilder(messageBuilder);
        
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

        
        sendAction.execute(context);

    }
    
    @Test
    public void testDisabledSendMessage() {
        TestCase testCase = new TestCase();
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setEndpoint(endpoint);
        
        TestActor disabledActor = new TestActor();
        disabledActor.setDisabled(true);
        sendAction.setActor(disabledActor);
        
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        sendAction.setMessageBuilder(messageBuilder);

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpoint.getActor()).thenReturn(null);
        testCase.addTestAction(sendAction);
        testCase.execute(context);

    }
    
    @Test
    public void testDisabledSendMessageByEndpointActor() {
        TestCase testCase = new TestCase();
        SendMessageAction sendAction = new SendMessageAction();
        sendAction.setEndpoint(endpoint);
        
        TestActor disabledActor = new TestActor();
        disabledActor.setDisabled(true);
        
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        sendAction.setMessageBuilder(messageBuilder);

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpoint.getActor()).thenReturn(disabledActor);
        testCase.addTestAction(sendAction);
        testCase.execute(context);

    }
    
}
