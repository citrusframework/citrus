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

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.exceptions.TestCaseFailedException;
import org.citrusframework.http.actions.HttpServerRequestActionBuilder;
import org.citrusframework.http.actions.HttpServerResponseActionBuilder.HttpMessageBuilderSupport;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpClientBuilder;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.http.server.HttpServerBuilder;
import org.citrusframework.openapi.AutoFillType;
import org.citrusframework.openapi.OpenApiRepository;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.actions.OpenApiActionBuilder;
import org.citrusframework.openapi.actions.OpenApiServerRequestActionBuilder;
import org.citrusframework.openapi.actions.OpenApiServerResponseActionBuilder;
import org.citrusframework.openapi.integration.OpenApiServerIT.Config;
import org.citrusframework.spi.Resources;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.util.SocketUtils;
import org.citrusframework.validation.json.JsonPathMessageValidationContext;
import org.citrusframework.validation.json.JsonPathMessageValidationContext.Builder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static java.util.Collections.singletonList;
import static org.citrusframework.openapi.actions.OpenApiActionBuilder.openapi;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.testng.Assert.assertThrows;

@Test
@ContextConfiguration(classes = {Config.class})
@DirtiesContext
public class OpenApiServerIT extends TestNGCitrusSpringSupport {

    public static final String VALID_PET_PATH = "classpath:org/citrusframework/openapi/petstore/pet.json";
    public static final String INVALID_PET_PATH = "classpath:org/citrusframework/openapi/petstore/pet_invalid.json";

    @Autowired
    private HttpServer httpServer;

    @Autowired
    private HttpClient httpClient;

    private final OpenApiSpecification petstoreSpec = OpenApiSpecification.from(
        Resources.create("classpath:org/citrusframework/openapi/petstore/petstore-v3.json"));

    @CitrusTest
    public void getPetById() {
        variable("petId", "1001");

        when(http()
            .client(httpClient)
            .send()
            .get("/pet/${petId}")
            .message()
            .accept(APPLICATION_JSON_VALUE)
            .fork(true));

        then(openapi(petstoreSpec)
            .server(httpServer)
            .receive("getPetById")
            .message()
        );

        HttpStatus httpStatus = HttpStatus.OK;
        then(openapi(petstoreSpec)
            .server(httpServer)
            .send("getPetById", httpStatus));

        then(http()
            .client(httpClient)
            .receive()
            .response(httpStatus.value())
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
    public void getPetByIdWithUnknownResponse() {
        variable("petId", "1001");

        when(http()
            .client(httpClient)
            .send()
            .get("/pet/${petId}")
            .message()
            .accept(APPLICATION_JSON_VALUE)
            .fork(true));

        then(openapi(petstoreSpec)
            .server(httpServer)
            .receive("getPetById")
            .message()
        );

        // Fake any unexpected status code, to test whether we do not run into
        // validation issues.
        HttpStatus httpStatus = HttpStatus.CREATED;
        then(openapi(petstoreSpec)
            .server(httpServer)
            .send("getPetById", httpStatus));

        then(http()
            .client(httpClient)
            .receive()
            .response(httpStatus.value()));
    }


    @CitrusTest
    public void getPetByIdShouldFailOnInvalidResponse() {
        variable("petId", "1001");

        when(http()
            .client(httpClient)
            .send()
            .get("/pet/${petId}")
            .message()
            .accept(APPLICATION_JSON_VALUE)
            .fork(true));

        then(openapi(petstoreSpec)
            .server(httpServer)
            .receive("getPetById"));

        HttpMessageBuilderSupport getPetByIdResponseBuilder = openapi(petstoreSpec)
            .server(httpServer)
            .send("getPetById", HttpStatus.OK)
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
        assertThrows(TestCaseFailedException.class, () -> then(getPetByIdResponseBuilder));
    }

    @CitrusTest
    public void getPetByIdShouldSucceedOnInvalidResponseWithValidationDisabled() {
        variable("petId", "1001");

        when(http()
            .client(httpClient)
            .send()
            .get("/pet/${petId}")
            .message()
            .accept(APPLICATION_JSON_VALUE)
            .fork(true));

        then(openapi(petstoreSpec)
            .server(httpServer)
            .receive("getPetById"));

        HttpMessageBuilderSupport getPetByIdResponseBuilder = openapi(petstoreSpec)
            .server(httpServer)
            .send("getPetById", HttpStatus.OK)
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
        then(getPetByIdResponseBuilder);

        then(http()
            .client(httpClient)
            .receive()
            .response(HttpStatus.OK.value())
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

    @CitrusTest
    public void shouldExecuteAddPet() {
        shouldExecuteAddPet(openapi(petstoreSpec), VALID_PET_PATH, true);
    }

    @CitrusTest
    public void shouldFailOnMissingNameInRequest() {
        shouldExecuteAddPet(openapi(petstoreSpec), INVALID_PET_PATH, false);
    }

    @CitrusTest
    public void shouldPassOnMissingNameInRequestIfValidationIsDisabled() {
        shouldExecuteAddPet(openapi(petstoreSpec), INVALID_PET_PATH, false);
    }

    @CitrusTest
    public void shouldFailOnMissingNameInResponse() {
        variable("petId", "1001");

        when(http()
            .client(httpClient)
            .send()
            .get("/pet/${petId}")
            .message()
            .accept(APPLICATION_JSON_VALUE)
            .fork(true));

        then(openapi(petstoreSpec)
            .server(httpServer)
            .receive("getPetById"));

        OpenApiServerResponseActionBuilder sendMessageActionBuilder = openapi(petstoreSpec)
            .server(httpServer)
            .send("getPetById", HttpStatus.OK);
        sendMessageActionBuilder.message().body(Resources.create(INVALID_PET_PATH));

        assertThrows(TestCaseFailedException.class, () -> then(sendMessageActionBuilder));
    }

    @CitrusTest
    public void shouldFailOnMissingPayload() {
        variable("petId", "1001");

        when(http()
            .client(httpClient)
            .send()
            .get("/pet/${petId}")
            .message()
            .accept(APPLICATION_JSON_VALUE)
            .fork(true));

        then(openapi(petstoreSpec)
            .server(httpServer)
            .receive("getPetById"));

        OpenApiServerResponseActionBuilder sendMessageActionBuilder = openapi(petstoreSpec)
            .server(httpServer)
            .send("getPetById", HttpStatus.OK)
            .autoFill(AutoFillType.NONE);

        assertThrows(TestCaseFailedException.class, () -> then(sendMessageActionBuilder));
    }

    @CitrusTest
    public void shouldFailOnWrongQueryIdTypeWithOasDisabled() {
        variable("petId", "xxx");

        when(http()
            .client(httpClient)
            .send()
            .post("/pet")
            .message()
            .body(Resources.create(VALID_PET_PATH))
            .contentType(APPLICATION_JSON_VALUE)
            .fork(true));

        OpenApiServerRequestActionBuilder addPetBuilder = openapi(petstoreSpec)
            .server(httpServer)
            .receive("addPet");

        assertThrows(TestCaseFailedException.class, () -> then(addPetBuilder));
    }

    @CitrusTest
    public void shouldSucceedOnWrongQueryIdTypeWithOasDisabled() {
        variable("petId", -1);

        when(http()
            .client(httpClient)
            .send()
            .post("/pet")
            .message()
            .body(Resources.create(VALID_PET_PATH))
            .contentType(APPLICATION_JSON_VALUE)
            .fork(true));

        OpenApiServerRequestActionBuilder addPetBuilder = openapi(petstoreSpec)
            .server(httpServer)
            .receive("addPet")
            .schemaValidation(false);

        try {
            when(addPetBuilder);
        } catch (Exception e) {
            fail("Method threw an exception: " + e.getMessage());
        }
    }

    @CitrusTest
    public void shouldExecuteAddPetAndSucceedOnValidateReceivedJsonMessage() {
        variable("petId", "1001");

        when(http()
            .client(httpClient)
            .send()
            .post("/pet")
            .message()
            .body(Resources.create(VALID_PET_PATH))
            .contentType(APPLICATION_JSON_VALUE)
            .fork(true));

        then(openapi(petstoreSpec)
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

        when(openapi(petstoreSpec)
            .server(httpServer)
            .send("addPet", HttpStatus.CREATED));

        then(http()
            .client(httpClient)
            .receive()
            .response(HttpStatus.CREATED.value()));
    }

    @CitrusTest
    public void shouldExecuteAddPetAndFailOnValidateReceivedJsonMessage() {
        variable("petId", "1001");

        when(http()
            .client(httpClient)
            .send()
            .post("/pet")
            .message()
            .body(Resources.create(VALID_PET_PATH))
            .contentType(APPLICATION_JSON_VALUE)
            .fork(true));

        HttpServerRequestActionBuilder.HttpMessageBuilderSupport receiveActionBuilder = openapi(
            petstoreSpec)
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
        assertThrows(() -> then(receiveActionBuilder));
    }

    @CitrusTest
    public void shouldExecuteAddPetAndSucceedOnValidateJsonPathOnReceivedMessage() {
        variable("petId", "1001");

        when(http()
            .client(httpClient)
            .send()
            .post("/pet")
            .message()
            .body(Resources.create(VALID_PET_PATH))
            .contentType(APPLICATION_JSON_VALUE)
            .fork(true));

        then(openapi(petstoreSpec)
            .server(httpServer)
            .receive("addPet")
            .message()
            .validate(JsonPathMessageValidationContext.Builder.jsonPath().expression("$.name",
                "@assertThat(anyOf(equalTo('hasso'), equalTo('cutie'), equalTo('fluffy')))@")));

        when(openapi(petstoreSpec)
            .server(httpServer)
            .send("addPet", HttpStatus.CREATED));

        then(http()
            .client(httpClient)
            .receive()
            .response(HttpStatus.CREATED.value()));
    }

    @CitrusTest
    public void shouldExecuteAddPetAndFailOnValidateJsonPathOnReceivedMessage() {
        variable("petId", "1001");

        when(http()
            .client(httpClient)
            .send()
            .post("/pet")
            .message()
            .body(Resources.create(VALID_PET_PATH))
            .contentType(APPLICATION_JSON_VALUE)
            .fork(true));

        HttpServerRequestActionBuilder.HttpMessageBuilderSupport receiveActionBuilder = openapi(
            petstoreSpec)
            .server(httpServer)
            .receive("addPet")
            .message()
            .validate(Builder.jsonPath().expression("$.name",
                "other name"));

        assertThrows(() -> then(receiveActionBuilder));
    }

    private void shouldExecuteAddPet(OpenApiActionBuilder openapi, String requestFile,
        boolean valid) {
        variable("petId", "1001");

        when(http()
            .client(httpClient)
            .send()
            .post("/pet")
            .message()
            .body(Resources.create(requestFile))
            .contentType(APPLICATION_JSON_VALUE)
            .fork(true));

        OpenApiServerRequestActionBuilder receiveActionBuilder = openapi
            .server(httpServer)
            .receive("addPet");

        if (valid) {
            then(receiveActionBuilder);

            when(openapi
                .server(httpServer)
                .send("addPet", HttpStatus.CREATED));

            then(http()
                .client(httpClient)
                .receive()
                .response(HttpStatus.CREATED.value()));

        } else {
            assertThrows(() -> then(receiveActionBuilder));
        }
    }

    @DataProvider
    public Object[][] aliases() {
        return new Object[][]{
            {"petstore-v3"},
            {"Swagger Petstore"},
            {"Swagger Petstore/1.0.1"},
        };
    }

    /**
     * Using OpenApiRepository, we should be able to resolve the OpenAPI spec from the name of the
     * spec file or one of its aliases.
     */
    @CitrusTest
    @Test(dataProvider = "aliases")
    public void getPetByIdUsingOpenApiRepositoryAndAlias(String alias) {
        variable("petId", "1001");

        when(http()
            .client(httpClient)
            .send()
            .get("/pet/${petId}")
            .message()
            .accept(APPLICATION_JSON_VALUE)
            .fork(true));

        then(openapi(alias)
            .server(httpServer)
            .receive("getPetById")
            .message()
        );

        HttpStatus httpStatus = HttpStatus.OK;
        then(openapi(alias)
            .server(httpServer)
            .send("getPetById", httpStatus));

        then(http()
            .client(httpClient)
            .receive()
            .response(httpStatus.value())
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
