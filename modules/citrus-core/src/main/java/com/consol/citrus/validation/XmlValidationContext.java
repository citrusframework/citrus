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

package com.consol.citrus.validation;

import java.util.*;

import javax.xml.namespace.NamespaceContext;

import org.springframework.core.io.Resource;

/**
 * XML validation context holding validation specific information needed for XML 
 * message validation.
 * 
 * @author Christoph Deppisch
 */
public class XmlValidationContext extends ValidationContext {
    /** Map holding xpath expressions as key and expected values as values */
    private Map<String, String> expectedMessageElements;
    
    /** Map holding xpath expressions to identify the ignored message elements */
    private Set<String> ignoreMessageElements;

    /** Namespace context resolving namespaces in XML message */
    private NamespaceContext namespaceContext;
    
    /** dtdResource for DTD validation */
    private Resource dtdResource;
    
    /** Map holding expected namespaces */
    private Map<String, String> expectedNamespaces;
    
    /** Should message be validated with its schema definition */
    private boolean schemaValidation = true;
    
    /**
     * Get the control message elements that have to be present in
     * the received message. Message element values are compared as well.
     * @return the expectedMessageElements
     */
    public Map<String, String> getExpectedMessageElements() {
        return expectedMessageElements;
    }

    /**
     * Set the control message elements explicitly validated during message validation.
     * @param expectedMessageElements the expectedMessageElements to set
     */
    public void setExpectedMessageElements(Map<String, String> expectedMessageElements) {
        this.expectedMessageElements = expectedMessageElements;
    }

    /**
     * Get ignored message elements.
     * @return the ignoreMessageElements
     */
    public Set<String> getIgnoreMessageElements() {
        return ignoreMessageElements;
    }

    /**
     * Set ignored message elements.
     * @param ignoreMessageElements the ignoreMessageElements to set
     */
    public void setIgnoreMessageElements(Set<String> ignoreMessageElements) {
        this.ignoreMessageElements = ignoreMessageElements;
    }

    /**
     * Get the namespace context.
     * @return the namespaceContext
     */
    public NamespaceContext getNamespaceContext() {
        return namespaceContext;
    }

    /**
     * Set the namespace context.
     * @param namespaceContext the namespaceContext to set
     */
    public void setNamespaceContext(NamespaceContext namespaceContext) {
        this.namespaceContext = namespaceContext;
    }

    /**
     * Get the dtd resource.
     * @return the dtdResource
     */
    public Resource getDTDResource() {
        return dtdResource;
    }

    /**
     * Set dtd resource.
     * @param dtdResource the dtdResource to set
     */
    public void setDTDResource(Resource dtdResource) {
        this.dtdResource = dtdResource;
    }

    /**
     * Get control namespace elements.
     * @return the expectedNamespaces
     */
    public Map<String, String> getExpectedNamespaces() {
        return expectedNamespaces;
    }

    /**
     * Set the control namespace elements.
     * @param expectedNamespaces the expectedNamespaces to set
     */
    public void setExpectedNamespaces(Map<String, String> expectedNamespaces) {
        this.expectedNamespaces = expectedNamespaces;
    }

    /**
     * Is schema validation enabled.
     * @return the schemaValidation
     */
    public boolean isSchemaValidation() {
        return schemaValidation;
    }

    /**
     * Enable/disable schema validation.
     * @param schemaValidation the schemaValidation to set
     */
    public void setSchemaValidation(boolean schemaValidation) {
        this.schemaValidation = schemaValidation;
    }

}
