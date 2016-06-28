/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.dsl.builder;

import com.consol.citrus.actions.WaitAction;
import com.consol.citrus.condition.Condition;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class WaitConditionBuilder {

    /** Condition */
    private final Condition condition;

    /** Wait test action */
    private final WaitAction action;

    /**
     * Default constructor using fields.
     * @param action
     * @param condition
     */
    public WaitConditionBuilder(WaitAction action, Condition condition) {
        this.action = action;
        this.condition = condition;
    }

    /**
     * The total length of seconds to wait on the condition to be satisfied
     * @param seconds
     * @return
     */
    public WaitConditionBuilder seconds(String seconds) {
        action.setSeconds(seconds);
        return this;
    }

    /**
     * The total length of seconds to wait on the condition to be satisfied
     * @param seconds
     * @return
     */
    public WaitConditionBuilder seconds(Long seconds) {
        action.setSeconds(seconds.toString());
        return this;
    }

    /**
     * The total length of milliseconds to wait on the condition to be satisfied
     * @param milliseconds
     * @return
     */
    public WaitConditionBuilder ms(String milliseconds) {
        action.setMilliseconds(milliseconds);
        return this;
    }

    /**
     * The total length of milliseconds to wait on the condition to be satisfied
     * @param milliseconds
     * @return
     */
    public WaitConditionBuilder ms(Long milliseconds) {
        action.setMilliseconds(String.valueOf(milliseconds));
        return this;
    }

    /**
     * The interval in seconds to use between each test of the condition
     * @param interval
     * @return
     */
    public WaitConditionBuilder interval(String interval) {
        action.setInterval(interval);
        return this;
    }

    /**
     * The interval in seconds to use between each test of the condition
     * @param interval
     * @return
     */
    public WaitConditionBuilder interval(Long interval) {
        action.setInterval(String.valueOf(interval));
        return this;
    }

}
