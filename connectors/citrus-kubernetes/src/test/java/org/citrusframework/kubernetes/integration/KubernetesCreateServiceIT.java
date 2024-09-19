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

package org.citrusframework.kubernetes.integration;

import java.util.Collections;

import io.fabric8.kubernetes.api.model.Service;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.kubernetes.client.KubernetesClient;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.kubernetes.actions.KubernetesActionBuilder.kubernetes;

public class KubernetesCreateServiceIT extends AbstractKubernetesIT {

    @Autowired
    private KubernetesClient k8sClient;

    @Mock
    private HttpServer service;

    private final String namespace = "test";

    @Test
    @CitrusTest
    public void shouldCreateService() {
        when(kubernetes()
                .client(k8sClient.getClient())
                .services()
                .create("my-service")
                .server(service)
                .inNamespace(namespace));

        then(context -> {
            Service service = k8sClient.getClient().services()
                    .inNamespace(namespace)
                    .withName("my-service")
                    .get();

            Assert.assertNotNull(service);
            Assert.assertEquals(service.getMetadata().getLabels().size(), 1);
            Assert.assertEquals(service.getMetadata().getLabels().get("app"), "citrus");
            Assert.assertEquals(service.getSpec().getPorts().size(), 1);
            Assert.assertEquals(service.getSpec().getPorts().get(0).getPort(), 80);
            Assert.assertEquals(service.getSpec().getPorts().get(0).getTargetPort().getIntVal(), 8080);
            Assert.assertEquals(service.getSpec().getSelector().size(), 1);
            Assert.assertEquals(service.getSpec().getSelector().get("citrusframework.org/test-id"),
                    "KubernetesCreateServiceIT.shouldCreateService");
        });
    }
    @Test
    @CitrusTest
    public void shouldCreateServiceWithCustomPortMapping() {
        when(kubernetes()
                .client(k8sClient.getClient())
                .services()
                .create("my-service")
                .server(service)
                .portMapping(80, 8888)
                .withPodSelector(Collections.singletonMap("test", "citrus"))
                .inNamespace(namespace));

        then(context -> {
            Service service = k8sClient.getClient().services()
                    .inNamespace(namespace)
                    .withName("my-service")
                    .get();

            Assert.assertNotNull(service);
            Assert.assertEquals(service.getMetadata().getLabels().size(), 1);
            Assert.assertEquals(service.getMetadata().getLabels().get("app"), "citrus");
            Assert.assertEquals(service.getSpec().getPorts().size(), 1);
            Assert.assertEquals(service.getSpec().getPorts().get(0).getPort(), 80);
            Assert.assertEquals(service.getSpec().getPorts().get(0).getTargetPort().getIntVal(), 8888);
            Assert.assertEquals(service.getSpec().getSelector().size(), 1);
            Assert.assertEquals(service.getSpec().getSelector().get("test"), "citrus");
        });
    }

}
