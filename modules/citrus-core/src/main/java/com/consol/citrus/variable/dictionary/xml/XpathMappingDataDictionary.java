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

package com.consol.citrus.variable.dictionary.xml;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.util.XMLUtils;
import com.consol.citrus.xml.namespace.NamespaceContextBuilder;
import com.consol.citrus.xml.xpath.XPathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.xml.namespace.SimpleNamespaceContext;
import org.w3c.dom.Node;

import javax.xml.namespace.NamespaceContext;
import java.util.Map;

/**
 * Xml data dictionary implementation maps elements via XPath expressions. When element is identified by some expression
 * in dictionary value is overwritten accordingly. Namespace context is either evaluated on the fly or by global namespace
 * context builder.
 *
 * @author Christoph Deppisch
 * @since 1.4
 */
public class XpathMappingDataDictionary extends AbstractXmlDataDictionary implements InitializingBean {

    @Autowired(required = false)
    private NamespaceContextBuilder namespaceContextBuilder = new NamespaceContextBuilder();

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(XpathMappingDataDictionary.class);

    @Override
    public String translate(Node node, String value, TestContext context) {
        for (Map.Entry<String, String> expressionEntry : mappings.entrySet()) {
            String expression = expressionEntry.getKey();

            Node finding = XPathUtils.evaluateAsNode(node.getOwnerDocument(), expression, buildNamespaceContext(node));

            if (finding != null && finding.equals(node)) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Data dictionary setting element '%s' value: %s", XMLUtils.getNodesPathName(node), expressionEntry.getValue()));
                }
                return context.replaceDynamicContentInString(expressionEntry.getValue());
            }
        }

        return value;
    }

    /**
     * Builds namespace context with dynamic lookup on received node document and global namespace mappings from
     * namespace context builder.
     * @param node the element node from message
     * @return
     */
    private NamespaceContext buildNamespaceContext(Node node) {
        SimpleNamespaceContext simpleNamespaceContext = new SimpleNamespaceContext();
        Map<String, String> namespaces = XMLUtils.lookupNamespaces(node.getOwnerDocument());

        // add default namespace mappings
        namespaces.putAll(namespaceContextBuilder.getNamespaceMappings());

        simpleNamespaceContext.setBindings(namespaces);

        return simpleNamespaceContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (getPathMappingStrategy() != null &&
                !getPathMappingStrategy().equals(PathMappingStrategy.EXACT_MATCH)) {
            log.warn(String.format("%s ignores path mapping strategy other than %s",
                    getClass().getSimpleName(), PathMappingStrategy.EXACT_MATCH));
        }

        super.afterPropertiesSet();
    }

    /**
     * @return
     */
    public NamespaceContextBuilder getNamespaceContextBuilder() {
        return namespaceContextBuilder;
    }

    /**
     * @param namespaceContextBuilder
     */
    public void setNamespaceContextBuilder(NamespaceContextBuilder namespaceContextBuilder) {
        this.namespaceContextBuilder = namespaceContextBuilder;
    }
}
