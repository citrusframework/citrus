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

package org.citrusframework.kubernetes.config.xml;

import org.citrusframework.kubernetes.actions.KubernetesExecuteAction;
import org.citrusframework.kubernetes.client.KubernetesClient;
import org.citrusframework.kubernetes.command.*;
import org.citrusframework.kubernetes.message.KubernetesMessageHeaders;
import org.citrusframework.testng.AbstractActionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

public class KubernetesExecuteActionParserTest extends AbstractActionParserTest<KubernetesExecuteAction> {

    @Test
    public void testKubernetesExecuteActionParser() {
        assertActionCount(21);
        assertActionClassAndName(KubernetesExecuteAction.class, "kubernetes-execute");

        KubernetesExecuteAction action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), Info.class);
        Assert.assertEquals(action.getKubernetesClient().getClass(), KubernetesClient.class);
        Assert.assertEquals(action.getCommand().getParameters().size(), 0);

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), Info.class);
        Assert.assertEquals(action.getKubernetesClient(), beanDefinitionContext.getBean("myK8sClient", KubernetesClient.class));
        Assert.assertEquals(action.getCommand().getParameters().size(), 0);

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), ListPods.class);
        Assert.assertEquals(action.getCommand().getParameters().size(), 0);
        Assert.assertEquals(action.getCommandResult(), "{}");
        Assert.assertEquals(action.getCommandResultExpressions().size(), 2L);
        Assert.assertEquals(action.getCommandResultExpressions().get("$.apiVersion"), "v1");
        Assert.assertEquals(action.getCommandResultExpressions().get("$..name.toString()"), "[a,b,c,d]");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), ListEvents.class);
        Assert.assertEquals(action.getCommand().getParameters().size(), 0);

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), ListPods.class);
        Assert.assertEquals(action.getCommand().getParameters().size(), 1);
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.LABEL).toString(), "pod_label");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), CreatePod.class);
        Assert.assertNotNull(((CreatePod) action.getCommand()).getTemplate());
        Assert.assertEquals(action.getCommand().getParameters().size(), 1);
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.NAMESPACE).toString(), "default");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), GetPod.class);
        Assert.assertEquals(action.getCommand().getParameters().size(), 1);
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.NAME).toString(), "myPod");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), DeletePod.class);
        Assert.assertEquals(action.getCommand().getParameters().size(), 1);
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.NAME).toString(), "myPod");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), ListServices.class);
        Assert.assertEquals(action.getCommand().getParameters().size(), 2);
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.LABEL).toString(), "!service_label");
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.NAMESPACE).toString(), "myNamespace");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), CreateService.class);
        Assert.assertNotNull(((CreateService) action.getCommand()).getTemplate());
        Assert.assertEquals(action.getCommand().getParameters().size(), 1);
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.NAMESPACE).toString(), "default");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), GetService.class);
        Assert.assertEquals(action.getCommand().getParameters().size(), 2);
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.NAME).toString(), "myService");
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.NAMESPACE).toString(), "myNamespace");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), DeleteService.class);
        Assert.assertEquals(action.getCommand().getParameters().size(), 2);
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.NAME).toString(), "myService");
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.NAMESPACE).toString(), "myNamespace");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), ListReplicationControllers.class);
        Assert.assertEquals(action.getCommand().getParameters().size(), 1);
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.NAMESPACE).toString(), "myNamespace");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), ListNodes.class);
        Assert.assertEquals(action.getCommand().getParameters().size(), 1);
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.LABEL).toString(), "node_label=active");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), ListEndpoints.class);
        Assert.assertEquals(action.getCommand().getParameters().size(), 1);
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.LABEL).toString(), "endpoint_label!=active");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), ListNamespaces.class);
        Assert.assertEquals(action.getCommand().getParameters().size(), 1);
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.LABEL).toString(), "namespace_label1!=active,namespace_label2=active");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), WatchPods.class);
        Assert.assertEquals(action.getCommand().getParameters().size(), 1);
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.LABEL).toString(), "pod_label");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), WatchServices.class);
        Assert.assertEquals(action.getCommand().getParameters().size(), 3);
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.LABEL).toString(), "!service_label");
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.NAME).toString(), "myService");
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.NAMESPACE).toString(), "myNamespace");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), WatchReplicationControllers.class);
        Assert.assertEquals(action.getCommand().getParameters().size(), 2);
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.NAME).toString(), "myController");
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.NAMESPACE).toString(), "myNamespace");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), WatchNodes.class);
        Assert.assertEquals(action.getCommand().getParameters().size(), 1);
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.LABEL).toString(), "node_label");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), WatchNamespaces.class);
        Assert.assertEquals(action.getCommand().getParameters().size(), 1);
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.LABEL).toString(), "namespace_label");
    }
}
