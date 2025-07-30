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

package org.citrusframework.actions.kubernetes;

import java.util.Map;

import org.citrusframework.TestAction;
import org.citrusframework.endpoint.Endpoint;

public interface KubernetesServiceCreateActionBuilder<T extends TestAction, B extends KubernetesServiceCreateActionBuilder<T, B>>
        extends KubernetesActionBuilderBase<T, B> {

    B service(String serviceName);

    B ports(String... ports);

    B ports(int... ports);

    B port(String port);

    B port(int port);

    B portMapping(String port, String targetPort);

    B portMapping(int port, int targetPort);

    B targetPorts(String... targetPorts);

    B targetPorts(int... targetPorts);

    B targetPort(String targetPort);

    B targetPort(int targetPort);

    B protocol(String protocol);

    B label(String label, String value);

    B withPodSelector(Map<String, String> selector);

    B server(Endpoint httpServer);

    B server(String httpServerName);

    B autoCreateServerBinding(boolean enabled);
}
