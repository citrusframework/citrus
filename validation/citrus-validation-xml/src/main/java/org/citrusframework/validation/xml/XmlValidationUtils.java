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

import java.util.Set;
import javax.xml.namespace.NamespaceContext;

import org.citrusframework.CitrusSettings;
import org.citrusframework.util.StringUtils;
import org.citrusframework.util.XMLUtils;
import org.citrusframework.xml.xpath.XPathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Christoph Deppisch
 * @since 2.3
 */
public abstract class XmlValidationUtils {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(XmlValidationUtils.class);

    /**
     * Prevent instantiation.
     */
    private XmlValidationUtils() {
        super();
    }

    /**
     * Checks if given element node is either on ignore list or
     * contains @ignore@ tag inside control message
     * @param source
     * @param received
     * @param ignoreExpressions
     * @param namespaceContext
     * @return
     */
    public static boolean isElementIgnored(Node source, Node received, Set<String> ignoreExpressions, NamespaceContext namespaceContext) {
        if (isElementIgnored(received, ignoreExpressions, namespaceContext)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Element: '" + received.getLocalName() + "' is on ignore list - skipped validation");
            }
            return true;
        } else if (source.getFirstChild() != null &&
                StringUtils.hasText(source.getFirstChild().getNodeValue()) &&
                source.getFirstChild().getNodeValue().trim().equals(CitrusSettings.IGNORE_PLACEHOLDER)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Element: '" + received.getLocalName() + "' is ignored by placeholder '" +
                        CitrusSettings.IGNORE_PLACEHOLDER + "'");
            }
            return true;
        }
        return false;
    }

    /**
     * Checks whether the node is ignored by node path expression or xpath expression.
     * @param received
     * @param ignoreExpressions
     * @param namespaceContext
     * @return
     */
    public static boolean isElementIgnored(final Node received, Set<String> ignoreExpressions, NamespaceContext namespaceContext) {
        if (ignoreExpressions == null || ignoreExpressions.isEmpty()) {
            return false;
        }

        /** This is the faster version, but then the ignoreValue name must be
         * the full path name like: Numbers.NumberItem.AreaCode
         */
        if (ignoreExpressions.contains(XMLUtils.getNodesPathName(received))) {
            return true;
        }

        /** This is the slower version, but here the ignoreValues can be
         * the short path name like only: AreaCode
         *
         * If there are more nodes with the same short name,
         * the first one will match, eg. if there are:
         *      Numbers1.NumberItem.AreaCode
         *      Numbers2.NumberItem.AreaCode
         * And ignoreValues contains just: AreaCode
         * the only first Node: Numbers1.NumberItem.AreaCode will be ignored.
         */
        for (String expression : ignoreExpressions) {
            if (received == XMLUtils.findNodeByName(received.getOwnerDocument(), expression)) {
                return true;
            }
        }

        /** This is the XPath version using XPath expressions in
         * ignoreValues to identify nodes to be ignored
         */
        for (String expression : ignoreExpressions) {
            if (XPathUtils.isXPathExpression(expression)) {
                NodeList foundNodes = XPathUtils.evaluateAsNodeList(received.getOwnerDocument(),
                        expression,
                        namespaceContext);

                if (foundNodes != null) {
                    for (int i = 0; i < foundNodes.getLength(); i++) {
                        if (foundNodes.item(i) != null && foundNodes.item(i).isSameNode(received)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Checks whether the current attribute is ignored either by global ignore placeholder in source attribute value or
     * by xpath ignore expressions.
     *
     * @param receivedElement
     * @param receivedAttribute
     * @param ignoreMessageElements
     * @return
     */
    public static boolean isAttributeIgnored(Node receivedElement, Node receivedAttribute, Node sourceAttribute,
                                             Set<String> ignoreMessageElements, NamespaceContext namespaceContext) {
        if (isAttributeIgnored(receivedElement, receivedAttribute, ignoreMessageElements, namespaceContext)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Attribute '" + receivedAttribute.getLocalName() + "' is on ignore list - skipped value validation");
            }

            return true;
        } else if ((StringUtils.hasText(sourceAttribute.getNodeValue()) &&
                sourceAttribute.getNodeValue().trim().equals(CitrusSettings.IGNORE_PLACEHOLDER))) {
            if (logger.isDebugEnabled()) {
                logger.debug("Attribute: '" + receivedAttribute.getLocalName() + "' is ignored by placeholder '" +
                        CitrusSettings.IGNORE_PLACEHOLDER + "'");
            }

            return true;
        }

        return false;
    }

    /**
     * Checks whether the current attribute is ignored.
     * @param receivedElement
     * @param receivedAttribute
     * @param ignoreMessageElements
     * @return
     */
    private static boolean isAttributeIgnored(Node receivedElement, Node receivedAttribute,
                                             Set<String> ignoreMessageElements, NamespaceContext namespaceContext) {
        if (ignoreMessageElements == null || ignoreMessageElements.isEmpty()) {
            return false;
        }

        /** This is the faster version, but then the ignoreValue name must be
         * the full path name like: Numbers.NumberItem.AreaCode
         */
        if (ignoreMessageElements.contains(XMLUtils.getNodesPathName(receivedElement) + "." + receivedAttribute.getNodeName())) {
            return true;
        }

        /** This is the slower version, but here the ignoreValues can be
         * the short path name like only: AreaCode
         *
         * If there are more nodes with the same short name,
         * the first one will match, eg. if there are:
         *      Numbers1.NumberItem.AreaCode
         *      Numbers2.NumberItem.AreaCode
         * And ignoreValues contains just: AreaCode
         * the only first Node: Numbers1.NumberItem.AreaCode will be ignored.
         */
        for (String expression : ignoreMessageElements) {
            Node foundAttributeNode = XMLUtils.findNodeByName(receivedElement.getOwnerDocument(), expression);

            if (foundAttributeNode != null && receivedAttribute.isSameNode(foundAttributeNode)) {
                return true;
            }
        }

        /** This is the XPath version using XPath expressions in
         * ignoreValues to identify nodes to be ignored
         */
        for (String expression : ignoreMessageElements) {
            if (XPathUtils.isXPathExpression(expression)) {
                Node foundAttributeNode = XPathUtils.evaluateAsNode(receivedElement.getOwnerDocument(),
                        expression,
                        namespaceContext);
                if (foundAttributeNode != null && foundAttributeNode.isSameNode(receivedAttribute)) {
                    return true;
                }
            }
        }

        return false;
    }
}
