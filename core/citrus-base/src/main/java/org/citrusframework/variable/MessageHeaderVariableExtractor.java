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

package org.citrusframework.variable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.UnknownElementException;
import org.citrusframework.message.Message;

/**
 * Variable extractor reading message headers and saves them to new test variables.
 *
 * @author Christoph Deppisch
 */
public class MessageHeaderVariableExtractor implements VariableExtractor {

    /** Map holding header names and target variable names */
    private final Map<String, Object> headerMappings;

    public MessageHeaderVariableExtractor() {
        this(new Builder());
    }

    /**
     * Constructor using fluent builder.
     * @param builder
     */
    private MessageHeaderVariableExtractor(Builder builder) {
        this.headerMappings = builder.expressions;
    }

    /**
     * Reads header information and saves new test variables.
     */
    public void extractVariables(Message message, TestContext context) {
        if (headerMappings.isEmpty()) { return; }

        for (Entry<String, Object> entry : headerMappings.entrySet()) {
            String headerElementName = entry.getKey();
            String targetVariableName = Optional.ofNullable(entry.getValue())
                    .map(Object::toString)
                    .orElseThrow(() -> new CitrusRuntimeException(String.format("Variable name must be set for extractor " +
                            "on header '%s'", headerElementName)));

            if (message.getHeader(headerElementName) == null) {
                throw new UnknownElementException("Could not find header element " + headerElementName + " in received header");
            }

            context.setVariable(targetVariableName, message.getHeader(headerElementName).toString());
        }
    }

    /**
     * Fluent builder.
     */
    public static final class Builder implements VariableExtractor.Builder<MessageHeaderVariableExtractor, Builder> {

        private final Map<String, Object> expressions = new LinkedHashMap<>();

        /**
         * Static entry method for builder.
         * @return
         */
        public static Builder fromHeaders() {
            return new Builder();
        }

        /**
         * Evaluate all header name expressions and store values as new variables to the test context.
         * @param expressions
         * @return
         */
        public Builder headers(Map<String, String> expressions) {
            this.expressions.putAll(expressions);
            return this;
        }

        /**
         * Reads header by its name and stores value as new variable to the test context.
         * @param headerName
         * @param variableName
         * @return
         */
        public Builder header(final String headerName, final String variableName) {
            this.expressions.put(headerName, variableName);
            return this;
        }

        @Override
        public Builder expressions(Map<String, Object> expressions) {
            this.expressions.putAll(expressions);
            return this;
        }

        @Override
        public Builder expression(final String headerName, final Object variableName) {
            this.expressions.put(headerName, variableName);
            return this;
        }

        @Override
        public MessageHeaderVariableExtractor build() {
            return new MessageHeaderVariableExtractor(this);
        }
    }

    /**
     * Gets the headerMappings.
     * @return the headerMappings
     */
    public Map<String, Object> getHeaderMappings() {
        return headerMappings;
    }
}
