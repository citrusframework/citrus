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

package org.citrusframework;

/**
 * Runner adds default alias methods using Gherkin behavior driven development style (GIVEN, WHEN, THEN).
 */
public interface GherkinTestActionRunner extends TestActionRunner {

    /**
     * Behavior driven style alias for run method.
     * @param action
     * @param <T>
     * @return
     */
    default <T extends TestAction> T given(T action) {
        return given((TestActionBuilder<T>) () -> action);
    }

    /**
     * Behavior driven style alias for run method.
     * @param builder
     * @param <T>
     * @return
     */
    default <T extends TestAction> T given(TestActionBuilder<T> builder) {
        return run(builder);
    }

    /**
     * Behavior driven style alias for run method.
     * @param action
     * @param <T>
     * @return
     */
    default <T extends TestAction> T when(T action) {
        return when((TestActionBuilder<T>) () -> action);
    }

    /**
     * Behavior driven style alias for run method.
     * @param builder
     * @param <T>
     * @return
     */
    default <T extends TestAction> T when(TestActionBuilder<T> builder) {
        return run(builder);
    }

    /**
     * Behavior driven style alias for run method.
     * @param action
     * @param <T>
     * @return
     */
    default <T extends TestAction> T then(T action) {
        return then((TestActionBuilder<T>) () -> action);
    }

    /**
     * Behavior driven style alias for run method.
     * @param builder
     * @param <T>
     * @return
     */
    default <T extends TestAction> T then(TestActionBuilder<T> builder) {
        return run(builder);
    }

    /**
     * Behavior driven style alias for run method.
     * @param action
     * @param <T>
     * @return
     */
    default <T extends TestAction> T and(T action) {
        return and((TestActionBuilder<T>) () -> action);
    }

    /**
     * Behavior driven style alias for run method.
     * @param builder
     * @param <T>
     * @return
     */
    default <T extends TestAction> T and(TestActionBuilder<T> builder) {
        return run(builder);
    }
}
