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

import io.fabric8.kubernetes.api.model.Pod;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.kubernetes.actions.CreateResourceAction;
import org.citrusframework.xml.XmlTestLoader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CreateResourceTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadKubernetesActions() {
        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/kubernetes/xml/create-resource-test.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "CreateResourceTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 2L);
        Assert.assertEquals(result.getTestAction(0).getClass(), CreateResourceAction.class);
        Assert.assertTrue(result.getTestResult().isSuccess());

        String namespace = "test";
        Pod pod = k8sClient.pods().inNamespace(namespace).withName("my-pod-1").get();
        Assert.assertNotNull(pod);
        Assert.assertEquals(pod.getMetadata().getLabels().size(), 1);
        Assert.assertEquals(pod.getMetadata().getLabels().get("test"), "citrus");

        pod = k8sClient.pods().inNamespace(namespace).withName("my-pod").get();
        Assert.assertNotNull(pod);
        Assert.assertEquals(pod.getMetadata().getLabels().size(), 1);
        Assert.assertEquals(pod.getMetadata().getLabels().get("name"), "my-pod");
    }
}
