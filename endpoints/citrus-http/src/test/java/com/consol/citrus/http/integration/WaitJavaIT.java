/*
 * Copyright 2006-2018 the original author or authors.
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

package com.consol.citrus.http.integration;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.client.HttpClientBuilder;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.http.server.HttpServerBuilder;
import com.consol.citrus.testng.spring.TestNGCitrusSpringSupport;
import org.springframework.http.HttpMethod;
import org.springframework.util.SocketUtils;
import org.testng.annotations.Test;

import static com.consol.citrus.actions.ReceiveMessageAction.Builder.receive;
import static com.consol.citrus.actions.SendMessageAction.Builder.send;
import static com.consol.citrus.actions.StartServerAction.Builder.start;
import static com.consol.citrus.actions.StopServerAction.Builder.stop;
import static com.consol.citrus.container.FinallySequence.Builder.doFinally;
import static com.consol.citrus.container.Wait.Builder.waitFor;


@Test
public class WaitJavaIT extends TestNGCitrusSpringSupport {

    private final int serverPort = SocketUtils.findAvailableTcpPort();

    private HttpServer httpServer = new HttpServerBuilder()
            .port(serverPort)
            .timeout(500L)
            .build();

    private HttpClient client = new HttpClientBuilder()
            .requestUrl(String.format("http://localhost:%s/test", serverPort))
            .requestMethod(HttpMethod.GET)
            .build();

    @CitrusTest
    public void waitHttpAsAction() {

        //GIVEN
        given(start(httpServer));

        then(doFinally().actions(stop(httpServer)));

        //WHEN
        when(waitFor()
                .execution()
                .action(send(client)));

        //THEN
        then(receive(client));

        //THEN
        then(waitFor()
                .execution()
                .action(send(String.format("http://localhost:%s/test", serverPort))));
    }

    @CitrusTest
    public void waitHttp() {

        //GIVEN
        given(start(httpServer));

        then(doFinally().actions(stop(httpServer)));

        then(waitFor()
            .http()
            .url(String.format("http://localhost:%s/test", serverPort)));
    }
}
