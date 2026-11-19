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

import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import org.citrusframework.TestCase;
import org.citrusframework.kubernetes.actions.KubernetesExecuteAction;
import org.citrusframework.kubernetes.command.GetService;
import org.citrusframework.kubernetes.command.Info;
import org.citrusframework.kubernetes.command.ListPods;
import org.citrusframework.kubernetes.message.KubernetesMessageHeaders;
import org.citrusframework.yaml.YamlTestLoader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExecuteTest extends AbstractYamlActionTest {

    @Test
    public void shouldLoadExecuteActions() {
        createPod("my-pod");
        createService();

        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/kubernetes/yaml/kubernetes-execute.citrus.it.yaml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("KubernetesExecuteTest", result.getName());
        Assertions.assertEquals(4L, result.getActionCount());

        Assertions.assertEquals(KubernetesExecuteAction.class, result.getTestAction(0).getClass());
        Assertions.assertEquals(Info.class, ((KubernetesExecuteAction) result.getTestAction(0)).getCommand().getClass());

        Assertions.assertEquals(KubernetesExecuteAction.class, result.getTestAction(1).getClass());
        KubernetesExecuteAction listPods = (KubernetesExecuteAction) result.getTestAction(1);
        Assertions.assertEquals(ListPods.class, listPods.getCommand().getClass());

        Assertions.assertEquals(KubernetesExecuteAction.class, result.getTestAction(2).getClass());
        KubernetesExecuteAction getPod = (KubernetesExecuteAction) result.getTestAction(2);
        Assertions.assertEquals("my-pod", getPod.getCommand().getParameters().get(KubernetesMessageHeaders.NAME));

        Assertions.assertEquals(KubernetesExecuteAction.class, result.getTestAction(3).getClass());
        KubernetesExecuteAction getService = (KubernetesExecuteAction) result.getTestAction(3);
        Assertions.assertEquals(GetService.class, getService.getCommand().getClass());

        Assertions.assertTrue(result.getTestResult().isSuccess());
    }

    @Test
    public void shouldLoadDeploymentStateSample() {
        k8sClient.pods().inNamespace("test").resource(new PodBuilder()
                .withNewMetadata()
                    .withName("todo-pod")
                    .addToLabels("app", "todo")
                .endMetadata()
                .withNewStatus()
                    .withPhase("Running")
                .endStatus()
                .build()).create();
        createService();

        YamlTestLoader testLoader = createTestLoader("classpath:org/citrusframework/kubernetes/yaml/deployment-state.citrus.it.yaml");

        testLoader.load();
        TestCase result = testLoader.getTestCase();
        Assertions.assertEquals("DeploymentStateTest", result.getName());
        Assertions.assertEquals(2L, result.getActionCount());
        Assertions.assertEquals(KubernetesExecuteAction.class, result.getTestAction(0).getClass());
        Assertions.assertEquals(KubernetesExecuteAction.class, result.getTestAction(1).getClass());
        Assertions.assertTrue(result.getTestResult().isSuccess());
    }

    private void createPod(String name) {
        k8sClient.pods().inNamespace("test").resource(new PodBuilder()
                .withNewMetadata()
                    .withName(name)
                .endMetadata()
                .withNewStatus()
                    .withPhase("Running")
                .endStatus()
                .build()).create();
    }

    private void createService() {
        k8sClient.services().inNamespace("test").resource(new ServiceBuilder()
                .withNewMetadata()
                    .withName("citrus-sample-todo-service")
                .endMetadata()
                .build()).create();
    }
}
