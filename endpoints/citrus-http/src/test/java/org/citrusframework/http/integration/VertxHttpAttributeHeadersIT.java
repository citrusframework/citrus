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

package org.citrusframework.http.integration;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpClientBuilder;
import org.citrusframework.http.vertx.VertxHttpServer;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Staged request attributes reach Citrus server receive actions as message headers, mirroring
 * the Jetty {@code handleAttributeHeaders} contract: verbatim names, raw values, overwriting
 * wire headers of the same name, and only when the flag is enabled.
 */
@Test
public class VertxHttpAttributeHeadersIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    private static final String PATH = "/attributes";

    private VertxHttpServer prototype;

    @BeforeClass
    public void startPrototype() {
        prototype = new VertxHttpServer();
        prototype.setName("vertxAttributes");
        prototype.setPort(0);
        prototype.setHandleAttributeHeaders(true);
        prototype.setRequestAttributes((request, attributes) -> {
            attributes.put("app.user", "alice");
            attributes.put("app.attempt", 7);
            attributes.put("X-Source", "attribute");
        });
        prototype.initialize();
        prototype.start();
    }

    @AfterClass(alwaysRun = true)
    public void stopPrototype() {
        if (prototype != null) {
            prototype.stop();
            prototype.destroy();
        }
    }

    @CitrusTest
    public void attributesAreReceivedAsMessageHeadersWhenEnabled() {
        HttpClient httpClient = newClient();

        when(parallel().actions(
                http().client(httpClient).send().post(PATH).message().body("order")
                        .header("X-Source", "wire").build(),
                sequential().actions(
                        http().server(prototype).receive().post(PATH).message().body("order")
                                .header("app.user", "alice")
                                .header("X-Source", "attribute")
                                .validate((message, context) -> org.testng.Assert.assertEquals(
                                        message.getHeader("app.attempt"), 7)).build(),
                        http().server(prototype).send().response(HttpStatus.OK).message()
                                .body("stored").build()).build()));

        then(http().client(httpClient).receive().response(HttpStatus.OK).message().body("stored"));
    }

    @CitrusTest
    public void attributesAreIgnoredWhenDisabled() {
        prototype.stop();
        prototype.setHandleAttributeHeaders(false);
        prototype.start();
        HttpClient httpClient = newClient();

        try {
            when(parallel().actions(
                    http().client(httpClient).send().post(PATH).message().body("order")
                            .header("X-Source", "wire").build(),
                    sequential().actions(
                            http().server(prototype).receive().post(PATH).message().body("order")
                                    .header("X-Source", "wire")
                                    .validate((message, context) -> {
                                        org.testng.Assert.assertNull(message.getHeader("app.user"));
                                        org.testng.Assert.assertNull(message.getHeader("app.attempt"));
                                    }).build(),
                            http().server(prototype).send().response(HttpStatus.OK).message()
                                    .body("stored").build()).build()));

            then(http().client(httpClient).receive().response(HttpStatus.OK).message().body("stored"));
        } finally {
            prototype.stop();
            prototype.setHandleAttributeHeaders(true);
            prototype.start();
        }
    }

    private HttpClient newClient() {
        return new HttpClientBuilder()
                .requestUrl("http://127.0.0.1:" + prototype.getActualPort() + PATH)
                .initialize()
                .build();
    }
}
