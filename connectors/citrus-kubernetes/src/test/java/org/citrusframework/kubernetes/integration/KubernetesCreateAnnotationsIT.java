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

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.kubernetes.client.KubernetesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class KubernetesCreateAnnotationsIT extends AbstractKubernetesIT implements TestActionSupport {

    @Autowired
    private KubernetesClient k8sClient;

    private final String namespace = "test";

    @Test
    @CitrusTest
    public void shouldCreateAnnotations(@CitrusResource GherkinTestActionRunner runner) {
        runner.given(context -> {
            Pod pod = new PodBuilder()
                    .withNewMetadata()
                        .withName("my-pod")
                        .withNamespace(namespace)
                    .endMetadata()
                    .withNewStatus()
                      .withPhase("Running")
                    .endStatus()
                .build();

            k8sClient.getClient().pods()
                    .inNamespace(namespace)
                    .resource(pod)
                    .create();
        });

        runner.when(kubernetes()
                .client(k8sClient.getClient())
                .pods()
                .addAnnotation("my-pod")
                .annotation("test", "citrus")
                .inNamespace(namespace));

        runner.then(context -> {
            Pod pod = k8sClient.getClient().pods()
                    .inNamespace(namespace)
                    .withName("my-pod")
                    .get();

            Assertions.assertNotNull(pod);
            Assertions.assertEquals(1, pod.getMetadata().getAnnotations().size());
            Assertions.assertEquals("citrus", pod.getMetadata().getAnnotations().get("test"));
        });
    }

}
