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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.namespace.NamespaceContext;

import org.citrusframework.XmlValidationHelper;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.UnknownElementException;
import org.citrusframework.exceptions.ValidationException;
import org.citrusframework.message.Message;
import org.citrusframework.util.StringUtils;
import org.citrusframework.util.XMLUtils;
import org.citrusframework.validation.AbstractMessageValidator;
import org.citrusframework.validation.ValidationUtils;
import org.citrusframework.validation.context.ValidationContext;
import org.citrusframework.xml.namespace.NamespaceContextBuilder;
import org.citrusframework.xml.xpath.XPathExpressionResult;
import org.citrusframework.xml.xpath.XPathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Message validator evaluates set of XPath expressions on message payload and checks that values are as expected.
 * @author Christoph Deppisch
 * @since 2.3
 */
public class XpathMessageValidator extends AbstractMessageValidator<XpathMessageValidationContext> {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(XpathMessageValidator.class);

    private NamespaceContextBuilder namespaceContextBuilder;

    @Override
    public void validateMessage(Message receivedMessage, Message controlMessage,
                                TestContext context, XpathMessageValidationContext validationContext) throws ValidationException {
        if (validationContext.getXpathExpressions().isEmpty()) {
            return;
        }

        if (receivedMessage.getPayload() == null || !StringUtils.hasText(receivedMessage.getPayload(String.class))) {
            throw new ValidationException("Unable to validate message elements - receive message payload was empty");
        }

        logger.debug("Start XPath element validation ...");

        Document received = XMLUtils.parseMessagePayload(receivedMessage.getPayload(String.class));
        NamespaceContext namespaceContext = getNamespaceContextBuilder(context)
                .buildContext(receivedMessage, validationContext.getNamespaces());

        for (Map.Entry<String, Object> entry : validationContext.getXpathExpressions().entrySet()) {
            String xPathExpression = entry.getKey();
            Object expectedValue = entry.getValue();

            xPathExpression = context.replaceDynamicContentInString(xPathExpression);

            Object xPathResult;
            if (XPathUtils.isXPathExpression(xPathExpression)) {
                XPathExpressionResult resultType = XPathExpressionResult.fromString(
                        xPathExpression, XPathExpressionResult.NODE);
                xPathExpression = XPathExpressionResult.cutOffPrefix(xPathExpression);

                //Give ignore elements the chance to prevent the validation in case result type is node
                if (resultType.equals(XPathExpressionResult.NODE) &&
                        XmlValidationUtils.isElementIgnored(XPathUtils.evaluateAsNode(received, xPathExpression, namespaceContext),
                                validationContext.getIgnoreExpressions(),
                                namespaceContext)) {
                    continue;
                }

                xPathResult = XPathUtils.evaluate(received,
                        xPathExpression,
                        namespaceContext,
                        resultType);
            } else {
                Node node = XMLUtils.findNodeByName(received, xPathExpression);

                if (node == null) {
                    throw new UnknownElementException(
                            "Element ' " + xPathExpression + "' could not be found in DOM tree");
                }

                if (XmlValidationUtils.isElementIgnored(node, validationContext.getIgnoreExpressions(), namespaceContext)) {
                    continue;
                }

                xPathResult = getNodeValue(node);
            }

            if (expectedValue instanceof String) {
                //check if expected value is variable or function (and resolve it, if yes)
                expectedValue = context.replaceDynamicContentInString(String.valueOf(expectedValue));
            }

            //do the validation of actual and expected value for element
            ValidationUtils.validateValues(xPathResult, expectedValue, xPathExpression, context);

            if (logger.isDebugEnabled()) {
                logger.debug("Validating element: " + xPathExpression + "='" + expectedValue + "': OK.");
            }
        }

        logger.info("XPath element validation successful: All elements OK");
    }

    @Override
    protected Class<XpathMessageValidationContext> getRequiredValidationContextType() {
        return XpathMessageValidationContext.class;
    }

    @Override
    public XpathMessageValidationContext findValidationContext(List<ValidationContext> validationContexts) {
        List<XpathMessageValidationContext> xpathMessageValidationContexts = validationContexts.stream()
                .filter(XpathMessageValidationContext.class::isInstance)
                .map(XpathMessageValidationContext.class::cast)
                .collect(Collectors.toList());

        if (xpathMessageValidationContexts.size() > 1) {
            XpathMessageValidationContext xpathMessageValidationContext = xpathMessageValidationContexts.get(0);

            // Collect all xpath expressions and combine into one single validation context
            Map<String, Object> xpathExpressions = xpathMessageValidationContexts.stream()
                    .map(XpathMessageValidationContext::getXpathExpressions)
                    .reduce((collect, map) -> {
                        collect.putAll(map);
                        return collect;
                    })
                    .orElseGet(Collections::emptyMap);

            if (!xpathExpressions.isEmpty()) {
                xpathMessageValidationContext.getXpathExpressions().putAll(xpathExpressions);
                return xpathMessageValidationContext;
            }
        }

        return super.findValidationContext(validationContexts);
    }

    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return new DomXmlMessageValidator().supportsMessageType(messageType, message);
    }

    /**
     * Resolves an XML node's value
     * @param node
     * @return node's string value
     */
    private String getNodeValue(Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE && node.getFirstChild() != null) {
            return node.getFirstChild().getNodeValue();
        } else {
            return node.getNodeValue();
        }
    }

    /**
     * Get explicit namespace context builder set on this class or obtain instance from reference resolver.
     * @param context
     * @return
     */
    private NamespaceContextBuilder getNamespaceContextBuilder(TestContext context) {
        if (namespaceContextBuilder != null) {
            return namespaceContextBuilder;
        }

        return XmlValidationHelper.getNamespaceContextBuilder(context);
    }

    /**
     * Sets the namespace context builder.
     * @param namespaceContextBuilder
     */
    public void setNamespaceContextBuilder(NamespaceContextBuilder namespaceContextBuilder) {
        this.namespaceContextBuilder = namespaceContextBuilder;
    }
}
