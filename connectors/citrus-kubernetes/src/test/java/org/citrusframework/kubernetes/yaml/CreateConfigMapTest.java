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

package org.citrusframework.kubernetes.yaml;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import io.fabric8.kubernetes.api.model.ConfigMap;
import org.apache.commons.codec.binary.Base64;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.kubernetes.actions.CreateConfigMapAction;
import org.citrusframework.kubernetes.integration.KubernetesCreateConfigMapsIT;
import org.citrusframework.spi.Resources;
import org.citrusframework.util.FileUtils;
import org.citrusframework.yaml.YamlTestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CreateConfigMapTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadKubernetesActions() throws IOException {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/kubernetes/yaml/create-config-map.citrus.it.yaml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("CreateConfigMapTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(2L, result.getActionCount());
        Assertions.assertEquals(CreateConfigMapAction.class, result.getTestAction(0).getClass());
        Assertions.assertTrue(result.getTestResult().isSuccess());

        String namespace = "test";
        ConfigMap configMap = k8sClient.configMaps().inNamespace(namespace).withName("my-config-map-1").get();
        Assertions.assertNotNull(configMap);
        Assertions.assertEquals(1, configMap.getData().size());
        Assertions.assertEquals(Base64.encodeBase64String("bar".getBytes(StandardCharsets.UTF_8)), configMap.getData().get("foo"));

        String configMapContent = FileUtils.readToString(Resources.fromClasspath("configMap.properties", KubernetesCreateConfigMapsIT.class));

        configMap = k8sClient.configMaps().inNamespace(namespace).withName("my-config-map-2").get();
        Assertions.assertNotNull(configMap);
        Assertions.assertEquals(1, configMap.getData().size());
        Assertions.assertEquals(
                Base64.encodeBase64String(configMapContent.getBytes(StandardCharsets.UTF_8)), configMap.getData().get("configMap.properties"));
    }
}
