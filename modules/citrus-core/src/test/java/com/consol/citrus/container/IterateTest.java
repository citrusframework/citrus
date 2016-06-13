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
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;

import static org.mockito.Mockito.*;

import static org.hamcrest.Matchers.*;

/**
 * @author Christoph Deppisch
 */
public class IterateTest extends AbstractTestNGUnitTest {

    private TestAction action = Mockito.mock(TestAction.class);

    @Test(dataProvider = "expressionProvider")
    public void testIteration(String expression) {
        Iterate iterate = new Iterate();

        reset(action);

        iterate.setActions(Collections.singletonList(action));

        iterate.setCondition(expression);
        iterate.setIndexName("i");

        iterate.execute(context);

        Assert.assertNotNull(context.getVariable("${i}"));
        Assert.assertEquals(context.getVariable("${i}"), "5");

        verify(action, times(5)).execute(context);
    }

    @DataProvider
    public Object[][] expressionProvider() {
        return new Object[][] {
            new Object[] {"i lt= 5"},
            new Object[] {"@assertThat(lessThanOrEqualTo(5))@"},
            new Object[] {"@assertThat('${i}', 'lessThanOrEqualTo(5)')@"}
        };
    }

    @Test
    public void testStep() {
        Iterate iterate = new Iterate();
        
        reset(action);

        iterate.setActions(Collections.singletonList(action));

        iterate.setCondition("i lt= 10");
        iterate.setIndexName("i");
        iterate.setStep(2);

        iterate.execute(context);

        Assert.assertNotNull(context.getVariable("${i}"));
        Assert.assertEquals(context.getVariable("${i}"), "9");

        verify(action, times(5)).execute(context);
    }
    
    @Test
    public void testStart() {
        Iterate iterate = new Iterate();
        
        reset(action);

        iterate.setActions(Collections.singletonList(action));

        iterate.setCondition("i lt= 10");
        iterate.setIndexName("i");
        iterate.setStep(2);
        iterate.setStart(2);

        iterate.execute(context);

        Assert.assertNotNull(context.getVariable("${i}"));
        Assert.assertEquals(context.getVariable("${i}"), "10");

        verify(action, times(5)).execute(context);
    }
    
    @Test
    public void testNoIterationBasedOnCondition() {
        Iterate iterate = new Iterate();
        
        List<TestAction> actions = new ArrayList<TestAction>();
        TestAction action = Mockito.mock(TestAction.class);

        reset(action);
        actions.add(action);
        iterate.setActions(actions);
        
        iterate.setCondition("i lt 0");
        iterate.setIndexName("i");
        
        iterate.execute(context);
        
        Assert.assertNull(context.getVariables().get("i"));
    }

    @Test
    public void testIterationWithIndexManipulation() {
        Iterate iterate = new Iterate();

        List<TestAction> actions = new ArrayList<TestAction>();
        TestAction incrementTestAction = new AbstractTestAction() {
            @Override
            public void doExecute(TestContext context) {
                Long end = Long.valueOf(context.getVariable("end"));
                context.setVariable("end", String.valueOf(end - 25));
            }
        };

        reset(action);

        actions.add(action);
        actions.add(incrementTestAction);
        iterate.setActions(actions);

        iterate.setCondition("i lt ${end}");
        iterate.setIndexName("i");

        context.setVariable("end", 100);
        iterate.execute(context);

        Assert.assertNotNull(context.getVariables().get("i"));
        Assert.assertEquals(context.getVariable("${i}"), "4");

        verify(action, times(4)).execute(context);
    }

    @Test
    public void testIterationConditionExpression() {
        Iterate iterate = new Iterate();

        reset(action);

        iterate.setActions(Collections.singletonList(action));

        iterate.setConditionExpression(new IteratingConditionExpression() {
            @Override
            public boolean evaluate(int index, TestContext context) {
                return index <= 5;
            }
        });

        iterate.execute(context);

        Assert.assertNotNull(context.getVariable("${i}"));
        Assert.assertEquals(context.getVariable("${i}"), "5");

        verify(action, times(5)).execute(context);
    }

    @Test
    public void testHamcrestIterationConditionExpression() {
        Iterate iterate = new Iterate();

        reset(action);

        iterate.setActions(Collections.singletonList(action));

        iterate.setConditionExpression(new HamcrestConditionExpression(lessThanOrEqualTo(5)));

        iterate.execute(context);

        Assert.assertNotNull(context.getVariable("${i}"));
        Assert.assertEquals(context.getVariable("${i}"), "5");

        verify(action, times(5)).execute(context);
    }
}
