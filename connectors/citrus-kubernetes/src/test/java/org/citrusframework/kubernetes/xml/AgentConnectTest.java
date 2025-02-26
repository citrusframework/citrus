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

import java.io.IOException;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.client.LocalPortForward;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.kubernetes.CitrusAgentSettings;
import org.citrusframework.kubernetes.actions.AgentConnectAction;
import org.citrusframework.xml.XmlTestLoader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AgentConnectTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadKubernetesActions() throws IOException {
        String namespace = "test";
        Pod pod = new PodBuilder()
                .withNewMetadata()
                    .withName(CitrusAgentSettings.getAgentName())
                    .addToLabels("app", "citrus")
                    .addToLabels(AgentConnectAction.KUBERNETES_LABEL_NAME, CitrusAgentSettings.getAgentName())
                    .addToLabels(AgentConnectAction.KUBERNETES_LABEL_MANAGED_BY, "citrus")
                .endMetadata()
                .build();

        k8sClient.pods()
                .inNamespace(namespace)
                .resource(pod)
                .create();

        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/kubernetes/xml/agent-connect-test.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "AgentConnectTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 1L);
        Assert.assertEquals(result.getTestAction(0).getClass(), AgentConnectAction.class);
        Assert.assertTrue(result.getTestResult().isSuccess());

        Assert.assertTrue(context.getReferenceResolver().isResolvable("citrus-agent:port-forward"));
        LocalPortForward portForward = context.getReferenceResolver().resolve("citrus-agent:port-forward", LocalPortForward.class);
        portForward.close();

        DeploymentList deployments = k8sClient.apps().deployments().inNamespace(namespace).list();
        Assert.assertNotNull(deployments);
        Assert.assertEquals(deployments.getItems().size(), 1L);
        Deployment deployment = deployments.getItems().get(0);
        Assert.assertNotNull(deployment);
        Assert.assertEquals(deployment.getMetadata().getLabels().size(), 3);
        Assert.assertEquals(deployment.getMetadata().getLabels().get("app"), "citrus");
        Assert.assertEquals(deployment.getMetadata().getLabels().get(AgentConnectAction.KUBERNETES_LABEL_NAME), CitrusAgentSettings.getAgentName());
        Assert.assertEquals(deployment.getMetadata().getLabels().get(AgentConnectAction.KUBERNETES_LABEL_MANAGED_BY), "citrus");
        Assert.assertNotNull(deployment.getSpec().getTemplate());
        Assert.assertEquals(deployment.getSpec().getTemplate().getSpec().getContainers().size(), 1L);
        Assert.assertEquals(deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getImage(), "citrusframework/citrus-agent:latest");

        Service service = k8sClient.services().inNamespace(namespace).withName(CitrusAgentSettings.getAgentName()).get();
        Assert.assertNotNull(service);
        Assert.assertEquals(service.getMetadata().getLabels().size(), 3);
        Assert.assertEquals(service.getMetadata().getLabels().get("app"), "citrus");
        Assert.assertEquals(service.getMetadata().getLabels().get(AgentConnectAction.KUBERNETES_LABEL_NAME), CitrusAgentSettings.getAgentName());
        Assert.assertEquals(service.getMetadata().getLabels().get(AgentConnectAction.KUBERNETES_LABEL_MANAGED_BY), "citrus");
        Assert.assertEquals(service.getSpec().getPorts().size(), 1);
        Assert.assertEquals(service.getSpec().getPorts().get(0).getPort(), 80);
        Assert.assertEquals(service.getSpec().getPorts().get(0).getTargetPort().getIntVal(), 8080);
        Assert.assertEquals(service.getSpec().getSelector().size(), 1);
        Assert.assertEquals(service.getSpec().getSelector().get(AgentConnectAction.KUBERNETES_LABEL_NAME), CitrusAgentSettings.getAgentName());

        HttpClient serviceClient = context.getReferenceResolver().resolve("citrus-agent.client", HttpClient.class);
        Assert.assertNotNull(serviceClient);
        Assert.assertEquals(serviceClient.getEndpointConfiguration().getRequestUrl(), "http://localhost:31234");
    }
}
