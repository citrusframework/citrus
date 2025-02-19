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
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.kubernetes.actions.AbstractKubernetesAction;
import org.citrusframework.kubernetes.actions.ServiceDisconnectAction;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;

@XmlRootElement(name = "disconnect")
public class Disconnect extends AbstractKubernetesAction.Builder<AbstractKubernetesAction, Disconnect> implements ReferenceResolverAware {

    private AbstractKubernetesAction.Builder<? extends AbstractKubernetesAction, ?> delegate;

    @XmlElement
    public void setService(Service service) {
        ServiceDisconnectAction.Builder builder = new ServiceDisconnectAction.Builder();
        builder.service(service.getName());
        this.delegate = builder;
    }

    @Override
    public Disconnect description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public Disconnect actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public Disconnect client(KubernetesClient client) {
        delegate.client(client);
        return this;
    }

    @Override
    public Disconnect inNamespace(String namespace) {
        this.delegate.inNamespace(namespace);
        return this;
    }

    @Override
    public Disconnect autoRemoveResources(boolean enabled) {
        this.delegate.autoRemoveResources(enabled);
        return this;
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.delegate.setReferenceResolver(referenceResolver);
    }

    @Override
    public AbstractKubernetesAction doBuild() {
        if (delegate == null) {
            throw new CitrusRuntimeException("Missing Kubernetes disconnect action - please provide proper action details");
        }

        return delegate.build();
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {})
    public static class Service {

        @XmlAttribute(required = true)
        protected String name;

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
