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

package com.consol.citrus.validation.interceptor;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.xml.namespace.SimpleNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.exceptions.UnknownElementException;
import com.consol.citrus.functions.FunctionUtils;
import com.consol.citrus.util.XMLUtils;
import com.consol.citrus.variable.VariableUtils;
import com.consol.citrus.xml.xpath.XPathUtils;

/**
 * Interceptor implementation evaluating XPath expressions on message payload during message construction.
 * Class identifies XML elements inside the message payload via XPath expressions in order to overwrite their value.
 * 
 * @author Christoph Deppisch
 */
public class XpathMessageConstructionInterceptor implements MessageConstructionInterceptor<String> {

    /** Overwrites message elements before validating (via XPath expressions) */
    private Map<String, String> xPathExpressions = new HashMap<String, String>();
    
    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(XpathMessageConstructionInterceptor.class);
    
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
    public String interceptMessageConstruction(String messagePayload, TestContext context) {
        Document doc = XMLUtils.parseMessagePayload(messagePayload);

        if (doc == null) {
            throw new CitrusRuntimeException("Not able to set message elements, because no XML ressource defined");
        }
        
        for (Entry<String, String> entry : xPathExpressions.entrySet()) {
            String pathExpression = entry.getKey();
            String valueExpression = entry.getValue();

            if (VariableUtils.isVariableName(valueExpression)) {
                valueExpression = context.getVariable(valueExpression);
            } else if(context.getFunctionRegistry().isFunction(valueExpression)) {
                valueExpression = FunctionUtils.resolveFunction(valueExpression, context);
            } 

            if (valueExpression == null) {
                throw new CitrusRuntimeException("Can not set null values in XML document - path expression is " + pathExpression);
            }
            
            Node node;
            if (XPathUtils.isXPathExpression(pathExpression)) {
                SimpleNamespaceContext nsContext = new SimpleNamespaceContext();
                nsContext.setBindings(XMLUtils.lookupNamespaces(messagePayload));
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
            
            if(log.isDebugEnabled()) {
                log.debug("Element " +  pathExpression + " was set to value: " + valueExpression);
            }
        }
        
        return XMLUtils.serialize(doc);
    }

    /**
     * Intercept the message and modify the message payload.
     */
    public Message<String> interceptMessageConstruction(Message<String> message, TestContext context) {
       return MessageBuilder.withPayload(interceptMessageConstruction(message.getPayload(), context))
                            .copyHeaders(message.getHeaders()).build();
    }

    /**
     * @param xPathExpressions the xPathExpressions to set
     */
    public void setxPathExpressions(Map<String, String> xPathExpressions) {
        this.xPathExpressions = xPathExpressions;
    }

}
