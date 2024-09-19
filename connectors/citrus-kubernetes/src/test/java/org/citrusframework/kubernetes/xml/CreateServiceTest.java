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

import io.fabric8.kubernetes.api.model.Service;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.kubernetes.actions.CreateServiceAction;
import org.citrusframework.xml.XmlTestLoader;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CreateServiceTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadKubernetesActions() {
        context.getReferenceResolver().bind("myServer", Mockito.mock(HttpServer.class));

        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/kubernetes/xml/create-service-test.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "CreateServiceTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 2L);
        Assert.assertEquals(result.getTestAction(0).getClass(), CreateServiceAction.class);
        Assert.assertTrue(result.getTestResult().isSuccess());

        String namespace = "test";
        Service service = k8sClient.services().inNamespace(namespace).withName("my-service-1").get();
        Assert.assertNotNull(service);
        Assert.assertEquals(service.getMetadata().getLabels().size(), 1);
        Assert.assertEquals(service.getMetadata().getLabels().get("app"), "citrus");
        Assert.assertEquals(service.getSpec().getPorts().size(), 1);
        Assert.assertEquals(service.getSpec().getPorts().get(0).getPort(), 80);
        Assert.assertEquals(service.getSpec().getPorts().get(0).getTargetPort().getIntVal(), 8080);
        Assert.assertEquals(service.getSpec().getSelector().size(), 1);
        Assert.assertEquals(service.getSpec().getSelector().get("citrusframework.org/test-id"), "CreateServiceTest");

        service = k8sClient.services().inNamespace(namespace).withName("my-service-2").get();
        Assert.assertNotNull(service);
        Assert.assertEquals(service.getMetadata().getLabels().size(), 1);
        Assert.assertEquals(service.getMetadata().getLabels().get("app"), "citrus");
        Assert.assertEquals(service.getSpec().getPorts().size(), 1);
        Assert.assertEquals(service.getSpec().getPorts().get(0).getPort(), 80);
        Assert.assertEquals(service.getSpec().getPorts().get(0).getTargetPort().getIntVal(), 8888);
        Assert.assertEquals(service.getSpec().getSelector().size(), 1);
        Assert.assertEquals(service.getSpec().getSelector().get("test"), "citrus");
    }
}
