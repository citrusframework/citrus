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

package org.citrusframework.container;

import org.citrusframework.TestAction;
import org.citrusframework.UnitTestSupport;
import org.citrusframework.actions.FailAction;
import org.citrusframework.actions.SleepAction;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Martin Maher
 * @since 2.5
 */
public class TimerTest extends UnitTestSupport {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(TimerTest.class);

    private final TestAction action = Mockito.mock(TestAction.class);
    private final int defaultRepeatCount = 3;
    private final long defaultInterval = 50L;

    @Test
    public void shouldSuccessfullyRunTimerWithNestedAction() {
        reset(action);

        Timer timer = createDefaultTimerWithNestedAction(false, action);

        timer.execute(context);

        assertTimerIndex(defaultRepeatCount, timer);

        verify(action, times(defaultRepeatCount)).execute(context);
    }

    @Test
    public void shouldSuccessfullyRunTimerWithNestedActionThatTakesLongerThanTimerInterval() {
        reset(action);

        Timer timer = createDefaultTimerWithNestedAction(false, action, getSleepAction());

        timer.execute(context);

        assertTimerIndex(defaultRepeatCount, timer);

        verify(action, times(defaultRepeatCount)).execute(context);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class)
    public void shouldFailPropagatingErrorUpCallStack() {
        Timer timer = createDefaultTimerWithNestedActionThatFails(false);
        timer.execute(context);
    }

    @Test
    public void shouldSuccessfullyRunForkedTimerWithNestedAction() {
        reset(action);

        Timer timer = createDefaultTimerWithNestedAction(true, action);

        timer.execute(context);

        allowForkedTimerToComplete(defaultInterval * defaultRepeatCount);

        assertTimerIndex(defaultRepeatCount, timer);

        verify(action, times(defaultRepeatCount)).execute(context);
    }

    @Test
    public void shouldCompleteSuccessfullyForForkedTimerWithNestedActionThatFails() {
        Timer timer = createDefaultTimerWithNestedActionThatFails(true);
        timer.execute(context);

        allowForkedTimerToComplete(defaultInterval);

        assertTimerIndex(1, timer);
        assertNotNull(timer.timerException);
    }

    private Timer createDefaultTimerWithNestedActionThatFails(boolean forked) {
        return createDefaultTimerWithNestedAction(forked, getFailAction());
    }

    private Timer createDefaultTimerWithNestedAction(boolean forked, TestAction... testAction) {
        return createTimerWithNestedAction(defaultRepeatCount, defaultInterval, forked, testAction);
    }

    private FailAction getFailAction() {
        return new FailAction.Builder().message("Something nasty happened").build();
    }

    private SleepAction getSleepAction() {
        return new SleepAction.Builder()
                .milliseconds(200L)
                .build();
    }

    private Timer createTimerWithNestedAction(int repeatCount, long interval, boolean forked, TestAction... actions) {
        return new Timer.Builder()
                .interval(interval)
                .repeatCount(repeatCount)
                .fork(forked)
                .actions(actions)
                .build();
    }

    private void allowForkedTimerToComplete(long sleepTime) {
        try {
            Thread.currentThread().sleep(sleepTime + 1000L);
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting for forked timer", e);
        }
    }

    private void assertTimerIndex(int expectedValue, Timer timer) {
        assertEquals(context.getVariable(timer.getTimerId() + Timer.INDEX_SUFFIX), String.valueOf(expectedValue));
    }

}
