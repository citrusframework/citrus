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

package org.citrusframework.container;

import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.TestCase;
import org.citrusframework.UnitTestSupport;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.actions.CreateVariablesAction.Builder.createVariable;
import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.container.Conditional.Builder.conditional;
import static org.citrusframework.container.HamcrestConditionExpression.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

public class ConditionalTestActionBuilderTest extends UnitTestSupport {
    @Test
    public void testConditionalBuilderHamcrestConditionExpression() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.variable("var", 5);
        builder.variable("noExecution", "true");

        builder.$(conditional().when(assertThat("${var}", is("5")))
                .actions(echo("${var}"), createVariable("execution", "true")));

        builder.$(conditional().when(assertThat("${var}", lessThan("5")))
                .actions(echo("${var}"), createVariable("noExecution", "false")));

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
