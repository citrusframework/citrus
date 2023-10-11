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

package org.citrusframework.validation.xml;

import java.util.HashMap;
import java.util.Map;

import org.citrusframework.builder.WithExpressions;
import org.citrusframework.message.DelegatingPathExpressionProcessor;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.message.MessageProcessorAdapter;
import org.citrusframework.validation.DelegatingPayloadVariableExtractor;
import org.citrusframework.variable.VariableExtractor;
import org.citrusframework.variable.VariableExtractorAdapter;
import org.citrusframework.util.StringUtils;

/**
 * Specialised Xml validation context adds XPath expression evaluation.
 * @author Christoph Deppisch
 * @since 2.3
 */
public class XpathMessageValidationContext extends XmlMessageValidationContext {

    /** Map holding xpath expressions as key and expected values as values */
    private final Map<String, Object> xPathExpressions;

    /**
     * Default constructor.
     */
    public XpathMessageValidationContext() {
        this(new Builder());
    }

    /**
     * Constructor using fluent builder.
     * @param builder
     */
    public XpathMessageValidationContext(Builder builder) {
        super(builder);
        this.xPathExpressions = builder.expressions;
    }

    /**
     * Fluent builder.
     */
    public static final class Builder extends XmlValidationContextBuilder<XpathMessageValidationContext, Builder>
            implements WithExpressions<Builder>, VariableExtractorAdapter, MessageProcessorAdapter {

        private final Map<String, Object> expressions = new HashMap<>();

        /**
         * Static entry method for fluent builder API.
         * @return
         */
        public static Builder xpath() {
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
        public XpathMessageValidationContext build() {
            return new XpathMessageValidationContext(this);
        }
    }

    /**
     * Get the control message elements that have to be present in
     * the received message. Message element values are compared as well.
     * @return the xPathExpressions
     */
    public Map<String, Object> getXpathExpressions() {
        return xPathExpressions;
    }

    /**
     * Check whether given path expression is a JSONPath expression.
     * @param pathExpression
     * @return
     */
    public static boolean isXpathExpression(String pathExpression) {
        return StringUtils.hasText(pathExpression) && (pathExpression.startsWith("/"));
    }
}
