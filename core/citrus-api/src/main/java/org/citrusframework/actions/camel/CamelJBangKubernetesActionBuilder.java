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

package org.citrusframework.actions.camel;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.ReferenceResolverAwareBuilder;

public interface CamelJBangKubernetesActionBuilder<T extends TestAction, B extends CamelJBangKubernetesActionBuilder<T, B>>
        extends ReferenceResolverAwareBuilder<T, B>, TestActionBuilder<T> {

    /**
     * Build and deploy a Camel project to Kubernetes
     */
    CamelKubernetesIntegrationRunActionBuilder<?, ?> run();

    /**
     * Delete a deployed Camel application using the previous exported Kubernetes manifest.
     */
    CamelKubernetesIntegrationDeleteActionBuilder<?, ?> delete();

    /**
     * Verify the Kubernetes pod status and logs of a deployed Camel integration.
     */
    CamelKubernetesIntegrationVerifyActionBuilder<?, ?> verify();

    /**
     * Export a Camel project from given Camel integration.
     */
    CamelKubernetesIntegrationRunActionBuilder<?, ?> export();
}
