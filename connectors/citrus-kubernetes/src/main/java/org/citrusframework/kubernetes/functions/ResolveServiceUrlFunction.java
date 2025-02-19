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
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.functions.Function;
import org.citrusframework.http.server.HttpServer;
import org.citrusframework.kubernetes.KubernetesSettings;

import static org.citrusframework.kubernetes.KubernetesSupport.getNamespace;

/**
 * Function resolves URL to a Kubernetes service. Supports different modes such as local and in-cluster.
 * In local mode constructs a localhost service URL with a given local service port.
 * The in-cluster mode uses the service name and the current namespace to construct a Kubernetes service URL.
 */
public class ResolveServiceUrlFunction implements Function {

    @Override
    public String execute(List<String> parameterList, TestContext context) {
        if (parameterList.isEmpty()) {
            throw new CitrusRuntimeException("Missing service name for resolve function");
        }

        String serviceName = parameterList.get(0);

        boolean secure = false;
        int servicePort = 0;
        if (parameterList.size() > 1) {
            try {
                servicePort = Integer.parseInt(parameterList.get(1));
            } catch (IllegalArgumentException e) {
                secure = Boolean.parseBoolean(parameterList.get(1).toLowerCase(Locale.US));
            }
        }

        if (parameterList.size() > 2) {
            secure = Boolean.parseBoolean(parameterList.get(2).toLowerCase(Locale.US));
        }

        String scheme = "http://";
        if (secure) {
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
}
