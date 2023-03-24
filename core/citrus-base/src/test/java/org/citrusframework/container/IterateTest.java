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

import org.citrusframework.DefaultTestActionBuilder;
import org.citrusframework.TestAction;
import org.citrusframework.UnitTestSupport;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Christoph Deppisch
 */
public class IterateTest extends UnitTestSupport {

    private TestAction action = Mockito.mock(TestAction.class);

    @Test(dataProvider = "expressionProvider")
    public void testIteration(String expression) {
        reset(action);

        Iterate iterate = new Iterate.Builder()
                .condition(expression)
                .index("i")
                .actions(() -> action)
                .build();
        iterate.execute(context);

        Assert.assertNotNull(context.getVariable("${i}"));
        Assert.assertEquals(context.getVariable("${i}"), "5");

        verify(action, times(5)).execute(context);
    }

    @DataProvider
    public Object[][] expressionProvider() {
        return new Object[][] {
            new Object[] {"i lt= 5"},
            new Object[] {"@lowerThan(6)@"}
        };
    }

    @Test
    public void testStep() {
        reset(action);

        Iterate iterate = new Iterate.Builder()
                .condition("i lt= 10")
                .index("i")
                .step(2)
                .actions(() -> action)
                .build();
        iterate.execute(context);

        Assert.assertNotNull(context.getVariable("${i}"));
        Assert.assertEquals(context.getVariable("${i}"), "9");

        verify(action, times(5)).execute(context);
    }

    @Test
    public void testStart() {
        reset(action);

        Iterate iterate = new Iterate.Builder()
                .condition("i lt= 10")
                .index("i")
                .step(2)
                .startsWith(2)
                .actions(() -> action)
                .build();
        iterate.execute(context);

        Assert.assertNotNull(context.getVariable("${i}"));
        Assert.assertEquals(context.getVariable("${i}"), "10");

        verify(action, times(5)).execute(context);
    }

    @Test
    public void testNoIterationBasedOnCondition() {
        TestAction action = Mockito.mock(TestAction.class);

        reset(action);

        Iterate iterate = new Iterate.Builder()
                .condition("i lt 0")
                .index("i")
                .actions(action)
                .build();
        iterate.execute(context);

        Assert.assertNull(context.getVariables().get("i"));
    }

    @Test
    public void testIterationWithIndexManipulation() {
        TestAction incrementTestAction = DefaultTestActionBuilder.action(context -> {
            long end = Long.parseLong(context.getVariable("end"));
            context.setVariable("end", String.valueOf(end - 25));
        }).build();

        reset(action);

        context.setVariable("end", 100);

        Iterate iterate = new Iterate.Builder()
                .condition("i lt ${end}")
                .index("i")
                .actions(action, incrementTestAction)
                .build();
        iterate.execute(context);

        Assert.assertNotNull(context.getVariables().get("i"));
        Assert.assertEquals(context.getVariable("${i}"), "4");

        verify(action, times(4)).execute(context);
    }

    @Test
    public void testIterationConditionExpression() {
        reset(action);

        Iterate iterate = new Iterate.Builder()
                .condition((index, context) -> index <= 5)
                .index("i")
                .actions(() -> action)
                .build();
        iterate.execute(context);

        Assert.assertNotNull(context.getVariable("${i}"));
        Assert.assertEquals(context.getVariable("${i}"), "5");

        verify(action, times(5)).execute(context);
    }

}
