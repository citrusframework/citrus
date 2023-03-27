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

package org.citrusframework.citrus.validation.json;

import java.util.HashMap;
import java.util.Map;

import org.citrusframework.citrus.builder.WithExpressions;
import org.citrusframework.citrus.validation.context.DefaultValidationContext;
import org.citrusframework.citrus.validation.context.ValidationContext;
import org.springframework.util.StringUtils;

/**
 * Specialised validation context adds JSON path expressions for message validation.
 * @author Christoph Deppisch
 * @since 2.3
 */
public class JsonPathMessageValidationContext extends DefaultValidationContext {

    /** Map holding jsonPath expressions as key and expected values as values */
    private final Map<String, Object> jsonPathExpressions;

    /**
     * Constructor using fluent builder.
     * @param builder
     */
    public JsonPathMessageValidationContext(Builder builder) {
        this.jsonPathExpressions = builder.expressions;
    }

    /**
     * Default constructor.
     */
    public JsonPathMessageValidationContext() {
        this(new Builder());
    }

    /**
     * Fluent builder.
     */
    public static final class Builder
            implements ValidationContext.Builder<JsonPathMessageValidationContext, Builder>, WithExpressions<Builder> {

        private final Map<String, Object> expressions = new HashMap<>();

        /**
         * Static entry method for fluent builder API.
         * @return
         */
        public static Builder jsonPath() {
            return new Builder();
        }

        @Override
        public Builder expressions(Map<String, Object> expressions) {
            this.expressions.putAll(expressions);
            return this;
        }

        @Override
        public Builder expression(final String expression, final Object value) {
            this.expressions.put(expression, value);
            return this;
        }

        @Override
        public JsonPathMessageValidationContext build() {
            return new JsonPathMessageValidationContext(this);
        }
    }

    /**
     * Get the control message elements that have to be present in
     * the received message. Message element values are compared as well.
     * @return the jsonPathExpressions
     */
    public Map<String, Object> getJsonPathExpressions() {
        return jsonPathExpressions;
    }

    /**
     * Check whether given path expression is a JSONPath expression.
     * @param pathExpression
     * @return
     */
    public static boolean isJsonPathExpression(String pathExpression) {
        return StringUtils.hasText(pathExpression) && (pathExpression.startsWith("$"));
    }
}
