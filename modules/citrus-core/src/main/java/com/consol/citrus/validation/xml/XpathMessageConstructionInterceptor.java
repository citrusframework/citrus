/*
 * Copyright 2006-2010 the original author or authors.
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
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.UnknownElementException;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.util.XMLUtils;
import com.consol.citrus.validation.interceptor.AbstractMessageConstructionInterceptor;
import com.consol.citrus.xml.xpath.XPathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.xml.namespace.SimpleNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Interceptor implementation evaluating XPath expressions on message payload during message construction.
 * Class identifies XML elements inside the message payload via XPath expressions in order to overwrite their value.
 * 
 * @author Christoph Deppisch
 */
public class XpathMessageConstructionInterceptor extends AbstractMessageConstructionInterceptor {

    /** Overwrites message elements before validating (via XPath expressions) */
    private Map<String, String> xPathExpressions = new HashMap<String, String>();
    
    /** Logger */
    private static Logger log = LoggerFactory.getLogger(XpathMessageConstructionInterceptor.class);

    /**
     * Default constructor.
     */
    public XpathMessageConstructionInterceptor() {
        super();
    }

    /**
     * Default constructor using fields.
     * @param xPathExpressions
     */
    public XpathMessageConstructionInterceptor(Map<String, String> xPathExpressions) {
        super();
        this.xPathExpressions = xPathExpressions;
    }

    /**
     * Intercept the message payload construction and replace elements identified 
     * via XPath expressions.
     *
     * Method parses the message payload to DOM document representation, therefore message payload
     * needs to be XML here.
     */
    @Override
    public Message interceptMessage(Message message, String messageType, TestContext context) {
        if (message.getPayload() == null || !StringUtils.hasText(message.getPayload(String.class))) {
            return message;
        }

        Document doc = XMLUtils.parseMessagePayload(message.getPayload(String.class));

        if (doc == null) {
            throw new CitrusRuntimeException("Not able to set message elements, because no XML ressource defined");
        }

        for (Entry<String, String> entry : xPathExpressions.entrySet()) {
            String pathExpression = entry.getKey();
            String valueExpression = entry.getValue();

            //check if value expr is variable or function (and resolve it if yes)
            valueExpression = context.replaceDynamicContentInString(valueExpression);

            Node node;
            if (XPathUtils.isXPathExpression(pathExpression)) {
                SimpleNamespaceContext nsContext = new SimpleNamespaceContext();
                nsContext.setBindings(XMLUtils.lookupNamespaces(message.getPayload(String.class)));
                node = XPathUtils.evaluateAsNode(doc, pathExpression, nsContext);
            } else {
                node = XMLUtils.findNodeByName(doc, pathExpression);
            }

            if (node == null) {
                throw new UnknownElementException("Could not find element for expression" + pathExpression);
            }

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (node.getFirstChild() == null) {
                    node.appendChild(doc.createTextNode(valueExpression));
                } else {
                    node.getFirstChild().setNodeValue(valueExpression);
                }
            } else {
                node.setNodeValue(valueExpression);
            }
            
            if (log.isDebugEnabled()) {
                log.debug("Element " +  pathExpression + " was set to value: " + valueExpression);
            }
        }
        
        message.setPayload(XMLUtils.serialize(doc));
        return message;
    }

    @Override
    public boolean supportsMessageType(String messageType) {
        return MessageType.XML.toString().equalsIgnoreCase(messageType);
    }

    /**
     * @param xPathExpressions the xPathExpressions to set
     */
    public void setXPathExpressions(Map<String, String> xPathExpressions) {
        this.xPathExpressions = xPathExpressions;
    }

    /**
     * Gets the xPathExpressions.
     * @return the xPathExpressions
     */
    public Map<String, String> getXPathExpressions() {
        return xPathExpressions;
    }
}
