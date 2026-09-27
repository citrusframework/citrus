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

package org.citrusframework.api.actions;

import java.util.List;

import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActionRunner;
import org.citrusframework.TestBehavior;

public interface ApplyTestBehaviorActionBuilder<T extends TestAction>
        extends ActionBuilder<T, ApplyTestBehaviorActionBuilder<T>>, TestActionBuilder<T> {

    ApplyTestBehaviorActionBuilder<T> behavior(TestBehavior behavior);

    ApplyTestBehaviorActionBuilder<T> on(TestActionRunner runner);

    /**
     * Applies the behavior on a copy of the test context, so its variables do not affect the calling test.
     * Use {@link #publish(String...)} to hand variables back.
     */
    ApplyTestBehaviorActionBuilder<T> isolated();

    /**
     * Sets whether the behavior shares the variables of the calling test. Enabled by default.
     */
    ApplyTestBehaviorActionBuilder<T> globalContext(boolean enabled);

    /**
     * Copies given variables from an isolated behavior back to the calling test once the behavior is done.
     */
    ApplyTestBehaviorActionBuilder<T> publish(String... variableNames);

    /**
     * Declares variables the behavior needs. Applying the behavior without them fails before any of its
     * actions run.
     */
    ApplyTestBehaviorActionBuilder<T> requires(String... variableNames);

    interface BuilderFactory {

        ApplyTestBehaviorActionBuilder<?> apply();

        default ApplyTestBehaviorActionBuilder<?> apply(TestBehavior behavior) {
            return apply().behavior(behavior);
        }

        /**
         * Applies given behaviors one after another as a single composite behavior.
         */
        default ApplyTestBehaviorActionBuilder<?> apply(List<TestBehavior> behaviors) {
            return apply(behaviors.stream()
                    .reduce(TestBehavior::andThen)
                    .orElseThrow(() -> new IllegalArgumentException("Missing test behaviors to apply")));
        }
    }

}
