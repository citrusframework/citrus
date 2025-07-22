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

package org.citrusframework.container;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActionContainerBuilder;

public interface AsyncContainerBuilder<T extends TestActionContainer, B extends TestActionContainerBuilder<T, B>>
        extends TestActionContainerBuilder<T, B> {

    /**
     * Adds a error action.
     * @param action
     * @return
     */
    AsyncContainerBuilder<T, B> errorAction(TestAction action);

    /**
     * Adds a success action.
     * @param action
     * @return
     */
    AsyncContainerBuilder<T, B> successAction(TestAction action);

    /**
     * Adds a error action.
     * @param action
     * @return
     */
    AsyncContainerBuilder<T, B> errorAction(TestActionBuilder<?> action);

    /**
     * Adds a success action.
     * @param action
     * @return
     */
    AsyncContainerBuilder<T, B> successAction(TestActionBuilder<?> action);

    /**
     * Adds one to many error actions.
     * @param actions
     * @return
     */
    AsyncContainerBuilder<T, B> errorActions(TestActionBuilder<?>... actions);

    /**
     * Adds one to many success actions.
     * @param actions
     * @return
     */
    AsyncContainerBuilder<T, B> successActions(TestActionBuilder<?>... actions);

    /**
     * Adds one to many error actions.
     * @param actions
     * @return
     */
    AsyncContainerBuilder<T, B> errorActions(TestAction... actions);

    /**
     * Adds one to many success actions.
     * @param actions
     * @return
     */
    AsyncContainerBuilder<T, B> successActions(TestAction... actions);

    interface BuilderFactory {

        AsyncContainerBuilder<?, ?> async();

    }
}
