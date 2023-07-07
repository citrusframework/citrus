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

package org.citrusframework.actions.dsl;

import org.citrusframework.DefaultTestCaseRunner;
import org.citrusframework.TestCase;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.container.Timer;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.citrusframework.actions.EchoAction.Builder.echo;
import static org.citrusframework.container.Timer.Builder.timer;

/**
 * @author Martin Maher
 * @since 2.5
 */
public class TimerTestActionBuilderTest extends UnitTestSupport {

    @Test
    public void testTimerBuilder() {
        final String timerId = "testTimer1";
        final int delay = 100;
        final int interval = 200;
        final int repeatCount = 1;
        final boolean fork = false;

        DefaultTestCaseRunner builder = new DefaultTestCaseRunner(context);
        builder.$(timer()
                    .timerId(timerId)
                    .delay(delay)
                    .interval(interval)
                    .repeatCount(repeatCount)
                    .fork(fork)
                    .actions(echo("hello")));

        TestCase test = builder.getTestCase();
        Assert.assertEquals(test.getActionCount(), 1);
        Assert.assertEquals(test.getActions().get(0).getClass(), Timer.class);

        Timer action = (Timer) test.getActions().get(0);
        Assert.assertEquals(action.getName(), "timer");
        Assert.assertEquals(action.getDelay(), delay);
        Assert.assertEquals(action.getInterval(), interval);
        Assert.assertEquals(action.getRepeatCount(), repeatCount);
        Assert.assertEquals(action.isFork(), fork);
        Assert.assertEquals(action.getActionCount(), 1);
    }
}
