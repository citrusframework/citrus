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

package com.consol.citrus.group;

import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.TestAction;
import com.consol.citrus.testng.AbstractBaseTest;

/**
 * @author Christoph Christoph Deppisch Consol* Software GmbH
 */
public class IterateTest extends AbstractBaseTest {
    @Test
    public void testIteration() {
        Iterate iterate = new Iterate();
        
        List<TestAction> actions = new ArrayList<TestAction>();
        TestAction action = EasyMock.createMock(TestAction.class);

        reset(action);
        
        action.execute(context);
        expectLastCall().times(5);
        
        replay(action);
        
        actions.add(action);
        iterate.setActions(actions);
        
        iterate.setCondition("i lt= 5");
        iterate.setIndexName("i");
        
        iterate.execute(context);
        
        Assert.assertNotNull(context.getVariable("${i}"));
        Assert.assertEquals(context.getVariable("${i}"), "5");
    }
    
    @Test
    public void testStep() {
        Iterate iterate = new Iterate();
        
        List<TestAction> actions = new ArrayList<TestAction>();
        TestAction action = EasyMock.createMock(TestAction.class);

        reset(action);
        
        action.execute(context);
        expectLastCall().times(5);
        
        replay(action);
        
        actions.add(action);
        iterate.setActions(actions);
        
        iterate.setCondition("i lt= 10");
        iterate.setIndexName("i");
        iterate.setStep(2);
        
        iterate.execute(context);
        
        Assert.assertNotNull(context.getVariable("${i}"));
        Assert.assertEquals(context.getVariable("${i}"), "9");
    }
    
    @Test
    public void testStart() {
        Iterate iterate = new Iterate();
        
        List<TestAction> actions = new ArrayList<TestAction>();
        TestAction action = EasyMock.createMock(TestAction.class);

        reset(action);
        
        action.execute(context);
        expectLastCall().times(5);
        
        replay(action);
        
        actions.add(action);
        iterate.setActions(actions);
        
        iterate.setCondition("i lt= 10");
        iterate.setIndexName("i");
        iterate.setStep(2);
        iterate.setIndex(2);
        
        iterate.execute(context);
        
        Assert.assertNotNull(context.getVariable("${i}"));
        Assert.assertEquals(context.getVariable("${i}"), "10");
    }
    
    @Test
    public void testNoIterationBasedOnCondition() {
        Iterate iterate = new Iterate();
        
        List<TestAction> actions = new ArrayList<TestAction>();
        TestAction action = EasyMock.createMock(TestAction.class);

        reset(action);
        replay(action);
        
        actions.add(action);
        iterate.setActions(actions);
        
        iterate.setCondition("i lt 0");
        iterate.setIndexName("i");
        
        iterate.execute(context);
        
        Assert.assertNull(context.getVariables().get("i"));
    }
}
