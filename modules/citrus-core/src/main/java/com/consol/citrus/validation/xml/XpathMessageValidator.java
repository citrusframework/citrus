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
import com.consol.citrus.context.XpathAssertionResult;
import com.consol.citrus.exceptions.UnknownElementException;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.message.Message;
import com.consol.citrus.util.XMLUtils;
import com.consol.citrus.validation.AbstractMessageValidator;
import com.consol.citrus.validation.ValidationUtils;
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
import java.util.Map;

/**
 * Message validator evaluates set of XPath expressions on message payload and checks that values are as expected.
 * @author Christoph Deppisch
 * @since 2.3
 */
public class XpathMessageValidator extends AbstractMessageValidator<XpathMessageValidationContext> {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(XpathMessageValidator.class);

    private static boolean suppressExceptionsOnXpathEvaluations;

    static {
        suppressExceptionsOnXpathEvaluations = Boolean.parseBoolean(System.getProperty("citrus.core.validation.xml.suppress_exceptions_on_xpath_evaluations", "false"));
    }

    @Autowired(required = false)
    private NamespaceContextBuilder namespaceContextBuilder = new NamespaceContextBuilder();

    @Override
    public void validateMessage(Message receivedMessage, Message controlMessage, TestContext context, XpathMessageValidationContext validationContext) throws ValidationException {
        if (CollectionUtils.isEmpty(validationContext.getXpathExpressions())) { return; }

        if (receivedMessage.getPayload() == null || !StringUtils.hasText(receivedMessage.getPayload(String.class))) {
            throw new ValidationException("Unable to validate message elements - receive message payload was empty");
        }

        log.debug("Start XPath element validation ...");

        Document received = XMLUtils.parseMessagePayload(receivedMessage.getPayload(String.class));
        NamespaceContext namespaceContext = namespaceContextBuilder.buildContext(
                receivedMessage, validationContext.getNamespaces());

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

            validateValues(xPathResult, expectedValue, xPathExpression, context);
        }

    }

    @Override
    protected Class<XpathMessageValidationContext> getRequiredValidationContextType() {
        return XpathMessageValidationContext.class;
    }

    @Override
    public boolean supportsMessageType(String messageType, Message message) {
        return new DomXmlMessageValidator().supportsMessageType(messageType, message);
    }

    /**
     * Validate the values of an XPath expression.
     * If a ValidationException is thrown, and exceptions are to be suppressed, then construct an XpathAssertionResult from
     * the expected result, actual result, and XPath expression, and add this as a failure to the Test Context.
     * Otherwise, re-throw the exception.
     * @param xPathResult Actual result of the evaluation
     * @param expectedValue Expected result of the evaluation
     * @param xPathExpression XPath expression to evaluate
     * @param context Test Context in which the evaluation occurs
     */
    protected void validateValues(Object xPathResult, Object expectedValue, String xPathExpression, TestContext context) {
        try {
            ValidationUtils.validateValues(xPathResult, expectedValue, xPathExpression, context);
            if (log.isDebugEnabled()) {
                log.debug("Validating element: " + xPathExpression + "='" + expectedValue + "': OK.");
            }
        }
        catch (ValidationException ex) {
            if (suppressExceptionsOnXpathEvaluations) {
                context.addFailure(new XpathAssertionResult(xPathExpression, (String)expectedValue, (String)xPathResult));
                if (log.isDebugEnabled()) {
                    log.debug("Validating element: " + xPathExpression + "='" + expectedValue + "': Failed.");
                }
            }
            else {
                throw ex;
            }
        }
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
