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

import java.io.IOException;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentList;
import io.fabric8.kubernetes.client.LocalPortForward;
import org.citrusframework.TestCase;
import org.citrusframework.TestCaseMetaInfo;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.kubernetes.CitrusAgentSettings;
import org.citrusframework.kubernetes.actions.AgentDisconnectAction;
import org.citrusframework.yaml.YamlTestLoader;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AgentDisconnectTest extends AbstractYamlActionTest {

    @Mock
    private LocalPortForward portForward;

    @Mock
    private HttpClient serviceClient;


    @Test
    public void shouldLoadKubernetesActions() {
        String namespace = "test";
        context.getReferenceResolver().bind("citrus-agent:port-forward", portForward);
        context.getReferenceResolver().bind("citrus-agent.client", serviceClient);

        lenient().when(portForward.isAlive()).thenReturn(true);

        Deployment deployment = new DeploymentBuilder()
                .withNewMetadata()
                .withName(CitrusAgentSettings.getAgentName())
                .withNamespace(namespace)
                .endMetadata()
                .build();

        k8sClient.apps().deployments()
                .inNamespace(namespace)
                .resource(deployment)
                .create();

        Service service = new ServiceBuilder()
                .withNewMetadata()
                .withName(CitrusAgentSettings.getAgentName())
                .withNamespace(namespace)
                .endMetadata()
                .build();

        k8sClient.services()
                .inNamespace(namespace)
                .resource(service)
                .create();

        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/kubernetes/yaml/agent-disconnect.citrus.it.yaml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("AgentDisconnectTest", result.getName());
        Assertions.assertEquals("Christoph", result.getMetaInfo().getAuthor());
        Assertions.assertEquals(TestCaseMetaInfo.Status.FINAL, result.getMetaInfo().getStatus());
        Assertions.assertEquals(1L, result.getActionCount());
        Assertions.assertEquals(AgentDisconnectAction.class, result.getTestAction(0).getClass());
        Assertions.assertTrue(result.getTestResult().isSuccess());

        DeploymentList deployments = k8sClient.apps().deployments().inNamespace(namespace).list();
        Assertions.assertNotNull(deployments);
        Assertions.assertEquals(0L, deployments.getItems().size());

        ServiceList services = k8sClient.services().inNamespace(namespace).list();
        Assertions.assertNotNull(services);
        Assertions.assertEquals(0L, services.getItems().size());
    }
}
