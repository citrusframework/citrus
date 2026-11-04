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

package org.citrusframework.kubernetes.actions.dsl;

import java.net.URL;
import java.util.UUID;

import com.github.dockerjava.api.command.CreateContainerResponse;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.NodeList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.ServiceResource;
import org.citrusframework.base.DefaultTestCaseRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.TestCase;
import org.citrusframework.kubernetes.UnitTestSupport;
import org.citrusframework.kubernetes.actions.KubernetesExecuteAction;
import org.citrusframework.kubernetes.client.KubernetesClient;
import org.citrusframework.kubernetes.command.Info;
import org.citrusframework.kubernetes.command.ListNamespaces;
import org.citrusframework.kubernetes.command.ListNodes;
import org.citrusframework.kubernetes.command.ListPods;
import org.citrusframework.kubernetes.command.WatchNodes;
import org.citrusframework.kubernetes.command.WatchServices;
import org.citrusframework.kubernetes.message.KubernetesMessageHeaders;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mockito;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * @since 2.7
 */
@ExtendWith(MockitoExtension.class)
public class KubernetesTestActionBuilderTest extends UnitTestSupport implements TestActionSupport {

    @Mock
    private MixedOperation<Pod, PodList, PodResource> podsOperation;
    @Mock
    private MixedOperation<Service, ServiceList, ServiceResource<Service>> servicesOperation;
    @Mock
    private MixedOperation<Namespace, NamespaceList, Resource<Namespace>> namespacesOperation;
    @Mock
    private MixedOperation<Node, NodeList, Resource<Node>> nodesOperation;
    @Mock
    private ServiceResource<Service> serviceResource;

    @Mock
    private io.fabric8.kubernetes.client.KubernetesClient k8sClient;

    @SuppressWarnings("unchecked")
    @Test
    public void testKubernetesBuilder() throws Exception {
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
        lenient().when(servicesOperation.withName("myService")).thenReturn(serviceResource);
        when(servicesOperation.inNamespace("myNamespace")).thenReturn(servicesOperation);

        final KubernetesClient client = new KubernetesClient();
        client.getEndpointConfiguration().setKubernetesClient(k8sClient);

        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(kubernetes().client(client)
            .execute()
            .info()
            .validate((commandResult, context) -> {
                Assertions.assertEquals("v1", commandResult.getResult().getApiVersion());
                Assertions.assertEquals("https://localhost:8443", commandResult.getResult().getMasterUrl());
                Assertions.assertEquals("test", commandResult.getResult().getNamespace());
            }));

        builder.$(kubernetes().client(client)
            .execute()
            .pods()
            .list()
            .label("active")
            .namespace("myNamespace"));

        builder.$(kubernetes().client(client)
            .execute()
            .nodes()
            .list()
            .validate((nodes, context) -> Assertions.assertNotNull(nodes.getResult())));

        builder.$(kubernetes().client(client)
            .execute()
            .namespaces()
            .list()
            .validate((namespaces, context) -> Assertions.assertNotNull(namespaces.getResult())));

        builder.$(kubernetes().client(client)
            .execute()
            .nodes()
            .watch()
            .label("new"));

        builder.$(kubernetes().client(client)
            .execute()
            .services()
            .watch()
            .name("myService")
            .namespace("myNamespace")
            .validate((services, context) -> {
                Assertions.assertNotNull(services);
                Assertions.assertNotNull(services.getResult());
                Assertions.assertEquals(Watcher.Action.MODIFIED, services.getResult().getAction());
            }));

        TestCase test = builder.getTestCase();
        Assertions.assertEquals(6, test.getActionCount());
        Assertions.assertEquals(KubernetesExecuteAction.class, test.getActions().get(0).getClass());
        Assertions.assertEquals(KubernetesExecuteAction.class, test.getActiveAction().getClass());

        KubernetesExecuteAction action = (KubernetesExecuteAction)test.getActions().get(0);
        Assertions.assertEquals("kubernetes-execute", action.getName());
        Assertions.assertEquals(Info.class, action.getCommand().getClass());

        action = (KubernetesExecuteAction)test.getActions().get(1);
        Assertions.assertEquals("kubernetes-execute", action.getName());
        Assertions.assertEquals(ListPods.class, action.getCommand().getClass());
        Assertions.assertEquals("myNamespace", action.getCommand().getParameters().get(KubernetesMessageHeaders.NAMESPACE));
        Assertions.assertEquals("active", action.getCommand().getParameters().get(KubernetesMessageHeaders.LABEL));

        action = (KubernetesExecuteAction)test.getActions().get(2);
        Assertions.assertEquals("kubernetes-execute", action.getName());
        Assertions.assertEquals(ListNodes.class, action.getCommand().getClass());
        Assertions.assertNotNull(action.getCommand().getResultCallback());

        action = (KubernetesExecuteAction)test.getActions().get(3);
        Assertions.assertEquals("kubernetes-execute", action.getName());
        Assertions.assertEquals(ListNamespaces.class, action.getCommand().getClass());

        action = (KubernetesExecuteAction)test.getActions().get(4);
        Assertions.assertEquals("kubernetes-execute", action.getName());
        Assertions.assertEquals(WatchNodes.class, action.getCommand().getClass());
        Assertions.assertEquals("new", action.getCommand().getParameters().get(KubernetesMessageHeaders.LABEL));

        action = (KubernetesExecuteAction)test.getActions().get(5);
        Assertions.assertEquals("kubernetes-execute", action.getName());
        Assertions.assertEquals(WatchServices.class, action.getCommand().getClass());
        Assertions.assertEquals("myService", action.getCommand().getParameters().get(KubernetesMessageHeaders.NAME));
        Assertions.assertEquals("myNamespace", action.getCommand().getParameters().get(KubernetesMessageHeaders.NAMESPACE));

        verify(watch, atLeastOnce()).close();
    }
}
