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

package org.citrusframework.report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.citrusframework.DefaultTestCase;
import org.citrusframework.TestAction;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.AbstractTestAction;
import org.citrusframework.container.AbstractActionContainer;
import org.citrusframework.container.TestActionContainer;
import org.citrusframework.context.TestContext;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Christoph Deppisch
 */
public class FailureStackTestListenerTest extends UnitTestSupport {

    @Test
    public void testFirstActionFailing() {
        DefaultTestCase test = new DefaultTestCase();
        test.setPackageName("org.citrusframework.util");
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

        Assert.assertFalse(failureStack.isEmpty());
        Assert.assertEquals(failureStack.size(), 1);

        FailureStackElement failureStackElement = failureStack.get(0);
        Assert.assertEquals(failureStackElement.getStackMessage(), "at org/citrusframework/util/FailureStackExampleTest(" + failedAction.getName() + ":13)");
        Assert.assertEquals(failureStackElement.getLineNumberStart().longValue(), 13L);
    }

    @Test
    public void testNestedContainerBeforeFailedAction() {
        DefaultTestCase test = new DefaultTestCase();
        test.setPackageName("org.citrusframework.util");
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

        Assert.assertFalse(failureStack.isEmpty());
        Assert.assertEquals(failureStack.size(), 1);
        FailureStackElement failureStackElement = failureStack.get(0);
        Assert.assertEquals(failureStackElement.getStackMessage(), "at org/citrusframework/util/FailureStackExampleTest(" + failedAction.getName() + ":34)");
        Assert.assertEquals(failureStackElement.getLineNumberStart().longValue(), 34L);
    }

    @Test
    public void testMiddleActionFailing() {
        DefaultTestCase test = new DefaultTestCase();
        test.setPackageName("org.citrusframework.util");
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

        Assert.assertFalse(failureStack.isEmpty());
        Assert.assertEquals(failureStack.size(), 1);
        FailureStackElement failureStackElement = failureStack.get(0);
        Assert.assertEquals(failureStackElement.getStackMessage(), "at org/citrusframework/util/FailureStackExampleTest(" + failedAction.getName() + ":24)");
        Assert.assertEquals(failureStackElement.getLineNumberStart().longValue(), 24L);
    }

    @Test
    public void testActionFailingInContainer() {
        DefaultTestCase test = new DefaultTestCase();
        test.setPackageName("org.citrusframework.util");
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

        Assert.assertFalse(failureStack.isEmpty());
        Assert.assertEquals(failureStack.size(), 2);
        FailureStackElement failureStackElement = failureStack.get(1);
        Assert.assertEquals(failureStackElement.getStackMessage(), "at org/citrusframework/util/FailureStackExampleTest(" + failedAction.getName() + ":29)");
        Assert.assertEquals(failureStackElement.getLineNumberStart().longValue(), 29L);

        failureStackElement = failureStack.get(0);
        Assert.assertEquals(failureStackElement.getStackMessage(), "at org/citrusframework/util/FailureStackExampleTest(sequential:25)");
        Assert.assertEquals(failureStackElement.getLineNumberStart().longValue(), 25L);
    }

    public void testActionFailingInContainerHierarchy() {
        DefaultTestCase test = new DefaultTestCase();
        test.setPackageName("org.citrusframework.util");
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
        nestedContainer.setActiveAction(failedContainer);
        nestedContainer.setExecutedAction(failedContainer);
        actions.add(nestedContainer);

        actions.add(new MockedTestAction("fail"));
        actions.add(new MockedTestAction("echo"));

        test.setActions(actions);
        setActiveActions(test, nestedContainer);

        List<FailureStackElement> failureStack = FailureStackTestListener.getFailureStack(test);

        Assert.assertFalse(failureStack.isEmpty());
        Assert.assertEquals(failureStack.size(), 3);
        FailureStackElement failureStackElement = failureStack.get(2);
        Assert.assertEquals(failureStackElement.getStackMessage(), "at org/citrusframework/util/FailureStackExampleTest(" + failedAction.getName() + ":31)");
        Assert.assertEquals(failureStackElement.getLineNumberStart().longValue(), 31L);

        failureStackElement = failureStack.get(1);
        Assert.assertEquals(failureStackElement.getStackMessage(), "at org/citrusframework/util/FailureStackExampleTest(iterate:30)");
        Assert.assertEquals(failureStackElement.getLineNumberStart().longValue(), 30L);

        failureStackElement = failureStack.get(0);
        Assert.assertEquals(failureStackElement.getStackMessage(), "at org/citrusframework/util/FailureStackExampleTest(sequential:25)");
        Assert.assertEquals(failureStackElement.getLineNumberStart().longValue(), 25L);
    }

    @Test
    public void testContainerItselfFailing() {
        DefaultTestCase test = new DefaultTestCase();
        test.setPackageName("org.citrusframework.util");
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

        Assert.assertFalse(failureStack.isEmpty());
        Assert.assertEquals(failureStack.size(), 2);
        FailureStackElement failureStackElement = failureStack.get(1);
        Assert.assertEquals(failureStackElement.getStackMessage(), "at org/citrusframework/util/FailureStackExampleTest(" + failedAction.getName() + ":17-22)");
        Assert.assertEquals(failureStackElement.getLineNumberStart().longValue(), 17L);
        Assert.assertEquals(failureStackElement.getLineNumberEnd().longValue(), 22L);

        failureStackElement = failureStack.get(0);
        Assert.assertEquals(failureStackElement.getStackMessage(), "at org/citrusframework/util/FailureStackExampleTest(parallel:14)");
        Assert.assertEquals(failureStackElement.getLineNumberStart().longValue(), 14L);
    }

    @Test
    public void testLastActionFailing() {
        DefaultTestCase test = new DefaultTestCase();
        test.setPackageName("org.citrusframework.util");
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

        Assert.assertFalse(failureStack.isEmpty());
        Assert.assertEquals(failureStack.size(), 1);
        FailureStackElement failureStackElement = failureStack.get(0);
        Assert.assertEquals(failureStackElement.getStackMessage(), "at org/citrusframework/util/FailureStackExampleTest(" + failedAction.getName() + ":35-37)");
        Assert.assertEquals(failureStackElement.getLineNumberStart().longValue(), 35L);
        Assert.assertEquals(failureStackElement.getLineNumberEnd().longValue(), 37L);
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
            setName(name);
            setActions(Arrays.asList(actions));
        }

        @Override
        public void doExecute(TestContext context) {}
    }
}
