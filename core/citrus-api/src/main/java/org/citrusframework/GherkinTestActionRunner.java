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

import java.util.List;

/**
 * Runner adds default alias methods using Gherkin behavior driven development style (GIVEN, WHEN, THEN).
 */
public interface GherkinTestActionRunner extends TestActionRunner {

    /**
     * Behavior driven style alias for run method.
     */
    default <T extends TestAction> GherkinTestActionRunner given(T action) {
        return given((TestActionBuilder<T>) () -> action);
    }

    /**
     * Behavior driven style alias for run method.
     */
    default <T extends TestAction> GherkinTestActionRunner given(TestActionBuilder<T> builder) {
        run(builder);
        return this;
    }

    /**
     * Behavior driven style alias for run method.
     */
    default GherkinTestActionRunner given(List<TestActionBuilder<?>> builders) {
        run(builders);
        return this;
    }

    /**
     * Behavior driven style alias for run method.
     */
    default <T extends TestAction> GherkinTestActionRunner when(T action) {
        return when((TestActionBuilder<T>) () -> action);
    }

    /**
     * Behavior driven style alias for run method.
     */
    default <T extends TestAction> GherkinTestActionRunner when(TestActionBuilder<T> builder) {
        run(builder);
        return this;
    }

    /**
     * Behavior driven style alias for run method.
     */
    default GherkinTestActionRunner when(List<TestActionBuilder<?>> builders) {
        run(builders);
        return this;
    }

    /**
     * Behavior driven style alias for run method.
     */
    default <T extends TestAction> GherkinTestActionRunner then(T action) {
        return then((TestActionBuilder<T>) () -> action);
    }

    /**
     * Behavior driven style alias for run method.
     */
    default <T extends TestAction> GherkinTestActionRunner then(TestActionBuilder<T> builder) {
        run(builder);
        return this;
    }

    /**
     * Behavior driven style alias for run method.
     */
    default GherkinTestActionRunner then(List<TestActionBuilder<?>> builders) {
        run(builders);
        return this;
    }

    /**
     * Behavior driven style alias for run method.
     */
    default <T extends TestAction> GherkinTestActionRunner and(T action) {
        return and((TestActionBuilder<T>) () -> action);
    }

    /**
     * Behavior driven style alias for run method.
     */
    default <T extends TestAction> GherkinTestActionRunner and(TestActionBuilder<T> builder) {
        run(builder);
        return this;
    }

    /**
     * Behavior driven style alias for run method.
     */
    default GherkinTestActionRunner and(List<TestActionBuilder<?>> builders) {
        run(builders);
        return this;
    }
}
