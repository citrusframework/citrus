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
import com.consol.citrus.util.XMLUtils;
import com.consol.citrus.variable.VariableExtractor;
import com.consol.citrus.xml.xpath.XPathExpressionResult;
import com.consol.citrus.xml.xpath.XPathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.namespace.NamespaceContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class reads message elements via XPath expressions and saves the text values as new test variables.
 * Implementation parsed the message payload as DOM document, so XML message payload is needed here.
 *  
 * @author Christoph Deppisch
 */
public class XpathPayloadVariableExtractor implements VariableExtractor {

    /** Map defines xpath expressions and target variable names */
    private Map<String, String> xPathExpressions = new HashMap<String, String>();
    
    /** Namespace definitions used in xpath expressions */
    private Map<String, String> namespaces = new HashMap<String, String>();
    
    /** Logger */
    private static Logger log = LoggerFactory.getLogger(XpathPayloadVariableExtractor.class);
    
    /**
     * Extract variables using Xpath expressions.
     */
    public void extractVariables(Message message, TestContext context) {
        if (CollectionUtils.isEmpty(xPathExpressions)) {return;}

        if (log.isDebugEnabled()) {
            log.debug("Reading XML elements with XPath");
        }
        
        NamespaceContext nsContext = context.getNamespaceContextBuilder().buildContext(message, namespaces);

        for (Entry<String, String> entry : xPathExpressions.entrySet()) {
            String pathExpression = entry.getKey();
            String variableName = entry.getValue();

            if (log.isDebugEnabled()) {
                log.debug("Evaluating XPath expression: " + pathExpression);
            }
            
            Document doc = XMLUtils.parseMessagePayload(message.getPayload(String.class));
            
            if (XPathUtils.isXPathExpression(pathExpression)) {
                XPathExpressionResult resultType = XPathExpressionResult.fromString(pathExpression, XPathExpressionResult.STRING);
                pathExpression = XPathExpressionResult.cutOffPrefix(pathExpression);
                
                String value = XPathUtils.evaluate(doc, pathExpression, nsContext, resultType);

                if (value == null) {
                    throw new CitrusRuntimeException("Not able to find value for expression: " + pathExpression);
                }
                
                context.setVariable(variableName, value);
            } else {
                Node node = XMLUtils.findNodeByName(doc, pathExpression);

                if (node == null) {
                    throw new UnknownElementException("No element found for expression" + pathExpression);
                }

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    if (node.getFirstChild() != null) {
                        context.setVariable(xPathExpressions.get(pathExpression), node.getFirstChild().getNodeValue());
                    } else {
                        context.setVariable(xPathExpressions.get(pathExpression), "");
                    }
                } else {
                    context.setVariable(xPathExpressions.get(pathExpression), node.getNodeValue());
                }
            }
        }
    }

    /**
     * Set the xPath expressions to identify the message elements and variable names.
     * @param xPathExpressions the xPathExpressions to set
     */
    public void setXpathExpressions(Map<String, String> xPathExpressions) {
        this.xPathExpressions = xPathExpressions;
    }
    
    /**
     * List of expected namespaces.
     * @param namespaces the namespaces to set
     */
    public void setNamespaces(Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

    /**
     * Gets the xPathExpressions.
     * @return the xPathExpressions
     */
    public Map<String, String> getXpathExpressions() {
        return xPathExpressions;
    }

    /**
     * Gets the namespaces.
     * @return the namespaces
     */
    public Map<String, String> getNamespaces() {
        return namespaces;
    }
}
