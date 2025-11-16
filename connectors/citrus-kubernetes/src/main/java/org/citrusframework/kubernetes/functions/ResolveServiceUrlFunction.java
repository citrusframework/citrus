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
import java.util.Locale;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.InvalidFunctionUsageException;
import org.citrusframework.functions.ParameterizedFunction;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.kubernetes.KubernetesSettings;
import org.citrusframework.yaml.SchemaProperty;

import static org.citrusframework.kubernetes.KubernetesSupport.getNamespace;

/**
 * Function resolves URL to a Kubernetes service. Supports different modes such as local and in-cluster.
 * In local mode constructs a localhost service URL with a given local service port.
 * The in-cluster mode uses the service name and the current namespace to construct a Kubernetes service URL.
 */
public class ResolveServiceUrlFunction implements ParameterizedFunction<ResolveServiceUrlFunction.Parameters> {

    @Override
    public String execute(Parameters params, TestContext context) {
        String serviceName = params.getServiceName();
        int servicePort = params.getServicePort();

        String scheme = "http://";
        if (params.isSecure()) {
            scheme = "https://";
        }

        if (KubernetesSettings.isLocal()) {
            if (context.getReferenceResolver().isResolvable(serviceName)) {
                HttpServer server = context.getReferenceResolver().resolve(serviceName, HttpServer.class);
                servicePort = server.getPort();
            }

            return String.format("%slocalhost%s", scheme, servicePort > 0 ? ":" + servicePort : "");
        } else {
            return String.format("%s%s.%s", scheme, serviceName, getNamespace(context));
        }
    }

    @Override
    public Parameters getParameters() {
        return new Parameters();
    }

    public static class Parameters implements FunctionParameters {
        private String serviceName;
        private int servicePort = 0;
        private boolean secure = false;

        @Override
        public void configure(List<String> parameterList, TestContext context) {
            if (parameterList == null || parameterList.isEmpty()) {
                throw new InvalidFunctionUsageException("Function parameters must not be empty");
            }

            setServiceName(parameterList.get(0));

            if (parameterList.size() > 1) {
                try {
                    setServicePort(Integer.parseInt(parameterList.get(1)));
                } catch (IllegalArgumentException e) {
                    setSecure(Boolean.parseBoolean(parameterList.get(1).toLowerCase(Locale.US)));
                }
            }

            if (parameterList.size() > 2) {
                setSecure(Boolean.parseBoolean(parameterList.get(2).toLowerCase(Locale.US)));
            }
        }

        public String getServiceName() {
            return serviceName;
        }

        @SchemaProperty(required = true, description = "The service name.")
        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public int getServicePort() {
            return servicePort;
        }

        @SchemaProperty(required = true, description = "The service port.")
        public void setServicePort(int servicePort) {
            this.servicePort = servicePort;
        }

        public boolean isSecure() {
            return secure;
        }

        @SchemaProperty(description = "When enabled use secure Http connection.")
        public void setSecure(boolean secure) {
            this.secure = secure;
        }
    }
}
