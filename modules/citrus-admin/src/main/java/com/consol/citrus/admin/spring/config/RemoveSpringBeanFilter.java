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

import org.w3c.dom.*;
import org.w3c.dom.traversal.NodeFilter;

/**
 * Filter removes a Spring bean definition from the beans section in a
 * Spring XML application context.
 * 
 * @author Christoph Deppisch
 */
public class RemoveSpringBeanFilter extends AbstractSpringBeanFilter {

    /** Id or bean name of the bean definition to be removed */
    protected String elementId;

    /** Delete marker set as user data on nodes (element + child nodes) that need to be deleted */
    private static final String DELETE_NODE_MARKER = "citrus_admin_delete_marker";
    
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
            
            if (element.getNextSibling() != null && element.getNextSibling().getNodeType() == Node.TEXT_NODE) {
                element.getParentNode().removeChild(element.getNextSibling());
            }
            
            markChildElementsForDeletion(element);
            return NodeFilter.FILTER_REJECT;
        }
        
        if (shouldDeleteNode(element)) {
            return NodeFilter.FILTER_REJECT;
        }
        
        return NodeFilter.FILTER_ACCEPT;
    }

    /**
     * Mark child elements for deletion. When elements get processed by filter in future
     * reject them.
     * @param element
     */
    private void markChildElementsForDeletion(Element element) {
        for (int i = 0; i < element.getChildNodes().getLength(); i++) {
            element.getChildNodes().item(i).setUserData(DELETE_NODE_MARKER, true, null);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public short acceptText(Text text) {
        if (shouldDeleteNode(text)) {
            return NodeFilter.FILTER_REJECT;
        }
        
        return NodeFilter.FILTER_ACCEPT;
    }

    /**
     * Checks whether the given element is a child of the deleted element node.
     * @param element
     * @return
     */
    private boolean shouldDeleteNode(Node element) {
        if (element.getUserData(DELETE_NODE_MARKER) != null) {
            if (element.getNodeType() == Node.ELEMENT_NODE) {
                markChildElementsForDeletion((Element) element);
            }
            return true;
        }

        return false;
    }

}
