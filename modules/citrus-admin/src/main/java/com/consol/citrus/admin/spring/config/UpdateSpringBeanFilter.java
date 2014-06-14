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

package com.consol.citrus.admin.spring.config;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeFilter;

/**
 * Filter searches fo a Spring bean definition and overwrites it with a new bean definition in 
 * a Spring XML application context.
 * 
 * @author Christoph Deppisch
 */
public class UpdateSpringBeanFilter extends RemoveSpringBeanFilter {

    /** New bean definition element to add */
    private Element beanDefinition;
    
    /** New node added */
    private Node added = null;

    /** Number of bean definitions that were updated by this filter operation */
    private int updatedBeans = 0;
    
    /**
     * Constructor using element id field.
     */
    public UpdateSpringBeanFilter(String elementId, Element beanDefinition) {
        super(elementId);
        this.beanDefinition = beanDefinition;
    }
    
    /**
     * {@inheritDoc}
     */
    public short accept(Element element) {
        if (added == null && (isEqualById(element, elementId) || isEqualByBeanName(element, elementId))) {
            if (element.getNextSibling() != null) {
                added = element.getParentNode().insertBefore(element.getOwnerDocument().importNode(beanDefinition, true), element.getNextSibling());
            } else {
                added = element.getParentNode().appendChild(element.getOwnerDocument().importNode(beanDefinition, true));
            }

            updatedBeans++;
        }
        
        if (element.equals(added)) {
            return NodeFilter.FILTER_ACCEPT; 
        }
        
        return super.accept(element);
    }

    /**
     * Gets the number of updated bean definitions.
     * @return
     */
    public int getUpdatedBeans() {
        return updatedBeans;
    }
}
