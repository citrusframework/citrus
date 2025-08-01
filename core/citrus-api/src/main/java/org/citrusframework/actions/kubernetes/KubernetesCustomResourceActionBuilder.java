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
    KubernetesCustomResourceCreateActionBuilder<?, ?> create();

    /**
     * Delete custom resource instance.
     * @param name the name of the Kubernetes custom resource.
     */
    KubernetesCustomResourceDeleteActionBuilder<?, ?> delete(String name);

    /**
     * Verify that given custom resource matches a condition.
     */
    KubernetesCustomResourceVerifyActionBuilder<?, ?> verify();

    /**
     * Verify that given custom resource matches a condition.
     * @param resourceType the type of the customer resource.
     */
    default KubernetesCustomResourceVerifyActionBuilder<?, ?> verify(Class<?> resourceType) {
        return verify().resourceType(resourceType);
    }

    /**
     * Verify that given custom resource matches a condition.
     * @param name the name of the custom resource.
     * @param resourceType the type of the customer resource.
     */
    default KubernetesCustomResourceVerifyActionBuilder<?, ?> verify(String name, Class<?> resourceType) {
        return verify().resourceType(resourceType).resourceName(name);
    }

    /**
     * Verify that given custom resource matches a condition.
     * @param name the name of the custom resource.
     */
    default KubernetesCustomResourceVerifyActionBuilder<?, ?> verify(String name) {
        return verify().resourceName(name);
    }

    /**
     * Verify that given custom resource matches a condition.
     * @param label the label to filter results.
     * @param value the value of the label.
     */
    default KubernetesCustomResourceVerifyActionBuilder<?, ?> verify(String label, String value) {
        return verify().label(label, value);
    }
}
