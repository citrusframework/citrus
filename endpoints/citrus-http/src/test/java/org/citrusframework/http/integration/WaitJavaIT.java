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

import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpClientBuilder;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.http.server.HttpServerBuilder;
import org.citrusframework.junit.jupiter.spring.CitrusSpringSupport;
import org.citrusframework.spring.config.CitrusSpringConfig;
import org.citrusframework.util.SocketUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.junit.jupiter.api.Test;

@CitrusSpringSupport
@ContextConfiguration(classes = CitrusSpringConfig.class)
public class WaitJavaIT implements TestActionSupport {

    private final int serverPort = SocketUtils.findAvailableTcpPort();

    private HttpServer httpServer = new HttpServerBuilder()
            .port(serverPort)
            .timeout(500L)
            .build();

    private HttpClient client = new HttpClientBuilder()
            .requestUrl(String.format("http://localhost:%s/test", serverPort))
            .requestMethod(RequestMethod.GET)
            .build();

    @Test
    @CitrusTest
    public void waitHttpAsAction(@CitrusResource GherkinTestActionRunner runner) {

        //GIVEN
        runner.given(start(httpServer));

        runner.then(doFinally().actions(stop(httpServer)));

        //WHEN
        runner.when(waitFor()
                .execution()
                .action(send(client)));

        //THEN
        runner.then(receive(client));

        //THEN
        runner.then(waitFor()
                .execution()
                .action(send(String.format("http://localhost:%s/test", serverPort))));
    }

    @Test
    @CitrusTest
    public void waitHttp(@CitrusResource GherkinTestActionRunner runner) {

        //GIVEN
        runner.given(start(httpServer));

        runner.then(doFinally().actions(stop(httpServer)));

        runner.then(waitFor()
            .http()
            .url(String.format("http://localhost:%s/test", serverPort)));
    }
}
