/*
 * Copyright 2006-2011 the original author or authors.
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

import org.citrusframework.UnitTestSupport;
import org.citrusframework.TestAction;
import org.citrusframework.actions.FailAction;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

/**
 * @author Matthias Beil
 * @since 1.2
 */
public class ConditionalTest extends UnitTestSupport {

    private TestAction action = Mockito.mock(TestAction.class);

    @Test
    public void testConditionFalse() {
        reset(action);

        final Conditional conditionalAction = new Conditional.Builder()
                .when("1 = 0")
                .actions(() -> action)
                .build();
        conditionalAction.execute(this.context);
        verify(action, never()).execute(this.context);
    }

    @Test
    public void testConditionMatcherFalse() {
        reset(action);

        final Conditional conditionalAction = new Conditional.Builder()
                .when("@lowerThan(-1)@")
                .actions(() -> action)
                .build();
        conditionalAction.execute(this.context);
        verify(action, never()).execute(this.context);
    }

    @Test
    public void testSingleAction() {
        final TestAction action = Mockito.mock(TestAction.class);

        reset(action);

        final Conditional conditionalAction = new Conditional.Builder()
                .when("1 = 1")
                .actions(() -> action)
                .build();
        conditionalAction.execute(this.context);

        verify(action).execute(this.context);
    }

    @Test
    public void testMatcherSingleAction() {
        final TestAction action = Mockito.mock(TestAction.class);

        reset(action);

        final Conditional conditionalAction = new Conditional.Builder()
                .when("@empty()@")
                .actions(() -> action)
                .build();
        conditionalAction.execute(this.context);

        verify(action).execute(this.context);
    }

    @Test
    public void testMultipleActions() {
        final TestAction action1 = Mockito.mock(TestAction.class);
        final TestAction action2 = Mockito.mock(TestAction.class);
        final TestAction action3 = Mockito.mock(TestAction.class);

        reset(action1, action2, action3);

        final Conditional conditionalAction = new Conditional.Builder()
                .when("1 = 1")
                .actions(action1, action2, action3)
                .build();
        conditionalAction.execute(this.context);

        verify(action1).execute(this.context);
        verify(action2).execute(this.context);
        verify(action3).execute(this.context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testFirstActionFailing() {
        final TestAction action1 = Mockito.mock(TestAction.class);
        final TestAction action2 = Mockito.mock(TestAction.class);
        final TestAction action3 = Mockito.mock(TestAction.class);

        reset(action1, action2, action3);

        final Conditional conditionalAction = new Conditional.Builder()
                .when("1 = 1")
                .actions(new FailAction.Builder().build(), action1, action2, action3)
                .build();
        conditionalAction.execute(this.context);

    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testLastActionFailing() {
        final TestAction action1 = Mockito.mock(TestAction.class);
        final TestAction action2 = Mockito.mock(TestAction.class);
        final TestAction action3 = Mockito.mock(TestAction.class);

        reset(action1, action2, action3);

        final Conditional conditionalAction = new Conditional.Builder()
                .when("1 = 1")
                .actions(action1, action2, action3, new FailAction.Builder().build())
                .build();
        conditionalAction.execute(this.context);

        verify(action1).execute(this.context);
        verify(action2).execute(this.context);
        verify(action3).execute(this.context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void testFailingAction() {
        final TestAction action1 = Mockito.mock(TestAction.class);
        final TestAction action2 = Mockito.mock(TestAction.class);
        final TestAction action3 = Mockito.mock(TestAction.class);

        reset(action1, action2, action3);

        final Conditional conditionalAction = new Conditional.Builder()
                .when("1 = 1")
                .actions(action1, new FailAction.Builder().build(), action2, action3)
                .build();
        conditionalAction.execute(this.context);

        verify(action1).execute(this.context);
    }

}
