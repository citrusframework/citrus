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

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.functions.ParameterizedFunction;
import org.citrusframework.kubernetes.KubernetesSettings;
import org.citrusframework.kubernetes.KubernetesSupport;
import org.citrusframework.yaml.SchemaProperty;

public class ServiceClusterIpFunction implements ParameterizedFunction<ServiceClusterIpFunction.Parameters> {

    @Override
    public String execute(List<String> parameterList, TestContext context) {
        if (KubernetesSettings.isLocal()) {
            return "127.0.0.1";
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

        String clusterIp = service.getSpec().getClusterIP();
        if (clusterIp != null) {
            return clusterIp;
        }

        if (!service.getSpec().getExternalIPs().isEmpty()) {
            return service.getSpec().getExternalIPs().get(0);
        }

        throw new CitrusRuntimeException(String.format("Unable to resolve cluster ip on service instance %s - no cluster ip set", service.getMetadata().getName()));
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
