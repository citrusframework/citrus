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
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpClientBuilder;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.http.server.HttpServerBuilder;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.actions.OpenApiActionBuilder;
import org.citrusframework.openapi.actions.OpenApiServerRequestActionBuilder;
import org.citrusframework.openapi.actions.OpenApiServerResponseActionBuilder;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.spi.Resources;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.util.SocketUtils;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.openapi.actions.OpenApiActionBuilder.openapi;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.fail;

@Test
public class OpenApiServerIT extends TestNGCitrusSpringSupport {

    public static final String VALID_PET_PATH = "classpath:org/citrusframework/openapi/petstore/pet.json";
    public static final String INVALID_PET_PATH = "classpath:org/citrusframework/openapi/petstore/pet_invalid.json";

    private final int port = SocketUtils.findAvailableTcpPort(8080);

    @BindToRegistry
    private final HttpServer httpServer = new HttpServerBuilder()
            .port(port)
            .timeout(5000L)
            .autoStart(true)
            .defaultStatus(HttpStatus.NO_CONTENT)
            .build();

    @BindToRegistry
    private final HttpClient httpClient = new HttpClientBuilder()
            .requestUrl("http://localhost:%d/petstore/v3".formatted(port))
            .build();

    /**
     * Directly loaded open api.
     */
    private final OpenApiSpecification petstoreSpec = OpenApiSpecification.from(
        Resources.create("classpath:org/citrusframework/openapi/petstore/petstore-v3.json"));

    @CitrusTest
    public void shouldExecuteGetPetById() {
        shouldExecuteGetPetById(openapi(petstoreSpec));
    }


    private void shouldExecuteGetPetById(OpenApiActionBuilder openapi) {
        variable("petId", "1001");

        when(http()
                .client(httpClient)
                .send()
                .get("/pet/${petId}")
                .message()
                .accept("application/json")
                .fork(true));

        then(openapi
                .server(httpServer)
                .receive("getPetById"));

        then(openapi
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
    public void shouldExecuteAddPet() {
        shouldExecuteAddPet(openapi(petstoreSpec), VALID_PET_PATH, true);
    }

    @CitrusTest
    public void shouldFailOnMissingNameInRequest() {
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
            .accept("application/json")
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

        OpenApiServerRequestActionBuilder addPetBuilder = openapi(petstoreSpec)
            .server(httpServer)
            .receive("addPet");

        assertThrows(TestCaseFailedException.class, () -> then(addPetBuilder));
    }

    @CitrusTest
    public void shouldSucceedOnWrongQueryIdTypeWithOasDisabled() {
        variable("petId", "xxx");

        when(http()
            .client(httpClient)
            .send()
            .post("/pet")
            .message()
            .body(Resources.create(VALID_PET_PATH))
            .contentType("application/json")
            .fork(true));

        OpenApiServerRequestActionBuilder addPetBuilder = openapi(petstoreSpec)
            .server(httpServer)
            .receive("addPet")
            .disableOasValidation(true);

        try {
            when(addPetBuilder);
        } catch (Exception e) {
            fail("Method threw an exception: " + e.getMessage());
        }
    }

    private void shouldExecuteAddPet(OpenApiActionBuilder openapi, String requestFile, boolean valid) {
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
        } else {
            assertThrows(() -> then(receiveActionBuilder));
        }

        then(openapi
                .server(httpServer)
                .send("addPet", HttpStatus.CREATED));

        then(http()
                .client(httpClient)
                .receive()
                .response(HttpStatus.CREATED));
    }
}
