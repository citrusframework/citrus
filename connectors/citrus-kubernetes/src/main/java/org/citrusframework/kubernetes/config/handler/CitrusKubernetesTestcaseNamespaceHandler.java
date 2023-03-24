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

package org.citrusframework.kubernetes.config.handler;

import org.citrusframework.kubernetes.command.*;
import org.citrusframework.kubernetes.config.xml.*;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class CitrusKubernetesTestcaseNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("info", new KubernetesExecuteActionParser<>(Info.class));
        registerBeanDefinitionParser("list-events", new KubernetesExecuteActionParser<>(ListEvents.class));
        registerBeanDefinitionParser("list-endpoints", new KubernetesExecuteActionParser<>(ListEndpoints.class));
        registerBeanDefinitionParser("list-nodes", new KubernetesExecuteActionParser<>(ListNodes.class));
        registerBeanDefinitionParser("watch-nodes", new KubernetesExecuteActionParser<>(WatchNodes.class));
        registerBeanDefinitionParser("create-service", new CreateServiceActionParser());
        registerBeanDefinitionParser("get-service", new KubernetesExecuteActionParser<>(GetService.class));
        registerBeanDefinitionParser("delete-service", new KubernetesExecuteActionParser<>(DeleteService.class));
        registerBeanDefinitionParser("list-services", new KubernetesExecuteActionParser<>(ListServices.class));
        registerBeanDefinitionParser("watch-services", new KubernetesExecuteActionParser<>(WatchServices.class));
        registerBeanDefinitionParser("list-replication-controllers", new KubernetesExecuteActionParser<>(ListReplicationControllers.class));
        registerBeanDefinitionParser("watch-replication-controllers", new KubernetesExecuteActionParser<>(WatchReplicationControllers.class));
        registerBeanDefinitionParser("list-namespaces", new KubernetesExecuteActionParser<>(ListNamespaces.class));
        registerBeanDefinitionParser("watch-namespaces", new KubernetesExecuteActionParser<>(WatchNamespaces.class));
        registerBeanDefinitionParser("create-pod", new CreatePodActionParser());
        registerBeanDefinitionParser("get-pod", new KubernetesExecuteActionParser<>(GetPod.class));
        registerBeanDefinitionParser("delete-pod", new KubernetesExecuteActionParser<>(DeletePod.class));
        registerBeanDefinitionParser("list-pods", new KubernetesExecuteActionParser<>(ListPods.class));
        registerBeanDefinitionParser("watch-pods", new KubernetesExecuteActionParser<>(WatchPods.class));
    }
}
