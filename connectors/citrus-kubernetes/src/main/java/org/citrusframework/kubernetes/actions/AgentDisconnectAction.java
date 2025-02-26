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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.citrusframework.context.TestContext;
import org.citrusframework.kubernetes.KubernetesSettings;

/**
 * Action closes port forward for a Kubernetes agent.
 * Resolves port forward from Citrus context and closes it when still alive.
 */
public class AgentDisconnectAction extends ServiceDisconnectAction {

    private final String agentName;

    public AgentDisconnectAction(Builder builder) {
        super("agent-disconnect", builder);

        this.agentName = builder.agentName;
    }

    @Override
    public void doExecute(TestContext context) {
        logger.info("Disconnect from Kubernetes agent '{}'", agentName);

        if (!KubernetesSettings.isLocal()) {
            getKubernetesClient().resourceList(getAgentManifest())
                    .inNamespace(namespace(context))
                    .delete();
        }

        super.doExecute(context);
    }

    private Collection<? extends HasMetadata> getAgentManifest() {
        List<HasMetadata> resources = new ArrayList<>();

        resources.add(new DeploymentBuilder()
                .withNewMetadata()
                    .withName(agentName)
                .endMetadata()
                .build());

        resources.add(new ServiceBuilder()
                .withNewMetadata()
                    .withName(agentName)
                .endMetadata()
                .build());

        return resources;
    }

    /**
     * Action builder.
     */
    public static class Builder extends ServiceDisconnectAction.Builder {

        private String agentName = KubernetesSettings.getServiceName();

        public Builder client(KubernetesClient kubernetesClient) {
            super.client(kubernetesClient);
            return this;
        }

        public Builder service(String name) {
            agent(name);
            return this;
        }

        public Builder agent(String agentName) {
            this.agentName = agentName;
            return this;
        }

        @Override
        public AgentDisconnectAction doBuild() {
            super.service(agentName);
            return new AgentDisconnectAction(this);
        }
    }
}
