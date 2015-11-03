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

package com.consol.citrus.actions;

import com.consol.citrus.condition.Condition;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.easymock.EasyMock;
import org.springframework.util.StringUtils;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;
import static org.testng.Assert.fail;

/**
 * @author Martin Maher
 * @since 2.4
 */
public class WaitActionTest {
    private TestContext contextMock = EasyMock.createMock(TestContext.class);
    private Condition conditionMock = EasyMock.createMock(Condition.class);
    private long startTime;
    private long endTime;

    @Test
    public void shouldSatisfyWaitConditionOnFirstAttempt() throws Exception {
        String seconds = "10";
        String interval = "1000";

        WaitAction testling = getWaitAction(seconds, interval);

        reset(contextMock, conditionMock);
        prepareContextMock(seconds, interval);
        expect(conditionMock.getName()).andReturn("check").atLeastOnce();
        expect(conditionMock.isSatisfied(contextMock)).andReturn(Boolean.TRUE).once();
        expect(conditionMock.getSuccessMessage(contextMock)).andReturn("Condition success!").once();
        replay(contextMock, conditionMock);

        startTimer();
        testling.doExecute(contextMock);
        stopTimer();

        verify(contextMock, conditionMock);
        assertConditionExecutedWithinSeconds("1");
    }

    @Test
    public void shouldSatisfyWaitConditionOnLastAttempt() throws Exception {
        String seconds = "4";
        String interval = "1000";

        WaitAction testling = getWaitAction(seconds, interval);

        reset(contextMock, conditionMock);
        prepareContextMock(seconds, interval);
        expect(conditionMock.getName()).andReturn("check").atLeastOnce();
        expect(conditionMock.isSatisfied(contextMock)).andReturn(Boolean.FALSE).times(3);
        expect(conditionMock.isSatisfied(contextMock)).andReturn(Boolean.TRUE).once();
        expect(conditionMock.getSuccessMessage(contextMock)).andReturn("Condition success!").once();
        replay(contextMock, conditionMock);

        startTimer();
        testling.doExecute(contextMock);
        stopTimer();

        verify(contextMock, conditionMock);
        assertConditionExecutedWithinSeconds(seconds);
    }

    @Test
    public void shouldSatisfyWaitConditionWithBiggerIntervalThanTimeout() throws Exception {
        String seconds = "1";
        String interval = "10000";

        WaitAction testling = getWaitAction(seconds, interval);

        reset(contextMock, conditionMock);
        prepareContextMock(seconds, interval);
        expect(conditionMock.getName()).andReturn("check").atLeastOnce();
        expect(conditionMock.isSatisfied(contextMock)).andReturn(Boolean.TRUE).once();
        expect(conditionMock.getSuccessMessage(contextMock)).andReturn("Condition success!").once();
        replay(contextMock, conditionMock);

        startTimer();
        testling.doExecute(contextMock);
        stopTimer();

        verify(contextMock, conditionMock);
        assertConditionExecutedWithinSeconds(seconds);
    }

    @Test
    public void shouldNotSatisfyWaitCondition() throws Exception {
        String seconds = "3";
        String interval = "1000";

        WaitAction testling = getWaitAction(seconds, interval);

        reset(contextMock, conditionMock);
        prepareContextMock(seconds, interval);
        expect(conditionMock.getName()).andReturn("check").atLeastOnce();
        expect(conditionMock.isSatisfied(contextMock)).andReturn(Boolean.FALSE).times(3);
        expect(conditionMock.getErrorMessage(contextMock)).andReturn("Condition failed!").once();
        replay(contextMock, conditionMock);

        startTimer();
        try {
            testling.doExecute(contextMock);
            fail("Was expecting CitrusRuntimeException to be thrown");
        } catch (CitrusRuntimeException e) {
            // expected
        }
        stopTimer();

        verify(contextMock, conditionMock);
        assertConditionExecutedWithinSeconds(seconds);
    }


    @Test
    public void shouldNotSatisfyWaitConditionWithBiggerIntervalThanTimeout() throws Exception {
        String seconds = "1";
        String interval = "10000";

        WaitAction testling = getWaitAction(seconds, interval);

        reset(contextMock, conditionMock);
        prepareContextMock(seconds, interval);
        expect(conditionMock.getName()).andReturn("check").atLeastOnce();
        expect(conditionMock.isSatisfied(contextMock)).andReturn(Boolean.FALSE).once();
        expect(conditionMock.getErrorMessage(contextMock)).andReturn("Condition failed!").once();
        replay(contextMock, conditionMock);

        startTimer();
        try {
            testling.doExecute(contextMock);
            fail("Was expecting CitrusRuntimeException to be thrown");
        } catch (CitrusRuntimeException e) {
            // expected
        }
        stopTimer();

        verify(contextMock, conditionMock);
        assertConditionExecutedWithinSeconds(seconds);
    }

    private void prepareContextMock(String waitTime, String interval) {
        expect(contextMock.replaceDynamicContentInString(waitTime)).andReturn(waitTime).atLeastOnce();
        expect(contextMock.replaceDynamicContentInString(interval)).andReturn(interval).atLeastOnce();
    }

    private WaitAction getWaitAction(String waitTimeSeconds, String interval) {
        WaitAction testling = new WaitAction();
        testling.setCondition(conditionMock);

        if (StringUtils.hasText(waitTimeSeconds)) {
            testling.setSeconds(waitTimeSeconds);
        }

        testling.setInterval(interval);
        return testling;
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