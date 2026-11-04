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
import org.citrusframework.openapi.actions.OpenApiServerRequestActionBuilder;
import org.citrusframework.openapi.actions.OpenApiServerResponseActionBuilder;
import org.citrusframework.openapi.integration.OpenApiServerIT.Config;
import org.citrusframework.spi.Resources;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.citrusframework.util.SocketUtils;
import org.citrusframework.validation.context.json.JsonPathMessageValidationContext.Builder;
import org.citrusframework.validation.context.json.JsonPathMessageValidationContext;
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
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.junit.jupiter.api.Assertions.assertThrows;

@CitrusSpringSupport
@ContextConfiguration(classes = {CitrusSpringConfig.class, Config.class})
@DirtiesContext
@TestMethodOrder(MethodOrderer.MethodName.class)
@Isolated("ITs share one HTTP server instance")
public class OpenApiServerIT implements TestActionSupport {

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
    public void getPetById(@CitrusResource TestCaseRunner runner) {
        runner.variable("petId", "1001");

        runner.when(http()
            .client(httpClient)
            .send()
            .get("/pet/${petId}")
            .message()
            .accept(APPLICATION_JSON_VALUE)
            .fork(true));

        runner.then(openapi(petstoreSpec)
            .server(httpServer)
            .receive("getPetById")
            .message()
        );

        HttpStatus httpStatus = OK;
        runner.then(openapi(petstoreSpec)
            .server(httpServer)
            .send("getPetById", httpStatus.name()));

        runner.then(http()
            .client(httpClient)
            .receive()
            .response(httpStatus)
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
                """));
    }

    /**
     * Sending an open api response with a status not specified in the spec should not fail. This is
     * because the OpenAPI spec does not strictly require modelling of all possible responses.
     */
    @CitrusTest
    @Test
    public void getPetByIdWithUnknownResponse(@CitrusResource TestCaseRunner runner) {
        runner.variable("petId", "1001");

        runner.when(http()
            .client(httpClient)
            .send()
            .get("/pet/${petId}")
            .message()
            .accept(APPLICATION_JSON_VALUE)
            .fork(true));

        runner.then(openapi(petstoreSpec)
            .server(httpServer)
            .receive("getPetById")
            .message()
        );

        // Fake any unexpected status code, to test whether we do not run into
        // validation issues.
        HttpStatus httpStatus = HttpStatus.CREATED;
        runner.then(openapi(petstoreSpec)
            .server(httpServer)
            .send("getPetById", httpStatus.name()));

        runner.then(http()
            .client(httpClient)
            .receive()
            .response(httpStatus));
    }


    @CitrusTest
    @Test
    public void getPetByIdShouldFailOnInvalidResponse(@CitrusResource TestCaseRunner runner) {
        runner.variable("petId", "1001");

        runner.when(http()
            .client(httpClient)
            .send()
            .get("/pet/${petId}")
            .message()
            .accept(APPLICATION_JSON_VALUE)
            .fork(true));

        runner.then(openapi(petstoreSpec)
            .server(httpServer)
            .receive("getPetById"));

        TestActionBuilder<?> getPetByIdResponseBuilder = openapi(petstoreSpec)
            .server(httpServer)
            .send("getPetById", OK.name())
            .message().body("""
                        {
                          "id": "xxxx",
                          "name": "Garfield",
                          "category": {
                            "id": 111,
                            "name": "Comic"
                          },
                          "photoUrls": [],
                          "tags":  [],
                          "status": "available"
                        }
                """);
        assertThrows(TestCaseFailedException.class, () -> runner.then(getPetByIdResponseBuilder));
    }

    @CitrusTest
    @Test
    public void getPetByIdShouldSucceedOnInvalidResponseWithValidationDisabled(@CitrusResource TestCaseRunner runner) {
        runner.variable("petId", "1001");

        runner.when(http()
            .client(httpClient)
            .send()
            .get("/pet/${petId}")
            .message()
            .accept(APPLICATION_JSON_VALUE)
            .fork(true));

        runner.then(openapi(petstoreSpec)
            .server(httpServer)
            .receive("getPetById"));

        TestActionBuilder<?> getPetByIdResponseBuilder = openapi(petstoreSpec)
            .server(httpServer)
            .send("getPetById", OK.name())
            .schemaValidation(false)
            .message().body("""
                        {
                          "id": "xxxx",
                          "name": "Garfield",
                          "category": {
                            "id": 111,
                            "name": "Comic"
                          },
                          "photoUrls": [],
                          "tags":  [],
                          "status": "available"
                        }
                """);
        runner.then(getPetByIdResponseBuilder);

        runner.then(http()
            .client(httpClient)
            .receive()
            .response(OK)
            .message()
            .body("""
                        {
                          "id": "xxxx",
                          "name": "Garfield",
                          "category": {
                            "id": 111,
                            "name": "Comic"
                          },
                          "photoUrls": [],
                          "tags":  [],
                          "status": "available"
                        }
                """));
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
    public void properlyValidateReceiveRequestValidation(OpenApiSpecification openApiSpecification,
        Boolean builderValidation, boolean shouldPass,
        @CitrusResource TestCaseRunner runner) {
        shouldExecuteAddPet(openApiSpecification, INVALID_PET_PATH, builderValidation, shouldPass, runner);
    }

    @CitrusTest
    @ParameterizedTest(name = "spec={0}, builderValidation={1}, shouldPass={2}")
    @MethodSource("validationConfigurations")
    public void performProperSendResponseValidation(OpenApiSpecification openApiSpecification,
        Boolean builderValidation, boolean shouldPass, @CitrusResource TestCaseRunner runner) {
        runner.variable("petId", "1001");

        runner.when(http()
            .client(httpClient)
            .send()
            .get("/petstore/v3/pet/${petId}")
            .message()
            .accept(APPLICATION_JSON_VALUE)
            .fork(true));

        runner.then(openapi(openApiSpecification)
            .server(httpServer)
            .receive("getPetById"));

        OpenApiServerResponseActionBuilder sendMessageActionBuilder = (OpenApiServerResponseActionBuilder) openapi(
            openApiSpecification)
            .server(httpServer)
            .send("getPetById", OK.name());
        sendMessageActionBuilder.message()
            .body(Resources.create(INVALID_PET_PATH));

        if (builderValidation != null) {
            sendMessageActionBuilder.schemaValidation(builderValidation);
        }

        if (shouldPass) {
            runner.when(sendMessageActionBuilder);
            runner.then(http()
                .client(httpClient)
                .receive()
                .response(OK));
        } else {
            assertThrows(TestCaseFailedException.class, () -> runner.then(sendMessageActionBuilder));
        }
    }

    @CitrusTest
    @Test
    public void shouldFailOnMissingPayload(@CitrusResource TestCaseRunner runner) {
        runner.variable("petId", "1001");

        runner.when(http()
            .client(httpClient)
            .send()
            .get("/pet/${petId}")
            .message()
            .accept(APPLICATION_JSON_VALUE)
            .fork(true));

        runner.then(openapi(petstoreSpec)
            .server(httpServer)
            .receive("getPetById"));

        TestActionBuilder<?> sendMessageActionBuilder = openapi(petstoreSpec)
            .server(httpServer)
            .send("getPetById", OK.name())
            .autoFill(AutoFillType.NONE);

        assertThrows(TestCaseFailedException.class, () -> runner.then(sendMessageActionBuilder));
    }

    @CitrusTest
    @Test
    public void shouldFailOnWrongQueryIdTypeWithOasDisabled(@CitrusResource TestCaseRunner runner) {
        runner.variable("petId", "xxx");

        runner.when(http()
            .client(httpClient)
            .send()
            .post("/pet")
            .message()
            .body(Resources.create(VALID_PET_PATH))
            .contentType(APPLICATION_JSON_VALUE)
            .fork(true));

        TestActionBuilder<?> addPetBuilder = openapi(petstoreSpec)
            .server(httpServer)
            .receive("addPet");

        assertThrows(TestCaseFailedException.class, () -> runner.then(addPetBuilder));
    }

    @CitrusTest
    @Test
    public void shouldSucceedOnWrongQueryIdTypeWithOasDisabled(@CitrusResource TestCaseRunner runner) {
        runner.variable("petId", -1);

        runner.when(http()
            .client(httpClient)
            .send()
            .post("/pet")
            .message()
            .body(Resources.create(VALID_PET_PATH))
            .contentType(APPLICATION_JSON_VALUE)
            .fork(true));

        runner.when(openapi(petstoreSpec)
            .server(httpServer)
            .receive("addPet")
            .schemaValidation(false));
    }

    @CitrusTest
    @Test
    public void shouldExecuteAddPetAndSucceedOnValidateReceivedJsonMessage(@CitrusResource TestCaseRunner runner) {
        runner.variable("petId", "1001");

        runner.when(http()
            .client(httpClient)
            .send()
            .post("/pet")
            .message()
            .body(Resources.create(VALID_PET_PATH))
            .contentType(APPLICATION_JSON_VALUE)
            .fork(true));

        runner.then(openapi(petstoreSpec)
            .server(httpServer)
            .receive("addPet")
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

        runner.when(openapi(petstoreSpec)
            .server(httpServer)
            .send("addPet", HttpStatus.CREATED.name()));

        runner.then(http()
            .client(httpClient)
            .receive()
            .response(HttpStatus.CREATED));
    }

    @CitrusTest
    @Test
    public void shouldExecuteAddPetAndFailOnValidateReceivedJsonMessage(@CitrusResource TestCaseRunner runner) {
        runner.variable("petId", "1001");

        runner.when(http()
            .client(httpClient)
            .send()
            .post("/pet")
            .message()
            .body(Resources.create(VALID_PET_PATH))
            .contentType(APPLICATION_JSON_VALUE)
            .fork(true));

        TestActionBuilder<?> receiveActionBuilder = openapi(petstoreSpec)
            .server(httpServer)
            .receive("addPet")
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
        assertThrows(TestCaseFailedException.class, () -> runner.then(receiveActionBuilder));
    }

    @CitrusTest
    @Test
    public void shouldExecuteAddPetAndSucceedOnValidateJsonPathOnReceivedMessage(@CitrusResource TestCaseRunner runner) {
        runner.variable("petId", "1001");

        runner.when(http()
            .client(httpClient)
            .send()
            .post("/pet")
            .message()
            .body(Resources.create(VALID_PET_PATH))
            .contentType(APPLICATION_JSON_VALUE)
            .fork(true));

        runner.then(openapi(petstoreSpec)
            .server(httpServer)
            .receive("addPet")
            .message()
            .validate(JsonPathMessageValidationContext.Builder.jsonPath().expression("$.name",
                "@assertThat(anyOf(equalTo('hasso'), equalTo('cutie'), equalTo('fluffy')))@")));

        runner.when(openapi(petstoreSpec)
            .server(httpServer)
            .send("addPet", HttpStatus.CREATED.name()));

        runner.then(http()
            .client(httpClient)
            .receive()
            .response(HttpStatus.CREATED));
    }

    @CitrusTest
    @Test
    public void shouldExecuteAddPetAndFailOnValidateJsonPathOnReceivedMessage(@CitrusResource TestCaseRunner runner) {
        runner.variable("petId", "1001");

        runner.when(http()
            .client(httpClient)
            .send()
            .post("/pet")
            .message()
            .body(Resources.create(VALID_PET_PATH))
            .contentType(APPLICATION_JSON_VALUE)
            .fork(true));

        TestActionBuilder<?> receiveActionBuilder = openapi(petstoreSpec)
            .server(httpServer)
            .receive("addPet")
            .message()
            .validate(Builder.jsonPath().expression("$.name",
                "other name"));

        assertThrows(TestCaseFailedException.class, () -> runner.then(receiveActionBuilder));
    }

    private void shouldExecuteAddPet(OpenApiSpecification openApiSpecification, String requestFile,
        Boolean validateServerReceive, boolean shouldPass, TestCaseRunner runner) {
        runner.variable("petId", "1001");

        runner.when(http()
            .client(httpClient)
            .send()
            .post("/pet")
            .message()
            .body(Resources.create(requestFile))
            .contentType(APPLICATION_JSON_VALUE)
            .fork(true));

        OpenApiServerRequestActionBuilder receiveActionBuilder = (OpenApiServerRequestActionBuilder) openapi(
            openApiSpecification)
            .server(httpServer)
            .receive("addPet");

        if (validateServerReceive != null) {
            receiveActionBuilder.schemaValidation(validateServerReceive);
        }

        if (shouldPass) {
            runner.then(receiveActionBuilder);

            runner.when(openapi(openApiSpecification)
                .server(httpServer)
                .send("addPet", HttpStatus.CREATED));

            runner.then(http()
                .client(httpClient)
                .receive()
                .response(HttpStatus.CREATED));

        } else {
            assertThrows(TestCaseFailedException.class, () -> runner.then(receiveActionBuilder));
        }
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

        runner.when(http()
            .client(httpClient)
            .send()
            .get("/pet/${petId}")
            .message()
            .accept(APPLICATION_JSON_VALUE)
            .fork(true));

        runner.then(openapi().alias(alias)
            .server(httpServer)
            .receive("getPetById")
            .message()
        );

        HttpStatus httpStatus = OK;
        runner.then(openapi().alias(alias)
            .server(httpServer)
            .send("getPetById", httpStatus.name()));

        runner.then(http()
            .client(httpClient)
            .receive()
            .response(httpStatus)
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
                """));
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
                .requestUrl("http://localhost:%d/petstore/v3".formatted(port))
                .build();
        }

        @Bean
        public OpenApiRepository petstoreOpenApiRepository() {
            return new OpenApiRepository()
                .locations(singletonList(
                    "classpath:org/citrusframework/openapi/petstore/petstore-v3.json"));
        }
    }
}
