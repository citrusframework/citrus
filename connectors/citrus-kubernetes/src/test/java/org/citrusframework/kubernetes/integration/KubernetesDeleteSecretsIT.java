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

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.kubernetes.client.KubernetesClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.kubernetes.actions.KubernetesActionBuilder.kubernetes;

public class KubernetesDeleteSecretsIT extends AbstractKubernetesIT {

    @Autowired
    private KubernetesClient k8sClient;

    private final String namespace = "test";

    @Test
    @CitrusTest
    public void shouldDeleteSecret() {
        given(context -> {
            Secret secret = new SecretBuilder()
                    .withNewMetadata()
                    .withName("my-secret")
                    .withNamespace(namespace)
                    .endMetadata()
                    .build();

            k8sClient.getClient().secrets()
                    .inNamespace(namespace)
                    .resource(secret)
                    .create();
        });

        when(kubernetes()
                .client(k8sClient.getClient())
                .secrets()
                .delete("my-secret")
                .inNamespace(namespace));

        then(context -> {
            Secret secret = k8sClient.getClient().secrets()
                    .inNamespace(namespace)
                    .withName("my-secret")
                    .get();

            Assert.assertNull(secret);
        });
    }

}
