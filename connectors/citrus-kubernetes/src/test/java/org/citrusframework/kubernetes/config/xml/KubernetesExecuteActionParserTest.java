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

package org.citrusframework.kubernetes.config.xml;

import org.citrusframework.kubernetes.actions.KubernetesExecuteAction;
import org.citrusframework.kubernetes.client.KubernetesClient;
import org.citrusframework.kubernetes.command.*;
import org.citrusframework.kubernetes.message.KubernetesMessageHeaders;
import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class KubernetesExecuteActionParserTest extends AbstractActionParserTest<KubernetesExecuteAction> {

    @Test
    public void testKubernetesExecuteActionParser() {
        assertActionCount(21);
        assertActionClassAndName(KubernetesExecuteAction.class, "kubernetes-execute");

        KubernetesExecuteAction action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(Info.class, action.getCommand().getClass());
        Assertions.assertEquals(KubernetesClient.class, action.getKubernetesClient().getClass());
        Assertions.assertEquals(0, action.getCommand().getParameters().size());

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(Info.class, action.getCommand().getClass());
        Assertions.assertEquals(beanDefinitionContext.getBean("myK8sClient", KubernetesClient.class), action.getKubernetesClient());
        Assertions.assertEquals(0, action.getCommand().getParameters().size());

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(ListPods.class, action.getCommand().getClass());
        Assertions.assertEquals(0, action.getCommand().getParameters().size());
        Assertions.assertEquals("{}", action.getCommandResult());
        Assertions.assertEquals(2L, action.getCommandResultExpressions().size());
        Assertions.assertEquals("v1", action.getCommandResultExpressions().get("$.apiVersion"));
        Assertions.assertEquals("[a,b,c,d]", action.getCommandResultExpressions().get("$..name.toString()"));

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(ListEvents.class, action.getCommand().getClass());
        Assertions.assertEquals(0, action.getCommand().getParameters().size());

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(ListPods.class, action.getCommand().getClass());
        Assertions.assertEquals(1, action.getCommand().getParameters().size());
        Assertions.assertEquals("pod_label", action.getCommand().getParameters().get(KubernetesMessageHeaders.LABEL).toString());

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(CreatePod.class, action.getCommand().getClass());
        Assertions.assertNotNull(((CreatePod) action.getCommand()).getTemplate());
        Assertions.assertEquals(1, action.getCommand().getParameters().size());
        Assertions.assertEquals("default", action.getCommand().getParameters().get(KubernetesMessageHeaders.NAMESPACE).toString());

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(GetPod.class, action.getCommand().getClass());
        Assertions.assertEquals(1, action.getCommand().getParameters().size());
        Assertions.assertEquals("myPod", action.getCommand().getParameters().get(KubernetesMessageHeaders.NAME).toString());

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(DeletePod.class, action.getCommand().getClass());
        Assertions.assertEquals(1, action.getCommand().getParameters().size());
        Assertions.assertEquals("myPod", action.getCommand().getParameters().get(KubernetesMessageHeaders.NAME).toString());

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(ListServices.class, action.getCommand().getClass());
        Assertions.assertEquals(2, action.getCommand().getParameters().size());
        Assertions.assertEquals("!service_label", action.getCommand().getParameters().get(KubernetesMessageHeaders.LABEL).toString());
        Assertions.assertEquals("myNamespace", action.getCommand().getParameters().get(KubernetesMessageHeaders.NAMESPACE).toString());

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(CreateService.class, action.getCommand().getClass());
        Assertions.assertNotNull(((CreateService) action.getCommand()).getTemplate());
        Assertions.assertEquals(1, action.getCommand().getParameters().size());
        Assertions.assertEquals("default", action.getCommand().getParameters().get(KubernetesMessageHeaders.NAMESPACE).toString());

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(GetService.class, action.getCommand().getClass());
        Assertions.assertEquals(2, action.getCommand().getParameters().size());
        Assertions.assertEquals("myService", action.getCommand().getParameters().get(KubernetesMessageHeaders.NAME).toString());
        Assertions.assertEquals("myNamespace", action.getCommand().getParameters().get(KubernetesMessageHeaders.NAMESPACE).toString());

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(DeleteService.class, action.getCommand().getClass());
        Assertions.assertEquals(2, action.getCommand().getParameters().size());
        Assertions.assertEquals("myService", action.getCommand().getParameters().get(KubernetesMessageHeaders.NAME).toString());
        Assertions.assertEquals("myNamespace", action.getCommand().getParameters().get(KubernetesMessageHeaders.NAMESPACE).toString());

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(ListReplicationControllers.class, action.getCommand().getClass());
        Assertions.assertEquals(1, action.getCommand().getParameters().size());
        Assertions.assertEquals("myNamespace", action.getCommand().getParameters().get(KubernetesMessageHeaders.NAMESPACE).toString());

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(ListNodes.class, action.getCommand().getClass());
        Assertions.assertEquals(1, action.getCommand().getParameters().size());
        Assertions.assertEquals("node_label=active", action.getCommand().getParameters().get(KubernetesMessageHeaders.LABEL).toString());

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(ListEndpoints.class, action.getCommand().getClass());
        Assertions.assertEquals(1, action.getCommand().getParameters().size());
        Assertions.assertEquals("endpoint_label!=active", action.getCommand().getParameters().get(KubernetesMessageHeaders.LABEL).toString());

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(ListNamespaces.class, action.getCommand().getClass());
        Assertions.assertEquals(1, action.getCommand().getParameters().size());
        Assertions.assertEquals("namespace_label1!=active,namespace_label2=active", action.getCommand().getParameters().get(KubernetesMessageHeaders.LABEL).toString());

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(WatchPods.class, action.getCommand().getClass());
        Assertions.assertEquals(1, action.getCommand().getParameters().size());
        Assertions.assertEquals("pod_label", action.getCommand().getParameters().get(KubernetesMessageHeaders.LABEL).toString());

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(WatchServices.class, action.getCommand().getClass());
        Assertions.assertEquals(3, action.getCommand().getParameters().size());
        Assertions.assertEquals("!service_label", action.getCommand().getParameters().get(KubernetesMessageHeaders.LABEL).toString());
        Assertions.assertEquals("myService", action.getCommand().getParameters().get(KubernetesMessageHeaders.NAME).toString());
        Assertions.assertEquals("myNamespace", action.getCommand().getParameters().get(KubernetesMessageHeaders.NAMESPACE).toString());

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(WatchReplicationControllers.class, action.getCommand().getClass());
        Assertions.assertEquals(2, action.getCommand().getParameters().size());
        Assertions.assertEquals("myController", action.getCommand().getParameters().get(KubernetesMessageHeaders.NAME).toString());
        Assertions.assertEquals("myNamespace", action.getCommand().getParameters().get(KubernetesMessageHeaders.NAMESPACE).toString());

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(WatchNodes.class, action.getCommand().getClass());
        Assertions.assertEquals(1, action.getCommand().getParameters().size());
        Assertions.assertEquals("node_label", action.getCommand().getParameters().get(KubernetesMessageHeaders.LABEL).toString());

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(WatchNamespaces.class, action.getCommand().getClass());
        Assertions.assertEquals(1, action.getCommand().getParameters().size());
        Assertions.assertEquals("namespace_label", action.getCommand().getParameters().get(KubernetesMessageHeaders.LABEL).toString());
    }
}
