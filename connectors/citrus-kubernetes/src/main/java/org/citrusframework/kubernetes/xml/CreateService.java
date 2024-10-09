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
import org.citrusframework.kubernetes.actions.CreateServiceAction;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;

@XmlRootElement(name = "create-service")
public class CreateService extends AbstractKubernetesAction.Builder<CreateServiceAction, CreateService> implements ReferenceResolverAware {

    private final CreateServiceAction.Builder delegate = new CreateServiceAction.Builder();

    @XmlAttribute(required = true)
    public void setName(String name) {
        this.delegate.service(name);
    }

    @XmlAttribute
    public void setServer(String serverName) {
        this.delegate.server(serverName);
    }

    @XmlAttribute(name = "auto-create-server-binding")
    public void setAutoCreateServerBinding(boolean enabled) {
        this.delegate.autoCreateServerBinding(enabled);
    }

    @XmlElement
    public void setPorts(PortMappings portMappings) {
        portMappings.getPorts().forEach(
                mapping -> this.delegate.portMapping(mapping.getPort(), mapping.getTargetPort()));
    }

    @XmlAttribute
    public void setProtocol(String protocol) {
        this.delegate.protocol(protocol);
    }

    @XmlElement(required = true)
    public void setSelector(PodSelector selector) {
        selector.getSelector().forEach(
                label -> this.delegate.label(label.getName(), label.getValue()));

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
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.delegate.setReferenceResolver(referenceResolver);
    }

    @Override
    public CreateServiceAction doBuild() {
        return delegate.build();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "ports"
    })
    public static class PortMappings {

        @XmlElement(name = "port-mapping", required = true)
        protected List<PortMapping> ports;

        public List<PortMapping> getPorts() {
            if (ports == null) {
                ports = new ArrayList<>();
            }
            return this.ports;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class PortMapping {

            @XmlAttribute(required = true)
            protected String port;
            @XmlAttribute(name = "target-port")
            protected String targetPort;

            public void setPort(String port) {
                this.port = port;
            }

            public String getPort() {
                return port;
            }

            public void setTargetPort(String targetPort) {
                this.targetPort = targetPort;
            }

            public String getTargetPort() {
                return targetPort;
            }
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "selector"
    })
    public static class PodSelector {

        @XmlElement(name = "label", required = true)
        protected List<Label> selector;

        public List<Label> getSelector() {
            if (selector == null) {
                selector = new ArrayList<>();
            }
            return this.selector;
        }

        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class Label {

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
