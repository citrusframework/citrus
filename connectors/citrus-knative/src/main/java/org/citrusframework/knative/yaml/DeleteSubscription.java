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
import org.citrusframework.TestActor;
import org.citrusframework.knative.actions.AbstractKnativeAction;
import org.citrusframework.knative.actions.DeleteKnativeResourceAction;
import org.citrusframework.kubernetes.ClusterType;
import org.citrusframework.spi.ReferenceResolver;

public class DeleteSubscription extends AbstractKnativeAction.Builder<DeleteKnativeResourceAction, DeleteSubscription> {

    private final DeleteKnativeResourceAction.Builder delegate = new DeleteKnativeResourceAction.Builder();

    public void setName(String name) {
        this.delegate.resource(name);
    }

    @Override
    public DeleteSubscription description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public DeleteSubscription actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public DeleteSubscription clusterType(ClusterType clusterType) {
        delegate.clusterType(clusterType);
        return this;
    }

    @Override
    public DeleteSubscription client(KubernetesClient client) {
        delegate.client(client);
        return this;
    }

    @Override
    public DeleteSubscription client(KnativeClient client) {
        delegate.client(client);
        return this;
    }

    @Override
    public DeleteSubscription inNamespace(String namespace) {
        this.delegate.inNamespace(namespace);
        return this;
    }

    @Override
    public DeleteSubscription autoRemoveResources(boolean enabled) {
        this.delegate.autoRemoveResources(enabled);
        return this;
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.delegate.setReferenceResolver(referenceResolver);
    }

    @Override
    public DeleteKnativeResourceAction build() {
        this.delegate.component("messaging");
        this.delegate.kind("subscriptions");

        return delegate.build();
    }

    @Override
    protected DeleteKnativeResourceAction doBuild() {
        return this.delegate.doBuild();
    }
}
