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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.citrusframework.actions.CreateVariablesAction;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Bean definition parser for create-variables action in test case.
 *
 * @author Christoph Deppisch
 */
public class CreateVariablesActionParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(CreateVariablesActionFactoryBean.class);

        DescriptionElementParser.doParse(element, beanDefinition);

        Map<String, String> variables = new LinkedHashMap<String, String>();
        List<Element> variableElements = DomUtils.getChildElementsByTagName(element, "variable");
        for (Element variable : variableElements) {
            Element variableValueElement = DomUtils.getChildElementByTagName(variable, "value");
            if (variableValueElement == null) {
                variables.put(variable.getAttribute("name"), variable.getAttribute("value"));
            } else {
                Element variableScript = DomUtils.getChildElementByTagName(variableValueElement, "script");
                if (variableScript != null) {
                    String scriptEngine = variableScript.getAttribute("type");
                    variables.put(variable.getAttribute("name"), "script:<" + scriptEngine + ">" + variableScript.getTextContent());
                }

                Element variableData = DomUtils.getChildElementByTagName(variableValueElement, "data");
                if (variableData != null) {
                    variables.put(variable.getAttribute("name"), DomUtils.getTextValue(variableData).trim());
                }
            }
        }
        beanDefinition.addPropertyValue("variables", variables);

        return beanDefinition.getBeanDefinition();
    }

    /**
     * Test action factory bean.
     */
    public static class CreateVariablesActionFactoryBean extends AbstractTestActionFactoryBean<CreateVariablesAction, CreateVariablesAction.Builder> {

        private final CreateVariablesAction.Builder builder = new CreateVariablesAction.Builder();

        @Override
        public CreateVariablesAction getObject() throws Exception {
            return builder.build();
        }

        public void setVariables(Map<String, String> variables) {
            variables.forEach(builder::variable);
        }

        @Override
        public Class<?> getObjectType() {
            return CreateVariablesAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public CreateVariablesAction.Builder getBuilder() {
            return builder;
        }
    }
}
