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

package org.citrusframework.knative.yaml;

import io.fabric8.knative.client.KnativeClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActionContainerBuilder;
import org.citrusframework.TestActor;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.knative.KnativeSettings;
import org.citrusframework.knative.actions.AbstractKnativeAction;
import org.citrusframework.knative.actions.KnativeAction;
import org.citrusframework.kubernetes.ClusterType;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;

public class Knative implements TestActionBuilder<KnativeAction>, ReferenceResolverAware {

    private AbstractKnativeAction.Builder<?, ?> builder;

    private String description;
    private String actor;

    private String k8sClient;
    private String knativeClient;
    private String namespace;
    private boolean autoRemoveResources = KnativeSettings.isAutoRemoveResources();

    private ClusterType clusterType;

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

    public void setClusterType(ClusterType clusterType) {
        this.clusterType = clusterType;
    }

    public void setKubernetesClient(String client) {
        this.k8sClient = client;
    }

    public void setClient(String client) {
        this.knativeClient = client;
    }

    public void setCreateBroker(CreateBroker builder) {
        this.builder = builder;
    }

    public void setDeleteBroker(DeleteBroker builder) {
        this.builder = builder;
    }

    public void setVerifyBroker(VerifyBroker builder) {
        this.builder = builder;
    }

    public void setCreateTrigger(CreateTrigger builder) {
        this.builder = builder;
    }

    public void setDeleteTrigger(DeleteTrigger builder) {
        this.builder = builder;
    }

    public void setCreateChannel(CreateChannel builder) {
        this.builder = builder;
    }

    public void setDeleteChannel(DeleteChannel builder) {
        this.builder = builder;
    }

    public void setCreateSubscription(CreateSubscription builder) {
        this.builder = builder;
    }

    public void setDeleteSubscription(DeleteSubscription builder) {
        this.builder = builder;
    }

    public void setSendEvent(SendEvent builder) {
        this.builder = builder;
    }

    public void setReceiveEvent(ReceiveEvent builder) {
        this.builder = builder;
    }

    public void setDeleteResource(DeleteResource builder) {
        this.builder = builder;
    }

    @Override
    public KnativeAction build() {
        if (builder == null) {
            throw new CitrusRuntimeException("Missing Knative action - please provide proper action details");
        }

        if (builder instanceof TestActionContainerBuilder<?,?>) {
            ((TestActionContainerBuilder<?,?>) builder).getActions().stream()
                    .filter(action -> action instanceof ReferenceResolverAware)
                    .forEach(action -> ((ReferenceResolverAware) action).setReferenceResolver(referenceResolver));
        }

        builder.setReferenceResolver(referenceResolver);

        builder.description(description);
        builder.inNamespace(namespace);
        builder.autoRemoveResources(autoRemoveResources);

        if (clusterType != null) {
            builder.clusterType(clusterType);
        }

        if (referenceResolver != null) {
            if (k8sClient != null) {
                builder.client(referenceResolver.resolve(k8sClient, KubernetesClient.class));
            }

            if (knativeClient != null) {
                builder.client(referenceResolver.resolve(knativeClient, KnativeClient.class));
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
