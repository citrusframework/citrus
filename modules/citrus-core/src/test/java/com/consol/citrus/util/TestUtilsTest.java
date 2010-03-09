/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.util;

import java.util.*;

import org.springframework.util.CollectionUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.TestAction;
import com.consol.citrus.TestCase;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.container.AbstractActionContainer;
import com.consol.citrus.container.TestActionContainer;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.testng.AbstractBaseTest;

/**
 * @author Christoph Deppisch
 */
public class TestUtilsTest extends AbstractBaseTest {

    @Test
    public void testFirstActionFailing() {
        TestCase test = new TestCase();
        test.setPackageName("com.consol.citrus.util");
        test.setName("FailureStackExampleTest");
        test.setTestContext(context);
        
        TestAction failedAction = new MockedTestAction("sleep");
        
        List<TestAction> actions = new ArrayList<TestAction>();
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
        test.setLastExecutedAction(failedAction);
        
        Stack<String> failureStack = TestUtils.getFailureStack(test);
        
        Assert.assertFalse(failureStack.isEmpty());
        Assert.assertTrue(failureStack.size() == 1);
        Assert.assertEquals(failureStack.pop(), "at com/consol/citrus/util/FailureStackExampleTest(" + failedAction.getName() + ":13)");
    }
    
    @Test
    public void testNestedContainerBeforeFailedAction() {
        TestCase test = new TestCase();
        test.setPackageName("com.consol.citrus.util");
        test.setName("FailureStackExampleTest");
        test.setTestContext(context);
        
        TestAction failedAction = new MockedTestAction("fail");
        
        List<TestAction> actions = new ArrayList<TestAction>();
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
        test.setLastExecutedAction(failedAction);
        
        Stack<String> failureStack = TestUtils.getFailureStack(test);
        
        Assert.assertFalse(failureStack.isEmpty());
        Assert.assertTrue(failureStack.size() == 1);
        Assert.assertEquals(failureStack.pop(), "at com/consol/citrus/util/FailureStackExampleTest(" + failedAction.getName() + ":34)");
    }
    
    @Test
    public void testMiddleActionFailing() {
        TestCase test = new TestCase();
        test.setPackageName("com.consol.citrus.util");
        test.setName("FailureStackExampleTest");
        test.setTestContext(context);
        
        TestAction failedAction = new MockedTestAction("sleep");
        
        List<TestAction> actions = new ArrayList<TestAction>();
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
        test.setLastExecutedAction(failedAction);
        
        Stack<String> failureStack = TestUtils.getFailureStack(test);
        
        Assert.assertFalse(failureStack.isEmpty());
        Assert.assertTrue(failureStack.size() == 1);
        Assert.assertEquals(failureStack.pop(), "at com/consol/citrus/util/FailureStackExampleTest(" + failedAction.getName() + ":24)");
    }
    
    @Test
    public void testActionFailingInContainer() {
        TestCase test = new TestCase();
        test.setPackageName("com.consol.citrus.util");
        test.setName("FailureStackExampleTest");
        test.setTestContext(context);
        
        TestAction failedAction = new MockedTestAction("sleep");
        
        List<TestAction> actions = new ArrayList<TestAction>();
        actions.add(new MockedTestAction("sleep"));
        
        actions.add(new MockedActionContainer("parallel", 
                new MockedTestAction("sleep"),
                new MockedTestAction("fail"),
                new MockedActionContainer("sequential", new MockedTestAction("sleep"), new MockedTestAction("echo"))));
        
        actions.add(new MockedTestAction("sleep"));
        
        TestAction failedContainer = new MockedActionContainer("sequential", 
                new MockedTestAction("echo"),
                failedAction,
                new MockedActionContainer("iterate", new MockedTestAction("sleep")));
        ((TestActionContainer)failedContainer).setLastExecutedAction(failedAction);
        actions.add(failedContainer);
        
        actions.add(new MockedTestAction("fail"));
        actions.add(new MockedTestAction("echo"));
        
        test.setActions(actions);
        test.setLastExecutedAction(failedContainer);
        
        Stack<String> failureStack = TestUtils.getFailureStack(test);
        
        Assert.assertFalse(failureStack.isEmpty());
        Assert.assertTrue(failureStack.size() == 2);
        Assert.assertEquals(failureStack.pop(), "at com/consol/citrus/util/FailureStackExampleTest(" + failedAction.getName() + ":29)");
        Assert.assertEquals(failureStack.pop(), "at com/consol/citrus/util/FailureStackExampleTest(sequential:25)");
    }
    
    public void testActionFailingInContainerHierarchy() {
        TestCase test = new TestCase();
        test.setPackageName("com.consol.citrus.util");
        test.setName("FailureStackExampleTest");
        test.setTestContext(context);
        
        TestAction failedAction = new MockedTestAction("sleep");
        
        List<TestAction> actions = new ArrayList<TestAction>();
        actions.add(new MockedTestAction("sleep"));
        
        actions.add(new MockedActionContainer("parallel", 
                new MockedTestAction("sleep"),
                new MockedTestAction("fail"),
                new MockedActionContainer("sequential", new MockedTestAction("sleep"), new MockedTestAction("echo"))));
        
        actions.add(new MockedTestAction("sleep"));
        
        TestAction failedContainer = new MockedActionContainer("iterate", failedAction);
        ((TestActionContainer)failedContainer).setLastExecutedAction(failedAction);
        
        TestAction nestedContainer = new MockedActionContainer("sequential", 
                new MockedTestAction("echo"),
                new MockedTestAction("sleep"),
                failedContainer);
        ((TestActionContainer)nestedContainer).setLastExecutedAction(failedContainer);
        actions.add(nestedContainer);
        
        actions.add(new MockedTestAction("fail"));
        actions.add(new MockedTestAction("echo"));
        
        test.setActions(actions);
        test.setLastExecutedAction(nestedContainer);
        
        Stack<String> failureStack = TestUtils.getFailureStack(test);
        
        Assert.assertFalse(failureStack.isEmpty());
        Assert.assertTrue(failureStack.size() == 3);
        Assert.assertEquals(failureStack.pop(), "at com/consol/citrus/util/FailureStackExampleTest(" + failedAction.getName() + ":31)");
        Assert.assertEquals(failureStack.pop(), "at com/consol/citrus/util/FailureStackExampleTest(iterate:30)");
        Assert.assertEquals(failureStack.pop(), "at com/consol/citrus/util/FailureStackExampleTest(sequential:25)");
    }
    
    @Test
    public void testContainerItselfFailing() {
        TestCase test = new TestCase();
        test.setPackageName("com.consol.citrus.util");
        test.setName("FailureStackExampleTest");
        test.setTestContext(context);
        
        TestAction failedAction = new MockedActionContainer("sequential", new MockedTestAction("sleep"), new MockedTestAction("echo"));
        
        List<TestAction> actions = new ArrayList<TestAction>();
        actions.add(new MockedTestAction("sleep"));
        
        TestAction failedContainer = new MockedActionContainer("parallel", 
                new MockedTestAction("sleep"),
                new MockedTestAction("fail"),
                failedAction);
        ((TestActionContainer)failedContainer).setLastExecutedAction(failedAction);
        actions.add(failedContainer);
        
        actions.add(new MockedTestAction("sleep"));
        
        actions.add(new MockedActionContainer("sequential", 
                new MockedTestAction("echo"),
                new MockedTestAction("sleep"),
                new MockedActionContainer("iterate", new MockedTestAction("sleep"))));
        
        actions.add(new MockedTestAction("fail"));
        actions.add(new MockedTestAction("echo"));
        
        test.setActions(actions);
        test.setLastExecutedAction(failedContainer);
        
        Stack<String> failureStack = TestUtils.getFailureStack(test);
        
        Assert.assertFalse(failureStack.isEmpty());
        Assert.assertTrue(failureStack.size() == 2);
        Assert.assertEquals(failureStack.pop(), "at com/consol/citrus/util/FailureStackExampleTest(" + failedAction.getName() + ":17)");
        Assert.assertEquals(failureStack.pop(), "at com/consol/citrus/util/FailureStackExampleTest(parallel:14)");
    }

    @Test
    public void testLastActionFailing() {
        TestCase test = new TestCase();
        test.setPackageName("com.consol.citrus.util");
        test.setName("FailureStackExampleTest");
        test.setTestContext(context);
        
        TestAction failedAction = new MockedTestAction("echo");
        
        List<TestAction> actions = new ArrayList<TestAction>();
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
        test.setLastExecutedAction(failedAction);
        
        Stack<String> failureStack = TestUtils.getFailureStack(test);
        
        Assert.assertFalse(failureStack.isEmpty());
        Assert.assertTrue(failureStack.size() == 1);
        Assert.assertEquals(failureStack.pop(), "at com/consol/citrus/util/FailureStackExampleTest(" + failedAction.getName() + ":35)");
    }
    
    private static class MockedTestAction extends AbstractTestAction {

        public MockedTestAction(String name) {
            setName(name);
        }
        
        @Override
        public void execute(TestContext context) {}
    }
    
    private static class MockedActionContainer extends AbstractActionContainer {

        @SuppressWarnings("unchecked")
        public MockedActionContainer(String name, TestAction... actions) {
            setName(name);
            setActions(CollectionUtils.arrayToList(actions));
        }
        
        @Override
        public void execute(TestContext context) {}
    }
}
