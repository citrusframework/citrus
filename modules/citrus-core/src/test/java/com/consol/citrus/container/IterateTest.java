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

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.TestAction;
import com.consol.citrus.testng.AbstractBaseTest;

/**
 * @author Christoph Deppisch
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
