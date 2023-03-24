/*
 * Copyright 2006-2017 the original author or authors.
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
import org.citrusframework.kubernetes.command.CreatePod;
import org.citrusframework.kubernetes.message.KubernetesMessageHeaders;
import org.citrusframework.testng.AbstractActionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CreatePodActionParserTest extends AbstractActionParserTest<KubernetesExecuteAction> {

    @Test
    public void testCreatePodActionParser() {
        assertActionCount(2);
        assertActionClassAndName(KubernetesExecuteAction.class, "kubernetes-execute");

        KubernetesExecuteAction action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), CreatePod.class);
        Assert.assertEquals(action.getKubernetesClient().getClass(), KubernetesClient.class);
        Assert.assertEquals(action.getCommand().getParameters().size(), 0);
        Assert.assertEquals(((CreatePod) action.getCommand()).getTemplate(), "classpath:templates/hello-jetty.yml");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), CreatePod.class);
        Assert.assertEquals(action.getKubernetesClient(), beanDefinitionContext.getBean("myK8sClient", KubernetesClient.class));
        Assert.assertEquals(action.getCommand().getParameters().size(), 3);
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.NAME), "myPod");
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.NAMESPACE), "default");
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.LABEL), "pod=new");
        Assert.assertNull(((CreatePod) action.getCommand()).getTemplate());
        Assert.assertEquals(((CreatePod) action.getCommand()).getImage(), "busybox:latest");
        Assert.assertEquals(((CreatePod) action.getCommand()).getContainerName(), "myContainer");
        Assert.assertEquals(((CreatePod) action.getCommand()).getContainerPort(), "8080");
        Assert.assertEquals(((CreatePod) action.getCommand()).getProtocol(), "TCP");
        Assert.assertEquals(((CreatePod) action.getCommand()).getPullPolicy(), "Always");
        Assert.assertEquals(((CreatePod) action.getCommand()).getRestartPolicy(), "Always");
        Assert.assertEquals(((CreatePod) action.getCommand()).getContainerCommand(), "exec");
    }
}
