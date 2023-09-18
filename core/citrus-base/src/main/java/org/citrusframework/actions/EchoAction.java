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

import java.util.Date;

import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.context.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Prints messages to the console/logger during test execution.
 *
 * @author Christoph Deppisch
 * @since 2006
 */
public class EchoAction extends AbstractTestAction {

    /** Log message */
    private final String message;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(EchoAction.class);

    /**
     * Default constructor using the builder.
     * @param builder
     */
    private EchoAction(EchoAction.Builder builder) {
        super("echo", builder);

        this.message = builder.message;
    }

    @Override
    public void doExecute(TestContext context) {
        if (message == null) {
            logger.info("Citrus test " + new Date(System.currentTimeMillis()));
        } else {
            logger.info(context.getLogModifier().mask(context.replaceDynamicContentInString(message)));
        }
    }

    /**
     * Gets the message.
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractTestActionBuilder<EchoAction, Builder> {

        private String message;

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        public static Builder echo() {
            return new Builder();
        }

        /**
         * Fluent API action building entry method used in Java DSL.
         * @param message
         * @return
         */
        public static Builder echo(String message) {
            Builder builder = new Builder();
            builder.message(message);
            return builder;
        }

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        public static Builder print() {
            return new Builder();
        }

        /**
         * Fluent API action building entry method used in Java DSL.
         * @param message
         * @return
         */
        public static Builder print(String message) {
            Builder builder = new Builder();
            builder.message(message);
            return builder;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        @Override
        public EchoAction build() {
            return new EchoAction(this);
        }
    }
}
