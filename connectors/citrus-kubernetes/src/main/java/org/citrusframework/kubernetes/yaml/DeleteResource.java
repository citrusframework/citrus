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
import org.citrusframework.kubernetes.actions.DeleteResourceAction;
import org.citrusframework.yaml.SchemaProperty;

public class DeleteResource extends AbstractKubernetesAction.Builder<DeleteResourceAction, DeleteResource> {

    private final DeleteResourceAction.Builder delegate = new DeleteResourceAction.Builder();

    @SchemaProperty
    public void setData(String content) {
        delegate.content(content);
    }

    @SchemaProperty
    public void setFile(String path) {
        delegate.resource(path);
    }

    @Override
    public DeleteResource description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public DeleteResource actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public DeleteResource client(KubernetesClient client) {
        delegate.client(client);
        return this;
    }

    @Override
    public DeleteResource inNamespace(String namespace) {
        this.delegate.inNamespace(namespace);
        return this;
    }

    @Override
    public DeleteResource autoRemoveResources(boolean enabled) {
        this.delegate.autoRemoveResources(enabled);
        return this;
    }

    @Override
    public DeleteResourceAction doBuild() {
        return delegate.build();
    }
}
