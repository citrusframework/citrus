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

import com.consol.citrus.TestAction;
import com.consol.citrus.actions.FailAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

/**
 * @author Christoph Deppisch
 */
public class RepeatOnErrorUntilTrueTest extends AbstractTestNGUnitTest {

    private TestAction action = Mockito.mock(TestAction.class);

    @Test(dataProvider = "expressionProvider")
    public void testSuccessOnFirstIteration(String expression) {
        RepeatOnErrorUntilTrue repeat = new RepeatOnErrorUntilTrue();

        reset(action);

        repeat.setActions(Collections.singletonList(action));

        repeat.setIndexName("i");
        repeat.setCondition(expression);

        repeat.execute(context);
        verify(action).execute(context);
    }

    @DataProvider
    public Object[][] expressionProvider() {
        return new Object[][] {
                new Object[] {"i = 5"},
                new Object[] {"@assertThat(is(5))@"},
                new Object[] {"@assertThat('${i}', 'is(5)')@"}
        };
    }
    
    @Test(expectedExceptions=CitrusRuntimeException.class)
    public void testRepeatOnErrorNoSuccess() {
        RepeatOnErrorUntilTrue repeat = new RepeatOnErrorUntilTrue();
        
        List<TestAction> actions = new ArrayList<>();

        reset(action);

        actions.add(action);
        actions.add(new FailAction());

        repeat.setActions(actions);

        repeat.setIndexName("i");
        repeat.setCondition("i = 5");
        repeat.setAutoSleep(0L);

        repeat.execute(context);
        verify(action, times(4)).execute(context);
    }

    @Test(expectedExceptions=CitrusRuntimeException.class)
    public void testRepeatOnErrorNoSuccessConditionExpression() {
        RepeatOnErrorUntilTrue repeat = new RepeatOnErrorUntilTrue();

        List<TestAction> actions = new ArrayList<>();

        reset(action);

        actions.add(action);
        actions.add(new FailAction());

        repeat.setActions(actions);

        repeat.setConditionExpression(new IteratingConditionExpression() {
            @Override
            public boolean evaluate(int index, TestContext context) {
                return index == 5;
            }
        });
        repeat.setAutoSleep(0L);

        repeat.execute(context);
        verify(action, times(4)).execute(context);
    }

    @Test(expectedExceptions=CitrusRuntimeException.class)
    public void testRepeatOnErrorNoSuccessHamcrestConditionExpression() {
        RepeatOnErrorUntilTrue repeat = new RepeatOnErrorUntilTrue();

        List<TestAction> actions = new ArrayList<>();

        reset(action);

        actions.add(action);
        actions.add(new FailAction());

        repeat.setActions(actions);

        repeat.setConditionExpression(new HamcrestConditionExpression(is(5)));
        repeat.setAutoSleep(0L);

        repeat.execute(context);
        verify(action, times(4)).execute(context);
    }
}
