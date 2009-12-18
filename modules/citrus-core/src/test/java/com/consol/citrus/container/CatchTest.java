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

import java.util.*;

import static org.easymock.EasyMock.*;

import org.easymock.EasyMock;
import org.testng.annotations.Test;

import com.consol.citrus.TestAction;
import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.actions.FailAction;
import com.consol.citrus.testng.AbstractBaseTest;


/**
 * @author Christoph Christoph Deppisch Consol* Software GmbH
 */
public class CatchTest extends AbstractBaseTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testCatchDefaultException() {
        Catch catchAction = new Catch();
        
        List actionList = Collections.singletonList(new FailAction());
        catchAction.setActions(actionList);
        
        catchAction.execute(context);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testCatchException() {
        Catch catchAction = new Catch();
        
        List actionList = Collections.singletonList(new FailAction());
        catchAction.setActions(actionList);
        
        catchAction.setException("com.consol.citrus.exceptions.CitrusRuntimeException");
        
        catchAction.execute(context);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testNothingToCatch() {
        Catch catchAction = new Catch();
        
        List actionList = Collections.singletonList(new EchoAction());
        catchAction.setActions(actionList);
        
        catchAction.setException("com.consol.citrus.exceptions.CitrusRuntimeException");
        
        catchAction.execute(context);
    }
    
    @Test
    public void testCatchFirstActionFailing() {
        Catch catchAction = new Catch();
        
        TestAction action = EasyMock.createMock(TestAction.class);

        reset(action);
        
        action.execute(context);
        expectLastCall().once();
        
        replay(action);
        
        List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(new FailAction());
        actionList.add(action);
        
        catchAction.setActions(actionList);
        
        catchAction.setException("com.consol.citrus.exceptions.CitrusRuntimeException");
        
        catchAction.execute(context);
    }
    
    @Test
    public void testCatchSomeActionFailing() {
        Catch catchAction = new Catch();
        
        TestAction action = EasyMock.createMock(TestAction.class);
        
        reset(action);
        
        action.execute(context);
        expectLastCall().times(2);
        
        replay(action);
        
        List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(action);
        actionList.add(new FailAction());
        actionList.add(action);
        
        catchAction.setActions(actionList);
        
        catchAction.setException("com.consol.citrus.exceptions.CitrusRuntimeException");
        
        catchAction.execute(context);
    }
}
