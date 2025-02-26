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

package org.citrusframework.kubernetes.actions;

import io.fabric8.kubernetes.client.LocalPortForward;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.http.client.HttpClient;
import org.citrusframework.http.client.HttpClientBuilder;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.kubernetes.KubernetesSettings;
import org.citrusframework.util.StringUtils;

import static org.citrusframework.kubernetes.actions.KubernetesActionBuilder.kubernetes;

/**
 * Action connects the test to a Kubernetes service so clients may invoke the service.
 * This is for services that are only accessible from within the cluster (e.g. service type is ClusterIP).
 * The test action connects to the service via port forwarding and exposes a Http client to the Citrus context
 * so other test actions can access the service running in Kubernetes.
 */
public class ServiceConnectAction extends AbstractKubernetesAction {

    protected final String clientName;
    protected final String serviceName;
    protected final String port;
    protected final String localPort;

    protected ServiceConnectAction(String name, Builder builder) {
        super(name, builder);

        this.serviceName = builder.serviceName;
        this.port = builder.port;
        this.clientName = builder.clientName;
        this.localPort = builder.localPort;
    }

    public ServiceConnectAction(Builder builder) {
        this("service-connect", builder);
    }

    @Override
    public void doExecute(TestContext context) {
        if (KubernetesSettings.isLocal()) {
            if (context.getReferenceResolver().isResolvable(serviceName, HttpServer.class)) {
                HttpServer server = context.getReferenceResolver().resolve(serviceName, HttpServer.class);
                exposeServiceClient(context, server.getPort());
            }

            return;
        }

        LocalPortForward portForward;
        if (StringUtils.hasText(localPort)) {
            portForward = getKubernetesClient().services()
                    .inNamespace(namespace(context))
                    .withName(serviceName)
                    .portForward(Integer.parseInt(context.replaceDynamicContentInString(port)), Integer.parseInt(context.replaceDynamicContentInString(localPort)));
        } else {
            portForward = getKubernetesClient().services()
                    .inNamespace(namespace(context))
                    .withName(serviceName)
                    .portForward(Integer.parseInt(context.replaceDynamicContentInString(port)));
        }

        if (context.getReferenceResolver().isResolvable(clientName)) {
            throw new CitrusRuntimeException("Failed to bind Kubernetes service client '%s' - client already exists".formatted(clientName));
        }

        exposeServiceClient(context, portForward.getLocalPort());

        if (context.getReferenceResolver().isResolvable(serviceName + ":port-forward")) {
            throw new CitrusRuntimeException("Failed to bind Kubernetes service port forward '%s' - already exists".formatted(serviceName + ":port-forward"));
        }
        context.getReferenceResolver().bind(serviceName + ":port-forward", portForward);

        if (isAutoRemoveResources()) {
            context.doFinally(kubernetes().client(getKubernetesClient())
                    .services()
                    .disconnect(serviceName)
                    .inNamespace(namespace(context)));
        }
    }

    private void exposeServiceClient(TestContext context, int localPort) {
        if (context.getReferenceResolver().isResolvable(clientName, HttpClient.class)) {
            HttpClient serviceClient = context.getReferenceResolver().resolve(clientName, HttpClient.class);
            serviceClient.getEndpointConfiguration().setRequestUrl("http://localhost:%d".formatted(localPort));
        } else {
            HttpClient serviceClient = new HttpClientBuilder()
                    .requestUrl("http://localhost:%d".formatted(localPort))
                    .build();
            context.getReferenceResolver().bind(clientName, serviceClient);
        }
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractKubernetesAction.Builder<ServiceConnectAction, Builder> {

        private String clientName;
        protected String localPort;
        private String serviceName = KubernetesSettings.getServiceName();
        private String port = "8080";

        public Builder service(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public Builder client(String clientName) {
            this.clientName = clientName;
            return this;
        }

        public Builder port(String port) {
            this.port = port;
            return this;
        }

        public Builder port(int port) {
            this.port = String.valueOf(port);
            return this;
        }

        public Builder portMapping(String port, String localPort) {
            if (port != null) {
                port(port);
            }

            if (localPort != null) {
                localPort(localPort);
            }
            return this;
        }

        public Builder portMapping(int port, int localPort) {
            port(port);
            localPort(localPort);
            return this;
        }

        public Builder localPort(String localPort) {
            this.localPort = localPort;
            return this;
        }

        public Builder localPort(int localPort) {
            this.localPort = String.valueOf(localPort);
            return this;
        }

        @Override
        public ServiceConnectAction doBuild() {
            if (clientName == null) {
                client(serviceName + ".client");
            }

            return new ServiceConnectAction(this);
        }
    }
}
