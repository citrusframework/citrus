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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.fabric8.kubernetes.client.dsl.Updatable;
import org.citrusframework.CitrusSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.kubernetes.KubernetesSettings;
import org.citrusframework.kubernetes.KubernetesVariableNames;

public class CreateServiceAction extends AbstractKubernetesAction {

    private final String serviceName;
    private final List<String> ports;
    private final List<String> targetPorts;
    private final String protocol;
    private final Map<String, String> podSelector;

    public CreateServiceAction(Builder builder) {
        super("create-service", builder);

        this.serviceName = builder.serviceName;
        this.ports = builder.ports;
        this.targetPorts = builder.targetPorts;
        this.protocol = builder.protocol;
        this.podSelector = builder.podSelector;
    }

    @Override
    public void doExecute(TestContext context) {
        List<ServicePort> servicePorts = new ArrayList<>();
        for (int i = 0; i < ports.size(); i++) {
            String targetPort;

            if (i < targetPorts.size()) {
                targetPort = targetPorts.get(i);
            } else {
                targetPort = ports.get(i);
            }

            servicePorts.add(new ServicePortBuilder()
                    .withName("port-mapping-" + i)
                    .withProtocol(context.replaceDynamicContentInString(protocol))
                    .withPort(Integer.parseInt(context.replaceDynamicContentInString(ports.get(i))))
                    .withTargetPort(new IntOrString(Integer.parseInt(context.replaceDynamicContentInString(targetPort))))
                    .build());
        }

        Service service = new ServiceBuilder()
                .withNewMetadata()
                    .withNamespace(namespace(context))
                    .withName(context.replaceDynamicContentInString(serviceName))
                    .withLabels(KubernetesSettings.getDefaultLabels())
                .endMetadata()
                .withNewSpec()
                    .withSelector(context.resolveDynamicValuesInMap(podSelector))
                    .withPorts(servicePorts)
                .endSpec()
                .build();

        Service created = getKubernetesClient().services().inNamespace(namespace(context))
                .resource(service)
                .createOr(Updatable::update);

        if (created.getSpec().getClusterIP() != null) {
            context.setVariable(KubernetesVariableNames.SERVICE_CLUSTER_IP.value(), created.getSpec().getClusterIP());
        }
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractKubernetesAction.Builder<CreateServiceAction, Builder> {

        private String serviceName;
        private final List<String> ports = new ArrayList<>();
        private final List<String> targetPorts = new ArrayList<>();
        private String protocol = "TCP";
        private final Map<String, String> podSelector = new HashMap<>();

        public Builder service(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public Builder ports(String... ports) {
            Arrays.stream(ports).forEach(this::port);
            return this;
        }

        public Builder ports(int... ports) {
            Arrays.stream(ports).forEach(this::port);
            return this;
        }

        public Builder port(String port) {
            this.ports.add(port);
            return this;
        }

        public Builder port(int port) {
            this.ports.add(String.valueOf(port));
            return this;
        }

        public Builder portMapping(String port, String targetPort) {
            if (port != null) {
                port(port);
            }

            if (targetPort != null) {
                targetPort(targetPort);
            }
            return this;
        }

        public Builder portMapping(int port, int targetPort) {
            port(port);
            targetPort(targetPort);
            return this;
        }

        public Builder targetPorts(String... targetPorts) {
            Arrays.stream(targetPorts).forEach(this::targetPort);
            return this;
        }

        public Builder targetPorts(int... targetPorts) {
            Arrays.stream(targetPorts).forEach(this::targetPort);
            return this;
        }

        public Builder targetPort(String targetPort) {
            this.targetPorts.add(targetPort);
            return this;
        }

        public Builder targetPort(int targetPort) {
            this.targetPorts.add(String.valueOf(targetPort));
            return this;
        }

        public Builder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder label(String label, String value) {
            this.podSelector.put(label, value);
            return this;
        }

        public Builder withPodSelector(Map<String, String> selector) {
            this.podSelector.putAll(selector);
            return this;
        }

        @Override
        public CreateServiceAction doBuild() {
            if (ports.isEmpty()) {
                ports.add("80");
            }

            if (targetPorts.isEmpty()) {
                targetPorts.add("8080");
            }

            if (podSelector.isEmpty()) {
                // Add default selector to the very specific Pod that is running the test right now.
                // This way the service will route all traffic to the currently running test
                podSelector.put("citrusframework.org/test-id", "${%s}".formatted(CitrusSettings.TEST_NAME_VARIABLE));
            }

            return new CreateServiceAction(this);
        }
    }
}
