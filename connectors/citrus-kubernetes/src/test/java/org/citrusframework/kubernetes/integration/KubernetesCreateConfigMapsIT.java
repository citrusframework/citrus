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

import io.fabric8.kubernetes.api.model.ConfigMap;
import org.apache.commons.codec.binary.Base64;
import org.citrusframework.GherkinTestActionRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.annotations.CitrusTest;
import org.citrusframework.annotations.CitrusResource;
import org.citrusframework.kubernetes.client.KubernetesClient;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class KubernetesCreateConfigMapsIT extends AbstractKubernetesIT implements TestActionSupport {

    @Autowired
    private KubernetesClient k8sClient;

    private final String namespace = "test";

    @Test
    @CitrusTest
    public void shouldCreateConfigMap(@CitrusResource GherkinTestActionRunner runner) {
        runner.given(doFinally().actions(context -> k8sClient.getClient().configMaps()
                .inNamespace(namespace)
                .withName("my-config-map")
                .delete()));

        runner.when(kubernetes()
                .client(k8sClient.getClient())
                .configMaps()
                .create("my-config-map")
                .properties(Collections.singletonMap("foo", "bar"))
                .inNamespace(namespace));

        runner.then(context -> {
            ConfigMap configMap = k8sClient.getClient().configMaps()
                    .inNamespace(namespace)
                    .withName("my-config-map")
                    .get();

            Assertions.assertNotNull(configMap);
            Assertions.assertEquals(1, configMap.getData().size());
            Assertions.assertEquals(Base64.encodeBase64String("bar".getBytes(StandardCharsets.UTF_8)), configMap.getData().get("foo"));
        });
    }

    @Test
    @CitrusTest
    public void shouldCreateConfigMapFromFile(@CitrusResource GherkinTestActionRunner runner) throws IOException {
        runner.given(doFinally().actions(context -> k8sClient.getClient().configMaps()
                .inNamespace(namespace)
                .withName("my-config-map")
                .delete()));

        runner.when(kubernetes()
                .client(k8sClient.getClient())
                .configMaps()
                .create("my-config-map")
                .fromFile("classpath:org/citrusframework/kubernetes/integration/configMap.properties")
                .inNamespace(namespace));

        String configMapContent = FileUtils.readToString(Resources.fromClasspath("configMap.properties", KubernetesCreateConfigMapsIT.class));

        runner.then(context -> {
            ConfigMap configMap = k8sClient.getClient().configMaps()
                    .inNamespace(namespace)
                    .withName("my-config-map")
                    .get();

            Assertions.assertNotNull(configMap);
            Assertions.assertEquals(1, configMap.getData().size());
            Assertions.assertEquals(
                    Base64.encodeBase64String(configMapContent.getBytes(StandardCharsets.UTF_8)), configMap.getData().get("configMap.properties"));
        });
    }

}
