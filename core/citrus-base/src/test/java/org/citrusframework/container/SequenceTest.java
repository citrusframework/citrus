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

import org.citrusframework.TestAction;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.FailAction;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;


/**
 * @author Christoph Deppisch
 */
public class SequenceTest extends UnitTestSupport {

    private TestAction action = Mockito.mock(TestAction.class);

    @Test
    public void testSingleAction() {
        reset(action);

        Sequence sequenceAction = new Sequence.Builder()
                .actions(() -> action)
                .build();
        sequenceAction.execute(context);

        verify(action).execute(context);
    }

    @Test
    public void testMultipleActions() {
        TestAction action1 = Mockito.mock(TestAction.class);
        TestAction action2 = Mockito.mock(TestAction.class);
        TestAction action3 = Mockito.mock(TestAction.class);

        reset(action1, action2, action3);

        Sequence sequenceAction = new Sequence.Builder()
                .actions(action1, action2, action3)
                .build();
        sequenceAction.execute(context);
        verify(action1).execute(context);
        verify(action2).execute(context);
        verify(action3).execute(context);
    }

    @Test(expectedExceptions=CitrusRuntimeException.class)
    public void testFirstActionFailing() {
        TestAction action1 = Mockito.mock(TestAction.class);
        TestAction action2 = Mockito.mock(TestAction.class);
        TestAction action3 = Mockito.mock(TestAction.class);

        reset(action1, action2, action3);

        Sequence sequenceAction = new Sequence.Builder()
                .actions(new FailAction.Builder().build(), action1, action2, action3)
                .build();
        sequenceAction.execute(context);
    }

    @Test(expectedExceptions=CitrusRuntimeException.class)
    public void testLastActionFailing() {
        TestAction action1 = Mockito.mock(TestAction.class);
        TestAction action2 = Mockito.mock(TestAction.class);
        TestAction action3 = Mockito.mock(TestAction.class);

        reset(action1, action2, action3);

        Sequence sequenceAction = new Sequence.Builder()
                .actions(action1, action2, action3, new FailAction.Builder().build())
                .build();
        sequenceAction.execute(context);
        verify(action1).execute(context);
        verify(action2).execute(context);
        verify(action3).execute(context);
    }

    @Test(expectedExceptions=CitrusRuntimeException.class)
    public void testFailingAction() {
        TestAction action1 = Mockito.mock(TestAction.class);
        TestAction action2 = Mockito.mock(TestAction.class);
        TestAction action3 = Mockito.mock(TestAction.class);

        reset(action1, action2, action3);

        Sequence sequenceAction = new Sequence.Builder()
                .actions(action1, new FailAction.Builder().build(), action2, action3)
                .build();
        sequenceAction.execute(context);
        verify(action1).execute(context);
    }
}
