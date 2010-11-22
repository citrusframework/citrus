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

import static org.easymock.EasyMock.*;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.TestAction;
import com.consol.citrus.container.RepeatUntilTrue;
import com.consol.citrus.testng.AbstractBaseTest;

/**
 * @author Christoph Deppisch
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
