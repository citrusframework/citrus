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

package org.citrusframework.groovy.dsl;

import java.io.IOException;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.groovy.GroovyTestLoader;
import org.citrusframework.groovy.NoopMessageProcessor;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.DelegatingPathExpressionProcessor;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.message.MessageType;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.util.FileUtils;
import org.citrusframework.validation.DefaultMessageHeaderValidator;
import org.citrusframework.validation.DefaultTextEqualsMessageValidator;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.context.DefaultValidationContext;
import org.citrusframework.validation.context.HeaderValidationContext;
import org.citrusframework.variable.MessageHeaderVariableExtractor;
import org.citrusframework.variable.dictionary.DataDictionary;
import org.mockito.Mockito;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.citrusframework.endpoint.direct.DirectEndpoints.direct;

public class SendTest extends AbstractGroovyActionDslTest {

    @BindToRegistry
    final DataDictionary<?> myDataDictionary = Mockito.mock(DataDictionary.class);

    private final DefaultMessageHeaderValidator headerValidator = new DefaultMessageHeaderValidator();
    private final DefaultTextEqualsMessageValidator validator = new DefaultTextEqualsMessageValidator().enableTrim().normalizeLineEndings();

    @Test
    public void shouldLoadSend() throws IOException {
        GroovyTestLoader testLoader = createTestLoader("classpath:org/citrusframework/groovy/dsl/send.citrus.test.groovy");

        MessageQueue helloQueue = new DefaultMessageQueue("helloQueue");
        context.getReferenceResolver().bind("helloQueue", helloQueue);
        context.getReferenceResolver().bind("helloEndpoint", direct().asynchronous().queue(helloQueue).build());

        context.getReferenceResolver().bind("jsonPathMessageProcessorBuilder", new NoopMessageProcessor.Builder());
        context.getReferenceResolver().bind("xpathMessageProcessorBuilder", new NoopMessageProcessor.Builder());

        testLoader.load();

        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("SendTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(9L, result.getActionCount());
        Assertions.assertEquals(SendMessageAction.class, result.getTestAction(0).getClass());

        int actionIndex = 0;

        SendMessageAction action = (SendMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();
        Assertions.assertEquals("Hello from Citrus!", messageBuilder.build(context, MessageType.PLAINTEXT.name()).getPayload(String.class));

        Message controlMessage = new DefaultMessage("Hello from Citrus!")
                                        .setHeader("operation", "sayHello");
        Message receivedMessage = helloQueue.receive();
        headerValidator.validateMessage(receivedMessage, controlMessage, context, new HeaderValidationContext.Builder().build());
        validator.validateMessage(receivedMessage, controlMessage, context, new DefaultValidationContext());

        action = (SendMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assertions.assertEquals("<TestMessage>Hello Citrus</TestMessage>", messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(1, messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals("sayHello", messageBuilder.buildMessageHeaders(context).get("operation"));
        Assertions.assertEquals(0, action.getMessageProcessors().size());
        Assertions.assertEquals("helloEndpoint", action.getEndpointUri());

        Assertions.assertNull(action.getDataDictionary());

        controlMessage = new DefaultMessage("<TestMessage>Hello Citrus</TestMessage>")
                .setHeader("operation", "sayHello");
        receivedMessage = helloQueue.receive();
        headerValidator.validateMessage(receivedMessage, controlMessage, context, new HeaderValidationContext.Builder().build());
        validator.validateMessage(receivedMessage, controlMessage, context, new DefaultValidationContext());

        action = (SendMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assertions.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "            <TestMessage xmlns=\"http://citrusframework.org/test\">Hello Citrus</TestMessage>", messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(1, messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals("sayHello", messageBuilder.buildMessageHeaders(context).get("operation"));
        Assertions.assertEquals(1, messageBuilder.buildMessageHeaderData(context).size());
        Assertions.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "            <Header xmlns=\"http://citrusframework.org/test\"><operation>hello</operation></Header>", messageBuilder.buildMessageHeaderData(context).get(0).trim());
        Assertions.assertEquals(0, action.getMessageProcessors().size());
        Assertions.assertEquals("helloEndpoint", action.getEndpointUri());

        Assertions.assertNull(action.getDataDictionary());

        controlMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "            <TestMessage xmlns=\"http://citrusframework.org/test\">Hello Citrus</TestMessage>")
                .setHeader("operation", "sayHello");
        receivedMessage = helloQueue.receive();
        headerValidator.validateMessage(receivedMessage, controlMessage, context, new HeaderValidationContext.Builder().build());
        validator.validateMessage(receivedMessage, controlMessage, context, new DefaultValidationContext());

        action = (SendMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assertions.assertEquals(FileUtils.readToString(FileUtils.getFileResource("classpath:org/citrusframework/groovy/test-request-payload.xml")), messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(0, messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals(0, action.getMessageProcessors().size());
        Assertions.assertEquals("helloEndpoint", action.getEndpointUri());

        controlMessage = new DefaultMessage("""
                <?xml version="1.0" encoding="UTF-8"?>
                <TestRequest>
                    <Message>Hello World!</Message>
                </TestRequest>""");
        receivedMessage = helloQueue.receive();
        headerValidator.validateMessage(receivedMessage, controlMessage, context, new HeaderValidationContext.Builder().build());
        validator.validateMessage(receivedMessage, controlMessage, context, new DefaultValidationContext());

        action = (SendMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals(1, action.getVariableExtractors().size());
        Assertions.assertTrue(action.getVariableExtractors().get(0) instanceof MessageHeaderVariableExtractor);
        MessageHeaderVariableExtractor headerVariableExtractor = (MessageHeaderVariableExtractor)action.getVariableExtractors().get(0);

        Assertions.assertEquals(1, headerVariableExtractor.getHeaderMappings().size());
        Assertions.assertEquals("operation", headerVariableExtractor.getHeaderMappings().get("operation"));
        Assertions.assertEquals("helloEndpoint", action.getEndpointUri());

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assertions.assertEquals("<TestMessage>Hello Citrus</TestMessage>", messageBuilder.buildMessagePayload(context, action.getMessageType()));

        Assertions.assertEquals(1, action.getMessageProcessors().size());
        Assertions.assertTrue(action.getMessageProcessors().get(0) instanceof DelegatingPathExpressionProcessor);
        DelegatingPathExpressionProcessor messageProcessor = (DelegatingPathExpressionProcessor)action.getMessageProcessors().get(0);

        Assertions.assertEquals(1, messageProcessor.getPathExpressions().size());
        Assertions.assertEquals("newValue", messageProcessor.getPathExpressions().get("/TestMessage/text()"));

        Assertions.assertNotNull(action.getDataDictionary());

        controlMessage = new DefaultMessage("<TestMessage>Hello Citrus</TestMessage>")
                .setHeader("operation", "sayHello");
        receivedMessage = helloQueue.receive();
        headerValidator.validateMessage(receivedMessage, controlMessage, context, new HeaderValidationContext.Builder().build());
        validator.validateMessage(receivedMessage, controlMessage, context, new DefaultValidationContext());

        action = (SendMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assertions.assertEquals("<TestMessage>Hello Citrus</TestMessage>", messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(8, messageBuilder.buildMessageHeaders(context).size());
        Assertions.assertEquals(5, messageBuilder.buildMessageHeaders(context).get("intValue"));
        Assertions.assertEquals(10L, messageBuilder.buildMessageHeaders(context).get("longValue"));
        Assertions.assertEquals(10.0F, messageBuilder.buildMessageHeaders(context).get("floatValue"));
        Assertions.assertEquals(10.0D, messageBuilder.buildMessageHeaders(context).get("doubleValue"));
        Assertions.assertEquals((byte) 1, messageBuilder.buildMessageHeaders(context).get("byteValue"));
        Assertions.assertEquals((short) 10, messageBuilder.buildMessageHeaders(context).get("shortValue"));
        Assertions.assertEquals(true, messageBuilder.buildMessageHeaders(context).get("boolValue"));
        Assertions.assertEquals("Hello Citrus", messageBuilder.buildMessageHeaders(context).get("stringValue"));

        Assertions.assertNull(action.getEndpoint());
        Assertions.assertEquals("direct:helloQueue", action.getEndpointUri());

        Assertions.assertEquals(0, action.getMessageProcessors().size());

        controlMessage = new DefaultMessage("<TestMessage>Hello Citrus</TestMessage>")
                .setHeader("intValue" ,5)
                .setHeader("longValue" ,10L)
                .setHeader("floatValue" ,10.0F)
                .setHeader("doubleValue" ,10.0D)
                .setHeader("byteValue" , (byte) 1)
                .setHeader("shortValue" , (short) 10)
                .setHeader("boolValue" ,true)
                .setHeader("stringValue" ,"Hello Citrus");
        receivedMessage = helloQueue.receive();
        headerValidator.validateMessage(receivedMessage, controlMessage, context, new HeaderValidationContext.Builder().build());
        validator.validateMessage(receivedMessage, controlMessage, context, new DefaultValidationContext());

        action = (SendMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertEquals("helloEndpoint", action.getEndpointUri());

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        messageBuilder = (DefaultMessageBuilder)action.getMessageBuilder();

        Assertions.assertEquals("{ \"FooMessage\": { \"foo\": \"Hello World!\" }, { \"bar\": \"@ignore@\" }}", messageBuilder.buildMessagePayload(context, action.getMessageType()));

        Assertions.assertEquals(1, action.getMessageProcessors().size());
        Assertions.assertTrue(action.getMessageProcessors().get(0) instanceof DelegatingPathExpressionProcessor);
        DelegatingPathExpressionProcessor jsonMessageProcessor = (DelegatingPathExpressionProcessor)action.getMessageProcessors().get(0);

        Assertions.assertEquals(1, jsonMessageProcessor.getPathExpressions().size());
        Assertions.assertEquals("newValue", jsonMessageProcessor.getPathExpressions().get("$.FooMessage.foo"));

        controlMessage = new DefaultMessage("{ \"FooMessage\": { \"foo\": \"Hello World!\" }, { \"bar\": \"@ignore@\" }}");
        receivedMessage = helloQueue.receive();
        headerValidator.validateMessage(receivedMessage, controlMessage, context, new HeaderValidationContext.Builder().build());
        validator.validateMessage(receivedMessage, controlMessage, context, new DefaultValidationContext());

        action = (SendMessageAction) result.getTestAction(actionIndex++);
        Assertions.assertTrue(action.isSchemaValidation());
        Assertions.assertEquals("fooSchema", action.getSchema());
        Assertions.assertEquals("fooRepository", action.getSchemaRepository());

        controlMessage = new DefaultMessage("{ \"FooMessage\": { \"foo\": \"Hello World!\" }, { \"bar\": \"@ignore@\" }}");
        receivedMessage = helloQueue.receive();
        headerValidator.validateMessage(receivedMessage, controlMessage, context, new HeaderValidationContext.Builder().build());
        validator.validateMessage(receivedMessage, controlMessage, context, new DefaultValidationContext());

        action = (SendMessageAction) result.getTestAction(actionIndex);
        Assertions.assertTrue(action.isSchemaValidation());
        Assertions.assertEquals("fooSchema", action.getSchema());
        Assertions.assertEquals("fooRepository", action.getSchemaRepository());

        controlMessage = new DefaultMessage("<TestMessage>Hello Citrus</TestMessage>");
        receivedMessage = helloQueue.receive();
        headerValidator.validateMessage(receivedMessage, controlMessage, context, new HeaderValidationContext.Builder().build());
        validator.validateMessage(receivedMessage, controlMessage, context, new DefaultValidationContext());
    }
}
