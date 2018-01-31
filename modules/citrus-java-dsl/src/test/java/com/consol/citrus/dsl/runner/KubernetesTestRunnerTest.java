/*
 * Copyright 2006-2016 the original author or authors.
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

package com.consol.citrus.dsl.runner;

import com.consol.citrus.TestCase;
import com.consol.citrus.kubernetes.actions.KubernetesExecuteAction;
import com.consol.citrus.kubernetes.client.KubernetesClient;
import com.consol.citrus.kubernetes.command.*;
import com.consol.citrus.kubernetes.message.KubernetesMessageHeaders;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.github.dockerjava.api.command.CreateContainerResponse;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.ClientMixedOperation;
import io.fabric8.kubernetes.client.dsl.ClientNonNamespaceOperation;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URL;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class KubernetesTestRunnerTest extends AbstractTestNGUnitTest {

    private io.fabric8.kubernetes.client.KubernetesClient k8sClient = Mockito.mock(io.fabric8.kubernetes.client.KubernetesClient.class);

    @Test
    public void testKubernetesBuilder() throws Exception {
        ClientMixedOperation podsOperation = Mockito.mock(ClientMixedOperation.class);
        ClientNonNamespaceOperation namespacesOperation = Mockito.mock(ClientNonNamespaceOperation.class);
        ClientNonNamespaceOperation nodesOperation = Mockito.mock(ClientNonNamespaceOperation.class);
        ClientMixedOperation servicesOperation = Mockito.mock(ClientMixedOperation.class);

        Watch watch = Mockito.mock(Watch.class);

        CreateContainerResponse response = new CreateContainerResponse();
        response.setId(UUID.randomUUID().toString());

        reset(k8sClient, podsOperation, namespacesOperation, nodesOperation, servicesOperation);

        when(k8sClient.getApiVersion()).thenReturn("v1");
        when(k8sClient.getMasterUrl()).thenReturn(new URL("https://localhost:8443"));
        when(k8sClient.getNamespace()).thenReturn("test");

        when(k8sClient.pods()).thenReturn(podsOperation);
        when(podsOperation.list()).thenReturn(new PodList());
        when(podsOperation.inNamespace("myNamespace")).thenReturn(podsOperation);

        when(k8sClient.namespaces()).thenReturn(namespacesOperation);
        when(namespacesOperation.list()).thenReturn(new NamespaceList());

        when(k8sClient.nodes()).thenReturn(nodesOperation);
        when(nodesOperation.list()).thenReturn(new NodeList());
        when(nodesOperation.watch(any(Watcher.class))).thenAnswer(invocationOnMock -> {
            ((Watcher) invocationOnMock.getArguments()[0]).eventReceived(Watcher.Action.ADDED, new Node());
            return watch;
        });

        when(k8sClient.services()).thenReturn(servicesOperation);
        when(servicesOperation.watch(any(Watcher.class))).thenAnswer(invocationOnMock -> {
            ((Watcher) invocationOnMock.getArguments()[0]).eventReceived(Watcher.Action.MODIFIED, new Service());
            return watch;
        });
        when(servicesOperation.withName("myService")).thenReturn(servicesOperation);
        when(servicesOperation.inNamespace("myNamespace")).thenReturn(servicesOperation);

        final KubernetesClient client = new KubernetesClient();
        client.getEndpointConfiguration().setKubernetesClient(k8sClient);

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                kubernetes(action -> action.client(client)
                    .info()
                    .validate((commandResult, context) -> {
                        Assert.assertEquals(commandResult.getResult().getApiVersion(), "v1");
                        Assert.assertEquals(commandResult.getResult().getMasterUrl(), "https://localhost:8443");
                        Assert.assertEquals(commandResult.getResult().getNamespace(), "test");
                    }));

                kubernetes(action -> action.client(client)
                    .pods()
                    .list()
                    .label("active")
                    .namespace("myNamespace"));

                kubernetes(action -> action.client(client)
                    .nodes()
                    .list()
                    .validate((nodes, context) -> {
                        Assert.assertNotNull(nodes.getResult());
                    }));

                kubernetes(action -> action.client(client)
                    .namespaces()
                    .list()
                    .validate((namespaces, context) -> {
                        Assert.assertNotNull(namespaces.getResult());
                    }));

                kubernetes(action -> action.client(client)
                        .nodes()
                        .watch()
                        .label("new"));

                kubernetes(action -> action.client(client)
                        .services()
                        .watch()
                        .name("myService")
                        .namespace("myNamespace")
                        .validate((services, context) -> {
                            Assert.assertNotNull(services);
                            Assert.assertNotNull(services.getResult());
                            Assert.assertEquals(((WatchEventResult) services).getAction(), Watcher.Action.MODIFIED);
                        }));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 6);
        Assert.assertEquals(test.getActions().get(0).getClass(), KubernetesExecuteAction.class);
        Assert.assertEquals(test.getActiveAction().getClass(), KubernetesExecuteAction.class);

        KubernetesExecuteAction action = (KubernetesExecuteAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "kubernetes-execute");
        Assert.assertEquals(action.getCommand().getClass(), Info.class);

        action = (KubernetesExecuteAction)test.getActions().get(1);
        Assert.assertEquals(action.getName(), "kubernetes-execute");
        Assert.assertEquals(action.getCommand().getClass(), ListPods.class);
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.NAMESPACE), "myNamespace");
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.LABEL), "active");

        action = (KubernetesExecuteAction)test.getActions().get(2);
        Assert.assertEquals(action.getName(), "kubernetes-execute");
        Assert.assertEquals(action.getCommand().getClass(), ListNodes.class);
        Assert.assertNotNull(action.getCommand().getResultCallback());

        action = (KubernetesExecuteAction)test.getActions().get(3);
        Assert.assertEquals(action.getName(), "kubernetes-execute");
        Assert.assertEquals(action.getCommand().getClass(), ListNamespaces.class);

        action = (KubernetesExecuteAction)test.getActions().get(4);
        Assert.assertEquals(action.getName(), "kubernetes-execute");
        Assert.assertEquals(action.getCommand().getClass(), WatchNodes.class);
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.LABEL), "new");

        action = (KubernetesExecuteAction)test.getActions().get(5);
        Assert.assertEquals(action.getName(), "kubernetes-execute");
        Assert.assertEquals(action.getCommand().getClass(), WatchServices.class);
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.NAME), "myService");
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.NAMESPACE), "myNamespace");

        verify(watch, atLeastOnce()).close();
    }
}
