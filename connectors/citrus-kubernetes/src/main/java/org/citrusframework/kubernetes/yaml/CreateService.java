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

import java.util.ArrayList;
import java.util.List;

import io.fabric8.kubernetes.client.KubernetesClient;
import org.citrusframework.TestActor;
import org.citrusframework.kubernetes.actions.AbstractKubernetesAction;
import org.citrusframework.kubernetes.actions.CreateServiceAction;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.yaml.SchemaProperty;

public class CreateService extends AbstractKubernetesAction.Builder<CreateServiceAction, CreateService> implements ReferenceResolverAware {

    private final CreateServiceAction.Builder delegate = new CreateServiceAction.Builder();

    @SchemaProperty
    public void setName(String name) {
        this.delegate.service(name);
    }

    @SchemaProperty
    public void setServer(String serverName) {
        this.delegate.server(serverName);
    }

    @SchemaProperty
    public void setAutoCreateServerBinding(boolean enabled) {
        this.delegate.autoCreateServerBinding(enabled);
    }

    @SchemaProperty
    public void setPorts(List<PortMapping> portMappings) {
        portMappings.forEach(
                mapping -> this.delegate.portMapping(mapping.getPort(), mapping.getTargetPort()));
    }

    @SchemaProperty
    public void setProtocol(String protocol) {
        this.delegate.protocol(protocol);
    }

    @SchemaProperty
    public void setSelector(PodSelector selector) {
        selector.getLabels().forEach(
                label -> this.delegate.label(label.getName(), label.getValue()));

    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.delegate.setReferenceResolver(referenceResolver);
    }

    @Override
    public CreateService description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public CreateService actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public CreateService client(KubernetesClient client) {
        delegate.client(client);
        return this;
    }

    @Override
    public CreateService inNamespace(String namespace) {
        this.delegate.inNamespace(namespace);
        return this;
    }

    @Override
    public CreateService autoRemoveResources(boolean enabled) {
        this.delegate.autoRemoveResources(enabled);
        return this;
    }

    @Override
    public CreateServiceAction doBuild() {
        return delegate.build();
    }

    public static class PortMapping {

        protected String port;
        protected String targetPort;

        @SchemaProperty
        public void setPort(String port) {
            this.port = port;
        }

        public String getPort() {
            return port;
        }

        @SchemaProperty
        public void setTargetPort(String targetPort) {
            this.targetPort = targetPort;
        }

        public String getTargetPort() {
            return targetPort;
        }
    }

    public static class PodSelector {

        protected List<Label> labels;

        public List<Label> getLabels() {
            if (labels == null) {
                labels = new ArrayList<>();
            }
            return this.labels;
        }

        @SchemaProperty
        public void setLabels(List<Label> labels) {
            this.labels = labels;
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
}
