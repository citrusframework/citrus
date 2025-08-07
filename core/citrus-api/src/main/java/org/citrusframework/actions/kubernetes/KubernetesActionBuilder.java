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

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;

public interface KubernetesActionBuilder<T extends TestAction, B extends TestActionBuilder.DelegatingTestActionBuilder<T>> {

    /**
     * The Kubernetes client that performs all operations while connected to the cluster.
     */
    KubernetesActionBuilder<T, B> client(Object endpoint);

    /**
     * Performs actions on Kubernetes agent.
     */
    KubernetesAgentActionBuilder agent();

    /**
     * Performs actions on Kubernetes services.
     */
    KubernetesServiceActionBuilder services();

    /**
     * Performs actions on Kubernetes resources.
     */
    KubernetesResourceActionBuilder resources();

    /**
     * Performs actions on Kubernetes pods.
     */
    KubernetesDeploymentActionBuilder deployments();

    /**
     * Performs actions on Kubernetes pods.
     */
    KubernetesPodActionBuilder pods();

    /**
     * Performs actions on Kubernetes custom resources.
     */
    KubernetesCustomResourceActionBuilder customResources();

    /**
     * Performs actions on Kubernetes secrets.
     */
    KubernetesSecretActionBuilder secrets();

    /**
     * Performs actions on Kubernetes config maps.
     */
    KubernetesConfigMapActionBuilder configMaps();

    /**
     * Execute commands with the Kubernetes API.
     */
    KubernetesExecuteActionBuilder<?, ?> execute();

    interface BuilderFactory {

        KubernetesActionBuilder<?, ?> kubernetes();

        default KubernetesActionBuilder<?, ?> k8s() {
            return kubernetes();
        }

    }
}
