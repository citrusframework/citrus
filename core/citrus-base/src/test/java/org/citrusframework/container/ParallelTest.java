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

package org.citrusframework.container;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.TestAction;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.EchoAction;
import org.citrusframework.actions.FailAction;
import org.citrusframework.actions.SleepAction;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;


/**
 * @author Christoph Deppisch
 */
public class ParallelTest extends UnitTestSupport {

    private TestAction action = Mockito.mock(TestAction.class);

    @Test
    public void testSingleAction() {
        Parallel parallelAction = new Parallel.Builder().build();

        reset(action);

        List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(action);

        parallelAction.setActions(actionList);

        parallelAction.execute(context);

        verify(action).execute(context);
    }

    @Test
    public void testParallelMultipleActions() {
        Parallel parallelAction = new Parallel.Builder().build();

        reset(action);

        List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(new EchoAction.Builder().build());
        actionList.add(action);
        actionList.add(new EchoAction.Builder().build());

        parallelAction.setActions(actionList);

        parallelAction.execute(context);

        verify(action).execute(context);
    }

    @Test
    public void testParallelActions() {
        Parallel parallelAction = new Parallel.Builder().build();

        List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(new EchoAction.Builder().build());
        actionList.add(new EchoAction.Builder().build());
        actionList.add(new EchoAction.Builder().build());

        SleepAction sleep = new SleepAction.Builder()
                .milliseconds(300L)
                .build();
        actionList.add(sleep);

        parallelAction.setActions(actionList);

        parallelAction.execute(context);
    }

    @Test(expectedExceptions=CitrusRuntimeException.class)
    public void testOneActionThatIsFailing() {
        Parallel parallelAction = new Parallel.Builder().build();

        List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(new FailAction.Builder().build());

        parallelAction.setActions(actionList);

        parallelAction.execute(context);
    }

    @Test(expectedExceptions=CitrusRuntimeException.class)
    public void testOnlyActionFailingActions() {
        Parallel parallelAction = new Parallel.Builder().build();

        List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(new FailAction.Builder().build());
        actionList.add(new FailAction.Builder().build());
        actionList.add(new FailAction.Builder().build());

        parallelAction.setActions(actionList);

        parallelAction.execute(context);
    }

    @Test(expectedExceptions=CitrusRuntimeException.class)
    public void testSingleFailingAction() {
        Parallel parallelAction = new Parallel.Builder().build();

        List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(new EchoAction.Builder().build());
        actionList.add(new FailAction.Builder().build());
        actionList.add(new EchoAction.Builder().build());

        parallelAction.setActions(actionList);

        parallelAction.execute(context);
    }

    @Test(expectedExceptions=CitrusRuntimeException.class)
    public void testSomeFailingActions() {
        Parallel parallelAction = new Parallel.Builder().build();

        reset(action);

        List<TestAction> actionList = new ArrayList<TestAction>();
        actionList.add(new EchoAction.Builder().build());
        actionList.add(new FailAction.Builder().build());
        actionList.add(action);
        actionList.add(new FailAction.Builder().build());

        parallelAction.setActions(actionList);

        parallelAction.execute(context);

        verify(action).execute(context);
    }
}
