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

package org.citrusframework.knative.xml;

import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.base.annotations.CitrusAnnotations;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpClientBuilder;
import org.citrusframework.http.endpoint.builder.HttpEndpoints;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.knative.actions.eventing.ReceiveEventAction;
import org.citrusframework.knative.actions.eventing.SendEventAction;
import org.citrusframework.spi.BindToRegistry;
import org.citrusframework.util.SocketUtils;
import org.citrusframework.xml.XmlTestLoader;
import org.springframework.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SendReceiveEventTest extends AbstractXmlActionTest {

    private final int port = SocketUtils.findAvailableTcpPort(8081);

    @BindToRegistry
    private final HttpClient knativeClient = new HttpClientBuilder()
            .requestUrl("http://localhost:%d".formatted(port))
            .build();

    @BindToRegistry
    public HttpServer knativeBroker = HttpEndpoints.http()
            .server()
            .port(port)
            .timeout(100)
            .defaultStatus(HttpStatus.ACCEPTED)
            .autoStart(true)
            .build();

    @BeforeEach
    @Override
    public void prepareTest() {
        super.prepareTest();
        CitrusAnnotations.injectAll(this);
    }

    @AfterEach
    public void shutdown() {
        knativeBroker.stop();
    }

    @Test
    public void shouldLoadKnativeActions() {
        context.getReferenceResolver().bind("my-broker", knativeClient);
        context.getReferenceResolver().bind("my-service", knativeBroker);

        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/knative/xml/send-receive-event.citrus.it.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("SendReceiveEventTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(2L, result.getActionCount());
        Assertions.assertEquals(SendEventAction.class, result.getTestAction(0).getClass());
        Assertions.assertEquals(ReceiveEventAction.class, result.getTestAction(1).getClass());
        Assertions.assertTrue(result.getTestResult().isSuccess());
    }
}
