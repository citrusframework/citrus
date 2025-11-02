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
import org.citrusframework.TestActor;
import org.citrusframework.kubernetes.actions.AbstractKubernetesAction;
import org.citrusframework.kubernetes.actions.CreateCustomResourceAction;
import org.citrusframework.yaml.SchemaProperty;

public class CreateCustomResource extends AbstractKubernetesAction.Builder<CreateCustomResourceAction, CreateCustomResource> {

    private final CreateCustomResourceAction.Builder delegate = new CreateCustomResourceAction.Builder();

    @SchemaProperty
    public void setData(String content) {
        delegate.content(content);
    }

    @SchemaProperty
    public void setType(String resourceType) {
        try {
            delegate.resourceType(Class.forName(resourceType));
        } catch(ClassNotFoundException | ClassCastException e) {
            delegate.type(resourceType);
        }
    }

    @SchemaProperty
    public void setKind(String kind) {
        delegate.kind(kind);
    }

    @SchemaProperty
    public void setGroup(String group) {
        delegate.group(group);
    }

    @SchemaProperty
    public void setVersion(String version) {
        delegate.version(version);
    }

    @SchemaProperty
    public void setApiVersion(String apiVersion) {
        delegate.apiVersion(apiVersion);
    }

    @SchemaProperty
    public void setFile(String path) {
        delegate.file(path);
    }

    @Override
    public CreateCustomResource description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public CreateCustomResource actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public CreateCustomResource client(KubernetesClient client) {
        delegate.client(client);
        return this;
    }

    @Override
    public CreateCustomResource inNamespace(String namespace) {
        this.delegate.inNamespace(namespace);
        return this;
    }

    @Override
    public CreateCustomResource autoRemoveResources(boolean enabled) {
        this.delegate.autoRemoveResources(enabled);
        return this;
    }

    @Override
    public CreateCustomResourceAction doBuild() {
        return delegate.build();
    }
}
