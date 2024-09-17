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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import io.fabric8.kubernetes.api.model.Secret;
import org.apache.commons.codec.binary.Base64;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.kubernetes.client.KubernetesClient;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.container.FinallySequence.Builder.doFinally;
import static org.citrusframework.kubernetes.actions.KubernetesActionBuilder.kubernetes;

public class KubernetesCreateSecretsIT extends AbstractKubernetesIT {

    @Autowired
    private KubernetesClient k8sClient;

    private final String namespace = "test";

    @Test
    @CitrusTest
    public void shouldCreateSecret() {
        given(doFinally().actions(context -> k8sClient.getClient().secrets()
                .inNamespace(namespace)
                .withName("my-secret")
                .delete()));

        when(kubernetes()
                .client(k8sClient.getClient())
                .secrets()
                .create("my-secret")
                .properties(Collections.singletonMap("foo", "bar"))
                .inNamespace(namespace));

        then(context -> {
            Secret secret = k8sClient.getClient().secrets()
                    .inNamespace(namespace)
                    .withName("my-secret")
                    .get();

            Assert.assertNotNull(secret);
            Assert.assertEquals(secret.getData().size(), 1);
            Assert.assertEquals(secret.getData().get("foo"), Base64.encodeBase64String("bar".getBytes(StandardCharsets.UTF_8)));
        });
    }

    @Test
    @CitrusTest
    public void shouldCreateSecretFromFile() throws IOException {
        given(doFinally().actions(context -> k8sClient.getClient().secrets()
                .inNamespace(namespace)
                .withName("my-secret")
                .delete()));

        when(kubernetes()
                .client(k8sClient.getClient())
                .secrets()
                .create("my-secret")
                .fromFile("classpath:org/citrusframework/kubernetes/integration/secret.properties")
                .inNamespace(namespace));

        String secretContent = FileUtils.readToString(Resources.fromClasspath("secret.properties", KubernetesCreateSecretsIT.class));

        then(context -> {
            Secret secret = k8sClient.getClient().secrets()
                    .inNamespace(namespace)
                    .withName("my-secret")
                    .get();

            Assert.assertNotNull(secret);
            Assert.assertEquals(secret.getData().size(), 1);
            Assert.assertEquals(secret.getData().get("secret.properties"),
                    Base64.encodeBase64String(secretContent.getBytes(StandardCharsets.UTF_8)));
        });
    }

}
