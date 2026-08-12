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

package org.citrusframework.api.container;

import org.citrusframework.context.TestContext;

/**
 * Condition expression that limits iteration to a fixed number of executions.
 * Supports both "while" semantics (for iterate containers) and "until" semantics
 * (for repeat/repeatOnError containers).
 */
public class IterationCountConditionExpression implements IteratingConditionExpression {

    private final int numberOfIterations;
    private final boolean untilSemantics;

    private IterationCountConditionExpression(int numberOfIterations, boolean untilSemantics) {
        if (numberOfIterations <= 0) {
            throw new IllegalArgumentException("Number of iterations must be a positive integer");
        }
        this.numberOfIterations = numberOfIterations;
        this.untilSemantics = untilSemantics;
    }

    /**
     * Creates a condition for "while" style containers (e.g. iterate).
     * Returns true while the index has not exceeded the iteration count.
     */
    public static IterationCountConditionExpression whileCount(int numberOfIterations) {
        return new IterationCountConditionExpression(numberOfIterations, false);
    }

    /**
     * Creates a condition for "until" style containers (e.g. repeat, repeatOnError).
     * Returns true when the index has exceeded the iteration count, signaling the loop to stop.
     */
    public static IterationCountConditionExpression untilCount(int numberOfIterations) {
        return new IterationCountConditionExpression(numberOfIterations, true);
    }

    @Override
    public boolean evaluate(int index, TestContext context) {
        if (untilSemantics) {
            return index > numberOfIterations;
        }
        return index <= numberOfIterations;
    }

    public int getNumberOfIterations() {
        return numberOfIterations;
    }
}
