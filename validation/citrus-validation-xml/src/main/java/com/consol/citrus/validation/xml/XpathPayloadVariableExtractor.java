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

package com.consol.citrus.validation.xml;

import javax.xml.namespace.NamespaceContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.UnknownElementException;
import com.consol.citrus.message.Message;
import com.consol.citrus.util.XMLUtils;
import com.consol.citrus.variable.VariableExtractor;
import com.consol.citrus.xml.xpath.XPathExpressionResult;
import com.consol.citrus.xml.xpath.XPathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Class reads message elements via XPath expressions and saves the text values as new test variables.
 * Implementation parsed the message payload as DOM document, so XML message payload is needed here.
 *
 * @author Christoph Deppisch
 */
public class XpathPayloadVariableExtractor implements VariableExtractor {

    /** Map defines xpath expressions and target variable names */
    private final Map<String, String> xPathExpressions;

    /** Namespace definitions used in xpath expressions */
    private final Map<String, String> namespaces;

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(XpathPayloadVariableExtractor.class);

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
        if (CollectionUtils.isEmpty(xPathExpressions)) {
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Reading XML elements with XPath");
        }

        NamespaceContext nsContext = context.getNamespaceContextBuilder().buildContext(message, namespaces);

        for (Entry<String, String> entry : xPathExpressions.entrySet()) {
            String pathExpression = context.replaceDynamicContentInString(entry.getKey());
            String variableName = entry.getValue();

            if (log.isDebugEnabled()) {
                log.debug("Evaluating XPath expression: " + pathExpression);
            }

            Document doc = XMLUtils.parseMessagePayload(message.getPayload(String.class));

            if (XPathUtils.isXPathExpression(pathExpression)) {
                XPathExpressionResult resultType = XPathExpressionResult.fromString(pathExpression, XPathExpressionResult.STRING);
                pathExpression = XPathExpressionResult.cutOffPrefix(pathExpression);

                Object value = XPathUtils.evaluate(doc, pathExpression, nsContext, resultType);

                if (value == null) {
                    throw new CitrusRuntimeException("Not able to find value for expression: " + pathExpression);
                }

                if (value instanceof List) {
                    value = ((List) value).stream().collect(Collectors.joining(","));
                }

                context.setVariable(variableName, value);
            } else {
                Node node = XMLUtils.findNodeByName(doc, pathExpression);

                if (node == null) {
                    throw new UnknownElementException("No element found for expression" + pathExpression);
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
    public static final class Builder implements VariableExtractor.Builder<XpathPayloadVariableExtractor, Builder>, XmlNamespaceAware {
        private final Map<String, String> expressions = new HashMap<>();
        private final Map<String, String> namespaces = new HashMap<>();

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
        public Builder expressions(Map<String, String> expressions) {
            this.expressions.putAll(expressions);
            return this;
        }

        @Override
        public Builder expression(final String expression, final String variableName) {
            this.expressions.put(expression, variableName);
            return this;
        }

        @Override
        public void setNamespaces(Map<String, String> namespaces) {
            namespaces(namespaces);
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
    public Map<String, String> getXpathExpressions() {
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
