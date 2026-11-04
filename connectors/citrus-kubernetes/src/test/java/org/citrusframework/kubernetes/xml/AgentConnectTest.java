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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AgentConnectTest extends AbstractXmlActionTest {

    @Test
    public void shouldLoadKubernetesActions() throws IOException {
        String namespace = "test";
        Pod pod = new PodBuilder()
                .withNewMetadata()
                    .withName(CitrusAgentSettings.getAgentName())
                    .addToLabels("app", "citrus")
                    .addToLabels(AgentConnectAction.KUBERNETES_LABEL_RUNTIME, "yaks")
                    .addToLabels(AgentConnectAction.OPENSHIFT_LABEL_RUNTIME, "yaks")
                    .addToLabels(AgentConnectAction.KUBERNETES_LABEL_NAME, CitrusAgentSettings.getAgentName())
                    .addToLabels(AgentConnectAction.KUBERNETES_LABEL_MANAGED_BY, "citrus")
                .endMetadata()
                .build();

        k8sClient.pods()
                .inNamespace(namespace)
                .resource(pod)
                .create();

        XmlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/kubernetes/xml/agent-connect.citrus.it.xml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("AgentConnectTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(1L, result.getActionCount());
        Assertions.assertEquals(AgentConnectAction.class, result.getTestAction(0).getClass());
        Assertions.assertTrue(result.getTestResult().isSuccess());

        Assertions.assertTrue(context.getReferenceResolver().isResolvable("citrus-agent:port-forward"));
        LocalPortForward portForward = context.getReferenceResolver().resolve("citrus-agent:port-forward", LocalPortForward.class);
        portForward.close();

        DeploymentList deployments = k8sClient.apps().deployments().inNamespace(namespace).list();
        Assertions.assertNotNull(deployments);
        Assertions.assertEquals(1L, deployments.getItems().size());
        Deployment deployment = deployments.getItems().get(0);
        Assertions.assertNotNull(deployment);
        Assertions.assertEquals(5, deployment.getMetadata().getLabels().size());
        Assertions.assertEquals("citrus", deployment.getMetadata().getLabels().get("app"));
        Assertions.assertEquals("yaks", deployment.getMetadata().getLabels().get(AgentConnectAction.KUBERNETES_LABEL_RUNTIME));
        Assertions.assertEquals("yaks", deployment.getMetadata().getLabels().get(AgentConnectAction.OPENSHIFT_LABEL_RUNTIME));
        Assertions.assertEquals(CitrusAgentSettings.getAgentName(), deployment.getMetadata().getLabels().get(AgentConnectAction.KUBERNETES_LABEL_NAME));
        Assertions.assertEquals("citrus", deployment.getMetadata().getLabels().get(AgentConnectAction.KUBERNETES_LABEL_MANAGED_BY));
        Assertions.assertNotNull(deployment.getSpec().getTemplate());
        Assertions.assertEquals(1L, deployment.getSpec().getTemplate().getSpec().getContainers().size());
        Assertions.assertEquals("quay.io/citrusframework/citrus-agent:latest", deployment.getSpec().getTemplate().getSpec().getContainers().get(0).getImage());

        Service service = k8sClient.services().inNamespace(namespace).withName(CitrusAgentSettings.getAgentName()).get();
        Assertions.assertNotNull(service);
        Assertions.assertEquals(5, service.getMetadata().getLabels().size());
        Assertions.assertEquals("citrus", service.getMetadata().getLabels().get("app"));
        Assertions.assertEquals("yaks", service.getMetadata().getLabels().get(AgentConnectAction.KUBERNETES_LABEL_RUNTIME));
        Assertions.assertEquals("yaks", service.getMetadata().getLabels().get(AgentConnectAction.OPENSHIFT_LABEL_RUNTIME));
        Assertions.assertEquals(CitrusAgentSettings.getAgentName(), service.getMetadata().getLabels().get(AgentConnectAction.KUBERNETES_LABEL_NAME));
        Assertions.assertEquals("citrus", service.getMetadata().getLabels().get(AgentConnectAction.KUBERNETES_LABEL_MANAGED_BY));
        Assertions.assertEquals(1, service.getSpec().getPorts().size());
        Assertions.assertEquals(80, service.getSpec().getPorts().get(0).getPort());
        Assertions.assertEquals(8080, service.getSpec().getPorts().get(0).getTargetPort().getIntVal());
        Assertions.assertEquals(1, service.getSpec().getSelector().size());
        Assertions.assertEquals(CitrusAgentSettings.getAgentName(), service.getSpec().getSelector().get(AgentConnectAction.KUBERNETES_LABEL_NAME));

        HttpClient serviceClient = context.getReferenceResolver().resolve("citrus-agent.client", HttpClient.class);
        Assertions.assertNotNull(serviceClient);
        Assertions.assertEquals("http://localhost:31234", serviceClient.getEndpointConfiguration().getRequestUrl());
    }
}
