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

package com.consol.citrus.dsl.runner;

import com.consol.citrus.TestCase;
import com.consol.citrus.condition.*;
import com.consol.citrus.container.Wait;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.TestCaseFailedException;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

import static org.mockito.Mockito.*;

/**
 * @author Martin Maher
 * @since 2.4
 */
public class WaitTestRunnerTest extends AbstractTestNGUnitTest {

    private Condition condition = Mockito.mock(Condition.class);
    private File file = Mockito.mock(File.class);

    @Test
    public void testWaitBuilder() {
        reset(condition);
        when(condition.getName()).thenReturn("check");
        when(condition.isSatisfied(any(TestContext.class))).thenReturn(Boolean.FALSE);
        when(condition.isSatisfied(any(TestContext.class))).thenReturn(Boolean.TRUE);
        when(condition.getSuccessMessage(any(TestContext.class))).thenReturn("Condition success!");
        final String seconds = "3";
        final String interval = "500";

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                waitFor().seconds(seconds).interval(interval).condition(condition);
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), Wait.class);

        Wait action = (Wait)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "wait");
        Assert.assertEquals(action.getSeconds(), seconds);
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

        final String seconds = "3";
        final String interval = "500";

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                waitFor().file()
                         .seconds(seconds)
                         .interval(interval)
                         .resource(file);
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), Wait.class);

        Wait action = (Wait)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "wait");
        Assert.assertEquals(action.getSeconds(), seconds);
        Assert.assertEquals(action.getInterval(), interval);
        Assert.assertEquals(action.getCondition().getClass(), FileCondition.class);
    }

    @Test
    public void testWaitActionBuilderSuccess() {
        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                waitFor().execution().seconds(1L).interval(300L).action(sleep(200L));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), Wait.class);

        Wait action = (Wait)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "wait");
        Assert.assertEquals(action.getSeconds(), "1");
        Assert.assertEquals(action.getInterval(), "300");
        Assert.assertEquals(action.getCondition().getClass(), ActionCondition.class);
    }

    @Test(expectedExceptions = TestCaseFailedException.class)
    public void testWaitFileBuilderFailed() {
        reset(file);

        when(file.getPath()).thenReturn("path/to/some/file.txt");
        when(file.exists()).thenReturn(false);

        final String seconds = "1";
        final String interval = "500";

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                waitFor().file().seconds(seconds).interval(interval).resource(file);
            }
        };
    }

    @Test(expectedExceptions = TestCaseFailedException.class)
    public void testWaitActionBuilderFailed() {
        new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                waitFor().execution().ms(500L).interval(100L).action(fail("I am failing!"));
            }
        };
    }

    @Test
    public void testWaitBuilderDeprecated() {
        reset(condition);
        when(condition.getName()).thenReturn("check");
        when(condition.isSatisfied(any(TestContext.class))).thenReturn(Boolean.FALSE);
        when(condition.isSatisfied(any(TestContext.class))).thenReturn(Boolean.TRUE);
        when(condition.getSuccessMessage(any(TestContext.class))).thenReturn("Condition success!");

        final String seconds = "3";
        final String interval = "500";

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                waitFor(builder -> builder.interval(interval)
                                          .seconds(seconds)
                                          .condition(condition));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), Wait.class);

        Wait action = (Wait)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "wait");
        Assert.assertEquals(action.getSeconds(), seconds);
        Assert.assertEquals(action.getInterval(), interval);
        Assert.assertEquals(action.getCondition(), condition);
    }

    @Test
    public void testWaitFileBuilderSuccessDeprecated() {
        reset(file);

        when(file.getPath()).thenReturn("path/to/some/file.txt");
        when(file.exists()).thenReturn(false);
        when(file.exists()).thenReturn(true);
        when(file.isFile()).thenReturn(true);

        final String seconds = "3";
        final String interval = "500";

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                waitFor(builder -> builder.seconds(seconds).interval(interval).file().resource(file));
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), Wait.class);

        Wait action = (Wait)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "wait");
        Assert.assertEquals(action.getSeconds(), seconds);
        Assert.assertEquals(action.getInterval(), interval);
        Assert.assertEquals(action.getCondition().getClass(), FileCondition.class);
    }

    @Test(expectedExceptions = TestCaseFailedException.class)
    public void testWaitFileBuilderFailedDeprecated() {
        reset(file);

        when(file.getPath()).thenReturn("path/to/some/file.txt");
        when(file.exists()).thenReturn(false);

        final String seconds = "1";
        final String interval = "500";

        MockTestRunner builder = new MockTestRunner(getClass().getSimpleName(), applicationContext, context) {
            @Override
            public void execute() {
                waitFor(builder -> builder.file().seconds(seconds).interval(interval).resource(file));
            }
        };
    }
}
