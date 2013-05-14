/*
 * Copyright 2006-2013 the original author or authors.
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

package com.consol.citrus.admin.config;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeFilter;

/**
 * Filter removes a Spring bean definition from the beans section in a
 * Spring XML application context.
 * 
 * @author Christoph Deppisch
 */
public class RemoveSpringBeanFilter extends AbstractSpringBeanFilter {

    /** Id or bean name of the bean definition to be removed */
    private String elementId;
    
    /** Temporary holds removed node so all its children are removed too during the parsing operation */
    private Node delete;
    
    /**
     * Constructor using element id field.
     */
    public RemoveSpringBeanFilter(String elementId) {
        this.elementId = elementId;
    }
    
    /**
     * {@inheritDoc}
     */
    public short accept(Element element) {
        if (isEqualById(element, elementId) || isEqualByBeanName(element, elementId)) {
            delete = element;
            return NodeFilter.FILTER_REJECT;
        }
        
        if (delete != null && element.getParentNode() != null && element.getParentNode().equals(delete)) {
            return NodeFilter.FILTER_REJECT;
        }
        
        return NodeFilter.FILTER_ACCEPT;
    }

}
