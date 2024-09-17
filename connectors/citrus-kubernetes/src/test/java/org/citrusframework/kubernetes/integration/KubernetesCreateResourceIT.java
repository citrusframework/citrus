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
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.kubernetes.client.KubernetesClient;
import org.citrusframework.spi.Resources;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.kubernetes.actions.KubernetesActionBuilder.kubernetes;

public class KubernetesCreateResourceIT extends AbstractKubernetesIT {

    @Autowired
    private KubernetesClient k8sClient;

    private final String namespace = "test";

    @Test
    @CitrusTest
    public void shouldCreateResource() {
        when(kubernetes()
                .client(k8sClient.getClient())
                .resources()
                .create()
                .content("""
                apiVersion: v1
                kind: Pod
                metadata:
                  name: my-pod
                  labels:
                    test: citrus
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

            Assert.assertNotNull(pod);
            Assert.assertEquals(pod.getMetadata().getLabels().size(), 1);
            Assert.assertEquals(pod.getMetadata().getLabels().get("test"), "citrus");
        });
    }

    @Test
    @CitrusTest
    public void shouldCreateResourceFromFile() {
        when(kubernetes()
                .client(k8sClient.getClient())
                .resources()
                .create()
                .resource(Resources.fromClasspath("pod.yaml", KubernetesCreateResourceIT.class))
                .inNamespace(namespace));

        then(context -> {
            Pod pod = k8sClient.getClient().pods()
                    .inNamespace(namespace)
                    .withName("my-pod")
                    .get();

            Assert.assertNotNull(pod);
            Assert.assertEquals(pod.getMetadata().getLabels().size(), 1);
            Assert.assertEquals(pod.getMetadata().getLabels().get("name"), "my-pod");
        });
    }

}
