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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.w3c.dom.Element;
import org.w3c.dom.traversal.NodeFilter;

/**
 * Filter searches for all Spring bean definitions of specific type in a Spring XML application context. Bean definition type is 
 * identified by JaxB root element annotation name. Element name must match this annotation value in order to qualify for this filter.
 * 
 * @author Christoph Deppisch
 */
public class GetSpringBeansFilter extends AbstractSpringBeanFilter {

    /** Element type */
    private String elementName;
    
    /** Found bean definition element nodes */
    private List<Element> beanDefinitions = new ArrayList<Element>();
    
    /**
     * Constructor using bean definition id as field.
     */
    public GetSpringBeansFilter(Class<?> type) {
        XmlRootElement beanTypeAnnotation = type.getAnnotation(XmlRootElement.class);
        this.elementName = beanTypeAnnotation.name();
    }
    
    /**
     * {@inheritDoc}
     */
    public short accept(Element element) {
        if (element.getLocalName().equals(elementName)) {
            beanDefinitions.add(element);
        }
        
        return NodeFilter.FILTER_ACCEPT;
    }

    /**
     * Gets the beanDefinitions.
     * @return the beanDefinitions the beanDefinitions to get.
     */
    public List<Element> getBeanDefinitions() {
        return beanDefinitions;
    }
}
