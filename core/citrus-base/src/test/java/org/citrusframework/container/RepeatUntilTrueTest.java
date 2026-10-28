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

import org.citrusframework.TestAction;
import org.citrusframework.base.UnitTestSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RepeatUntilTrueTest extends UnitTestSupport {

    private TestAction action = Mockito.mock(TestAction.class);

    @ParameterizedTest
    @MethodSource("expressionProvider")
    public void testRepeat(String expression) {
        reset(action);

        RepeatUntilTrue repeatUntilTrue = new RepeatUntilTrue.Builder()
                .condition(expression)
                .index("i")
                .actions(() -> action)
                .build();
        repeatUntilTrue.execute(context);

        Assertions.assertNotNull(context.getVariable("${i}"));
        Assertions.assertEquals("4", context.getVariable("${i}"));

        verify(action, times(4)).execute(context);
    }

    public Object[][] expressionProvider() {
        return new Object[][] {
                new Object[] {"i = 5"},
                new Object[] {"@greaterThan(4)@"}
        };
    }

    @Test
    public void testRepeatMinimumOnce() {
        reset(action);

        RepeatUntilTrue repeatUntilTrue = new RepeatUntilTrue.Builder()
                .condition("i gt 0")
                .index("i")
                .actions(() -> action)
                .build();
        repeatUntilTrue.execute(context);

        Assertions.assertNotNull(context.getVariable("${i}"));
        Assertions.assertEquals("1", context.getVariable("${i}"));

        verify(action).execute(context);
    }

    @Test
    public void testRepeatConditionExpression() {
        reset(action);

        RepeatUntilTrue repeatUntilTrue = new RepeatUntilTrue.Builder()
                .condition((index, context) -> index == 5)
                .index("i")
                .actions(() -> action)
                .build();
        repeatUntilTrue.execute(context);

        Assertions.assertNotNull(context.getVariable("${i}"));
        Assertions.assertEquals("4", context.getVariable("${i}"));

        verify(action, times(4)).execute(context);
    }

    @Test
    public void testRepeatTimes() {
        reset(action);

        RepeatUntilTrue repeatUntilTrue = new RepeatUntilTrue.Builder()
                .times(5)
                .index("i")
                .actions(() -> action)
                .build();
        repeatUntilTrue.execute(context);

        Assertions.assertNotNull(context.getVariable("${i}"));
        Assertions.assertEquals("5", context.getVariable("${i}"));

        verify(action, times(5)).execute(context);
    }
}
