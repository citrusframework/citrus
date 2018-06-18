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

package com.consol.citrus.javadsl.design;

import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.dsl.design.AbstractTestBehavior;
import com.consol.citrus.dsl.testng.TestNGCitrusTestDesigner;
import com.consol.citrus.endpoint.adapter.EmptyResponseEndpointAdapter;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.http.config.annotation.HttpServerConfig;
import com.consol.citrus.http.server.HttpServer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.SocketUtils;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * @author Christoph Deppisch
 */
@Test
public class WaitJavaIT extends TestNGCitrusTestDesigner {

    /** Random http server port */
    private final static int serverPort = SocketUtils.findAvailableTcpPort();

    @CitrusEndpoint(name = "waitHttpServer")
    @HttpServerConfig
    private HttpServer httpServer;

    @CitrusTest
    public void waitFile() throws IOException {
        waitFor()
            .file(new ClassPathResource("citrus.properties").getFile());
    }

    @CitrusTest
    public void waitHttp() {
        httpServer.setPort(serverPort);
        httpServer.setEndpointAdapter(new EmptyResponseEndpointAdapter());

        start(httpServer);

        waitFor()
            .http(String.format("http://localhost:%s", serverPort));

        waitFor()
            .execution()
            .action(send(String.format("http://localhost:%s", serverPort)));

        doFinally().actions(stop(httpServer));
    }

    @CitrusTest
    public void waitAction() {
        waitFor()
            .execution()
            .interval(300L)
            .ms(500L)
            .action(sleep(250L));
    }

    @CitrusTest
    public void waitBehavior() {
        applyBehavior(new AbstractTestBehavior() {
            @Override
            public void apply() {
                try {
                    waitFor()
                        .file(new ClassPathResource("citrus.properties").getFile());
                } catch (IOException e) {
                    throw new CitrusRuntimeException(e);
                }
            }
        });
    }
}