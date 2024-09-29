package org.citrusframework.openapi.generator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.openapi.generator.util.MultipartConverter.multipartMessageToMap;
import static org.citrusframework.validation.json.JsonPathMessageValidationContext.Builder.jsonPath;
import static org.springframework.http.HttpStatus.OK;

import jakarta.servlet.http.Cookie;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.citrusframework.TestActor;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.annotations.CitrusTestSource;
import org.citrusframework.common.TestLoader;
import org.citrusframework.config.CitrusSpringConfig;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.MessageTimeoutException;
import org.citrusframework.exceptions.TestCaseFailedException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.http.actions.HttpClientRequestActionBuilder;
import org.citrusframework.http.actions.HttpClientResponseActionBuilder;
import org.citrusframework.http.actions.HttpServerRequestActionBuilder.HttpMessageBuilderSupport;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpClientBuilder;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.http.server.HttpServerBuilder;
import org.citrusframework.junit.jupiter.spring.CitrusSpringExtension;
import org.citrusframework.message.Message;
import org.citrusframework.openapi.generator.GeneratedRestApiIT.Config;
import org.citrusframework.openapi.generator.rest.extpetstore.model.PetIdentifier;
import org.citrusframework.openapi.generator.rest.extpetstore.request.ExtPetApi;
import org.citrusframework.openapi.generator.rest.extpetstore.spring.ExtPetStoreBeanConfiguration;
import org.citrusframework.openapi.generator.rest.petstore.request.PetApi;
import org.citrusframework.openapi.generator.rest.petstore.request.PetApi.GetPetByIdReceiveActionBuilder;
import org.citrusframework.openapi.generator.rest.petstore.spring.PetStoreBeanConfiguration;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.util.SocketUtils;
import org.citrusframework.validation.json.JsonPathVariableExtractor;
import org.citrusframework.validation.script.ScriptValidationContext;
import org.citrusframework.variable.GlobalVariables;
import org.citrusframework.variable.MessageHeaderVariableExtractor;
import org.citrusframework.ws.endpoint.builder.WebServiceEndpoints;
import org.citrusframework.ws.server.WebServiceServer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;

/**
 * This integration test class for the generated TestAPI aims to comprehensively test all aspects of
 * accessing the API using both Java and XML. In addition to serving as a test suite, it also acts
 * as a reference example.
 *
 * <p>Therefore, each test is designed to be self-contained and straightforward, allowing
 * anyone reviewing the code to easily grasp the purpose and context of the test without needing to
 * rely on shared setup or utility methods.
 */

@ExtendWith(CitrusSpringExtension.class)
@SpringBootTest(classes = {PetStoreBeanConfiguration.class, ExtPetStoreBeanConfiguration.class,
    CitrusSpringConfig.class, Config.class}, properties = {
    "extpetstore.basic.username=extUser",
    "extpetstore.basic.password=extPassword",
    "extpetstore.bearer.token=defaultBearerToken",
    "extpetstore.api-key-query=defaultTopSecretQueryApiKey",
    "extpetstore.api-key-header=defaultTopSecretHeaderApiKey",
    "extpetstore.api-key-cookie=defaultTopSecretCookieApiKey",
    "extpetstore.base64-encode-api-key=true"
}
)
class GeneratedRestApiIT {

    public static final List<Integer> PET_ID_LIST = List.of(1, 2);
    public static final List<String> PET_ID_AS_STRING_LIST = List.of("1", "2");
    public static final List<String> PET_ID_WITH_VARIABLE_EXPRESSIONS_LIST = List.of("${one}",
        "${two}");

    public static final PetIdentifier PET_IDENTIFIER = new PetIdentifier()._name("Louis")
        .alias("Alexander");
    public static final String PET_IDENTIFIER_AS_STRING = """
        {"alias":"Alexander","name":"Louis"}""";

    @Autowired
    private HttpServer httpServer;

    @Autowired
    private HttpServer otherHttpServer;

    @Autowired
    private PetApi petApi;

    @Autowired
    private ExtPetApi extPetApi;

    @Autowired
    private TestActor petStoreActor;

    @Autowired
    private HttpClient otherApplicationServiceClient;

    /**
     * Demonstrates usage of parameter serialization according to
     * <a href="https://swagger.io/docs/specification/serialization/">...</a>
     */
    @Nested
    class ParameterSerialization {

        @Nested
        class PathParameter {

            @Nested
            class SimpleStyle {

                @Nested
                class Array {

                    @Test
                    void java_single_value(@CitrusResource TestCaseRunner runner) {

                        runner.when(extPetApi.sendGetPetWithSimpleStyleArray(List.of(1))
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/simple/1")
                            .message());

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK)
                            .message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithSimpleStyleArray("200"));

                    }

                    @Test
                    void java_array_value(@CitrusResource TestCaseRunner runner) {

                        runner.when(extPetApi.sendGetPetWithSimpleStyleArray(PET_ID_LIST)
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/simple/1,2")
                            .message());

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK)
                            .message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithSimpleStyleArray("200"));

                    }

                    @Test
                    void java_array_value_non_type_safe(@CitrusResource TestCaseRunner runner) {

                        runner.when(extPetApi.sendGetPetWithSimpleStyleArray$(PET_ID_AS_STRING_LIST)
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/simple/1,2")
                            .message());

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK)
                            .message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithSimpleStyleArray("200"));

                    }

                    @Test
                    void java_array_value_non_type_safe_with_variables(
                        @CitrusResource TestCaseRunner runner) {
                        runner.variable("one", "1");
                        runner.variable("two", "2");

                        runner.when(
                            extPetApi.sendGetPetWithSimpleStyleArray$(
                                    PET_ID_WITH_VARIABLE_EXPRESSIONS_LIST)
                                .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/simple/1,2")
                            .message());

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK)
                            .message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithSimpleStyleArray("200"));

                    }

                    @Test
                    void java_object_value(@CitrusResource TestCaseRunner runner) {

                        // Note that we need to disable oas validation here, as validation is
                        // currently not supported with the chosen serialization approach.
                        runner.when(extPetApi.sendGetPetWithSimpleStyleObject(
                                PET_IDENTIFIER)
                            .schemaValidation(false)
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/simple/object/alias,Alexander,name,Louis")
                            .message());

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK)
                            .message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithSimpleStyleObject("200"));
                    }

                    @Test
                    void java_object_value_non_type_safe(@CitrusResource TestCaseRunner runner) {

                        // Note that we need to disable oas validation here, as validation is
                        // currently not supported with the chosen serialization approach.
                        runner.when(extPetApi.sendGetPetWithSimpleStyleObject$(PET_IDENTIFIER_AS_STRING)
                            .schemaValidation(false)
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/simple/object/alias,Alexander,name,Louis")
                            .message());

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK)
                            .message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithSimpleStyleObject("200"));
                    }

                }

                @Nested
                class ExplodedArray {

                    @Test
                    void java_single_value(@CitrusResource TestCaseRunner runner) {
                        runner.variable("petId", "citrus:randomNumber(10)");

                        runner.when(extPetApi.sendGetPetWithSimpleStyleArrayExploded(List.of(1))
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/simple/exploded/1")
                            .message());

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithSimpleStyleArrayExploded("200"));

                    }

                    @Test
                    void java_array_value(@CitrusResource TestCaseRunner runner) {
                        runner.variable("petId", "citrus:randomNumber(10)");

                        runner.when(extPetApi.sendGetPetWithSimpleStyleArrayExploded(PET_ID_LIST)
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/simple/exploded/1,2")
                            .message());

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithSimpleStyleArrayExploded("200"));

                    }

                    @Test
                    void java_array_value_non_type_safe(@CitrusResource TestCaseRunner runner) {

                        runner.when(
                            extPetApi.sendGetPetWithSimpleStyleArrayExploded$(PET_ID_AS_STRING_LIST)
                                .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/simple/exploded/1,2")
                            .message());

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK)
                            .message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithSimpleStyleArrayExploded("200"));

                    }

                    @Test
                    void java_array_value_non_type_safe_with_variables(
                        @CitrusResource TestCaseRunner runner) {
                        runner.variable("one", "1");
                        runner.variable("two", "2");

                        runner.when(
                            extPetApi.sendGetPetWithSimpleStyleArrayExploded$(
                                    PET_ID_WITH_VARIABLE_EXPRESSIONS_LIST)
                                .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/simple/exploded/1,2")
                            .message());

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK)
                            .message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithSimpleStyleArrayExploded("200"));

                    }

                    @Test
                    void java_object_value(@CitrusResource TestCaseRunner runner) {

                        // Note that we need to disable oas validation here, as validation is
                        // currently not supported with the chosen serialization approach.
                        runner.when(extPetApi.sendGetPetWithSimpleStyleObjectExploded(
                                PET_IDENTIFIER)
                            .schemaValidation(false)
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get(
                                "/api/v3/ext/pet/simple/exploded/object/alias=Alexander,name=Louis")
                            .message());

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK)
                            .message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithSimpleStyleObjectExploded("200"));
                    }

                    @Test
                    void java_object_value_non_type_safe(@CitrusResource TestCaseRunner runner) {

                        // Note that we need to disable oas validation here, as validation is
                        // currently not supported with the chosen serialization approach.
                        runner.when(extPetApi.sendGetPetWithSimpleStyleObjectExploded$(
                                PET_IDENTIFIER_AS_STRING)
                            .schemaValidation(false)
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get(
                                "/api/v3/ext/pet/simple/exploded/object/alias=Alexander,name=Louis")
                            .message());

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK)
                            .message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithSimpleStyleObjectExploded("200"));
                    }

                }
            }

            @Nested
            class LabelStyle {

                @Nested
                class Array {

                    /**
                     * Non exploded representation is currently not supported by validator.
                     * Therefore, we get a validation exception. The other tests use disabled
                     * request validation to overcome this issue.
                     */
                    @Test
                    void throws_request_validation_exception(
                        @CitrusResource TestCaseRunner runner) {

                        HttpClientRequestActionBuilder builder = extPetApi.sendGetPetWithLabelStyleArray(
                                PET_ID_LIST)
                            .fork(false);

                        assertThatThrownBy(() -> runner.when(builder))
                            .isInstanceOf(TestCaseFailedException.class)
                            .hasCauseInstanceOf(ValidationException.class)
                            .hasMessageContaining(
                                "ERROR - Instance type (string) does not match any allowed primitive type (allowed: [\"integer\"]): []");
                    }

                    @Test
                    void java_single_value(@CitrusResource TestCaseRunner runner) {

                        // Note that we need to disable oas validation here, as validation is
                        // currently not supported with the chosen serialization approach.
                        runner.when(extPetApi.sendGetPetWithLabelStyleArray(List.of(1))
                            .schemaValidation(true)
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/label/.1")
                            .message());

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK)
                            .message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithLabelStyleArray("200"));

                    }

                    @Test
                    void java_array_value(@CitrusResource TestCaseRunner runner) {

                        // Note that we need to disable oas validation here, as validation is
                        // currently not supported with the chosen serialization approach.
                        runner.when(extPetApi.sendGetPetWithLabelStyleArray(PET_ID_LIST)
                            .schemaValidation(false)
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/label/.1,2")
                            .message());

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK)
                            .message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithLabelStyleArray("200"));

                    }

                    @Test
                    void java_array_value_non_type_safe(@CitrusResource TestCaseRunner runner) {

                        // Note that we need to disable oas validation here, as validation is
                        // currently not supported with the chosen serialization approach.
                        runner.when(extPetApi.sendGetPetWithLabelStyleArray$(PET_ID_AS_STRING_LIST)
                            .schemaValidation(false)
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/label/.1,2")
                            .message());

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK)
                            .message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithLabelStyleArray("200"));

                    }

                    @Test
                    void java_array_value_non_type_safe_with_variables(
                        @CitrusResource TestCaseRunner runner) {
                        runner.variable("one", "1");
                        runner.variable("two", "2");

                        // Note that we need to disable oas validation here, as validation is
                        // currently not supported with the chosen serialization approach.
                        runner.when(
                            extPetApi.sendGetPetWithLabelStyleArray$(
                                    PET_ID_WITH_VARIABLE_EXPRESSIONS_LIST)
                                .schemaValidation(false)
                                .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/label/.1,2")
                            .message());

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK)
                            .message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithLabelStyleArray("200"));

                    }

                    @Test
                    void java_object_value(@CitrusResource TestCaseRunner runner) {

                        // Note that we need to disable oas validation here, as validation is
                        // currently not supported with the chosen serialization approach.
                        runner.when(extPetApi.sendGetPetWithLabelStyleObject(
                                PET_IDENTIFIER)
                            .schemaValidation(false)
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/label/object/.alias,Alexander,name,Louis")
                            .message());

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK)
                            .message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithLabelStyleObject("200"));
                    }

                    @Test
                    void java_object_value_non_type_safe(@CitrusResource TestCaseRunner runner) {

                        // Note that we need to disable oas validation here, as validation is
                        // currently not supported with the chosen serialization approach.
                        runner.when(extPetApi.sendGetPetWithLabelStyleObject$("""
                                {"name":"Louis","alias":"Alexander"}
                                """)
                            .schemaValidation(false)
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/label/object/.alias,Alexander,name,Louis")
                            .message());

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK)
                            .message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithLabelStyleObject("200"));
                    }
                }

                @Nested
                class ExplodedArray {

                    @Test
                    void java_single_value(@CitrusResource TestCaseRunner runner) {
                        runner.variable("petId", "citrus:randomNumber(10)");

                        runner.when(extPetApi.sendGetPetWithLabelStyleArrayExploded(List.of(1))
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/label/exploded/.1")
                            .message());

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithLabelStyleArrayExploded("200"));

                    }

                    @Test
                    void java_array_value(@CitrusResource TestCaseRunner runner) {
                        runner.variable("petId", "citrus:randomNumber(10)");

                        runner.when(extPetApi.sendGetPetWithLabelStyleArrayExploded(PET_ID_LIST)
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/label/exploded/.1.2")
                            .message());

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithLabelStyleArrayExploded("200"));

                    }

                    @Test
                    void java_array_value_non_type_safe(@CitrusResource TestCaseRunner runner) {

                        runner.when(
                            extPetApi.sendGetPetWithLabelStyleArrayExploded$(PET_ID_AS_STRING_LIST)
                                .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/label/exploded/.1.2")
                            .message());

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK)
                            .message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithLabelStyleArrayExploded("200"));

                    }

                    @Test
                    void java_array_value_non_type_safe_with_variables(
                        @CitrusResource TestCaseRunner runner) {
                        runner.variable("one", "1");
                        runner.variable("two", "2");

                        runner.when(
                            extPetApi.sendGetPetWithLabelStyleArrayExploded$(
                                    PET_ID_WITH_VARIABLE_EXPRESSIONS_LIST)
                                .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/label/exploded/.1.2")
                            .message());

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK)
                            .message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithLabelStyleArrayExploded("200"));

                    }

                    @Test
                    void java_object_value(@CitrusResource TestCaseRunner runner) {

                        // Note that we need to disable oas validation here, as validation is
                        // currently not supported with the chosen serialization approach.
                        runner.when(extPetApi.sendGetPetWithLabelStyleObjectExploded$("""
                                {"name":"Louis","alias":"Alexander"}
                                """)
                            .schemaValidation(false)
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get(
                                "/api/v3/ext/pet/label/exploded/object/.alias=Alexander.name=Louis")
                            .message());

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK)
                            .message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithLabelStyleObjectExploded("200"));
                    }

                    @Test
                    void java_object_value_non_type_safe(@CitrusResource TestCaseRunner runner) {

                        // Note that we need to disable oas validation here, as validation is
                        // currently not supported with the chosen serialization approach.
                        runner.when(extPetApi.sendGetPetWithLabelStyleObjectExploded(
                                PET_IDENTIFIER)
                            .schemaValidation(false)
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get(
                                "/api/v3/ext/pet/label/exploded/object/.alias=Alexander.name=Louis")
                            .message());

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK)
                            .message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithLabelStyleObjectExploded("200"));
                    }
                }

            }

            @Nested
            class MatrixStyle {

                @Nested
                class Array {

                    @Test
                    void java_single_value(@CitrusResource TestCaseRunner runner) {

                        runner.when(extPetApi.sendGetPetWithMatrixStyleArray(List.of(1))
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/matrix/;petId=1")
                            .message());

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK)
                            .message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithMatrixStyleArray("200"));

                    }

                    @Test
                    void java_array_value(@CitrusResource TestCaseRunner runner) {

                        runner.when(extPetApi.sendGetPetWithMatrixStyleArray(PET_ID_LIST)
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/matrix/;petId=1,2")
                            .message());

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK)
                            .message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithMatrixStyleArray("200"));

                    }

                    @Test
                    void java_array_value_non_type_safe(@CitrusResource TestCaseRunner runner) {

                        runner.when(extPetApi.sendGetPetWithMatrixStyleArray$(PET_ID_AS_STRING_LIST)
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/matrix/;petId=1,2")
                            .message());

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK)
                            .message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithMatrixStyleArray("200"));

                    }

                    @Test
                    void java_array_value_non_type_safe_with_variables(
                        @CitrusResource TestCaseRunner runner) {
                        runner.variable("one", "1");
                        runner.variable("two", "2");

                        runner.when(
                            extPetApi.sendGetPetWithMatrixStyleArray$(
                                    PET_ID_WITH_VARIABLE_EXPRESSIONS_LIST)
                                .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/matrix/;petId=1,2")
                            .message());

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK)
                            .message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithMatrixStyleArray("200"));

                    }

                    @Test
                    void java_object_value(@CitrusResource TestCaseRunner runner) {

                        // Note that we need to disable oas validation here, as validation is
                        // currently not supported with the chosen serialization approach.
                        runner.when(extPetApi.sendGetPetWithMatrixStyleObject(
                                PET_IDENTIFIER)
                            .schemaValidation(false)
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/matrix/object/;petId=alias,Alexander,name,Louis")
                            .message());

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK)
                            .message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithMatrixStyleObject("200"));
                    }

                    @Test
                    void java_object_value_non_type_safe(@CitrusResource TestCaseRunner runner) {

                        // Note that we need to disable oas validation here, as validation is
                        // currently not supported with the chosen serialization approach.
                        runner.when(extPetApi.sendGetPetWithMatrixStyleObject$(
                                PET_IDENTIFIER_AS_STRING)
                            .schemaValidation(false)
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/matrix/object/;petId=alias,Alexander,name,Louis")
                            .message());

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK)
                            .message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithMatrixStyleObject("200"));
                    }

                }

                @Nested
                class ExplodedArray {

                    @Test
                    void java_single_value(@CitrusResource TestCaseRunner runner) {

                        runner.when(extPetApi.sendGetPetWithMatrixStyleArrayExploded(List.of(1))
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/matrix/exploded/;petId=1")
                            .message());

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithMatrixStyleArrayExploded("200"));

                    }

                    @Test
                    void java_array_value(@CitrusResource TestCaseRunner runner) {

                        runner.when(extPetApi.sendGetPetWithMatrixStyleArrayExploded(PET_ID_LIST)
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/matrix/exploded/;petId=1;petId=2")
                            .message());

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithMatrixStyleArrayExploded("200"));

                    }

                    @Test
                    void java_array_value_non_type_safe(@CitrusResource TestCaseRunner runner) {

                        runner.when(
                            extPetApi.sendGetPetWithMatrixStyleArrayExploded$(PET_ID_AS_STRING_LIST)
                                .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/matrix/exploded/;petId=1;petId=2")
                            .message());

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK)
                            .message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithMatrixStyleArrayExploded("200"));

                    }

                    @Test
                    void java_array_value_non_type_safe_with_variables(
                        @CitrusResource TestCaseRunner runner) {

                        runner.variable("one", "1");
                        runner.variable("two", "2");

                        runner.when(
                            extPetApi.sendGetPetWithMatrixStyleArrayExploded$(
                                    PET_ID_WITH_VARIABLE_EXPRESSIONS_LIST)
                                .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/matrix/exploded/;petId=1;petId=2")
                            .message());

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK)
                            .message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithMatrixStyleArrayExploded("200"));

                    }

                    @Test
                    void java_object_value(@CitrusResource TestCaseRunner runner) {

                        // Note that we need to disable oas validation here, as validation is
                        // currently not supported with the chosen serialization approach.
                        runner.when(extPetApi.sendGetPetWithMatrixStyleObjectExploded(
                                PET_IDENTIFIER)
                            .schemaValidation(false)
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get(
                                "/api/v3/ext/pet/matrix/exploded/object/;alias=Alexander;name=Louis")
                            .message());

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK)
                            .message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithMatrixStyleObject("200"));
                    }

                    @Test
                    void java_object_value_non_type_safe(@CitrusResource TestCaseRunner runner) {

                        // Note that we need to disable oas validation here, as validation is
                        // currently not supported with the chosen serialization approach.
                        runner.when(extPetApi.sendGetPetWithMatrixStyleObjectExploded$(
                                PET_IDENTIFIER_AS_STRING)
                            .schemaValidation(false)
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get(
                                "/api/v3/ext/pet/matrix/exploded/object/;alias=Alexander;name=Louis")
                            .message());

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK)
                            .message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithMatrixStyleObject("200"));
                    }

                }
            }
        }

        @Nested
        class HeaderParameter {

            @Nested
            class SimpleStyle {

                @Nested
                class Array {


                    @Test
                    void java_single_value(@CitrusResource TestCaseRunner runner) {

                        runner.when(extPetApi.sendGetPetWithSimpleStyleHeader(List.of(1))
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/header/simple")
                            .message().header("petId", "1"));

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithSimpleStyleHeader("200"));

                    }

                    @Test
                    void java_array_value(@CitrusResource TestCaseRunner runner) {

                        runner.when(extPetApi.sendGetPetWithSimpleStyleHeader(PET_ID_LIST)
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/header/simple")
                            .message().header("petId", "1,2"));

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithSimpleStyleHeader("200"));

                    }

                    @Test
                    void java_array_value_non_type_safe(@CitrusResource TestCaseRunner runner) {

                        runner.when(extPetApi.sendGetPetWithSimpleStyleHeader$(
                                PET_ID_AS_STRING_LIST)
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/header/simple")
                            .message().header("petId", "1,2"));

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithSimpleStyleHeader("200"));

                    }

                    @Test
                    void java_array_value_non_type_safe_with_variables(
                        @CitrusResource TestCaseRunner runner) {

                        runner.variable("one", "1");
                        runner.variable("two", "2");

                        runner.when(extPetApi.sendGetPetWithSimpleStyleHeader$(
                                PET_ID_WITH_VARIABLE_EXPRESSIONS_LIST)
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/header/simple")
                            .message().header("petId", "1,2"));

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithSimpleStyleHeader("200"));

                    }

                    @Test
                    void java_object_value(@CitrusResource TestCaseRunner runner) {

                        // Note that we need to disable oas validation here, as validation is
                        // currently not supported with the chosen serialization approach.
                        runner.when(extPetApi
                            .sendGetPetWithSimpleStyleObjectHeader(
                                PET_IDENTIFIER)
                            .schemaValidation(false)
                            .fork(true));
                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/header/simple/object")
                            .message().header("petId", "alias,Alexander,name,Louis"));

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithSimpleStyleExplodedHeader("200"));
                    }

                    @Test
                    void java_object_value_non_type_safe(@CitrusResource TestCaseRunner runner) {

                        // Note that we need to disable oas validation here, as validation is
                        // currently not supported with the chosen serialization approach.
                        runner.when(extPetApi
                            .sendGetPetWithSimpleStyleObjectHeader$(
                                PET_IDENTIFIER_AS_STRING)
                            .schemaValidation(false)
                            .fork(true));
                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/header/simple/object")
                            .message().header("petId", "alias,Alexander,name,Louis"));

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithSimpleStyleExplodedHeader("200"));
                    }

                }

                @Nested
                class ArrayExploded {

                    @Test
                    void java_single_value(@CitrusResource TestCaseRunner runner) {

                        runner.when(extPetApi.sendGetPetWithSimpleStyleExplodedHeader(List.of(1))
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/header/simple/exploded")
                            .message().header("petId", "1"));

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithSimpleStyleExplodedHeader("200"));

                    }

                    @Test
                    void java_array_value(@CitrusResource TestCaseRunner runner) {

                        runner.when(extPetApi.sendGetPetWithSimpleStyleExplodedHeader(PET_ID_LIST)
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/header/simple/exploded")
                            .message().header("petId", "1,2"));

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithSimpleStyleExplodedHeader("200"));

                    }

                    @Test
                    void java_array_value_non_type_safe(@CitrusResource TestCaseRunner runner) {

                        runner.when(extPetApi.sendGetPetWithSimpleStyleExplodedHeader$(
                                PET_ID_AS_STRING_LIST)
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/header/simple/exploded")
                            .message().header("petId", "1,2"));

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithSimpleStyleExplodedHeader("200"));

                    }

                    @Test
                    void java_array_value_non_type_safe_with_variables(
                        @CitrusResource TestCaseRunner runner) {

                        runner.variable("one", "1");
                        runner.variable("two", "2");

                        runner.when(extPetApi.sendGetPetWithSimpleStyleExplodedHeader$(
                                PET_ID_WITH_VARIABLE_EXPRESSIONS_LIST)
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/header/simple/exploded")
                            .message().header("petId", "1,2"));

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithSimpleStyleExplodedHeader("200"));

                    }

                    @Test
                    void java_object_value(@CitrusResource TestCaseRunner runner) {

                        // Note that we need to disable oas validation here, as validation is
                        // currently not supported with the chosen serialization approach.
                        runner.when(extPetApi
                            .sendGetPetWithSimpleStyleExplodedObjectHeader(
                                PET_IDENTIFIER)
                            .schemaValidation(false)
                            .fork(true));
                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/header/simple/exploded/object")
                            .message().header("petId", "alias=Alexander,name=Louis"));

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(
                            extPetApi.receiveGetPetWithSimpleStyleExplodedObjectHeader("200"));
                    }

                    @Test
                    void java_object_value_non_type_safe(@CitrusResource TestCaseRunner runner) {

                        // Note that we need to disable oas validation here, as validation is
                        // currently not supported with the chosen serialization approach.
                        runner.when(extPetApi
                            .sendGetPetWithSimpleStyleExplodedObjectHeader$(
                                PET_IDENTIFIER_AS_STRING)
                            .schemaValidation(false)
                            .fork(true));
                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/header/simple/exploded/object")
                            .message().header("petId", "alias=Alexander,name=Louis"));

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(
                            extPetApi.receiveGetPetWithSimpleStyleExplodedObjectHeader("200"));
                    }
                }

            }

        }

        @Nested
        class QueryParameter {

            @Nested
            class FormStyle {

                @Nested
                class Array {

                    @Test
                    void java_single_value(@CitrusResource TestCaseRunner runner) {

                        runner.when(extPetApi.sendGetPetWithFormStyleQuery(List.of(1))
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/query/form")
                            .message().queryParam("petId", "1"));

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithFormStyleQuery("200"));

                    }

                    @Test
                    void java_arrya_value(@CitrusResource TestCaseRunner runner) {

                        runner.when(extPetApi.sendGetPetWithFormStyleQuery(PET_ID_LIST)
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/query/form")
                            .message().queryParam("petId", "1,2"));

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithFormStyleQuery("200"));
                    }

                    @Test
                    void java_arrya_value_non_type_safe(@CitrusResource TestCaseRunner runner) {

                        runner.when(extPetApi.sendGetPetWithFormStyleQuery$(PET_ID_AS_STRING_LIST)
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/query/form")
                            .message().queryParam("petId", "1,2"));

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithFormStyleQuery("200"));
                    }

                    @Test
                    void java_arrya_value_non_type_safe_with_variables(
                        @CitrusResource TestCaseRunner runner) {

                        runner.variable("one", "1");
                        runner.variable("two", "2");

                        runner.when(extPetApi.sendGetPetWithFormStyleQuery$(
                                PET_ID_WITH_VARIABLE_EXPRESSIONS_LIST)
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/query/form")
                            .message().queryParam("petId", "1,2"));

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithFormStyleQuery("200"));
                    }

                    @Test
                    void java_object_value(@CitrusResource TestCaseRunner runner) {

                        // Note that we need to disable oas validation here, as validation is
                        // currently not supported with the chosen serialization approach.
                        runner.when(extPetApi
                            .sendGetPetWithFormStyleObjectQuery(
                                PET_IDENTIFIER)
                            .schemaValidation(false)
                            .fork(true));
                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/query/form/object")
                            .message().queryParam("petId", "alias,Alexander,name,Louis"));

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithFormStyleObjectQuery("200"));
                    }

                    @Test
                    void java_object_value_non_type_safe(@CitrusResource TestCaseRunner runner) {

                        // Note that we need to disable oas validation here, as validation is
                        // currently not supported with the chosen serialization approach.
                        runner.when(extPetApi
                            .sendGetPetWithFormStyleObjectQuery$(
                                PET_IDENTIFIER_AS_STRING)
                            .schemaValidation(false)
                            .fork(true));
                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/query/form/object")
                            .message().queryParam("petId", "alias,Alexander,name,Louis"));

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithFormStyleObjectQuery("200"));
                    }
                }

                @Nested
                class ArrayExploded {

                    @Test
                    void java_single_value(@CitrusResource TestCaseRunner runner) {

                        runner.when(extPetApi.sendGetPetWithFormStyleExplodedQuery(List.of(1))
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/query/form/exploded")
                            .message().queryParam("petId", "1"));

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithFormStyleQuery("200"));

                    }

                    @Test
                    void java_array_value(@CitrusResource TestCaseRunner runner) {

                        runner.when(extPetApi.sendGetPetWithFormStyleExplodedQuery(PET_ID_LIST)
                            .fork(true));

                        // Note that citrus currently fails to validate a query parameter array. Thus, we
                        // assert against the query_params header.
                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/query/form/exploded")
                            .message()
                            .queryParam("petId", "1")
                            .queryParam("petId", "2"));

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithFormStyleQuery("200"));
                    }

                    @Test
                    void java_array_value_non_type_safe(@CitrusResource TestCaseRunner runner) {

                        runner.when(
                            extPetApi.sendGetPetWithFormStyleExplodedQuery$(PET_ID_AS_STRING_LIST)
                                .fork(true));

                        // Note that citrus currently fails to validate a query parameter array. Thus, we
                        // assert against the query_params header.
                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/query/form/exploded")
                            .message()
                            .queryParam("petId", "1")
                            .queryParam("petId", "2"));

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithFormStyleQuery("200"));
                    }

                    @Test
                    void java_array_value_non_type_safe_with_variables(
                        @CitrusResource TestCaseRunner runner) {

                        runner.variable("one", "1");
                        runner.variable("two", "2");

                        runner.when(extPetApi.sendGetPetWithFormStyleExplodedQuery$(
                                PET_ID_WITH_VARIABLE_EXPRESSIONS_LIST)
                            .fork(true));

                        // Note that citrus currently fails to validate a query parameter array. Thus, we
                        // assert against the query_params header.
                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/query/form/exploded")
                            .message()
                            .queryParam("petId", "1")
                            .queryParam("petId", "2"));

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithFormStyleQuery("200"));
                    }

                    @Test
                    void java_object_value(@CitrusResource TestCaseRunner runner) {

                        runner.when(extPetApi
                            .sendGetPetWithFormStyleExplodedObjectQuery(
                                PET_IDENTIFIER)
                            .fork(true));
                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/query/form/exploded/object")
                            .message()
                            .queryParam("alias", "Alexander")
                            .queryParam("name", "Louis"));

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithFormStyleObjectQuery("200"));
                    }

                    @Test
                    void java_object_value_non_type_safe(@CitrusResource TestCaseRunner runner) {

                        runner.when(extPetApi
                            .sendGetPetWithFormStyleExplodedObjectQuery$(
                                PET_IDENTIFIER_AS_STRING)
                            .fork(true));
                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/query/form/exploded/object")
                            .message()
                            .queryParam("alias", "Alexander")
                            .queryParam("name", "Louis"));

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithFormStyleObjectQuery("200"));
                    }
                }
            }

            @Nested
            class DeepObjectStyleExploded {

                @Nested
                class ArrayExploded {

                    @Test
                    void java_object_value(@CitrusResource TestCaseRunner runner) {

                        runner.when(extPetApi
                            .sendGetPetWithDeepObjectTypeQuery(
                                PET_IDENTIFIER)
                            .fork(true));
                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/query/deep/object")
                            .message()
                            .queryParam("petId[alias]", "Alexander")
                            .queryParam("petId[name]", "Louis"));

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithDeepObjectTypeQuery("200"));
                    }

                    @Test
                    void java_object_value_non_type_safe(@CitrusResource TestCaseRunner runner) {

                        runner.when(extPetApi
                            .sendGetPetWithDeepObjectTypeQuery$(
                                PET_IDENTIFIER_AS_STRING)
                            .fork(true));
                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/query/deep/object")
                            .message()
                            .queryParam("petId[alias]", "Alexander")
                            .queryParam("petId[name]", "Louis"));

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithDeepObjectTypeQuery("200"));
                    }
                }
            }
        }

        @Nested
        class CookieParameter {

            @Nested
            class FormStyle {

                @Nested
                class Array {

                    @Test
                    void java_single_value(@CitrusResource TestCaseRunner runner) {

                        runner.when(extPetApi.sendGetPetWithFormStyleCookie(List.of(1))
                            .fork(true));

                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/cookie/form")
                            .message().cookie(new Cookie("petId", "1")));

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithFormStyleCookie("200"));

                    }

                    @Test
                    void java_array_value(@CitrusResource TestCaseRunner runner) {

                        runner.when(extPetApi.sendGetPetWithFormStyleCookie(PET_ID_LIST)
                            .fork(true));

                        // Cookies may not contain "," which is used to separate array values.
                        // Therefore, cookies are URL encoded and reach the server accordingly.
                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/cookie/form")
                            .message().cookie(new Cookie("petId", "1%2C2")));

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithFormStyleCookie("200"));

                    }

                    @Test
                    void java_array_value_non_type_safe(@CitrusResource TestCaseRunner runner) {

                        runner.when(extPetApi.sendGetPetWithFormStyleCookie$(PET_ID_AS_STRING_LIST)
                            .fork(true));

                        // Cookies may not contain "," which is used to separate array values.
                        // Therefore, cookies are URL encoded and reach the server accordingly.
                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/cookie/form")
                            .message().cookie(new Cookie("petId", "1%2C2")));

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithFormStyleCookie("200"));

                    }

                    @Test
                    void java_array_value_non_type_safe_with_variables(
                        @CitrusResource TestCaseRunner runner) {

                        runner.variable("one", "1");
                        runner.variable("two", "2");

                        runner.when(extPetApi.sendGetPetWithFormStyleCookie$(
                                PET_ID_WITH_VARIABLE_EXPRESSIONS_LIST)
                            .fork(true));

                        // Cookies may not contain "," which is used to separate array values.
                        // Therefore, cookies are URL encoded and reach the server accordingly.
                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/cookie/form")
                            .message().cookie(new Cookie("petId", "1%2C2")));

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithFormStyleCookie("200"));

                    }

                    @Test
                    void java_object_value(@CitrusResource TestCaseRunner runner) {

                        // Note that we need to disable oas validation here, as validation is
                        // currently not supported with the chosen serialization approach.
                        runner.when(extPetApi.sendGetPetWithFormObjectStyleCookie(
                                PET_IDENTIFIER)
                            .schemaValidation(false)
                            .fork(true));

                        // Cookies may not contain "," which is used to separate array values.
                        // Therefore, cookies are URL encoded and reach the server accordingly.
                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/cookie/form/object")
                            .message()
                            .cookie(new Cookie("petId", "alias%2CAlexander%2Cname%2CLouis")));

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithFormStyleCookie("200"));

                    }

                    @Test
                    void java_object_value_none_type(@CitrusResource TestCaseRunner runner) {

                        // Note that we need to disable oas validation here, as validation is
                        // currently not supported with the chosen serialization approach.
                        runner.when(extPetApi.sendGetPetWithFormObjectStyleCookie$(
                                PET_IDENTIFIER_AS_STRING)
                            .schemaValidation(false)
                            .fork(true));

                        // Cookies may not contain "," which is used to separate array values.
                        // Therefore, cookies are URL encoded and reach the server accordingly.
                        runner.then(http().server(httpServer)
                            .receive()
                            .get("/api/v3/ext/pet/cookie/form/object")
                            .message()
                            .cookie(new Cookie("petId", "alias%2CAlexander%2Cname%2CLouis")));

                        runner.then(http().server(httpServer)
                            .send()
                            .response(OK).message()
                            .contentType("application/json")
                            .body("[]"));

                        runner.when(extPetApi.receiveGetPetWithFormStyleCookie("200"));

                    }
                }
            }
        }

        /**
         * Demonstrates testing of array data in query parameters.
         */
        @Nested
        class Combined {

            @Test
            @CitrusTestSource(type = TestLoader.SPRING, packageName = "org.citrusframework.openapi.generator.GeneratedApiTest", name = "withArrayQueryDataTest")
            void xml() {
            }

            @Test
            void java(@CitrusResource TestCaseRunner runner) {
                runner.variable("petId", "1234");
                runner.variable("nick1", "Wind");
                runner.variable("nick2", "Storm");
                runner.variable("tag2", "tag2Value");

                runner.when(extPetApi
                    .sendUpdatePetWithArrayQueryData$("${petId}", "Thunder", "sold",
                        List.of("tag1", "${tag2}"),
                        List.of("${nick1}", "${nick2}"), "header1")
                    .fork(true));

                runner.then(http().server(httpServer)
                    .receive()
                    .put("/api/v3/ext/pet/1234")
                    .message()
                    .validate(ScriptValidationContext.Builder.groovy().script("""
                    assert receivedMessage.getHeader("sampleStringHeader") == header1
                    org.assertj.core.api.Assertions.assertThat(((org.citrusframework.http.message.HttpMessage)receivedMessage).getQueryParams()).containsExactlyInAnyOrderEntriesOf(
                                                         java.util.Map.of(
                                                             "tags", java.util.List.of("tag1", "tag2Value"),
                                                             "name", java.util.List.of("Thunder"),
                                                             "nicknames", java.util.List.of("Wind", "Storm"),
                                                             "status", java.util.List.of("sold")
                    """))
                    .validate((message, context) -> {
                        assertThat(message.getHeader("sampleStringHeader")).isEqualTo("header1");
                        assertThat(
                            ((HttpMessage) message).getQueryParams()).containsExactlyInAnyOrderEntriesOf(
                            Map.of(
                                "tags", List.of("tag1", "tag2Value"),
                                "name", List.of("Thunder"),
                                "nicknames", List.of("Wind", "Storm"),
                                "status", List.of("sold")
                            )
                        );
                    }));

                runner.then(http().server(httpServer)
                    .send()
                    .response(OK));

                runner.when(extPetApi
                    .receiveUpdatePetWithArrayQueryData(OK)
                    .message());

            }
        }
    }

    /**
     * Demonstrates the usage of form data.
     */
    @Nested
    class FormData {

        @Test
        @CitrusTestSource(type = TestLoader.SPRING, packageName = "org.citrusframework.openapi.generator.GeneratedApiTest", name = "withFormDataTest")
        void xml() {
        }

        @Test
        void updatePetWithForm_java(@CitrusResource TestCaseRunner runner) {
            runner.variable("petId", "citrus:randomNumber(10)");

            runner.when(petApi.sendUpdatePetWithForm$("${petId}")
                ._name("Tom")
                .status("sold")
                .fork(true));

            runner.then(http().server(httpServer)
                .receive()
                .post("/api/v3/pet/${petId}")
                .message()
                .queryParam("name", "Tom")
                .queryParam("status", "sold"));

            runner.then(http().server(httpServer)
                .send()
                .response(OK));

            runner.when(petApi.receiveUpdatePetWithForm("200"));

        }
    }

    /**
     * Demonstrates the usage of validation is disablement in API requests and
     * responses.
     */
    @Nested
    class DisabledValidation {

        /**
         * Test scenarios where response validation is disabled for the API requests.
         */
        @Nested
        class ResponseValidationDisabled {

            @Test
            @CitrusTestSource(type = TestLoader.SPRING, packageName = "org.citrusframework.openapi.generator.GeneratedApiTest", name = "withResponseValidationDisabledTest")
            void xml() {
            }

            @Test
            void java(
                @CitrusResource TestCaseRunner runner) {

                runner.variable("petId", "1234");

                runner.when(petApi
                    .sendGetPetById$("${petId}")
                    .fork(true));

                runner.then(http().server(httpServer)
                    .receive()
                    .get("/api/v3/pet/${petId}")
                    .message()
                    .accept("@contains('application/json')@"));

                runner.then(http().server(httpServer)
                    .send()
                    .response(OK)
                    .message()
                    .body(Resources.create(
                        "classpath:org/citrusframework/openapi/generator/GeneratedApiTest/payloads/invalidGetPetById_response.json"))
                    .contentType("application/json"));

                runner.when(petApi
                    .receiveGetPetById(OK)
                    .schemaValidation(false));

            }
        }
    }

    /**
     * Contains test cases for scenarios where validation failures are expected.
     */
    @Nested
    class ValidationFailures {

        /**
         * Tests where validation fails due to an incorrect reason phrase in the API response.
         */
        @Nested
        class FailOnReasonPhrase {

            @Test
            @CitrusTestSource(type = TestLoader.SPRING, packageName = "org.citrusframework.openapi.generator.GeneratedApiTest", name = "withFailOnReasonPhraseTest")
            void xml() {
            }

            @Test
            void java(@CitrusResource TestCaseRunner runner) {

                runner.variable("petId", "1234");

                runner.when(petApi
                    .sendGetPetById$("${petId}")
                    .fork(true));

                runner.then(http().server(httpServer)
                    .receive()
                    .get("/api/v3/pet/${petId}")
                    .message()
                    .accept("@contains('application/json')@"));

                runner.then(http().server(httpServer)
                    .send()
                    .response(OK)
                    .message()
                    .body(Resources.create(
                        "classpath:org/citrusframework/openapi/generator/GeneratedApiTest/payloads/getPetById_response.json"))
                    .contentType("application/json"));

                HttpClientResponseActionBuilder.HttpMessageBuilderSupport builder = petApi
                    .receiveGetPetById(OK).message().reasonPhrase("Almost OK");
                assertThatThrownBy(() -> runner.when(builder)).isInstanceOf(
                        TestCaseFailedException.class)
                    .hasMessageContaining(
                        "Values not equal for header element 'citrus_http_reason_phrase', expected 'Almost OK' but was 'OK'")
                    .hasCauseInstanceOf(ValidationException.class);
            }
        }

        /**
         * Tests where validation fails due to an incorrect HTTP status in the API response.
         */
        @Nested
        class FailOnStatus {

            @Test
            @CitrusTestSource(type = TestLoader.SPRING, packageName = "org.citrusframework.openapi.generator.GeneratedApiTest", name = "withFailOnStatusTest")
            void xml() {
            }

            @Test
            void java(@CitrusResource TestCaseRunner runner) {

                runner.variable("petId", "1234");

                runner.when(petApi
                    .sendGetPetById$("${petId}")
                    .fork(true));

                runner.then(http().server(httpServer)
                    .receive()
                    .get("/api/v3/pet/${petId}")
                    .message()
                    .accept("@contains('application/json')@"));

                runner.then(http().server(httpServer)
                    .send()
                    .response(OK)
                    .message()
                    .body(Resources.create(
                        "classpath:org/citrusframework/openapi/generator/GeneratedApiTest/payloads/getPetById_response.json"))
                    .contentType("application/json"));

                GetPetByIdReceiveActionBuilder getPetByIdResponseActionBuilder = petApi
                    .receiveGetPetById("201");
                assertThatThrownBy(() -> runner.when(getPetByIdResponseActionBuilder)).isInstanceOf(
                        TestCaseFailedException.class)
                    .hasMessageContaining(
                        "Values not equal for header element 'citrus_http_reason_phrase', expected 'CREATED' but was 'OK'")
                    .hasCauseInstanceOf(ValidationException.class);
            }
        }

        /**
         * Tests where validation fails due to an incorrect HTTP version in the API response.
         */
        @Nested
        class FailOnVersion {

            @Test
            @CitrusTestSource(type = TestLoader.SPRING, packageName = "org.citrusframework.openapi.generator.GeneratedApiTest", name = "withFailOnVersionTest")
            void xml() {
            }

            @Test
            void java(@CitrusResource TestCaseRunner runner) {

                runner.variable("petId", "1234");

                runner.when(petApi
                    .sendGetPetById$("${petId}")
                    .fork(true));

                runner.then(http().server(httpServer)
                    .receive()
                    .get("/api/v3/pet/${petId}")
                    .message()
                    .accept("@contains('application/json')@"));

                runner.then(http().server(httpServer)
                    .send()
                    .response(OK)
                    .message()
                    .body(Resources.create(
                        "classpath:org/citrusframework/openapi/generator/GeneratedApiTest/payloads/getPetById_response.json"))
                    .contentType("application/json"));

                HttpClientResponseActionBuilder.HttpMessageBuilderSupport builder = petApi
                    .receiveGetPetById(OK).message().version("HTTP/1.0");
                assertThatThrownBy(() -> runner.when(builder)).isInstanceOf(
                        TestCaseFailedException.class)
                    .hasMessageContaining(
                        "Values not equal for header element 'citrus_http_version', expected 'HTTP/1.0' but was 'HTTP/1.1'")
                    .hasCauseInstanceOf(ValidationException.class);
            }
        }

        /**
         * Tests where validation fails due to an invalid response body.
         */
        @Nested
        class FailOnInvalidResponse {

            @Test
            @CitrusTestSource(type = TestLoader.SPRING, packageName = "org.citrusframework.openapi.generator.GeneratedApiTest", name = "withFailOnInvalidResponseTest")
            void xml() {
            }

            @Test
            void java(@CitrusResource TestCaseRunner runner) {

                runner.variable("petId", "1234");

                runner.when(petApi
                    .sendGetPetById$("${petId}")
                    .fork(true));

                runner.then(http().server(httpServer)
                    .receive()
                    .get("/api/v3/pet/${petId}")
                    .message()
                    .accept("@contains('application/json')@"));

                runner.then(http().server(httpServer)
                    .send()
                    .response(OK)
                    .message()
                    .body(Resources.create(
                        "classpath:org/citrusframework/openapi/generator/GeneratedApiTest/payloads/invalidGetPetById_response.json"))
                    .contentType("application/json"));

                GetPetByIdReceiveActionBuilder getPetByIdResponseActionBuilder = petApi.receiveGetPetById(
                    OK);
                assertThatThrownBy(() -> runner.when(getPetByIdResponseActionBuilder)).isInstanceOf(
                        TestCaseFailedException.class)
                    .hasMessageContaining("Object has missing required properties ([\"name\"]): []")
                    .hasCauseInstanceOf(ValidationException.class);

            }
        }

        /**
         * Tests where validation fails due to an incorrect body resource during validation.
         */
        @Nested
        class FailOnBodyResourceValidation {

            @Test
            @CitrusTestSource(type = TestLoader.SPRING, packageName = "org.citrusframework.openapi.generator.GeneratedApiTest", name = "withFailOnBodyResourceValidationTest")
            void xml() {
            }

            @Test
            void java(@CitrusResource TestCaseRunner runner) {

                runner.variable("petId", "1234");

                runner.when(petApi
                    .sendGetPetById$("${petId}")
                    .fork(true));

                runner.then(http().server(httpServer)
                    .receive()
                    .get("/api/v3/pet/${petId}")
                    .message()
                    .accept("@contains('application/json')@"));

                runner.then(http().server(httpServer)
                    .send()
                    .response(OK)
                    .message()
                    .body(Resources.create(
                        "classpath:org/citrusframework/openapi/generator/GeneratedApiTest/payloads/getPetById_response.json"))
                    .contentType("application/json"));

                HttpClientResponseActionBuilder.HttpMessageBuilderSupport builder = petApi
                    .receiveGetPetById(OK).message().body(Resources.create(
                        "classpath:org/citrusframework/openapi/generator/GeneratedApiTest/payloads/thisAintNoPed.json"));
                assertThatThrownBy(() -> runner.when(builder)).isInstanceOf(
                        TestCaseFailedException.class)
                    .hasMessageContaining(
                        "Number of entries is not equal in element: '$', expected '[description]' but was '[photoUrls, name, id, category, tags, status]'")
                    .hasCauseInstanceOf(ValidationException.class);

            }
        }

        /**
         * Tests where validation fails due to incorrect data in the response body.
         */
        @Nested
        class FailOnBodyDataValidation {

            @Test
            @CitrusTestSource(type = TestLoader.SPRING, packageName = "org.citrusframework.openapi.generator.GeneratedApiTest", name = "withFailOnBodyDataValidationTest")
            void xml() {
            }

            @Test
            void java(@CitrusResource TestCaseRunner runner) {

                runner.variable("petId", "1234");

                runner.when(petApi
                    .sendGetPetById$("${petId}")
                    .fork(true));

                runner.then(http().server(httpServer)
                    .receive()
                    .get("/api/v3/pet/${petId}")
                    .message()
                    .accept("@contains('application/json')@"));

                runner.then(http().server(httpServer)
                    .send()
                    .response(OK)
                    .message()
                    .body(Resources.create(
                        "classpath:org/citrusframework/openapi/generator/GeneratedApiTest/payloads/getPetById_response.json"))
                    .contentType("application/json"));

                HttpClientResponseActionBuilder.HttpMessageBuilderSupport builder = petApi
                    .receiveGetPetById(OK).message().body("""
                        {"description": "no pet"}""");
                assertThatThrownBy(() -> runner.when(builder)).isInstanceOf(
                        TestCaseFailedException.class)
                    .hasMessageContaining(
                        "Number of entries is not equal in element: '$', expected '[description]' but was '[photoUrls, name, id, category, tags, status]'")
                    .hasCauseInstanceOf(ValidationException.class);

            }
        }

        /**
         * Tests where validation fails due to invalid JSON path expressions in the response body.
         */
        @Nested
        class FailOnJsonPathInvalid {

            @Test
            @CitrusTestSource(type = TestLoader.SPRING, packageName = "org.citrusframework.openapi.generator.GeneratedApiTest", name = "withFailOnJsonPathInvalidTest")
            void xml() {
            }

            @Test
            void java(@CitrusResource TestCaseRunner runner) {

                runner.variable("petId", "1234");

                runner.when(petApi
                    .sendGetPetById$("${petId}")
                    .fork(true));

                runner.then(http().server(httpServer)
                    .receive()
                    .get("/api/v3/pet/${petId}")
                    .message()
                    .accept("@contains('application/json')@"));

                runner.then(http().server(httpServer)
                    .send()
                    .response(OK)
                    .message()
                    .body(Resources.create(
                        "classpath:org/citrusframework/openapi/generator/GeneratedApiTest/payloads/getPetById_response.json"))
                    .contentType("application/json"));

                HttpClientResponseActionBuilder.HttpMessageBuilderSupport builder = petApi
                    .receiveGetPetById(OK).message()
                    .validate(jsonPath().expression("$.name", "unknown"));
                assertThatThrownBy(() -> runner.when(builder))
                    .isInstanceOf(TestCaseFailedException.class)
                    .hasMessageContaining(
                        "Values not equal for element '$.name', expected 'unknown' but was")
                    .hasCauseInstanceOf(ValidationException.class);

            }
        }
    }

    /**
     * Demonstrates the usage of a TestActor to control execution of test actions.
     */
    @Nested
    class Actor {

        @Test
        @CitrusTestSource(type = TestLoader.SPRING, packageName = "org.citrusframework.openapi.generator.GeneratedApiTest", name = "withActorTest")
        void xml() {
        }

        @Test
        void java(@CitrusResource TestCaseRunner runner) {
            runner.variable("petId", "1234");

            runner.when(petApi
                .sendGetPetById$("${petId}")
                .actor(petStoreActor)
                .fork(true));

            HttpMessageBuilderSupport builder = http().server(httpServer)
                .receive()
                .get("/api/v3/pet/${petId}")
                .message()
                .accept("@contains('application/json')@");

            assertThatThrownBy(() -> runner.$(builder))
                .isInstanceOf(TestCaseFailedException.class)
                .hasCauseInstanceOf(MessageTimeoutException.class)
                .hasMessageContaining(
                    "Action timeout after 5000 milliseconds. Failed to receive message on endpoint: 'httpServer.inbound'");
        }
    }

    /**
     * Demonstrates the usage of a specific URI for sending HTTP requests in tests.
     */
    @Nested
    class SpecificUri {

        @Test
        @CitrusTestSource(type = TestLoader.SPRING, packageName = "org.citrusframework.openapi.generator.GeneratedApiTest", name = "withSpecificUriTest")
        void xml() {
        }

        @Test
        void java(@CitrusResource TestCaseRunner runner) {
            runner.variable("petId", "1234");

            runner.when(petApi
                .sendGetPetById$("${petId}")
                .uri("http://localhost:${petstoreApplicationPort}")
                .fork(true));

            runner.then(http().server(httpServer)
                .receive()
                .get("/api/v3/pet/${petId}")
                .message()
                .accept("@contains('application/json')@"));

            runner.then(http().server(httpServer)
                .send()
                .response(OK)
                .message()
                .body(Resources.create(
                    "classpath:org/citrusframework/openapi/generator/GeneratedApiTest/payloads/getPetById_response.json"))
                .contentType("application/json"));

            runner.then(petApi.receiveGetPetById(OK)
                .message()
                .validate(jsonPath().expression("$.name", "@matches('hasso|cutie|fluffy')@"))
            );
        }
    }

    /**
     * Demonstrates the usage of a specific endpoint for sending HTTP requests in tests.
     */
    @Nested
    class SpecificEndpoint {

        @Test
        @CitrusTestSource(type = TestLoader.SPRING, packageName = "org.citrusframework.openapi.generator.GeneratedApiTest", name = "withSpecificEndpointTest")
        void xml() {
        }

        @Test
        void java(@CitrusResource TestCaseRunner runner) {
            runner.variable("petId", "1234");

            runner.when(petApi
                .sendGetPetById$("${petId}").endpoint(otherApplicationServiceClient)
                .fork(true));

            runner.then(http().server(otherHttpServer)
                .receive()
                .get("/api/v3/pet/${petId}")
                .message()
                .accept("@contains('application/json')@"));

            runner.then(http().server(otherHttpServer)
                .send()
                .response(OK)
                .message()
                .body(Resources.create(
                    "classpath:org/citrusframework/openapi/generator/GeneratedApiTest/payloads/getPetById_response.json"))
                .contentType("application/json"));

            runner.then(petApi.receiveGetPetById(OK).endpoint(otherApplicationServiceClient)
                .message()
                .validate(jsonPath().expression("$.name", "@matches('hasso|cutie|fluffy')@"))
            );
        }
    }

    /**
     * Demonstrates the usage of a nested <receive> element within an XML configuration for
     * testing.
     */
    @Nested
    class NestedReceiveInXml {

        @Test
        @CitrusTestSource(type = TestLoader.SPRING, packageName = "org.citrusframework.openapi.generator.GeneratedApiTest", name = "withNestedReceiveInXmlTest")
        void xml() {
        }

    }

    /**
     * Demonstrates the usage of different authentication mechanisms for testing.
     */
    @Nested
    class Security {

        @Nested
        class BasicAuthentication {

            /**
             * Demonstrates basic authentication using global credentials from properties.
             */
            @Nested
            class BasicAuthenticationFromProperties {

                @Test
                @CitrusTestSource(type = TestLoader.SPRING, packageName = "org.citrusframework.openapi.generator.GeneratedApiTest", name = "withBasicAuthenticationFromPropertiesTest")
                void xml() {
                }

                @Test
                void java(@CitrusResource TestCaseRunner runner) {
                    runner.variable("petId", "1234");

                    runner.when(extPetApi
                        .sendGetPetByIdWithBasicAuthentication$("${petId}", "true")
                        .fork(true));

                    runner.then(http().server(httpServer)
                        .receive()
                        .get("/api/v3/ext/secure-basic/pet/${petId}")
                        .message()
                        .header("Authorization", "Basic ZXh0VXNlcjpleHRQYXNzd29yZA=="));

                    runner.then(http().server(httpServer)
                        .send()
                        .response(OK)
                        .message()
                        .body(Resources.create(
                            "classpath:org/citrusframework/openapi/generator/GeneratedApiTest/payloads/getPetById_response.json"))
                        .contentType("application/json"));

                    runner.when(extPetApi
                        .receiveGetPetByIdWithBasicAuthentication(OK)
                        .message());

                }

            }

            /**
             * Demonstrates specific basic authentication with custom credentials.
             */
            @Nested
            class WithBasicAuthenticationOverridingProperties {

                @Test
                @CitrusTestSource(type = TestLoader.SPRING, packageName = "org.citrusframework.openapi.generator.GeneratedApiTest", name = "withBasicAuthenticationOverridingPropertiesTest")
                void xml() {
                }

                @Test
                void java(
                    @CitrusResource TestCaseRunner runner) {
                    runner.variable("petId", "1234");

                    runner.when(extPetApi
                        .sendGetPetByIdWithBasicAuthentication$("${petId}", "true")
                        .basicAuthUsername("admin")
                        .basicAuthPassword("topSecret")
                        .fork(true));

                    runner.then(http().server(httpServer)
                        .receive()
                        .get("/api/v3/ext/secure-basic/pet/${petId}")
                        .message().header("Authorization", "Basic YWRtaW46dG9wU2VjcmV0"));

                    runner.then(http().server(httpServer)
                        .send()
                        .response(OK)
                        .message()
                        .body(Resources.create(
                            "classpath:org/citrusframework/openapi/generator/GeneratedApiTest/payloads/getPetById_response.json"))
                        .contentType("application/json"));

                    runner.when(extPetApi
                        .receiveGetPetByIdWithBasicAuthentication(OK)
                        .message());

                }

            }
        }

        @Nested
        class BearerAuthentication {

            /**
             * Demonstrates bearer authentication using a global token from properties.
             */
            @Nested
            class WithBasicBearerAuthenticationFromProperties {

                @Test
                @CitrusTestSource(type = TestLoader.SPRING, packageName = "org.citrusframework.openapi.generator.GeneratedApiTest", name = "withBasicBearerAuthenticationFromPropertiesTest")
                void xml() {
                }

                @Test
                void java(
                    @CitrusResource TestCaseRunner runner) {
                    runner.variable("petId", "1234");

                    runner.when(extPetApi
                        .sendGetPetByIdWithBearerAuthentication$("${petId}", "true")
                        .fork(true));

                    runner.then(http().server(httpServer)
                        .receive()
                        .get("/api/v3/ext/secure-bearer/pet/${petId}")
                        .message()
                        .header("Authorization", "Bearer ZGVmYXVsdEJlYXJlclRva2Vu"));

                    runner.then(http().server(httpServer)
                        .send()
                        .response(OK)
                        .message()
                        .body(Resources.create(
                            "classpath:org/citrusframework/openapi/generator/GeneratedApiTest/payloads/getPetById_response.json"))
                        .contentType("application/json"));

                    runner.when(extPetApi
                        .receiveGetPetByIdWithBearerAuthentication(OK)
                        .message());

                }
            }

            /**
             * Demonstrates bearer authentication using specific token.
             */
            @Nested
            class WithBasicBearerAuthenticationOverridingProperties {

                @Test
                @CitrusTestSource(type = TestLoader.SPRING, packageName = "org.citrusframework.openapi.generator.GeneratedApiTest", name = "withBasicBearerAuthenticationOverridingPropertiesTest")
                void xml() {
                }

                @Test
                void java(@CitrusResource TestCaseRunner runner) {
                    runner.variable("petId", "1234");

                    runner.when(extPetApi
                        .sendGetPetByIdWithBearerAuthentication$("${petId}", "true")
                        .basicAuthBearer("bearerToken")
                        .fork(true));

                    runner.then(http().server(httpServer)
                        .receive()
                        .get("/api/v3/ext/secure-bearer/pet/${petId}")
                        .message().header("Authorization", "Bearer YmVhcmVyVG9rZW4="));

                    runner.then(http().server(httpServer)
                        .send()
                        .response(OK)
                        .message()
                        .body(Resources.create(
                            "classpath:org/citrusframework/openapi/generator/GeneratedApiTest/payloads/getPetById_response.json"))
                        .contentType("application/json"));

                    runner.when(extPetApi
                        .receiveGetPetByIdWithBearerAuthentication(OK)
                        .message());

                }

            }

        }


        @Nested
        class ApiKeyAuthentication {


            /**
             * Demonstrates API key authentication using default values from properties.
             */
            @Nested
            class ApiKeyAuthenticationFromProperties {

                @Test
                @CitrusTestSource(type = TestLoader.SPRING, packageName = "org.citrusframework.openapi.generator.GeneratedApiTest", name = "withApiKeysFromPropertiesTest")
                void xml() {
                }

                @Test
                void java(@CitrusResource TestCaseRunner runner) {

                    runner.variable("petId", "1234");

                    runner.when(extPetApi
                        .sendGetPetByIdWithApiKeyAuthentication$("${petId}", "false")
                        .fork(true));

                    runner.then(http().server(httpServer)
                        .receive()
                        .get("/api/v3/ext/secure-api-key/pet/${petId}")
                        .message()
                        .header("api_key_header",
                            "citrus:encodeBase64('defaultTopSecretHeaderApiKey')")
                        .cookie(new Cookie("api_key_cookie",
                            "citrus:encodeBase64('defaultTopSecretCookieApiKey')"))
                        .queryParam("api_key_query",
                            "citrus:encodeBase64('defaultTopSecretQueryApiKey')")
                        .accept("@contains('application/json')@"));

                    runner.then(http().server(httpServer)
                        .send()
                        .response(OK)
                        .message()
                        .body(Resources.create(
                            "classpath:org/citrusframework/openapi/generator/GeneratedApiTest/payloads/invalidGetPetById_response.json"))
                        .contentType("application/json"));

                    runner.when(extPetApi
                        .receiveGetPetByIdWithApiKeyAuthentication(OK)
                        .schemaValidation(false));
                }

            }

            /**
             * Demonstrates API key authentication with custom values overriding properties.
             */
            @Nested
            class ApiKeyAuthenticationOverridingProperties {

                @Test
                @CitrusTestSource(type = TestLoader.SPRING, packageName = "org.citrusframework.openapi.generator.GeneratedApiTest", name = "withApiKeysOverridingPropertiesTest")
                void xml() {
                }

                @Test
                void java(
                    @CitrusResource TestCaseRunner runner) {

                    runner.variable("petId", "1234");
                    runner.variable("apiKeyHeader", "TopSecretHeader");
                    runner.variable("apiKeyCookie", "TopSecretCookie");
                    runner.variable("apiKeyQuery", "TopSecretQuery");

                    runner.when(extPetApi
                        .sendGetPetByIdWithApiKeyAuthentication$("${petId}", "false")
                        .apiKeyHeader("${apiKeyHeader}")
                        .apiKeyCookie("${apiKeyCookie}")
                        .apiKeyQuery("${apiKeyQuery}")
                        .fork(true));

                    runner.then(http().server(httpServer)
                        .receive()
                        .get("/api/v3/ext/secure-api-key/pet/${petId}")
                        .message()
                        .header("api_key_header", "citrus:encodeBase64('TopSecretHeader')")
                        .cookie(
                            new Cookie("api_key_cookie", "citrus:encodeBase64('TopSecretCookie')"))
                        .queryParam("api_key_query", "citrus:encodeBase64('TopSecretQuery')")
                        .accept("@contains('application/json')@"));

                    runner.then(http().server(httpServer)
                        .send()
                        .response(OK)
                        .message()
                        .body(Resources.create(
                            "classpath:org/citrusframework/openapi/generator/GeneratedApiTest/payloads/invalidGetPetById_response.json"))
                        .contentType("application/json"));

                    runner.when(extPetApi
                        .receiveGetPetByIdWithApiKeyAuthentication(OK)
                        .schemaValidation(false));
                }
            }
        }
    }

    /**
     * Demonstrates testing of multipart requests.
     */
    @Nested
    class Multipart {

        @Test
        @CitrusTestSource(type = TestLoader.SPRING, packageName = "org.citrusframework.openapi.generator.GeneratedApiTest", name = "withMultiPartTest")
        void xml() {
        }

        @Test
        void java(@CitrusResource TestCaseRunner runner) throws IOException {

            byte[] templateData = FileUtils.copyToByteArray(
                Resources.create(
                    "classpath:org/citrusframework/openapi/generator/GeneratedApiTest/payloads/vaccinationTemplate.bin"));
            String additionalData = FileUtils.readToString(Resources.create(
                "classpath:org/citrusframework/openapi/generator/GeneratedApiTest/payloads/vaccinationAdditionalData.json"));

            runner.when(extPetApi.sendGenerateVaccinationReport$(
                    "classpath:org/citrusframework/openapi/generator/GeneratedApiTest/payloads/vaccinationTemplate.bin",
                    "1")
                .additionalData(additionalData)
                .optIntVal(100)
                .optBoolVal(true)
                .optStringVal("a")
                .optNumberVal(BigDecimal.valueOf(1L))
                .optDateVal(LocalDate.of(2024, 12, 1))
                .fork(true));

            runner.then(http().server(httpServer)
                .receive()
                .post("/api/v3/ext/pet/vaccination/status-report")
                .message()
                .validate((message, context) ->
                    Assertions.assertThat(multipartMessageToMap((HttpMessage) message))
                        .containsExactlyInAnyOrderEntriesOf(Map.of(
                            "additionalData", additionalData,
                            "reqIntVal", "1",
                            "template", templateData,
                            "optIntVal", "100",
                            "optBoolVal", "true",
                            "optDateVal", "[2024,12,1]",
                            "optNumberVal", "1",
                            "optStringVal", "a"))
                )
            );

            runner.then(http().server(httpServer)
                .send()
                .response(OK)
                .message().contentType("application/pdf")
                .body(Resources.create(
                    "classpath:org/citrusframework/openapi/generator/GeneratedApiTest/payloads/vaccinationReport.pdf")));

            runner.then(extPetApi.receiveGenerateVaccinationReport(OK));
        }
    }

    /**
     * Demonstrates testing of requests using additional, non-API query parameters, headers, and
     * cookies.
     */
    @Nested
    class NonApiQueryParameters {

        @Test
        @CitrusTestSource(type = TestLoader.SPRING, packageName = "org.citrusframework.openapi.generator.GeneratedApiTest", name = "withNonApiQueryParamTest")
        void xml() {
        }

        @Test
        void java(@CitrusResource TestCaseRunner runner) {
            runner.variable("petId", "citrus:randomNumber(10)");

            runner.when(petApi.sendUpdatePet()
                .message().body(Resources.create(
                    "classpath:org/citrusframework/openapi/generator/GeneratedApiTest/payloads/getPetById_response.json"))
                .queryParam("nonApiQueryParam", "nonApiQueryParamValue")
                .header("nonApiHeader", "nonApiHeaderValue")
                .cookie(new Cookie("nonApiCookie", "nonApiCookieValue"))
                .fork(true));

            runner.then(http().server(httpServer)
                .receive()
                .put("/api/v3/pet")
                .message()
                .queryParam("nonApiQueryParam", "nonApiQueryParamValue")
                .header("nonApiHeader", "nonApiHeaderValue")
                .cookie(new Cookie("nonApiCookie", "nonApiCookieValue"))
                .body(Resources.create(
                    "classpath:org/citrusframework/openapi/generator/GeneratedApiTest/payloads/getPetById_response_validation.json")));

            runner.then(http().server(httpServer)
                .send()
                .response(OK));

            runner.when(petApi.receiveUpdatePetWithForm("200"));

        }

    }

    /**
     * Demonstrates testing of form URL-encoded data in requests.
     */
    @Nested
    class FormUrlEncoded {

        @Test
        @CitrusTestSource(type = TestLoader.SPRING, packageName = "org.citrusframework.openapi.generator.GeneratedApiTest", name = "withFormUrlEncodedTest")
        void xml() {
        }

        @Test
        void java(@CitrusResource TestCaseRunner runner) {
            runner.variable("petId", "1234");
            runner.variable("nick1", "Wind");
            runner.variable("nick2", "Storm");
            runner.variable("tag2", "tag2Value");

            runner.when(extPetApi
                .sendUpdatePetWithFormUrlEncoded$("${petId}", "Thunder", "sold", "5",
                    List.of("tag1", "${tag2}"))
                .nicknames(
                    "${nick1}",
                    "${nick2}",
                    URLEncoder.encode("Wei{:/?#[]@!$&'()*+,;=%\"<>^`{|}~ }rd",
                        StandardCharsets.UTF_8)
                )
                .owners("2")
                .fork(true));

            runner.then(http().server(httpServer)
                .receive()
                .put("/api/v3/ext/pet/form/1234")
                .message()
                .contentType("application/x-www-form-urlencoded")
                .validate((Message message, TestContext context) ->
                    assertThat(message.getPayload(String.class))
                        .contains("name=Thunder")
                        .contains("status=sold")
                        .contains("nicknames=Wind")
                        .contains("nicknames=Storm")
                        .contains("tags=tag2")
                        .contains("tags=tag2Value")
                        .contains("age=5")
                        .contains(
                            "nicknames=Wei%257B%253A%252F%253F%2523%255B%255D%2540%2521%2524%2526%2527%2528%2529*%252B%252C%253B%253D%2525%2522%253C%253E%255E%2560%257B%257C%257D%257E%2B%257Drd")
                ));

            runner.then(http().server(httpServer)
                .send()
                .response(OK));

            runner.when(extPetApi
                .receiveUpdatePetWithFormUrlEncoded(OK)
                .message());

        }
    }

    /**
     * Demonstrates testing of requests using the type-safe Java DSL.
     */
    @Nested
    class TypeSafeJavaDsl {

        @Test
        void java(@CitrusResource TestCaseRunner runner) {
            runner.variable("petId", "1234");
            runner.variable("nick1", "Wind");
            runner.variable("nick2", "Storm");
            runner.variable("tag2", "tag2Value");

            runner.when(extPetApi
                .sendUpdatePetWithFormUrlEncoded(1234L, "Thunder", "sold", 5,
                    List.of("tag1", "${tag2}"))
                .nicknames(
                    "${nick1}",
                    "${nick2}",
                    URLEncoder.encode("Wei{:/?#[]@!$&'()*+,;=%\"<>^`{|}~ }rd",
                        StandardCharsets.UTF_8)
                )
                .owners("2")
                .fork(true));
            runner.then(http().server(httpServer)
                .receive()
                .put("/api/v3/ext/pet/form/1234")
                .message()
                .contentType("application/x-www-form-urlencoded")
                .validate((Message message, TestContext context) ->
                    assertThat(message.getPayload(String.class))
                        .contains("name=Thunder")
                        .contains("status=sold")
                        .contains("nicknames=Wind")
                        .contains("nicknames=Storm")
                        .contains("tags=tag2")
                        .contains("tags=tag2Value")
                        .contains("age=5")
                        .contains(
                            "nicknames=Wei%257B%253A%252F%253F%2523%255B%255D%2540%2521%2524%2526%2527%2528%2529*%252B%252C%253B%253D%2525%2522%253C%253E%255E%2560%257B%257C%257D%257E%2B%257Drd")
                ));

            runner.then(http().server(httpServer)
                .send()
                .response(OK));

            runner.when(extPetApi
                .receiveUpdatePetWithFormUrlEncoded(OK)
                .message());

        }
    }

    /**
     * Demonstrates testing of plain text bodies in requests.
     */
    @Nested
    class BodyAsPlainText {

        @Test
        @CitrusTestSource(type = TestLoader.SPRING, packageName = "org.citrusframework.openapi.generator.GeneratedApiTest", name = "withBodyAsPlainTextTest")
        void xml() {
        }

        @Test
        void java(@CitrusResource TestCaseRunner runner) {
            runner.variable("petId", "citrus:randomNumber(10)");

            runner.when(petApi.sendUpdatePet()
                .message().body("""
                    {
                      "id": ${petId},
                      "name": "citrus:randomEnumValue('hasso','cutie','fluffy')",
                      "category": {
                        "id": ${petId},
                        "name": "citrus:randomEnumValue('dog', 'cat', 'fish')"
                      },
                      "photoUrls": [
                        "http://localhost:8080/photos/${petId}"
                      ],
                      "tags": [
                        {
                          "id": ${petId},
                          "name": "generated"
                        }
                      ],
                      "status": "citrus:randomEnumValue('available', 'pending', 'sold')"
                    }
                        """)
                .fork(true));

            runner.then(http().server(httpServer)
                .receive()
                .put("/api/v3/pet")
                .message().body(Resources.create(
                    "classpath:org/citrusframework/openapi/generator/GeneratedApiTest/payloads/getPetById_response_validation.json")));

            runner.then(http().server(httpServer)
                .send()
                .response(OK));

            runner.when(petApi.receiveUpdatePetWithForm("200"));

        }
    }

    /**
     * Demonstrates testing of request bodies read from resources.
     */
    @Nested
    class BodyFromResource {

        @Test
        @CitrusTestSource(type = TestLoader.SPRING, packageName = "org.citrusframework.openapi.generator.GeneratedApiTest", name = "withBodyFromResourceTest")
        void xml() {
        }

        @Test
        void java(@CitrusResource TestCaseRunner runner) {
            runner.variable("petId", "citrus:randomNumber(10)");

            runner.when(petApi.sendUpdatePet()
                .message().body(Resources.create(
                    "classpath:org/citrusframework/openapi/generator/GeneratedApiTest/payloads/getPetById_response.json"))
                .fork(true));

            runner.then(http().server(httpServer)
                .receive()
                .put("/api/v3/pet")
                .message().body(Resources.create(
                    "classpath:org/citrusframework/openapi/generator/GeneratedApiTest/payloads/getPetById_response_validation.json")));

            runner.then(http().server(httpServer)
                .send()
                .response(OK));

            runner.when(petApi.receiveUpdatePetWithForm("200"));

        }
    }

    /**
     * Demonstrates testing of control response bodies from plain text.
     */
    @Nested
    class ReceiveBodyFromPlainText {

        @Test
        @CitrusTestSource(type = TestLoader.SPRING, packageName = "org.citrusframework.openapi.generator.GeneratedApiTest", name = "withReceiveBodyFromPlainTextTest")
        void xml() {
        }

        @Test
        void java(@CitrusResource TestCaseRunner runner) {
            runner.variable("petId", "1234");

            runner.when(petApi
                .sendGetPetById$("${petId}")
                .fork(true));

            runner.then(http().server(httpServer)
                .receive()
                .get("/api/v3/pet/${petId}")
                .message()
                .accept("@contains('application/json')@"));

            runner.then(http().server(httpServer)
                .send()
                .response(OK)
                .message()
                .cookie(new Cookie("NonApiCookie", "nonApiCookieValue"))
                .body(Resources.create(
                    "classpath:org/citrusframework/openapi/generator/GeneratedApiTest/payloads/getPetById_response.json"))
                .contentType("application/json"));

            runner.then(petApi.receiveGetPetById(OK)
                .message()
                .body("""
                    {
                      "id": ${petId},
                      "name": "@matches('hasso|cutie|fluffy')@",
                      "category": {
                        "id": ${petId},
                        "name": "@matches('dog|cat|fish')@"
                      },
                      "photoUrls": [
                        "http://localhost:8080/photos/${petId}"
                      ],
                      "tags": [
                        {
                          "id": ${petId},
                          "name": "generated"
                        }
                      ],
                      "status": "@matches('available|pending|sold')@"
                    }
                    """)
            );
        }
    }

    /**
     * Demonstrates testing of control response bodies from resource.
     */
    @Nested
    class ReceiveBodyFromResource {

        @Test
        @CitrusTestSource(type = TestLoader.SPRING, packageName = "org.citrusframework.openapi.generator.GeneratedApiTest", name = "withReceiveBodyFromResourceTest")
        void xml() {
        }

        @Test
        void java(@CitrusResource TestCaseRunner runner) {
            runner.variable("petId", "1234");

            runner.when(petApi
                .sendGetPetById$("${petId}")
                .fork(true));

            runner.then(http().server(httpServer)
                .receive()
                .get("/api/v3/pet/${petId}")
                .message()
                .accept("@contains('application/json')@"));

            runner.then(http().server(httpServer)
                .send()
                .response(OK)
                .message()
                .cookie(new Cookie("NonApiCookie", "nonApiCookieValue"))
                .body(Resources.create(
                    "classpath:org/citrusframework/openapi/generator/GeneratedApiTest/payloads/getPetById_response.json"))
                .contentType("application/json"));

            runner.then(petApi.receiveGetPetById(OK)
                .message()
                .body(Resources.create(
                    "classpath:org/citrusframework/openapi/generator/GeneratedApiTest/payloads/getPetById_response_validation.json"))
            );
        }
    }

    /**
     * Demonstrates testing of received non-API cookies.
     */
    @Nested
    class ReceiveNonApiCookie {

        @Test
        @CitrusTestSource(type = TestLoader.SPRING, packageName = "org.citrusframework.openapi.generator.GeneratedApiTest", name = "withReceiveNonApiCookieTest")
        void xml() {
        }

        @Test
        void java(@CitrusResource TestCaseRunner runner) {
            runner.variable("petId", "1234");

            runner.when(petApi
                .sendGetPetById$("${petId}")
                .fork(true));

            runner.then(http().server(httpServer)
                .receive()
                .get("/api/v3/pet/${petId}")
                .message()
                .accept("@contains('application/json')@"));

            runner.then(http().server(httpServer)
                .send()
                .response(OK)
                .message()
                .cookie(new Cookie("NonApiCookie", "nonApiCookieValue"))
                .body(Resources.create(
                    "classpath:org/citrusframework/openapi/generator/GeneratedApiTest/payloads/getPetById_response.json"))
                .contentType("application/json"));

            runner.then(petApi.receiveGetPetById(OK)
                .message()
                .cookie(new Cookie("NonApiCookie", "nonApiCookieValue"))
            );
        }
    }

    /**
     * Demonstrates validation of response by JSON path validation.
     */
    @Nested
    class JsonPathValidation {

        @Test
        @CitrusTestSource(type = TestLoader.SPRING, packageName = "org.citrusframework.openapi.generator.GeneratedApiTest", name = "withJsonPathValidationTest")
        void xml() {
        }

        @Test
        @CitrusTest
        void java(@CitrusResource TestCaseRunner runner) {

            runner.variable("petId", "1234");

            runner.when(petApi
                .sendGetPetById$("${petId}")
                .fork(true));

            runner.then(http().server(httpServer)
                .receive()
                .get("/api/v3/pet/${petId}")
                .message()
                .accept("@contains('application/json')@"));

            runner.then(http().server(httpServer)
                .send()
                .response(OK)
                .message()
                .body(Resources.create(
                    "classpath:org/citrusframework/openapi/generator/GeneratedApiTest/payloads/getPetById_response.json"))
                .contentType("application/json"));

            runner.then(petApi.receiveGetPetById(OK)
                .message()
                .validate(jsonPath().expression("$.name", "@matches('hasso|cutie|fluffy')@"))
            );
        }
    }

    /**
     * Demonstrates extraction of response data using JSON path.
     */
    @Nested
    class JsonPathExtraction {

        @Test
        @CitrusTestSource(type = TestLoader.SPRING, packageName = "org.citrusframework.openapi.generator.GeneratedApiTest", name = "withJsonPathExtractionTest")
        void xml() {
        }

        @Test
        void java(
            @CitrusResource TestCaseRunner runner, @CitrusResource TestContext context) {

            runner.variable("petId", "1234");

            runner.when(petApi
                .sendGetPetById$("${petId}")
                .fork(true));

            runner.then(http().server(httpServer)
                .receive()
                .get("/api/v3/pet/${petId}")
                .message()
                .accept("@contains('application/json')@"));

            runner.then(http().server(httpServer)
                .send()
                .response(OK)
                .message()
                .body(Resources.create(
                    "classpath:org/citrusframework/openapi/generator/GeneratedApiTest/payloads/getPetById_response.json"))
                .contentType("application/json"));

            runner.when(petApi
                .receiveGetPetById(OK)
                .message()
                .extract(MessageHeaderVariableExtractor.Builder.fromHeaders()
                    .expression("Content-Type", "varContentType"))
                .extract(JsonPathVariableExtractor.Builder.fromJsonPath()
                    .expression("$.name", "varName")))
            ;

            assertThat(context.getVariable("varContentType")).isEqualTo("application/json");
            assertThat(context.getVariable("varName")).matches("hasso|cutie|fluffy");
        }
    }

    /**
     * Demonstrates testing of API cookies in requests.
     */
    @Nested
    class ApiCookie {

        @Test
        @CitrusTestSource(type = TestLoader.SPRING, packageName = "org.citrusframework.openapi.generator.GeneratedApiTest", name = "withApiCookieTest")
        void xml() {
        }

        @Test
        void java(@CitrusResource TestCaseRunner runner) {
            runner.variable("petId", "1234");

            runner.when(extPetApi
                .sendGetPetWithCookie$("${petId}", "cookieValue")
                .optTrxId("trxId")
                .fork(true));

            runner.then(http().server(httpServer)
                .receive()
                .get("/api/v3/ext/pet/${petId}")
                .message()
                .cookie(new Cookie("session_id", "cookieValue"))
                .cookie(new Cookie("opt_trx_id", "trxId"))
            );

            runner.then(http().server(httpServer)
                .send()
                .response(OK)
                .message()
                .body(Resources.create(
                    "classpath:org/citrusframework/openapi/generator/GeneratedApiTest/payloads/getPetById_response.json"))
                .contentType("application/json"));

            runner.when(extPetApi
                .receiveGetPetWithCookie(OK)
                .message());
        }
    }

    /**
     * Demonstrates testing of file uploads and validations of the response.
     */
    @Nested
    class FileUpload {

        @Test
        @CitrusTestSource(type = TestLoader.SPRING, packageName = "org.citrusframework.openapi.generator.GeneratedApiTest", name = "withFileUploadTest")
        void uploadFile_xml() {
        }

        @Test
        @CitrusTest
        void uploadFile_java(@CitrusResource TestCaseRunner runner) {

            String additionalMetadata = "myMeta";
            String file = "filedata";

            runner.variable("petId", "1234");

            runner.when(petApi.sendUploadFile$("${petId}")
                .additionalMetadata(additionalMetadata)
                .message()
                .body(file)
                .fork(true));

            runner.then(http().server(httpServer)
                .receive()
                .post("/api/v3/pet/${petId}/uploadImage")
                .message()
                .contentType("application/octet-stream")
                .queryParam("additionalMetadata", "myMeta")
                .validate((message, context) -> {
                    Object payload = message.getPayload();
                    assertThat(payload).isInstanceOf(byte[].class);
                    assertThat(new String((byte[]) payload, StandardCharsets.UTF_8)).isEqualTo(
                        "filedata");
                })
            );

            runner.then(http().server(httpServer)
                .send()
                .response(OK)
                .message()
                .body("""
                    {"code": 12, "type":"post-image-ok", "message":"image successfully uploaded"}
                    """)
                .contentType("application/json"));

            runner.then(petApi
                .receiveUploadFile(OK)
                .message()
                .validate(jsonPath().expression("$.code", "12"))
                .validate(jsonPath().expression("$.message", "image successfully uploaded")));
        }
    }

    @TestConfiguration
    public static class Config {

        private final int port = SocketUtils.findAvailableTcpPort(8080);

        private final int otherPort = SocketUtils.findAvailableTcpPort(8081);

        private final int wsPort = SocketUtils.findAvailableTcpPort(8090);

        /**
         * Main http client for accessing the main http server.
         */
        @Bean(name = {"petstore.endpoint", "extpetstore.endpoint"})
        public HttpClient applicationServiceClient() {
            return new HttpClientBuilder()
                .requestUrl("http://localhost:%d".formatted(port))
                .handleCookies(true)
                .build();
        }

        /**
         * Http client accessing "other" server, see configuration of other server bean below.
         */
        @Bean
        public HttpClient otherApplicationServiceClient() {
            return new HttpClientBuilder()
                .requestUrl("http://localhost:%d".formatted(otherPort))
                .build();
        }

        /**
         * A sample actor used to test actor configuration and functionality.
         */
        @Bean
        public TestActor petStoreActor() {
            TestActor petStoreActor = new TestActor();
            petStoreActor.setName("PetStoreActor");
            petStoreActor.setDisabled(true);
            return petStoreActor;
        }

        /**
         * Http server for mocking server side messaging and asserting data being send from test api
         * requests.
         */
        @Bean
        public HttpServer httpServer() {
            return new HttpServerBuilder()
                .port(port)
                .timeout(5000L)
                .autoStart(true)
                .defaultStatus(HttpStatus.NO_CONTENT)
                .handleCookies(true)
                .handleHandleSemicolonPathContent(true)
                .build();
        }

        @Bean
        public WebServiceServer soapServer() {
            return WebServiceEndpoints.soap().server()
                .port(wsPort)
                .timeout(5000)
                .autoStart(true)
                .build();
        }

        /**
         * A second http server. Mainly for tests that assert, that the default endpoint
         * configuration can be overridden by an explicit endpoint.
         */
        @Bean
        public HttpServer otherHttpServer() {
            return new HttpServerBuilder()
                .port(otherPort)
                .timeout(5000L)
                .autoStart(true)
                .defaultStatus(HttpStatus.NO_CONTENT)
                .build();
        }

        /*
         * Global variables, that make the ports available within the test context. Mainly
         * used to access to port for explicit endpoint configuration tests.
         */
        @Bean
        public GlobalVariables globalVariables() {
            return new GlobalVariables.Builder()
                .variable("petstoreApplicationPort", port)
                .variable("otherPetstoreApplicationPort", otherPort).build();
        }

//        @Bean
//        public ApiActionBuilderCustomizer petApiCustomizer() {
//            return new ApiActionBuilderCustomizer() {
//                @Override
//                public <T extends TestApiOpenApiClientRequestActionBuilder> T customizeRequestBuilder(
//                    GeneratedApi generatedApi, T builder) {
//                    return ApiActionBuilderCustomizer.super.customizeRequestBuilder(generatedApi,
//                        builder);
//                }
//
//                @Override
//                public <T extends TestApiOpenApiClientResponseActionBuilder> T customizeResponseBuilder(
//                    GeneratedApi generatedApi, T builder) {
//                    return ApiActionBuilderCustomizer.super.customizeResponseBuilder(generatedApi,
//                        builder);
//                }
//            };
//        }
    }
}

