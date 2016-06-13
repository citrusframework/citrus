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
import com.consol.citrus.context.TestContext;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;


/**
 * @author Christoph Deppisch
 */
public class RepeatUntilTrueTest extends AbstractTestNGUnitTest {

    private TestAction action = Mockito.mock(TestAction.class);

    @Test(dataProvider = "expressionProvider")
    public void testRepeat(String expression) {
        RepeatUntilTrue repeatUntilTrue = new RepeatUntilTrue();

        reset(action);

        repeatUntilTrue.setActions(Collections.singletonList(action));

        repeatUntilTrue.setCondition(expression);
        repeatUntilTrue.setIndexName("i");

        repeatUntilTrue.execute(context);

        Assert.assertNotNull(context.getVariable("${i}"));
        Assert.assertEquals(context.getVariable("${i}"), "4");

        verify(action, times(4)).execute(context);
    }

    @DataProvider
    public Object[][] expressionProvider() {
        return new Object[][] {
                new Object[] {"i = 5"},
                new Object[] {"@assertThat(is(5))@"},
                new Object[] {"@assertThat('${i}', 'is(5)')@"}
        };
    }
    
    @Test
    public void testRepeatMinimumOnce() {
        RepeatUntilTrue repeatUntilTrue = new RepeatUntilTrue();
        
        reset(action);

        repeatUntilTrue.setActions(Collections.singletonList(action));

        repeatUntilTrue.setCondition("i gt 0");
        repeatUntilTrue.setIndexName("i");

        repeatUntilTrue.execute(context);

        Assert.assertNotNull(context.getVariable("${i}"));
        Assert.assertEquals(context.getVariable("${i}"), "1");

        verify(action).execute(context);
    }

    @Test
    public void testRepeatConditionExpression() {
        RepeatUntilTrue repeatUntilTrue = new RepeatUntilTrue();

        reset(action);

        repeatUntilTrue.setActions(Collections.singletonList(action));

        repeatUntilTrue.setConditionExpression(new IteratingConditionExpression() {
            @Override
            public boolean evaluate(int index, TestContext context) {
                return index == 5;
            }
        });

        repeatUntilTrue.execute(context);

        Assert.assertNotNull(context.getVariable("${i}"));
        Assert.assertEquals(context.getVariable("${i}"), "4");

        verify(action, times(4)).execute(context);
    }

    @Test
    public void testRepeatHamcrestConditionExpression() {
        RepeatUntilTrue repeatUntilTrue = new RepeatUntilTrue();

        reset(action);

        repeatUntilTrue.setActions(Collections.singletonList(action));

        repeatUntilTrue.setConditionExpression(new HamcrestConditionExpression(is(5)));

        repeatUntilTrue.execute(context);

        Assert.assertNotNull(context.getVariable("${i}"));
        Assert.assertEquals(context.getVariable("${i}"), "4");

        verify(action, times(4)).execute(context);
    }
}
