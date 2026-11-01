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

package org.citrusframework.validation.hamcrest;

import org.citrusframework.base.DefaultTestCaseRunner;
import org.citrusframework.dsl.TestActionSupport;
import org.citrusframework.TestCase;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.container.RepeatOnErrorUntilTrue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.citrusframework.validation.hamcrest.HamcrestConditionExpression.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RepeatOnErrorTestActionBuilderTest extends UnitTestSupport implements TestActionSupport {
    @SuppressWarnings({"deprecation", "removal"})
    @Test
    public void testRepeatOnErrorBuilderWithHamcrestConditionExpression() {
        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.variable("var", "foo");

        builder.$(repeatOnError().autoSleep(250)
                        .until("i gt 5")
                .actions(echo("${var}"), sleep().milliseconds(50), echo("${var}")));

        builder.$(repeatOnError().autoSleep(200)
                        .index("k")
                        .startsWith(2)
                        .until(assertThat(is(5)))
                .actions(echo("${var}")));

        Assertions.assertNotNull(context.getVariable("i"));
        Assertions.assertEquals("1", context.getVariable("i"));
        Assertions.assertNotNull(context.getVariable("k"));
        Assertions.assertEquals("2", context.getVariable("k"));

        TestCase test = builder.getTestCase();
        assertEquals(2, test.getActionCount());
        assertEquals(RepeatOnErrorUntilTrue.class, test.getActions().get(0).getClass());
        assertEquals("repeat-on-error", test.getActions().get(0).getName());

        RepeatOnErrorUntilTrue container = (RepeatOnErrorUntilTrue)test.getActions().get(0);
        assertEquals(3, container.getActionCount());
        assertEquals(Long.valueOf(250L), container.getAutoSleep());
        assertEquals("i gt 5", container.getCondition());
        assertEquals(1, container.getStart());
        assertEquals("i", container.getIndexName());
        assertEquals(EchoAction.class, container.getTestAction(0).getClass());

        container = (RepeatOnErrorUntilTrue)test.getActions().get(1);
        assertEquals(1, container.getActionCount());
        assertEquals(Long.valueOf(200L), container.getAutoSleep());
        assertEquals(2, container.getStart());
        assertEquals("k", container.getIndexName());
        assertEquals(EchoAction.class, container.getTestAction(0).getClass());
    }
}
