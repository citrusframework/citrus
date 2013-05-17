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

/**
 * Filter adds a new Spring bean definition to the very end of the beans section in a 
 * Spring XML application context.
 * 
 * @author Christoph Deppisch
 */
public class AddSpringBeanFilter extends AbstractSpringBeanFilter {

    /** Bean definition element to add */
    private Element beanDefinition;
    
    /**
     * Constructor using element XML fragment as field.
     */
    public AddSpringBeanFilter(Element beanDefinition) {
        this.beanDefinition = beanDefinition;
    }
    
    /**
     * {@inheritDoc}
     */
    public short accept(Element element) {
        if (DomUtils.nodeNameEquals(element, "beans")) {
            element.appendChild(element.getOwnerDocument().createTextNode("\n    ")); //TODO make indentation configurable
            element.appendChild(element.getOwnerDocument().importNode(beanDefinition, true));
        }
        
        return NodeFilter.FILTER_ACCEPT;
    }

}
