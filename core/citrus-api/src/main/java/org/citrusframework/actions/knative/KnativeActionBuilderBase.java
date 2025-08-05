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

package org.citrusframework.actions.knative;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.actions.ActionBuilder;
import org.citrusframework.actions.ReferenceResolverAwareBuilder;
import org.citrusframework.kubernetes.ClusterType;

public interface KnativeActionBuilderBase<T extends TestAction, B extends KnativeActionBuilderBase<T, B>>
        extends ActionBuilder<T, B>, ReferenceResolverAwareBuilder<T, B>, TestActionBuilder<T> {

    /**
     * Use a custom Knative or Kubernetes client.
     */
    B client(Object o);

    /**
     * Use an explicit namespace.
     */
    B inNamespace(String namespace);

    /**
     * Explicitly set cluster type for this action.
     */
    B clusterType(ClusterType clusterType);

    /**
     * Enable/Disable automatic removal of created Knative resources.
     */
    B autoRemoveResources(boolean enabled);
}
