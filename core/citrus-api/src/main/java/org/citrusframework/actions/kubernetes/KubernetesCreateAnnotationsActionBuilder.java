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

import java.util.Map;

import org.citrusframework.TestAction;
import org.citrusframework.kubernetes.KubernetesResourceType;

public interface KubernetesCreateAnnotationsActionBuilder<T extends TestAction, B extends KubernetesCreateAnnotationsActionBuilder<T, B>>
        extends KubernetesActionBuilderBase<T, B> {

    B resource(String resourceName);

    B deployment(String name);

    B pod(String name);

    B secret(String name);

    B configMap(String name);

    B service(String name);

    B type(KubernetesResourceType resourceType);

    B type(String resourceType);

    B annotations(Map<String, String> annotations);

    B annotation(String annotation, String value);
}
