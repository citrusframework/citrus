/*
 * Copyright the original author or authors.
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

package org.citrusframework.base.report;

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.base.DefaultTestCase;
import org.citrusframework.TestAction;
import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.api.container.TestActionContainer;
import org.citrusframework.container.AbstractActionContainer;
import org.citrusframework.container.Sequence;
import org.citrusframework.context.TestContext;
import org.citrusframework.report.FailureStackElement;
import org.citrusframework.report.FailureStackTestListener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FailureStackTestListenerTest {

    @Test
    public void testFirstActionFailing() {
        DefaultTestCase test = new DefaultTestCase();
        test.setPackageName("org.citrusframework.base.report");
        test.setName("FailureStackExampleTest");

        TestAction failedAction = new MockedTestAction("sleep");

        List<TestAction> actions = new ArrayList<>();
        actions.add(failedAction);

        actions.add(new MockedActionContainer("parallel",
                new MockedTestAction("sleep"),
                new MockedTestAction("fail"),
                new MockedActionContainer("sequential", new MockedTestAction("sleep"), new MockedTestAction("echo"))));

        actions.add(new MockedTestAction("sleep"));

        actions.add(new MockedActionContainer("sequential",
                new MockedTestAction("echo"),
                new MockedTestAction("sleep"),
                new MockedActionContainer("iterate", new MockedTestAction("sleep"))));

        actions.add(new MockedTestAction("fail"));
        actions.add(new MockedTestAction("echo"));

        test.setActions(actions);
        setActiveActions(test, failedAction);

        List<FailureStackElement> failureStack = FailureStackTestListener.getFailureStack(test);

        Assertions.assertFalse(failureStack.isEmpty());
        Assertions.assertEquals(1, failureStack.size());

        FailureStackElement failureStackElement = failureStack.get(0);
        Assertions.assertEquals("at org/citrusframework/base/report/FailureStackExampleTest(" + failedAction.getName() + ":13)", failureStackElement.getStackMessage());
        Assertions.assertEquals(13L, failureStackElement.getLineNumberStart().longValue());
    }

    @Test
    public void testNestedContainerBeforeFailedAction() {
        DefaultTestCase test = new DefaultTestCase();
        test.setPackageName("org.citrusframework.base.report");
        test.setName("FailureStackExampleTest");

        TestAction failedAction = new MockedTestAction("fail");

        List<TestAction> actions = new ArrayList<>();
        actions.add(new MockedTestAction("sleep"));

        actions.add(new MockedActionContainer("parallel",
                new MockedTestAction("sleep"),
                new MockedTestAction("fail"),
                new MockedActionContainer("sequential", new MockedTestAction("sleep"), new MockedTestAction("echo"))));

        actions.add(new MockedTestAction("sleep"));

        actions.add(new MockedActionContainer("sequential",
                new MockedTestAction("echo"),
                new MockedTestAction("sleep"),
                new MockedActionContainer("iterate", new MockedTestAction("sleep"))));

        actions.add(failedAction);
        actions.add(new MockedTestAction("echo"));

        test.setActions(actions);
        setActiveActions(test, failedAction);

        List<FailureStackElement> failureStack = FailureStackTestListener.getFailureStack(test);

        Assertions.assertFalse(failureStack.isEmpty());
        Assertions.assertEquals(1, failureStack.size());
        FailureStackElement failureStackElement = failureStack.get(0);
        Assertions.assertEquals("at org/citrusframework/base/report/FailureStackExampleTest(" + failedAction.getName() + ":34)", failureStackElement.getStackMessage());
        Assertions.assertEquals(34L, failureStackElement.getLineNumberStart().longValue());
    }

    @Test
    public void testMiddleActionFailing() {
        DefaultTestCase test = new DefaultTestCase();
        test.setPackageName("org.citrusframework.base.report");
        test.setName("FailureStackExampleTest");

        TestAction failedAction = new MockedTestAction("sleep");

        List<TestAction> actions = new ArrayList<>();
        actions.add(new MockedTestAction("sleep"));

        actions.add(new MockedActionContainer("parallel",
                new MockedTestAction("sleep"),
                new MockedTestAction("fail"),
                new MockedActionContainer("sequential", new MockedTestAction("sleep"), new MockedTestAction("echo"))));

        actions.add(failedAction);

        actions.add(new MockedActionContainer("sequential",
                new MockedTestAction("echo"),
                new MockedTestAction("sleep"),
                new MockedActionContainer("iterate", new MockedTestAction("sleep"))));

        actions.add(new MockedTestAction("fail"));
        actions.add(new MockedTestAction("echo"));

        test.setActions(actions);
        setActiveActions(test, failedAction);

        List<FailureStackElement> failureStack = FailureStackTestListener.getFailureStack(test);

        Assertions.assertFalse(failureStack.isEmpty());
        Assertions.assertEquals(1, failureStack.size());
        FailureStackElement failureStackElement = failureStack.get(0);
        Assertions.assertEquals("at org/citrusframework/base/report/FailureStackExampleTest(" + failedAction.getName() + ":24)", failureStackElement.getStackMessage());
        Assertions.assertEquals(24L, failureStackElement.getLineNumberStart().longValue());
    }

    @Test
    public void testActionFailingInContainer() {
        DefaultTestCase test = new DefaultTestCase();
        test.setPackageName("org.citrusframework.base.report");
        test.setName("FailureStackExampleTest");

        TestAction failedAction = new MockedTestAction("sleep");

        List<TestAction> actions = new ArrayList<>();
        actions.add(new MockedTestAction("sleep"));

        actions.add(new MockedActionContainer("parallel",
                new MockedTestAction("sleep"),
                new MockedTestAction("fail"),
                new MockedActionContainer("sequential", new MockedTestAction("sleep"), new MockedTestAction("echo"))));

        actions.add(new MockedTestAction("sleep"));

        TestActionContainer failedContainer = new MockedActionContainer("sequential",
                new MockedTestAction("echo"),
                failedAction,
                new MockedActionContainer("iterate", new MockedTestAction("sleep")));
        setActiveActions(failedContainer, failedAction);
        actions.add(failedContainer);

        actions.add(new MockedTestAction("fail"));
        actions.add(new MockedTestAction("echo"));

        test.setActions(actions);
        setActiveActions(test, failedContainer);

        List<FailureStackElement> failureStack = FailureStackTestListener.getFailureStack(test);

        Assertions.assertFalse(failureStack.isEmpty());
        Assertions.assertEquals(2, failureStack.size());
        FailureStackElement failureStackElement = failureStack.get(1);
        Assertions.assertEquals("at org/citrusframework/base/report/FailureStackExampleTest(" + failedAction.getName() + ":29)", failureStackElement.getStackMessage());
        Assertions.assertEquals(29L, failureStackElement.getLineNumberStart().longValue());

        failureStackElement = failureStack.get(0);
        Assertions.assertEquals("at org/citrusframework/base/report/FailureStackExampleTest(sequential:25)", failureStackElement.getStackMessage());
        Assertions.assertEquals(25L, failureStackElement.getLineNumberStart().longValue());
    }

    @Test
    public void testActionFailingInContainerHierarchy() {
        DefaultTestCase test = new DefaultTestCase();
        test.setPackageName("org.citrusframework.base.report");
        test.setName("FailureStackExampleTest");

        TestAction failedAction = new MockedTestAction("sleep");

        List<TestAction> actions = new ArrayList<>();
        actions.add(new MockedTestAction("sleep"));

        actions.add(new MockedActionContainer("parallel",
                new MockedTestAction("sleep"),
                new MockedTestAction("fail"),
                new MockedActionContainer("sequential", new MockedTestAction("sleep"), new MockedTestAction("echo"))));

        actions.add(new MockedTestAction("sleep"));

        TestActionContainer failedContainer = new MockedActionContainer("iterate", failedAction);
        setActiveActions(failedContainer, failedAction);

        TestActionContainer nestedContainer = new MockedActionContainer("sequential",
                new MockedTestAction("echo"),
                new MockedTestAction("sleep"),
                failedContainer);
        setActiveActions(nestedContainer, failedContainer);
        actions.add(nestedContainer);

        actions.add(new MockedTestAction("fail"));
        actions.add(new MockedTestAction("echo"));

        test.setActions(actions);
        setActiveActions(test, nestedContainer);

        List<FailureStackElement> failureStack = FailureStackTestListener.getFailureStack(test);

        Assertions.assertFalse(failureStack.isEmpty());
        Assertions.assertEquals(3, failureStack.size());
        FailureStackElement failureStackElement = failureStack.get(2);
        Assertions.assertEquals("at org/citrusframework/base/report/FailureStackExampleTest(" + failedAction.getName() + ":31)", failureStackElement.getStackMessage());
        Assertions.assertEquals(31L, failureStackElement.getLineNumberStart().longValue());

        failureStackElement = failureStack.get(1);
        Assertions.assertEquals("at org/citrusframework/base/report/FailureStackExampleTest(iterate:30)", failureStackElement.getStackMessage());
        Assertions.assertEquals(30L, failureStackElement.getLineNumberStart().longValue());

        failureStackElement = failureStack.get(0);
        Assertions.assertEquals("at org/citrusframework/base/report/FailureStackExampleTest(sequential:25)", failureStackElement.getStackMessage());
        Assertions.assertEquals(25L, failureStackElement.getLineNumberStart().longValue());
    }

    @Test
    public void testContainerItselfFailing() {
        DefaultTestCase test = new DefaultTestCase();
        test.setPackageName("org.citrusframework.base.report");
        test.setName("FailureStackExampleTest");

        TestAction failedAction = new MockedActionContainer("sequential", new MockedTestAction("sleep"), new MockedTestAction("echo"));

        List<TestAction> actions = new ArrayList<>();
        actions.add(new MockedTestAction("sleep"));

        TestActionContainer failedContainer = new MockedActionContainer("parallel",
                new MockedTestAction("sleep"),
                new MockedTestAction("fail"),
                failedAction);
        setActiveActions(failedContainer, failedAction);
        actions.add(failedContainer);

        actions.add(new MockedTestAction("sleep"));

        actions.add(new MockedActionContainer("sequential",
                new MockedTestAction("echo"),
                new MockedTestAction("sleep"),
                new MockedActionContainer("iterate", new MockedTestAction("sleep"))));

        actions.add(new MockedTestAction("fail"));
        actions.add(new MockedTestAction("echo"));

        test.setActions(actions);
        setActiveActions(test, failedContainer);

        List<FailureStackElement> failureStack = FailureStackTestListener.getFailureStack(test);

        Assertions.assertFalse(failureStack.isEmpty());
        Assertions.assertEquals(2, failureStack.size());
        FailureStackElement failureStackElement = failureStack.get(1);
        Assertions.assertEquals("at org/citrusframework/base/report/FailureStackExampleTest(" + failedAction.getName() + ":17-22)", failureStackElement.getStackMessage());
        Assertions.assertEquals(17L, failureStackElement.getLineNumberStart().longValue());
        Assertions.assertEquals(22L, failureStackElement.getLineNumberEnd().longValue());

        failureStackElement = failureStack.get(0);
        Assertions.assertEquals("at org/citrusframework/base/report/FailureStackExampleTest(parallel:14)", failureStackElement.getStackMessage());
        Assertions.assertEquals(14L, failureStackElement.getLineNumberStart().longValue());
    }

    @Test
    public void testLastActionFailing() {
        DefaultTestCase test = new DefaultTestCase();
        test.setPackageName("org.citrusframework.base.report");
        test.setName("FailureStackExampleTest");

        TestAction failedAction = new MockedTestAction("echo");

        List<TestAction> actions = new ArrayList<>();
        actions.add(new MockedTestAction("sleep"));

        actions.add(new MockedActionContainer("parallel",
                new MockedTestAction("sleep"),
                new MockedTestAction("fail"),
                new MockedActionContainer("sequential", new MockedTestAction("sleep"), new MockedTestAction("echo"))));

        actions.add(new MockedTestAction("sleep"));

        actions.add(new MockedActionContainer("sequential",
                new MockedTestAction("echo"),
                new MockedTestAction("sleep"),
                new MockedActionContainer("iterate", new MockedTestAction("sleep"))));

        actions.add(new MockedTestAction("fail"));
        actions.add(failedAction);

        test.setActions(actions);
        setActiveActions(test, failedAction);

        List<FailureStackElement> failureStack = FailureStackTestListener.getFailureStack(test);

        Assertions.assertFalse(failureStack.isEmpty());
        Assertions.assertEquals(1, failureStack.size());
        FailureStackElement failureStackElement = failureStack.get(0);
        Assertions.assertEquals("at org/citrusframework/base/report/FailureStackExampleTest(" + failedAction.getName() + ":35-37)", failureStackElement.getStackMessage());
        Assertions.assertEquals(35L, failureStackElement.getLineNumberStart().longValue());
        Assertions.assertEquals(37L, failureStackElement.getLineNumberEnd().longValue());
    }

    private void setActiveActions(TestActionContainer container, TestAction failedAction) {
        for (TestAction action : container.getActions()) {
            container.setActiveAction(action);
            container.setExecutedAction(action);
            if (action.equals(failedAction)) {
                break;
            }
        }
    }

    private static class MockedTestAction extends AbstractTestAction {

        public MockedTestAction(String name) {
            setName(name);
        }

        @Override
        public void doExecute(TestContext context) {}
    }

    private static class MockedActionContainer extends AbstractActionContainer {
        public MockedActionContainer(String name, TestAction... actions) {
            super(name, new Sequence.Builder().actions(actions));
        }

        @Override
        public void doExecute(TestContext context) {}
    }
}
