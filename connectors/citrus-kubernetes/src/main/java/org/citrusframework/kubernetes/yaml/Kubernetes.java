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

package org.citrusframework.kubernetes.yaml;

import io.fabric8.kubernetes.client.KubernetesClient;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActionContainerBuilder;
import org.citrusframework.TestActor;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.kubernetes.KubernetesSettings;
import org.citrusframework.kubernetes.actions.AbstractKubernetesAction;
import org.citrusframework.kubernetes.actions.KubernetesAction;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;

public class Kubernetes implements TestActionBuilder<KubernetesAction>, ReferenceResolverAware {

    private AbstractKubernetesAction.Builder<?, ?> builder;

    private String description;
    private String actor;

    private String kubernetesClient;
    private String namespace;

    private boolean autoRemoveResources = KubernetesSettings.isAutoRemoveResources();

    private ReferenceResolver referenceResolver;

    public void setDescription(String value) {
        this.description = description;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setAutoRemove(boolean autoRemoveResources) {
        this.autoRemoveResources = autoRemoveResources;
    }

    public void setClient(String client) {
        this.kubernetesClient = client;
    }

    public void setAgent(Agent builder) {
        this.builder = builder;
    }

    public void setConnect(Connect builder) {
        this.builder = builder;
    }

    public void setDisconnect(Disconnect builder) {
        this.builder = builder;
    }

    public void setCreateService(CreateService builder) {
        this.builder = builder;
    }

    public void setCreateSecret(CreateSecret builder) {
        this.builder = builder;
    }

    public void setCreateConfigMap(CreateConfigMap builder) {
        this.builder = builder;
    }

    public void setCreateResource(CreateResource builder) {
        this.builder = builder;
    }

    public void setCreateCustomResource(CreateCustomResource builder) {
        this.builder = builder;
    }

    public void setCreateLabels(CreateLabels builder) {
        this.builder = builder;
    }

    public void setCreateAnnotations(CreateAnnotations builder) {
        this.builder = builder;
    }

    public void setDeleteService(DeleteService builder) {
        this.builder = builder;
    }

    public void setDeleteSecret(DeleteSecret builder) {
        this.builder = builder;
    }

    public void setDeleteConfigMap(DeleteConfigMap builder) {
        this.builder = builder;
    }

    public void setDeleteResource(DeleteResource builder) {
        this.builder = builder;
    }

    public void setDeleteCustomResource(DeleteCustomResource builder) {
        this.builder = builder;
    }

    public void setVerifyPod(VerifyPod builder) {
        this.builder = builder;
    }

    public void setVerifyCustomResource(VerifyCustomResource builder) {
        this.builder = builder;
    }

    public void setWatchPodLogs(WatchPodLogs builder) {
        this.builder = builder;
    }

    @Override
    public KubernetesAction build() {
        if (builder == null) {
            throw new CitrusRuntimeException("Missing Kubernetes action - please provide proper action details");
        }

        if (builder instanceof TestActionContainerBuilder<?,?>) {
            ((TestActionContainerBuilder<?,?>) builder).getActions().stream()
                    .filter(action -> action instanceof ReferenceResolverAware)
                    .forEach(action -> ((ReferenceResolverAware) action).setReferenceResolver(referenceResolver));
        }

        if (builder instanceof ReferenceResolverAware) {
            ((ReferenceResolverAware) builder).setReferenceResolver(referenceResolver);
        }

        builder.description(description);
        builder.inNamespace(namespace);
        builder.autoRemoveResources(autoRemoveResources);

        if (referenceResolver != null) {
            if (kubernetesClient != null) {
                builder.client(referenceResolver.resolve(kubernetesClient, KubernetesClient.class));
            }

            if (actor != null) {
                builder.actor(referenceResolver.resolve(actor, TestActor.class));
            }
        }

        return builder.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }
}
