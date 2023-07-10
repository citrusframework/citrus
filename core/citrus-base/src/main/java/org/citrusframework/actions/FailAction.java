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

/**
 * Action fails the test explicitly. User can specify a cause message.
 *
 * @author Christoph Deppisch
 */
public class FailAction extends AbstractTestAction {
    /** User defined cause message to explain the error */
    private final String message;

    /**
     * Default constructor.
     */
    public FailAction(Builder builder) {
        super("fail", builder);

        this.message = builder.message;
    }

    @Override
    public void doExecute(TestContext context) {
        throw new CitrusRuntimeException(context.replaceDynamicContentInString(message));
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
    public static final class Builder extends AbstractTestActionBuilder<FailAction, FailAction.Builder> {

        private String message = "Generated error to interrupt test execution";

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        public static FailAction.Builder fail() {
            return new Builder();
        }

        /**
         * Fluent API action building entry method used in Java DSL.
         * @param message
         * @return
         */
        public static FailAction.Builder fail(String message) {
            FailAction.Builder builder = new FailAction.Builder();
            builder.message(message);
            return builder;
        }

        public FailAction.Builder message(String message) {
            this.message = message;
            return this;
        }

        @Override
        public FailAction build() {
            return new FailAction(this);
        }
    }
}
