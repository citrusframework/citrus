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

package com.consol.citrus.container;

import com.consol.citrus.TestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.util.BooleanExpressionParser;
import com.consol.citrus.validation.matcher.ValidationMatcherUtils;
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
    private static Logger log = LoggerFactory.getLogger(Conditional.class);

    /** Boolean condition expression string */
    protected String condition;

    /** Optional condition expression evaluates to true or false */
    private ConditionExpression conditionExpression;

    /**
     * Default constructor.
     */
    public Conditional() {
        setName("conditional");
    }

    @Override
    public void doExecute(final TestContext context) {
        if (checkCondition(context)) {
            log.debug("Condition [ {} ] evaluates to true, executing nested actions", condition);

            for (final TestAction action : actions) {
                setActiveAction(action);
                action.execute(context);
            }
        } else {
            log.debug("Condition [ {} ] evaluates to false, not executing nested actions", condition);
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
            } catch (AssertionError e) {
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
     * Condition which allows execution if true.
     * @param condition
     */
    public void setCondition(final String condition) {
        this.condition = condition;
    }

    /**
     * Gets the condition expression.
     * @return the expression
     */
    public String getCondition() {
        return this.condition;
    }

    /**
     * Condition expression allows container execution if evaluates to true.
     * @param conditionExpression
     */
    public void setConditionExpression(ConditionExpression conditionExpression) {
        this.conditionExpression = conditionExpression;
    }

    /**
     * Gets the condition expression.
     * @return the conditionExpression
     */
    public ConditionExpression getConditionExpression() {
        return conditionExpression;
    }

}
