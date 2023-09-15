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

import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action used for time measurement during test. User can define a time line that is followed
 * during the test case. Action can print out the watched time to the console/logger.
 *
 * @author Christoph Deppisch
 * @since 2006
 */
public class StopTimeAction extends AbstractTestAction {

    public static final String DEFAULT_TIMELINE_ID = "CITRUS_TIMELINE";
    public static final String DEFAULT_TIMELINE_VALUE_SUFFIX = "_VALUE";

    /** Current time line id */
    private final String id;
    private final String suffix;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(StopTimeAction.class);

    /**
     * Default constructor.
     */
    public StopTimeAction(Builder builder) {
        super("stop-time", builder);

        this.id = builder.id;
        this.suffix = builder.suffix;
    }

    @Override
    public void doExecute(TestContext context) {
        String timeLineId = context.replaceDynamicContentInString(id);
        String timeLineSuffix = context.replaceDynamicContentInString(suffix);

        try {
            if (context.getVariables().containsKey(timeLineId)) {
                long time = System.currentTimeMillis() - context.getVariable(timeLineId, Long.class);
                context.setVariable(timeLineId + timeLineSuffix, time);

                if (description != null) {
                    logger.info("TimeWatcher " + timeLineId + " after " + time + " ms (" + description + ")");
                } else {
                    logger.info("TimeWatcher " + timeLineId + " after " + time + " ms");
                }
            } else {
                logger.info("Starting TimeWatcher: " + timeLineId);
                context.setVariable(timeLineId, System.currentTimeMillis());
                context.setVariable(timeLineId + timeLineSuffix, 0L);
            }
        } catch (Exception e) {
            throw new CitrusRuntimeException(e);
        }
    }

    /**
     * Gets the id.
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the suffix.
     * @return
     */
    public String getSuffix() {
        return suffix;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractTestActionBuilder<StopTimeAction, Builder> {

        private String id = DEFAULT_TIMELINE_ID;
        private String suffix = DEFAULT_TIMELINE_VALUE_SUFFIX;

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        public static Builder stopTime() {
            return new Builder();
        }

        /**
         * Fluent API action building entry method used in Java DSL.
         * @param id
         * @return
         */
        public static Builder stopTime(String id) {
            Builder builder = new Builder();
            builder.id(id);
            return builder;
        }

        /**
         * Fluent API action building entry method used in Java DSL.
         * @param id
         * @param suffix
         * @return
         */
        public static Builder stopTime(String id, String suffix) {
            Builder builder = new Builder();
            builder.id(id);
            builder.suffix(suffix);
            return builder;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder suffix(String suffix) {
            this.suffix = suffix;
            return this;
        }

        @Override
        public StopTimeAction build() {
            return new StopTimeAction(this);
        }
    }
}
