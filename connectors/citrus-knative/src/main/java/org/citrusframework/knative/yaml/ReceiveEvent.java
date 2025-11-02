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
import org.citrusframework.knative.actions.eventing.ReceiveEventAction;
import org.citrusframework.kubernetes.ClusterType;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.yaml.SchemaProperty;

public class ReceiveEvent extends AbstractKnativeAction.Builder<ReceiveEventAction, ReceiveEvent> implements ReferenceResolverAware {

    private final ReceiveEventAction.Builder delegate = new ReceiveEventAction.Builder();

    @SchemaProperty
    public void setService(String name) {
        this.delegate.serviceName(name);
    }

    @SchemaProperty
    public void setPort(int port) {
        this.delegate.servicePort(port);
    }

    @SchemaProperty
    public void setTimeout(long timeout) {
        this.delegate.timeout(timeout);
    }

    @SchemaProperty
    public void setEvent(Event event) {
        event.getAttributes().forEach(
                attr -> this.delegate.attribute(attr.getName(), attr.getValue())
        );
        this.delegate.eventData(event.getData());
    }

    @Override
    public ReceiveEvent description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public ReceiveEvent actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public ReceiveEvent clusterType(ClusterType clusterType) {
        delegate.clusterType(clusterType);
        return this;
    }

    @Override
    public ReceiveEvent client(KubernetesClient client) {
        delegate.client(client);
        return this;
    }

    @Override
    public ReceiveEvent client(KnativeClient client) {
        delegate.client(client);
        return this;
    }

    @Override
    public ReceiveEvent inNamespace(String namespace) {
        this.delegate.inNamespace(namespace);
        return this;
    }

    @Override
    public ReceiveEvent autoRemoveResources(boolean enabled) {
        this.delegate.autoRemoveResources(enabled);
        return this;
    }

    @Override
    public ReceiveEventAction build() {
        return delegate.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.delegate.setReferenceResolver(referenceResolver);
    }

    public static class Event {

        protected String data;

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

        public String getData() {
            return data;
        }

        @SchemaProperty
        public void setData(String data) {
            this.data = data;
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
    protected ReceiveEventAction doBuild() {
        return this.delegate.doBuild();
    }
}
