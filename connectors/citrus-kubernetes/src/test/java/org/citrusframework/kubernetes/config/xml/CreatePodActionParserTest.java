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
import org.citrusframework.kubernetes.command.CreatePod;
import org.citrusframework.kubernetes.message.KubernetesMessageHeaders;
import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CreatePodActionParserTest extends AbstractActionParserTest<KubernetesExecuteAction> {

    @Test
    public void testCreatePodActionParser() {
        assertActionCount(2);
        assertActionClassAndName(KubernetesExecuteAction.class, "kubernetes-execute");

        KubernetesExecuteAction action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(CreatePod.class, action.getCommand().getClass());
        Assertions.assertEquals(KubernetesClient.class, action.getKubernetesClient().getClass());
        Assertions.assertEquals(0, action.getCommand().getParameters().size());
        Assertions.assertEquals("classpath:templates/hello-netty.yml", ((CreatePod) action.getCommand()).getTemplate());

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(CreatePod.class, action.getCommand().getClass());
        Assertions.assertEquals(beanDefinitionContext.getBean("myK8sClient", KubernetesClient.class), action.getKubernetesClient());
        Assertions.assertEquals(3, action.getCommand().getParameters().size());
        Assertions.assertEquals("myPod", action.getCommand().getParameters().get(KubernetesMessageHeaders.NAME));
        Assertions.assertEquals("default", action.getCommand().getParameters().get(KubernetesMessageHeaders.NAMESPACE));
        Assertions.assertEquals("pod=new", action.getCommand().getParameters().get(KubernetesMessageHeaders.LABEL));
        Assertions.assertNull(((CreatePod) action.getCommand()).getTemplate());
        Assertions.assertEquals("busybox:latest", ((CreatePod) action.getCommand()).getImage());
        Assertions.assertEquals("myContainer", ((CreatePod) action.getCommand()).getContainerName());
        Assertions.assertEquals("8080", ((CreatePod) action.getCommand()).getContainerPort());
        Assertions.assertEquals("TCP", ((CreatePod) action.getCommand()).getProtocol());
        Assertions.assertEquals("Always", ((CreatePod) action.getCommand()).getPullPolicy());
        Assertions.assertEquals("Always", ((CreatePod) action.getCommand()).getRestartPolicy());
        Assertions.assertEquals("exec", ((CreatePod) action.getCommand()).getContainerCommand());
    }
}
