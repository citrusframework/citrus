/*
 * Copyright 2006-2018 the original author or authors.
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

package org.citrusframework.generate.dictionary;

import java.util.Map;
import javax.xml.xpath.XPathConstants;

import org.citrusframework.context.TestContext;
import org.citrusframework.variable.dictionary.xml.XpathMappingDataDictionary;
import org.citrusframework.xml.namespace.DefaultNamespaceContext;
import org.citrusframework.xml.xpath.XPathUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Christoph Deppisch
 */
public class InboundXmlDataDictionary extends XpathMappingDataDictionary {

    @Override
    public <T> T translate(Node node, T value, TestContext context) {
        if (value instanceof String) {
            String toTranslate;
            if (!mappings.isEmpty()) {
                toTranslate = (String) translateIfPresent(node, value, context);
            } else {
                toTranslate = (String) value;
            }

            if (toTranslate.equals(value)) {
                if (toTranslate.equals("true") || toTranslate.equals("false")) {
                    return (T) "@matches(true|false)@";
                } else if (Character.isDigit(toTranslate.charAt(0))) {
                    return (T) "@isNumber()@";
                } else if (toTranslate.startsWith("string")) {
                    return (T) "@notEmpty()@";
                }
            } else {
                return (T) toTranslate;
            }
        }

        return value;
    }

    /**
     * Translate value if node is present identified by Xpath evaluation.
     * @param node
     * @param value
     * @param context
     * @param <T>
     * @return
     */
    private <T> T translateIfPresent(Node node, T value, TestContext context) {
        for (Map.Entry<String, String> expressionEntry : mappings.entrySet()) {
            String expression = expressionEntry.getKey();

            DefaultNamespaceContext namespaceContext = new DefaultNamespaceContext();
            namespaceContext.addNamespaces(context.getNamespaceContextBuilder().getNamespaceMappings());

            NodeList findings = (NodeList) XPathUtils.evaluateExpression(node.getOwnerDocument(), expression, namespaceContext, XPathConstants.NODESET);

            if (findings != null && containsNode(findings, node)) {
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

    @Override
    public void initialize() {
        super.initialize();

        mappings.put("//*[string-length(normalize-space(text())) > 0]", "@ignore@");
        mappings.put("//@*", "@ignore@");
    }
}
