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
import org.w3c.dom.ls.LSParserFilter;
import org.w3c.dom.ls.LSSerializerFilter;
import org.w3c.dom.traversal.NodeFilter;

/**
 * Abstract XML load and save serializer filter for manipulating the XML application context DOM object model.
 * Subclasses may add, remove or update Spring bean definitions on the Spring XML application context.
 * 
 * @author Christoph Deppisch
 */
public abstract class AbstractSpringBeanFilter implements LSSerializerFilter, LSParserFilter {

    /**
     * Checks for element equality by bean id attribute.
     * @param element
     * @return
     */
    protected boolean isEqualById(Element element, String elementId) {
        return element.hasAttribute("id") && element.getAttribute("id").equals(elementId);
    }

    /**
     * Checks for element equality by bean name attribute.
     * @param element
     * @return
     */
    protected boolean isEqualByBeanName(Element element, String elementId) {
        return element.hasAttribute("name") && element.getAttribute("name").equals(elementId);
    }
    
    /**
     * {@inheritDoc}
     */
    public short startElement(Element element) {
        return NodeFilter.FILTER_ACCEPT;
    }
    
    /**
     * {@inheritDoc}
     */
    public short acceptNode(Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            return accept((Element)node);
        }
        
        return NodeFilter.FILTER_ACCEPT;
    }
    
    /**
     * Abstract element accept method. Subclasses must implement this method in order to decide filter accepts or declines this
     * element node.
     */
    public abstract short accept(Element element);
    
    /**
     * {@inheritDoc}
     */
    public int getWhatToShow() {
        return NodeFilter.SHOW_ELEMENT;
    }
}
