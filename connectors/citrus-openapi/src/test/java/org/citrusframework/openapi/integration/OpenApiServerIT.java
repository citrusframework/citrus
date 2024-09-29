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
import org.citrusframework.http.actions.HttpServerResponseActionBuilder.HttpMessageBuilderSupport;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpClientBuilder;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.http.server.HttpServerBuilder;
import org.citrusframework.openapi.OpenApiRepository;
import org.citrusframework.openapi.actions.OpenApiActionBuilder;
import org.citrusframework.openapi.actions.OpenApiServerRequestActionBuilder;
import org.citrusframework.openapi.actions.OpenApiServerResponseActionBuilder;
import org.citrusframework.openapi.integration.OpenApiServerIT.Config;
import org.citrusframework.spi.Resources;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.util.SocketUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import java.util.List;

import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.openapi.actions.OpenApiActionBuilder.openapi;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.fail;

@Test
@ContextConfiguration(classes =  {Config.class})
public class OpenApiServerIT extends TestNGCitrusSpringSupport {

    public static final String VALID_PET_PATH = "classpath:org/citrusframework/openapi/petstore/pet.json";
    public static final String INVALID_PET_PATH = "classpath:org/citrusframework/openapi/petstore/pet_invalid.json";

    @Autowired
    private HttpServer httpServer;

    @Autowired
    private HttpClient httpClient;

    @CitrusTest
    public void shouldExecuteGetPetById() {
        variable("petId", "1001");

        when(http()
                .client(httpClient)
                .send()
                .get("/pet/${petId}")
                .message()
                .accept("application/json")
                .fork(true));

        then(openapi("petstore-v3")
                .server(httpServer)
                .receive("getPetById")
            .message()
        );

        then(openapi("petstore-v3")
                .server(httpServer)
                .send("getPetById", HttpStatus.OK));

        then(http()
                .client(httpClient)
                .receive()
                .response(HttpStatus.OK)
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

    @CitrusTest
    public void shouldExecuteGetPetByIdWithRandomizedId() {

        when(http()
            .client(httpClient)
            .send()
            .get("/pet/726354")
            .message()
            .accept("application/json")
            .fork(true));

        then(openapi("petstore-v3")
            .server(httpServer)
            .receive("getPetById")
            .message()
        );

        then(openapi("petstore-v3")
            .server(httpServer)
            .send("getPetById", HttpStatus.OK));

        then(http()
            .client(httpClient)
            .receive()
            .response(HttpStatus.OK)
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

    @CitrusTest
    public void executeGetPetByIdShouldFailOnInvalidResponse() {
        variable("petId", "1001");

        when(http()
            .client(httpClient)
            .send()
            .get("/pet/${petId}")
            .message()
            .accept("application/json")
            .fork(true));

        then(openapi("petstore-v3")
            .server(httpServer)
            .receive("getPetById"));

        HttpMessageBuilderSupport getPetByIdResponseBuilder = openapi("petstore-v3")
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
        assertThrows(TestCaseFailedException.class, () ->then(getPetByIdResponseBuilder));
    }

    @CitrusTest
    public void executeGetPetByIdShouldSucceedOnInvalidResponseWithValidationDisabled() {
        variable("petId", "1001");

        when(http()
            .client(httpClient)
            .send()
            .get("/pet/${petId}")
            .message()
            .accept("application/json")
            .fork(true));

        then(openapi("petstore-v3")
            .server(httpServer)
            .receive("getPetById"));

        HttpMessageBuilderSupport getPetByIdResponseBuilder = openapi("petstore-v3")
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
            .response(HttpStatus.OK)
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
        shouldExecuteAddPet(openapi("petstore-v3"), VALID_PET_PATH, true, true);
    }

    @CitrusTest
    public void shouldFailOnMissingNameInRequest() {
        shouldExecuteAddPet(openapi("petstore-v3"), INVALID_PET_PATH, false, true);
    }

    @CitrusTest
    public void shouldPassOnMissingNameInRequestIfValidationIsDisabled() {
        shouldExecuteAddPet(openapi("petstore-v3"), INVALID_PET_PATH, false, false);
    }

    @CitrusTest
    public void shouldFailOnMissingNameInResponse() {
        variable("petId", "1001");

        when(http()
            .client(httpClient)
            .send()
            .get("/pet/${petId}")
            .message()
            .accept("application/json")
            .fork(true));

        then(openapi("petstore-v3")
            .server(httpServer)
            .receive("getPetById"));

        OpenApiServerResponseActionBuilder sendMessageActionBuilder = openapi("petstore-v3")
            .server(httpServer)
            .send("getPetById", HttpStatus.OK);
        sendMessageActionBuilder.message().body(Resources.create(INVALID_PET_PATH));

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
            .contentType("application/json")
            .fork(true));

        OpenApiServerRequestActionBuilder addPetBuilder = openapi("petstore-v3")
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
            .contentType("application/json")
            .fork(true));

        OpenApiServerRequestActionBuilder addPetBuilder = openapi("petstore-v3")
            .server(httpServer)
            .receive("addPet")
            .schemaValidation(false);

        try {
            when(addPetBuilder);
        } catch (Exception e) {
            fail("Method threw an exception: " + e.getMessage());
        }
    }

    private void shouldExecuteAddPet(OpenApiActionBuilder openapi, String requestFile, boolean valid, boolean validationEnabled) {
        variable("petId", "1001");

        when(http()
                .client(httpClient)
                .send()
                .post("/pet")
                .message()
                .body(Resources.create(requestFile))
                .contentType("application/json")
                .fork(true));

        OpenApiServerRequestActionBuilder receiveActionBuilder = openapi
            .server(httpServer)
            .receive("addPet");
        if (valid) {
            then(receiveActionBuilder);

            then(openapi
                .server(httpServer)
                .send("addPet", HttpStatus.CREATED));

            then(http()
                .client(httpClient)
                .receive()
                .response(HttpStatus.CREATED));

        } else {
            assertThrows(() -> then(receiveActionBuilder));
        }


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
                .locations(List.of("classpath:org/citrusframework/openapi/petstore/petstore-v3.json"));
        }
    }

}
