/*
 * Copyright 2006-2014 the original author or authors.
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

import com.consol.citrus.variable.GlobalVariables;
import com.consol.citrus.variable.GlobalVariablesPropertyLoader;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.*;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class GlobalVariablesParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(GlobalVariables.class);
        parseVariableDefinitions(builder, element);

        parserContext.getRegistry().registerBeanDefinition(GlobalVariables.BEAN_NAME, builder.getBeanDefinition());

        List<String> propertyFiles = new ArrayList<String>();
        List<Element> propertyFileElements = DomUtils.getChildElementsByTagName(element, "file");
        for (Element propertyFileElement : propertyFileElements) {
            propertyFiles.add(propertyFileElement.getAttribute("path"));
        }

        if (!propertyFiles.isEmpty()) {
            BeanDefinitionBuilder variablesPropertyLoader = BeanDefinitionBuilder.rootBeanDefinition(GlobalVariablesPropertyLoader.class);
            variablesPropertyLoader.addPropertyValue("propertyFiles", propertyFiles);
            parserContext.getRegistry().registerBeanDefinition(GlobalVariablesPropertyLoader.BEAN_NAME, variablesPropertyLoader.getBeanDefinition());
        }

        return null;
    }

    /**
     * Parses all variable definitions and adds those to the bean definition
     * builder as property value.
     * @param builder the target bean definition builder.
     * @param element the source element.
     */
    private void parseVariableDefinitions(BeanDefinitionBuilder builder, Element element) {
        Map<String, String> testVariables = new LinkedHashMap<String, String>();
        List<Element> variableElements = DomUtils.getChildElementsByTagName(element, "variable");
        for (Element variableDefinition : variableElements) {
            testVariables.put(variableDefinition.getAttribute("name"), variableDefinition.getAttribute("value"));
        }

        if (!testVariables.isEmpty()) {
            builder.addPropertyValue("variables", testVariables);
        }
    }
}
