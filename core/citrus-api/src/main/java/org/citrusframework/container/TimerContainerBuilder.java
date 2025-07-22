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

import org.citrusframework.TestActionContainerBuilder;

public interface TimerContainerBuilder<T extends TestActionContainer, B extends TestActionContainerBuilder<T, B>>
        extends TestActionContainerBuilder<T, B> {

    /**
     * Initial delay in milliseconds before first timer event should fire.
     */
    TimerContainerBuilder<T, B> delay(long delay);

    /**
     * Interval in milliseconds between each timer. As soon as the interval has elapsed the next timer event is fired.
     */
    TimerContainerBuilder<T, B> interval(long interval);

    /**
     * The maximum number of times the timer event is fired. Once this maximum number has been reached the timer is
     * stopped
     */
    TimerContainerBuilder<T, B> repeatCount(int repeatCount);

    /**
     * Fork the timer so that other actions can run in parallel to the nested timer actions
     */
    TimerContainerBuilder<T, B> fork(boolean fork);

    /**
     * Automatically stop the timer when test is finished.
     */
    TimerContainerBuilder<T, B> autoStop(boolean autoStop);

    /**
     * Set the timer's id. This is useful when referencing the timer from other test actions like stop-timer
     * @param timerId a unique timer id within the test context
     */
    TimerContainerBuilder<T, B> id(String timerId);

    /**
     * Set the timer's id. This is useful when referencing the timer from other test actions like stop-timer
     * @param timerId a unique timer id within the test context
     */
    TimerContainerBuilder<T, B> timerId(String timerId);

    interface BuilderFactory {

        TimerContainerBuilder<?, ?> timer();

    }
}
