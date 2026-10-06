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
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.citrusframework.validation.assertj.AssertJ.conditionOf;

public class AssertJConditionExpressionTest extends UnitTestSupport {

    @Test
    public void shouldAssertIndex() {
        AssertJConditionExpression condition = conditionOf(i -> assertThat(i).isLessThan(3));

        assertThat(condition.evaluate(2, context)).isTrue();
        assertThat(condition.evaluate(3, context)).isFalse();
    }

    @Test
    public void shouldAssertIndexWithCondition() {
        AssertJConditionExpression condition = conditionOf(new Condition<Integer>(i -> i < 3, "less than 3"));

        assertThat(condition.evaluate(2, context)).isTrue();
        assertThat(condition.evaluate(3, context)).isFalse();
    }

    @Test
    public void shouldAssertExplicitValueWithVariables() {
        context.setVariable("status", "OK-1");
        AssertJConditionExpression condition = conditionOf("${status}", s -> assertThat(s).asString().startsWith("OK"));

        assertThat(condition.evaluate(context)).isTrue();
        assertThat(condition.isSatisfied(context)).isTrue();
        assertThat(condition.evaluate(99, context)).isTrue();

        context.setVariable("status", "FAILED");
        assertThat(condition.evaluate(context)).isFalse();
    }

    @Test
    public void shouldConvertExplicitValue() {
        context.setVariable("count", "3");
        AssertJConditionExpression condition = conditionOf(Integer.class, "${count}", c -> assertThat(c).isEqualTo(3));

        assertThat(condition.evaluate(context)).isTrue();

        context.setVariable("count", "4");
        assertThat(condition.evaluate(context)).isFalse();
    }

    @Test
    public void shouldAssertExplicitValueWithCondition() {
        AssertJConditionExpression condition = conditionOf("OK", new Condition<>("OK"::equals, "ok"));

        assertThat(condition.evaluate(context)).isTrue();
    }

    @Test
    public void shouldEvaluateFalseWhenValueCannotBeConverted() {
        AssertJConditionExpression condition = conditionOf(Integer.class, "many", c -> assertThat(c).isPositive());

        assertThat(condition.evaluate(context)).isFalse();
        assertThat(condition.getErrorMessage(context)).contains("java.lang.Integer");
    }

    @Test
    public void shouldDescribeOutcome() {
        AssertJConditionExpression condition = conditionOf(Integer.class, "3", c -> assertThat(c).isGreaterThan(5));

        assertThat(condition.getName()).isEqualTo("assertj-condition");
        assertThat(condition.isSatisfied(context)).isFalse();
        assertThat(condition.getErrorMessage(context))
                .startsWith("AssertJ condition not satisfied: satisfies(java.lang.Integer)")
                .contains("to be greater than");
        assertThat(condition.getSuccessMessage(context)).isEqualTo("AssertJ condition satisfied: satisfies(java.lang.Integer)");
    }
}
