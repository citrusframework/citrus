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

import com.consol.citrus.CitrusConstants;
import com.consol.citrus.util.XMLUtils;
import com.consol.citrus.xml.xpath.XPathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Node;

import javax.xml.namespace.NamespaceContext;
import java.util.Set;

/**
 * @author Christoph Deppisch
 * @since 2.2.1
 */
public abstract class XmlValidationUtils {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(XmlValidationUtils.class);

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
    public static boolean isIgnored(Node source, Node received, Set<String> ignoreExpressions,
                                         NamespaceContext namespaceContext) {
        if (XmlValidationUtils.isIgnored(received, ignoreExpressions, namespaceContext)) {
            if (log.isDebugEnabled()) {
                log.debug("Element: '" + received.getLocalName() + "' is on ignore list - skipped validation");
            }
            return true;
        } else if (source.getFirstChild() != null &&
                StringUtils.hasText(source.getFirstChild().getNodeValue()) &&
                source.getFirstChild().getNodeValue().trim().equals(CitrusConstants.IGNORE_PLACEHOLDER)) {
            if (log.isDebugEnabled()) {
                log.debug("Element: '" + received.getLocalName() + "' is ignored by placeholder '" +
                        CitrusConstants.IGNORE_PLACEHOLDER + "'");
            }
            return true;
        }
        return false;
    }

    /**
     * Checks whether the node is ignored by node path expression or xpath expression.
     * @param node
     * @param ignoreExpressions
     * @param namespaceContext
     * @return
     */
    public static boolean isIgnored(final Node node, Set<String> ignoreExpressions,
                                  NamespaceContext namespaceContext) {
        if (CollectionUtils.isEmpty(ignoreExpressions)) {
            return false;
        }

        /** This is the faster version, but then the ignoreValue name must be
         * the full path name like: Numbers.NumberItem.AreaCode
         */
        if (ignoreExpressions.contains(XMLUtils.getNodesPathName(node))) {
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
            if (node == XMLUtils.findNodeByName(node.getOwnerDocument(), expression)) {
                return true;
            }
        }

        /** This is the XPath version using XPath expressions in
         * ignoreValues to identify nodes to be ignored
         */
        for (String expression : ignoreExpressions) {
            if (XPathUtils.isXPathExpression(expression)) {
                Node foundNode = XPathUtils.evaluateAsNode(node.getOwnerDocument(),
                        expression,
                        namespaceContext);

                if (foundNode != null && foundNode.isSameNode(node)) {
                    return true;
                }
            }
        }

        return false;
    }
}
