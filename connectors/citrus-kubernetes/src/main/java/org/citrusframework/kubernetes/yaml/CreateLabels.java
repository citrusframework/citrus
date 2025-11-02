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

import java.util.List;

import io.fabric8.kubernetes.client.KubernetesClient;
import org.citrusframework.TestActor;
import org.citrusframework.kubernetes.actions.AbstractKubernetesAction;
import org.citrusframework.kubernetes.actions.CreateLabelsAction;
import org.citrusframework.yaml.SchemaProperty;

public class CreateLabels extends AbstractKubernetesAction.Builder<CreateLabelsAction, CreateLabels> {

    private final CreateLabelsAction.Builder delegate = new CreateLabelsAction.Builder();

    @SchemaProperty
    public void setResource(String name) {
        this.delegate.resource(name);
    }

    @SchemaProperty
    public void setType(String resourceType) {
        this.delegate.type(resourceType);
    }

    @SchemaProperty
    public void setLabels(List<Label> labels) {
        labels.forEach(label -> this.delegate.label(label.getName(), label.getValue()));
    }

    @Override
    public CreateLabels description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public CreateLabels actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public CreateLabels client(KubernetesClient client) {
        delegate.client(client);
        return this;
    }

    @Override
    public CreateLabels inNamespace(String namespace) {
        this.delegate.inNamespace(namespace);
        return this;
    }

    @Override
    public CreateLabels autoRemoveResources(boolean enabled) {
        this.delegate.autoRemoveResources(enabled);
        return this;
    }

    @Override
    public CreateLabelsAction doBuild() {
        return delegate.build();
    }

    public static class Label {

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
