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
import org.citrusframework.knative.actions.eventing.SendEventAction;
import org.citrusframework.kubernetes.ClusterType;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;

@XmlRootElement(name = "send-event")
public class SendEvent extends AbstractKnativeAction.Builder<SendEventAction, SendEvent> implements ReferenceResolverAware {

    private final SendEventAction.Builder delegate = new SendEventAction.Builder();

    @XmlAttribute(required = true)
    public void setBroker(String urlOrName) {
        if (urlOrName.startsWith("http://") || urlOrName.startsWith("https://")) {
            this.delegate.brokerUrl(urlOrName);
        } else {
            this.delegate.broker(urlOrName);
        }
    }

    @XmlAttribute(name = "fork")
    public void setFork(Boolean value) {
        this.delegate.fork(value);
    }

    @XmlAttribute
    public void setTimeout(long timeout) {
        this.delegate.timeout(timeout);
    }

    @XmlElement(name = "event", required = true)
    public void setEvent(Event event) {
        event.getAttributes().getAttributes().forEach(
                attr -> this.delegate.attribute(attr.getName(), attr.getValue())
        );
        this.delegate.eventData(event.getData());
    }

    @Override
    public SendEvent description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public SendEvent actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public SendEvent clusterType(ClusterType clusterType) {
        delegate.clusterType(clusterType);
        return this;
    }

    @Override
    public SendEvent client(KubernetesClient client) {
        delegate.client(client);
        return this;
    }

    @Override
    public SendEvent client(KnativeClient client) {
        delegate.client(client);
        return this;
    }

    @Override
    public SendEvent inNamespace(String namespace) {
        this.delegate.inNamespace(namespace);
        return this;
    }

    @Override
    public SendEvent autoRemoveResources(boolean enabled) {
        this.delegate.autoRemoveResources(enabled);
        return this;
    }

    @Override
    public SendEventAction build() {
        return delegate.build();
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.delegate.setReferenceResolver(referenceResolver);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "attributes",
            "data"
    })
    public static class Event {

        @XmlElement
        protected String data;

        @XmlElement(name = "ce-attributes")
        protected Attributes attributes;

        public Attributes getAttributes() {
            if (attributes == null) {
                attributes = new Attributes();
            }
            return this.attributes;
        }

        public String getData() {
            return data;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
                "attributes",
        })
        public static class Attributes {

            @XmlElement(name = "ce-attribute")
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

    @Override
    protected SendEventAction doBuild() {
        return this.delegate.doBuild();
    }
}
