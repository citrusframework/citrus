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

package com.consol.citrus.dsl.builder;

import com.consol.citrus.container.Timer;
import com.consol.citrus.dsl.design.TestDesigner;
import com.consol.citrus.dsl.runner.TestRunner;

/**
 * @author Martin Maher
 * @since 2.5
 */
public class TimerBuilder extends AbstractTestContainerBuilder<Timer> {
    public TimerBuilder(TestRunner runner, Timer container) {
        super(runner, container);
    }

    public TimerBuilder(TestRunner runner) {
        this(runner, new Timer());
    }

    public TimerBuilder(TestDesigner designer, Timer container) {
        super(designer, container);
    }

    public TimerBuilder(TestDesigner designer) {
        this(designer, new Timer());
    }

    /**
     * Initial delay in milliseconds before first timer event should fire.
     *
     * @param delay
     */
    public TimerBuilder delay(long delay) {
        action.setDelay(delay);
        return this;
    }

    /**
     * Interval in milliseconds between each timer. As soon as the interval has elapsed the next timer event is fired.
     *
     * @param interval
     */
    public TimerBuilder interval(long interval) {
        action.setInterval(interval);
        return this;
    }

    /**
     * The maximum number of times the timer event is fired. Once this maximum number has been reached the timer is
     * stopped
     *
     * @param repeatCount
     */
    public TimerBuilder repeatCount(int repeatCount) {
        action.setRepeatCount(repeatCount);
        return this;
    }

    /**
     * Fork the timer so that other actions can run in parallel to the nested timer actions
     *
     * @param fork
     */
    public TimerBuilder fork(boolean fork) {
        action.setFork(fork);
        return this;
    }

    /**
     * Set the timer's id. This is useful when referencing the timer from other test actions like stop-timer
     *
     * @param timerId a unique timer id within the test context
     */
    public TimerBuilder timerId(String timerId) {
        action.setTimerId(timerId);
        return this;
    }

}
