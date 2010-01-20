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

package com.consol.citrus.group;

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
 * @author Christoph Christoph Deppisch Consol* Software GmbH
 */
public class RepeatOnErrorUntilTrueTest extends AbstractBaseTest {
    @Test
    public void testSuccessOnFirstIteration() {
        RepeatOnErrorUntilTrue repeat = new RepeatOnErrorUntilTrue();
        
        List<TestAction> actions = new ArrayList<TestAction>();
        TestAction action = EasyMock.createMock(TestAction.class);

        reset(action);
        
        action.execute(context);
        expectLastCall().once();
        
        replay(action);
        
        actions.add(action);
        
        repeat.setActions(actions);
        
        repeat.setIndexName("i");
        repeat.setCondition("i = 5");
        
        repeat.execute(context);
    }
    
    @Test(expectedExceptions=CitrusRuntimeException.class)
    public void testRepeatOnErrorNoSuccess() {
        RepeatOnErrorUntilTrue repeat = new RepeatOnErrorUntilTrue();
        
        List<TestAction> actions = new ArrayList<TestAction>();
        TestAction action = EasyMock.createMock(TestAction.class);

        reset(action);
        
        action.execute(context);
        expectLastCall().times(4);
        
        replay(action);
        
        actions.add(action);
        actions.add(new FailAction());
        
        repeat.setActions(actions);
        
        repeat.setIndexName("i");
        repeat.setCondition("i = 5");
        repeat.setAutoSleep(0);
        
        repeat.execute(context);
    }
}
