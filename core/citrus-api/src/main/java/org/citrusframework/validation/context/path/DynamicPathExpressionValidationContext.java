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

package org.citrusframework.validation.context.path;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.citrusframework.context.TestContext;
import org.citrusframework.message.DelegatingPathExpressionProcessor;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.util.IsJsonPathPredicate;
import org.citrusframework.validation.DelegatingPayloadVariableExtractor;
import org.citrusframework.validation.context.DefaultValidationContext;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.validation.context.json.JsonMessageValidationContext;
import org.citrusframework.validation.context.json.JsonPathMessageValidationContext;
import org.citrusframework.validation.context.xml.XpathMessageValidationContext;
import org.citrusframework.validation.expression.PathExpressionValidationContextBuilder;
import org.citrusframework.variable.VariableExtractor;

/**
 * Specialised validation context holding path expressions that reference dynamic variables
 * (e.g. <code>${variableName}</code>).
 * <p>
 * The type of such a path expression (JSONPath or XPath) is unknown when the test is being
 * constructed, because the variable is only resolved at runtime. This context resolves the
 * expressions at runtime and delegates the validation to the matching JSONPath or XPath
 * validation context.
 * <p>
 * The context may hold a mix of dynamic and static path expressions. As soon as a single
 * expression references a variable all expressions are routed to this context. Static
 * expressions are not affected by the variable resolution and are classified as JSONPath or
 * XPath in exactly the same way as the dynamic ones.
 */
public class DynamicPathExpressionValidationContext extends DefaultValidationContext {

    /** Map holding path expressions as key and expected values as values */
    private final Map<String, Object> expressions;

    /** Namespace definitions used to resolve prefixes in XPath expressions */
    private final Map<String, String> namespaceContext;

    /**
     * Constructor using fluent builder.
     * @param builder
     */
    public DynamicPathExpressionValidationContext(Builder builder) {
        this.expressions = builder.expressions;
        this.namespaceContext = builder.namespaceContext;
    }

    /**
     * Default constructor.
     */
    public DynamicPathExpressionValidationContext() {
        this(new Builder());
    }

    @Override
    public boolean requiresValidator() {
        return true;
    }

    @Override
    public Optional<String> getCorrespondingValidationModule() {
        return Optional.empty();
    }

    /**
     * Resolves the dynamic path expressions and creates the matching validation contexts.
     * A path expression that resolves to a JSONPath expression is added to a
     * {@link JsonPathMessageValidationContext}, all other expressions are added to a
     * {@link XpathMessageValidationContext}.
     * @param context the test context used to resolve the dynamic content
     * @return the list of validation contexts handling the resolved path expressions
     */
    public List<ValidationContext> resolve(TestContext context) {
        Map<String, Object> jsonPathExpressions = new LinkedHashMap<>();
        Map<String, Object> xpathExpressions = new LinkedHashMap<>();

        for (Map.Entry<String, Object> expression : expressions.entrySet()) {
            String path = context.replaceDynamicContentInString(expression.getKey());

            if (IsJsonPathPredicate.getInstance().test(path)) {
                jsonPathExpressions.put(path, expression.getValue());
            } else {
                xpathExpressions.put(path, expression.getValue());
            }
        }

        List<ValidationContext> validationContexts = new ArrayList<>();

        if (!jsonPathExpressions.isEmpty()) {
            // the message validation context is required to compare the control message body
            // with the received message, just like for an explicit JSONPath validation
            validationContexts.add(new JsonMessageValidationContext.Builder().build());
            validationContexts.add(new JsonPathMessageValidationContext.Builder()
                    .expressions(jsonPathExpressions)
                    .build());
        }

        if (!xpathExpressions.isEmpty()) {
            XpathMessageValidationContext.Builder builder = new XpathMessageValidationContext.Builder()
                    .expressions(xpathExpressions);

            if (!namespaceContext.isEmpty()) {
                builder.namespaceContext(namespaceContext);
            }

            validationContexts.add(builder.build());
        }

        return validationContexts;
    }

    /**
     * Get the path expressions that reference dynamic variables.
     * @return the expressions
     */
    public Map<String, Object> getExpressions() {
        return expressions;
    }

    /**
     * Get the namespace definitions used to resolve prefixes in XPath expressions.
     * @return the namespace context
     */
    public Map<String, String> getNamespaceContext() {
        return namespaceContext;
    }

    /**
     * Fluent builder.
     */
    public static final class Builder implements PathExpressionValidationContextBuilder<DynamicPathExpressionValidationContext, Builder> {

        private final Map<String, Object> expressions = new HashMap<>();
        private final Map<String, String> namespaceContext = new HashMap<>();

        /**
         * Static entry method for fluent builder API.
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

        @Override
        public Builder jsonPath(final String expression, final Object value) {
            return expression(expression, value);
        }

        @Override
        public Builder xpath(final String expression, final Object value) {
            return expression(expression, value);
        }

        /**
         * Set the namespace definitions used to resolve prefixes in XPath expressions.
         * @param namespaceContext
         */
        public Builder namespaceContext(Map<String, String> namespaceContext) {
            this.namespaceContext.putAll(namespaceContext);
            return this;
        }

        @Override
        public MessageProcessor asProcessor() {
            return new DelegatingPathExpressionProcessor.Builder()
                    .expressions(expressions)
                    .build();
        }

        @Override
        public VariableExtractor asExtractor() {
            return new DelegatingPayloadVariableExtractor.Builder()
                    .expressions(expressions)
                    .build();
        }

        @Override
        public DynamicPathExpressionValidationContext build() {
            return new DynamicPathExpressionValidationContext(this);
        }
    }
}
