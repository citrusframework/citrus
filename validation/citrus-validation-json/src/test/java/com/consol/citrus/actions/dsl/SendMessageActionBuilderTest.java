package com.consol.citrus.actions.dsl;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.consol.citrus.DefaultTestCaseRunner;
import com.consol.citrus.TestCase;
import com.consol.citrus.UnitTestSupport;
import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.actions.SendMessageAction;
import com.consol.citrus.container.SequenceAfterTest;
import com.consol.citrus.container.SequenceBeforeTest;
import com.consol.citrus.context.SpringBeanReferenceResolver;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.JsonSupport;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.EndpointConfiguration;
import com.consol.citrus.json.JsonSchemaRepository;
import com.consol.citrus.json.schema.SimpleJsonSchema;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.message.builder.ObjectMappingPayloadBuilder;
import com.consol.citrus.messaging.Consumer;
import com.consol.citrus.messaging.Producer;
import com.consol.citrus.report.TestActionListeners;
import com.consol.citrus.spi.ReferenceResolver;
import com.consol.citrus.validation.builder.DefaultMessageBuilder;
import com.consol.citrus.validation.context.HeaderValidationContext;
import com.consol.citrus.validation.json.JsonMessageValidationContext;
import com.consol.citrus.validation.json.JsonPathMessageProcessor;
import com.consol.citrus.validation.json.JsonPathVariableExtractor;
import com.consol.citrus.validation.json.report.GraciousProcessingReport;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import com.consol.citrus.variable.dictionary.DataDictionary;
import com.consol.citrus.variable.dictionary.json.JsonMappingDataDictionary;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import org.mockito.Mockito;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.StringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.consol.citrus.actions.ReceiveMessageAction.Builder.receive;
import static com.consol.citrus.actions.SendMessageAction.Builder.send;
import static com.consol.citrus.dsl.JsonPathSupport.jsonPath;
import static com.consol.citrus.dsl.JsonSupport.json;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

/**
 * @author Christoph Deppisch
 */
public class SendMessageActionBuilderTest extends UnitTestSupport {

    private ReferenceResolver referenceResolver = Mockito.mock(ReferenceResolver.class);
    private Endpoint messageEndpoint = Mockito.mock(Endpoint.class);
    private Producer messageProducer = Mockito.mock(Producer.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testSendBuilderWithPayloadModel() {
        reset(referenceResolver, messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "{\"message\":\"Hello Citrus!\"}");
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
        runner.run(send(messageEndpoint).message().body(new ObjectMappingPayloadBuilder(new TestRequest("Hello Citrus!"))));

        final TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = ((SendMessageAction) test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), DefaultMessageBuilder.class);

        final DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "{\"message\":\"Hello Citrus!\"}");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0L);

    }

    @Test
    public void testSendBuilderWithPayloadModelExplicitObjectMapper() {
        reset(messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "{\"message\":\"Hello Citrus!\"}");
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint).message().body(new ObjectMappingPayloadBuilder(new TestRequest("Hello Citrus!"), mapper)));

        final TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = ((SendMessageAction) test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), DefaultMessageBuilder.class);

        final DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "{\"message\":\"Hello Citrus!\"}");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0L);

    }

    @Test
    public void testSendBuilderWithPayloadModelExplicitObjectMapperName() {
        reset(referenceResolver, messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "{\"message\":\"Hello Citrus!\"}");
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
        runner.run(send(messageEndpoint).message().body(new ObjectMappingPayloadBuilder(new TestRequest("Hello Citrus!"), "myObjectMapper")));

        final TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = ((SendMessageAction) test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getMessageBuilder().getClass(), DefaultMessageBuilder.class);

        final DefaultMessageBuilder messageBuilder = (DefaultMessageBuilder) action.getMessageBuilder();
        Assert.assertEquals(messageBuilder.buildMessagePayload(context, action.getMessageType()), "{\"message\":\"Hello Citrus!\"}");
        Assert.assertEquals(messageBuilder.buildMessageHeaders(context).size(), 0L);

    }

    @Test
    public void testSendBuilderExtractJsonPathFromPayload() {
        reset(messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(message.getPayload(String.class), "{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}");
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint).message().type(MessageType.JSON).body("{\"text\":\"Hello World!\", \"person\":{\"name\":\"John\",\"surname\":\"Doe\"}, \"index\":5, \"id\":\"x123456789x\"}").extract(jsonPath().expression("$.text", "text").expression("$.person", "person")));

        Assert.assertNotNull(context.getVariable("text"));
        Assert.assertNotNull(context.getVariable("person"));
        Assert.assertEquals(context.getVariable("text"), "Hello World!");
        Assert.assertTrue(context.getVariable("person").contains("\"John\""));

        final TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = ((SendMessageAction) test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertEquals(action.getVariableExtractors().size(), 1);
        Assert.assertTrue(action.getVariableExtractors().get(0) instanceof JsonPathVariableExtractor);
        Assert.assertTrue(((JsonPathVariableExtractor) action.getVariableExtractors().get(0)).getJsonPathExpressions().containsKey("$.text"));
        Assert.assertTrue(((JsonPathVariableExtractor) action.getVariableExtractors().get(0)).getJsonPathExpressions().containsKey("$.person"));

    }

    @Test
    public void testJsonPathSupport() {
        reset(messageEndpoint, messageProducer);
        when(messageEndpoint.createProducer()).thenReturn(messageProducer);
        when(messageEndpoint.getActor()).thenReturn(null);
        doAnswer(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            Assert.assertEquals(StringUtils.trimAllWhitespace(message.getPayload(String.class)), "{\"TestRequest\":{\"Message\":\"HelloWorld!\"}}");
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint).message().type(MessageType.JSON).body("{ \"TestRequest\": { \"Message\": \"?\" }}").process(jsonPath().expression("$.TestRequest.Message", "Hello World!")));

        final TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = ((SendMessageAction) test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);

        Assert.assertTrue(action.getMessageBuilder() instanceof DefaultMessageBuilder);
        Assert.assertEquals(action.getMessageProcessors().size(), 1);
        Assert.assertTrue(action.getMessageProcessors().get(0) instanceof JsonPathMessageProcessor);
        Assert.assertEquals(((JsonPathMessageProcessor) action.getMessageProcessors().get(0)).getJsonPathExpressions().get("$.TestRequest.Message"), "Hello World!");
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
            Assert.assertEquals(StringUtils.trimAllWhitespace(message.getPayload(String.class)), "{\"TestRequest\":{\"Message\":\"HelloWorld!\"}}");
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint).message().type(MessageType.JSON).body("{ \"TestRequest\": { \"Message\": \"?\" }}").dictionary(dictionary));

        final TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = ((SendMessageAction) test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getDataDictionary(), dictionary);
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
            Assert.assertEquals(StringUtils.trimAllWhitespace(message.getPayload(String.class)), "{\"TestRequest\":{\"Message\":\"HelloWorld!\"}}");
            return null;
        }).when(messageProducer).send(any(Message.class), any(TestContext.class));

        when(referenceResolver.resolve(TestContext.class)).thenReturn(context);
        when(referenceResolver.resolve(TestActionListeners.class)).thenReturn(new TestActionListeners());
        when(referenceResolver.resolveAll(SequenceBeforeTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolveAll(SequenceAfterTest.class)).thenReturn(new HashMap<>());
        when(referenceResolver.resolve("customDictionary", DataDictionary.class)).thenReturn(dictionary);

        context.setReferenceResolver(referenceResolver);
        DefaultTestCaseRunner runner = new DefaultTestCaseRunner(context);
        runner.run(send(messageEndpoint).message().type(MessageType.JSON).body("{ \"TestRequest\": { \"Message\": \"?\" }}").dictionary("customDictionary"));

        final TestCase test = runner.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = ((SendMessageAction) test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertEquals(action.getDataDictionary(), dictionary);
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
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), SendMessageAction.class);

        final SendMessageAction action = ((SendMessageAction) test.getActions().get(0));
        Assert.assertEquals(action.getName(), "send");

        Assert.assertEquals(action.getEndpoint(), messageEndpoint);
        Assert.assertTrue(action.isSchemaValidation());
        Assert.assertEquals(action.getSchema(), "fooSchema");
        Assert.assertEquals(action.getSchemaRepository(), "fooRepository");

    }


}

