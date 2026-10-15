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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpClientBuilder;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.vertx.VertxHttpHandlerMapping;
import org.citrusframework.http.vertx.VertxHttpInterceptor;
import org.citrusframework.http.vertx.VertxHttpServer;
import org.citrusframework.http.vertx.VertxMappedInterceptor;
import org.citrusframework.message.Message;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Interceptors run around custom handlers and the Citrus adapter alike, mirroring the shared
 * handler-mapping interceptor chain: a path-scoped interceptor mutates matching requests and
 * wraps their replies, skips other paths, and completion is recorded for every exchange.
 */
@Test
public class VertxHttpInterceptorIT extends TestNGCitrusSpringSupport implements TestActionSupport {

    private VertxHttpServer prototype;
    private final List<String> completions = Collections.synchronizedList(new ArrayList<>());

    @BeforeClass
    public void startPrototype() {
        VertxHttpInterceptor scoped = VertxMappedInterceptor.include(List.of("/api/**", "/custom"),
                new VertxHttpInterceptor() {
                    @Override
                    public Message preHandle(HttpMessage request) {
                        request.setHeader("X-Intercepted", "yes");
                        return null;
                    }

                    @Override
                    public Message postHandle(HttpMessage request, Message reply) {
                        HttpMessage wrapped = new HttpMessage(reply);
                        wrapped.setHeader("X-Wrapped", "yes");
                        return wrapped;
                    }
                });
        VertxHttpInterceptor recorder = new VertxHttpInterceptor() {
            @Override
            public void afterCompletion(HttpMessage request, Message reply, Exception error) {
                completions.add(request.getPath() + "|" + (error == null ? "ok" : "error"));
            }
        };

        prototype = new VertxHttpServer();
        prototype.setName("vertxInterceptors");
        prototype.setPort(0);
        prototype.setInterceptors(List.<Object>of(scoped, recorder));
        prototype.setHandlers(List.of(new VertxHttpHandlerMapping(
                request -> request.getPath().equals("/custom"),
                request -> new HttpMessage("custom").status(HttpStatus.OK))));
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
    public void scopedInterceptorFiresForAdapterTraffic() {
        HttpClient httpClient = newClient();

        when(parallel().actions(
                http().client(httpClient).send().post("/api/orders").message().body("order").build(),
                sequential().actions(
                        http().server(prototype).receive().post("/api/orders").message().body("order")
                                .header("X-Intercepted", "yes").build(),
                        http().server(prototype).send().response(HttpStatus.OK).message()
                                .body("stored").build()).build()));

        then(http().client(httpClient).receive().response(HttpStatus.OK).message().body("stored")
                .header("X-Wrapped", "yes"));

        org.testng.Assert.assertTrue(completions.remove("/api/orders|ok"));
    }

    @CitrusTest
    public void scopedInterceptorSkipsOtherPaths() {
        HttpClient httpClient = newClient();

        when(parallel().actions(
                http().client(httpClient).send().post("/other").message().body("order").build(),
                sequential().actions(
                        http().server(prototype).receive().post("/other").message().body("order")
                                .validate((message, context) -> org.testng.Assert.assertNull(
                                        message.getHeader("X-Intercepted"))).build(),
                        http().server(prototype).send().response(HttpStatus.OK).message()
                                .body("stored").build()).build()));

        then(http().client(httpClient).receive().response(HttpStatus.OK).message().body("stored")
                .validate((message, context) -> org.testng.Assert.assertNull(
                        message.getHeader("X-Wrapped"))));

        org.testng.Assert.assertTrue(completions.remove("/other|ok"));
    }

    @CitrusTest
    public void interceptorFiresForCustomHandlerTraffic() {
        HttpClient httpClient = newClient();

        when(http().client(httpClient).send().get("/custom"));

        then(http().client(httpClient).receive().response(HttpStatus.OK).message().body("custom")
                .header("X-Wrapped", "yes"));

        org.testng.Assert.assertTrue(completions.remove("/custom|ok"));
    }

    private HttpClient newClient() {
        return new HttpClientBuilder()
                .requestUrl("http://127.0.0.1:" + prototype.getActualPort())
                .initialize()
                .build();
    }
}
