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

public interface KubernetesCustomResourceDeleteActionBuilder<T extends TestAction, B extends KubernetesCustomResourceDeleteActionBuilder<T, B>>
        extends KubernetesActionBuilderBase<T, B> {

    B resourceName(String name);

    B resourceType(Class<?> resourceType);

    B type(Class<?> resourceType);

    B type(String resourceType);

    B kind(String kind);

    B group(String group);

    B version(String version);

    B apiVersion(String apiVersion);
}
