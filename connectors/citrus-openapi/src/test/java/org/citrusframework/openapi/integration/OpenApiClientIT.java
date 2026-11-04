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

package org.citrusframework.openapi.integration;

import java.util.stream.Stream;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestCaseRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.api.actions.openapi.OpenApiClientRequestActionBuilder;
import org.citrusframework.api.actions.openapi.OpenApiClientResponseActionBuilder;
import org.citrusframework.api.openapi.AutoFillType;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.exceptions.TestCaseFailedException;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpClientBuilder;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.http.server.HttpServerBuilder;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.openapi.OpenApiRepository;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.integration.OpenApiClientIT.Config;
import org.citrusframework.openapi.validation.OpenApiValidationPolicy;
import org.citrusframework.spi.Resources;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.citrusframework.util.SocketUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import static java.util.Collections.singletonList;
import static org.citrusframework.message.MessageType.JSON;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.junit.jupiter.api.Assertions.assertThrows;

@CitrusSpringSupport
@ContextConfiguration(classes = {CitrusSpringConfig.class, Config.class})
@DirtiesContext
@TestMethodOrder(MethodOrderer.MethodName.class)
@Isolated("ITs share one HTTP server instance; unconsumed requests from one test leak into the next")
public class OpenApiClientIT implements TestActionSupport {

    public static final String VALID_PET_PATH = "classpath:org/citrusframework/openapi/petstore/pet.json";
    public static final String INVALID_PET_PATH = "classpath:org/citrusframework/openapi/petstore/pet_invalid.json";

    @Autowired
    private HttpServer httpServer;

    @Autowired
    private HttpClient httpClient;

    private static final OpenApiSpecification petstoreSpec = OpenApiSpecification.from(
        Resources.create("classpath:org/citrusframework/openapi/petstore/petstore-v3.json"));

    private static final OpenApiSpecification petstoreSpecWithValidationDisabled = OpenApiSpecification.from(
        Resources.create("classpath:org/citrusframework/openapi/petstore/petstore-v3.json"));

    @BeforeAll
    static void beforeTest() {
        petstoreSpecWithValidationDisabled.setApiRequestValidationEnabled(false);
        petstoreSpecWithValidationDisabled.setApiResponseValidationEnabled(false);
    }

    @CitrusTest
    @Test
    public void shouldExecuteGetPetById(@CitrusResource TestCaseRunner runner) {

        runner.variable("petId", "1001");

        runner.when(openapi().alias("petstore-v3")
            .client(httpClient)
            .send("getPetById")
            .message()
            .fork(true));

        runner.then(http().server(httpServer)
            .receive()
            .get("/pet/1001")
            .message()
            .accept("@contains('application/json')@"));

        runner.then(http().server(httpServer)
            .send()
            .response(OK)
            .message()
            .body(Resources.create(VALID_PET_PATH))
            .contentType(APPLICATION_JSON_VALUE));

        runner.then(openapi().alias("petstore-v3")
            .client(httpClient).receive("getPetById", OK.name())
            .schemaValidation(true));

    }

    @CitrusTest
    @Test
    public void shouldExecuteGetPetByIdAndSucceedOnJsonMessageValidation(@CitrusResource TestCaseRunner runner) {

        runner.variable("petId", "1001");

        runner.when(openapi().alias("petstore-v3")
            .client(httpClient)
            .send("getPetById")
            .message()
            .fork(true));

        runner.then(http().server(httpServer)
            .receive()
            .get("/pet/1001")
            .message()
            .accept("@contains('application/json')@"));

        runner.then(http().server(httpServer)
            .send()
            .response(OK)
            .message()
            .body(Resources.create(VALID_PET_PATH))
            .contentType(APPLICATION_JSON_VALUE));

        runner.then(openapi().alias("petstore-v3")
            .client(httpClient).receive("getPetById", OK.name())
            .schemaValidation(true)
            .message().body("""
                {
                "id": 1001,
                  "name": "@assertThat(anyOf(equalTo('hasso'), equalTo('cutie'), equalTo('fluffy')))@",
                  "category": {
                    "id": 1001,
                    "name": "@assertThat(anyOf(equalTo('dog'), equalTo('cat'), equalTo('fish')))@"
                  },
                  "photoUrls": [ "http://localhost:8080/photos/1001" ],
                  "tags": [
                    {
                      "id": 1001,
                      "name": "generated"
                    }
                  ],
                  "status": "@assertThat(anyOf(equalTo('available'), equalTo('pending'), equalTo('sold')))@"
                }"""));

    }

    @CitrusTest
    @Test
    public void shouldExecuteGetPetByIdAndSucceedOnJsonPathMessageValidation(@CitrusResource TestCaseRunner runner) {

        runner.variable("petId", "1001");

        runner.when(openapi().alias("petstore-v3")
            .client(httpClient)
            .send("getPetById")
            .message()
            .fork(true));

        runner.then(http().server(httpServer)
            .receive()
            .get("/pet/1001")
            .message()
            .accept("@contains('application/json')@"));

        runner.then(http().server(httpServer)
            .send()
            .response(OK)
            .message()
            .body(Resources.create(VALID_PET_PATH))
            .contentType(APPLICATION_JSON_VALUE));

        runner.then(openapi().alias("petstore-v3")
            .client(httpClient).receive("getPetById", OK.name())
            .schemaValidation(true)
            .message()
            .validate(validation().jsonPath()
                    .expression("$.name",
                    "@assertThat(anyOf(equalTo('hasso'), equalTo('cutie'), equalTo('fluffy')))@")));

    }

    @CitrusTest
    @Test
    public void shouldExecuteGetPetByIdAndFailOnJsonPathMessageValidation(@CitrusResource TestCaseRunner runner) {

        runner.variable("petId", "1001");

        runner.when(openapi().alias("petstore-v3")
            .client(httpClient)
            .send("getPetById")
            .message()
            .fork(true));

        runner.then(http().server(httpServer)
            .receive()
            .get("/pet/1001")
            .message()
            .accept("@contains('application/json')@"));

        runner.then(http().server(httpServer)
            .send()
            .response(OK)
            .message()
            .body(Resources.create(VALID_PET_PATH))
            .contentType(APPLICATION_JSON_VALUE));

        TestActionBuilder<?> clientResponseActionBuilder = openapi().alias("petstore-v3")
            .client(httpClient).receive("getPetById", OK.name())
            .schemaValidation(true)
            .message()
            .validate(validation().jsonPath()
                    .expression("$.name", "other name"));
        assertThrows(TestCaseFailedException.class, () -> runner.then(clientResponseActionBuilder));

    }

    @CitrusTest
    @Test
    public void shouldExecuteGetPetByIdAndFailOnJsonMessageValidation(@CitrusResource TestCaseRunner runner) {

        runner.variable("petId", "1001");

        runner.when(openapi().alias("petstore-v3")
            .client(httpClient)
            .send("getPetById")
            .message()
            .fork(true));

        runner.then(http().server(httpServer)
            .receive()
            .get("/pet/1001")
            .message()
            .accept("@contains('application/json')@"));

        runner.then(http().server(httpServer)
            .send()
            .response(OK)
            .message()
            .body(Resources.create(VALID_PET_PATH))
            .contentType(APPLICATION_JSON_VALUE));

        TestActionBuilder<?> clientResponseActionBuilder = openapi().alias("petstore-v3")
            .client(httpClient).receive("getPetById", OK.name())
            .schemaValidation(true)
            .message().body("""
                {
                "id": 1001,
                  "name": "other name",
                  "category": {
                    "id": 1001,
                    "name": "@assertThat(anyOf(equalTo('dog'), equalTo('cat'), equalTo('fish')))@"
                  },
                  "photoUrls": [ "http://localhost:8080/photos/1001" ],
                  "tags": [
                    {
                      "id": 1001,
                      "name": "generated"
                    }
                  ],
                  "status": "@assertThat(anyOf(equalTo('available'), equalTo('pending'), equalTo('sold')))@"
                }""");
        assertThrows(TestCaseFailedException.class, () -> runner.then(clientResponseActionBuilder));

    }

    @CitrusTest
    @Test
    public void shouldExecuteGetPetByIdAndFailOnXmlMessageValidation(@CitrusResource TestCaseRunner runner) {

        runner.variable("petId", "1001");

        runner.when(openapi().alias("petstore-v3")
            .client(httpClient)
            .send("getPetById")
            .message()
            .fork(true));

        runner.then(http().server(httpServer)
            .receive()
            .get("/pet/1001")
            .message()
            .accept("@contains('application/json')@"));

        runner.then(http().server(httpServer)
            .send()
            .response(OK)
            .message()
            .body(Resources.create(VALID_PET_PATH))
            .contentType(APPLICATION_JSON_VALUE));

        // ensure that we fail, even if we get the wrong type of message
        TestActionBuilder<?> clientResponseActionBuilder = openapi().alias("petstore-v3")
            .client(httpClient).receive("getPetById", OK.name())
            .schemaValidation(true)
            .message().body("""
                <some-soap-response>
                </some-soap-response>
                """);
        assertThrows(TestCaseFailedException.class, () -> runner.then(clientResponseActionBuilder));

    }

    @CitrusTest
    @Test
    public void shouldFailOnMissingNameInResponse(@CitrusResource TestCaseRunner runner) {

        runner.variable("petId", "1001");

        runner.when(openapi().alias("petstore-v3")
            .client(httpClient)
            .send("getPetById")
            .autoFill(AutoFillType.ALL)
            .message()
            .fork(true));

        runner.then(http().server(httpServer)
            .receive()
            .get("/pet/${petId}")
            .message()
            .accept("@contains('application/json')@"));

        runner.then(http().server(httpServer)
            .send()
            .response(OK)
            .message()
            .body(Resources.create(INVALID_PET_PATH))
            .contentType(APPLICATION_JSON_VALUE));

        TestActionBuilder<?> clientResponseActionBuilder = openapi().alias("petstore-v3")
            .client(httpClient)
            .receive("getPetById", OK.name())
            .schemaValidation(true);
        assertThrows(TestCaseFailedException.class, () -> runner.then(clientResponseActionBuilder));
    }

    @CitrusTest
    @Test
    public void shouldFailOnWrongControlMessage(@CitrusResource TestCaseRunner runner) {

        runner.variable("petId", "1001");

        runner.when(openapi().alias("petstore-v3")
            .client(httpClient)
            .send("getPetById")
            .autoFill(AutoFillType.ALL)
            .message()
            .fork(true));

        runner.then(http().server(httpServer)
            .receive()
            .get("/pet/${petId}")
            .message()
            .accept("@contains('application/json')@"));

        runner.then(http().server(httpServer)
            .send()
            .response(OK)
            .message()
            .body(Resources.create(VALID_PET_PATH))
            .contentType(APPLICATION_JSON_VALUE));

        // Asserting against an empty json should fail due to standard json text validation
        TestActionBuilder<?> clientResponseActionBuilder = openapi().alias("petstore-v3")
            .client(httpClient)
            .receive("getPetById", OK.name())
            .schemaValidation(true)
            .message()
            .type(JSON)
            .body("{}");
        assertThrows(TestCaseFailedException.class, () -> runner.then(clientResponseActionBuilder));
    }

    /**
     * Tests validation of plain http client open api schema validation using
     * {@link org.citrusframework.openapi.validation.OpenApiValidationContext}
     */
    @CitrusTest
    @Test
    public void shouldFailOnMissingNameInResponseWhenUsingExplicitSchemaValidation(@CitrusResource TestCaseRunner runner) {
        runner.variable("petId", "1001");

        runner.when(http()
                .client(httpClient)
                .send()
                .get("/pet/1001")
                .message()
                .fork(true));

        runner.then(http().server(httpServer)
                .receive()
                .get("/pet/${petId}")
                .message()
                .accept("@contains('application/json')@"));

        runner.then(http().server(httpServer)
                .send()
                .response(OK)
                .message()
                .body(Resources.create(INVALID_PET_PATH))
                .contentType(APPLICATION_JSON_VALUE));

        // We can validate an OpenAPI response against a specific schema,
        // even without explicitly specifying the spec in the context.
        // By default, the OpenApiSpecification will be resolved by the schemaRepository name.
        assertThrows(TestCaseFailedException.class, () -> runner.then(http().client(httpClient)
                .receive()
                .response(OK)
                .message()
                .type(JSON)
                .validate(validation().openApi()
                        .schemaValidation(true)
                        .schemaRepository("petstore-v3.json")
                        .schema("getPetById"))));
    }

    static Stream<Arguments> validationConfigurations() {
        return Stream.of(new Object[][]{
            {petstoreSpecWithValidationDisabled, null, true},
            {petstoreSpecWithValidationDisabled, true, false},
            {petstoreSpecWithValidationDisabled, false, true},
            {petstoreSpec, null, false},
            {petstoreSpec, true, false},
            {petstoreSpec, false, true},
        }).map(Arguments::of);
    }

    @CitrusTest
    @ParameterizedTest(name = "spec={0}, builderValidation={1}, shouldPass={2}")
    @MethodSource("validationConfigurations")
    public void shouldProperlyValidateSendRequest(OpenApiSpecification openApiSpecification,
        Boolean builderValidation, boolean shouldPass, @CitrusResource TestCaseRunner runner) {

        runner.variable("petId", "1001");

        OpenApiClientRequestActionBuilder<?, ?, ?> requestActionBuilder = openapi(openApiSpecification)
            .client(httpClient)
            .send("addPet");

        requestActionBuilder
            .message()
            .body(Resources.create(INVALID_PET_PATH));

        if (builderValidation != null) {
            requestActionBuilder.schemaValidation(builderValidation);
        }

        if (shouldPass) {
            runner.when(requestActionBuilder.fork(true));
            runner.then(http().server(httpServer)
                .receive()
                .post("/petstore/v3/pet")
                .message()
                .accept("@contains('application/json')@"));

            runner.then(http().server(httpServer)
                .send()
                .response(HttpStatus.CREATED)
                .message());

            runner.then(openapi(openApiSpecification)
                .client(httpClient)
                .receive("addPet", HttpStatus.CREATED.name()));

        } else {
            assertThrows(TestCaseFailedException.class, () -> runner.when(requestActionBuilder));
        }
    }

    @ParameterizedTest(name = "spec={0}, builderValidation={1}, shouldPass={2}")
    @MethodSource("validationConfigurations")
    public void shouldProperlyValidateReceiveResponse(OpenApiSpecification openApiSpecification,
        Boolean builderValidation, boolean shouldPass, @CitrusResource TestCaseRunner runner) {

        runner.variable("petId", "1001");

        runner.when(openapi(openApiSpecification)
            .client(httpClient)
            .send("getPetById")
            .autoFill(AutoFillType.ALL)
            .message()
            .fork(true));

        runner.then(http().server(httpServer)
            .receive()
            .get("/petstore/v3/pet/${petId}")
            .message()
            .accept("@contains('application/json')@"));

        runner.then(http().server(httpServer)
            .send()
            .response(OK)
            .message()
            .body(Resources.create(INVALID_PET_PATH))
            .contentType(APPLICATION_JSON_VALUE));

        OpenApiClientResponseActionBuilder<?, ?, ?> responseActionBuilder = openapi(openApiSpecification)
            .client(httpClient).receive("getPetById", OK.name());

        if (builderValidation != null) {
            responseActionBuilder.schemaValidation(builderValidation);
        }

        if (shouldPass) {
            runner.then(responseActionBuilder);
        } else {
            assertThrows(TestCaseFailedException.class, () -> runner.then(responseActionBuilder));
        }
    }

    @CitrusTest
    @Test
    public void shouldProperlyExecuteAddPetFromRepository(@CitrusResource TestCaseRunner runner) {
        runner.variable("petId", "1001");

        runner.when(openapi().alias("petstore-v3")
            .client(httpClient)
            .send("addPet")
            .autoFill(AutoFillType.ALL)
            .fork(true));

        runner.then(http().server(httpServer)
            .receive()
            .post("/pet")
            .message()
            .body("""
                        {
                          "id": "@isNumber()@",
                          "name": "@notEmpty()@",
                          "category": {
                            "id": "@isNumber()@",
                            "name": "@notEmpty()@"
                          },
                          "photoUrls": "@notEmpty()@",
                          "tags":  "@ignore@",
                          "status": "@matches(sold|pending|available)@"
                        }
                """)
            .contentType("application/json;charset=UTF-8"));

        runner.then(http().server(httpServer)
            .send()
            .response(HttpStatus.CREATED)
            .message());

        runner.then(openapi().alias("petstore-v3")
            .client(httpClient)
            .receive("addPet", HttpStatus.CREATED.name()));

    }

    @CitrusTest
    @Test
    public void shouldFailOnMissingNameInRequest(@CitrusResource TestCaseRunner runner) {
        runner.variable("petId", "1001");

        TestActionBuilder<?> addPetBuilder = openapi().alias("petstore-v3")
            .client(httpClient)
            .send("addPet")
            .message()
            .body(Resources.create(INVALID_PET_PATH));

        assertThrows(TestCaseFailedException.class, () -> runner.when(addPetBuilder));
    }

    @CitrusTest
    @Test
    public void shouldFailOnWrongQueryIdType(@CitrusResource TestCaseRunner runner) {
        runner.variable("petId", "xxxx");
        TestActionBuilder<?> addPetBuilder = openapi().alias("petstore-v3")
            .client(httpClient)
            .send("addPet")
            .message()
            .body(Resources.create(VALID_PET_PATH));
        assertThrows(TestCaseFailedException.class, () -> runner.when(addPetBuilder));
    }

    @CitrusTest
    @Test
    public void shouldSucceedOnWrongQueryIdTypeWithOasDisabledByBuilder(@CitrusResource TestCaseRunner runner) {
        runner.variable("petId", "xxxx");
        runner.when(openapi().alias("petstore-v3")
            .client(httpClient)
            .send("addPet")
            .schemaValidation(false)
            .message()
            .body(Resources.create(VALID_PET_PATH)));
    }

    @CitrusTest
    @Test
    public void shouldSucceedOnWrongQueryIdTypeWithOasDisabledBySpec(@CitrusResource TestCaseRunner runner) {
        runner.variable("petId", "xxxx");
        runner.when(openapi(petstoreSpecWithValidationDisabled)
            .client(httpClient)
            .send("addPet")
            .message()
            .body(Resources.create(VALID_PET_PATH)));
    }

    @CitrusTest
    @Test
    public void shouldFailOnWrongQueryIdTypeWithOasDisabledBySpecButEnabledByBuilder(@CitrusResource TestCaseRunner runner) {
        runner.variable("petId", "xxxx");
        var addPetBuilder = openapi(petstoreSpecWithValidationDisabled)
            .client(httpClient)
            .send("addPet")
            .schemaValidation(true)
            .message()
            .body(Resources.create(VALID_PET_PATH));
        assertThrows(TestCaseFailedException.class, () -> runner.when(addPetBuilder));

    }

    static Stream<Arguments> aliases() {
        return Stream.of(new Object[][]{
            {"petstore-v3"},
            {"Swagger Petstore"},
            {"Swagger Petstore/1.0.1"},
        }).map(Arguments::of);
    }

    /**
     * Using OpenApiRepository, we should be able to resolve the OpenAPI spec from the name of the
     * spec file or one of its aliases.
     */
    @CitrusTest
    @ParameterizedTest(name = "alias={0}")
    @MethodSource("aliases")
    public void getPetByIdUsingOpenApiRepositoryAndAlias(String alias, @CitrusResource TestCaseRunner runner) {
        runner.variable("petId", "1001");

        runner.when(openapi().alias(alias)
            .client(httpClient)
            .send("getPetById")
            .autoFill(AutoFillType.ALL)
            .message()
            .fork(true));

        runner.then(http().server(httpServer)
            .receive()
            .get("/pet/${petId}")
            .message()
            .accept("@contains('application/json')@"));

        runner.then(http().server(httpServer)
            .send()
            .response(OK)
            .message()
            .body(Resources.create(VALID_PET_PATH))
            .contentType(APPLICATION_JSON_VALUE));

        runner.then(openapi().alias(alias)
            .client(httpClient).receive("getPetById", OK.name())
            .schemaValidation(true));
    }

    @Configuration
    public static class Config {

        private final int port = SocketUtils.findAvailableTcpPort(8080);

        @Bean
        public HttpServer httpServer() {

            return new HttpServerBuilder()
                .port(port)
                .timeout(5000L)
                .autoStart(true)
                .defaultStatus(HttpStatus.NO_CONTENT)
                .build();
        }

        @Bean
        public HttpClient httpClient() {
            return new HttpClientBuilder()
                .requestUrl("http://localhost:%d".formatted(port))
                .build();
        }

        @Bean
        public OpenApiRepository petstoreOpenApiRepository() {
            return new OpenApiRepository()
                .locations(singletonList(
                    "classpath:org/citrusframework/openapi/petstore/petstore-v3.json"))
                .neglectBasePath(true)
                .validationPolicy(OpenApiValidationPolicy.REPORT);
        }
    }
}
