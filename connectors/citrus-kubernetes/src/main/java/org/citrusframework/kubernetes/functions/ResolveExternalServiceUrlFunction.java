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

package org.citrusframework.kubernetes.functions;

import java.util.List;
import java.util.Optional;

import io.fabric8.kubernetes.api.model.LoadBalancerStatus;
import io.fabric8.kubernetes.api.model.NodeAddress;
import io.fabric8.kubernetes.api.model.NodeList;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.functions.ParameterizedFunction;
import org.citrusframework.kubernetes.KubernetesSettings;
import org.citrusframework.kubernetes.KubernetesSupport;
import org.citrusframework.yaml.SchemaProperty;

/**
 * Function resolves the URL to a Kubernetes service by inspecting the exposed host ip and port.
 */
public class ResolveExternalServiceUrlFunction implements ParameterizedFunction<ResolveExternalServiceUrlFunction.Parameters> {

    @Override
    public String execute(List<String> parameterList, TestContext context) {
        if (KubernetesSettings.isLocal()) {
            // delegate to arbitrary resolve service function
            return new ResolveServiceUrlFunction().execute(parameterList, context);
        }

        Parameters params = new Parameters();
        params.configure(parameterList, context);
        return execute(params, context);
    }

    @Override
    public String execute(Parameters params, TestContext context) {
        String serviceName = params.getServiceName();

        String namespace = Optional.ofNullable(params.getNamespace())
                                    .orElseGet(() -> KubernetesSupport.getNamespace(context));
        KubernetesClient k8sClient = KubernetesSupport.getKubernetesClient(context);

        Service service = k8sClient.services()
                .inNamespace(namespace)
                .withName(serviceName)
                .get();

        if (service == null) {
            throw new CitrusRuntimeException(String.format("Unable to resolve service instance %s/%s", namespace, serviceName));
        }

        String hostIp = null;
        Integer nodePort = null;
        if ("NodePort".equals(service.getSpec().getType())) {
            List<ServicePort> servicePorts = service.getSpec().getPorts();
            if (servicePorts != null && !servicePorts.isEmpty()) {
                nodePort = servicePorts.get(0).getNodePort();
            }

            if (nodePort == null) {
                throw new CitrusRuntimeException(String.format("Unable to resolve service endpoint URL for service '%s' - " +
                        "failed to determine node port", service.getMetadata().getName()));
            }

            NodeList nodeList = k8sClient.nodes().list();
            if (nodeList != null && !nodeList.getItems().isEmpty() && nodeList.getItems().get(0).getStatus() != null) {
                List<NodeAddress> nodeAddresses = nodeList.getItems().get(0).getStatus().getAddresses();
                if (nodeAddresses != null) {
                    hostIp = nodeAddresses.stream()
                            .filter(nodeAddress -> "ExternalIP".equals(nodeAddress.getType()))
                            .map(NodeAddress::getAddress)
                            .findFirst()
                            .orElse(null);
                }
            }
        } else if ("LoadBalancer".equals(service.getSpec().getType()) && service.getStatus() != null) {
            LoadBalancerStatus loadBalancerStatus = service.getStatus().getLoadBalancer();
            if (loadBalancerStatus != null && loadBalancerStatus.getIngress() != null &&
                    !loadBalancerStatus.getIngress().isEmpty()) {
                hostIp = loadBalancerStatus.getIngress().get(0).getIp();
            }
        } else {
            List<String> externalIps = service.getSpec().getExternalIPs();
            if (externalIps != null && !externalIps.isEmpty()) {
                hostIp = externalIps.get(0);
            }

            List<ServicePort> servicePorts = service.getSpec().getPorts();
            if (servicePorts != null && !servicePorts.isEmpty()) {
                nodePort = servicePorts.get(0).getNodePort();
            }
        }

        if (hostIp == null) {
            throw new CitrusRuntimeException(String.format("Unable to resolve service endpoint URL for service '%s' - " +
                    "failed to determine load balancer ingress ip", service.getMetadata().getName()));
        }

        if (nodePort != null) {
            return "http://%s:%s".formatted(hostIp, nodePort);
        } else {
            return "http://%s".formatted(hostIp);
        }
    }

    @Override
    public Parameters getParameters() {
        return new Parameters();
    }

    public static class Parameters implements FunctionParameters {
        private String serviceName;
        private String namespace;

        @Override
        public void configure(List<String> parameterList, TestContext context) {
            if (parameterList == null || parameterList.isEmpty()) {
                throw new InvalidFunctionUsageException("Function parameters must not be empty");
            }

            setServiceName(parameterList.get(0));

            if (parameterList.size() > 1) {
                setNamespace(parameterList.get(1));
            }
        }

        public String getServiceName() {
            return serviceName;
        }

        @SchemaProperty(required = true, description = "The Kubernetes service name.")
        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getNamespace() {
            return namespace;
        }

        @SchemaProperty(description = "The Kubernetes namespace.")
        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }
    }
}
