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

import io.fabric8.kubernetes.api.model.Pod;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.kubernetes.actions.CreateResourceAction;
import org.citrusframework.yaml.YamlTestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CreateResourceTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadKubernetesActions() {
        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/kubernetes/yaml/create-resource.citrus.it.yaml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("CreateResourceTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(2L, result.getActionCount());
        Assertions.assertEquals(CreateResourceAction.class, result.getTestAction(0).getClass());
        Assertions.assertTrue(result.getTestResult().isSuccess());

        String namespace = "test";
        Pod pod = k8sClient.pods().inNamespace(namespace).withName("my-pod-1").get();
        Assertions.assertNotNull(pod);
        Assertions.assertEquals(1, pod.getMetadata().getLabels().size());
        Assertions.assertEquals("citrus", pod.getMetadata().getLabels().get("test"));

        pod = k8sClient.pods().inNamespace(namespace).withName("my-pod").get();
        Assertions.assertNotNull(pod);
        Assertions.assertEquals(1, pod.getMetadata().getLabels().size());
        Assertions.assertEquals("my-pod", pod.getMetadata().getLabels().get("name"));
    }
}
