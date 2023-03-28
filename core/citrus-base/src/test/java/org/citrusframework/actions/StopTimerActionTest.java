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

package org.citrusframework.actions;

import org.citrusframework.UnitTestSupport;
import org.citrusframework.container.StopTimer;
import org.citrusframework.context.TestContext;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.verify;

/**
 * @author Martin Maher
 * @since 2.5
 */
public class StopTimerActionTest extends UnitTestSupport {

    @Test
    public void shouldStopSpecificTimer() {
        String timerId = "timer#1";

        StopTimer timer = createMockTimer(context, timerId);

        StopTimerAction stopTimer = new StopTimerAction.Builder()
                .id(timerId)
                .build();

        Assert.assertEquals(stopTimer.getTimerId(), timerId);

        stopTimer.execute(context);

        verify(timer).stopTimer();
    }

    @Test
    public void shouldStopAllTimers() {
        StopTimer timer1 = createMockTimer(context, "timer#1");
        StopTimer timer2 = createMockTimer(context, "timer#2");

        StopTimerAction stopTimer = new StopTimerAction.Builder().build();
        stopTimer.execute(context);

        verify(timer1).stopTimer();
        verify(timer2).stopTimer();
    }

    @Test
    public void shouldNotFailWhenStopingTimerWithUnknownId() {
        StopTimerAction stopTimer = new StopTimerAction.Builder()
                .id("some-unknown-timer")
                .build();
        stopTimer.execute(context);
    }

    private static StopTimer createMockTimer(TestContext context, String timerId) {
        StopTimer timer = Mockito.mock(StopTimer.class);
        context.registerTimer(timerId, timer);
        return timer;
    }
}
