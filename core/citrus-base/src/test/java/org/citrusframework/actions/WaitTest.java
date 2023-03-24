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

import org.citrusframework.condition.Condition;
import org.citrusframework.container.Wait;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.testng.Assert.fail;

/**
 * @author Martin Maher
 * @since 2.4
 */
public class WaitTest {
    private TestContext contextMock = Mockito.mock(TestContext.class);
    private Condition conditionMock = Mockito.mock(Condition.class);
    private long startTime;
    private long endTime;

    @Test
    public void shouldSatisfyWaitConditionOnFirstAttempt() {
        String seconds = "10";
        String interval = "1000";

        Wait testling = getWaitAction(seconds, interval);

        reset(contextMock, conditionMock);
        prepareContextMock("10000", interval);
        when(conditionMock.getName()).thenReturn("check");
        when(conditionMock.isSatisfied(contextMock)).thenReturn(Boolean.TRUE);
        when(conditionMock.getSuccessMessage(contextMock)).thenReturn("Condition success!");
        startTimer();
        testling.execute(contextMock);
        stopTimer();

        assertConditionExecutedWithinSeconds("1");
    }

    @Test
    public void shouldSatisfyWaitConditionOnLastAttempt() {
        String seconds = "4";
        String interval = "1000";

        Wait testling = getWaitAction(seconds, interval);

        reset(contextMock, conditionMock);
        prepareContextMock("4000", interval);
        when(conditionMock.getName()).thenReturn("check");
        when(conditionMock.isSatisfied(contextMock)).thenReturn(Boolean.FALSE);
        when(conditionMock.isSatisfied(contextMock)).thenReturn(Boolean.TRUE);
        when(conditionMock.getSuccessMessage(contextMock)).thenReturn("Condition success!");
        startTimer();
        testling.execute(contextMock);
        stopTimer();

        assertConditionExecutedWithinSeconds(seconds);
    }

    @Test
    public void shouldSatisfyWaitConditionWithBiggerIntervalThanTimeout() {
        String seconds = "1";
        String interval = "10000";

        Wait testling = getWaitAction(seconds, interval);

        reset(contextMock, conditionMock);
        prepareContextMock("1000", interval);
        when(conditionMock.getName()).thenReturn("check");
        when(conditionMock.isSatisfied(contextMock)).thenReturn(Boolean.TRUE);
        when(conditionMock.getSuccessMessage(contextMock)).thenReturn("Condition success!");
        startTimer();
        testling.execute(contextMock);
        stopTimer();

        assertConditionExecutedWithinSeconds(seconds);
    }

    @Test
    public void shouldNotSatisfyWaitCondition() {
        String seconds = "3";
        String interval = "1000";

        Wait testling = getWaitAction(seconds, interval);

        reset(contextMock, conditionMock);
        prepareContextMock("3000", interval);
        when(conditionMock.getName()).thenReturn("check");
        when(conditionMock.isSatisfied(contextMock)).thenReturn(Boolean.FALSE);
        when(conditionMock.getErrorMessage(contextMock)).thenReturn("Condition failed!");
        startTimer();
        try {
            testling.execute(contextMock);
            fail("Was expecting CitrusRuntimeException to be thrown");
        } catch (CitrusRuntimeException e) {
            // expected
        }
        stopTimer();

        assertConditionExecutedWithinSeconds(seconds);
    }

    @Test
    public void shouldNotSatisfyWaitConditionWithBiggerIntervalThanTimeout() {
        String seconds = "1";
        String interval = "10000";

        Wait testling = getWaitAction(seconds, interval);

        reset(contextMock, conditionMock);
        prepareContextMock("1000", interval);
        when(conditionMock.getName()).thenReturn("check");
        when(conditionMock.isSatisfied(contextMock)).thenReturn(Boolean.FALSE);
        when(conditionMock.getErrorMessage(contextMock)).thenReturn("Condition failed!");
        startTimer();
        try {
            testling.execute(contextMock);
            fail("Was expecting CitrusRuntimeException to be thrown");
        } catch (CitrusRuntimeException e) {
            // expected
        }
        stopTimer();

        assertConditionExecutedWithinSeconds(seconds);
    }

    private void prepareContextMock(String waitTime, String interval) {
        when(contextMock.replaceDynamicContentInString(waitTime)).thenReturn(waitTime);
        when(contextMock.replaceDynamicContentInString(interval)).thenReturn(interval);
    }

    private Wait getWaitAction(String waitTimeSeconds, String interval) {
        return new Wait.Builder()
                .condition(conditionMock)
                .interval(interval)
                .seconds(Long.parseLong(waitTimeSeconds))
                .build();
    }

    private void assertConditionExecutedWithinSeconds(String seconds) {
        final long tolerance = 500L; // allow some tolerance in check
        final long totalExecutionTime = endTime - startTime;
        long permittedTime = (Integer.parseInt(seconds) * 1000L) + tolerance;
        if (totalExecutionTime > permittedTime) {
            fail(String.format("Expected conditional check to execute in %s milliseconds but took %s milliseconds", permittedTime, totalExecutionTime));
        }
    }

    private void startTimer() {
        startTime = System.currentTimeMillis();
    }

    private void stopTimer() {
        endTime = System.currentTimeMillis();
    }
}
