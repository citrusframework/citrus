package org.citrusframework.openapi.generator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.citrusframework.message.MessagePayloadUtils.normalizeWhitespace;
import static org.citrusframework.util.FileUtils.readToString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import jakarta.servlet.http.Cookie;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.citrusframework.Citrus;
import org.citrusframework.CitrusInstanceManager;
import org.citrusframework.TestAction;
import org.citrusframework.TestCase;
import org.citrusframework.actions.SendMessageAction.SendMessageActionBuilder;
import org.citrusframework.common.SpringXmlTestLoader;
import org.citrusframework.common.TestLoader;
import org.citrusframework.config.CitrusSpringConfig;
import org.citrusframework.context.TestContext;
import org.citrusframework.endpoint.EndpointConfiguration;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpEndpointConfiguration;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.json.schema.SimpleJsonSchema;
import org.citrusframework.junit.jupiter.spring.CitrusSpringExtension;
import org.citrusframework.message.DefaultMessage;
import org.citrusframework.message.Message;
import org.citrusframework.messaging.Producer;
import org.citrusframework.messaging.SelectiveConsumer;
import org.citrusframework.openapi.generator.rest.multiparttest.request.MultiparttestControllerApi.PostFileRequest;
import org.citrusframework.openapi.generator.rest.petstore.request.PetApi.AddPetRequest;
import org.citrusframework.openapi.generator.rest.petstore.request.PetApi.GetPetByIdRequest;
import org.citrusframework.spi.Resources;
import org.citrusframework.testapi.ApiActionBuilderCustomizerService;
import org.citrusframework.testapi.GeneratedApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.MultiValueMap;

/**
 * This test tests the generated API
 */
@Isolated
@DirtiesContext
@ExtendWith({CitrusSpringExtension.class})
@SpringBootTest(classes = {CitrusSpringConfig.class, GeneratedApiIT.Config.class})
@TestPropertySource(
    properties = {"applicationServiceClient.basic.username=Max Mustermann",
        "applicationServiceClient.basic.password=Top secret"}
)
class GeneratedApiIT {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private HttpClient httpClientMock;

    @Mock
    private Producer producerMock;

    @Mock
    private SelectiveConsumer consumerMock;

    private TestContext testContext;

    @BeforeEach
    void beforeEach() {
        testContext = applicationContext.getBean(TestContext.class);
    }

    @Test
    void testValidationFailure() {
        mockProducerAndConsumer(createReceiveMessage("{\"some\":  \"payload\"}"));
        assertThatThrownBy(
            () -> executeTest("getPetByIdRequestTest", testContext)).hasCauseExactlyInstanceOf(
            ValidationException.class);
    }

    @Nested
    class WithValidationMatcher {

        @BeforeEach
        void beforeEach() {
            mockProducerAndConsumer(createReceiveMessage(""));
        }

        @Test
        void testSendWithBody() {
            ArgumentMatcher<Message> messageMatcher = message -> {
                HttpMessage httpMessage = (HttpMessage) message;
                try {
                    assertThat(httpMessage.getPayload())
                        .isEqualTo(
                            readToString(Resources.create(
                                    "org/citrusframework/openapi/generator/GeneratedApiTest/payloads/addPetMessage.json"),
                                StandardCharsets.UTF_8)
                        );
                } catch (IOException e) {
                    throw new CitrusRuntimeException("Unable to parse file!", e);
                }
                return true;
            };

            sendAndValidateMessage("sendWithBodyTest", messageMatcher);
        }

        @Test
        void testSendWithBodyLiteralWithVariable() {
            ArgumentMatcher<Message> messageMatcher = message -> {
                HttpMessage httpMessage = (HttpMessage) message;
                assertThat(((String) httpMessage.getPayload()).trim()).isEqualTo("{\"id\": 15}");
                return true;
            };
            sendAndValidateMessage("sendWithBodyLiteralWithVariableTest", messageMatcher);
        }

        @Test
        void testXCitrusApiHeaders() {
            ArgumentMatcher<Message> messageMatcher = message -> {
                HttpMessage httpMessage = (HttpMessage) message;
                assertThat(httpMessage.getHeader("x-citrus-api-name")).isEqualTo("petstore");
                assertThat(httpMessage.getHeader("x-citrus-app")).isEqualTo("PETS");
                assertThat(httpMessage.getHeader("x-citrus-api-version")).isEqualTo("1.0.0");
                return true;
            };

            sendAndValidateMessage("sendWithBodyLiteralTest", messageMatcher);
        }

        @Test
        void testSendWithExtraHeaders() {
            ArgumentMatcher<Message> messageMatcher = message -> {
                HttpMessage httpMessage = (HttpMessage) message;
                assertThat(httpMessage.getHeader("h1")).isEqualTo("v1");
                assertThat(httpMessage.getHeader("h2")).isEqualTo("v2");
                return true;
            };

            sendAndValidateMessage("sendWithExtraHeaderTest", messageMatcher);
        }

        @Test
        void testSendWithBodyLiteral() {
            ArgumentMatcher<Message> messageMatcher = message -> {
                HttpMessage httpMessage = (HttpMessage) message;
                assertThat(((String) httpMessage.getPayload()).trim()).isEqualTo("{\"id\": 13}");
                return true;
            };

            sendAndValidateMessage("sendWithBodyLiteralTest", messageMatcher);
        }

        private void sendAndValidateMessage(String testName,
            ArgumentMatcher<Message> messageMatcher) {
            GeneratedApiIT.this.sendAndValidateMessage(testName, messageMatcher,
                AddPetRequest.class);
        }

    }

    @Nested
    class WithMultipartMessage {

        @Test
        void testSendMultipartFile() {
            mockProducerAndConsumer(createReceiveMessage(""));

            ArgumentMatcher<Message> messageMatcher = message -> {
                assertThat(message.getPayload()).isInstanceOf(MultiValueMap.class);
                MultiValueMap<?, ?> multiValueMap = (MultiValueMap<?, ?>) message.getPayload();
                List<?> multipartFile = multiValueMap.get("multipartFile");
                try {
                    assertThat(((Resource) multipartFile.get(0)).getURL().toString())
                        .endsWith(
                            "test-classes/org/citrusframework/openapi/generator/GeneratedApiTest/payloads/getPetByIdControlMessage1.json");
                } catch (IOException e) {
                    throw new CitrusRuntimeException("Unable to parse file!", e);
                }

                return true;
            };

            sendAndValidateMessage("postFileTest", messageMatcher, PostFileRequest.class);
        }

        @Test
        void testSendMultipartWithFileAttribute() {
            Message payload = createReceiveMessage("{\"id\": 1}");
            mockProducerAndConsumer(payload);

            executeTest("multipartWithFileAttributesTest", testContext);
            ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);
            verify(producerMock).send(messageArgumentCaptor.capture(), eq(testContext));
            Object producedMessagePayload = messageArgumentCaptor.getValue().getPayload();
            assertThat(producedMessagePayload).isInstanceOf(MultiValueMap.class);

            Object templateValue = ((MultiValueMap<?, ?>) producedMessagePayload).get("template");
            assertThat(templateValue)
                .asInstanceOf(InstanceOfAssertFactories.LIST)
                .element(0)
                .hasFieldOrPropertyWithValue("path",
                    "org/citrusframework/openapi/generator/GeneratedApiTest/payloads/MultipartTemplate.xml");

            Object additionalDataValue = ((MultiValueMap<?, ?>) producedMessagePayload).get(
                "additionalData");
            assertThat(additionalDataValue)
                .asInstanceOf(InstanceOfAssertFactories.LIST)
                .element(0)
                .hasFieldOrPropertyWithValue("path",
                    "org/citrusframework/openapi/generator/GeneratedApiTest/payloads/AdditionalData.json");

            Object schemaValue = ((MultiValueMap<?, ?>) producedMessagePayload).get("_schema");
            assertThat(schemaValue)
                .asInstanceOf(InstanceOfAssertFactories.LIST)
                .element(0)
                .hasFieldOrPropertyWithValue("path",
                    "org/citrusframework/openapi/generator/GeneratedApiTest/payloads/Schema.json");
        }

        @Test
        void testSendMultipartWithPlainText() {
            mockProducerAndConsumer(createReceiveMessage("{\"id\": 1}"));
            executeTest("multipartWithPlainTextTest", testContext);
            ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);
            verify(producerMock).send(messageArgumentCaptor.capture(), eq(testContext));
            String producedMessagePayload = normalizeWhitespace(
                messageArgumentCaptor.getValue().getPayload().toString(),
                true,
                true
            );

            String expectedPayload =
                "{template=[ <template></template> ], additionalData=[ {\"data1\":\"value1\"} ], _schema=[ {\"schema\":\"mySchema\"} ]}";
            assertThat(producedMessagePayload).isEqualTo(expectedPayload);
        }

        @Test
        void testSendMultipartWithMultipleDatatypes() {
            Message receiveMessage = createReceiveMessage("{\"id\": 1}");
            mockProducerAndConsumer(receiveMessage);

            executeTest("multipartWithMultipleDatatypesTest", testContext);
            ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);
            verify(producerMock).send(messageArgumentCaptor.capture(), eq(testContext));
            String producedMessagePayload = normalizeWhitespace(
                messageArgumentCaptor.getValue().getPayload().toString(),
                true,
                true
            );

            String expectedPayload = "{stringData=[Test], booleanData=[true], integerData=[1]}";
            assertThat(producedMessagePayload).isEqualTo(expectedPayload);
        }
    }

    @Nested
    class WithDefaultReceiveMessage {

        private Message defaultRecieveMessage;

        @BeforeEach
        void beforeEach() throws IOException {
            defaultRecieveMessage = createReceiveMessage(
                readToString(Resources.create(
                        "org/citrusframework/openapi/generator/GeneratedApiTest/payloads/getPetByIdControlMessage1.json"),
                    StandardCharsets.UTF_8)
            );
            mockProducerAndConsumer(defaultRecieveMessage);
        }

        @Test
        void testJsonPathExtraction() {
            TestCase testCase = executeTest("jsonPathExtractionTest", testContext);
            TestAction testAction = testCase.getActions().get(0);
            assertThat(testAction).isInstanceOf(GetPetByIdRequest.class);

            assertThat(testContext.getVariable("name")).isEqualTo("Snoopy");
            assertThat(testContext.getVariable("id")).isEqualTo("12");
        }

        @Test
        void testCustomizer() {
            TestCase testCase = executeTest("getPetByIdRequestTest", testContext);

            TestAction testAction = testCase.getActions().get(0);
            assertThat(testAction).isInstanceOf(GetPetByIdRequest.class);

            ArgumentMatcher<Message> messageMatcher = message -> {
                HttpMessage httpMessage = (HttpMessage) message;
                assertThat(httpMessage.getHeader("x-citrus-api-version")).isEqualTo(
                    "1.0.0");

                return true;
            };
            verify(producerMock).send(ArgumentMatchers.argThat(messageMatcher), eq(testContext));
            verify(consumerMock).receive(testContext, 5000L);
        }

        @Test
        void testBasicAuthorization() {
            TestCase testCase = executeTest("getPetByIdRequestTest", testContext);

            TestAction testAction = testCase.getActions().get(0);
            assertThat(testAction).isInstanceOf(GetPetByIdRequest.class);

            ArgumentMatcher<Message> messageMatcher = message -> {
                HttpMessage httpMessage = (HttpMessage) message;
                assertThat(httpMessage.getHeader("Authorization")).isEqualTo(
                    "Basic YWRtaW46dG9wLXNlY3JldA==");
                return true;
            };
            verify(producerMock).send(ArgumentMatchers.argThat(messageMatcher), eq(testContext));
            verify(consumerMock).receive(testContext, 5000L);
        }

        @Test
        void testRequestPath() {
            TestCase testCase = executeTest("getPetByIdRequestTest", testContext);
            TestAction testAction = testCase.getActions().get(0);
            assertThat(testAction).isInstanceOf(GetPetByIdRequest.class);

            ArgumentMatcher<Message> messageMatcher = message -> {
                HttpMessage httpMessage = (HttpMessage) message;
                assertThat(httpMessage.getHeader("citrus_request_path")).isEqualTo("/pet/1234");
                return true;
            };
            verify(producerMock).send(ArgumentMatchers.argThat(messageMatcher), eq(testContext));
            verify(consumerMock).receive(testContext, 5000L);
        }

        @Test
        void testCookies() {
            TestCase testCase = executeTest("getPetByIdRequestTest", testContext);
            TestAction testAction = testCase.getActions().get(0);
            assertThat(testAction).isInstanceOf(GetPetByIdRequest.class);

            ArgumentMatcher<Message> messageMatcher = message -> {
                HttpMessage httpMessage = (HttpMessage) message;
                Cookie cookie1 = httpMessage.getCookies().get(0);
                Cookie cookie2 = httpMessage.getCookies().get(1);
                assertThat(cookie1.getName()).isEqualTo("c1");
                assertThat(cookie1.getValue()).isEqualTo("v1");
                assertThat(cookie2.getName()).isEqualTo("c2");
                assertThat(cookie2.getValue()).isEqualTo("v2");
                return true;
            };
            verify(producerMock).send(ArgumentMatchers.argThat(messageMatcher), eq(testContext));
            verify(consumerMock).receive(testContext, 5000L);
        }

        @Test
        void testJsonPathValidation() {
            TestCase testCase = executeTest("jsonPathValidationTest", testContext);
            assertTestActionType(testCase, GetPetByIdRequest.class);
        }

        @Test
        void scriptValidationFailureTest() {
            TestCase testCase = executeTest("scriptValidationTest", testContext);
            assertTestActionType(testCase, GetPetByIdRequest.class);
        }

        @Test
        void jsonSchemaValidationFailureTest() {
            assertThatThrownBy(() -> executeTest("jsonSchemaValidationFailureTest", testContext))
                .hasCauseExactlyInstanceOf(ValidationException.class);

            SimpleJsonSchema testSchema = (SimpleJsonSchema) applicationContext.getBean(
                "failingTestSchema");

            // Assert that schema validation was called
            verify(testSchema).getSchema();
            JsonSchema schema = testSchema.getSchema();
            verify(schema).validate(any());
        }

        @Test
        void jsonDeactivatedSchemaValidationTest() {
            SimpleJsonSchema testSchema = (SimpleJsonSchema) applicationContext.getBean(
                "testSchema");
            Mockito.clearInvocations(testSchema, testSchema.getSchema());

            TestCase testCase = executeTest("jsonDeactivatedSchemaValidationTest", testContext);

            assertTestActionType(testCase, GetPetByIdRequest.class);

            // Assert that schema validation was called
            Mockito.verifyNoInteractions(testSchema);
        }

        @Test
        void defaultOas3SchemaValidationTest() {
            SimpleJsonSchema testSchema = (SimpleJsonSchema) applicationContext.getBean("oas3");
            Mockito.clearInvocations(testSchema, testSchema.getSchema());

            TestCase testCase = executeTest("defaultOas3SchemaValidationTest", testContext);

            assertTestActionType(testCase, GetPetByIdRequest.class);

            // Assert that schema validation was called
            verify(testSchema).getSchema();
            JsonSchema schema = testSchema.getSchema();
            verify(schema).validate(any());
        }

        @Test
        void jsonSchemaValidationTest() {
            SimpleJsonSchema testSchema = (SimpleJsonSchema) applicationContext.getBean(
                "testSchema");
            Mockito.clearInvocations(testSchema, testSchema.getSchema());

            TestCase testCase = executeTest("jsonSchemaValidationTest", testContext);

            assertTestActionType(testCase, GetPetByIdRequest.class);

            // Assert that schema validation was called
            verify(testSchema).getSchema();
            JsonSchema schema = testSchema.getSchema();
            verify(schema).validate(any());
        }

        @Test
        void testJsonPathValidationFailure() {
            mockProducerAndConsumer(defaultRecieveMessage);

            assertThatThrownBy(() -> executeTest("jsonPathValidationFailureTest", testContext))
                .hasCauseExactlyInstanceOf(ValidationException.class);
        }

        private static Stream<Arguments> testValidationFailures() {
            return Stream.of(
                Arguments.of("failOnStatusTest",
                    "Values not equal for header element 'citrus_http_status_code', expected '201' but was '200'"),
                Arguments.of(
                    "failOnReasonPhraseTest",
                    "Values not equal for header element 'citrus_http_reason_phrase', expected 'Almost OK' but was 'OK'"
                ),
                Arguments.of(
                    "failOnVersionTest",
                    "Values not equal for header element 'citrus_http_version', expected 'HTTP/1.0' but was 'HTTP/1.1'"
                )
            );
        }

        @ParameterizedTest
        @MethodSource
        void testValidationFailures(String testName, String expectedErrorMessage) {
            assertThatThrownBy(() -> executeTest(testName, testContext))
                .hasCauseExactlyInstanceOf(ValidationException.class)
                .message()
                .startsWith(expectedErrorMessage);
        }
    }

//    @Test
//    void testCoverageLogger() throws IOException {
//        List<String> logMessages = new ArrayList<>();
//        Logger logger = LoggerFactory.getLogger(GetPetByIdRequest.class);
//        org.qos.logback.classic.Logger l = (org.qos.logback.classic.Logger) logger;
//        l.setLevel(Level.TRACE);
//        l.addAppender(
//            new AppenderBase<>() {
//                @Override
//                protected void append(ILoggingEvent eventObject) {}
//
//                @Override
//                public synchronized void doAppend(ILoggingEvent eventObject) {
//                    logMessages.add(eventObject.getMessage());
//                    super.doAppend(eventObject);
//                }
//            }
//        );
//
//
//
//        mockProducer(httpClient);
//
//        Message receiveMessage = createReceiveMessage(
//            FileUtils.readToString(Resources.create("org/citrusframework/openapi/generator/GeneratedApiTest/payloads/getPetByIdControlMessage1.json"), StandardCharsets.UTF_8)
//        );
//
//        mockConsumer(httpClient, testContext, receiveMessage);
//
//        executeTest("getPetByIdRequestTest", testContext);
//
//        assertThat(logMessages.get(0)).isEqualTo("getPetById;GET;\"{}\";\"\";\"\"");
//    }

    /**
     * Test the send message using the given matcher
     */
    private void sendAndValidateMessage(String testName, ArgumentMatcher<Message> messageMatcher,
        Class<?> apiClass) {

        TestCase testCase = executeTest(testName, testContext);
        assertTestActionType(testCase, apiClass);

        verify(producerMock).send(ArgumentMatchers.argThat(messageMatcher), eq(testContext));
    }

    /**
     * Assert that an action of type 'apiClass' is contained in the list of test actions
     */
    private void assertTestActionType(TestCase testCase, Class<?> apiClass) {
        TestAction testAction = testCase
            .getActions()
            .stream()
            .filter(action -> apiClass.isAssignableFrom(action.getClass()))
            .findAny()
            .orElse(null);
        assertThat(testAction).isNotNull();
    }

    private void mockProducerAndConsumer(Message receiveMessage) {
        when(httpClientMock.createProducer()).thenReturn(producerMock);
        when(httpClientMock.createConsumer()).thenReturn(consumerMock);
        when(consumerMock.receive(testContext, 5000L)).thenReturn(receiveMessage);
    }

    private TestCase executeTest(String testName, TestContext testContext) {
        assertThat(CitrusInstanceManager.get()).isPresent();

        Citrus citrus = CitrusInstanceManager.get().get();
        TestLoader loader = new SpringXmlTestLoader().citrusContext(citrus.getCitrusContext())
            .citrus(citrus)
            .context(testContext);
        loader.setTestName(testName);
        loader.setPackageName("org.citrusframework.openapi.generator.GeneratedApiTest");
        loader.load();
        return loader.getTestCase();
    }

    private Message createReceiveMessage(String payload) {
        Message receiveMessage = new DefaultMessage();
        receiveMessage.setPayload(payload);
        receiveMessage.getHeaders().put("citrus_http_reason_phrase", "OK");
        receiveMessage.getHeaders().put("citrus_http_version", "HTTP/1.1");
        receiveMessage.getHeaders().put("citrus_http_status_code", 200);
        return receiveMessage;
    }

    public static class Config {

        @Bean(name = {"applicationServiceClient", "multipartTestEndpoint",
            "soapSampleStoreEndpoint", "petStoreEndpoint"})
        public HttpClient applicationServiceClient() {
            HttpClient clientMock = mock();
            EndpointConfiguration endpointConfigurationMock = mock();
            when(clientMock.getEndpointConfiguration()).thenReturn(new HttpEndpointConfiguration());
            when(endpointConfigurationMock.getTimeout()).thenReturn(5000L);
            return clientMock;
        }

        @Bean
        public ApiActionBuilderCustomizerService customizer() {
            return new ApiActionBuilderCustomizerService() {
                @Override
                public <T extends SendMessageActionBuilder<?, ?, ?>> T build(
                    GeneratedApi generatedApi, TestAction action, TestContext context, T builder) {
                    builder.getMessageBuilderSupport()
                        .header("x-citrus-api-version", generatedApi.getApiVersion());
                    return builder;
                }
            };
        }

        @Bean({"oas3", "testSchema"})
        public SimpleJsonSchema testSchema() {
            JsonSchema schemaMock = mock();
            SimpleJsonSchema jsonSchemaMock = mock();

            when(jsonSchemaMock.getSchema()).thenReturn(schemaMock);

            Set<ValidationMessage> okReport = new HashSet<>();
            when(schemaMock.validate(any())).thenReturn(okReport);
            return jsonSchemaMock;
        }

        @Bean
        public SimpleJsonSchema failingTestSchema() {
            JsonSchema schemaMock = mock();
            SimpleJsonSchema jsonSchemaMock = mock();

            when(jsonSchemaMock.getSchema()).thenReturn(schemaMock);

            Set<ValidationMessage> nokReport = new HashSet<>();
            nokReport.add(new ValidationMessage.Builder().customMessage(
                "This is a simulated validation error message").build());
            when(schemaMock.validate(any())).thenReturn(nokReport);
            return jsonSchemaMock;
        }
    }
}
