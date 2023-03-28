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

package org.citrusframework.container;

import org.citrusframework.TestAction;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.FailAction;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Christoph Deppisch
 */
public class RepeatOnErrorUntilTrueTest extends UnitTestSupport {

    private TestAction action = Mockito.mock(TestAction.class);

    @Test(dataProvider = "expressionProvider")
    public void testSuccessOnFirstIteration(String expression) {
        reset(action);

        RepeatOnErrorUntilTrue repeat = new RepeatOnErrorUntilTrue.Builder()
                .condition(expression)
                .index("i")
                .actions(() -> action)
                .build();
        repeat.execute(context);
        verify(action).execute(context);
    }

    @DataProvider
    public Object[][] expressionProvider() {
        return new Object[][] {
                new Object[] {"i = 5"},
                new Object[] {"@greaterThan(4)@"}
        };
    }

    @Test(expectedExceptions=CitrusRuntimeException.class)
    public void testRepeatOnErrorNoSuccess() {
        reset(action);

        RepeatOnErrorUntilTrue repeat = new RepeatOnErrorUntilTrue.Builder()
                .condition("i = 5")
                .index("i")
                .autoSleep(0L)
                .actions(() -> action, new FailAction.Builder())
                .build();
        repeat.execute(context);
        verify(action, times(4)).execute(context);
    }

    @Test(expectedExceptions=CitrusRuntimeException.class)
    public void testRepeatOnErrorNoSuccessConditionExpression() {
        reset(action);

        RepeatOnErrorUntilTrue repeat = new RepeatOnErrorUntilTrue.Builder()
                .condition((index, context) -> index == 5)
                .index("i")
                .autoSleep(0L)
                .actions(() -> action, new FailAction.Builder())
                .build();
        repeat.execute(context);
        verify(action, times(4)).execute(context);
    }

}
