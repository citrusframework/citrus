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
import org.citrusframework.knative.actions.eventing.SendEventAction;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;

public class SendEvent extends AbstractKnativeAction.Builder<SendEventAction, SendEvent> implements ReferenceResolverAware {

    private final SendEventAction.Builder delegate = new SendEventAction.Builder();

    public void setBroker(String urlOrName) {
        if (urlOrName.startsWith("http://") || urlOrName.startsWith("https://")) {
            this.delegate.brokerUrl(urlOrName);
        } else {
            this.delegate.broker(urlOrName);
        }
    }

    public void setFork(Boolean value) {
        this.delegate.fork(value);
    }

    public void setTimeout(long timeout) {
        this.delegate.timeout(timeout);
    }

    public void setEvent(Event event) {
        event.getAttributes().forEach(
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

    public static class Event {

        protected List<Attribute> attributes;
        protected String data;

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

        public void setData(String data) {
            this.data = data;
        }

        public static class Attribute {

            protected String name;
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

    @Override
    protected SendEventAction doBuild() {
        return this.delegate.doBuild();
    }
}