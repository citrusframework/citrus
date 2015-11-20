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
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.*;

import static org.mockito.Mockito.*;


/**
 * @author Christoph Deppisch
 */
public class SequenceTest extends AbstractTestNGUnitTest {

    private TestAction action = Mockito.mock(TestAction.class);

    @Test
    public void testSingleAction() {
        Sequence sequenceAction = new Sequence();

        reset(action);

        sequenceAction.setActions(Collections.singletonList(action));

        sequenceAction.execute(context);

        verify(action).execute(context);
    }
    
    @Test
    public void testMultipleActions() {
        Sequence sequenceAction = new Sequence();
        
        TestAction action1 = Mockito.mock(TestAction.class);
        TestAction action2 = Mockito.mock(TestAction.class);
        TestAction action3 = Mockito.mock(TestAction.class);

        reset(action1, action2, action3);

        List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(action1);
        actionList.add(action2);
        actionList.add(action3);

        sequenceAction.setActions(actionList);

        sequenceAction.execute(context);
        verify(action1).execute(context);
        verify(action2).execute(context);
        verify(action3).execute(context);
    }
    
    @Test(expectedExceptions=CitrusRuntimeException.class)
    public void testFirstActionFailing() {
        Sequence sequenceAction = new Sequence();
        
        TestAction action1 = Mockito.mock(TestAction.class);
        TestAction action2 = Mockito.mock(TestAction.class);
        TestAction action3 = Mockito.mock(TestAction.class);

        reset(action1, action2, action3);

        
        List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(new FailAction());
        actionList.add(action1);
        actionList.add(action2);
        actionList.add(action3);
        
        sequenceAction.setActions(actionList);
        
        sequenceAction.execute(context);
    }
    
    @Test(expectedExceptions=CitrusRuntimeException.class)
    public void testLastActionFailing() {
        Sequence sequenceAction = new Sequence();
        
        TestAction action1 = Mockito.mock(TestAction.class);
        TestAction action2 = Mockito.mock(TestAction.class);
        TestAction action3 = Mockito.mock(TestAction.class);

        reset(action1, action2, action3);

        List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(action1);
        actionList.add(action2);
        actionList.add(action3);
        actionList.add(new FailAction());

        sequenceAction.setActions(actionList);

        sequenceAction.execute(context);
        verify(action1).execute(context);
        verify(action2).execute(context);
        verify(action3).execute(context);
    }
    
    @Test(expectedExceptions=CitrusRuntimeException.class)
    public void testFailingAction() {
        Sequence sequenceAction = new Sequence();
        
        TestAction action1 = Mockito.mock(TestAction.class);
        TestAction action2 = Mockito.mock(TestAction.class);
        TestAction action3 = Mockito.mock(TestAction.class);

        reset(action1, action2, action3);

        List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(action1);
        actionList.add(new FailAction());
        actionList.add(action2);
        actionList.add(action3);

        sequenceAction.setActions(actionList);

        sequenceAction.execute(context);
        verify(action1).execute(context);
    }
}
