/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
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
