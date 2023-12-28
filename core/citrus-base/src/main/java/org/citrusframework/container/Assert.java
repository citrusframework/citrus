/*
 * Copyright 2006-2024 the original author or authors.
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

import org.citrusframework.AbstractExceptionContainerBuilder;
import org.citrusframework.TestAction;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

import static java.lang.String.format;
import static org.citrusframework.validation.matcher.ValidationMatcherUtils.isValidationMatcherExpression;
import static org.citrusframework.validation.matcher.ValidationMatcherUtils.resolveValidationMatcher;

/**
 * Assert exception to happen in nested test action.
 *
 * @author Christoph Deppisch
 * @since 2009
 */
public class Assert extends AbstractActionContainer {

    /**
     * Nested test action
     */
    private final TestActionBuilder<?> action;

    /**
     * Asserted exception
     */
    private final Class<? extends Throwable> exception;

    /**
     * Localized exception message for control
     */
    private final String message;

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(Assert.class);

    /**
     * Default constructor.
     */
    public Assert(Builder builder) {
        super("assert", builder);

        this.action = builder.action;
        this.exception = builder.exception;
        this.message = builder.message;
    }

    @Override
    public void doExecute(TestContext context) {
        if (logger.isDebugEnabled()) {
            logger.debug("Assert container asserting exceptions of type {}", exception.getSimpleName());
        }

        try {
            executeAction(this.action.build(), context);
        } catch (Exception e) {
            logger.debug("Validating caught exception: {}", e.getMessage());

            if (!exception.isAssignableFrom(e.getClass())) {
                throw new ValidationException("Validation failed for asserted exception type - expected: '" +
                        exception + "' but was: '" + e.getClass().getName() + "'", e);
            }

            if (message != null) {
                if (isValidationMatcherExpression(message)) {
                    resolveValidationMatcher("message", e.getLocalizedMessage(), message, context);
                } else if (!context.replaceDynamicContentInString(message).equals(e.getLocalizedMessage())) {
                    throw new ValidationException("Validation failed for asserted exception message - expected: '" +
                            message + "' but was: '" + e.getLocalizedMessage() + "'", e);
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Asserted exception is as expected: " + e.getClass() + ": " + e.getLocalizedMessage());
            }

            logger.info("Assert exception validation successful: All values OK");

            return;
        }

        throw new ValidationException("Missing asserted exception '" + exception + "'");
    }

    /**
     * Gets the action.
     *
     * @return the action
     */
    public TestAction getAction() {
        return action.build();
    }

    /**
     * Get the message to send.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets the exception.
     *
     * @return the exception
     */
    public Class<? extends Throwable> getException() {
        return exception;
    }

    @Override
    public TestAction getTestAction(int index) {
        return getAction();
    }

    @Override
    public List<TestAction> getActions() {
        return Collections.singletonList(getAction());
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractExceptionContainerBuilder<Assert, Builder> {

        private TestActionBuilder<?> action;
        private Class<? extends Throwable> exception = CitrusRuntimeException.class;
        private String message;

        /**
         * Fluent API action building entry method used in Java DSL.
         *
         * @return
         */
        public static Builder assertException() {
            return new Builder();
        }

        @Override
        public Builder actions(TestActionBuilder<?>... actions) {
            this.action = actions[0];
            return super.actions(actions[0]);
        }

        /**
         * Catch exception type during execution.
         *
         * @param exception
         * @return
         */
        public Builder exception(Class<? extends Throwable> exception) {
            this.exception = exception;
            return this;
        }

        /**
         * Catch exception type during execution.
         *
         * @param type
         * @return
         */
        public Builder exception(String type) {
            try {
                this.exception = (Class<? extends Throwable>) Class.forName(type);
            } catch (ClassNotFoundException e) {
                throw new CitrusRuntimeException(format("Failed to instantiate exception class of type '%s'", type), e);
            }
            return this;
        }

        /**
         * Expect error message in exception.
         *
         * @param message
         */
        public Builder message(String message) {
            this.message = message;
            return this;
        }

        /**
         * Sets the test action to execute during assert.
         *
         * @param action
         */
        public Builder action(TestAction action) {
            return action(() -> action);
        }

        /**
         * Sets the test action to execute during assert.
         */
        public Builder action(TestActionBuilder<?> builder) {
            return actions(builder);
        }

        @Override
        public Assert doBuild() {
            return new Assert(this);
        }
    }
}
