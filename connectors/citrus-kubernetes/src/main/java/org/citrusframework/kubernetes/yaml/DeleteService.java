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
import org.citrusframework.kubernetes.actions.DeleteServiceAction;
import org.citrusframework.api.yaml.SchemaProperty;

import java.util.ArrayList;
import java.util.List;

public class DeleteService extends AbstractKubernetesAction.Builder<DeleteServiceAction, DeleteService> {

    private final DeleteServiceAction.Builder delegate = new DeleteServiceAction.Builder();

    @SchemaProperty(description = "Name of the Kubernetes Service to delete.")
    public void setName(String name) {
        this.delegate.service(name);
    }

    @SchemaProperty(description = "Expected command result as JSON.")
    public void setResult(String result) {
        this.delegate.result(result);
    }

    @SchemaProperty(description = "Path expressions to validate on the command result.")
    public void setValidate(List<Validation> validations) {
        validations.forEach(validation -> this.delegate.validate(validation.getPath(), validation.getValue()));
    }

    @Override
    public DeleteService description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public DeleteService actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public DeleteService client(KubernetesClient client) {
        delegate.client(client);
        return this;
    }

    @Override
    public DeleteService inNamespace(String namespace) {
        this.delegate.inNamespace(namespace);
        return this;
    }

    @Override
    public DeleteService autoRemoveResources(boolean enabled) {
        this.delegate.autoRemoveResources(enabled);
        return this;
    }

    @Override
    public DeleteServiceAction doBuild() {
        return delegate.build();
    }

    public static class Validation {
        private String path;
        private String value;

        @SchemaProperty(description = "JsonPath expression to validate.")
        public void setPath(String path) {
            this.path = path;
        }

        @SchemaProperty(description = "Expected value for the path expression.")
        public void setValue(String value) {
            this.value = value;
        }

        public String getPath() {
            return path;
        }

        public String getValue() {
            return value;
        }
    }
}
