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

import java.io.IOException;

import io.fabric8.kubernetes.client.LocalPortForward;
import org.citrusframework.context.TestContext;
import org.citrusframework.kubernetes.KubernetesSettings;

/**
 * Action closes port forward for a Kubernetes service.
 * Resolves port forward from Citrus context and closes it when still alive.
 */
public class ServiceDisconnectAction extends AbstractKubernetesAction {

    private final String serviceName;

    protected ServiceDisconnectAction(String name, Builder builder) {
        super(name, builder);

        this.serviceName = builder.serviceName;
    }

    public ServiceDisconnectAction(Builder builder) {
        this("service-disconnect", builder);
    }

    @Override
    public void doExecute(TestContext context) {
        logger.info("Disconnect from Kubernetes service '{}'", serviceName);

        if (KubernetesSettings.isLocal()) {
            return;
        }

        if (context.getReferenceResolver().isResolvable(serviceName + ":port-forward", LocalPortForward.class)) {
            LocalPortForward portForward = context.getReferenceResolver().resolve(serviceName + ":port-forward", LocalPortForward.class);
            try {
                if (portForward.isAlive()) {
                    portForward.close();
                    logger.info("Successfully disconnected from Kubernetes service '{}'", serviceName);
                }
            } catch (IOException e) {
                logger.warn("Failed to close local port forward for Kubernetes service '{}'", serviceName);
            }
        } else {
            logger.warn("Failed to disconnect from Kubernetes service '{}' - no port forward available", serviceName);
        }
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractKubernetesAction.Builder<ServiceDisconnectAction, Builder> {

        private String serviceName = KubernetesSettings.getServiceName();

        public Builder service(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        @Override
        public ServiceDisconnectAction doBuild() {
            return new ServiceDisconnectAction(this);
        }
    }
}
