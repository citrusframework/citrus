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

package org.citrusframework.integration.container;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.concurrent.Executors;

import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.integration.common.FileHelper;
import org.citrusframework.message.MessageType;
import org.citrusframework.spi.Resources;
import org.citrusframework.testng.spring.TestNGCitrusSpringSupport;
import org.citrusframework.util.SocketUtils;
import com.sun.net.httpserver.HttpServer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.citrusframework.actions.ReceiveMessageAction.Builder.receive;
import static org.citrusframework.actions.SendMessageAction.Builder.send;
import static org.citrusframework.actions.SleepAction.Builder.sleep;
import static org.citrusframework.container.Parallel.Builder.parallel;
import static org.citrusframework.container.Sequence.Builder.sequential;
import static org.citrusframework.container.Wait.Builder.waitFor;

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
                .resource(Resources.fromClasspath("citrus.properties").getFile()));
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
