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

import static org.citrusframework.http.actions.HttpActionBuilder.http;
import static org.citrusframework.openapi.actions.OpenApiActionBuilder.openapi;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpClientBuilder;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.http.server.HttpServerBuilder;
import org.citrusframework.openapi.AutoFillType;
import org.citrusframework.openapi.OpenApiSpecification;
import org.citrusframework.openapi.integration.OpenApiClientIT.Config;
import org.citrusframework.spi.Resources;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.util.SocketUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This integration test demonstrates how to configure and test OpenAPI specifications with different context paths
 * using the Citrus framework. {@link OpenApiSpecification}s can be hooked to different context paths, with detailed control
 * over how the context path is constructed.
 * <p>
 * By default, the basePath from the OpenAPI specification is used. If the basePath is not present, the plain operation
 * path is used. However, you can configure detailed control over the context path using the `contextPath` and `neglectBasePath` properties.
 * <p>
 * The test uses these specifications with different context paths to verify the correct API paths are used in the HTTP requests and responses.
 *
 * @see OpenApiSpecification
 * @see CitrusTest
 */
@Test
@ContextConfiguration(classes = {Config.class})
public class OpenApiClientApiContextPathIT extends TestNGCitrusSpringSupport {

    public static final String VALID_PET_PATH = "classpath:org/citrusframework/openapi/petstore/pet.json";

    @Autowired
    private HttpServer httpServer;

    @Autowired
    private HttpClient httpClient;

    private static final OpenApiSpecification petstoreSpec = OpenApiSpecification.from(
        Resources.create("classpath:org/citrusframework/openapi/petstore/petstore-v3.json"))
        .alias("spec1");

    private static final OpenApiSpecification petstoreSpecNeglectingBasePath = OpenApiSpecification.from(
            Resources.create("classpath:org/citrusframework/openapi/petstore/petstore-v3.json"))
        .alias("spec2")
        .neglectBasePath(true);

    private static final OpenApiSpecification petstoreSpecWithContextPath = OpenApiSpecification.from(
            Resources.create("classpath:org/citrusframework/openapi/petstore/petstore-v3.json"))
        .alias("spec3")
        .rootContextPath("/api");

    private static final OpenApiSpecification petstoreSpecWithContextPathNeglectingBasePath = OpenApiSpecification.from(
            Resources.create("classpath:org/citrusframework/openapi/petstore/petstore-v3.json"))
        .alias("spec4")
        .neglectBasePath(true)
        .rootContextPath("/api");

    @DataProvider
    public static Object[][] openApiSpecifications() {
        return new Object[][]{
            {petstoreSpec, "/petstore/v3/pet"},
            {petstoreSpecNeglectingBasePath, "/pet"},
            {petstoreSpecWithContextPath, "/api/petstore/v3/pet"},
            {petstoreSpecWithContextPathNeglectingBasePath, "/api/pet"},
        };
    }

    @CitrusTest
    @Test(dataProvider = "openApiSpecifications")
    public void shouldExecuteGetPetById(OpenApiSpecification spec, String contextPath) {

        variable("petId", "1001");

        when(openapi(spec)
            .client(httpClient)
            .send("getPetById")
            .autoFill(AutoFillType.ALL)
            .message()
            .fork(true));

        then(http().server(httpServer)
            .receive()
            .get(contextPath+"/1001")
            .message()
            .accept("@contains('application/json')@"));

        then(http().server(httpServer)
            .send()
            .response(OK)
            .message()
            .body(Resources.create(VALID_PET_PATH))
            .contentType(APPLICATION_JSON_VALUE));

        then(openapi(spec)
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

    }
}
