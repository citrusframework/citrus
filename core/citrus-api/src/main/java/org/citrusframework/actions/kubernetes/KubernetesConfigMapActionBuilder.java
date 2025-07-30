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

public interface KubernetesConfigMapActionBuilder<T extends TestAction> {

    /**
     * Create configMap instance.
     * @param configMapName the name of the Kubernetes configMap.
     */
    <B extends KubernetesConfigMapCreateActionBuilder<T, B>> B create(String configMapName);

    /**
     * Add annotation on configMap instance.
     * @param configMapName the name of the Kubernetes configMap.
     */
    <B extends KubernetesCreateAnnotationsActionBuilder<T, B>> B addAnnotation(String configMapName);

    /**
     * Add label on configMap instance.
     * @param configMapName the name of the Kubernetes configMap.
     */
    <B extends KubernetesCreateLabelsActionBuilder<T, B>> B addLabel(String configMapName);

    /**
     * Delete configMap instance.
     * @param configMapName the name of the Kubernetes configMap.
     */
    <B extends KubernetesConfigMapDeleteActionBuilder<T, B>> B delete(String configMapName);
}
