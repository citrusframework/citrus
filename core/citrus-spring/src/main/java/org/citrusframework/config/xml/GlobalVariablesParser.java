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

package org.citrusframework.config.xml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.variable.GlobalVariables;
import org.citrusframework.variable.GlobalVariablesPropertyLoader;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * @author Christoph Deppisch
 * @since 1.4
 */
public class GlobalVariablesParser implements BeanDefinitionParser {

    /** Bean name in Spring application context */
    public static final String BEAN_NAME = "globalVariables";

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(GlobalVariablesFactoryBean.class);
        parseVariableDefinitions(builder, element);

        parserContext.getRegistry().registerBeanDefinition(BEAN_NAME, builder.getBeanDefinition());

        List<String> propertyFiles = new ArrayList<>();
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
        Map<String, String> testVariables = new LinkedHashMap<>();
        List<Element> variableElements = DomUtils.getChildElementsByTagName(element, "variable");
        for (Element variableDefinition : variableElements) {
            testVariables.put(variableDefinition.getAttribute("name"), variableDefinition.getAttribute("value"));
        }

        if (!testVariables.isEmpty()) {
            builder.addPropertyValue("variables", testVariables);
        }
    }

    /**
     * Factory bean.
     */
    public static class GlobalVariablesFactoryBean implements FactoryBean<GlobalVariables> {

        private final GlobalVariables.Builder builder = new GlobalVariables.Builder();

        @Override
        public GlobalVariables getObject() throws Exception {
            return builder.build();
        }

        /**
         * Set the global variables.
         * @param variables the variables to set
         */
        public void setVariables(Map<String, Object> variables) {
            this.builder.variables(variables);
        }

        @Override
        public Class<?> getObjectType() {
            return GlobalVariables.class;
        }
    }
}
