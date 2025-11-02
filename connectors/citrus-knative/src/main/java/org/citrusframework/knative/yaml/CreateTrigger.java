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

package org.citrusframework.knative.yaml;

import java.util.ArrayList;
import java.util.List;

import io.fabric8.knative.client.KnativeClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.citrusframework.TestActor;
import org.citrusframework.knative.actions.AbstractKnativeAction;
import org.citrusframework.knative.actions.eventing.CreateTriggerAction;
import org.citrusframework.kubernetes.ClusterType;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.yaml.SchemaProperty;

public class CreateTrigger extends AbstractKnativeAction.Builder<CreateTriggerAction, CreateTrigger> {

    private final CreateTriggerAction.Builder delegate = new CreateTriggerAction.Builder();

    @SchemaProperty
    public void setName(String name) {
        this.delegate.trigger(name);
    }

    @SchemaProperty
    public void setBroker(String name) {
        this.delegate.broker(name);
    }

    @SchemaProperty
    public void setChannel(String name) {
        this.delegate.channel(name);
    }

    @SchemaProperty
    public void setService(String name) {
        this.delegate.service(name);
    }

    @SchemaProperty
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
    public CreateTriggerAction build() {
        return delegate.build();
    }

    public static class Filter {

        protected List<Attribute> attributes;

        @SchemaProperty
        public void setAttributes(List<Attribute> attributes) {
            this.attributes = attributes;
        }

        public List<Attribute> getAttributes() {
            if (attributes == null) {
                attributes = new ArrayList<>();
            }
            return this.attributes;
        }

        public static class Attribute {

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

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.delegate.setReferenceResolver(referenceResolver);
    }

    @Override
    protected CreateTriggerAction doBuild() {
        return this.delegate.doBuild();
    }
}
