/*
 * Copyright 2006-2010 ConSol* Software GmbH.
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
public class RepeatUntilTrueTest extends AbstractBaseTest {
    @Test
    public void testRepeat() {
        RepeatUntilTrue repeatUntilTrue = new RepeatUntilTrue();
        
        List<TestAction> actions = new ArrayList<TestAction>();
        TestAction action = EasyMock.createMock(TestAction.class);

        reset(action);
        
        action.execute(context);
        expectLastCall().times(4);
        
        replay(action);
        
        actions.add(action);
        repeatUntilTrue.setActions(actions);
        
        repeatUntilTrue.setCondition("i = 5");
        repeatUntilTrue.setIndexName("i");
        
        repeatUntilTrue.execute(context);
        
        Assert.assertNotNull(context.getVariable("${i}"));
        Assert.assertEquals(context.getVariable("${i}"), "4");
    }
    
    @Test
    public void testRepeatMinimumOnce() {
        RepeatUntilTrue repeatUntilTrue = new RepeatUntilTrue();
        
        List<TestAction> actions = new ArrayList<TestAction>();
        TestAction action = EasyMock.createMock(TestAction.class);

        reset(action);
        
        action.execute(context);
        expectLastCall().once();
        
        replay(action);
        
        actions.add(action);
        repeatUntilTrue.setActions(actions);
        
        repeatUntilTrue.setCondition("i gt 0");
        repeatUntilTrue.setIndexName("i");
        
        repeatUntilTrue.execute(context);
        
        Assert.assertNotNull(context.getVariable("${i}"));
        Assert.assertEquals(context.getVariable("${i}"), "1");
    }
}
