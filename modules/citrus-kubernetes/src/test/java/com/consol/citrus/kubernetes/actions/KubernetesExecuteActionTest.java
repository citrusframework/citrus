/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.kubernetes.actions;

import com.consol.citrus.kubernetes.client.KubernetesClient;
import com.consol.citrus.kubernetes.command.ListPods;
import com.consol.citrus.kubernetes.message.KubernetesMessageHeaders;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.dsl.ClientMixedOperation;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class KubernetesExecuteActionTest extends AbstractTestNGUnitTest {

    private io.fabric8.kubernetes.client.KubernetesClient kubernetesClient = Mockito.mock(io.fabric8.kubernetes.client.KubernetesClient.class);

    private KubernetesClient client = new KubernetesClient();

    @BeforeClass
    public void setup() {
        client.getEndpointConfiguration().setKubernetesClient(kubernetesClient);
    }

    @Test
    public void testListPods() throws Exception {
        final ClientMixedOperation clientOperation = Mockito.mock(ClientMixedOperation.class);
        PodList response = new PodList();
        response.getItems().add(new Pod());

        reset(kubernetesClient, clientOperation);

        when(kubernetesClient.pods()).thenReturn(clientOperation);
        when(clientOperation.inAnyNamespace()).thenReturn(clientOperation);
        when(clientOperation.list()).thenReturn(response);

        KubernetesExecuteAction action = new KubernetesExecuteAction();
        action.setCommand(new ListPods());
        action.setKubernetesClient(client);

        action.execute(context);

        Assert.assertEquals(action.getCommand().getParameters().size(), 0);
        Assert.assertFalse(action.getCommand().getCommandResult().hasError());
        Assert.assertEquals(action.getCommand().getCommandResult().getResult(), response);

        verify(clientOperation).inAnyNamespace();
    }

    @Test
    public void testListPodsInNamespace() throws Exception {
        final ClientMixedOperation clientOperation = Mockito.mock(ClientMixedOperation.class);
        PodList response = new PodList();
        response.getItems().add(new Pod());

        reset(kubernetesClient, clientOperation);

        when(kubernetesClient.pods()).thenReturn(clientOperation);
        when(clientOperation.inNamespace("myNamespace")).thenReturn(clientOperation);
        when(clientOperation.list()).thenReturn(response);

        KubernetesExecuteAction action = new KubernetesExecuteAction();
        action.setCommand(new ListPods().namespace("myNamespace"));
        action.setKubernetesClient(client);

        action.execute(context);

        Assert.assertEquals(action.getCommand().getParameters().size(), 1);
        Assert.assertFalse(action.getCommand().getCommandResult().hasError());
        Assert.assertEquals(action.getCommand().getCommandResult().getResult(), response);

        verify(clientOperation).inNamespace("myNamespace");
    }

    @Test
    public void testListPodsInDefaultClientNamespace() throws Exception {
        final ClientMixedOperation clientOperation = Mockito.mock(ClientMixedOperation.class);
        PodList response = new PodList();
        response.getItems().add(new Pod());

        reset(kubernetesClient, clientOperation);

        when(kubernetesClient.getNamespace()).thenReturn("myNamespace");
        when(kubernetesClient.pods()).thenReturn(clientOperation);
        when(clientOperation.inNamespace("myNamespace")).thenReturn(clientOperation);
        when(clientOperation.list()).thenReturn(response);

        KubernetesExecuteAction action = new KubernetesExecuteAction();
        action.setCommand(new ListPods());
        action.setKubernetesClient(client);

        action.execute(context);

        Assert.assertEquals(action.getCommand().getParameters().size(), 0);
        Assert.assertFalse(action.getCommand().getCommandResult().hasError());
        Assert.assertEquals(action.getCommand().getCommandResult().getResult(), response);

        verify(clientOperation).inNamespace("myNamespace");
    }

    @Test
    public void testListPodsWithLabels() throws Exception {
        final ClientMixedOperation clientOperation = Mockito.mock(ClientMixedOperation.class);
        PodList response = new PodList();
        response.getItems().add(new Pod());

        reset(kubernetesClient, clientOperation);

        when(kubernetesClient.pods()).thenReturn(clientOperation);
        when(clientOperation.inAnyNamespace()).thenReturn(clientOperation);
        when(clientOperation.withLabels(anyMap())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Map<String, String> labels = (Map<String, String>) invocation.getArguments()[0];
                Assert.assertEquals(labels.size(), 2);
                Assert.assertEquals(labels.get("app"), null);
                Assert.assertEquals(labels.get("pod_label"), "active");

                return clientOperation;
            }
        });
        when(clientOperation.list()).thenReturn(response);

        KubernetesExecuteAction action = new KubernetesExecuteAction();
        action.setCommand(new ListPods()
            .label("app")
            .label("pod_label", "active"));
        action.setKubernetesClient(client);

        action.execute(context);

        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.LABEL), "app,pod_label=active");
        Assert.assertFalse(action.getCommand().getCommandResult().hasError());
        Assert.assertEquals(action.getCommand().getCommandResult().getResult(), response);
    }

    @Test
    public void testListPodsWithoutLabels() throws Exception {
        final ClientMixedOperation clientOperation = Mockito.mock(ClientMixedOperation.class);
        PodList response = new PodList();
        response.getItems().add(new Pod());

        reset(kubernetesClient, clientOperation);

        when(kubernetesClient.pods()).thenReturn(clientOperation);
        when(clientOperation.inAnyNamespace()).thenReturn(clientOperation);
        when(clientOperation.withoutLabels(anyMap())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Map<String, String> labels = (Map<String, String>) invocation.getArguments()[0];
                Assert.assertEquals(labels.size(), 2);
                Assert.assertEquals(labels.get("app"), null);
                Assert.assertEquals(labels.get("pod_label"), "inactive");

                return clientOperation;
            }
        });
        when(clientOperation.list()).thenReturn(response);

        KubernetesExecuteAction action = new KubernetesExecuteAction();
        action.setCommand(new ListPods()
                .withoutLabel("app")
                .withoutLabel("pod_label", "inactive"));
        action.setKubernetesClient(client);

        action.execute(context);

        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.LABEL), "!app,pod_label!=inactive");
        Assert.assertFalse(action.getCommand().getCommandResult().hasError());
        Assert.assertEquals(action.getCommand().getCommandResult().getResult(), response);
    }

    @Test
    public void testListPodsMixedLabels() throws Exception {
        final ClientMixedOperation clientOperation = Mockito.mock(ClientMixedOperation.class);
        PodList response = new PodList();
        response.getItems().add(new Pod());

        reset(kubernetesClient, clientOperation);

        when(kubernetesClient.pods()).thenReturn(clientOperation);
        when(clientOperation.inAnyNamespace()).thenReturn(clientOperation);
        when(clientOperation.withLabels(anyMap())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Map<String, String> labels = (Map<String, String>) invocation.getArguments()[0];
                Assert.assertEquals(labels.size(), 2);
                Assert.assertEquals(labels.get("app"), null);
                Assert.assertEquals(labels.get("with"), "active");

                return clientOperation;
            }
        });
        when(clientOperation.withoutLabels(anyMap())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Map<String, String> labels = (Map<String, String>) invocation.getArguments()[0];
                Assert.assertEquals(labels.size(), 2);
                Assert.assertEquals(labels.get("running"), null);
                Assert.assertEquals(labels.get("without"), "inactive");

                return clientOperation;
            }
        });
        when(clientOperation.list()).thenReturn(response);

        KubernetesExecuteAction action = new KubernetesExecuteAction();
        action.setCommand(new ListPods()
                .label("app")
                .withoutLabel("running")
                .label("with", "active")
                .withoutLabel("without", "inactive"));
        action.setKubernetesClient(client);

        action.execute(context);

        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.LABEL), "app,!running,with=active,without!=inactive");
        Assert.assertFalse(action.getCommand().getCommandResult().hasError());
        Assert.assertEquals(action.getCommand().getCommandResult().getResult(), response);
    }
}