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

import com.consol.citrus.Citrus;
import com.consol.citrus.TestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.util.BooleanExpressionParser;
import com.consol.citrus.validation.matcher.ValidationMatcherUtils;
import org.springframework.util.PropertyPlaceholderHelper;

import java.util.Properties;

/**
 * @author Christoph Deppisch
 */
public abstract class AbstractIteratingActionContainer extends AbstractActionContainer {
    /** Boolean expression string */
    protected String condition;

    /** Optional condition expression evaluates to true or false */
    protected IteratingConditionExpression conditionExpression;

    /** Name of index variable */
    protected String indexName = "i";

    /** Looping index */
    protected int index;

    /** Cache start index for further container executions - e.g. in loop */
    protected int start = 1;
	
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

        for (TestAction action: actions) {
            setLastExecutedAction(action);
            action.execute(context);
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
        if (conditionString.indexOf(Citrus.VARIABLE_PREFIX + indexName + Citrus.VARIABLE_SUFFIX) != -1) {
            Properties props = new Properties();
            props.put(indexName, String.valueOf(index));
            conditionString = new PropertyPlaceholderHelper(Citrus.VARIABLE_PREFIX, Citrus.VARIABLE_SUFFIX).replacePlaceholders(conditionString, props);
        }

        conditionString = context.replaceDynamicContentInString(conditionString);

        if (ValidationMatcherUtils.isValidationMatcherExpression(conditionString)) {
            try {
                ValidationMatcherUtils.resolveValidationMatcher("iteratingCondition", String.valueOf(index), conditionString, context);
                return true;
            } catch (AssertionError e) {
                return false;
            }
        }

        if (conditionString.indexOf(indexName) != -1) {
            conditionString = conditionString.replaceAll(indexName, String.valueOf(index));
        }

        return BooleanExpressionParser.evaluate(conditionString);
    }
    
    /**
     * Aborting condition.
     * @param condition
     */
    public void setCondition(String condition) {
        this.condition = condition;
    }

    /**
     * Aborting condition expression.
     * @param conditionExpression
     */
    public void setConditionExpression(IteratingConditionExpression conditionExpression) {
        this.conditionExpression = conditionExpression;
    }

    /**
     * Name of index variable.
     * @param indexName
     */
    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }
    
    /**
     * Setter for looping index.
     * @param index the index to set
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Setter for index start.
     * @param start the start index value.
     */
    public void setStart(int start) {
        this.start = start;
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
