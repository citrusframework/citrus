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

import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.traversal.NodeFilter;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchema;

/**
 * Filter searches for a Spring bean definition in a Spring XML application context. Bean definition is identified by its 
 * id or name attribute.
 * 
 * @author Christoph Deppisch
 */
public class GetSpringBeanFilter extends AbstractSpringBeanFilter {

    /** Bean definition id */
    private String id;

    /** Element type name */
    private final String elementName;

    /** Element namespace to look for */
    private String elementNamespace;
    
    /** Found bean definition element node */
    private Element beanDefinition;
    
    /**
     * Constructor using bean definition id as field.
     */
    public GetSpringBeanFilter(String id, Class<?> type) {
        this.elementName = type.getAnnotation(XmlRootElement.class).name();
        this.elementNamespace = type.getPackage().getAnnotation(XmlSchema.class).namespace();
        this.id = id;
    }
    
    /**
     * {@inheritDoc}
     */
    public short accept(Element element) {
        if (DomUtils.nodeNameEquals(element, elementName) &&
            isEqualByNamespace(element, elementNamespace) &&
            (isEqualById(element, id) || isEqualByBeanName(element, id))) {
            beanDefinition = element;
        }
        
        return NodeFilter.FILTER_ACCEPT;
    }

    /**
     * Gets the beanDefinition.
     * @return the beanDefinition the beanDefinition to get.
     */
    public Element getBeanDefinition() {
        return beanDefinition;
    }
}
