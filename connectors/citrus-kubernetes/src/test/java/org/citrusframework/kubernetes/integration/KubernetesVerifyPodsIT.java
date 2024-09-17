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

import io.fabric8.kubernetes.api.model.ContainerStatusBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodConditionBuilder;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.exceptions.ActionTimeoutException;
import org.citrusframework.kubernetes.client.KubernetesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import static org.citrusframework.container.Assert.Builder.assertException;
import static org.citrusframework.kubernetes.actions.KubernetesActionBuilder.kubernetes;

public class KubernetesVerifyPodsIT extends AbstractKubernetesIT {

    @Autowired
    private KubernetesClient k8sClient;

    private final String namespace = "test";

    @Test
    @CitrusTest
    public void shouldVerifyPodStatus() {
        given(context -> {
            Pod pod = new PodBuilder()
                    .withNewMetadata()
                        .withName("my-pod")
                        .withNamespace(namespace)
                    .endMetadata()
                    .withNewStatus()
                      .withPhase("Running")
                      .withConditions(new PodConditionBuilder()
                              .withType("Ready")
                              .withStatus("true")
                              .build())
                      .withContainerStatuses(new ContainerStatusBuilder()
                              .withName("container")
                              .withReady(true)
                              .build())
                    .endStatus()
                .build();

            k8sClient.getClient().pods()
                    .inNamespace(namespace)
                    .resource(pod)
                    .create();
        });

        when(kubernetes()
                .client(k8sClient.getClient())
                .pods()
                .verify("my-pod")
                .inNamespace(namespace)
                .isRunning()
                .maxAttempts(2));
    }

    @Test
    @CitrusTest
    public void shouldFailValidationForContainerStatus() {
        given(context -> {
            Pod pod = new PodBuilder()
                    .withNewMetadata()
                        .withName("my-pod")
                        .withNamespace(namespace)
                    .endMetadata()
                    .withNewStatus()
                      .withPhase("Running")
                      .withConditions(new PodConditionBuilder()
                              .withType("Ready")
                              .withStatus("true")
                              .build())
                      .withContainerStatuses(new ContainerStatusBuilder()
                              .withName("container")
                              .withReady(false)
                              .build())
                    .endStatus()
                .build();

            k8sClient.getClient().pods()
                    .inNamespace(namespace)
                    .resource(pod)
                    .create();
        });

        then(assertException()
                .exception(ActionTimeoutException.class)
                .when(kubernetes()
                    .client(k8sClient.getClient())
                    .pods()
                    .verify("my-pod")
                    .inNamespace(namespace)
                    .isRunning()
                    .maxAttempts(2)));
    }

}
