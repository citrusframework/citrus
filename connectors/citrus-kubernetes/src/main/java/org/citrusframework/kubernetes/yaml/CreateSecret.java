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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.fabric8.kubernetes.client.KubernetesClient;
import org.citrusframework.TestActor;
import org.citrusframework.kubernetes.actions.AbstractKubernetesAction;
import org.citrusframework.kubernetes.actions.CreateSecretAction;

public class CreateSecret extends AbstractKubernetesAction.Builder<CreateSecretAction, CreateSecret> {

    private final CreateSecretAction.Builder delegate = new CreateSecretAction.Builder();

    public void setName(String name) {
        this.delegate.secret(name);
    }

    public void setProperties(List<Property> properties) {
        Map<String, String> props = new HashMap<>();
        properties.forEach(property -> props.put(property.getName(), property.getValue()));
        this.delegate.properties(props);
    }

    public void setFile(String path) {
        delegate.fromFile(path);
    }

    @Override
    public CreateSecret description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public CreateSecret actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public CreateSecret client(KubernetesClient client) {
        delegate.client(client);
        return this;
    }

    @Override
    public CreateSecret inNamespace(String namespace) {
        this.delegate.inNamespace(namespace);
        return this;
    }

    @Override
    public CreateSecret autoRemoveResources(boolean enabled) {
        this.delegate.autoRemoveResources(enabled);
        return this;
    }

    @Override
    public CreateSecretAction doBuild() {
        return delegate.build();
    }

    public static class Property {

        protected String name;
        protected String value;

        public String getName() {
            return name;
        }

        public void setName(String value) {
            this.name = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

    }
}
