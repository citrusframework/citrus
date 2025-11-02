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
import org.citrusframework.knative.actions.messaging.CreateChannelAction;
import org.citrusframework.kubernetes.ClusterType;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.yaml.SchemaProperty;

public class CreateChannel extends AbstractKnativeAction.Builder<CreateChannelAction, CreateChannel> {

    private final CreateChannelAction.Builder delegate = new CreateChannelAction.Builder();

    @SchemaProperty
    public void setName(String name) {
        this.delegate.channel(name);
    }

    @Override
    public CreateChannel description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public CreateChannel actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public CreateChannel clusterType(ClusterType clusterType) {
        delegate.clusterType(clusterType);
        return this;
    }

    @Override
    public CreateChannel client(KubernetesClient client) {
        delegate.client(client);
        return this;
    }

    @Override
    public CreateChannel client(KnativeClient client) {
        delegate.client(client);
        return this;
    }

    @Override
    public CreateChannel inNamespace(String namespace) {
        this.delegate.inNamespace(namespace);
        return this;
    }

    @Override
    public CreateChannel autoRemoveResources(boolean enabled) {
        this.delegate.autoRemoveResources(enabled);
        return this;
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.delegate.setReferenceResolver(referenceResolver);
    }

    @Override
    public CreateChannelAction build() {
        return delegate.build();
    }

    @Override
    protected CreateChannelAction doBuild() {
        return this.delegate.doBuild();
    }
}
