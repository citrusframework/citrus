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

package com.consol.citrus.util;

import com.consol.citrus.TestAction;
import com.consol.citrus.TestCase;
import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.container.AbstractActionContainer;
import com.consol.citrus.container.TestActionContainer;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.report.FailureStackElement;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.springframework.util.CollectionUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christoph Deppisch
 */
public class TestUtilsTest extends AbstractTestNGUnitTest {

    @Test
    public void testFirstActionFailing() {
        TestCase test = new TestCase();
        test.setPackageName("com.consol.citrus.util");
        test.setName("FailureStackExampleTest");

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
        test.setActiveAction(failedAction);
        
        List<FailureStackElement> failureStack = TestUtils.getFailureStack(test);
        
        Assert.assertFalse(failureStack.isEmpty());
        Assert.assertTrue(failureStack.size() == 1);
        
        FailureStackElement failureStackElement = failureStack.get(0);
        Assert.assertEquals(failureStackElement.getStackMessage(), "at com/consol/citrus/util/FailureStackExampleTest(" + failedAction.getName() + ":13)");
        Assert.assertEquals(failureStackElement.getLineNumberStart().longValue(), 13L);
    }
    
    @Test
    public void testNestedContainerBeforeFailedAction() {
        TestCase test = new TestCase();
        test.setPackageName("com.consol.citrus.util");
        test.setName("FailureStackExampleTest");

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
        test.setActiveAction(failedAction);
        
        List<FailureStackElement> failureStack = TestUtils.getFailureStack(test);
        
        Assert.assertFalse(failureStack.isEmpty());
        Assert.assertTrue(failureStack.size() == 1);
        FailureStackElement failureStackElement = failureStack.get(0);
        Assert.assertEquals(failureStackElement.getStackMessage(), "at com/consol/citrus/util/FailureStackExampleTest(" + failedAction.getName() + ":34)");
        Assert.assertEquals(failureStackElement.getLineNumberStart().longValue(), 34L);
    }
    
    @Test
    public void testMiddleActionFailing() {
        TestCase test = new TestCase();
        test.setPackageName("com.consol.citrus.util");
        test.setName("FailureStackExampleTest");

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
        test.setActiveAction(failedAction);
        
        List<FailureStackElement> failureStack = TestUtils.getFailureStack(test);
        
        Assert.assertFalse(failureStack.isEmpty());
        Assert.assertTrue(failureStack.size() == 1);
        FailureStackElement failureStackElement = failureStack.get(0);
        Assert.assertEquals(failureStackElement.getStackMessage(), "at com/consol/citrus/util/FailureStackExampleTest(" + failedAction.getName() + ":24)");
        Assert.assertEquals(failureStackElement.getLineNumberStart().longValue(), 24L);
    }
    
    @Test
    public void testActionFailingInContainer() {
        TestCase test = new TestCase();
        test.setPackageName("com.consol.citrus.util");
        test.setName("FailureStackExampleTest");

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
        ((TestActionContainer)failedContainer).setActiveAction(failedAction);
        actions.add(failedContainer);
        
        actions.add(new MockedTestAction("fail"));
        actions.add(new MockedTestAction("echo"));
        
        test.setActions(actions);
        test.setActiveAction(failedContainer);
        
        List<FailureStackElement> failureStack = TestUtils.getFailureStack(test);
        
        Assert.assertFalse(failureStack.isEmpty());
        Assert.assertTrue(failureStack.size() == 2);
        FailureStackElement failureStackElement = failureStack.get(1);
        Assert.assertEquals(failureStackElement.getStackMessage(), "at com/consol/citrus/util/FailureStackExampleTest(" + failedAction.getName() + ":29)");
        Assert.assertEquals(failureStackElement.getLineNumberStart().longValue(), 29L);
        
        failureStackElement = failureStack.get(0);
        Assert.assertEquals(failureStackElement.getStackMessage(), "at com/consol/citrus/util/FailureStackExampleTest(sequential:25)");
        Assert.assertEquals(failureStackElement.getLineNumberStart().longValue(), 25L);
    }
    
    public void testActionFailingInContainerHierarchy() {
        TestCase test = new TestCase();
        test.setPackageName("com.consol.citrus.util");
        test.setName("FailureStackExampleTest");

        TestAction failedAction = new MockedTestAction("sleep");
        
        List<TestAction> actions = new ArrayList<TestAction>();
        actions.add(new MockedTestAction("sleep"));
        
        actions.add(new MockedActionContainer("parallel", 
                new MockedTestAction("sleep"),
                new MockedTestAction("fail"),
                new MockedActionContainer("sequential", new MockedTestAction("sleep"), new MockedTestAction("echo"))));
        
        actions.add(new MockedTestAction("sleep"));
        
        TestAction failedContainer = new MockedActionContainer("iterate", failedAction);
        ((TestActionContainer)failedContainer).setActiveAction(failedAction);
        
        TestAction nestedContainer = new MockedActionContainer("sequential", 
                new MockedTestAction("echo"),
                new MockedTestAction("sleep"),
                failedContainer);
        ((TestActionContainer)nestedContainer).setActiveAction(failedContainer);
        actions.add(nestedContainer);
        
        actions.add(new MockedTestAction("fail"));
        actions.add(new MockedTestAction("echo"));
        
        test.setActions(actions);
        test.setActiveAction(nestedContainer);
        
        List<FailureStackElement> failureStack = TestUtils.getFailureStack(test);
        
        Assert.assertFalse(failureStack.isEmpty());
        Assert.assertTrue(failureStack.size() == 3);
        FailureStackElement failureStackElement = failureStack.get(2);
        Assert.assertEquals(failureStackElement.getStackMessage(), "at com/consol/citrus/util/FailureStackExampleTest(" + failedAction.getName() + ":31)");
        Assert.assertEquals(failureStackElement.getLineNumberStart().longValue(), 31L);
        
        failureStackElement = failureStack.get(1);
        Assert.assertEquals(failureStackElement.getStackMessage(), "at com/consol/citrus/util/FailureStackExampleTest(iterate:30)");
        Assert.assertEquals(failureStackElement.getLineNumberStart().longValue(), 30L);
        
        failureStackElement = failureStack.get(0);
        Assert.assertEquals(failureStackElement.getStackMessage(), "at com/consol/citrus/util/FailureStackExampleTest(sequential:25)");
        Assert.assertEquals(failureStackElement.getLineNumberStart().longValue(), 25L);
    }
    
    @Test
    public void testContainerItselfFailing() {
        TestCase test = new TestCase();
        test.setPackageName("com.consol.citrus.util");
        test.setName("FailureStackExampleTest");

        TestAction failedAction = new MockedActionContainer("sequential", new MockedTestAction("sleep"), new MockedTestAction("echo"));
        
        List<TestAction> actions = new ArrayList<TestAction>();
        actions.add(new MockedTestAction("sleep"));
        
        TestAction failedContainer = new MockedActionContainer("parallel", 
                new MockedTestAction("sleep"),
                new MockedTestAction("fail"),
                failedAction);
        ((TestActionContainer)failedContainer).setActiveAction(failedAction);
        actions.add(failedContainer);
        
        actions.add(new MockedTestAction("sleep"));
        
        actions.add(new MockedActionContainer("sequential", 
                new MockedTestAction("echo"),
                new MockedTestAction("sleep"),
                new MockedActionContainer("iterate", new MockedTestAction("sleep"))));
        
        actions.add(new MockedTestAction("fail"));
        actions.add(new MockedTestAction("echo"));
        
        test.setActions(actions);
        test.setActiveAction(failedContainer);
        
        List<FailureStackElement> failureStack = TestUtils.getFailureStack(test);
        
        Assert.assertFalse(failureStack.isEmpty());
        Assert.assertTrue(failureStack.size() == 2);
        FailureStackElement failureStackElement = failureStack.get(1);
        Assert.assertEquals(failureStackElement.getStackMessage(), "at com/consol/citrus/util/FailureStackExampleTest(" + failedAction.getName() + ":17-22)");
        Assert.assertEquals(failureStackElement.getLineNumberStart().longValue(), 17L);
        Assert.assertEquals(failureStackElement.getLineNumberEnd().longValue(), 22L);
        
        failureStackElement = failureStack.get(0);
        Assert.assertEquals(failureStackElement.getStackMessage(), "at com/consol/citrus/util/FailureStackExampleTest(parallel:14)");
        Assert.assertEquals(failureStackElement.getLineNumberStart().longValue(), 14L);
    }

    @Test
    public void testLastActionFailing() {
        TestCase test = new TestCase();
        test.setPackageName("com.consol.citrus.util");
        test.setName("FailureStackExampleTest");

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
        test.setActiveAction(failedAction);
        
        List<FailureStackElement> failureStack = TestUtils.getFailureStack(test);
        
        Assert.assertFalse(failureStack.isEmpty());
        Assert.assertTrue(failureStack.size() == 1);
        FailureStackElement failureStackElement = failureStack.get(0);
        Assert.assertEquals(failureStackElement.getStackMessage(), "at com/consol/citrus/util/FailureStackExampleTest(" + failedAction.getName() + ":35-37)");
        Assert.assertEquals(failureStackElement.getLineNumberStart().longValue(), 35L);
        Assert.assertEquals(failureStackElement.getLineNumberEnd().longValue(), 37L);
    }
    
    private static class MockedTestAction extends AbstractTestAction {

        public MockedTestAction(String name) {
            setName(name);
        }
        
        @Override
        public void doExecute(TestContext context) {}
    }
    
    private static class MockedActionContainer extends AbstractActionContainer {

        @SuppressWarnings("unchecked")
        public MockedActionContainer(String name, TestAction... actions) {
            setName(name);
            setActions(CollectionUtils.arrayToList(actions));
        }
        
        @Override
        public void doExecute(TestContext context) {}
    }
}
