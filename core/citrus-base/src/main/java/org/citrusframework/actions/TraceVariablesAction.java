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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.context.TestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action that prints variable values to the console/logger. Action requires a list of variable
 * names. Tries to find the variables in the test context and print its values.
 *
 * @author Christoph Deppisch
 * @since 2006
 */
public class TraceVariablesAction extends AbstractTestAction {
    /** List of variable names */
    private final List<String> variableNames;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(TraceVariablesAction.class);

    /**
     * Default constructor.
     */
    public TraceVariablesAction(Builder builder) {
        super("trace", builder);

        this.variableNames = builder.variableNames;
    }

    @Override
    public void doExecute(TestContext context) {
        logger.info("Trace variables");

        Iterator<String> it;
        if (variableNames != null && variableNames.size() > 0) {
            it = variableNames.iterator();
        } else {
            it = context.getVariables().keySet().iterator();
        }

        while (it.hasNext()) {
            String key = it.next();
            String value = context.getVariable(key);

            logger.info("Variable " + context.getLogModifier().mask(key + " = " + value));
        }
    }

    /**
     * Gets the variableNames.
     * @return the variableNames
     */
    public List<String> getVariableNames() {
        return variableNames;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractTestActionBuilder<TraceVariablesAction, Builder> {

        private final List<String> variableNames = new ArrayList<>();

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        public static Builder trace() {
            return new Builder();
        }

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        public static Builder traceVariables() {
            return new Builder();
        }

        /**
         * Fluent API action building entry method used in Java DSL.
         * @param variableNames
         * @return
         */
        public static Builder traceVariables(String... variableNames) {
            Builder builder = new Builder();
            builder.variables(variableNames);
            return builder;
        }

        /**
         * Fluent API action building entry method used in Java DSL.
         * @param variable
         * @return
         */
        public static Builder traceVariables(String variable) {
            Builder builder = new Builder();
            builder.variable(variable);
            return builder;
        }

        public Builder variable(String variable) {
            this.variableNames.add(variable);
            return this;
        }

        public Builder variables(String... variables) {
            Stream.of(variables).forEach(this::variable);
            return this;
        }

        @Override
        public TraceVariablesAction build() {
            return new TraceVariablesAction(this);
        }
    }
}
