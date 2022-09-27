/*
 * Copyright 2006-2010 the original author or authors.
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

import java.util.Properties;

import com.consol.citrus.AbstractIteratingContainerBuilder;
import com.consol.citrus.CitrusSettings;
import com.consol.citrus.TestActionBuilder;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.util.BooleanExpressionParser;
import com.consol.citrus.validation.matcher.ValidationMatcherUtils;
import org.springframework.util.PropertyPlaceholderHelper;

/**
 * @author Christoph Deppisch
 */
public abstract class AbstractIteratingActionContainer extends AbstractActionContainer {
    /** Boolean expression string */
    protected final String condition;

    /** Optional condition expression evaluates to true or false */
    protected final IteratingConditionExpression conditionExpression;

    /** Name of index variable */
    protected final String indexName;

    /** Cache start index for further container executions - e.g. in loop */
    protected final int start;

    /** Looping index */
    protected int index;

    public AbstractIteratingActionContainer(String name, AbstractIteratingContainerBuilder<?, ?> builder) {
        super(name, builder);

        this.condition = builder.getCondition();
        this.conditionExpression = builder.getConditionExpression();
        this.indexName = builder.getIndexName();
        this.index = builder.getIndex();
        this.start = builder.getStart();
    }

    @Override
    public final void doExecute(TestContext context) {
        index = start;
        executeIteration(context);
    }

    /**
     * Execute embedded actions in loop.
     * @param context TestContext holding variable information.
     */
    protected abstract void executeIteration(TestContext context);

    /**
     * Executes the nested test actions.
     * @param context
     */
    protected void executeActions(TestContext context) {
        context.setVariable(indexName, String.valueOf(index));

        for (TestActionBuilder<?> actionBuilder: actions) {
            executeAction(actionBuilder.build(), context);
        }
    }

    /**
     * Check aborting condition.
     * @return
     */
    protected boolean checkCondition(TestContext context) {
        if (conditionExpression != null) {
            return conditionExpression.evaluate(index, context);
        }

        // replace dynamic content with each iteration
        String conditionString = condition;
        if (conditionString.contains(CitrusSettings.VARIABLE_PREFIX + indexName + CitrusSettings.VARIABLE_SUFFIX)) {
            Properties props = new Properties();
            props.put(indexName, String.valueOf(index));
            conditionString = new PropertyPlaceholderHelper(CitrusSettings.VARIABLE_PREFIX, CitrusSettings.VARIABLE_SUFFIX).replacePlaceholders(conditionString, props);
        }

        conditionString = context.replaceDynamicContentInString(conditionString);

        if (ValidationMatcherUtils.isValidationMatcherExpression(conditionString)) {
            try {
                ValidationMatcherUtils.resolveValidationMatcher("iteratingCondition", String.valueOf(index), conditionString, context);
                return true;
            } catch (AssertionError | ValidationException e) {
                return false;
            }
        }

        if (conditionString.indexOf(indexName) != -1) {
            conditionString = conditionString.replaceAll(indexName, String.valueOf(index));
        }

        return BooleanExpressionParser.evaluate(conditionString);
    }

    @Override
    public boolean isDone(TestContext context) {
        return super.isDone(context) || !checkCondition(context);
    }

    /**
     * Gets the condition.
     * @return the condition
     */
    public String getCondition() {
        return condition;
    }

    /**
     * Gets the condition.
     * @return the conditionExpression
     */
    public IteratingConditionExpression getConditionExpression() {
        return conditionExpression;
    }

    /**
     * Gets the indexName.
     * @return the indexName
     */
    public String getIndexName() {
        return indexName;
    }

    /**
     * Gets the index.
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Gets the start index.
     * @return
     */
    public int getStart() {
        return start;
    }
}
