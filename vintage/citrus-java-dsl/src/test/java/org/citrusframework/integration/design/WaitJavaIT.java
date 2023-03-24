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

package org.citrusframework.integration.design;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.dsl.design.AbstractTestBehavior;
import org.citrusframework.dsl.testng.TestNGCitrusTestDesigner;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpClientBuilder;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.http.server.HttpServerBuilder;
import org.citrusframework.integration.common.FileHelper;
import org.citrusframework.message.MessageType;
import org.citrusframework.util.SocketUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.RequestMethod;
import org.testng.annotations.Test;


@Test
public class WaitJavaIT extends TestNGCitrusTestDesigner {

    private final int serverPort = SocketUtils.findAvailableTcpPort();

    private HttpServer httpServer = new HttpServerBuilder()
            .port(serverPort)
            .timeout(500L)
            .build();

    private HttpClient client = new HttpClientBuilder()
            .requestUrl(String.format("http://localhost:%s/test", serverPort))
            .requestMethod(RequestMethod.GET)
            .build();

    @CitrusTest
    public void waitMessage() {
        //GIVEN
        String messageName = "myTestMessage";

        parallel().actions(
                sequential().actions(
                        //WHEN
                        waitFor()
                                .message()
                                .name(messageName)
                ),
                sequential().actions(
                        //THEN
                        send("channelRequestSender")
                                .messageName(messageName)
                                .payload("Wait for me")
                                .header("Operation", "waitForMe"),

                        receive("channelResponseReceiver")
                                .selector(Collections.singletonMap("Operation", "waitForMe"))
                                .messageType(MessageType.PLAINTEXT)
                                .messageName(messageName)
                                .header("Operation", "waitForMe")
                )
        );
    }

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
            .action(send(client));

        //THEN
        receive(client);

        //THEN
        waitFor()
            .execution()
            .action(send(String.format("http://localhost:%s", serverPort)));

        doFinally().actions(stop(httpServer));
    }

    @CitrusTest
    public void waitHttp() {

        //GIVEN
        start(httpServer);

        //WHEN
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
        File file = FileHelper.createTmpFile();

        applyBehavior(new AbstractTestBehavior() {
            @Override
            public void apply() {
                waitFor()
                        .file()
                        .resource(file);
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
