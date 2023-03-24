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

package org.citrusframework.container;

import java.time.Duration;

import org.citrusframework.TestActionBuilder;
import org.citrusframework.condition.Condition;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public abstract class WaitConditionBuilder<T extends Condition, S extends WaitConditionBuilder<T, S>> implements TestActionBuilder<Wait> {

    /** Parent wait action builder */
    private final Wait.Builder<T> builder;

    /** Self reference */
    protected S self;

    /**
     * Default constructor using fields.
     * @param builder
     */
    public WaitConditionBuilder(Wait.Builder<T> builder) {
        this.builder = builder;
        this.self = (S) this;
    }

    @Override
    public Wait build() {
        return builder.build();
    }

    /**
     * The interval in milliseconds to use between each test of the condition
     * @param interval The interval to use
     * @return The altered WaitBuilder
     */
    public S interval(Long interval) {
        builder.interval(interval);
        return self;
    }

    /**
     * The interval in milliseconds to use between each test of the condition
     * @param interval The interval to use
     * @return The altered WaitBuilder
     */
    public S interval(String interval) {
        builder.interval(interval);
        return self;
    }

    public S milliseconds(long milliseconds) {
        builder.milliseconds(milliseconds);
        return self;
    }

    public S milliseconds(String milliseconds) {
        builder.milliseconds(milliseconds);
        return self;
    }

    public S seconds(double seconds) {
        builder.seconds(seconds);
        return self;
    }

    public S time(Duration duration) {
        builder.time(duration);
        return self;
    }

    /**
     * Gets the condition.
     * @return
     */
    public T getCondition() {
        return builder.getCondition();
    }

    /**
     * Gets the builder.
     * @return
     */
    public Wait.Builder<T> getBuilder() {
        return builder;
    }
}
