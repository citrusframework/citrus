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

import io.fabric8.kubernetes.client.KubernetesClient;
import org.citrusframework.TestActor;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.kubernetes.actions.AbstractKubernetesAction;
import org.citrusframework.kubernetes.actions.ServiceConnectAction;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.yaml.SchemaProperty;

public class Connect extends AbstractKubernetesAction.Builder<AbstractKubernetesAction, Connect> implements ReferenceResolverAware {

    private AbstractKubernetesAction.Builder<? extends AbstractKubernetesAction, ?> delegate;

    @SchemaProperty
    public void setService(Service service) {
        ServiceConnectAction.Builder builder = new ServiceConnectAction.Builder();

        builder.service(service.getName());
        if (service.getClient() != null) {
            builder.client(service.getClient());
        }
        if (service.getPort() != null) {
            builder.port(service.getPort());
        }
        if (service.getLocalPort() != null) {
            builder.localPort(service.getLocalPort());
        }

        this.delegate = builder;
    }

    @Override
    public Connect description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public Connect actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public Connect client(KubernetesClient client) {
        delegate.client(client);
        return this;
    }

    @Override
    public Connect inNamespace(String namespace) {
        this.delegate.inNamespace(namespace);
        return this;
    }

    @Override
    public Connect autoRemoveResources(boolean enabled) {
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
            throw new CitrusRuntimeException("Missing Kubernetes connect action - please provide proper action details");
        }

        return delegate.build();
    }

    public static class Service {

        protected String name;
        protected String client;
        protected String port;
        protected String localPort;

        @SchemaProperty
        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @SchemaProperty
        public void setClient(String client) {
            this.client = client;
        }

        public String getClient() {
            return client;
        }

        @SchemaProperty
        public void setPort(String port) {
            this.port = port;
        }

        public String getPort() {
            return port;
        }

        @SchemaProperty
        public void setLocalPort(String localPort) {
            this.localPort = localPort;
        }

        public String getLocalPort() {
            return localPort;
        }
    }
}
