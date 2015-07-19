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
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import java.util.*;

import static org.easymock.EasyMock.*;

/**
 * @author Matthias Beil
 * @since 1.2
 */
public class ConditionalTest extends AbstractTestNGUnitTest {

    private TestAction action = EasyMock.createMock(TestAction.class);

    @Test(expectedExceptions = IllegalStateException.class)
    public void testConditionFalse() {

        final Conditional conditionalAction = new Conditional();
        conditionalAction.setCondition("1 = 0");

        reset(action);

        action.execute(this.context);
        expectLastCall().once();

        replay(action);

        conditionalAction.setActions(Collections.singletonList(action));

        conditionalAction.execute(this.context);

        // must throw IllegalStateException, as the action should never be called
        expectLastCall().once();

        verify(action);
    }

    @Test
    public void testSingleAction() {

        final Conditional conditionalAction = new Conditional();
        conditionalAction.setCondition("1 = 1");

        final TestAction action = EasyMock.createMock(TestAction.class);

        reset(action);

        action.execute(this.context);
        expectLastCall().once();

        replay(action);

        final List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(action);

        conditionalAction.setActions(actionList);

        conditionalAction.execute(this.context);
        
        verify(action);
    }

    @Test
    public void testMultipleActions() {

        final Conditional conditionalAction = new Conditional();
        conditionalAction.setCondition("1 = 1");

        final TestAction action1 = EasyMock.createMock(TestAction.class);
        final TestAction action2 = EasyMock.createMock(TestAction.class);
        final TestAction action3 = EasyMock.createMock(TestAction.class);

        reset(action1, action2, action3);

        action1.execute(this.context);
        expectLastCall().once();
        action2.execute(this.context);
        expectLastCall().once();
        action3.execute(this.context);
        expectLastCall().once();

        replay(action1, action2, action3);

        final List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(action1);
        actionList.add(action2);
        actionList.add(action3);

        conditionalAction.setActions(actionList);

        conditionalAction.execute(this.context);
        
        verify(action1, action2, action3);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testFirstActionFailing() {

        final Conditional conditionalAction = new Conditional();
        conditionalAction.setCondition("1 = 1");

        final TestAction action1 = EasyMock.createMock(TestAction.class);
        final TestAction action2 = EasyMock.createMock(TestAction.class);
        final TestAction action3 = EasyMock.createMock(TestAction.class);

        reset(action1, action2, action3);

        replay(action1, action2, action3);

        final List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(new FailAction());
        actionList.add(action1);
        actionList.add(action2);
        actionList.add(action3);

        conditionalAction.setActions(actionList);

        conditionalAction.execute(this.context);
        
        verify(action1, action2, action3);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testLastActionFailing() {

        final Conditional conditionalAction = new Conditional();
        conditionalAction.setCondition("1 = 1");

        final TestAction action1 = EasyMock.createMock(TestAction.class);
        final TestAction action2 = EasyMock.createMock(TestAction.class);
        final TestAction action3 = EasyMock.createMock(TestAction.class);

        reset(action1, action2, action3);

        action1.execute(this.context);
        expectLastCall().once();
        action2.execute(this.context);
        expectLastCall().once();
        action3.execute(this.context);
        expectLastCall().once();

        replay(action1, action2, action3);

        final List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(action1);
        actionList.add(action2);
        actionList.add(action3);
        actionList.add(new FailAction());

        conditionalAction.setActions(actionList);

        conditionalAction.execute(this.context);
        
        verify(action1, action2, action3);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testFailingAction() {

        final Conditional conditionalAction = new Conditional();
        conditionalAction.setCondition("1 = 1");

        final TestAction action1 = EasyMock.createMock(TestAction.class);
        final TestAction action2 = EasyMock.createMock(TestAction.class);
        final TestAction action3 = EasyMock.createMock(TestAction.class);

        reset(action1, action2, action3);

        action1.execute(this.context);
        expectLastCall().once();

        replay(action1, action2, action3);

        final List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(action1);
        actionList.add(new FailAction());
        actionList.add(action2);
        actionList.add(action3);

        conditionalAction.setActions(actionList);

        conditionalAction.execute(this.context);
        
        verify(action1, action2, action3);
    }

}
