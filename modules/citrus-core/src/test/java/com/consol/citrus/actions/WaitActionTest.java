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
        String waitForSeconds = "10";
        String testIntervalSeconds = "1";

        WaitAction testling = getWaitAction(waitForSeconds, testIntervalSeconds);

        reset(contextMock, conditionMock);
        prepareContextMock(waitForSeconds, testIntervalSeconds);
        expect(conditionMock.isSatisfied(contextMock)).andReturn(Boolean.TRUE).once();
        replay(contextMock, conditionMock);

        startTimer();
        testling.doExecute(contextMock);
        stopTimer();

        verify(contextMock, conditionMock);
        assertConditionExecutedWithinSeconds("1");
    }

    @Test
    public void shouldSatisfyWaitConditionOnLastAttempt() throws Exception {
        String waitForSeconds = "4";
        String testIntervalSeconds = "1";

        WaitAction testling = getWaitAction(waitForSeconds, testIntervalSeconds);

        reset(contextMock, conditionMock);
        prepareContextMock(waitForSeconds, testIntervalSeconds);
        expect(conditionMock.isSatisfied(contextMock)).andReturn(Boolean.FALSE).times(3);
        expect(conditionMock.isSatisfied(contextMock)).andReturn(Boolean.TRUE).once();
        replay(contextMock, conditionMock);

        startTimer();
        testling.doExecute(contextMock);
        stopTimer();

        verify(contextMock, conditionMock);
        assertConditionExecutedWithinSeconds(waitForSeconds);
    }

    @Test
    public void shouldSatisfyWaitConditionWithBiggerIntervalThanTimeout() throws Exception {
        String waitForSeconds = "1";
        String testIntervalSeconds = "10";

        WaitAction testling = getWaitAction(waitForSeconds, testIntervalSeconds);

        reset(contextMock, conditionMock);
        prepareContextMock(waitForSeconds, testIntervalSeconds);
        expect(conditionMock.isSatisfied(contextMock)).andReturn(Boolean.TRUE).once();
        replay(contextMock, conditionMock);

        startTimer();
        testling.doExecute(contextMock);
        stopTimer();

        verify(contextMock, conditionMock);
        assertConditionExecutedWithinSeconds(waitForSeconds);
    }

    @Test
    public void shouldNotSatisfyWaitCondition() throws Exception {
        String waitForSeconds = "3";
        String testIntervalSeconds = "1";

        WaitAction testling = getWaitAction(waitForSeconds, testIntervalSeconds);

        reset(contextMock, conditionMock);
        prepareContextMock(waitForSeconds, testIntervalSeconds);
        expect(conditionMock.isSatisfied(contextMock)).andReturn(Boolean.FALSE).times(3);
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
        assertConditionExecutedWithinSeconds(waitForSeconds);
    }


    @Test
    public void shouldNotSatisfyWaitConditionWithBiggerIntervalThanTimeout() throws Exception {
        String waitForSeconds = "1";
        String testIntervalSeconds = "10";

        WaitAction testling = getWaitAction(waitForSeconds, testIntervalSeconds);

        reset(contextMock, conditionMock);
        prepareContextMock(waitForSeconds, testIntervalSeconds);
        expect(conditionMock.isSatisfied(contextMock)).andReturn(Boolean.FALSE).once();
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
        assertConditionExecutedWithinSeconds(waitForSeconds);
    }

    private void prepareContextMock(String waitForSeconds, String testIntervalSeconds) {
        expect(contextMock.resolveDynamicValue(waitForSeconds)).andReturn(waitForSeconds).atLeastOnce();
        expect(contextMock.resolveDynamicValue(testIntervalSeconds)).andReturn(testIntervalSeconds).atLeastOnce();
    }

    private WaitAction getWaitAction(String waitForSeconds, String testIntervalSeconds) {
        WaitAction testling = new WaitAction();
        testling.setCondition(conditionMock);
        testling.setWaitForSeconds(waitForSeconds);
        testling.setTestIntervalSeconds(testIntervalSeconds);
        return testling;
    }

    private void assertConditionExecutedWithinSeconds(String seconds) {
        final long tolerance = 500L; // allow some tolerance in check
        final long secInMillisec = 1000L;
        final long totalExecutionTime = endTime - startTime;
        long permittedTime = (Integer.parseInt(seconds) * secInMillisec) + tolerance;
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