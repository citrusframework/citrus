/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.container;

import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.testng.annotations.Test;

import com.consol.citrus.TestAction;
import com.consol.citrus.actions.FailAction;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractBaseTest;

/**
 * @author Christoph Deppisch
 */
public class SequenceTest extends AbstractBaseTest {

    @Test
    public void testSingleAction() {
        Sequence sequenceAction = new Sequence();
        
        TestAction action = EasyMock.createMock(TestAction.class);

        reset(action);
        
        action.execute(context);
        expectLastCall().once();
        
        replay(action);
        
        List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(action);
        
        sequenceAction.setActions(actionList);
        
        sequenceAction.execute(context);
    }
    
    @Test
    public void testMultipleActions() {
        Sequence sequenceAction = new Sequence();
        
        TestAction action1 = EasyMock.createMock(TestAction.class);
        TestAction action2 = EasyMock.createMock(TestAction.class);
        TestAction action3 = EasyMock.createMock(TestAction.class);

        reset(action1, action2, action3);
        
        action1.execute(context);
        expectLastCall().once();
        action2.execute(context);
        expectLastCall().once();
        action3.execute(context);
        expectLastCall().once();
        
        replay(action1, action2, action3);
        
        List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(action1);
        actionList.add(action2);
        actionList.add(action3);
        
        sequenceAction.setActions(actionList);
        
        sequenceAction.execute(context);
    }
    
    @Test(expectedExceptions=CitrusRuntimeException.class)
    public void testFirstActionFailing() {
        Sequence sequenceAction = new Sequence();
        
        TestAction action1 = EasyMock.createMock(TestAction.class);
        TestAction action2 = EasyMock.createMock(TestAction.class);
        TestAction action3 = EasyMock.createMock(TestAction.class);

        reset(action1, action2, action3);
        
        replay(action1, action2, action3);
        
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
        
        TestAction action1 = EasyMock.createMock(TestAction.class);
        TestAction action2 = EasyMock.createMock(TestAction.class);
        TestAction action3 = EasyMock.createMock(TestAction.class);

        reset(action1, action2, action3);
        
        action1.execute(context);
        expectLastCall().once();
        action2.execute(context);
        expectLastCall().once();
        action3.execute(context);
        expectLastCall().once();
        
        replay(action1, action2, action3);
        
        List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(action1);
        actionList.add(action2);
        actionList.add(action3);
        actionList.add(new FailAction());
        
        sequenceAction.setActions(actionList);
        
        sequenceAction.execute(context);
    }
    
    @Test(expectedExceptions=CitrusRuntimeException.class)
    public void testFailingAction() {
        Sequence sequenceAction = new Sequence();
        
        TestAction action1 = EasyMock.createMock(TestAction.class);
        TestAction action2 = EasyMock.createMock(TestAction.class);
        TestAction action3 = EasyMock.createMock(TestAction.class);

        reset(action1, action2, action3);
        
        action1.execute(context);
        expectLastCall().once();
        
        replay(action1, action2, action3);
        
        List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(action1);
        actionList.add(new FailAction());
        actionList.add(action2);
        actionList.add(action3);
        
        sequenceAction.setActions(actionList);
        
        sequenceAction.execute(context);
    }
}
