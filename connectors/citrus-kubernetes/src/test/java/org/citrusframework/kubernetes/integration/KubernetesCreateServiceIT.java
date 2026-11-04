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
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.kubernetes.client.KubernetesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class KubernetesCreateServiceIT extends AbstractKubernetesIT implements TestActionSupport {

    @Autowired
    private KubernetesClient k8sClient;

    private final String namespace = "test";

    @Test
    @CitrusTest
    public void shouldCreateService(@CitrusResource GherkinTestActionRunner runner) {
        runner.when(kubernetes()
                .client(k8sClient.getClient())
                .services()
                .create("my-service")
                .inNamespace(namespace));

        runner.then(context -> {
            Service service = k8sClient.getClient().services()
                    .inNamespace(namespace)
                    .withName("my-service")
                    .get();

            Assertions.assertNotNull(service);
            Assertions.assertEquals(1, service.getMetadata().getLabels().size());
            Assertions.assertEquals("citrus", service.getMetadata().getLabels().get("app"));
            Assertions.assertEquals(1, service.getSpec().getPorts().size());
            Assertions.assertEquals(80, service.getSpec().getPorts().get(0).getPort());
            Assertions.assertEquals(8080, service.getSpec().getPorts().get(0).getTargetPort().getIntVal());
            Assertions.assertEquals(1, service.getSpec().getSelector().size());
            Assertions.assertEquals(
                    "KubernetesCreateServiceIT.shouldCreateService", service.getSpec().getSelector().get("citrusframework.org/test-id"));
        });
    }
    @Test
    @CitrusTest
    public void shouldCreateServiceWithCustomPortMapping(@CitrusResource GherkinTestActionRunner runner) {
        runner.when(kubernetes()
                .client(k8sClient.getClient())
                .services()
                .create("my-service")
                .portMapping(80, 8888)
                .withPodSelector(Collections.singletonMap("test", "citrus"))
                .inNamespace(namespace));

        runner.then(context -> {
            Service service = k8sClient.getClient().services()
                    .inNamespace(namespace)
                    .withName("my-service")
                    .get();

            Assertions.assertNotNull(service);
            Assertions.assertEquals(1, service.getMetadata().getLabels().size());
            Assertions.assertEquals("citrus", service.getMetadata().getLabels().get("app"));
            Assertions.assertEquals(1, service.getSpec().getPorts().size());
            Assertions.assertEquals(80, service.getSpec().getPorts().get(0).getPort());
            Assertions.assertEquals(8888, service.getSpec().getPorts().get(0).getTargetPort().getIntVal());
            Assertions.assertEquals(1, service.getSpec().getSelector().size());
            Assertions.assertEquals("citrus", service.getSpec().getSelector().get("test"));
        });
    }

}
