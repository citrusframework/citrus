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
import com.consol.citrus.actions.WaitAction;
import com.consol.citrus.condition.Condition;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.dsl.builder.BuilderSupport;
import com.consol.citrus.dsl.builder.WaitActionBuilder;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * @author Martin Maher
 * @since 2.4
 */
public class WaitTestRunnerTest extends AbstractTestNGUnitTest {
    private Condition condition = Mockito.mock(Condition.class);

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
                waitFor(new BuilderSupport<WaitActionBuilder>() {
                    @Override
                    public void configure(WaitActionBuilder builder) {
                        builder.condition(condition).seconds(seconds).interval(interval);
                    }
                });
            }
        };

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), WaitAction.class);

        WaitAction action = (WaitAction)test.getActions().get(0);
        Assert.assertEquals(action.getName(), "wait");
        Assert.assertEquals(action.getSeconds(), seconds);
        Assert.assertEquals(action.getInterval(), interval);
        Assert.assertEquals(action.getCondition(), condition);

    }
}
