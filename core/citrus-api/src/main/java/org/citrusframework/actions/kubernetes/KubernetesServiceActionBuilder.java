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

public interface KubernetesServiceActionBuilder<T extends TestAction> {

    /**
     * Connect to given Kubernetes service via local port forward.
     */
    <B extends KubernetesServiceConnectActionBuilder<T,B>> B connect();

    /**
     * Connect to given Kubernetes service via local port forward.
     * @param serviceName the name of the Kubernetes service.
     */
    <B extends KubernetesServiceConnectActionBuilder<T,B>> B connect(String serviceName);

    /**
     * Connect to given Kubernetes service via local port forward.
     */
    <B extends KubernetesServiceDisconnectActionBuilder<T,B>> B disconnect();

    /**
     * Connect to given Kubernetes service via local port forward.
     * @param serviceName the name of the Kubernetes service.
     */
    <B extends KubernetesServiceDisconnectActionBuilder<T,B>> B disconnect(String serviceName);

    /**
     * Create service instance.
     * @param serviceName the name of the Kubernetes service.
     */
    <B extends KubernetesServiceCreateActionBuilder<T,B>> B create(String serviceName);

    /**
     * Add annotation on service instance.
     * @param serviceName the name of the Kubernetes service.
     */
    <B extends KubernetesCreateAnnotationsActionBuilder<T,B>> B addAnnotation(String serviceName);

    /**
     * Add label on service instance.
     * @param serviceName the name of the Kubernetes service.
     */
    <B extends KubernetesCreateLabelsActionBuilder<T,B>> B addLabel(String serviceName);

    /**
     * Delete all service instance in current namespace.
     */
    <B extends KubernetesServiceDeleteActionBuilder<T,B>> B delete();

    /**
     * Delete service instance.
     * @param serviceName the name of the Kubernetes service.
     */
    <B extends KubernetesServiceDeleteActionBuilder<T,B>> B delete(String serviceName);
}
