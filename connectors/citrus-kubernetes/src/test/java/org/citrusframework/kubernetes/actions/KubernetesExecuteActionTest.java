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

package org.citrusframework.kubernetes.actions;

import java.util.Map;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import org.citrusframework.kubernetes.client.KubernetesClient;
import org.citrusframework.kubernetes.command.ListPods;
import org.citrusframework.kubernetes.command.ListResult;
import org.citrusframework.kubernetes.message.KubernetesMessageHeaders;
import org.citrusframework.testng.AbstractTestNGUnitTest;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class KubernetesExecuteActionTest extends AbstractTestNGUnitTest {

    private final KubernetesClient client = new KubernetesClient();

    @Mock
    private io.fabric8.kubernetes.client.KubernetesClient kubernetesClient;

    @BeforeClass
    public void setup() {
        MockitoAnnotations.openMocks(this);
        client.getEndpointConfiguration().setKubernetesClient(kubernetesClient);
    }

    @Test
    public void testListPods() {
        final MixedOperation clientOperation = Mockito.mock(MixedOperation.class);
        PodList response = new PodList();
        response.getItems().add(new Pod());

        reset(kubernetesClient, clientOperation);

        when(kubernetesClient.pods()).thenReturn(clientOperation);
        when(clientOperation.inAnyNamespace()).thenReturn(clientOperation);
        when(clientOperation.list()).thenReturn(response);

        KubernetesExecuteAction action = new KubernetesExecuteAction.Builder()
                .client(client)
                .command(new ListPods())
                .build();
        action.execute(context);

        Assert.assertEquals(action.getCommand().getParameters().size(), 0);
        Assert.assertFalse(action.getCommand().getCommandResult().hasError());
        Assert.assertEquals(((ListResult) action.getCommand().getCommandResult().getResult()).getItems(), response.getItems());

        verify(clientOperation).inAnyNamespace();
    }

    @Test
    public void testListPodsInNamespace() {
        final MixedOperation clientOperation = Mockito.mock(MixedOperation.class);
        PodList response = new PodList();
        response.getItems().add(new Pod());

        reset(kubernetesClient, clientOperation);

        when(kubernetesClient.pods()).thenReturn(clientOperation);
        when(clientOperation.inNamespace("myNamespace")).thenReturn(clientOperation);
        when(clientOperation.list()).thenReturn(response);

        KubernetesExecuteAction action = new KubernetesExecuteAction.Builder()
                .client(client)
                .command(new ListPods().namespace("myNamespace"))
                .build();
        action.execute(context);

        Assert.assertEquals(action.getCommand().getParameters().size(), 1);
        Assert.assertFalse(action.getCommand().getCommandResult().hasError());
        Assert.assertEquals(((ListResult) action.getCommand().getCommandResult().getResult()).getItems(), response.getItems());

        verify(clientOperation).inNamespace("myNamespace");
    }

    @Test
    public void testListPodsInDefaultClientNamespace() {
        final MixedOperation clientOperation = Mockito.mock(MixedOperation.class);
        PodList response = new PodList();
        response.getItems().add(new Pod());

        reset(kubernetesClient, clientOperation);

        when(kubernetesClient.getNamespace()).thenReturn("myNamespace");
        when(kubernetesClient.pods()).thenReturn(clientOperation);
        when(clientOperation.inNamespace("myNamespace")).thenReturn(clientOperation);
        when(clientOperation.list()).thenReturn(response);

        KubernetesExecuteAction action = new KubernetesExecuteAction.Builder()
                .client(client)
                .command(new ListPods())
                .build();
        action.execute(context);

        Assert.assertEquals(action.getCommand().getParameters().size(), 0);
        Assert.assertFalse(action.getCommand().getCommandResult().hasError());
        Assert.assertEquals(((ListResult) action.getCommand().getCommandResult().getResult()).getItems(), response.getItems());

        verify(clientOperation).inNamespace("myNamespace");
    }

    @Test
    public void testListPodsWithLabels() {
        final MixedOperation clientOperation = Mockito.mock(MixedOperation.class);
        PodList response = new PodList();
        response.getItems().add(new Pod());

        reset(kubernetesClient, clientOperation);

        when(kubernetesClient.pods()).thenReturn(clientOperation);
        when(clientOperation.inAnyNamespace()).thenReturn(clientOperation);
        when(clientOperation.withLabels(anyMap())).thenAnswer(invocation -> {
            Map<String, String> labels = (Map<String, String>) invocation.getArguments()[0];
            Assert.assertEquals(labels.size(), 2);
            Assert.assertEquals(labels.get("app"), null);
            Assert.assertEquals(labels.get("pod_label"), "active");

            return clientOperation;
        });
        when(clientOperation.list()).thenReturn(response);

        KubernetesExecuteAction action = new KubernetesExecuteAction.Builder()
                .client(client)
                .command(new ListPods()
                        .label("app")
                        .label("pod_label", "active"))
                .build();
        action.execute(context);

        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.LABEL), "app,pod_label=active");
        Assert.assertFalse(action.getCommand().getCommandResult().hasError());
        Assert.assertEquals(((ListResult) action.getCommand().getCommandResult().getResult()).getItems(), response.getItems());
    }

    @Test
    public void testListPodsWithoutLabels() {
        final MixedOperation clientOperation = Mockito.mock(MixedOperation.class);
        PodList response = new PodList();
        response.getItems().add(new Pod());

        reset(kubernetesClient, clientOperation);

        when(kubernetesClient.pods()).thenReturn(clientOperation);
        when(clientOperation.inAnyNamespace()).thenReturn(clientOperation);
        when(clientOperation.withoutLabels(anyMap())).thenAnswer(invocation -> {
            Map<String, String> labels = (Map<String, String>) invocation.getArguments()[0];
            Assert.assertEquals(labels.size(), 2);
            Assert.assertEquals(labels.get("app"), null);
            Assert.assertEquals(labels.get("pod_label"), "inactive");

            return clientOperation;
        });
        when(clientOperation.list()).thenReturn(response);

        KubernetesExecuteAction action = new KubernetesExecuteAction.Builder()
                .client(client)
                .command(new ListPods()
                        .withoutLabel("app")
                        .withoutLabel("pod_label", "inactive"))
                .build();
        action.execute(context);

        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.LABEL), "!app,pod_label!=inactive");
        Assert.assertFalse(action.getCommand().getCommandResult().hasError());
        Assert.assertEquals(((ListResult) action.getCommand().getCommandResult().getResult()).getItems(), response.getItems());
    }

    @Test
    public void testListPodsMixedLabels() {
        final MixedOperation clientOperation = Mockito.mock(MixedOperation.class);
        PodList response = new PodList();
        response.getItems().add(new Pod());

        reset(kubernetesClient, clientOperation);

        when(kubernetesClient.pods()).thenReturn(clientOperation);
        when(clientOperation.inAnyNamespace()).thenReturn(clientOperation);
        when(clientOperation.withLabels(anyMap())).thenAnswer(invocation -> {
            Map<String, String> labels = (Map<String, String>) invocation.getArguments()[0];
            Assert.assertEquals(labels.size(), 2);
            Assert.assertEquals(labels.get("app"), null);
            Assert.assertEquals(labels.get("with"), "active");

            return clientOperation;
        });
        when(clientOperation.withoutLabels(anyMap())).thenAnswer(invocation -> {
            Map<String, String> labels = (Map<String, String>) invocation.getArguments()[0];
            Assert.assertEquals(labels.size(), 2);
            Assert.assertEquals(labels.get("running"), null);
            Assert.assertEquals(labels.get("without"), "inactive");

            return clientOperation;
        });
        when(clientOperation.list()).thenReturn(response);

        KubernetesExecuteAction action = new KubernetesExecuteAction.Builder()
                .client(client)
                .command(new ListPods()
                        .label("app")
                        .withoutLabel("running")
                        .label("with", "active")
                        .withoutLabel("without", "inactive"))
                .build();
        action.execute(context);

        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.LABEL), "app,!running,with=active,without!=inactive");
        Assert.assertFalse(action.getCommand().getCommandResult().hasError());
        Assert.assertEquals(((ListResult) action.getCommand().getCommandResult().getResult()).getItems(), response.getItems());
    }
}
