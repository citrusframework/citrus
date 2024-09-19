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
import org.citrusframework.annotations.CitrusAnnotations;
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
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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

    @BeforeClass
    @Override
    public void setupMocks() {
        super.setupMocks();
        CitrusAnnotations.injectAll(this);
    }

    @AfterClass(alwaysRun = true)
    public void shutdown() {
        knativeBroker.stop();
    }

    @Test
    public void shouldLoadKnativeActions() {
        context.getReferenceResolver().bind("my-broker", knativeClient);
        context.getReferenceResolver().bind("my-service", knativeBroker);

        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/knative/xml/send-receive-event-test.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "SendReceiveEventTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 2L);
        Assert.assertEquals(result.getTestAction(0).getClass(), SendEventAction.class);
        Assert.assertEquals(result.getTestAction(1).getClass(), ReceiveEventAction.class);
        Assert.assertTrue(result.getTestResult().isSuccess());
    }
}
