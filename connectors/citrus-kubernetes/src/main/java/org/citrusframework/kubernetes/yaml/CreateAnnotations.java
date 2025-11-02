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
import org.citrusframework.kubernetes.actions.CreateAnnotationsAction;
import org.citrusframework.yaml.SchemaProperty;

public class CreateAnnotations extends AbstractKubernetesAction.Builder<CreateAnnotationsAction, CreateAnnotations> {

    private final CreateAnnotationsAction.Builder delegate = new CreateAnnotationsAction.Builder();

    @SchemaProperty
    public void setResource(String name) {
        this.delegate.resource(name);
    }

    @SchemaProperty
    public void setType(String resourceType) {
        this.delegate.type(resourceType);
    }

    @SchemaProperty
    public void setAnnotations(List<Annotation> annotations) {
        annotations.forEach(
                annotation -> this.delegate.annotation(annotation.getName(), annotation.getValue()));
    }

    @Override
    public CreateAnnotations description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public CreateAnnotations actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public CreateAnnotations client(KubernetesClient client) {
        delegate.client(client);
        return this;
    }

    @Override
    public CreateAnnotations inNamespace(String namespace) {
        this.delegate.inNamespace(namespace);
        return this;
    }

    @Override
    public CreateAnnotations autoRemoveResources(boolean enabled) {
        this.delegate.autoRemoveResources(enabled);
        return this;
    }

    @Override
    public CreateAnnotationsAction doBuild() {
        return delegate.build();
    }

    public static class Annotation {

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
