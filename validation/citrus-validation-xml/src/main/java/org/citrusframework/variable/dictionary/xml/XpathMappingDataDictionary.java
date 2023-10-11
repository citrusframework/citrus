/*
 * Copyright 2006-2013 the original author or authors.
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

package org.citrusframework.variable.dictionary.xml;

import java.util.Map;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathConstants;

import org.citrusframework.XmlValidationHelper;
import org.citrusframework.common.InitializingPhase;
import org.citrusframework.context.TestContext;
import org.citrusframework.util.XMLUtils;
import org.citrusframework.variable.dictionary.DataDictionary;
import org.citrusframework.xml.namespace.DefaultNamespaceContext;
import org.citrusframework.xml.namespace.NamespaceContextBuilder;
import org.citrusframework.xml.xpath.XPathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Xml data dictionary implementation maps elements via XPath expressions. When element is identified by some expression
 * in dictionary value is overwritten accordingly. Namespace context is either evaluated on the fly or by global namespace
 * context builder.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class XpathMappingDataDictionary extends AbstractXmlDataDictionary implements InitializingPhase {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(XpathMappingDataDictionary.class);

    private NamespaceContextBuilder namespaceContextBuilder;

    @Override
    public <T> T translate(Node node, T value, TestContext context) {
        for (Map.Entry<String, String> expressionEntry : mappings.entrySet()) {
            String expression = expressionEntry.getKey();

            NodeList findings = (NodeList) XPathUtils.evaluateExpression(node.getOwnerDocument(), expression,
                    buildNamespaceContext(node, context), XPathConstants.NODESET);

            if (findings != null && containsNode(findings, node)) {
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("Data dictionary setting element '%s' value: %s",
                            XMLUtils.getNodesPathName(node), expressionEntry.getValue()));
                }
                return convertIfNecessary(expressionEntry.getValue(), value, context);
            }
        }

        return value;
    }

    /**
     * Checks if given node set contains node.
     * @param findings
     * @param node
     * @return
     */
    private boolean containsNode(NodeList findings, Node node) {
        for (int i = 0; i < findings.getLength(); i++) {
            if (findings.item(i).equals(node)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Builds namespace context with dynamic lookup on received node document and global namespace mappings from
     * namespace context builder.
     * @param node the element node from message
     * @param context the current test context
     * @return
     */
    private NamespaceContext buildNamespaceContext(Node node, TestContext context) {
        DefaultNamespaceContext simpleNamespaceContext = new DefaultNamespaceContext();
        Map<String, String> namespaces = XMLUtils.lookupNamespaces(node.getOwnerDocument());

        // add default namespace mappings
        namespaces.putAll(getNamespaceContextBuilder(context).getNamespaceMappings());

        simpleNamespaceContext.addNamespaces(namespaces);

        return simpleNamespaceContext;
    }

    @Override
    public void initialize() {
        if (getPathMappingStrategy() != null &&
                !getPathMappingStrategy().equals(DataDictionary.PathMappingStrategy.EXACT)) {
            logger.warn(String.format("%s ignores path mapping strategy other than %s",
                    getClass().getSimpleName(), DataDictionary.PathMappingStrategy.EXACT));
        }

        super.initialize();
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
