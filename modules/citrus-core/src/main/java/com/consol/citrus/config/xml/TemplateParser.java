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

package com.consol.citrus.config.xml;

import org.springframework.beans.factory.BeanCreationException;
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
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class TemplateParser implements BeanDefinitionParser {

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
	public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(Template.class);

        DescriptionElementParser.doParse(element, builder);

        String name = element.getAttribute("name");
        if (!StringUtils.hasText(name)) {
            throw new BeanCreationException("Must specify proper template name attribute");
        }

        builder.addPropertyValue("name", element.getLocalName() + "(" + element.getAttribute("name") + ")");

        String globalContext = element.getAttribute("global-context");
        if (StringUtils.hasText(globalContext)) {
            builder.addPropertyValue("globalContext", globalContext);
        }

        ActionContainerParser.doParse(element, parserContext, builder);
        
        parserContext.getRegistry().registerBeanDefinition(name, builder.getBeanDefinition());
        return parserContext.getRegistry().getBeanDefinition(name);
    }
}
