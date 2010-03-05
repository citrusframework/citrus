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
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import com.consol.citrus.container.Template;

/**
 * Bean definition parser for template definition in test case.
 * 
 * @author Christoph Deppisch
 */
public class TemplateParser implements BeanDefinitionParser {

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
	public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(Template.class);

        DescriptionElementParser.doParse(element, builder);

        String name = element.getAttribute("name");

        if (StringUtils.hasText(name)) {
            builder.addPropertyValue("name", name);
        } else {
            builder.addPropertyValue("name", parserContext.getReaderContext().generateBeanName(builder.getBeanDefinition()));
        }

        String globalContext = element.getAttribute("global-context");
        if (StringUtils.hasText(globalContext)) {
            builder.addPropertyValue("globalContext", globalContext);
        }
        
        builder.addPropertyValue("name", element.getLocalName() + "(" + element.getAttribute("name") + ")");
        
        ActionContainerParser.doParse(element, parserContext, builder);
        
        parserContext.getRegistry().registerBeanDefinition(name, builder.getBeanDefinition());
        return parserContext.getRegistry().getBeanDefinition(name);
    }
}
