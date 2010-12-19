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

package com.consol.citrus.variable;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.core.Message;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.xml.namespace.NamespaceContextBuilder;

/**
 * @author Christoph Deppisch
 */
public class XpathPayloadVariableExtractor implements VariableExtractor {

    /** Map defines xpath expressions and target variable names */
    private Map<String, String> xPathExpressions = new HashMap<String, String>();
    
    /** Namespace definitions used in xpath expressions */
    private Map<String, String> namespaces;
    
    @Autowired(required= false)
    private NamespaceContextBuilder namespaceContextBuilder = new NamespaceContextBuilder();
    
    /**
     * Extract variables using Xpath expressions.
     */
    public void extractVariables(Message<?> message, TestContext context) {
        //TODO move logic from test context to this extractor.
        context.createVariablesFromMessageValues(xPathExpressions, 
                message, 
                namespaceContextBuilder.buildContext(message, namespaces));
    }

    /**
     * Set the xPath expressions to identify the message elements and variable names.
     * @param xPathExpressions the xPathExpressions to set
     */
    public void setxPathExpressions(Map<String, String> xPathExpressions) {
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
     * Sets the namespace context builder.
     * @param namespaceContextBuilder the namespaceContextBuilder to set
     */
    public void setNamespaceContextBuilder(NamespaceContextBuilder namespaceContextBuilder) {
        this.namespaceContextBuilder = namespaceContextBuilder;
    }
}
