/*
 * Copyright 2006-2016 the original author or authors.
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

import org.citrusframework.context.TestContext;
import org.hamcrest.Matcher;

/**
 * Condition expression that evaluates a Hamcrest matcher. The matcher is invoked with given
 * index of iterating test container.
 *
 * @author Christoph Deppisch
 * @since 2.6
 */
public class HamcrestConditionExpression implements IteratingConditionExpression, ConditionExpression {

    /** Matcher for evaluation */
    private final Matcher<?> conditionMatcher;

    /** Optional value to validate */
    private final Object value;

    /**
     * Default constructor using default value.
     * @param conditionMatcher
     */
    public HamcrestConditionExpression(Matcher<?> conditionMatcher) {
        this(conditionMatcher, null);
    }

    /**
     * Default constructor using fields.
     * @param conditionMatcher
     * @param value
     */
    public HamcrestConditionExpression(Matcher<?> conditionMatcher, Object value) {
        this.conditionMatcher = conditionMatcher;
        this.value = value;
    }

    public static HamcrestConditionExpression assertThat(Matcher<?> conditionMatcher) {
        return new HamcrestConditionExpression(conditionMatcher);
    }

    public static HamcrestConditionExpression assertThat(Object value, Matcher<?> conditionMatcher) {
        return new HamcrestConditionExpression(conditionMatcher, value);
    }

    @Override
    public boolean evaluate(int index, TestContext context) {
        return conditionMatcher.matches(index);
    }

    @Override
    public boolean evaluate(TestContext context) {
        if (value instanceof String) {
            return conditionMatcher.matches(context.replaceDynamicContentInString(value.toString()));
        } else {
            return conditionMatcher.matches(value);
        }
    }
}
