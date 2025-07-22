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

import java.time.Duration;

import org.citrusframework.TestActionContainerBuilder;
import org.citrusframework.actions.ActionBuilder;

public interface IteratingContainerBuilder<T extends TestActionContainer, B extends TestActionContainerBuilder<T, B>>
        extends ActionBuilder<T, B> {

    /**
     * Adds a condition to this iterate container.
     */
    B condition(String condition);

    /**
     * Adds a condition expression to this iterate container.
     */
    B condition(IteratingConditionExpression condition);

    /**
     * Sets the index variable name.
     */
    B index(String name);

    /**
     * Sets the timeout.
     */
    B timeout(Duration timeout);

    /**
     * Sets the index start value.
     */
    B startsWith(int index);

}
