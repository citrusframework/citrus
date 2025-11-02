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
import org.citrusframework.kubernetes.actions.DeleteSecretAction;
import org.citrusframework.yaml.SchemaProperty;

public class DeleteSecret extends AbstractKubernetesAction.Builder<DeleteSecretAction, DeleteSecret> {

    private final DeleteSecretAction.Builder delegate = new DeleteSecretAction.Builder();

    @SchemaProperty
    public void setName(String name) {
        this.delegate.secret(name);
    }

    @Override
    public DeleteSecret description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public DeleteSecret actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public DeleteSecret client(KubernetesClient client) {
        delegate.client(client);
        return this;
    }

    @Override
    public DeleteSecret inNamespace(String namespace) {
        this.delegate.inNamespace(namespace);
        return this;
    }

    @Override
    public DeleteSecret autoRemoveResources(boolean enabled) {
        this.delegate.autoRemoveResources(enabled);
        return this;
    }

    @Override
    public DeleteSecretAction doBuild() {
        return delegate.build();
    }
}
