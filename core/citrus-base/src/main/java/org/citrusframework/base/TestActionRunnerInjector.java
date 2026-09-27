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

package org.citrusframework.base;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.TestActionContainerBuilder;
import org.citrusframework.TestActionRunner;
import org.citrusframework.TestActionRunnerAware;

/**
 * Injects the active test action runner into a builder and into every builder nested in it,
 * following container builders and delegating builders.
 */
final class TestActionRunnerInjector {

    private TestActionRunnerInjector() {
        //prevent instantiation of utility class
    }

    static void inject(TestActionBuilder<?> builder, TestActionRunner runner) {
        inject(builder, runner, Collections.newSetFromMap(new IdentityHashMap<>()));
    }

    private static void inject(TestActionBuilder<?> builder, TestActionRunner runner, Set<TestActionBuilder<?>> visited) {
        if (builder == null || !visited.add(builder)) {
            return;
        }

        if (builder instanceof TestActionRunnerAware runnerAware) {
            runnerAware.setTestActionRunner(runner);
        }

        if (builder instanceof TestActionBuilder.DelegatingTestActionBuilder<?> delegating) {
            inject(delegating.getDelegate(), runner, visited);
        }

        if (builder instanceof TestActionContainerBuilder<?, ?> container) {
            for (TestActionBuilder<?> nested : container.getActions()) {
                inject(nested, runner, visited);
            }
        }
    }
}
