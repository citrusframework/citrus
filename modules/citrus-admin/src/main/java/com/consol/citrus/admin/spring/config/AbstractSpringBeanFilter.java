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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.w3c.dom.ls.LSParserFilter;
import org.w3c.dom.ls.LSSerializerFilter;
import org.w3c.dom.traversal.NodeFilter;

import java.util.Map;

/**
 * Abstract XML load and save serializer filter for manipulating the XML application context DOM object model.
 * Subclasses may add, remove or update Spring bean definitions on the Spring XML application context.
 * 
 * @author Christoph Deppisch
 */
public abstract class AbstractSpringBeanFilter implements LSSerializerFilter, LSParserFilter {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(AbstractSpringBeanFilter.class);

    /**
     * Checks for element equality by bean id attribute.
     * @param element
     * @param elementId
     * @return
     */
    protected boolean isEqualById(Element element, String elementId) {
        return element.hasAttribute("id") && element.getAttribute("id").equals(elementId);
    }

    /**
     * Checks for element equality by bean name attribute.
     * @param element
     * @param elementId
     * @return
     */
    protected boolean isEqualByBeanName(Element element, String elementId) {
        return element.hasAttribute("name") && element.getAttribute("name").equals(elementId);
    }

    /**
     * Checks for element equality by bean class name attribute.
     * @param element
     * @param elementClass
     * @return
     */
    protected boolean isEqualByBeanClass(Element element, String elementClass) {
        if (!element.hasAttribute("class")) {
            return false;
        }

        String beanClassName = element.getAttribute("class");

        if (beanClassName.equals(elementClass)) {
            return true;
        }

        try {
            Class targetClass = Class.forName(elementClass);
            Class sourceClass = Class.forName(beanClassName);
            return targetClass.isAssignableFrom(sourceClass);
        } catch (ClassNotFoundException e) {
            log.warn(String.format("Unable to check class inheritance for Spring bean types: %s + %s", beanClassName, elementClass));
        }

        return false;
    }

    /**
     * Checks for element equality by bean class name attribute.
     * @param element
     * @param attributes
     * @return
     */
    protected boolean isEqualByBeanAttributes(Element element, Map<String, String> attributes) {
        for (Map.Entry<String,String> attributeEntry : attributes.entrySet()) {
            if (!element.hasAttribute(attributeEntry.getKey())) {
                return false;
            }

            if (attributeEntry.getKey().equals("class") &&
                    isEqualByBeanClass(element, attributeEntry.getValue())) {
                continue;
            }

            if (!element.getAttribute(attributeEntry.getKey()).equals(attributeEntry.getValue())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks for element equality by namespace uri.
     * @param element
     * @param namespaceUri
     * @return
     */
    protected boolean isEqualByNamespace(Element element, String namespaceUri) {
        if (namespaceUri != null && element.getNamespaceURI() != null) {
            return element.getNamespaceURI().equals(namespaceUri);
        }

        return true;
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
        } else if (node.getNodeType() == Node.TEXT_NODE) {
            return acceptText((Text) node);
        }
        
        return NodeFilter.FILTER_ACCEPT;
    }
    
    /**
     * Accept text nodes such as whitespace nodes. Subclasses may decide to reject text nodes.
     * @param text
     * @return
     */
    public short acceptText(Text text) {
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
        return NodeFilter.SHOW_ALL;
    }
}
