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

import java.util.Map;
import java.util.Set;

import org.springframework.util.CollectionUtils;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.validation.ControlMessageValidationContext;
import com.consol.citrus.validation.ControlMessageValidationContextBuilder;
import com.consol.citrus.validation.context.ValidationContext;

/**
 * Validation context builder specific for XML message validation. Constructs a 
 * validation context for XPath, namespoace, and XML Tree validation.
 * 
 * @author Christoph Deppisch
 */
public class XmlMessageValidationContextBuilder extends ControlMessageValidationContextBuilder {

    /** XPath validation expressions */
    private Map<String, String> pathValidationExpressions;

    /** Ignored xml elements via XPath */
    private Set<String> ignoreExpressions;

    /** Control namespaces for message validation */
    private Map<String, String> controlNamespaces;

    /** Mark schema validation enabled */
    private boolean schemaValidation = true;
    
    /** Namespace definitions for xpath expression evaluation */
    private Map<String, String> namespaces;
    
    @Override
    public ControlMessageValidationContext prepareValidationContext(TestContext context) {
        XmlMessageValidationContext validationContext =  new XmlMessageValidationContext();
        
        validationContext.setPathValidationExpressions(pathValidationExpressions);
        validationContext.setIgnoreMessageElements(ignoreExpressions);
        validationContext.setControlNamespaces(controlNamespaces);
        validationContext.setSchemaValidation(schemaValidation);
        validationContext.setNamespaces(namespaces);
        
        return validationContext;
    }
    
    @Override
    public boolean supportsValidationContextType(Class<? extends ValidationContext> validationContextType) {
        // this builder supports XmlMessageValidationContext as well as simple ControlMessageValidationContext
        // in latter case only if Xml specific validation information is not set at all
        if (validationContextType.equals(XmlMessageValidationContext.class)) {
            return true;
        } else if (validationContextType.equals(ControlMessageValidationContext.class) && 
                CollectionUtils.isEmpty(controlNamespaces) &&
                CollectionUtils.isEmpty(ignoreExpressions) &&
                CollectionUtils.isEmpty(pathValidationExpressions)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Setter for XPath validation expressions.
     * @param validationExpressions
     */
    public void setPathValidationExpressions(Map<String, String> validationExpressions) {
        this.pathValidationExpressions = validationExpressions;
    }
    
    /**
     * Setter for ignored message elements.
     * @param ignoreExpressions
     */
    public void setIgnoreExpressions(Set<String> ignoreExpressions) {
        this.ignoreExpressions = ignoreExpressions;
    }
    
    /**
     * Setter for control namespaces that must be present in the XML message.
     * @param controlNamespaces the controlNamespaces to set
     */
    public void setControlNamespaces(Map<String, String> controlNamespaces) {
        this.controlNamespaces = controlNamespaces;
    }
    
    /**
     * Enable/Disable schema validation.
     * @param enableSchemaValidation flag to enable/disable schema validation
     */
    public void setSchemaValidation(boolean schemaValidation) {
        this.schemaValidation = schemaValidation;
    }

    /**
     * Set the namespace definitions.
     * @param namespaces the namespaces to set
     */
    public void setNamespaces(Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }
}
