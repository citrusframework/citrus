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
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.citrusframework.TestActor;
import org.citrusframework.kubernetes.actions.AbstractKubernetesAction;
import org.citrusframework.kubernetes.actions.ServiceDisconnectAction;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;

@XmlRootElement(name = "disconnect-service")
public class DisconnectService extends AbstractKubernetesAction.Builder<ServiceDisconnectAction, DisconnectService> implements ReferenceResolverAware {

    private final ServiceDisconnectAction.Builder delegate = new ServiceDisconnectAction.Builder();

    @XmlAttribute(required = true)
    public void setName(String name) {
        this.delegate.service(name);
    }

    @Override
    public DisconnectService description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public DisconnectService actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public DisconnectService client(KubernetesClient client) {
        delegate.client(client);
        return this;
    }

    @Override
    public DisconnectService inNamespace(String namespace) {
        this.delegate.inNamespace(namespace);
        return this;
    }

    @Override
    public DisconnectService autoRemoveResources(boolean enabled) {
        this.delegate.autoRemoveResources(enabled);
        return this;
    }

    @Override
    public void setReferenceResolver(ReferenceResolver referenceResolver) {
        this.delegate.setReferenceResolver(referenceResolver);
    }

    @Override
    public ServiceDisconnectAction doBuild() {
        return delegate.build();
    }

}
