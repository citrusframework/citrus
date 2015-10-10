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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * Pause the test execution until the condition is met or the wait time has been exceeded.
 *
 * @author Martin Maher
 * @since 2.4
 */
public class WaitAction extends AbstractTestAction {
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(WaitAction.class);

    private static final int SEC_IN_MILLISEC = 1000;

    public final static String DEFAULT_WAIT_TIME = "5";
    public final static String DEFAULT_INTERVAL = "1";

    /**
     * Condition to be met
     */
    private Condition condition;

    /**
     * The total time to wait in seconds, for the condition to be met before failing
     */
    private String waitForSeconds = DEFAULT_WAIT_TIME;

    /**
     * The time interval in seconds <<b>between</b> each test of the condition
     */
    private String testIntervalSeconds = DEFAULT_INTERVAL;

    /**
     * Default constructor.
     */
    public WaitAction() {
        setName("wait");
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public String getWaitForSeconds() {
        return waitForSeconds;
    }

    private long getWaitForMiliseconds(TestContext context) {
        return Integer.parseInt(context.resolveDynamicValue(waitForSeconds)) * SEC_IN_MILLISEC;
    }

    public void setWaitForSeconds(String waitForSeconds) {
        this.waitForSeconds = waitForSeconds;
    }

    public String getTestIntervalSeconds() {
        return testIntervalSeconds;
    }

    private long getTestIntervalMilieconds(TestContext context) {
        return Integer.parseInt(context.resolveDynamicValue(testIntervalSeconds)) * SEC_IN_MILLISEC;
    }

    public void setTestIntervalSeconds(String testIntervalSeconds) {
        this.testIntervalSeconds = testIntervalSeconds;
    }

    @Override
    public void doExecute(final TestContext context) {
        Boolean conditionSatisfied = null;
        long startTime = System.currentTimeMillis();
        long finishTime = startTime + getWaitForMiliseconds(context);
        long interval = getTestIntervalMilieconds(context);

        Callable<Boolean> callable = new Callable<Boolean>() {
            @Override
            public Boolean call() {
                return condition.isSatisfied(context);
            }
        };

        while (finishTime > System.currentTimeMillis()) {
            log.info(String.format("Testing condition %s", condition));
            long lastCheckStartTime = System.currentTimeMillis();
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Boolean> future = executor.submit(callable);
            try {
                long timeout = interval;
                if (lastCheckStartTime + timeout > finishTime) {
                    timeout = finishTime - lastCheckStartTime;
                }
                conditionSatisfied = future.get(timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                e.printStackTrace();
            }
            executor.shutdown();

            if (Boolean.TRUE.equals(conditionSatisfied)) {
                log.info(String.format("Condition %s satisfied", condition));
                return;
            }

            long lastCheckEndTime = System.currentTimeMillis();
            long sleepTime = lastCheckStartTime + interval - lastCheckEndTime;
            if (sleepTime > 0) {
                if(lastCheckEndTime + sleepTime > finishTime) {
                    sleepTime = finishTime - lastCheckEndTime;
                }
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        throw new CitrusRuntimeException(String.format("Condition %s NOT satisfied", condition));
    }
}
