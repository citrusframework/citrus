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

public interface KubernetesResourceActionBuilder {

    /**
     * Create any Kubernetes resource instance from yaml.
     */
    KubernetesResourceCreateActionBuilder<?, ?> create();

    /**
     * Add annotation on resource instance.
     * @param resourceName the name of the Kubernetes resource.
     * @param resourceType the type of the Kubernetes resource.
     */
    KubernetesCreateAnnotationsActionBuilder<?, ?> addAnnotation(String resourceName, String resourceType);

    /**
     * Add label on resource instance.
     * @param resourceName the name of the Kubernetes resource.
     * @param resourceType the type of the Kubernetes resource.
     */
    KubernetesCreateLabelsActionBuilder<?, ?> addLabel(String resourceName, String resourceType);

    /**
     * Delete any Kubernetes resource instance.
     * @param content the Kubernetes resource as YAML content.
     */
    KubernetesResourceDeleteActionBuilder<?, ?> delete(String content);
}
