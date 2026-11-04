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

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.kubernetes.actions.ServiceConnectAction;
import org.citrusframework.xml.XmlTestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ServiceConnectTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadKubernetesActions() {
        String namespace = "test";
        Pod pod = new PodBuilder()
                .withNewMetadata()
                    .withName("my-pod")
                    .withLabels(Collections.singletonMap("app", "camel"))
                .endMetadata()
                .build();

        k8sClient.pods()
                .inNamespace(namespace)
                .resource(pod)
                .create();

        Service service = new ServiceBuilder()
                .withNewMetadata()
                .withName("my-service")
                .withNamespace(namespace)
                .endMetadata()
                .withNewSpec()
                    .withSelector(Collections.singletonMap("app", "camel"))
                .endSpec()
                .build();

        k8sClient.services()
                .inNamespace(namespace)
                .resource(service)
                .create();

        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/kubernetes/xml/service-connect.citrus.it.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("ServiceConnectTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(1L, result.getActionCount());
        Assertions.assertEquals(ServiceConnectAction.class, result.getTestAction(0).getClass());
        Assertions.assertTrue(result.getTestResult().isSuccess());

        HttpClient serviceClient = context.getReferenceResolver().resolve("my-service.client", HttpClient.class);
        Assertions.assertNotNull(serviceClient);
        Assertions.assertEquals("http://localhost:31234", serviceClient.getEndpointConfiguration().getRequestUrl());
    }
}
