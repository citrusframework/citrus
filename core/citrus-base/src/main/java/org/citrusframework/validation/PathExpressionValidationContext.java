/*
 * Copyright the original author or authors.
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

package org.citrusframework.validation;

import java.util.HashMap;
import java.util.Map;

import org.citrusframework.builder.WithExpressions;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.validation.context.DefaultValidationContext;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.json.JsonPathMessageValidationContext;
import org.citrusframework.validation.xml.XpathMessageValidationContext;

public class PathExpressionValidationContext {

    /**
     * Prevent instantiation. Please use {@link Builder}
     */
    private PathExpressionValidationContext() {
        // prevent direct instantiation
    }

    /**
     * Fluent builder.
     */
    public static final class Builder
            implements ValidationContext.Builder<ValidationContext, Builder>, WithExpressions<Builder> {

        private Map<String, Object> expressions = new HashMap<>();

        /**
         * Static entry method for fluent builder API.
         * @return
         */
        public static Builder pathExpression() {
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

        public Builder jsonPath(final String expression, final Object value) {
            if (!JsonPathMessageValidationContext.isJsonPathExpression(expression)) {
                throw new CitrusRuntimeException(String.format("Unsupported json path expression '%s'", expression));
            }
            return expression(expression, value);
        }

        public Builder xpath(final String expression, final Object value) {
            if (!XpathMessageValidationContext.isXpathExpression(expression)) {
                throw new CitrusRuntimeException(String.format("Unsupported xpath expression '%s'", expression));
            }

            return expression(expression, value);
        }

        @Override
        public ValidationContext build() {
            if (expressions.isEmpty()) {
                return new DefaultValidationContext();
            }

            String expression = expressions.keySet().iterator().next();

            if (JsonPathMessageValidationContext.isJsonPathExpression(expression)) {
                return new JsonPathMessageValidationContext.Builder()
                        .expressions(expressions)
                        .build();
            } else if (XpathMessageValidationContext.isXpathExpression(expression)){
                return new XpathMessageValidationContext.Builder()
                        .expressions(expressions)
                        .build();
            } else {
                throw new CitrusRuntimeException(String.format("Unsupported path expression '%s'", expression));
            }
        }
    }
}
