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

package com.consol.citrus.integration.runner;

import java.io.File;
import java.io.IOException;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.runner.AbstractTestBehavior;
import com.consol.citrus.dsl.testng.TestNGCitrusTestRunner;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.client.HttpClientBuilder;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.http.server.HttpServerBuilder;
import com.consol.citrus.integration.common.FileHelper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.util.SocketUtils;
import org.testng.annotations.Test;

@Test
public class WaitTestRunnerIT extends TestNGCitrusTestRunner {

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
    public void waitFile() throws IOException {
        waitFor()
                .file()
                .resource(new ClassPathResource("citrus.properties").getFile());
    }

    @CitrusTest
    public void waitHttpAsAction() {
        //GIVEN
        start(httpServer);

        //WHEN
        waitFor()
            .execution()
            .action(send(action -> action.endpoint(client)));

        //THEN
        receive(action -> action.endpoint(client));

        //THEN
        waitFor()
            .execution()
            .action(send(http -> http.endpoint(String.format("http://localhost:%s/test", serverPort))));

        doFinally().actions(stop(httpServer));
    }

    @CitrusTest
    public void waitHttp() {
        //GIVEN
        start(httpServer);

        //THEN
        waitFor()
            .http()
            .url(String.format("http://localhost:%s", serverPort));

        doFinally().actions(stop(httpServer));
    }

    @CitrusTest
    public void waitAction() {
        waitFor()
                .execution()
                .interval(300L)
                .milliseconds(500L)
                .action(sleep(250L));
    }

    @CitrusTest
    public void waitForFileUsingResource() {
        applyBehavior(new AbstractTestBehavior() {
            @Override
            public void apply() {
                try {
                    waitFor()
                            .file()
                            .resource(new ClassPathResource("citrus.properties").getFile());
                } catch (IOException e) {
                    throw new CitrusRuntimeException(e);
                }
            }
        });
    }

    @CitrusTest
    public void waitForFileUsingPath() {
        File file = FileHelper.createTmpFile();

        applyBehavior(new AbstractTestBehavior() {
            @Override
            public void apply() {
                waitFor()
                        .file()
                        .path(file.toURI().toString());
            }
        });
    }
}
