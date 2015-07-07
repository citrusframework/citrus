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
import com.consol.citrus.actions.EchoAction;
import com.consol.citrus.actions.FailAction;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import java.util.*;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class CatchTest extends AbstractTestNGUnitTest {

    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testCatchDefaultException() {
        Catch catchAction = new Catch();
        
        List actionList = Collections.singletonList(new FailAction());
        catchAction.setActions(actionList);
        
        catchAction.execute(context);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testCatchException() {
        Catch catchAction = new Catch();
        
        List actionList = Collections.singletonList(new FailAction());
        catchAction.setActions(actionList);
        
        catchAction.setException(CitrusRuntimeException.class.getName());
        
        catchAction.execute(context);
    }
    
    @Test
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testNothingToCatch() {
        Catch catchAction = new Catch();
        
        List actionList = Collections.singletonList(new EchoAction());
        catchAction.setActions(actionList);
        
        catchAction.setException(CitrusRuntimeException.class.getName());
        
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
        
        catchAction.setException(CitrusRuntimeException.class.getName());
        
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
        
        catchAction.setException(CitrusRuntimeException.class.getName());
        
        catchAction.execute(context);
    }
}
