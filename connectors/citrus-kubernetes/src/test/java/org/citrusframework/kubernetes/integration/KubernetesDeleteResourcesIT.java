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

import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.kubernetes.client.KubernetesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.kubernetes.actions.KubernetesActionBuilder.kubernetes;

public class KubernetesDeleteResourcesIT extends AbstractKubernetesIT {

    @Autowired
    private KubernetesClient k8sClient;

    private final String namespace = "test";

    @Test
    @CitrusTest
    public void shouldDeleteResource() {
        given(context -> {
            Pod pod = new PodBuilder()
                    .withNewMetadata()
                        .withName("my-pod")
                        .withNamespace(namespace)
                    .endMetadata()
                    .withNewSpec()
                    .withContainers(new ContainerBuilder()
                            .withName("nginx")
                            .withImage("nginx")
                            .withPorts(new ContainerPortBuilder()
                                    .withContainerPort(80)
                                    .build())
                            .build())
                    .endSpec()
                    .withNewStatus()
                        .withPhase("Running")
                    .endStatus()
                    .build();

            k8sClient.getClient().pods()
                    .inNamespace(namespace)
                    .resource(pod)
                    .create();
        });

        when(kubernetes()
                .client(k8sClient.getClient())
                .resources()
                .delete("""
                apiVersion: v1
                kind: Pod
                metadata:
                  name: my-pod
                spec:
                  containers:
                    - name: nginx
                      image: nginx
                      ports:
                        - containerPort: 80
                """)
                .inNamespace(namespace));

        then(context -> {
            Pod pod = k8sClient.getClient().pods()
                    .inNamespace(namespace)
                    .withName("my-pod")
                    .get();

            Assert.assertNull(pod);
        });
    }

}
