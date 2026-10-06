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

package org.citrusframework.validation.assertj;

import org.assertj.core.api.Condition;
import org.assertj.core.api.ThrowingConsumer;

/**
 * Static factories for AssertJ based validation in the Citrus Java DSL.
 * <p>
 * Assertion lambdas fail by throwing {@link AssertionError}, so assertions from other
 * libraries such as JUnit Jupiter {@code Assertions} work as well.
 */
public final class AssertJ {

    private AssertJ() {
        // prevent instantiation of utility class
    }

    /**
     * Expected value that hands the received value to the given assertion.
     */
    public static <T> AssertJCheck satisfies(ThrowingConsumer<T> assertion) {
        return AssertJCheck.of(null, assertion);
    }

    /**
     * Expected value that converts the received value to the given type before handing it to the assertion.
     */
    public static <T> AssertJCheck satisfies(Class<T> type, ThrowingConsumer<T> assertion) {
        return AssertJCheck.of(type, assertion);
    }

    /**
     * Container condition that checks the current iteration index with the given assertion.
     */
    public static AssertJConditionExpression conditionOf(ThrowingConsumer<Integer> assertion) {
        return new AssertJConditionExpression(AssertJCheck.of(Integer.class, assertion));
    }

    /**
     * Container condition that checks the current iteration index with the given AssertJ condition.
     */
    public static AssertJConditionExpression conditionOf(Condition<?> condition) {
        return new AssertJConditionExpression(AssertJCheck.of(condition));
    }

    /**
     * Container condition that checks the given value with the assertion. Test variables in a String value are resolved first.
     */
    public static AssertJConditionExpression conditionOf(Object value, ThrowingConsumer<Object> assertion) {
        return new AssertJConditionExpression(AssertJCheck.of(null, assertion), value);
    }

    /**
     * Container condition that resolves test variables in the given value, converts it to the given type and checks it with the assertion.
     */
    public static <T> AssertJConditionExpression conditionOf(Class<T> type, Object value, ThrowingConsumer<T> assertion) {
        return new AssertJConditionExpression(AssertJCheck.of(type, assertion), value);
    }

    /**
     * Container condition that checks the given value with the AssertJ condition. Test variables in a String value are resolved first.
     */
    public static AssertJConditionExpression conditionOf(Object value, Condition<?> condition) {
        return new AssertJConditionExpression(AssertJCheck.of(condition), value);
    }
}
