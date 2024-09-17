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

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.kubernetes.actions.DeleteServiceAction;
import org.citrusframework.yaml.YamlTestLoader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DeleteServiceTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadKubernetesActions() {
        String namespace = "test";
        Service service = new ServiceBuilder()
                .withNewMetadata()
                .withName("my-service")
                .withNamespace(namespace)
                .endMetadata()
                .build();

        k8sClient.services()
                .inNamespace(namespace)
                .resource(service)
                .create();

        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/kubernetes/yaml/delete-service-test.yaml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "DeleteServiceTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 1L);
        Assert.assertEquals(result.getTestAction(0).getClass(), DeleteServiceAction.class);
        Assert.assertTrue(result.getTestResult().isSuccess());

        service = k8sClient.services().inNamespace(namespace).withName("my-service").get();
        Assert.assertNull(service);
    }
}
