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

package org.citrusframework.xml.actions;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.ReceiveMessageAction;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.DelegatingPathExpressionProcessor;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.message.MessageType;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.TestUtils;
import org.citrusframework.validation.DefaultMessageValidator;
import org.citrusframework.validation.DefaultTextEqualsMessageValidator;
import org.citrusframework.validation.DelegatingPayloadVariableExtractor;
import org.citrusframework.validation.MessageValidator;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.context.DefaultMessageValidationContext;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.context.ValidationStatus;
import org.citrusframework.validation.context.json.JsonPathMessageValidationContext;
import org.citrusframework.validation.context.script.DefaultScriptValidationContext;
import org.citrusframework.validation.context.script.ScriptValidationContext;
import org.citrusframework.validation.context.xml.XmlMessageValidationContext;
import org.citrusframework.validation.context.xml.XpathMessageValidationContext;
import org.citrusframework.variable.MessageHeaderVariableExtractor;
import org.citrusframework.variable.dictionary.DataDictionary;
import org.citrusframework.xml.NoopMessageProcessor;
import org.citrusframework.xml.NoopVariableExtractor;
import org.citrusframework.xml.XmlTestLoader;
import org.mockito.Mockito;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.citrusframework.endpoint.direct.DirectEndpoints.direct;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;

public class ReceiveTest extends AbstractXmlActionTest {

    @BindToRegistry
    final DataDictionary<?> myDataDictionary = Mockito.mock(DataDictionary.class);

    @BindToRegistry
    final MessageValidator<?> myValidator = Mockito.mock(MessageValidator.class);

    @BindToRegistry
    final MessageValidator<?> defaultMessageValidator = Mockito.mock(MessageValidator.class);

    @Test
    @SuppressWarnings("unchecked")
    public void shouldLoadReceive() throws IOException {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/xml/actions/receive.citrus.it.xml");

        MessageQueue helloQueue = new DefaultMessageQueue("helloQueue");
        context.getMessageValidatorRegistry().addMessageValidator("textEqualsMessageValidator", new DefaultTextEqualsMessageValidator().enableTrim().normalizeLineEndings());
        context.getMessageValidatorRegistry().addMessageValidator("scriptMessageValidator", new DefaultMessageValidator() {
            @Override
            public ValidationContext findValidationContext(List<ValidationContext> validationContexts) {
                Optional<ValidationContext> scriptValidationContext = validationContexts.stream()
                        .filter(ScriptValidationContext.class::isInstance)
                        .findFirst();

                return scriptValidationContext.orElseGet(() -> super.findValidationContext(validationContexts));
            }
        });
        context.getMessageValidatorRegistry().addMessageValidator("jsonPathMessageValidator", new DefaultMessageValidator() {
            @Override
            public ValidationContext findValidationContext(List<ValidationContext> validationContexts) {
                Optional<ValidationContext> validationContext = validationContexts.stream()
                        .filter(JsonPathMessageValidationContext.class::isInstance)
                        .findFirst();

                return validationContext.orElseGet(() -> super.findValidationContext(validationContexts));
            }
        });
        context.getMessageValidatorRegistry().addMessageValidator("xpathMessageValidator", new DefaultMessageValidator() {
            @Override
            public ValidationContext findValidationContext(List<ValidationContext> validationContexts) {
                Optional<ValidationContext> validationContext = validationContexts.stream()
                        .filter(XpathMessageValidationContext.class::isInstance)
                        .findFirst();

                return validationContext.orElseGet(() -> super.findValidationContext(validationContexts));
            }
        });

        context.getReferenceResolver().bind("helloQueue", helloQueue);
        context.getReferenceResolver().bind("helloEndpoint", direct().asynchronous().queue(helloQueue).build());

        context.getReferenceResolver().bind("jsonPathVariableExtractorBuilder", new NoopVariableExtractor.Builder());
        context.getReferenceResolver().bind("xpathVariableExtractorBuilder", new NoopVariableExtractor.Builder());

        context.getReferenceResolver().bind("jsonPathMessageProcessorBuilder", new NoopMessageProcessor.Builder());
        context.getReferenceResolver().bind("xpathMessageProcessorBuilder", new NoopMessageProcessor.Builder());

        doAnswer(invocationOnMock -> {
            ((List<ValidationContext>) invocationOnMock.getArgument(3, List.class)).forEach(context -> context.updateStatus(ValidationStatus.PASSED));
            return null;
        }).when(myValidator).validateMessage(any(Message.class), any(Message.class), eq(context), anyList());

        doAnswer(invocationOnMock -> {
            ((List<ValidationContext>) invocationOnMock.getArgument(3, List.class)).forEach(context -> context.updateStatus(ValidationStatus.PASSED));
            return null;
        }).when(defaultMessageValidator).validateMessage(any(Message.class), any(Message.class), eq(context), anyList());

        helloQueue.send(new DefaultMessage("Hello from Citrus!").setHeader("operation", "sayHello"));
        helloQueue.send(new DefaultMessage("<TestMessage>Hello Citrus</TestMessage>").setHeader("operation", "sayHello"));
        helloQueue.send(new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<TestMessage xmlns=\"http://citrusframework.org/test\">Hello Citrus</TestMessage>")
                                .addHeaderData("<Header xmlns=\"http://citrusframework.org/test\"><operation>hello</operation></Header>")
                                .setHeader("operation", "sayHello"));
        helloQueue.send(new DefaultMessage("""
                <?xml version="1.0" encoding="UTF-8"?>
                <TestRequest>
                    <Message>Hello World!</Message>
                </TestRequest>""").setHeader("operation", "sayHello"));
        helloQueue.send(new DefaultMessage("<TestMessage>Hello Citrus</TestMessage>").setHeader("operation", "sayHello"));
        helloQueue.send(new DefaultMessage("<TestMessage>Hello Citrus</TestMessage>").setHeader("operation", "sayHello"));
        helloQueue.send(new DefaultMessage("<ns:TestMessage xmlns:ns=\"http://citrusframework.org\">Hello Citrus</ns:TestMessage>").setHeader("operation", "sayHello"));
        helloQueue.send(new DefaultMessage("<TestMessage>Hello Citrus</TestMessage>").setHeader("operation", "sayHello"));
        helloQueue.send(new DefaultMessage("<TestMessage>Hello Citrus</TestMessage>").setHeader("operation", "sayHello"));
        helloQueue.send(new DefaultMessage("<TestMessage>Hello Citrus</TestMessage>").setHeader("operation", "sayHello"));
        helloQueue.send(new DefaultMessage("<TestMessage>Hello Citrus</TestMessage>").setHeader("operation", "sayHello"));
        helloQueue.send(new DefaultMessage("<TestMessage>Hello Citrus</TestMessage>").setHeader("operation", "sayHello"));
        helloQueue.send(new DefaultMessage("<TestMessage>Hello Citrus</TestMessage>").setHeader("operation", "sayHello"));
        helloQueue.send(new DefaultMessage("{ \"FooMessage\": { \"foo\": \"Hello World!\" }, { \"bar\": \"@ignore@\" }}").setHeader("operation", "sayHello"));
        helloQueue.send(new DefaultMessage("{ \"message\": { \"text\": \"Hello World!\" }, { \"bar\": \"@ignore@\" }}").setHeader("operation", "sayHello"));
        helloQueue.send(new DefaultMessage("<TestMessage>Hello Citrus</TestMessage>").setHeader("operation", "sayHello"));
        helloQueue.send(new DefaultMessage("<TestMessage>Hello Citrus</TestMessage>").setHeader("operation", "sayHello"));

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("ReceiveTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(17L, result.getActionCount());

        int actionIndex = 0;

        ReceiveMessageAction action = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(ReceiveMessageAction.class, action.getClass());
        Assertions.assertEquals(10000L, action.getReceiveTimeout());
        Assertions.assertEquals("Hello from Citrus!", action.getMessageBuilder().build(context, MessageType.PLAINTEXT.name()).getPayload(String.class));

        action = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertTrue(action.getMessageSelectorMap().isEmpty());
        Assertions.assertNull(action.getMessageSelector());
        Assertions.assertEquals("helloEndpoint", action.getEndpointUri());

        Assertions.assertEquals(2, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assertions.assertTrue(action.getValidationContexts().get(1) instanceof DefaultMessageValidationContext);

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assertions.assertEquals("<TestMessage>Hello Citrus</TestMessage>", messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(1, messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals("sayHello", messageBuilder.buildMessageHeaders(context).get("operation"));
        Assertions.assertEquals(0, action.getMessageProcessors().size());
        Assertions.assertEquals(0, action.getControlMessageProcessors().size());

        Assertions.assertNull(action.getDataDictionary());
        Assertions.assertEquals(0, action.getMessageProcessors().size());
        Assertions.assertEquals(0, action.getControlMessageProcessors().size());

        action = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertTrue(action.getMessageSelectorMap().isEmpty());
        Assertions.assertNull(action.getMessageSelector());
        Assertions.assertEquals("helloEndpoint", action.getEndpointUri());

        Assertions.assertEquals(2, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assertions.assertTrue(action.getValidationContexts().get(1) instanceof DefaultMessageValidationContext);

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assertions.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<TestMessage xmlns=\"http://citrusframework.org/test\">Hello Citrus</TestMessage>", TestUtils.normalizeLineEndings(messageBuilder.buildMessagePayload(context, action.getMessageType()).toString()));
        Assertions.assertEquals(1, messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals("sayHello", messageBuilder.buildMessageHeaders(context).get("operation"));
        Assertions.assertEquals(1, messageBuilder.buildMessageHeaderData(context).size());
        Assertions.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<Header xmlns=\"http://citrusframework.org/test\">\n  <operation>hello</operation>\n</Header>", TestUtils.normalizeLineEndings(messageBuilder.buildMessageHeaderData(context).get(0).trim()));
        Assertions.assertEquals(0, action.getMessageProcessors().size());
        Assertions.assertEquals(0, action.getControlMessageProcessors().size());

        Assertions.assertNull(action.getDataDictionary());
        Assertions.assertEquals(0, action.getMessageProcessors().size());
        Assertions.assertEquals(0, action.getControlMessageProcessors().size());

        action = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(1, action.getMessageSelectorMap().size());
        Assertions.assertEquals("sayHello", action.getMessageSelectorMap().get("operation"));
        Assertions.assertNull(action.getMessageSelector());
        Assertions.assertEquals("helloEndpoint", action.getEndpointUri());

        Assertions.assertEquals(2, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().get(0) instanceof HeaderValidationContext);
        Assertions.assertTrue(action.getValidationContexts().get(1) instanceof DefaultMessageValidationContext);

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assertions.assertEquals(FileUtils.readToString(FileUtils.getFileResource("classpath:org/citrusframework/xml/test-request-payload.xml")), messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(0, messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals(0, action.getMessageProcessors().size());
        Assertions.assertEquals(0, action.getControlMessageProcessors().size());

        action = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertTrue(action.getMessageSelectorMap().isEmpty());
        Assertions.assertEquals("operation = 'sayHello'", action.getMessageSelector());
        Assertions.assertEquals("helloEndpoint", action.getEndpointUri());

        action = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(2, action.getVariableExtractors().size());
        Assertions.assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        MessageHeaderVariableExtractor headerVariableExtractor = (MessageHeaderVariableExtractor)action.getVariableExtractors().get(0);
        Assertions.assertTrue(action.getVariableExtractors().get(1) instanceof DelegatingPayloadVariableExtractor);
        DelegatingPayloadVariableExtractor variableExtractor = (DelegatingPayloadVariableExtractor)action.getVariableExtractors().get(1);

        Assertions.assertEquals(0L, variableExtractor.getNamespaces().size());
        Assertions.assertEquals(1, headerVariableExtractor.getHeaderMappings().size());
        Assertions.assertEquals("operation", headerVariableExtractor.getHeaderMappings().get("operation"));
        Assertions.assertEquals(1, variableExtractor.getPathExpressions().size());
        Assertions.assertEquals("text", variableExtractor.getPathExpressions().get("/TestMessage/text()"));

        Assertions.assertNotNull(action.getDataDictionary());

        action = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(2, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().get(0) instanceof XmlMessageValidationContext);
        Assertions.assertTrue(action.getValidationContexts().get(1) instanceof HeaderValidationContext);

        XmlMessageValidationContext xmlValidationContext = (XmlMessageValidationContext)action.getValidationContexts().get(0);
        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assertions.assertEquals("<ns:TestMessage xmlns:ns=\"http://citrusframework.org\">Hello Citrus</ns:TestMessage>", messageBuilder.buildMessagePayload(context, action.getMessageType()));

        Assertions.assertEquals(0, action.getMessageProcessors().size());
        Assertions.assertEquals(1, action.getControlMessageProcessors().size());
        Assertions.assertTrue(action.getControlMessageProcessors().get(0) instanceof DelegatingPathExpressionProcessor);
        DelegatingPathExpressionProcessor messageProcessor = (DelegatingPathExpressionProcessor)action.getControlMessageProcessors().get(0);

        Assertions.assertEquals(1, messageProcessor.getPathExpressions().size());
        Assertions.assertEquals("newValue", messageProcessor.getPathExpressions().get("/ns:TestMessage/"));

        Assertions.assertFalse(xmlValidationContext.isSchemaValidationEnabled());

        Assertions.assertEquals(1, xmlValidationContext.getIgnoreExpressions().size());
        Assertions.assertEquals("/ns:TestMessage/ns:ignore", xmlValidationContext.getIgnoreExpressions().iterator().next());
        Assertions.assertEquals(1, xmlValidationContext.getNamespaces().size());
        Assertions.assertEquals("http://citrusframework.org/test", xmlValidationContext.getNamespaces().get("ctx"));
        Assertions.assertEquals(1, xmlValidationContext.getControlNamespaces().size());
        Assertions.assertEquals("http://citrusframework.org", xmlValidationContext.getControlNamespaces().get("ns"));

        action = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(2, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().get(0) instanceof XpathMessageValidationContext);
        Assertions.assertTrue(action.getValidationContexts().get(1) instanceof HeaderValidationContext);
        XpathMessageValidationContext xPathValidationContext = (XpathMessageValidationContext)action.getValidationContexts().get(0);
        Assertions.assertNull(action.getEndpoint());
        Assertions.assertEquals("direct:helloQueue", action.getEndpointUri());

        Assertions.assertTrue(xPathValidationContext.isSchemaValidationEnabled());

        Assertions.assertEquals(2, xPathValidationContext.getXpathExpressions().size());
        Assertions.assertEquals("Hello Citrus", xPathValidationContext.getXpathExpressions().get("/TestMessage/text"));
        Assertions.assertEquals("true", xPathValidationContext.getXpathExpressions().get("boolean:/TestMessage/foo"));

        action = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(2, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().get(0) instanceof XpathMessageValidationContext);
        Assertions.assertTrue(action.getValidationContexts().get(1) instanceof HeaderValidationContext);
        xPathValidationContext = (XpathMessageValidationContext)action.getValidationContexts().get(0);
        Assertions.assertNull(action.getEndpoint());
        Assertions.assertEquals("direct:helloQueue", action.getEndpointUri());

        Assertions.assertTrue(xPathValidationContext.isSchemaValidationEnabled());

        Assertions.assertEquals(2, xPathValidationContext.getXpathExpressions().size());
        Assertions.assertEquals("Hello Citrus", xPathValidationContext.getXpathExpressions().get("/TestMessage/text"));
        Assertions.assertEquals("true", xPathValidationContext.getXpathExpressions().get("boolean:/TestMessage/foo"));

        action = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(3, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().get(0) instanceof ScriptValidationContext);
        Assertions.assertTrue(action.getValidationContexts().get(1) instanceof XpathMessageValidationContext);
        Assertions.assertTrue(action.getValidationContexts().get(2) instanceof HeaderValidationContext);
        DefaultScriptValidationContext scriptValidationContext = (DefaultScriptValidationContext)action.getValidationContexts().get(0);
        Assertions.assertNull(action.getEndpoint());
        Assertions.assertEquals("direct:helloQueue", action.getEndpointUri());

        xPathValidationContext = (XpathMessageValidationContext)action.getValidationContexts().get(1);
        Assertions.assertTrue(xPathValidationContext.isSchemaValidationEnabled());

        Assertions.assertEquals(1, xPathValidationContext.getXpathExpressions().size());
        Assertions.assertEquals("true", xPathValidationContext.getXpathExpressions().get("boolean:/TestMessage/foo"));

        Assertions.assertEquals("groovy", scriptValidationContext.getScriptType());
        Assertions.assertEquals("assert true", scriptValidationContext.getValidationScript().trim());

        action = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(2, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().get(0) instanceof ScriptValidationContext);
        Assertions.assertTrue(action.getValidationContexts().get(1) instanceof HeaderValidationContext);
        scriptValidationContext = (DefaultScriptValidationContext)action.getValidationContexts().get(0);
        Assertions.assertNull(action.getEndpoint());
        Assertions.assertEquals("direct:helloQueue", action.getEndpointUri());

        Assertions.assertEquals("groovy", scriptValidationContext.getScriptType());
        Assertions.assertEquals("classpath:org/citrusframework/xml/test-validation-script.groovy", scriptValidationContext.getValidationScriptResourcePath());

        action = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(2, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().get(0) instanceof JsonPathMessageValidationContext);
        Assertions.assertTrue(action.getValidationContexts().get(1) instanceof HeaderValidationContext);
        JsonPathMessageValidationContext jsonPathValidationContext = (JsonPathMessageValidationContext)action.getValidationContexts().get(0);
        Assertions.assertNull(action.getEndpoint());
        Assertions.assertEquals("direct:helloQueue", action.getEndpointUri());

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);

        Assertions.assertEquals(2, jsonPathValidationContext.getJsonPathExpressions().size());
        Assertions.assertEquals("Hello Citrus", jsonPathValidationContext.getJsonPathExpressions().get("$.json.text"));
        Assertions.assertEquals("true", jsonPathValidationContext.getJsonPathExpressions().get("$..foo.bar"));

        action = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(2, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().get(0) instanceof JsonPathMessageValidationContext);
        Assertions.assertTrue(action.getValidationContexts().get(1) instanceof HeaderValidationContext);
        jsonPathValidationContext = (JsonPathMessageValidationContext)action.getValidationContexts().get(0);
        Assertions.assertNull(action.getEndpoint());
        Assertions.assertEquals("direct:helloQueue", action.getEndpointUri());

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);

        Assertions.assertEquals(2, jsonPathValidationContext.getJsonPathExpressions().size());
        Assertions.assertEquals("Hello Citrus", jsonPathValidationContext.getJsonPathExpressions().get("$.json.text"));
        Assertions.assertEquals("true", jsonPathValidationContext.getJsonPathExpressions().get("$..foo.bar"));

        action = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(2, action.getValidationContexts().size());
        Assertions.assertTrue(action.getValidationContexts().get(0) instanceof DefaultMessageValidationContext);
        Assertions.assertTrue(action.getValidationContexts().get(1) instanceof HeaderValidationContext);
        DefaultMessageValidationContext jsonValidationContext = (DefaultMessageValidationContext)action.getValidationContexts().get(0);

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assertions.assertEquals("{ \"FooMessage\": { \"foo\": \"Hello World!\" }, { \"bar\": \"@ignore@\" }}", messageBuilder.buildMessagePayload(context, action.getMessageType()));

        Assertions.assertEquals(0, action.getMessageProcessors().size());
        Assertions.assertEquals(1, action.getControlMessageProcessors().size());
        Assertions.assertTrue(action.getControlMessageProcessors().get(0) instanceof DelegatingPathExpressionProcessor);
        DelegatingPathExpressionProcessor jsonMessageProcessor = (DelegatingPathExpressionProcessor)action.getControlMessageProcessors().get(0);

        Assertions.assertEquals(1, jsonMessageProcessor.getPathExpressions().size());
        Assertions.assertEquals("newValue", jsonMessageProcessor.getPathExpressions().get("$.FooMessage.foo"));

        Assertions.assertEquals(1, jsonValidationContext.getIgnoreExpressions().size());
        Assertions.assertEquals("$.FooMessage.bar", jsonValidationContext.getIgnoreExpressions().iterator().next());

        action = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(2, action.getVariableExtractors().size());
        Assertions.assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        headerVariableExtractor = (MessageHeaderVariableExtractor)action.getVariableExtractors().get(0);
        Assertions.assertTrue(action.getVariableExtractors().get(1) instanceof DelegatingPayloadVariableExtractor);
        DelegatingPayloadVariableExtractor jsonVariableExtractor = (DelegatingPayloadVariableExtractor) action.getVariableExtractors().get(1);

        Assertions.assertEquals(1, headerVariableExtractor.getHeaderMappings().size());
        Assertions.assertEquals("operation", headerVariableExtractor.getHeaderMappings().get("operation"));
        Assertions.assertEquals(1, jsonVariableExtractor.getPathExpressions().size());
        Assertions.assertEquals("text", jsonVariableExtractor.getPathExpressions().get("$.message.text"));

        action = (ReceiveMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(1, action.getValidators().size());
        Assertions.assertEquals(context.getReferenceResolver().resolve("myValidator", MessageValidator.class), action.getValidators().get(0));
        HeaderValidationContext headerValidationContext = (HeaderValidationContext) action.getValidationContexts().get(0);
        Assertions.assertEquals(1, headerValidationContext.getValidatorNames().size());
        Assertions.assertEquals("myHeaderValidator", headerValidationContext.getValidatorNames().get(0));

        action = (ReceiveMessageAction) result.getTestAction(actionIndex);
        Assertions.assertEquals(2, action.getValidators().size());
        Assertions.assertEquals(context.getReferenceResolver().resolve("myValidator", MessageValidator.class), action.getValidators().get(0));
        Assertions.assertEquals(context.getReferenceResolver().resolve("defaultMessageValidator", MessageValidator.class), action.getValidators().get(1));
        headerValidationContext = (HeaderValidationContext) action.getValidationContexts().get(0);
        Assertions.assertEquals(2, headerValidationContext.getValidatorNames().size());
        Assertions.assertEquals("myHeaderValidator", headerValidationContext.getValidatorNames().get(0));
        Assertions.assertEquals("defaultHeaderValidator", headerValidationContext.getValidatorNames().get(1));
    }
}
