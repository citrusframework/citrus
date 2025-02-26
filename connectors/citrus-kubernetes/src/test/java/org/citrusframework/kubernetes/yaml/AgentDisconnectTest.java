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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;

public class AgentDisconnectTest extends AbstractYamlActionTest {

    @Mock
    private LocalPortForward portForward;

    @Mock
    private HttpClient serviceClient;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldLoadKubernetesActions() throws IOException {
        String namespace = "test";
        context.getReferenceResolver().bind("citrus-agent:port-forward", portForward);
        context.getReferenceResolver().bind("citrus-agent.client", serviceClient);

        when(portForward.isAlive()).thenReturn(true);

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

        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/kubernetes/yaml/agent-disconnect-test.yaml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assert.assertEquals(result.getName(), "AgentDisconnectTest");
        Assert.assertEquals(result.getMetaInfo().getAuthor(), "Christoph");
        Assert.assertEquals(result.getMetaInfo().getStatus(), TestCaseMetaInfo.Status.FINAL);
        Assert.assertEquals(result.getActionCount(), 1L);
        Assert.assertEquals(result.getTestAction(0).getClass(), AgentDisconnectAction.class);
        Assert.assertTrue(result.getTestResult().isSuccess());

        DeploymentList deployments = k8sClient.apps().deployments().inNamespace(namespace).list();
        Assert.assertNotNull(deployments);
        Assert.assertEquals(deployments.getItems().size(), 0L);

        ServiceList services = k8sClient.services().inNamespace(namespace).list();
        Assert.assertNotNull(services);
        Assert.assertEquals(services.getItems().size(), 0L);
    }
}
