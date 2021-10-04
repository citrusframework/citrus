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

package com.consol.citrus.integration.container;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.concurrent.Executors;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.integration.common.FileHelper;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.testng.spring.TestNGCitrusSpringSupport;
import com.sun.net.httpserver.HttpServer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.SocketUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.consol.citrus.actions.ReceiveMessageAction.Builder.receive;
import static com.consol.citrus.actions.SendMessageAction.Builder.send;
import static com.consol.citrus.actions.SleepAction.Builder.sleep;
import static com.consol.citrus.container.Parallel.Builder.parallel;
import static com.consol.citrus.container.Sequence.Builder.sequential;
import static com.consol.citrus.container.Wait.Builder.waitFor;

@Test
public class WaitJavaIT extends TestNGCitrusSpringSupport {

    private final int serverPort = SocketUtils.findAvailableTcpPort();
    private HttpServer server;

    @BeforeClass
    public void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", serverPort), 0);
        server.createContext("/test", httpExchange -> httpExchange.sendResponseHeaders(200, 0));
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();
    }

    @AfterClass(alwaysRun = true)
    public void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @CitrusTest
    public void waitMessage() {
        //GIVEN
        String messageName = "myTestMessage";

        run(parallel().actions(
                sequential().actions(
                        //WHEN
                        waitFor()
                                .message()
                                .name(messageName)
                ),
                sequential().actions(
                        //THEN
                        send("direct:waitQueue")
                                .message()
                                .name(messageName)
                                .body("Wait for me")
                                .header("Operation", "waitForMe"),

                        receive("direct:waitQueue")
                                .selector(Collections.singletonMap("Operation", "waitForMe"))
                                .message()
                                    .type(MessageType.PLAINTEXT)
                                    .name(messageName)
                                    .body("Wait for me")
                                    .header("Operation", "waitForMe")
                )
        ));
    }

    @CitrusTest
    public void waitFile() throws IOException {
        run(waitFor()
                .file()
                .resource(new ClassPathResource("citrus.properties").getFile()));
    }

    @CitrusTest
    public void waitHttp() {
        run(waitFor()
                .http()
                .url(String.format("http://localhost:%s/test", serverPort)));
    }

    @CitrusTest
    public void waitAction() {
        run(waitFor()
                .execution()
                .interval(300L)
                .milliseconds(500L)
                .action(sleep().milliseconds(250L)));
    }

    @CitrusTest
    public void waitForFileUsingResource() {
        File file = FileHelper.createTmpFile();

        run(waitFor()
                .file()
                .resource(file));
    }

    @CitrusTest
    public void waitForFileUsingPath() {
        File file = FileHelper.createTmpFile();

        run(waitFor()
                .file()
                .path(file.toURI().toString()));
    }
}
