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
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import java.util.*;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class RepeatOnErrorUntilTrueTest extends AbstractTestNGUnitTest {

    private TestAction action = EasyMock.createMock(TestAction.class);

    @Test
    public void testSuccessOnFirstIteration() {
        RepeatOnErrorUntilTrue repeat = new RepeatOnErrorUntilTrue();


        reset(action);
        
        action.execute(context);
        expectLastCall().once();
        
        replay(action);
        
        repeat.setActions(Collections.singletonList(action));
        
        repeat.setIndexName("i");
        repeat.setCondition("i = 5");
        
        repeat.execute(context);
    }
    
    @Test(expectedExceptions=CitrusRuntimeException.class)
    public void testRepeatOnErrorNoSuccess() {
        RepeatOnErrorUntilTrue repeat = new RepeatOnErrorUntilTrue();
        
        List<TestAction> actions = new ArrayList<TestAction>();

        reset(action);
        
        action.execute(context);
        expectLastCall().times(4);
        
        replay(action);
        
        actions.add(action);
        actions.add(new FailAction());
        
        repeat.setActions(actions);
        
        repeat.setIndexName("i");
        repeat.setCondition("i = 5");
        repeat.setAutoSleep(0L);
        
        repeat.execute(context);
    }

    @Test(expectedExceptions=CitrusRuntimeException.class)
    public void testRepeatOnErrorNoSuccessConditionExpression() {
        RepeatOnErrorUntilTrue repeat = new RepeatOnErrorUntilTrue();

        List<TestAction> actions = new ArrayList<TestAction>();

        reset(action);

        action.execute(context);
        expectLastCall().times(4);

        replay(action);

        actions.add(action);
        actions.add(new FailAction());

        repeat.setActions(actions);

        repeat.setConditionExpression(new IteratingConditionExpression() {
            @Override
            public boolean evaluate(int index, TestContext context) {
                return index == 5;
            }
        });
        repeat.setAutoSleep(0L);

        repeat.execute(context);
    }
}
