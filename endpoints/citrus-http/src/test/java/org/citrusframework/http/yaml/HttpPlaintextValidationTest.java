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

package org.citrusframework.http.yaml;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import org.citrusframework.exceptions.TestCaseFailedException;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.message.HttpMessage;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.endpoint.EndpointAdapter;
import org.citrusframework.endpoint.direct.DirectEndpointAdapter;
import org.citrusframework.endpoint.direct.DirectSyncEndpointConfiguration;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageQueue;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.util.SocketUtils;
import org.citrusframework.validation.DefaultMessageHeaderValidator;
import org.citrusframework.validation.DefaultTextEqualsMessageValidator;
import org.citrusframework.yaml.YamlTestLoader;
import org.springframework.http.HttpStatus;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.citrusframework.http.endpoint.builder.HttpEndpoints.http;

/**
 * Reproduction test for GitHub issue #1734.
 */
public class HttpPlaintextValidationTest extends AbstractYamlActionTest {

    @BindToRegistry
    private final DefaultMessageHeaderValidator headerValidator = new DefaultMessageHeaderValidator();

    @BindToRegistry
    private final DefaultTextEqualsMessageValidator validator = new DefaultTextEqualsMessageValidator().enableTrim();

    private final int port = SocketUtils.findAvailableTcpPort(8080);
    private final String uri = "http://localhost:" + port + "/test";

    private HttpServer httpServer;
    private HttpClient httpClient;

    private final MessageQueue inboundQueue = new DefaultMessageQueue("inboundQueue");
    private final Queue<HttpMessage> responses = new ArrayBlockingQueue<>(6);

    @BeforeClass
    public void setupEndpoints() {
        EndpointAdapter endpointAdapter = new DirectEndpointAdapter(new DirectSyncEndpointConfiguration()) {
            @Override
            public Message handleMessageInternal(Message request) {
                inboundQueue.send(request);
                return responses.isEmpty() ? new HttpMessage().status(HttpStatus.OK) : responses.remove();
            }
        };

        httpServer = http().server()
                .port(port)
                .timeout(500L)
                .endpointAdapter(endpointAdapter)
                .autoStart(true)
                .name("httpServer")
                .build();
        httpServer.initialize();

        httpClient = http().client()
                .requestUrl(uri)
                .name("httpClient")
                .build();
    }

    @AfterClass(alwaysRun = true)
    public void cleanupEndpoints() {
        if (httpServer != null) {
            httpServer.stop();
        }
    }

    @Test(expectedExceptions = { TestCaseFailedException.class })
    public void shouldFailPlaintextContainsValidation() {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/http/yaml/http-plaintext-validation.citrus.it.yaml");

        context.getReferenceResolver().bind("httpClient", httpClient);
        context.getReferenceResolver().bind("httpServer", httpServer);

        responses.add(new HttpMessage("{\"models\":[]}").status(HttpStatus.OK));

        testLoader.load();
    }
}
