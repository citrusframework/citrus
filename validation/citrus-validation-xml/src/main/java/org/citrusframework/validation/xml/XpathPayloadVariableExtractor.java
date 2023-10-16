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

package org.citrusframework.validation.xml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.xml.namespace.NamespaceContext;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.exceptions.UnknownElementException;
import org.citrusframework.message.DelegatingPathExpressionProcessor;
import org.citrusframework.message.Message;
import org.citrusframework.message.MessageProcessor;
import org.citrusframework.message.MessageProcessorAdapter;
import org.citrusframework.util.XMLUtils;
import org.citrusframework.validation.PathExpressionValidationContext;
import org.citrusframework.validation.ValidationContextAdapter;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.variable.VariableExtractor;
import org.citrusframework.xml.xpath.XPathExpressionResult;
import org.citrusframework.xml.xpath.XPathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Class reads message elements via XPath expressions and saves the text values as new test variables.
 * Implementation parsed the message payload as DOM document, so XML message payload is needed here.
 *
 * @author Christoph Deppisch
 */
public class XpathPayloadVariableExtractor implements VariableExtractor {
    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(XpathPayloadVariableExtractor.class);

    /** Map defines xpath expressions and target variable names */
    private final Map<String, Object> xPathExpressions;

    /** Namespace definitions used in xpath expressions */
    private final Map<String, String> namespaces;

    public XpathPayloadVariableExtractor() {
        this(new Builder());
    }

    /**
     * Constructor using fluent builder.
     * @param builder
     */
    private XpathPayloadVariableExtractor(Builder builder) {
        this.xPathExpressions = builder.expressions;
        this.namespaces = builder.namespaces;
    }

    /**
     * Extract variables using Xpath expressions.
     */
    public void extractVariables(Message message, TestContext context) {
        if (xPathExpressions == null || xPathExpressions.isEmpty()) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Reading XML elements with XPath");
        }

        NamespaceContext nsContext = context.getNamespaceContextBuilder().buildContext(message, namespaces);

        for (Entry<String, Object> entry : xPathExpressions.entrySet()) {
            String pathExpression = context.replaceDynamicContentInString(entry.getKey());
            String variableName = Optional.ofNullable(entry.getValue())
                    .map(Object::toString)
                    .orElseThrow(() -> new CitrusRuntimeException(String.format("Variable name must be set on " +
                            "extractor path expression '%s'", pathExpression)));

            if (logger.isDebugEnabled()) {
                logger.debug("Evaluating XPath expression: " + pathExpression);
            }

            Document doc = XMLUtils.parseMessagePayload(message.getPayload(String.class));

            if (XPathUtils.isXPathExpression(pathExpression)) {
                XPathExpressionResult resultType = XPathExpressionResult.fromString(pathExpression, XPathExpressionResult.STRING);

                Object value = XPathUtils.evaluate(doc, XPathExpressionResult.cutOffPrefix(pathExpression), nsContext, resultType);

                if (value == null) {
                    throw new CitrusRuntimeException("Not able to find value for expression: " + XPathExpressionResult.cutOffPrefix(pathExpression));
                }

                if (value instanceof List) {
                    value = ((List) value).stream().collect(Collectors.joining(","));
                }

                context.setVariable(variableName, value);
            } else {
                Node node = XMLUtils.findNodeByName(doc, pathExpression);

                if (node == null) {
                    throw new UnknownElementException("No element found for expression: " + XPathExpressionResult.cutOffPrefix(pathExpression));
                }

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    if (node.getFirstChild() != null) {
                        context.setVariable(variableName, node.getFirstChild().getNodeValue());
                    } else {
                        context.setVariable(variableName, "");
                    }
                } else {
                    context.setVariable(variableName, node.getNodeValue());
                }
            }
        }
    }

    /**
     * Fluent builder.
     */
    public static final class Builder implements VariableExtractor.Builder<XpathPayloadVariableExtractor, Builder>,
            XmlNamespaceAware, MessageProcessorAdapter, ValidationContextAdapter {
        private final Map<String, Object> expressions = new HashMap<>();
        private final Map<String, String> namespaces = new HashMap<>();

        public static Builder fromXpath() {
            return new Builder();
        }

        /**
         * Adds explicit namespace declaration for later path validation expressions.
         *
         * @param prefix
         * @param namespaceUri
         * @return
         */
        public Builder namespace(final String prefix, final String namespaceUri) {
            this.namespaces.put(prefix, namespaceUri);
            return this;
        }

        /**
         * Sets default namespace declarations on this action builder.
         *
         * @param namespaceMappings
         * @return
         */
        public Builder namespaces(final Map<String, String> namespaceMappings) {
            this.namespaces.putAll(namespaceMappings);
            return this;
        }

        @Override
        public Builder expressions(Map<String, Object> expressions) {
            this.expressions.putAll(expressions);
            return this;
        }

        @Override
        public Builder expression(final String expression, final Object variableName) {
            this.expressions.put(expression, variableName);
            return this;
        }

        @Override
        public void setNamespaces(Map<String, String> namespaces) {
            namespaces(namespaces);
        }

        @Override
        public MessageProcessor asProcessor() {
            return new DelegatingPathExpressionProcessor.Builder()
                    .expressions(expressions)
                    .build();
        }

        @Override
        public ValidationContext asValidationContext() {
            return new PathExpressionValidationContext.Builder()
                    .expressions(expressions)
                    .build();
        }

        @Override
        public XpathPayloadVariableExtractor build() {
            return new XpathPayloadVariableExtractor(this);
        }
    }

    /**
     * Gets the xPathExpressions.
     * @return the xPathExpressions
     */
    public Map<String, Object> getXpathExpressions() {
        return xPathExpressions;
    }

    /**
     * Gets the namespaces.
     * @return the namespaces
     */
    public Map<String, String> getNamespaces() {
        return namespaces;
    }
}
