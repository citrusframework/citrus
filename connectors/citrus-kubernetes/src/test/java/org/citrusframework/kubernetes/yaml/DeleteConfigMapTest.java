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

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.kubernetes.actions.DeleteConfigMapAction;
import org.citrusframework.yaml.YamlTestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DeleteConfigMapTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadKubernetesActions() {
        String namespace = "test";
        ConfigMap configMap = new ConfigMapBuilder()
                .withNewMetadata()
                    .withName("my-config-map")
                    .withNamespace(namespace)
                .endMetadata()
                .build();

        k8sClient.configMaps()
                .inNamespace(namespace)
                .resource(configMap)
                .create();

        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/kubernetes/yaml/delete-config-map.citrus.it.yaml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("DeleteConfigMapTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(1L, result.getActionCount());
        Assertions.assertEquals(DeleteConfigMapAction.class, result.getTestAction(0).getClass());
        Assertions.assertTrue(result.getTestResult().isSuccess());

        configMap = k8sClient.configMaps().inNamespace(namespace).withName("my-config-map").get();
        Assertions.assertNull(configMap);
    }
}
