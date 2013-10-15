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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
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

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(GroovyAction.class);
        
        DescriptionElementParser.doParse(element, beanDefinition);
        
        String useScriptTemplate = element.getAttribute("use-script-template");
        if (StringUtils.hasText(useScriptTemplate)) {
            beanDefinition.addPropertyValue("useScriptTemplate", Boolean.valueOf(useScriptTemplate));
        }
        
        String scriptTemplatePath = element.getAttribute("script-template");
        if (StringUtils.hasText(scriptTemplatePath)) {
            beanDefinition.addPropertyValue("scriptTemplatePath", scriptTemplatePath);
        }
        
        if (DomUtils.getTextValue(element) != null && DomUtils.getTextValue(element).length() > 0) {
            beanDefinition.addPropertyValue("script", DomUtils.getTextValue(element));
        }
        
        String filePath = element.getAttribute("resource");
        if (StringUtils.hasText(filePath)) {
            beanDefinition.addPropertyValue("scriptResourcePath", filePath);
        }
        
        return beanDefinition.getBeanDefinition();
    }
}
