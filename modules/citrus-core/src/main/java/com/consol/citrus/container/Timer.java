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

package com.consol.citrus.container;

import com.consol.citrus.TestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.util.StringUtils;

import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Martin Maher
 * @since 2.5
 */
public class Timer extends AbstractActionContainer implements StopTimer {
    /** Logger */
    private static Logger log = LoggerFactory.getLogger(Timer.class);

    private final static AtomicInteger nextSerialNumber = new AtomicInteger(0);

    protected static final String INDEX_SUFFIX = "-index";

    private long interval = 1000L;
    private long delay = 0L;
    private int repeatCount = Integer.MAX_VALUE;
    private boolean fork = false;
    private String timerId;

    protected boolean timerComplete = false;
    protected CitrusRuntimeException timerException = null;
    private java.util.Timer timer;

    public Timer() {
        setName("timer");
    }

    @Override
    public void doExecute(final TestContext context) {
        if (fork) {
            SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
            taskExecutor.execute(new Runnable() {
                public void run() {
                    configureAndRunTimer(context);
                }
            });
        } else {
            configureAndRunTimer(context);
        }
    }

    private void configureAndRunTimer(final TestContext context) {
        timer = new java.util.Timer(getTimerId(), false);

        context.registerTimer(getTimerId(), this);

        TimerTask timerTask = new TimerTask() {
            int indexCount = 0;

            @Override
            public void run() {
                try {
                    indexCount++;
                    updateIndexCountInTestContext(context);
                    log.debug(String.format("Timer event fired #%s - executing nested actions", indexCount));

                    for (TestAction action : actions) {
                        setActiveAction(action);
                        action.execute(context);
                    }
                    if (indexCount >= repeatCount) {
                        log.debug(String.format("Timer complete: %s iterations reached", repeatCount));
                        stopTimer();
                    }
                } catch (Exception e) {
                    handleException(e);
                }
            }

            private void updateIndexCountInTestContext(TestContext context) {
                context.setVariable(getTimerId() + INDEX_SUFFIX, String.valueOf(indexCount));
            }

            private void handleException(Exception e) {
                if (e instanceof CitrusRuntimeException) {
                    timerException = (CitrusRuntimeException) e;
                } else {
                    timerException = new CitrusRuntimeException(e);
                }
                log.error(String.format("Timer stopped as a result of nested action error (%s)", e.getMessage()));
                stopTimer();

                if (fork) {
                    context.addException(timerException);
                }
            }
        };
        timer.scheduleAtFixedRate(timerTask, delay, interval);

        while (!timerComplete) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                log.warn("Interrupted while waiting for timer to complete", e);
            }
        }

        if (timerException != null) {
            throw timerException;
        }
    }

    public String getTimerId() {
        if (StringUtils.isEmpty(timerId)) {
            timerId = "citrus-timer-" + serialNumber();
        }
        return timerId;
    }

    @Override
    public void stopTimer() {
        timer.cancel();
        timerComplete = true;
    }

    private static int serialNumber() {
        return nextSerialNumber.getAndIncrement();
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public void setTimerId(String timerId) {
        this.timerId = timerId;
    }

    public boolean isFork() {
        return fork;
    }

    public void setFork(boolean fork) {
        this.fork = fork;
    }
}
