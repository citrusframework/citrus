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
 * Filter searches fo a Spring bean definition and overwrites it with a new bean definition in 
 * a Spring XML application context.
 * 
 * @author Christoph Deppisch
 */
public class UpdateSpringBeanFilter extends AbstractSpringBeanFilter {

    /** Id or bean name of the bean definition to be updated */
    private String elementId;
    
    /** New bean definition element to add */
    private Element beanDefinition;
    
    /** Temporary holds removed node so all its children are removed too during the parsing operation */
    private Node delete = null;
    
    /**
     * Constructor using element id field.
     */
    public UpdateSpringBeanFilter(String elementId, Element beanDefinition) {
        this.elementId = elementId;
        this.beanDefinition = beanDefinition;
    }
    
    /**
     * {@inheritDoc}
     */
    public short accept(Element element) {
        if (delete == null && (isEqualById(element, elementId) || isEqualByBeanName(element, elementId))) {
            element.getParentNode().appendChild(element.getOwnerDocument().importNode(beanDefinition, true));
            delete = element;
            return NodeFilter.FILTER_REJECT;
        }
        
        if (delete != null && element.getParentNode() != null && element.getParentNode().equals(delete)) {
            return NodeFilter.FILTER_REJECT;
        }
        
        return NodeFilter.FILTER_ACCEPT;
    }

}
