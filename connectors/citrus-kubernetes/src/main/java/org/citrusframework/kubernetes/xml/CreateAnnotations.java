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

import java.util.ArrayList;
import java.util.List;

import io.fabric8.kubernetes.client.KubernetesClient;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.TestActor;
import org.citrusframework.kubernetes.actions.AbstractKubernetesAction;
import org.citrusframework.kubernetes.actions.CreateAnnotationsAction;

@XmlRootElement(name = "create-annotations")
public class CreateAnnotations extends AbstractKubernetesAction.Builder<CreateAnnotationsAction, CreateAnnotations> {

    private final CreateAnnotationsAction.Builder delegate = new CreateAnnotationsAction.Builder();

    @XmlAttribute(required = true)
    public void setResource(String name) {
        this.delegate.resource(name);
    }

    @XmlAttribute(required = true)
    public void setType(String resourceType) {
        this.delegate.type(resourceType);
    }

    @XmlElement
    public void setAnnotations(Annotations annotations) {
        annotations.getAnnotations().forEach(
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

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "annotations"
    })
    public static class Annotations {

        @XmlElement(name = "annotation", required = true)
        protected List<Annotation> annotations;

        public List<Annotation> getAnnotations() {
            if (annotations == null) {
                annotations = new ArrayList<>();
            }
            return this.annotations;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Annotation {

            @XmlAttribute(name = "name", required = true)
            protected String name;
            @XmlAttribute(name = "value")
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
}
