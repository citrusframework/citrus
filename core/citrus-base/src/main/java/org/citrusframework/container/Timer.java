/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.container;

import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.citrusframework.AbstractTestContainerBuilder;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

/**
 * @since 2.5
 */
public class Timer extends AbstractActionContainer implements StopTimer {
    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(Timer.class);

    private final static AtomicInteger nextSerialNumber = new AtomicInteger(0);

    protected static final String INDEX_SUFFIX = "-index";

    private final long interval;
    private final long delay;
    private final int repeatCount;
    private final boolean fork;
    private final boolean autoStop;
    private final String timerId;

    protected AtomicBoolean timerComplete = new AtomicBoolean(false);
    protected CitrusRuntimeException timerException = null;
    private java.util.Timer timer;

    private ExecutorService taskExecutor;

    public Timer(Builder builder) {
        super("timer", builder);

        this.interval = builder.interval;
        this.delay = builder.delay;
        this.repeatCount = builder.repeatCount;
        this.fork = builder.fork;
        this.autoStop = builder.autoStop;
        this.timerId = builder.timerId;
    }

    @Override
    public void doExecute(final TestContext context) {
        timer = new java.util.Timer(getTimerId(), false);

        if (fork) {
            taskExecutor = newSingleThreadExecutor();
            taskExecutor.execute(() -> configureAndRunTimer(context));
        } else {
            configureAndRunTimer(context);
        }
    }

    private void configureAndRunTimer(final TestContext context) {
        context.registerTimer(getTimerId(), this);

        TimerTask timerTask = new TimerTask() {
            int indexCount = 0;

            @Override
            public void run() {
                try {
                    indexCount++;
                    updateIndexCountInTestContext(context);
                    logger.debug("Timer event fired #{} - executing nested actions", indexCount);

                    for (TestActionBuilder<?> actionBuilder : actions)  {
                        executeAction(actionBuilder.build(), context);
                    }
                    if (indexCount >= repeatCount) {
                        logger.debug("Timer complete: {} iterations reached", repeatCount);
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
                logger.error("Timer stopped as a result of nested action error ({})", e.getMessage());
                stopTimer();

                if (fork) {
                    context.addException(timerException);
                }
            }
        };
        timer.scheduleAtFixedRate(timerTask, delay, interval);

        while (!timerComplete.get()) {
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                logger.warn("Interrupted while waiting for timer to complete", e);
            }
        }

        if (timerException != null) {
            throw timerException;
        }
    }

    public String getTimerId() {
        return timerId;
    }

    @Override
    public void stopTimer() {
        if (timerComplete.compareAndSet(false, true)) {
            if (fork && taskExecutor != null) {
                taskExecutor.shutdown();
            }

            if (timer != null) {
                timer.cancel();
            }
        }
    }

    private static int serialNumber() {
        return nextSerialNumber.getAndIncrement();
    }

    public long getInterval() {
        return interval;
    }

    public long getDelay() {
        return delay;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public boolean isFork() {
        return fork;
    }

    @Override
    public boolean isDone(TestContext context) {
        if (fork) {
            if (autoStop) {
                stopTimer();
            } else {
                // forked timers that should not be stopped automatically are always marked as done
                return true;
            }
        }

        return super.isDone(context);
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractTestContainerBuilder<Timer, Builder> {

        private long interval = 1000L;
        private long delay = 0L;
        private int repeatCount = Integer.MAX_VALUE;
        private boolean fork = false;
        private boolean autoStop = true;
        private String timerId;

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        public static Builder timer() {
            return new Builder();
        }

        /**
         * Initial delay in milliseconds before first timer event should fire.
         *
         * @param delay
         */
        public Builder delay(long delay) {
            this.delay = delay;
            return this;
        }

        /**
         * Interval in milliseconds between each timer. As soon as the interval has elapsed the next timer event is fired.
         *
         * @param interval
         */
        public Builder interval(long interval) {
            this.interval = interval;
            return this;
        }

        /**
         * The maximum number of times the timer event is fired. Once this maximum number has been reached the timer is
         * stopped
         *
         * @param repeatCount
         */
        public Builder repeatCount(int repeatCount) {
            this.repeatCount = repeatCount;
            return this;
        }

        /**
         * Fork the timer so that other actions can run in parallel to the nested timer actions
         *
         * @param fork
         */
        public Builder fork(boolean fork) {
            this.fork = fork;
            return this;
        }

        /**
         * Automatically stop the timer when test is finished.
         *
         * @param autoStop
         */
        public Builder autoStop(boolean autoStop) {
            this.autoStop = autoStop;
            return this;
        }

        /**
         * Set the timer's id. This is useful when referencing the timer from other test actions like stop-timer
         *
         * @param timerId a unique timer id within the test context
         */
        public Builder id(String timerId) {
            this.timerId = timerId;
            return this;
        }

        /**
         * Set the timer's id. This is useful when referencing the timer from other test actions like stop-timer
         *
         * @param timerId a unique timer id within the test context
         */
        public Builder timerId(String timerId) {
            this.timerId = timerId;
            return this;
        }

        @Override
        public Timer doBuild() {
            if (StringUtils.isEmpty(timerId)) {
                timerId = "citrus-timer-" + serialNumber();
            }

            return new Timer(this);
        }
    }
}
