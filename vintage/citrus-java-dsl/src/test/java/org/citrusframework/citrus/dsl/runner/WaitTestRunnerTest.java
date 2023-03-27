/*
 * Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.citrus.dsl.runner;

import java.io.File;

import org.citrusframework.citrus.TestCase;
import org.citrusframework.citrus.condition.ActionCondition;
import org.citrusframework.citrus.condition.Condition;
import org.citrusframework.citrus.condition.FileCondition;
import org.citrusframework.citrus.container.Wait;
import org.citrusframework.citrus.context.TestContext;
import org.citrusframework.citrus.exceptions.TestCaseFailedException;
import org.citrusframework.citrus.dsl.UnitTestSupport;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Martin Maher
 * @since 2.4
 */
public class WaitTestRunnerTest extends UnitTestSupport {

    private Condition condition = Mockito.mock(Condition.class);
    private File file = Mockito.mock(File.class);

    @Test
    public void testWaitBuilder() {
        reset(condition);
        when(condition.getName()).thenReturn("check");
        when(condition.isSatisfied(any(TestContext.class))).thenReturn(Boolean.FALSE);
        when(condition.isSatisfied(any(TestContext.class))).thenReturn(Boolean.TRUE);
        when(condition.getSuccessMessage(any(TestContext.class))).thenReturn("Condition success!");
        final double seconds = 3.0;
        final String interval = "500";

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                waitFor()
                        .seconds(seconds)
                        .interval(interval)
                        .condition(condition);
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), Wait.class);

        Wait action = (Wait)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "wait");
        Assert.assertEquals(action.getTime(), "3000");
        Assert.assertEquals(action.getInterval(), interval);
        Assert.assertEquals(action.getCondition(), condition);
    }

    @Test
    public void testWaitFileBuilderSuccess() {
        reset(file);

        when(file.getPath()).thenReturn("path/to/some/file.txt");
        when(file.exists()).thenReturn(false);
        when(file.exists()).thenReturn(true);
        when(file.isFile()).thenReturn(true);

        final String time = "3000";
        final String interval = "500";

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                waitFor().file()
                         .milliseconds(time)
                         .interval(interval)
                         .resource(file);
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), Wait.class);

        Wait action = (Wait)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "wait");
        Assert.assertEquals(action.getTime(), time);
        Assert.assertEquals(action.getInterval(), interval);
        Assert.assertEquals(action.getCondition().getClass(), FileCondition.class);
    }

    @Test
    public void testWaitActionBuilderSuccess() {
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                waitFor().execution()
                        .seconds(1L)
                        .interval(300L)
                        .action(sleep(200L));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), Wait.class);

        Wait action = (Wait)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "wait");
        Assert.assertEquals(action.getTime(), "1000");
        Assert.assertEquals(action.getInterval(), "300");
        Assert.assertEquals(action.getCondition().getClass(), ActionCondition.class);
    }

    @Test(expectedExceptions = TestCaseFailedException.class)
    public void testWaitFileBuilderFailed() {
        reset(file);

        when(file.getPath()).thenReturn("path/to/some/file.txt");
        when(file.exists()).thenReturn(false);

        final long seconds = 1L;
        final String interval = "500";

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                waitFor().file()
                        .seconds(seconds)
                        .interval(interval)
                        .resource(file);
            }
        };
    }

    @Test(expectedExceptions = TestCaseFailedException.class)
    public void testWaitActionBuilderFailed() {
        new MockTestRunner(getClass().getSimpleName(), context) {
            @Override
            public void execute() {
                waitFor().execution()
                        .milliseconds(500L)
                        .interval(100L)
                        .action(fail("I am failing!"));
            }
        };
    }
}
