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
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.kubernetes.actions.CreateServiceAction;
import org.citrusframework.yaml.YamlTestLoader;
import org.mockito.Mockito;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CreateServiceTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadKubernetesActions() {
        context.getReferenceResolver().bind("myServer", Mockito.mock(HttpServer.class));

        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/kubernetes/yaml/create-service.citrus.it.yaml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("CreateServiceTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(2L, result.getActionCount());
        Assertions.assertEquals(CreateServiceAction.class, result.getTestAction(0).getClass());
        Assertions.assertTrue(result.getTestResult().isSuccess());

        String namespace = "test";
        Service service = k8sClient.services().inNamespace(namespace).withName("my-service-1").get();
        Assertions.assertNotNull(service);
        Assertions.assertEquals(1, service.getMetadata().getLabels().size());
        Assertions.assertEquals("citrus", service.getMetadata().getLabels().get("app"));
        Assertions.assertEquals(1, service.getSpec().getPorts().size());
        Assertions.assertEquals(80, service.getSpec().getPorts().get(0).getPort());
        Assertions.assertEquals(8080, service.getSpec().getPorts().get(0).getTargetPort().getIntVal());
        Assertions.assertEquals(1, service.getSpec().getSelector().size());
        Assertions.assertEquals("CreateServiceTest", service.getSpec().getSelector().get("citrusframework.org/test-id"));

        service = k8sClient.services().inNamespace(namespace).withName("my-service-2").get();
        Assertions.assertNotNull(service);
        Assertions.assertEquals(1, service.getMetadata().getLabels().size());
        Assertions.assertEquals("citrus", service.getMetadata().getLabels().get("app"));
        Assertions.assertEquals(1, service.getSpec().getPorts().size());
        Assertions.assertEquals(80, service.getSpec().getPorts().get(0).getPort());
        Assertions.assertEquals(8888, service.getSpec().getPorts().get(0).getTargetPort().getIntVal());
        Assertions.assertEquals(1, service.getSpec().getSelector().size());
        Assertions.assertEquals("citrus", service.getSpec().getSelector().get("test"));
    }
}
