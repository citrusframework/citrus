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

import com.consol.citrus.condition.Condition;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public abstract class WaitConditionBuilder<T extends Condition, S extends WaitConditionBuilder> {

    /** Parent wait action builder */
    private final WaitBuilder builder;

    /** Condition */
    private final T condition;

    /** Self reference */
    private S self;

    /**
     * Default constructor using fields.
     * @param condition
     * @param builder
     */
    public WaitConditionBuilder(T condition, WaitBuilder builder) {
        this.condition = condition;
        this.builder = builder;

        this.self = (S) this;
    }

    /**
     * The total length of seconds to wait on the condition to be satisfied
     * @param seconds
     * @return
     */
    public S seconds(String seconds) {
        builder.seconds(seconds);
        return self;
    }

    /**
     * The total length of seconds to wait on the condition to be satisfied
     * @param seconds
     * @return
     */
    public S seconds(Long seconds) {
        builder.seconds(seconds.toString());
        return self;
    }

    /**
     * The total length of milliseconds to wait on the condition to be satisfied
     * @param milliseconds
     * @return
     */
    public S ms(String milliseconds) {
        builder.milliseconds(milliseconds);
        return self;
    }

    /**
     * The total length of milliseconds to wait on the condition to be satisfied
     * @param milliseconds
     * @return
     */
    public S ms(Long milliseconds) {
        builder.milliseconds(String.valueOf(milliseconds));
        return self;
    }

    /**
     * The total length of milliseconds to wait on the condition to be satisfied
     * @param milliseconds
     * @return
     */
    public S milliseconds(String milliseconds) {
        builder.milliseconds(milliseconds);
        return self;
    }

    /**
     * The total length of milliseconds to wait on the condition to be satisfied
     * @param milliseconds
     * @return
     */
    public S milliseconds(Long milliseconds) {
        builder.milliseconds(String.valueOf(milliseconds));
        return self;
    }

    /**
     * The interval in seconds to use between each test of the condition
     * @param interval
     * @return
     */
    public S interval(String interval) {
        builder.interval(interval);
        return self;
    }

    /**
     * The interval in seconds to use between each test of the condition
     * @param interval
     * @return
     */
    public S interval(Long interval) {
        builder.interval(String.valueOf(interval));
        return self;
    }

    /**
     * Gets the condition.
     *
     * @return
     */
    public T getCondition() {
        return condition;
    }

    /**
     * Gets the builder.
     *
     * @return
     */
    public WaitBuilder getBuilder() {
        return builder;
    }
}
