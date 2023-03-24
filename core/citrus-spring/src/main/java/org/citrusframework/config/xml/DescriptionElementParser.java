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

package org.citrusframework.config.xml;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Bean definition parser for description elements of test actions.
 * 
 * @author Christoph Deppisch
 */
public abstract class DescriptionElementParser {
    
    /**
     * Prevent instantiation.
     */
    private DescriptionElementParser() {
    }
    
    /**
     * Static parse method taking care of test action description.
     * @param element
     * @param builder
     */
    public static void doParse(Element element, BeanDefinitionBuilder builder) {
        Element descriptionElement = DomUtils.getChildElementByTagName(element, "description");
        if (descriptionElement != null) {
            builder.addPropertyValue("description", DomUtils.getTextValue(descriptionElement).trim());
        }
    }
}
