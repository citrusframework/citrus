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
import org.citrusframework.http.actions.HttpClientRequestActionBuilder.HttpMessageBuilderSupport;
import org.citrusframework.http.actions.HttpClientResponseActionBuilder;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpClientBuilder;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.http.server.HttpServerBuilder;
import org.citrusframework.openapi.AutoFillType;
import org.citrusframework.openapi.OpenApiRepository;
import org.citrusframework.openapi.actions.OpenApiClientResponseActionBuilder;
import org.citrusframework.openapi.integration.OpenApiClientIT.Config;
import org.citrusframework.openapi.validation.OpenApiValidationPolicy;
import org.citrusframework.spi.Resources;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.util.SocketUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static java.util.Collections.singletonList;
import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.message.MessageType.JSON;
import static org.citrusframework.openapi.actions.OpenApiActionBuilder.openapi;
import static org.citrusframework.openapi.validation.OpenApiMessageValidationContext.Builder.openApi;
import static org.citrusframework.validation.json.JsonPathMessageValidationContext.Builder.jsonPath;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.fail;

@Test
@ContextConfiguration(classes = {Config.class})
@DirtiesContext
public class OpenApiClientIT extends TestNGCitrusSpringSupport {

    public static final String VALID_PET_PATH = "classpath:org/citrusframework/openapi/petstore/pet.json";
    public static final String INVALID_PET_PATH = "classpath:org/citrusframework/openapi/petstore/pet_invalid.json";

    @Autowired
    private HttpServer httpServer;

    @Autowired
    private HttpClient httpClient;

    @CitrusTest
    @Test
    public void shouldExecuteGetPetById() {

        variable("petId", "1001");

        when(openapi("petstore-v3")
            .client(httpClient)
            .send("getPetById")
            .message()
            .fork(true));

        then(http().server(httpServer)
            .receive()
            .get("/pet/1001")
            .message()
            .accept("@contains('application/json')@"));

        then(http().server(httpServer)
            .send()
            .response(OK)
            .message()
            .body(Resources.create(VALID_PET_PATH))
            .contentType(APPLICATION_JSON_VALUE));

        then(openapi("petstore-v3")
            .client(httpClient).receive("getPetById", OK)
            .schemaValidation(true));

    }

    @CitrusTest
    @Test
    public void shouldExecuteGetPetByIdAndSucceedOnJsonMessageValidation() {

        variable("petId", "1001");

        when(openapi("petstore-v3")
            .client(httpClient)
            .send("getPetById")
            .message()
            .fork(true));

        then(http().server(httpServer)
            .receive()
            .get("/pet/1001")
            .message()
            .accept("@contains('application/json')@"));

        then(http().server(httpServer)
            .send()
            .response(OK)
            .message()
            .body(Resources.create(VALID_PET_PATH))
            .contentType(APPLICATION_JSON_VALUE));

        then(openapi("petstore-v3")
            .client(httpClient).receive("getPetById", OK)
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
    public void shouldExecuteGetPetByIdAndSucceedOnJsonPathMessageValidation() {

        variable("petId", "1001");

        when(openapi("petstore-v3")
            .client(httpClient)
            .send("getPetById")
            .message()
            .fork(true));

        then(http().server(httpServer)
            .receive()
            .get("/pet/1001")
            .message()
            .accept("@contains('application/json')@"));

        then(http().server(httpServer)
            .send()
            .response(OK)
            .message()
            .body(Resources.create(VALID_PET_PATH))
            .contentType(APPLICATION_JSON_VALUE));

        then(openapi("petstore-v3")
            .client(httpClient).receive("getPetById", OK)
            .schemaValidation(true)
            .message()
            .validate(
                jsonPath().expression("$.name",
                    "@assertThat(anyOf(equalTo('hasso'), equalTo('cutie'), equalTo('fluffy')))@")));

    }

    @CitrusTest
    @Test
    public void shouldExecuteGetPetByIdAndFailOnJsonPathMessageValidation() {

        variable("petId", "1001");

        when(openapi("petstore-v3")
            .client(httpClient)
            .send("getPetById")
            .message()
            .fork(true));

        then(http().server(httpServer)
            .receive()
            .get("/pet/1001")
            .message()
            .accept("@contains('application/json')@"));

        then(http().server(httpServer)
            .send()
            .response(OK)
            .message()
            .body(Resources.create(VALID_PET_PATH))
            .contentType(APPLICATION_JSON_VALUE));

        HttpClientResponseActionBuilder.HttpMessageBuilderSupport clientResponseActionBuilder = openapi(
            "petstore-v3")
            .client(httpClient).receive("getPetById", OK)
            .schemaValidation(true)
            .message()
            .validate(
                jsonPath().expression("$.name",
                    "other name"));
        assertThrows(() -> then(clientResponseActionBuilder));

    }

    @CitrusTest
    @Test
    public void shouldExecuteGetPetByIdAndFailOnJsonMessageValidation() {

        variable("petId", "1001");

        when(openapi("petstore-v3")
            .client(httpClient)
            .send("getPetById")
            .message()
            .fork(true));

        then(http().server(httpServer)
            .receive()
            .get("/pet/1001")
            .message()
            .accept("@contains('application/json')@"));

        then(http().server(httpServer)
            .send()
            .response(OK)
            .message()
            .body(Resources.create(VALID_PET_PATH))
            .contentType(APPLICATION_JSON_VALUE));

        HttpClientResponseActionBuilder.HttpMessageBuilderSupport clientResponseActionBuilder = openapi(
            "petstore-v3")
            .client(httpClient).receive("getPetById", OK)
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
        assertThrows(() -> then(clientResponseActionBuilder));

    }

    @CitrusTest
    @Test
    public void shouldExecuteGetPetByIdAndFailOnXmlMessageValidation() {

        variable("petId", "1001");

        when(openapi("petstore-v3")
            .client(httpClient)
            .send("getPetById")
            .message()
            .fork(true));

        then(http().server(httpServer)
            .receive()
            .get("/pet/1001")
            .message()
            .accept("@contains('application/json')@"));

        then(http().server(httpServer)
            .send()
            .response(OK)
            .message()
            .body(Resources.create(VALID_PET_PATH))
            .contentType(APPLICATION_JSON_VALUE));

        // ensure that we fail, even if we get the wrong type of message
        HttpClientResponseActionBuilder.HttpMessageBuilderSupport clientResponseActionBuilder = openapi(
            "petstore-v3")
            .client(httpClient).receive("getPetById", OK)
            .schemaValidation(true)
            .message().body("""
                <some-soap-response>
                </some-soap-response>
                """);
        assertThrows(() -> then(clientResponseActionBuilder));

    }

    @CitrusTest
    @Test
    public void shouldFailOnMissingNameInResponse() {

        variable("petId", "1001");

        when(openapi("petstore-v3")
            .client(httpClient)
            .send("getPetById")
            .autoFill(AutoFillType.ALL)
            .message()
            .fork(true));

        then(http().server(httpServer)
            .receive()
            .get("/pet/${petId}")
            .message()
            .accept("@contains('application/json')@"));

        then(http().server(httpServer)
            .send()
            .response(OK)
            .message()
            .body(Resources.create(INVALID_PET_PATH))
            .contentType(APPLICATION_JSON_VALUE));

        OpenApiClientResponseActionBuilder clientResponseActionBuilder = openapi("petstore-v3")
            .client(httpClient)
            .receive("getPetById", OK)
            .schemaValidation(true);
        assertThrows(() -> then(clientResponseActionBuilder));
    }

    @CitrusTest
    @Test
    public void shouldFailOnWrongControlMessage() {

        variable("petId", "1001");

        when(openapi("petstore-v3")
            .client(httpClient)
            .send("getPetById")
            .autoFill(AutoFillType.ALL)
            .message()
            .fork(true));

        then(http().server(httpServer)
            .receive()
            .get("/pet/${petId}")
            .message()
            .accept("@contains('application/json')@"));

        then(http().server(httpServer)
            .send()
            .response(OK)
            .message()
            .body(Resources.create(VALID_PET_PATH))
            .contentType(APPLICATION_JSON_VALUE));

        // Asserting against an empty json should fail due to standard json text validation
        HttpClientResponseActionBuilder.HttpMessageBuilderSupport clientResponseActionBuilder = openapi(
            "petstore-v3")
            .client(httpClient)
            .receive("getPetById", OK)
            .schemaValidation(true)
            .message()
            .type(JSON)
            .body("{}");
        assertThrows(() -> then(clientResponseActionBuilder));
    }

    /**
     * Tests validation of plain http client open api schema validation using
     * {@link org.citrusframework.openapi.validation.OpenApiValidationContext}
     */
    @CitrusTest
    @Test
    public void shouldFailOnMissingNameInResponseWhenUsingExplicitSchemaValidation() {
        variable("petId", "1001");

        when(http()
            .client(httpClient)
            .send()
            .get("/pet/1001")
            .message()
            .fork(true));

        then(http().server(httpServer)
            .receive()
            .get("/pet/${petId}")
            .message()
            .accept("@contains('application/json')@"));

        then(http().server(httpServer)
            .send()
            .response(OK)
            .message()
            .body(Resources.create(INVALID_PET_PATH))
            .contentType(APPLICATION_JSON_VALUE));

        // We can validate an OpenAPI response against a specific schema,
        // even without explicitly specifying the spec in the context.
        // By default, the OpenApiSpecification will be resolved by the schemaRepository name.
        HttpClientResponseActionBuilder.HttpMessageBuilderSupport receiveGetPetById = http().client(
                httpClient)
            .receive()
            .response(OK)
            .message()
            .type(JSON)
            .validate(openApi()
                .schemaValidation(true)
                .schemaRepository("petstore-v3.json")
                .schema("getPetById"));

        assertThrows(TestCaseFailedException.class, () -> then(receiveGetPetById));

    }

    @CitrusTest
    @Test
    public void shouldSucceedOnMissingNameInResponseWithValidationDisabled() {

        variable("petId", "1001");

        when(openapi("petstore-v3")
            .client(httpClient)
            .send("getPetById")
            .autoFill(AutoFillType.ALL)
            .message()
            .fork(true));

        then(http().server(httpServer)
            .receive()
            .get("/pet/${petId}")
            .message()
            .accept("@contains('application/json')@"));

        then(http().server(httpServer)
            .send()
            .response(OK)
            .message()
            .body(Resources.create(INVALID_PET_PATH))
            .contentType(APPLICATION_JSON_VALUE));

        then(openapi("petstore-v3")
            .client(httpClient).receive("getPetById", OK)
            .schemaValidation(false));

    }

    @CitrusTest
    @Test
    public void shouldProperlyExecuteAddPetFromRepository() {
        variable("petId", "1001");

        when(openapi("petstore-v3")
            .client(httpClient)
            .send("addPet")
            .autoFill(AutoFillType.ALL)
            .fork(true));

        then(http().server(httpServer)
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

        then(http().server(httpServer)
            .send()
            .response(HttpStatus.CREATED)
            .message());

        then(openapi("petstore-v3")
            .client(httpClient)
            .receive("addPet", HttpStatus.CREATED));

    }

    @CitrusTest
    @Test
    public void shouldFailOnMissingNameInRequest() {
        variable("petId", "1001");

        HttpMessageBuilderSupport addPetBuilder = openapi("petstore-v3")
            .client(httpClient)
            .send("addPet")
            .message().body(Resources.create(INVALID_PET_PATH));

        assertThrows(TestCaseFailedException.class, () -> when(addPetBuilder));
    }

    @CitrusTest
    @Test
    public void shouldFailOnWrongQueryIdType() {
        variable("petId", "xxxx");
        HttpMessageBuilderSupport addPetBuilder = openapi("petstore-v3")
            .client(httpClient)
            .send("addPet")
            .message().body(Resources.create(VALID_PET_PATH));
        assertThrows(TestCaseFailedException.class, () -> when(addPetBuilder));
    }

    @CitrusTest
    @Test
    public void shouldSucceedOnWrongQueryIdTypeWithOasDisabled() {
        variable("petId", "xxxx");
        HttpMessageBuilderSupport addPetBuilder = openapi("petstore-v3")
            .client(httpClient)
            .send("addPet")
            .schemaValidation(false)
            .message().body(Resources.create(VALID_PET_PATH));

        try {
            when(addPetBuilder);
        } catch (Exception e) {
            fail("Method threw an exception: " + e.getMessage());
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

        when(openapi(alias)
            .client(httpClient)
            .send("getPetById")
            .autoFill(AutoFillType.ALL)
            .message()
            .fork(true));

        then(http().server(httpServer)
            .receive()
            .get("/pet/${petId}")
            .message()
            .accept("@contains('application/json')@"));

        then(http().server(httpServer)
            .send()
            .response(OK)
            .message()
            .body(Resources.create(VALID_PET_PATH))
            .contentType(APPLICATION_JSON_VALUE));

        then(openapi(alias)
            .client(httpClient).receive("getPetById", OK)
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
