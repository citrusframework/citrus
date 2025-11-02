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
import org.citrusframework.knative.actions.eventing.DeleteBrokerAction;
import org.citrusframework.kubernetes.ClusterType;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.yaml.SchemaProperty;

public class DeleteBroker extends AbstractKnativeAction.Builder<DeleteBrokerAction, DeleteBroker> {

    private final DeleteBrokerAction.Builder delegate = new DeleteBrokerAction.Builder();

    @SchemaProperty
    public void setName(String name) {
        this.delegate.broker(name);
    }

    @Override
    public DeleteBroker description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public DeleteBroker actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public DeleteBroker clusterType(ClusterType clusterType) {
        delegate.clusterType(clusterType);
        return this;
    }

    @Override
    public DeleteBroker client(KubernetesClient client) {
        delegate.client(client);
        return this;
    }

    @Override
    public DeleteBroker client(KnativeClient client) {
        delegate.client(client);
        return this;
    }

    @Override
    public DeleteBroker inNamespace(String namespace) {
        this.delegate.inNamespace(namespace);
        return this;
    }

    @Override
    public DeleteBroker autoRemoveResources(boolean enabled) {
        this.delegate.autoRemoveResources(enabled);
        return this;
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.delegate.setReferenceResolver(referenceResolver);
    }

    @Override
    public DeleteBrokerAction build() {
        return delegate.build();
    }

    @Override
    protected DeleteBrokerAction doBuild() {
        return this.delegate.doBuild();
    }
}
