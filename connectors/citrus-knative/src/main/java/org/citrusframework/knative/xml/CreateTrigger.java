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

package org.citrusframework.knative.xml;

import java.util.ArrayList;
import java.util.List;

import io.fabric8.knative.client.KnativeClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import org.citrusframework.TestActor;
import org.citrusframework.knative.actions.AbstractKnativeAction;
import org.citrusframework.knative.actions.eventing.CreateTriggerAction;
import org.citrusframework.kubernetes.ClusterType;
import org.citrusframework.spi.ReferenceResolver;

@XmlRootElement(name = "create-trigger")
public class CreateTrigger extends AbstractKnativeAction.Builder<CreateTriggerAction, CreateTrigger> {

    private final CreateTriggerAction.Builder delegate = new CreateTriggerAction.Builder();

    @XmlAttribute(required = true)
    public void setName(String name) {
        this.delegate.trigger(name);
    }

    @XmlAttribute
    public void setBroker(String name) {
        this.delegate.broker(name);
    }

    @XmlAttribute
    public void setChannel(String name) {
        this.delegate.channel(name);
    }

    @XmlAttribute
    public void setService(String name) {
        this.delegate.service(name);
    }

    @XmlElement
    public void setFilter(Filter filter) {
        filter.getAttributes().forEach(attr -> this.delegate.filter(attr.getName(), attr.getValue()));
    }

    @Override
    public CreateTrigger description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public CreateTrigger actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public CreateTrigger clusterType(ClusterType clusterType) {
        delegate.clusterType(clusterType);
        return this;
    }

    @Override
    public CreateTrigger client(KubernetesClient client) {
        delegate.client(client);
        return this;
    }

    @Override
    public CreateTrigger client(KnativeClient client) {
        delegate.client(client);
        return this;
    }

    @Override
    public CreateTrigger inNamespace(String namespace) {
        this.delegate.inNamespace(namespace);
        return this;
    }

    @Override
    public CreateTrigger autoRemoveResources(boolean enabled) {
        this.delegate.autoRemoveResources(enabled);
        return this;
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.delegate.setReferenceResolver(referenceResolver);
    }

    @Override
    public CreateTriggerAction build() {
        return delegate.build();
    }

    @Override
    protected CreateTriggerAction doBuild() {
        return this.delegate.doBuild();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "attributes",
    })
    public static class Filter {

        @XmlElement(name = "attribute")
        protected List<Attribute> attributes;

        public List<Attribute> getAttributes() {
            if (attributes == null) {
                attributes = new ArrayList<>();
            }
            return this.attributes;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Attribute {

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
