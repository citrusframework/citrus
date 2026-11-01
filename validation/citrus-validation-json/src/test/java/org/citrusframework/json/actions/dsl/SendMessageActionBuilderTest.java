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

package org.citrusframework.json.actions.dsl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.citrusframework.TestCase;
import org.citrusframework.actions.SendMessageAction;
import org.citrusframework.base.DefaultTestCaseRunner;
import org.citrusframework.container.SequenceAfterTest;
import org.citrusframework.container.SequenceBeforeTest;
import org.citrusframework.context.TestContext;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.json.JsonSchemaRepository;
import org.citrusframework.json.UnitTestSupport;
import org.citrusframework.json.variable.dictionary.JsonMappingDataDictionary;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageType;
import org.citrusframework.messaging.Producer;
import org.citrusframework.report.TestActionListeners;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.validation.builder.DefaultMessageBuilder;
import org.citrusframework.validation.json.JsonPathMessageProcessor;
import org.citrusframework.validation.json.JsonPathVariableExtractor;
import org.citrusframework.variable.dictionary.DataDictionary;
import org.mockito.Mockito;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class SendMessageActionBuilderTest extends UnitTestSupport implements TestActionSupport {

    private final ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);
    private final Endpoint messageEndpoint = Mockito.mock(Endpoint.class);
    private final Producer messageProducer = Mockito.mock(Producer.class);

    private final JsonMapper mapper = JsonMapper.shared();

    @Test
    public void testSendBuilderWithPayloadModel() {
        reset(referenceResolver, messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assertions.assertEquals("{\"message\":\"Hello Citrus!\"}", message.getPayload(String.class));
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(ObjectMapper.class)).thenReturn(Collections.singletonMap("mapper", mapper));
        when(referenceResolver.resolve(ObjectMapper.class)).thenReturn(mapper);

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint).message().body(buildPayload().json().marshal(new TestRequest("Hello Citrus!"))));

        final TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendMessageAction.class, test.getActions().get(0).getClass());

        final SendMessageAction action = ((SendMessageAction) test.getActions().get(0));
        Assertions.assertEquals("send", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(DefaultMessageBuilder.class, action.getMessageBuilder().getClass());

        final DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals("{\"message\":\"Hello Citrus!\"}", messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(0L, messageBuilder.buildMessageHeaders(context).size());

    }

    @Test
    public void testSendBuilderWithPayloadModelExplicitObjectMapper() {
        reset(messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assertions.assertEquals("{\"message\":\"Hello Citrus!\"}", message.getPayload(String.class));
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint).message().body(buildPayload().json().marshal(new TestRequest("Hello Citrus!"), mapper)));

        final TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendMessageAction.class, test.getActions().get(0).getClass());

        final SendMessageAction action = ((SendMessageAction) test.getActions().get(0));
        Assertions.assertEquals("send", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(DefaultMessageBuilder.class, action.getMessageBuilder().getClass());

        final DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals("{\"message\":\"Hello Citrus!\"}", messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(0L, messageBuilder.buildMessageHeaders(context).size());

    }

    @Test
    public void testSendBuilderWithPayloadModelExplicitObjectMapperName() {
        reset(referenceResolver, messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assertions.assertEquals("{\"message\":\"Hello Citrus!\"}", message.getPayload(String.class));
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.isResolvable("myObjectMapper")).thenReturn(true);
        when(referenceResolver.resolve("myObjectMapper", ObjectMapper.class)).thenReturn(mapper);

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint).message().body(buildPayload().json().marshal(new TestRequest("Hello Citrus!"), "myObjectMapper")));

        final TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendMessageAction.class, test.getActions().get(0).getClass());

        final SendMessageAction action = ((SendMessageAction) test.getActions().get(0));
        Assertions.assertEquals("send", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(DefaultMessageBuilder.class, action.getMessageBuilder().getClass());

        final DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder) action.getMessageBuilder();
        Assertions.assertEquals("{\"message\":\"Hello Citrus!\"}", messageBuilder.buildMessagePayload(context, action.getMessageType()));
        Assertions.assertEquals(0L, messageBuilder.buildMessageHeaders(context).size());

    }

    @Test
    public void testSendBuilderExtractJsonPathFromPayload() {
        reset(messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assertions.assertEquals("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}", message.getPayload(String.class));
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint)
                .message()
                .type(MessageType.JSON)
                .body("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}")
                .extract(extractor().jsonPath().expression("$.text", "text")
                        .expression("$.person", "person")));

        Assertions.assertNotNull(context.getVariable("text"));
        Assertions.assertNotNull(context.getVariable("person"));
        Assertions.assertEquals("Hello World!", context.getVariable("text"));
        Assertions.assertTrue(context.getVariable("person").contains("\"John\""));

        final TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendMessageAction.class, test.getActions().get(0).getClass());

        final SendMessageAction action = ((SendMessageAction) test.getActions().get(0));
        Assertions.assertEquals("send", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());

        Assertions.assertEquals(1, action.getVariableExtractors().size());
        Assertions.assertTrue(action.getVariableExtractors().get(0) instanceof JsonPathVariableExtractor);
        Assertions.assertTrue(((JsonPathVariableExtractor) action.getVariableExtractors().get(0)).getJsonPathExpressions().containsKey("$.text"));
        Assertions.assertTrue(((JsonPathVariableExtractor) action.getVariableExtractors().get(0)).getJsonPathExpressions().containsKey("$.person"));

    }

    @Test
    public void testJsonPathSupport() {
        reset(messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assertions.assertEquals("{\"TestRequest\":{\"Message\":\"HelloWorld!\"}}", message.getPayload(String.class).replaceAll("\\s", ""));
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint)
                .message()
                .type(MessageType.JSON)
                .body("{ \"TestRequest\": { \"Message\": \"?\" }}")
                .process(processor().json()
                            .jsonPath()
                            .expression("$.TestRequest.Message", "Hello World!")));

        final TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendMessageAction.class, test.getActions().get(0).getClass());

        final SendMessageAction action = ((SendMessageAction) test.getActions().get(0));
        Assertions.assertEquals("send", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());

        Assertions.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assertions.assertEquals(1, action.getMessageProcessors().size());
        Assertions.assertTrue(action.getMessageProcessors().get(0) instanceof JsonPathMessageProcessor);
        Assertions.assertEquals("Hello World!", ((JsonPathMessageProcessor) action.getMessageProcessors().get(0)).getJsonPathExpressions().get("$.TestRequest.Message"));
    }

    @Test
    public void testSendBuilderWithDictionary() {
        final JsonMappingDataDictionary dictionary = new JsonMappingDataDictionary();
        dictionary.getMappings().put("TestRequest.Message", "Hello World!");

        reset(messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assertions.assertEquals("{\"TestRequest\":{\"Message\":\"HelloWorld!\"}}", message.getPayload(String.class).replaceAll("\\s", ""));
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint)
                .message()
                .type(MessageType.JSON)
                .body("{ \"TestRequest\": { \"Message\": \"?\" }}")
                .dictionary(dictionary));

        final TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendMessageAction.class, test.getActions().get(0).getClass());

        final SendMessageAction action = ((SendMessageAction) test.getActions().get(0));
        Assertions.assertEquals("send", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(dictionary, action.getDataDictionary());
    }

    @Test
    public void testSendBuilderWithDictionaryName() {
        final JsonMappingDataDictionary dictionary = new JsonMappingDataDictionary();
        dictionary.getMappings().put("TestRequest.Message", "Hello World!");

        reset(referenceResolver, messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assertions.assertEquals("{\"TestRequest\":{\"Message\":\"HelloWorld!\"}}", message.getPayload(String.class).replaceAll("\\s", ""));
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolve("customDictionary", DataDictionary.class)).thenReturn(dictionary);

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint)
                .message()
                .type(MessageType.JSON)
                .body("{ \"TestRequest\": { \"Message\": \"?\" }}")
                .dictionary("customDictionary"));

        final TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendMessageAction.class, test.getActions().get(0).getClass());

        final SendMessageAction action = ((SendMessageAction) test.getActions().get(0));
        Assertions.assertEquals("send", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertEquals(dictionary, action.getDataDictionary());
    }

    @Test
    public void testSendWithSchemaValidation() {

        reset(referenceResolver, messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);

        JsonSchemaRepository schemaRepository = new JsonSchemaRepository();
        schemaRepository.setName("fooRepository");

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolveAll(JsonSchemaRepository.class)).thenReturn(Map.of("fooRepository",schemaRepository));

        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint).message()
                        .schemaValidation(true)
                        .schema("fooSchema")
                        .schemaRepository("fooRepository")
                .type(MessageType.JSON).body("{ \"TestRequest\": { \"Message\": \"?\" }}"));

        final TestCase test = runner.getTestCase();
        Assertions.assertEquals(1, test.getActionCount());
        Assertions.assertEquals(SendMessageAction.class, test.getActions().get(0).getClass());

        final SendMessageAction action = ((SendMessageAction) test.getActions().get(0));
        Assertions.assertEquals("send", action.getName());

        Assertions.assertEquals(messageEndpoint, action.getEndpoint());
        Assertions.assertTrue(action.isSchemaValidation());
        Assertions.assertEquals("fooSchema", action.getSchema());
        Assertions.assertEquals("fooRepository", action.getSchemaRepository());

    }
}
