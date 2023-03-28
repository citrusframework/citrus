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
import org.citrusframework.kubernetes.command.CreateService;
import org.citrusframework.kubernetes.message.KubernetesMessageHeaders;
import org.citrusframework.testng.AbstractActionParserTest;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CreateServiceActionParserTest extends AbstractActionParserTest<KubernetesExecuteAction> {

    @Test
    public void testCreateServiceActionParser() {
        assertActionCount(2);
        assertActionClassAndName(KubernetesExecuteAction.class, "kubernetes-execute");

        KubernetesExecuteAction action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), CreateService.class);
        Assert.assertEquals(action.getKubernetesClient().getClass(), KubernetesClient.class);
        Assert.assertEquals(action.getCommand().getParameters().size(), 0);
        Assert.assertEquals(((CreateService) action.getCommand()).getTemplate(), "classpath:templates/hello-jetty-service.yml");

        action = getNextTestActionFromTest();
        Assert.assertNotNull(action.getCommand());
        Assert.assertEquals(action.getCommand().getClass(), CreateService.class);
        Assert.assertEquals(action.getKubernetesClient(), beanDefinitionContext.getBean("myK8sClient", KubernetesClient.class));
        Assert.assertEquals(action.getCommand().getParameters().size(), 3);
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.NAME), "myService");
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.NAMESPACE), "default");
        Assert.assertEquals(action.getCommand().getParameters().get(KubernetesMessageHeaders.LABEL), "service=new");
        Assert.assertNull(((CreateService) action.getCommand()).getTemplate());
        Assert.assertEquals(((CreateService) action.getCommand()).getPort(), "8080");
        Assert.assertEquals(((CreateService) action.getCommand()).getTargetPort(), "8080");
        Assert.assertEquals(((CreateService) action.getCommand()).getNodePort(), "31234");
        Assert.assertEquals(((CreateService) action.getCommand()).getProtocol(), "TCP");
        Assert.assertEquals(((CreateService) action.getCommand()).getSelector(), "app=myApp");
    }
}
