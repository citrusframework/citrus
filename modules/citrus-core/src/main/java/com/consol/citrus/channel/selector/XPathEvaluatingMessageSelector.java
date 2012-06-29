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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;
import org.springframework.integration.core.MessageSelector;
import org.w3c.dom.Document;
import org.w3c.dom.ls.LSException;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.XMLUtils;
import com.consol.citrus.xml.xpath.XPathUtils;

/**
 * Message selector accepts XML messages according to specified root element QName.
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
            log.warn("XPath evaluating message selector ignoring not well-formed XML message payload", e);
            return false; // non XML message - not accepted
        }
        
        try {
            return XPathUtils.evaluateAsString(doc, expression, null).equals(control);
        } catch (CitrusRuntimeException e) {
            if (e.getMessage().startsWith("Can not evaluate xpath expression") || 
                    e.getMessage().startsWith("No result for XPath expression")) {
                return false;
            } else {
                throw e;
            }
        }
    }

}
