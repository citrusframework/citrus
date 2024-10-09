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

import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.citrusframework.TestActor;
import org.citrusframework.kubernetes.actions.AbstractKubernetesAction;
import org.citrusframework.kubernetes.actions.DeleteCustomResourceAction;

public class DeleteCustomResource extends AbstractKubernetesAction.Builder<DeleteCustomResourceAction, DeleteCustomResource> {

    private final DeleteCustomResourceAction.Builder delegate = new DeleteCustomResourceAction.Builder();

    public void setName(String name) {
        this.delegate.resourceName(name);
    }

    public void setType(String resourceType) {
        try {
            delegate.resourceType((Class<CustomResource<?, ?>>) Class.forName(resourceType));
        } catch(ClassNotFoundException | ClassCastException e) {
            delegate.type(resourceType);
        }
    }

    public void setKind(String kind) {
        delegate.kind(kind);
    }

    public void setGroup(String group) {
        delegate.group(group);
    }

    public void setVersion(String version) {
        delegate.version(version);
    }

    public void setApiVersion(String apiVersion) {
        delegate.apiVersion(apiVersion);
    }

    @Override
    public DeleteCustomResource description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public DeleteCustomResource actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public DeleteCustomResource client(KubernetesClient client) {
        delegate.client(client);
        return this;
    }

    @Override
    public DeleteCustomResource inNamespace(String namespace) {
        this.delegate.inNamespace(namespace);
        return this;
    }

    @Override
    public DeleteCustomResource autoRemoveResources(boolean enabled) {
        this.delegate.autoRemoveResources(enabled);
        return this;
    }

    @Override
    public DeleteCustomResourceAction doBuild() {
        return delegate.build();
    }
}
