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

package org.citrusframework.actions.dsl;

import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.TestCase;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.container.RepeatUntilTrue;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.actions.SleepAction.Builder.sleep;
import static org.citrusframework.container.RepeatUntilTrue.Builder.repeat;
import static org.testng.Assert.assertEquals;

public class RepeatTestRunnerTest extends UnitTestSupport {
    @Test
    public void testRepeatBuilder() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.variable("var", "foo");

        builder.$(repeat().index("i")
                    .startsWith(2)
                    .until("i lt 5")
                .actions(echo("${var}"), sleep().milliseconds(100), echo("${var}")));

        Assert.assertNotNull(context.getVariable("i"));
        Assert.assertEquals(context.getVariable("i"), "2");

        TestCase test = builder.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), RepeatUntilTrue.class);
        assertEquals(test.getActions().get(0).getName(), "repeat");

        RepeatUntilTrue container = (RepeatUntilTrue)test.getActions().get(0);
        assertEquals(container.getActionCount(), 3);
        assertEquals(container.getCondition(), "i lt 5");
        assertEquals(container.getStart(), 2);
        assertEquals(container.getIndexName(), "i");
        assertEquals(container.getTestAction(0).getClass(), EchoAction.class);
    }

    @Test
    public void testRepeatBuilderWithConditionExpression() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.variable("var", "foo");

        builder.$(repeat().index("i")
                    .startsWith(2)
                    .until((index, context) -> index > 5)
                .actions(echo("${var}"), sleep().milliseconds(100), echo("${var}")));

        Assert.assertNotNull(context.getVariable("i"));
        Assert.assertEquals(context.getVariable("i"), "5");

        TestCase test = builder.getTestCase();
        assertEquals(test.getActionCount(), 1);
        assertEquals(test.getActions().get(0).getClass(), RepeatUntilTrue.class);
        assertEquals(test.getActions().get(0).getName(), "repeat");

        RepeatUntilTrue container = (RepeatUntilTrue)test.getActions().get(0);
        assertEquals(container.getActionCount(), 3);
        assertEquals(container.getStart(), 2);
        assertEquals(container.getIndexName(), "i");
        assertEquals(container.getTestAction(0).getClass(), EchoAction.class);
    }
}
