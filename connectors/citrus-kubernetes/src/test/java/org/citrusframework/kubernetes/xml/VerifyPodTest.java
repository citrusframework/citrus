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

import java.util.Collections;

import io.fabric8.kubernetes.api.model.ContainerStatusBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.PodConditionBuilder;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.kubernetes.actions.VerifyPodAction;
import org.citrusframework.xml.XmlTestLoader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class VerifyPodTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadKubernetesActions() {
        String namespace = "test";
        Pod pod = new PodBuilder()
            .withNewMetadata()
                .withName("my-pod")
                .withNamespace(namespace)
                .withLabels(Collections.singletonMap("test", "citrus"))
            .endMetadata()
            .withNewStatus()
              .withPhase("Running")
              .withConditions(new PodConditionBuilder()
                      .withType("Ready")
                      .withStatus("true")
                      .build())
              .withContainerStatuses(new ContainerStatusBuilder()
                      .withName("container")
                      .withReady(true)
                      .build())
            .endStatus()
        .build();

        k8sClient.pods()
                .inNamespace(namespace)
                .resource(pod)
                .create();

        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/kubernetes/xml/verify-pod-test.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "VerifyPodTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 3L);
        Assert.assertEquals(result.getTestAction(0).getClass(), VerifyPodAction.class);
        Assert.assertTrue(result.getTestResult().isSuccess());
    }
}
