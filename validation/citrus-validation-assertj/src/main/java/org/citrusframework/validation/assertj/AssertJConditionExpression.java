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

import org.citrusframework.api.condition.Condition;
import org.citrusframework.api.container.ConditionExpression;
import org.citrusframework.api.container.IteratingConditionExpression;
import org.citrusframework.context.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container condition backed by an AssertJ check. The condition holds when the check passes; a failed
 * assertion makes the condition {@code false} instead of failing the container.
 * <p>
 * Works with iterating containers (iterate, repeat, repeat-on-error), conditional containers and wait.
 * Without an explicit value, iterating containers check the current index.
 */
public class AssertJConditionExpression implements IteratingConditionExpression, ConditionExpression, Condition {

    private static final Logger logger = LoggerFactory.getLogger(AssertJConditionExpression.class);

    private final AssertJCheck check;
    private final Object value;
    private final boolean hasValue;

    private volatile String lastFailure;

    AssertJConditionExpression(AssertJCheck check) {
        this(check, null, false);
    }

    AssertJConditionExpression(AssertJCheck check, Object value) {
        this(check, value, true);
    }

    private AssertJConditionExpression(AssertJCheck check, Object value, boolean hasValue) {
        this.check = check;
        this.value = value;
        this.hasValue = hasValue;
    }

    @Override
    public boolean evaluate(int index, TestContext context) {
        return test(hasValue ? resolveValue(context) : index, context);
    }

    @Override
    public boolean evaluate(TestContext context) {
        return test(resolveValue(context), context);
    }

    @Override
    public boolean isSatisfied(TestContext context) {
        return evaluate(context);
    }

    @Override
    public String getName() {
        return "assertj-condition";
    }

    @Override
    public String getSuccessMessage(TestContext context) {
        return "AssertJ condition satisfied: " + check;
    }

    @Override
    public String getErrorMessage(TestContext context) {
        return "AssertJ condition not satisfied: " + check + (lastFailure != null ? " - " + lastFailure : "");
    }

    private Object resolveValue(TestContext context) {
        if (value instanceof String expression) {
            return context.replaceDynamicContentInString(expression);
        }

        return value;
    }

    private boolean test(Object actual, TestContext context) {
        try {
            check.check(actual, context);
            lastFailure = null;
            return true;
        } catch (AssertJCheck.TypeMismatchError e) {
            logger.debug("AssertJ condition cannot be checked: {}", e.getMessage());
            lastFailure = e.getMessage();
            return false;
        } catch (AssertionError e) {
            lastFailure = e.getMessage();
            return false;
        }
    }
}
