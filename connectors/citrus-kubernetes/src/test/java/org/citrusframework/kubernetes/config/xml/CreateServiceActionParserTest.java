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
import org.citrusframework.kubernetes.command.CreateService;
import org.citrusframework.kubernetes.message.KubernetesMessageHeaders;
import org.citrusframework.junit.jupiter.AbstractActionParserTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CreateServiceActionParserTest extends AbstractActionParserTest<KubernetesExecuteAction> {

    @Test
    public void testCreateServiceActionParser() {
        assertActionCount(2);
        assertActionClassAndName(KubernetesExecuteAction.class, "kubernetes-execute");

        KubernetesExecuteAction action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(CreateService.class, action.getCommand().getClass());
        Assertions.assertEquals(KubernetesClient.class, action.getKubernetesClient().getClass());
        Assertions.assertEquals(0, action.getCommand().getParameters().size());
        Assertions.assertEquals("classpath:templates/hello-service.yml", ((CreateService) action.getCommand()).getTemplate());

        action = getNextTestActionFromTest();
        Assertions.assertNotNull(action.getCommand());
        Assertions.assertEquals(CreateService.class, action.getCommand().getClass());
        Assertions.assertEquals(beanDefinitionContext.getBean("myK8sClient", KubernetesClient.class), action.getKubernetesClient());
        Assertions.assertEquals(3, action.getCommand().getParameters().size());
        Assertions.assertEquals("myService", action.getCommand().getParameters().get(KubernetesMessageHeaders.NAME));
        Assertions.assertEquals("default", action.getCommand().getParameters().get(KubernetesMessageHeaders.NAMESPACE));
        Assertions.assertEquals("service=new", action.getCommand().getParameters().get(KubernetesMessageHeaders.LABEL));
        Assertions.assertNull(((CreateService) action.getCommand()).getTemplate());
        Assertions.assertEquals("8080", ((CreateService) action.getCommand()).getPort());
        Assertions.assertEquals("8080", ((CreateService) action.getCommand()).getTargetPort());
        Assertions.assertEquals("31234", ((CreateService) action.getCommand()).getNodePort());
        Assertions.assertEquals("TCP", ((CreateService) action.getCommand()).getProtocol());
        Assertions.assertEquals("app=myApp", ((CreateService) action.getCommand()).getSelector());
    }
}
