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

package org.citrusframework.container;

import org.citrusframework.AbstractIteratingContainerBuilder;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.context.TestContextFactory;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.util.BooleanExpressionParser;
import org.citrusframework.validation.matcher.ValidationMatcherUtils;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.lang.Thread.currentThread;
import static java.util.Objects.nonNull;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public abstract class AbstractIteratingActionContainer extends AbstractActionContainer {

    /**
     * Boolean expression string
     */
    protected final String condition;

    /**
     * Optional condition expression evaluates to true or false
     */
    protected final IteratingConditionExpression conditionExpression;

    /**
     * Looping index
     */
    protected int index;

    /**
     * Name of index variable
     */
    protected final String indexName;

    /**
     * Cache start index for further container executions - e.g. in loop
     */
    protected final int start;

    /**
     * The maximum duration this iteration can take until it reaches a timeout.
     */
    private final Duration timeout;

    public AbstractIteratingActionContainer(String name, AbstractIteratingContainerBuilder<?, ?> builder) {
        super(name, builder);

        this.condition = builder.getCondition();
        this.conditionExpression = builder.getConditionExpression();
        this.index = builder.getIndex();
        this.indexName = builder.getIndexName();
        this.start = builder.getStart();
        this.timeout = builder.getTimeout();
    }

    @Override
    public final void doExecute(TestContext context) {
        index = start;

        if (nonNull(timeout) && timeout.toMillis() > 0) {
            executeIterationWithTimeout(context);
        } else {
            executeIteration(context);
        }
    }

    private void executeIterationWithTimeout(TestContext context) {
        var executor = newSingleThreadExecutor();

        try {
            var future = executor.submit(() -> executeIteration(context));
            future.get(timeout.toMillis(), MILLISECONDS);
        } catch (ExecutionException | TimeoutException e) {
            throw new CitrusRuntimeException("Iteration reached timeout!", e);
        } catch (InterruptedException e) {
            currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            executor.shutdown();
        }
    }

    @Override
    public boolean isDone(TestContext context) {
        return super.isDone(context) || !checkCondition(context);
    }

    /**
     * @return the condition
     */
    public String getCondition() {
        return condition;
    }

    /**
     * @return the condition expression
     */
    public IteratingConditionExpression getConditionExpression() {
        return conditionExpression;
    }

    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * @return the index name
     */
    public String getIndexName() {
        return indexName;
    }

    /**
     * @return the start index
     */
    public int getStart() {
        return start;
    }

    /**
     * The maximum duration this iteration can take until it reaches a timeout.
     */
    public Duration getTimeout() {
        return timeout;
    }

    /**
     * Execute embedded actions in loop.
     *
     * @param context Test context holding variable information.
     */
    protected abstract void executeIteration(TestContext context);

    /**
     * Executes the nested test actions.
     *
     * @param context Test context holding variable information.
     */
    protected void executeActions(TestContext context) {
        context.setVariable(indexName, String.valueOf(index));

        for (TestActionBuilder<?> actionBuilder : actions) {
            executeAction(actionBuilder.build(), context);
        }
    }

    /**
     * Check aborting condition.
     *
     * @return whether the conditioning has been satisfied.
     */
    protected boolean checkCondition(TestContext context) {
        if (conditionExpression != null) {
            return conditionExpression.evaluate(index, context);
        }

        // replace dynamic content with each iteration
        String conditionString = condition;
        TestContext temp = TestContextFactory.copyOf(context);
        temp.setVariable(indexName, String.valueOf(index));
        conditionString = temp.replaceDynamicContentInString(conditionString);

        if (ValidationMatcherUtils.isValidationMatcherExpression(conditionString)) {
            try {
                ValidationMatcherUtils.resolveValidationMatcher("iteratingCondition", String.valueOf(index), conditionString, context);
                return true;
            } catch (AssertionError | ValidationException e) {
                return false;
            }
        }

        if (conditionString.contains(indexName)) {
            conditionString = conditionString.replaceAll(indexName, String.valueOf(index));
        }

        return BooleanExpressionParser.evaluate(conditionString);
    }
}
