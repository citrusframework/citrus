/*
 * Copyright 2006-2023 the original author or authors.
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
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpClientBuilder;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.http.server.HttpServerBuilder;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.spi.Resources;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.util.SocketUtils;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.openapi.actions.OpenApiActionBuilder.openapi;

/**
 * @author Christoph Deppisch
 */
@Test
public class OpenApiServerIT extends TestNGCitrusSpringSupport {

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
                .accept("application/json")
                .fork(true));

        then(openapi(petstoreSpec)
                .server(httpServer)
                .receive("getPetById"));

        then(openapi(petstoreSpec)
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
    public void getAddPet() {
        variable("petId", "1001");

        when(http()
                .client(httpClient)
                .send()
                .post("/pet")
                .message()
                .body(Resources.create("classpath:org/citrusframework/openapi/petstore/pet.json"))
                .contentType("application/json")
                .fork(true));

        then(openapi(petstoreSpec)
                .server(httpServer)
                .receive("addPet"));

        then(openapi(petstoreSpec)
                .server(httpServer)
                .send("addPet", HttpStatus.CREATED));

        then(http()
                .client(httpClient)
                .receive()
                .response(HttpStatus.CREATED));
    }
}
