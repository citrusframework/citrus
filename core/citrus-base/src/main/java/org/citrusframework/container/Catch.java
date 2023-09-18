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

package org.citrusframework.container;

import org.citrusframework.AbstractExceptionContainerBuilder;
import org.citrusframework.TestActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action catches possible exceptions in nested test actions.
 *
 * @author Christoph Deppisch
 */
public class Catch extends AbstractActionContainer {
    /** Exception type caught */
    private final String exception;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(Catch.class);

    /**
     * Default constructor.
     */
    public Catch(Builder builder) {
        super("catch", builder);

        this.exception = builder.exception;
    }

    @Override
    public void doExecute(TestContext context) {
        if (logger.isDebugEnabled()) {
            logger.debug("Catch container catching exceptions of type " + exception);
        }

        for (TestActionBuilder<?> actionBuilder: actions) {
            try {
                executeAction(actionBuilder.build(), context);
            } catch (Exception e) {
                if (exception != null && exception.equals(e.getClass().getName())) {
                    logger.info("Caught exception " + e.getClass() + ": " + e.getLocalizedMessage());
                    continue;
                }
                throw new CitrusRuntimeException(e);
            }
        }
    }

    /**
     * Gets the exception.
     * @return the exception
     */
    public String getException() {
        return exception;
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractExceptionContainerBuilder<Catch, Builder> {

        private String exception = CitrusRuntimeException.class.getName();

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        public static Builder catchException() {
            return new Builder();
        }

        /**
         * Catch exception type during execution.
         * @param exception
         * @return
         */
        public Builder exception(Class<? extends Throwable> exception) {
            this.exception = exception.getName();
            return this;
        }

        /**
         * Catch exception type during execution.
         * @param type
         */
        public Builder exception(String type) {
            this.exception = type;
            return this;
        }

        @Override
        public Catch doBuild() {
            return new Catch(this);
        }
    }
}
