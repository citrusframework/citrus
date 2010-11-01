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

import javax.xml.namespace.NamespaceContext;

import com.consol.citrus.validation.ControlMessageValidationAware;

/**
 * Interface marking that a class is aware of Xml specific validation information like expected namespaces, XPath
 * expressions, Xml dom tree validation and so on.
 * 
 * @author Christoph Deppisch
 */
public interface XmlMessageValidationAware extends ControlMessageValidationAware {
    /**
     * Get XPath validation expressions.
     * @return the validation expressions.
     */
    public Map<String, String> getPathValidationExpressions();
    
    /**
     * Get the ignored message elements specified via XPath. 
     * @return the ignoreExpressions
     */
    public Set<String> getIgnoreExpressions();
    
    /**
     * Get the control namesapces which must be present 
     * in the received message.
     * @return the control namespaces map
     */
    public Map<String, String> getControlNamespaces();
    
    /**
     * Check schema validation enabled.
     * @return the flag to mark schema validation enabled/disabled
     */
    public boolean isSchemaValidation();
    
    /**
     * Get the namespace context for this validation.
     * @return the namespace context.
     */
    public NamespaceContext getNamespaceContext();
}
