/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.config.xml;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.consol.citrus.script.GroovyAction;

/**
 * Bean definition parser for groovy action in test case.
 * 
 * @author Christoph Deppisch
 */
public class GroovyActionParser implements BeanDefinitionParser {

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(GroovyAction.class);
        
        DescriptionElementParser.doParse(element, beanDefinition);
        
        if(DomUtils.getTextValue(element) != null && DomUtils.getTextValue(element).length() > 0) {
            beanDefinition.addPropertyValue("script", DomUtils.getTextValue(element));
        }
        
        String filePath = element.getAttribute("resource");
        if (StringUtils.hasText(filePath)) {
            if (filePath.startsWith("classpath:")) {
                beanDefinition.addPropertyValue("fileResource", new ClassPathResource(filePath.substring("classpath:".length())));
            } else if (filePath.startsWith("file:")) {
                beanDefinition.addPropertyValue("fileResource", new FileSystemResource(filePath.substring("file:".length())));
            } else {
                beanDefinition.addPropertyValue("fileResource", new FileSystemResource(filePath));
            }
        }
        
        return beanDefinition.getBeanDefinition();
    }
}
