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

package org.citrusframework.kubernetes.xml;

import io.fabric8.kubernetes.api.model.PodBuilder;
import org.citrusframework.TestCase;
import org.citrusframework.kubernetes.actions.DeletePodAction;
import org.citrusframework.xml.XmlTestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DeletePodTest extends AbstractXmlActionTest {

    @Test
    public void shouldDeletePodByName() {
        createPod("my-pod");

        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/kubernetes/xml/delete-pod-by-name.citrus.it.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("DeletePodByNameTest", result.getName());
        Assertions.assertEquals(1L, result.getActionCount());
        Assertions.assertEquals(DeletePodAction.class, result.getTestAction(0).getClass());
        Assertions.assertTrue(result.getTestResult().isSuccess());

        Assertions.assertNull(k8sClient.pods().inNamespace("test").withName("my-pod").get());
    }

    @Test
    public void shouldDeletePodByLabel() {
        k8sClient.pods().inNamespace("test").resource(new PodBuilder()
                .withNewMetadata()
                    .withName("pod-1")
                    .addToLabels("test", "citrus")
                .endMetadata()
                .build()).create();

        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/kubernetes/xml/delete-pod-by-label.citrus.it.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("DeletePodByLabelTest", result.getName());
        Assertions.assertEquals(1L, result.getActionCount());
        Assertions.assertEquals(DeletePodAction.class, result.getTestAction(0).getClass());
        Assertions.assertTrue(result.getTestResult().isSuccess());

        Assertions.assertNull(k8sClient.pods().inNamespace("test").withName("pod-1").get());
    }

    @Test
    public void shouldDeletePodByLabels() {
        context.setVariable("labelValue", "ready");

        k8sClient.pods().inNamespace("test").resource(new PodBuilder()
                .withNewMetadata()
                    .withName("pod-2")
                    .addToLabels("app", "todo")
                    .addToLabels("state", "ready")
                .endMetadata()
                .build()).create();
        k8sClient.pods().inNamespace("test").resource(new PodBuilder()
                .withNewMetadata()
                    .withName("pod-3")
                    .addToLabels("app", "todo")
                    .addToLabels("state", "pending")
                .endMetadata()
                .build()).create();

        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/kubernetes/xml/delete-pod-by-labels.citrus.it.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("DeletePodByLabelsTest", result.getName());
        Assertions.assertEquals(1L, result.getActionCount());
        Assertions.assertEquals(DeletePodAction.class, result.getTestAction(0).getClass());
        Assertions.assertTrue(result.getTestResult().isSuccess());

        Assertions.assertNull(k8sClient.pods().inNamespace("test").withName("pod-2").get());
        Assertions.assertNotNull(k8sClient.pods().inNamespace("test").withName("pod-3").get());
    }

    private void createPod(String name) {
        k8sClient.pods().inNamespace("test").resource(new PodBuilder()
                .withNewMetadata()
                    .withName(name)
                .endMetadata()
                .build()).create();
    }
}
