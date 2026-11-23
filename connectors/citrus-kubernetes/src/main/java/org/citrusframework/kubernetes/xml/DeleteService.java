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

package org.citrusframework.kubernetes.xml;

import io.fabric8.kubernetes.client.KubernetesClient;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.TestActor;
import org.citrusframework.kubernetes.actions.AbstractKubernetesAction;
import org.citrusframework.kubernetes.actions.DeleteServiceAction;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "delete-service")
public class DeleteService extends AbstractKubernetesAction.Builder<DeleteServiceAction, DeleteService> {

    private final DeleteServiceAction.Builder delegate = new DeleteServiceAction.Builder();

    private String result;

    private List<Validation> validations = new ArrayList<>();

    @XmlAttribute(required = true)
    public void setName(String name) {
        this.delegate.service(name);
    }

    @XmlAttribute
    public void setResult(String result) {
        this.result = result;
    }

    @XmlElement(name = "validate")
    public void setValidations(List<Validation> validations) {
        this.validations = validations;
    }

    List<Validation> getValidations() {
        return validations;
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
        if (org.citrusframework.util.StringUtils.hasText(result)) {
            delegate.result(result);
        }

        validations.forEach(validation -> delegate.validate(validation.getPath(), validation.getValue()));
        return delegate.build();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Validation {

        @XmlAttribute(required = true)
        protected String path;
        @XmlAttribute
        protected String value;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
