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
import org.citrusframework.kubernetes.actions.AgentConnectAction;
import org.citrusframework.kubernetes.actions.AgentDisconnectAction;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.yaml.SchemaProperty;

public class Agent extends AbstractKubernetesAction.Builder<AbstractKubernetesAction, Agent> implements ReferenceResolverAware {

    private AbstractKubernetesAction.Builder<? extends AbstractKubernetesAction, ?> delegate;

    @SchemaProperty
    public void setConnect(Connect connect) {
        AgentConnectAction.Builder builder = new AgentConnectAction.Builder();

        if (connect.getName() != null) {
            builder.agent(connect.getName());
        }
        if (connect.getTestJar() != null) {
            builder.testJar(connect.getTestJar());
        }
        if (connect.getRegistry() != null) {
            builder.registry(connect.getRegistry());
        }
        if (connect.getImage() != null) {
            builder.image(connect.getImage());
        }
        if (connect.getClient() != null) {
            builder.client(connect.getClient());
        }
        if (connect.getPort() != null) {
            builder.port(connect.getPort());
        }
        if (connect.getLocalPort() != null) {
            builder.localPort(connect.getLocalPort());
        }

        this.delegate = builder;
    }

    @SchemaProperty
    public void setDisconnect(Disconnect disconnect) {
        AgentDisconnectAction.Builder builder = new AgentDisconnectAction.Builder();
        builder.service(disconnect.getName());
        this.delegate = builder;
    }

    @Override
    public Agent description(String description) {
        delegate.description(description);
        return this;
    }

    @Override
    public Agent actor(TestActor actor) {
        delegate.actor(actor);
        return this;
    }

    @Override
    public Agent client(KubernetesClient client) {
        delegate.client(client);
        return this;
    }

    @Override
    public Agent inNamespace(String namespace) {
        this.delegate.inNamespace(namespace);
        return this;
    }

    @Override
    public Agent autoRemoveResources(boolean enabled) {
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

    public static class Connect {

        protected String name;
        protected String registry;
        protected String image;
        protected String client;
        protected String port;
        protected String testJar;
        protected String localPort;

        @SchemaProperty
        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String getImage() {
            return image;
        }

        @SchemaProperty
        public void setImage(String image) {
            this.image = image;
        }

        public String getRegistry() {
            return registry;
        }

        @SchemaProperty
        public void setRegistry(String registry) {
            this.registry = registry;
        }

        public String getTestJar() {
            return testJar;
        }

        @SchemaProperty
        public void setTestJar(String testJar) {
            this.testJar = testJar;
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

    public static class Disconnect {

        protected String name;

        @SchemaProperty
        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
