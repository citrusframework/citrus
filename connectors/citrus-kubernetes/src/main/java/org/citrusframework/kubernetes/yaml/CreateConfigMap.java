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
import org.citrusframework.kubernetes.actions.CreateConfigMapAction;
import org.citrusframework.yaml.SchemaProperty;

public class CreateConfigMap extends AbstractKubernetesAction.Builder<CreateConfigMapAction, CreateConfigMap> {

    private final CreateConfigMapAction.Builder delegate = new CreateConfigMapAction.Builder();

    @SchemaProperty
    public void setName(String name) {
        this.delegate.configMap(name);
    }

    @SchemaProperty
    public void setProperties(List<Property> properties) {
        Map<String, String> props = new HashMap<>();
        properties.forEach(property -> props.put(property.getName(), property.getValue()));
        this.delegate.properties(props);
    }

    @SchemaProperty
    public void setFile(String path) {
        delegate.fromFile(path);
    }

    @Override
    public CreateConfigMap description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public CreateConfigMap actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public CreateConfigMap client(KubernetesClient client) {
        delegate.client(client);
        return this;
    }

    @Override
    public CreateConfigMap inNamespace(String namespace) {
        this.delegate.inNamespace(namespace);
        return this;
    }

    @Override
    public CreateConfigMap autoRemoveResources(boolean enabled) {
        this.delegate.autoRemoveResources(enabled);
        return this;
    }

    @Override
    public CreateConfigMapAction doBuild() {
        return delegate.build();
    }

    public static class Property {

        protected String name;
        protected String value;

        public String getName() {
            return name;
        }

        @SchemaProperty
        public void setName(String value) {
            this.name = value;
        }

        public String getValue() {
            return value;
        }

        @SchemaProperty
        public void setValue(String value) {
            this.value = value;
        }

    }
}
