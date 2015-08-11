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

package com.consol.citrus.validation.xml;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.UnknownElementException;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.util.XMLUtils;
import com.consol.citrus.validation.AbstractMessageValidator;
import com.consol.citrus.validation.ValidationUtils;
import com.consol.citrus.validation.context.ValidationContext;
import com.consol.citrus.xml.namespace.NamespaceContextBuilder;
import com.consol.citrus.xml.xpath.XPathExpressionResult;
import com.consol.citrus.xml.xpath.XPathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.namespace.NamespaceContext;
import java.util.List;
import java.util.Map;

/**
 * Message validator evaluates set of XPath expressions on message payload and checks that values are as expected.
 * @author Christoph Deppisch
 * @since 2.3
 */
public class XpathMessageValidator extends AbstractMessageValidator<XpathMessageValidationContext> {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(XpathMessageValidator.class);

    @Autowired(required = false)
    private NamespaceContextBuilder namespaceContextBuilder = new NamespaceContextBuilder();

    @Override
    public void validateMessage(Message receivedMessage, TestContext context, XpathMessageValidationContext validationContext) throws ValidationException {
        if (CollectionUtils.isEmpty(validationContext.getXpathExpressions())) { return; }

        if (receivedMessage.getPayload() == null || !StringUtils.hasText(receivedMessage.getPayload(String.class))) {
            throw new ValidationException("Unable to validate message elements - receive message payload was empty");
        }

        log.info("Start XPath element validation");

        Document received = XMLUtils.parseMessagePayload(receivedMessage.getPayload(String.class));
        NamespaceContext namespaceContext = namespaceContextBuilder.buildContext(
                receivedMessage, validationContext.getNamespaces());

        for (Map.Entry<String, String> entry : validationContext.getXpathExpressions().entrySet()) {
            String xPathExpression = entry.getKey();
            String expectedValue = entry.getValue();
            String actualValue;

            xPathExpression = context.replaceDynamicContentInString(xPathExpression);

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

                actualValue = XPathUtils.evaluate(received,
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

                actualValue = getNodeValue(node);
            }
            //check if expected value is variable or function (and resolve it, if yes)
            expectedValue = context.replaceDynamicContentInString(expectedValue);

            //do the validation of actual and expected value for element
            ValidationUtils.validateValues(actualValue, expectedValue, xPathExpression, context);

            if (log.isDebugEnabled()) {
                log.debug("Validating element: " + xPathExpression + "='" + expectedValue + "': OK.");
            }
        }

        log.info("XPath element validation finished successfully: All elements OK");
    }

    @Override
    public XpathMessageValidationContext findValidationContext(List<ValidationContext> validationContexts) {
        for (ValidationContext validationContext : validationContexts) {
            if (validationContext instanceof XpathMessageValidationContext) {
                return (XpathMessageValidationContext) validationContext;
            }
        }

        return null;
    }

    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return messageType.equalsIgnoreCase(MessageType.XML.toString());
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
}
