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

import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.ContainerPortBuilder;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.kubernetes.actions.DeleteResourceAction;
import org.citrusframework.xml.XmlTestLoader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DeleteResourceTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadKubernetesActions() {
        String namespace = "test";
        Pod pod = new PodBuilder()
                .withNewMetadata()
                    .withName("my-pod")
                    .withNamespace(namespace)
                .endMetadata()
                .withNewSpec()
                .withContainers(new ContainerBuilder()
                        .withName("nginx")
                        .withImage("nginx")
                        .withPorts(new ContainerPortBuilder()
                                .withContainerPort(80)
                                .build())
                        .build())
                .endSpec()
                .withNewStatus()
                    .withPhase("Running")
                .endStatus()
                .build();

        k8sClient.pods()
                .inNamespace(namespace)
                .resource(pod)
                .create();

        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/kubernetes/xml/delete-resource-test.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "DeleteResourceTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 1L);
        Assert.assertEquals(result.getTestAction(0).getClass(), DeleteResourceAction.class);
        Assert.assertTrue(result.getTestResult().isSuccess());

        pod = k8sClient.pods().inNamespace(namespace).withName("my-pod").get();
        Assert.assertNull(pod);
    }
}
