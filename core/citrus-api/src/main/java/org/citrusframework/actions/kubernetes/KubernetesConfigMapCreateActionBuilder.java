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

public interface KubernetesConfigMapCreateActionBuilder<T extends TestAction, B extends KubernetesConfigMapCreateActionBuilder<T, B>>
        extends KubernetesActionBuilderBase<T, B> {

    B configMap(String configMapName);

    B fromFile(String filePath);

    B properties(Map<String, String> properties);
}
