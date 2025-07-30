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

public interface KubernetesCustomResourceActionBuilder<T extends TestAction> {

    /**
     * Create custom resource instance.
     */
    <B extends KubernetesCustomResourceCreateActionBuilder<T, B>> B create();

    /**
     * Delete custom resource instance.
     * @param name the name of the Kubernetes custom resource.
     */
    <B extends KubernetesCustomResourceDeleteActionBuilder<T, B>> B delete(String name);

    /**
     * Verify that given custom resource matches a condition.
     */
    <B extends KubernetesCustomResourceVerifyActionBuilder<T, B>> B verify();

    /**
     * Verify that given custom resource matches a condition.
     * @param resourceType the type of the customer resource.
     */
    default <B extends KubernetesCustomResourceVerifyActionBuilder<T, B>> B verify(Class<?> resourceType) {
        return (B) verify().resourceType(resourceType);
    }

    /**
     * Verify that given custom resource matches a condition.
     * @param name the name of the custom resource.
     * @param resourceType the type of the customer resource.
     */
    default <B extends KubernetesCustomResourceVerifyActionBuilder<T, B>> B verify(String name, Class<?> resourceType) {
        return (B) verify().resourceType(resourceType).resourceName(name);
    }

    /**
     * Verify that given custom resource matches a condition.
     * @param name the name of the custom resource.
     */
    default <B extends KubernetesCustomResourceVerifyActionBuilder<T, B>> B verify(String name) {
        return (B) verify().resourceName(name);
    }

    /**
     * Verify that given custom resource matches a condition.
     * @param label the label to filter results.
     * @param value the value of the label.
     */
    default <B extends KubernetesCustomResourceVerifyActionBuilder<T, B>> B verify(String label, String value) {
        return (B) verify().label(label, value);
    }
}
