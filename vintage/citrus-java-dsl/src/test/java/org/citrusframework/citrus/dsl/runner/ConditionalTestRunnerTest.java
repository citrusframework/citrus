/*
 * Copyright 2006-2015 the original author or authors.
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

package org.citrusframework.citrus.dsl.runner;

import org.citrusframework.citrus.TestCase;
import org.citrusframework.citrus.container.Conditional;
import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.dsl.UnitTestSupport;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.citrus.container.HamcrestConditionExpression.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

public class ConditionalTestRunnerTest extends UnitTestSupport {
    @Test
    public void testConditionalBuilder() {
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                variable("var", 5);

                conditional().when("${var} = 5")
                        .actions(echo("${var}"), createVariable("execution", "true"));
            }
        };

        TestContext context = builder.getTestContext();
        Assert.assertNotNull(context.getVariable("execution"));
        Assert.assertEquals(context.getVariable("execution"), "true");

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), Conditional.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "conditional");

        Conditional container = (Conditional)test.getActions().get(0);
        Assert.assertEquals(container.getActionCount(), 2);
        Assert.assertEquals(container.getCondition(), "${var} = 5");
    }

    @Test
    public void testConditionalBuilderSkip() {
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                variable("var", 0);

                conditional().when("${var} = 5")
                        .actions(echo("${var}"), createVariable("execution", "true"));
            }
        };

        TestContext context = builder.getTestContext();
        Assert.assertNull(context.getVariables().get("execution"));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), Conditional.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "conditional");

        Conditional container = (Conditional)test.getActions().get(0);
        Assert.assertEquals(container.getActionCount(), 2);
        Assert.assertEquals(container.getCondition(), "${var} = 5");
    }

    @Test
    public void testConditionalBuilderConditionExpression() {
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                variable("var", 5);

                conditional().when(context -> context.getVariable("var").equals("5"))
                        .actions(echo("${var}"), createVariable("execution", "true"));
            }
        };

        TestContext context = builder.getTestContext();
        Assert.assertNotNull(context.getVariable("execution"));
        Assert.assertEquals(context.getVariable("execution"), "true");

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), Conditional.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "conditional");

        Conditional container = (Conditional)test.getActions().get(0);
        Assert.assertEquals(container.getActionCount(), 2);
        Assert.assertNotNull(container.getConditionExpression());
    }

    @Test
    public void testConditionalBuilderHamcrestConditionExpression() {
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                variable("var", 5);
                variable("noExecution", "true");

                conditional().when(assertThat("${var}", is("5")))
                        .actions(echo("${var}"), createVariable("execution", "true"));

                conditional().when(assertThat("${var}", lessThan("5")))
                        .actions(echo("${var}"), createVariable("noExecution", "false"));
            }
        };

        TestContext context = builder.getTestContext();
        Assert.assertNotNull(context.getVariable("noExecution"));
        Assert.assertEquals(context.getVariable("noExecution"), "true");
        Assert.assertNotNull(context.getVariable("execution"));
        Assert.assertEquals(context.getVariable("execution"), "true");

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 2);
        Assert.assertEquals(test.getActions().get(0).getClass(), Conditional.class);
        Assert.assertEquals(test.getActions().get(0).getName(), "conditional");
        Assert.assertEquals(test.getActions().get(1).getClass(), Conditional.class);
        Assert.assertEquals(test.getActions().get(1).getName(), "conditional");

        Conditional container = (Conditional)test.getActions().get(0);
        Assert.assertEquals(container.getActionCount(), 2);
        Assert.assertNotNull(container.getConditionExpression());
    }
}
