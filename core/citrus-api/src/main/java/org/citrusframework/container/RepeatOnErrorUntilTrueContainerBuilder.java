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

public interface RepeatOnErrorUntilTrueContainerBuilder<T extends TestActionContainer, B extends TestActionContainerBuilder<T, B>>
        extends IteratingContainerBuilder<T, B>, TestActionContainerBuilder<T, B> {

    /**
     * Adds a condition to this iterate container.
     */
    RepeatOnErrorUntilTrueContainerBuilder<T, B> until(String condition);

    /**
     * Adds a condition expression to this iterate container.
     */
    RepeatOnErrorUntilTrueContainerBuilder<T, B> until(IteratingConditionExpression condition);

    /**
     * Sets the auto sleep time in between repeats in milliseconds.
     * @deprecated use {@link RepeatOnErrorUntilTrueContainerBuilder#autoSleep(Duration)} instead
     */
    RepeatOnErrorUntilTrueContainerBuilder<T, B> autoSleep(long autoSleepInMillis);

    /**
     * Sets the sleep interval between retries of this action.
     */
    RepeatOnErrorUntilTrueContainerBuilder<T, B> autoSleep(Duration autoSleep);

    interface BuilderFactory {

        RepeatOnErrorUntilTrueContainerBuilder<?, ?> repeatOnError();

    }
}
