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

import java.io.Closeable;

import org.citrusframework.TestAction;

public interface KubernetesAgentActionBuilder<T extends TestAction> {

    /**
     * Create new agent deployment and connect to given Kubernetes service via local port forward.
     */
    <B extends KubernetesAgentConnectActionBuilder<T, B>> B connect();

    /**
     * Create new agent deployment and connect to given Kubernetes service via local port forward.
     */
    <B extends KubernetesAgentConnectActionBuilder<T, B>> B connect(String agentName);

    /**
     * Disconnect from given Kubernetes agent.
     */
    <B extends KubernetesAgentDisconnectActionBuilder<T, B>> B disconnect();

    /**
     * Disconnect from given Kubernetes agent.
     */
    <B extends KubernetesAgentDisconnectActionBuilder<T, B>> B disconnect(String agentName);

}
