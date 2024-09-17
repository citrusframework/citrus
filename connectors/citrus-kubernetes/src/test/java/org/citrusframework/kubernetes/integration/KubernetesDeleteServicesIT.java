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

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.kubernetes.client.KubernetesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.kubernetes.actions.KubernetesActionBuilder.kubernetes;

public class KubernetesDeleteServicesIT extends AbstractKubernetesIT {

    @Autowired
    private KubernetesClient k8sClient;

    private final String namespace = "test";

    @Test
    @CitrusTest
    public void shouldDeleteService() {
        given(context -> {
            Service service = new ServiceBuilder()
                    .withNewMetadata()
                    .withName("my-service")
                    .withNamespace(namespace)
                    .endMetadata()
                    .build();

            k8sClient.getClient().services()
                    .inNamespace(namespace)
                    .resource(service)
                    .create();
        });

        when(kubernetes()
                .client(k8sClient.getClient())
                .services()
                .delete("my-service")
                .inNamespace(namespace));

        then(context -> {
            Service service = k8sClient.getClient().services()
                    .inNamespace(namespace)
                    .withName("my-service")
                    .get();

            Assert.assertNull(service);
        });
    }

}
