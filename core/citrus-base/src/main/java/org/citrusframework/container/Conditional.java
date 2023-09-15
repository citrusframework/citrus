/*
 * Copyright 2006-2011 the original author or authors.
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

import org.citrusframework.AbstractTestContainerBuilder;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.util.BooleanExpressionParser;
import org.citrusframework.validation.matcher.ValidationMatcherUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class executes nested test actions if condition expression evaluates to true.
 *
 * @author Matthias Beil, Christoph Deppisch
 * @since 1.2
 */
public class Conditional extends AbstractActionContainer {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(Conditional.class);

    /** Boolean condition expression string */
    private final String condition;

    /** Optional condition expression evaluates to true or false */
    private final ConditionExpression conditionExpression;

    /**
     * Default constructor.
     */
    public Conditional(Builder builder) {
        super("conditional", builder);

        this.condition = builder.condition;
        this.conditionExpression = builder.conditionExpression;
    }

    @Override
    public void doExecute(final TestContext context) {
        if (checkCondition(context)) {
            logger.debug("Condition [ {} ] evaluates to true, executing nested actions", condition);

            for (TestActionBuilder<?> actionBuilder : actions) {
                executeAction(actionBuilder.build(), context);
            }
        } else {
            logger.debug("Condition [ {} ] evaluates to false, not executing nested actions", condition);
        }
    }

    /**
     * Evaluates condition expression and returns boolean representation.
     * @param context
     * @return
     */
    private boolean checkCondition(TestContext context) {
        if (conditionExpression != null) {
            return conditionExpression.evaluate(context);
        }

        // replace dynamic content with each iteration
        String conditionString = context.replaceDynamicContentInString(condition);
        if (ValidationMatcherUtils.isValidationMatcherExpression(conditionString)) {
            try {
                ValidationMatcherUtils.resolveValidationMatcher("iteratingCondition", "", conditionString, context);
                return true;
            } catch (AssertionError | ValidationException e) {
                return false;
            }
        }

        return BooleanExpressionParser.evaluate(conditionString);
    }

    @Override
    public boolean isDone(TestContext context) {
        return super.isDone(context) || !checkCondition(context);
    }

    /**
     * Gets the condition expression.
     * @return the expression
     */
    public String getCondition() {
        return this.condition;
    }

    /**
     * Gets the condition expression.
     * @return the conditionExpression
     */
    public ConditionExpression getConditionExpression() {
        return conditionExpression;
    }


    /**
     * Action builder.
     */
    public static class Builder extends AbstractTestContainerBuilder<Conditional, Builder> {

        protected String condition;
        private ConditionExpression conditionExpression;

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        public static Builder conditional() {
            return new Builder();
        }

        /**
         * Condition which allows execution if true.
         * @param expression
         */
        public Builder when(String expression) {
            this.condition = expression;
            return this;
        }

        /**
         * Condition which allows execution if evaluates to true.
         * @param expression
         */
        public Builder when(ConditionExpression expression) {
            this.conditionExpression = expression;
            return this;
        }

        @Override
        public Conditional doBuild() {
            return new Conditional(this);
        }
    }
}
