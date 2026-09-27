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

import java.util.Objects;

import org.citrusframework.util.BehaviorNames;
import org.citrusframework.util.StringUtils;

/**
 * Behavior applies logic to given test action runner.
 */
@FunctionalInterface
public interface TestBehavior {

    /**
     * Behavior building method.
     */
    void apply(TestActionRunner runner);

    /**
     * Name of this behavior in test reports. Defaults to the de-camel-cased simple class name,
     * so {@code PlaceAnOrder} is reported as {@code place an order}. Lambdas and anonymous classes
     * are reported as {@value BehaviorNames#GENERIC_NAME}; use {@link #named(String, TestBehavior)}
     * to give them a name.
     */
    default String getName() {
        return BehaviorNames.of(getClass());
    }

    /**
     * Composes this behavior with given next behavior. The composite applies this behavior first and
     * is reported as {@code "<this name> then <next name>"}.
     */
    default TestBehavior andThen(TestBehavior next) {
        Objects.requireNonNull(next, "Missing next test behavior");

        TestBehavior first = this;
        return new TestBehavior() {
            @Override
            public void apply(TestActionRunner runner) {
                first.apply(runner);
                next.apply(runner);
            }

            @Override
            public String getName() {
                return first.getName() + " then " + next.getName();
            }
        };
    }

    /**
     * Gives given behavior an explicit report name. Mostly useful for lambdas, which cannot name themselves.
     */
    static TestBehavior named(String name, TestBehavior delegate) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Missing test behavior name");
        }

        Objects.requireNonNull(delegate, "Missing test behavior");

        return new TestBehavior() {
            @Override
            public void apply(TestActionRunner runner) {
                delegate.apply(runner);
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }
}
