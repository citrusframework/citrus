/*
 * Copyright 2006-2011 the original author or authors.
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
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.*;

import static org.mockito.Mockito.*;

/**
 * @author Matthias Beil
 * @since 1.2
 */
public class ConditionalTest extends AbstractTestNGUnitTest {

    private TestAction action = Mockito.mock(TestAction.class);

    @Test
    public void testConditionFalse() {
        final Conditional conditionalAction = new Conditional();
        conditionalAction.setCondition("1 = 0");

        reset(action);

        conditionalAction.setActions(Collections.singletonList(action));

        conditionalAction.execute(this.context);
        verify(action, never()).execute(this.context);
    }

    @Test
    public void testConditionMatcherFalse() {
        final Conditional conditionalAction = new Conditional();
        conditionalAction.setCondition("@assertThat('5', 'is(4)')@");

        reset(action);

        conditionalAction.setActions(Collections.singletonList(action));

        conditionalAction.execute(this.context);
        verify(action, never()).execute(this.context);
    }

    @Test
    public void testSingleAction() {
        final Conditional conditionalAction = new Conditional();
        conditionalAction.setCondition("1 = 1");

        final TestAction action = Mockito.mock(TestAction.class);

        reset(action);

        final List<TestAction> actionList = new ArrayList<>();
        actionList.add(action);

        conditionalAction.setActions(actionList);

        conditionalAction.execute(this.context);

        verify(action).execute(this.context);
    }

    @Test
    public void testMatcherSingleAction() {
        final Conditional conditionalAction = new Conditional();
        conditionalAction.setCondition("@assertThat('5', 'is(5)')@");

        final TestAction action = Mockito.mock(TestAction.class);

        reset(action);

        final List<TestAction> actionList = new ArrayList<>();
        actionList.add(action);

        conditionalAction.setActions(actionList);

        conditionalAction.execute(this.context);

        verify(action).execute(this.context);
    }

    @Test
    public void testMultipleActions() {
        final Conditional conditionalAction = new Conditional();
        conditionalAction.setCondition("1 = 1");

        final TestAction action1 = Mockito.mock(TestAction.class);
        final TestAction action2 = Mockito.mock(TestAction.class);
        final TestAction action3 = Mockito.mock(TestAction.class);

        reset(action1, action2, action3);

        final List<TestAction> actionList = new ArrayList<>();
        actionList.add(action1);
        actionList.add(action2);
        actionList.add(action3);

        conditionalAction.setActions(actionList);

        conditionalAction.execute(this.context);

        verify(action1).execute(this.context);
        verify(action2).execute(this.context);
        verify(action3).execute(this.context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testFirstActionFailing() {
        final Conditional conditionalAction = new Conditional();
        conditionalAction.setCondition("1 = 1");

        final TestAction action1 = Mockito.mock(TestAction.class);
        final TestAction action2 = Mockito.mock(TestAction.class);
        final TestAction action3 = Mockito.mock(TestAction.class);

        reset(action1, action2, action3);

        final List<TestAction> actionList = new ArrayList<>();
        actionList.add(new FailAction());
        actionList.add(action1);
        actionList.add(action2);
        actionList.add(action3);

        conditionalAction.setActions(actionList);

        conditionalAction.execute(this.context);

    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testLastActionFailing() {
        final Conditional conditionalAction = new Conditional();
        conditionalAction.setCondition("1 = 1");

        final TestAction action1 = Mockito.mock(TestAction.class);
        final TestAction action2 = Mockito.mock(TestAction.class);
        final TestAction action3 = Mockito.mock(TestAction.class);

        reset(action1, action2, action3);

        final List<TestAction> actionList = new ArrayList<>();
        actionList.add(action1);
        actionList.add(action2);
        actionList.add(action3);
        actionList.add(new FailAction());

        conditionalAction.setActions(actionList);

        conditionalAction.execute(this.context);

        verify(action1).execute(this.context);
        verify(action2).execute(this.context);
        verify(action3).execute(this.context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testFailingAction() {
        final Conditional conditionalAction = new Conditional();
        conditionalAction.setCondition("1 = 1");

        final TestAction action1 = Mockito.mock(TestAction.class);
        final TestAction action2 = Mockito.mock(TestAction.class);
        final TestAction action3 = Mockito.mock(TestAction.class);

        reset(action1, action2, action3);

        final List<TestAction> actionList = new ArrayList<>();
        actionList.add(action1);
        actionList.add(new FailAction());
        actionList.add(action2);
        actionList.add(action3);

        conditionalAction.setActions(actionList);

        conditionalAction.execute(this.context);

        verify(action1).execute(this.context);
    }

}
