/*
 * Copyright 2006-2010 the original author or authors.
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

package org.citrusframework.actions;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stop the test execution for a given amount of time.
 *
 * @author Christoph Deppisch
 * @since 2006
 */
public class SleepAction extends AbstractTestAction {

    /** Delay time */
    private final String time;
    private final TimeUnit timeUnit;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(SleepAction.class);

    /**
     * Default constructor.
     * @param builder
     */
    private SleepAction(Builder builder) {
        super("sleep", builder);

        this.time = builder.time;
        this.timeUnit = builder.timeUnit;
    }

    @Override
    public void doExecute(TestContext context) {
        String duration = context.resolveDynamicValue(time);

        try {
            logger.info(String.format("Sleeping %s %s", duration, timeUnit));

            if (duration.indexOf(".") > 0) {
                switch (timeUnit) {
                    case MILLISECONDS:
                        TimeUnit.MILLISECONDS.sleep(Math.round(Double.parseDouble(duration)));
                        break;
                    case SECONDS:
                        TimeUnit.MILLISECONDS.sleep(Math.round(Double.parseDouble(duration) * 1000));
                        break;
                    case MINUTES:
                        TimeUnit.MILLISECONDS.sleep(Math.round(Double.parseDouble(duration) * 60 * 1000));
                        break;
                    default:
                        throw new CitrusRuntimeException("Unsupported time expression for sleep action - " +
                                "please use one of milliseconds, seconds, minutes");
                }
            } else {
                timeUnit.sleep(Long.parseLong(duration));
            }

            logger.info(String.format("Returning after %s %s", duration, timeUnit));
        } catch (InterruptedException e) {
            throw new CitrusRuntimeException(e);
        }
    }

    /**
     * Gets the time expression.
     * @return the time expression
     */
    public String getTime() {
        return time;
    }

    /**
     * Obtains the timeUnit.
     * @return the time unit.
     */
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractTestActionBuilder<SleepAction, Builder> {

        private String time = "5000";
        private TimeUnit timeUnit = TimeUnit.MILLISECONDS;

        public static Builder delay() {
            return new Builder();
        }

        public static Builder sleep() {
            return new Builder();
        }

        public Builder milliseconds(Integer milliseconds) {
            return time(String.valueOf(milliseconds), TimeUnit.MILLISECONDS);
        }

        public Builder milliseconds(Long milliseconds) {
            return time(String.valueOf(milliseconds), TimeUnit.MILLISECONDS);
        }

        public Builder milliseconds(String expression) {
            time(expression, TimeUnit.MILLISECONDS);
            return this;
        }

        public Builder seconds(Double seconds) {
            milliseconds(Math.round(seconds * 1000));
            return this;
        }

        public Builder seconds(Integer seconds) {
            return time(String.valueOf(seconds  * 1000L), TimeUnit.MILLISECONDS);
        }

        public Builder seconds(Long seconds) {
            return time(String.valueOf(seconds  * 1000L), TimeUnit.MILLISECONDS);
        }


        public Builder time(Duration duration) {
            milliseconds(duration.toMillis());
            return this;
        }

        public Builder time(String expression, TimeUnit timeUnit) {
            time = expression;
            this.timeUnit = timeUnit;
            return this;
        }

        @Override
        public SleepAction build() {
            return new SleepAction(this);
        }

    }
}
