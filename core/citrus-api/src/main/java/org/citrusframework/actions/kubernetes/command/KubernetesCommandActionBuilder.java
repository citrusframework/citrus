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

package org.citrusframework.actions.kubernetes.command;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;

public interface KubernetesCommandActionBuilder<T extends TestAction, R, O, B extends KubernetesCommandActionBuilder<T, R, O, B>>
        extends TestActionBuilder<T> {

    /**
     * Adds expected command result.
     */
    B result(String result);

    /**
     * Adds JsonPath command result validation.
     */
    B validate(String path, Object value);

    /**
     * Adds command result callback.
     */
    B validate(KubernetesCommandResultCallback<O> callback);

    /**
     * Sets the label parameter.
     */
    B label(String key, String value);

    /**
     * Sets the label parameter.
     */
    B label(String key);

    /**
     * Sets the without label parameter.
     */
    B withoutLabel(String key, String value);

    /**
     * Sets the without label parameter.
     */
    B withoutLabel(String key);
}
