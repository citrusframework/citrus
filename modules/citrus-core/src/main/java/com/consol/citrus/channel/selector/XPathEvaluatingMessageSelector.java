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
package com.consol.citrus.channel.selector;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;
import org.springframework.integration.core.MessageSelector;
import org.springframework.xml.xpath.XPathExpressionFactory;
import org.springframework.xml.xpath.XPathParseException;
import org.w3c.dom.Document;
import org.w3c.dom.ls.LSException;

import com.consol.citrus.util.XMLUtils;
import com.consol.citrus.xml.xpath.XPathUtils;

/**
 * Message selector accepts XML messages in case XPath expression evaluation result matches
 * the expected value. With this selector someone can select messages aaccording to a message payload XML 
 * element value for instance.
 * 
 * Syntax is xpath://root/element
 * 
 * @author Christoph Deppisch
 * @since 1.2
 */
public class XPathEvaluatingMessageSelector implements MessageSelector {

    /** Expression to evaluate for acceptance */
    private final String expression;
    
    private final String control;
    
    /** Special selector element name identifying this message selector implementation */
    public static final String XPATH_SELECTOR_ELEMENT = "xpath:";
    
    /** Logger */
    private static Logger log = LoggerFactory.getLogger(XPathEvaluatingMessageSelector.class);
    
    /**
     * Default constructor using fields.
     */
    public XPathEvaluatingMessageSelector(String expression, String control) {
        this.control = control;
        this.expression = expression.substring(XPATH_SELECTOR_ELEMENT.length());
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean accept(Message<?> message) {
        Document doc;
        
        try {
            doc = XMLUtils.parseMessagePayload(message.getPayload().toString());
        } catch (LSException e) {
            log.warn("Ignoring non XML message for XPath message selector (" + e.getClass().getName() + ")");
            return false; // non XML message - not accepted
        }
        
        try {
            Map<String, String> namespaces = XPathUtils.getDynamicNamespaces(expression);
            
            if (!namespaces.isEmpty()) {
                return XPathExpressionFactory.createXPathExpression(XPathUtils.replaceDynamicNamespaces(expression, namespaces), namespaces)
                        .evaluateAsString(doc).equals(control);
            } else {
                return XPathExpressionFactory.createXPathExpression(expression, XMLUtils.lookupNamespaces(doc))
                        .evaluateAsString(doc).equals(control);
            }
            
            
        } catch (XPathParseException e) {
            log.warn("Could not evaluate XPath expression for message selector - ignoring message (" + e.getClass().getName() + ")");
            return false; // wrong XML message - not accepted
        }
    }

}
