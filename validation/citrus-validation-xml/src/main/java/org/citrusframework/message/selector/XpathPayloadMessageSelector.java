/*
 * Copyright 2006-2012 the original author or authors.
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
package org.citrusframework.message.selector;

import java.util.Map;

import org.citrusframework.context.TestContext;
import org.citrusframework.message.Message;
import org.citrusframework.util.XMLUtils;
import org.citrusframework.xml.xpath.XPathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.xml.xpath.XPathExpressionFactory;
import org.springframework.xml.xpath.XPathParseException;
import org.w3c.dom.Document;
import org.w3c.dom.ls.LSException;

/**
 * Message selector accepts XML messages in case XPath expression evaluation result matches
 * the expected value. With this selector someone can select messages according to a message payload XML
 * element value for instance.
 *
 * Syntax is xpath://root/element
 *
 * @author Christoph Deppisch
 * @since 3.0
 */
public class XpathPayloadMessageSelector extends AbstractMessageSelector {

    /** Special selector element name identifying this message selector implementation */
    public static final String SELECTOR_PREFIX = "xpath:";

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(XpathPayloadMessageSelector.class);

    /**
     * Default constructor using fields.
     */
    public XpathPayloadMessageSelector(String selectKey, String matchingValue, TestContext context) {
        super(selectKey.substring(SELECTOR_PREFIX.length()), matchingValue, context);
    }

    @Override
    public boolean accept(Message message) {
        Document doc;

        try {
            doc = XMLUtils.parseMessagePayload(getPayloadAsString(message));
        } catch (LSException e) {
            logger.warn("Ignoring non XML message for XPath message selector (" + e.getClass().getName() + ")");
            return false; // non XML message - not accepted
        }

        try {
            Map<String, String> namespaces = XMLUtils.lookupNamespaces(doc);

            // add default namespace mappings
            namespaces.putAll(context.getNamespaceContextBuilder().getNamespaceMappings());

            String value;
            if (XPathUtils.hasDynamicNamespaces(selectKey)) {
                namespaces.putAll(XPathUtils.getDynamicNamespaces(selectKey));
                value = XPathExpressionFactory.createXPathExpression(XPathUtils.replaceDynamicNamespaces(selectKey, namespaces), namespaces)
                        .evaluateAsString(doc);
            } else {
                value = XPathExpressionFactory.createXPathExpression(selectKey, namespaces)
                        .evaluateAsString(doc);
            }

            return evaluate(value);
        } catch (XPathParseException e) {
            logger.warn("Could not evaluate XPath expression for message selector - ignoring message (" + e.getClass().getName() + ")");
            return false; // wrong XML message - not accepted
        }
    }

    /**
     * Message selector factory for this implementation.
     */
    public static class Factory implements MessageSelectorFactory {

        @Override
        public boolean supports(String key) {
            return key.startsWith(SELECTOR_PREFIX);
        }

        @Override
        public XpathPayloadMessageSelector create(String key, String value, TestContext context) {
            return new XpathPayloadMessageSelector(key, value, context);
        }
    }

}
