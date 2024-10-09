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

public interface TestActionRunner {

    /**
     * Runs given test action.
     * @param action
     * @param <T>
     * @return
     */
    default <T extends TestAction> T run(T action) {
        return run((TestActionBuilder<T>) () -> action);
    }

    /**
     * Runs given test action.
     * @param action
     * @param <T>
     * @return
     */
    default <T extends TestAction> T $(T action) {
        return run((TestActionBuilder<T>) () -> action);
    }

    /**
     * Builds and runs given test action.
     * @param builder
     * @param <T>
     * @return
     */
    default <T extends TestAction> T $(TestActionBuilder<T> builder) {
        return run(builder);
    }

    /**
     * Builds and runs given test action.
     * @param builder
     * @param <T>
     * @return
     */
    <T extends TestAction> T run(TestActionBuilder<T> builder);

    /**
     * Apply test behavior on this test action runner.
     * @param behavior
     * @return
     */
    <T extends TestAction> TestActionBuilder<T> applyBehavior(TestBehavior behavior);
}
