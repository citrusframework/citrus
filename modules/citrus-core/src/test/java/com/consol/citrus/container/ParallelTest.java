/*
 * Copyright 2006-2009 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 *  Citrus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Citrus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Citrus.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.container;

import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.testng.annotations.Test;

import com.consol.citrus.TestAction;
import com.consol.citrus.actions.*;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractBaseTest;


/**
 * @author Christoph Christoph Deppisch Consol* Software GmbH
 */
public class ParallelTest extends AbstractBaseTest {

    @Test
    public void testSingleAction() {
        Parallel parallelAction = new Parallel();
        
        TestAction action = EasyMock.createMock(TestAction.class);

        reset(action);
        
        action.execute(context);
        expectLastCall().once();
        
        replay(action);
        
        List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(action);
        
        parallelAction.setActions(actionList);
        
        parallelAction.execute(context);
    }
    
    @Test
    public void testParallelMultipleActions() {
        Parallel parallelAction = new Parallel();
        
        TestAction action = EasyMock.createMock(TestAction.class);

        reset(action);
        
        action.execute(context);
        expectLastCall().once();
        
        replay(action);
        
        List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(new EchoAction());
        actionList.add(action);
        actionList.add(new EchoAction());
        
        parallelAction.setActions(actionList);
        
        parallelAction.execute(context);
    }
    
    @Test
    public void testParallelActions() {
        Parallel parallelAction = new Parallel();
        
        List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(new EchoAction());
        actionList.add(new EchoAction());
        actionList.add(new EchoAction());
        
        SleepAction sleep = new SleepAction();
        sleep.setDelay("0.3");
        actionList.add(sleep);
        
        parallelAction.setActions(actionList);
        
        parallelAction.execute(context);
    }
    
    @Test(expectedExceptions=CitrusRuntimeException.class)
    public void testOneActionThatIsFailing() {
        Parallel parallelAction = new Parallel();
        
        List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(new FailAction());
        
        parallelAction.setActions(actionList);
        
        parallelAction.execute(context);
    }
    
    @Test(expectedExceptions=CitrusRuntimeException.class)
    public void testOnlyActionFailingActions() {
        Parallel parallelAction = new Parallel();
        
        List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(new FailAction());
        actionList.add(new FailAction());
        actionList.add(new FailAction());
        
        parallelAction.setActions(actionList);
        
        parallelAction.execute(context);
    }
    
    @Test(expectedExceptions=CitrusRuntimeException.class)
    public void testSingleFailingAction() {
        Parallel parallelAction = new Parallel();
        
        List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(new EchoAction());
        actionList.add(new FailAction());
        actionList.add(new EchoAction());
        
        parallelAction.setActions(actionList);
        
        parallelAction.execute(context);
    }
    
    @Test(expectedExceptions=CitrusRuntimeException.class)
    public void testSomeFailingActions() {
        Parallel parallelAction = new Parallel();
        
        TestAction action = EasyMock.createMock(TestAction.class);

        reset(action);
        
        action.execute(context);
        expectLastCall().once();
        
        replay(action);
        
        List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(new EchoAction());
        actionList.add(new FailAction());
        actionList.add(action);
        actionList.add(new FailAction());
        
        parallelAction.setActions(actionList);
        
        parallelAction.execute(context);
    }
}
