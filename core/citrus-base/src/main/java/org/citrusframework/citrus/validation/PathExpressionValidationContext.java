package org.citrusframework.citrus.validation;

import java.util.HashMap;
import java.util.Map;

import org.citrusframework.citrus.builder.WithExpressions;
import org.citrusframework.citrus.exceptions.CitrusRuntimeException;
import org.citrusframework.citrus.validation.context.DefaultValidationContext;
import org.citrusframework.citrus.validation.context.ValidationContext;
import org.citrusframework.citrus.validation.json.JsonPathMessageValidationContext;
import org.citrusframework.citrus.validation.xml.XpathMessageValidationContext;

/**
 * @author Christoph Deppisch
 */
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
