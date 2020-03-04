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

package com.consol.citrus.integration.design;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.design.AbstractTestBehavior;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.http.config.annotation.HttpServerConfig;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.integration.common.FileHelper;
import com.consol.citrus.message.MessageType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.util.SocketUtils;
import org.testng.annotations.Test;


@Test
public class WaitJavaIT extends TestNGCitrusTestDesigner {

    @CitrusEndpoint(name = "waitHttpServer")
    @HttpServerConfig
    private HttpServer httpServer;

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
        String server = startHttpServerAndGetUrl();

        parallel().actions(
                sequential().actions(
                        //WHEN
                        waitFor()
                                .execution()
                                .action(send(server))
                ),
                sequential().actions(
                        //THEN
                        http().server(httpServer).receive().post(),
                        http().server(httpServer).receive().post(),
                        http().server(httpServer).respond(HttpStatus.OK)
                )
        );

        doFinally().actions(stop(httpServer));
    }

    @CitrusTest
    public void waitHttp() {

        //GIVEN
        String server = startHttpServerAndGetUrl();

        parallel().actions(
                sequential().actions(
                        //WHEN
                        waitFor()
                                .http()
                                .url(server)
                ),
                sequential().actions(
                        //THEN
                        http().server(httpServer).receive().head(),
                        http().server(httpServer).respond(HttpStatus.NOT_FOUND),
                        http().server(httpServer).receive().head(),
                        http().server(httpServer).respond(HttpStatus.OK)
                )
        );

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

    private String startHttpServerAndGetUrl() {
        final int serverPort = SocketUtils.findAvailableTcpPort();
        String server = String.format("http://localhost:%s", serverPort);
        httpServer.setPort(serverPort);
        start(httpServer);
        return server;
    }
}
