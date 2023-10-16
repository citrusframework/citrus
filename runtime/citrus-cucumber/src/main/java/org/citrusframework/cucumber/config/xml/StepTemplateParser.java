/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.cucumber.config.xml;

import java.util.List;
import java.util.regex.Pattern;

import org.citrusframework.TestAction;
import org.citrusframework.config.xml.AbstractTestActionFactoryBean;
import org.citrusframework.config.xml.ActionContainerParser;
import org.citrusframework.config.xml.DescriptionElementParser;
import org.citrusframework.cucumber.container.StepTemplate;
import org.citrusframework.util.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Parser configures a step template with pattern and parameter names.
 * @author Christoph Deppisch
 * @since 2.6
 */
public class StepTemplateParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(StepTemplateFactoryBean.class);

        DescriptionElementParser.doParse(element, builder);

        if (element.hasAttribute("given")) {
            builder.addPropertyValue("name", element.getLocalName() + "(given)");
            builder.addPropertyValue("pattern", Pattern.compile(element.getAttribute("given")));
        } else if (element.hasAttribute("when")) {
            builder.addPropertyValue("name", element.getLocalName() + "(when)");
            builder.addPropertyValue("pattern", Pattern.compile(element.getAttribute("when")));
        } else if (element.hasAttribute("then")) {
            builder.addPropertyValue("name", element.getLocalName() + "(then)");
            builder.addPropertyValue("pattern", Pattern.compile(element.getAttribute("then")));
        }

        if (element.hasAttribute("parameter-names")) {
            builder.addPropertyValue("parameterNames", element.getAttribute("parameter-names").split(","));
        }

        String globalContext = element.getAttribute("global-context");
        if (StringUtils.hasText(globalContext)) {
            builder.addPropertyValue("globalContext", globalContext);
        }

        ActionContainerParser.doParse(element, parserContext, builder);

        String beanName = parserContext.getReaderContext().generateBeanName(builder.getBeanDefinition());
        parserContext.getRegistry().registerBeanDefinition(beanName, builder.getBeanDefinition());
        return parserContext.getRegistry().getBeanDefinition(beanName);
    }

    /**
     * Test action factory bean.
     */
    public static class StepTemplateFactoryBean extends AbstractTestActionFactoryBean<StepTemplate, StepTemplate.Builder> {

        private final StepTemplate.Builder builder = new StepTemplate.Builder();

        /**
         * Sets the pattern property.
         *
         * @param pattern
         */
        public void setPattern(Pattern pattern) {
            builder.pattern(pattern);
        }

        /**
         * Sets the parameterNames property.
         *
         * @param parameterNames
         */
        public void setParameterNames(List<String> parameterNames) {
            builder.parameterNames(parameterNames);
        }

        /**
         * Sets the test actions.
         * @param actions
         */
        public void setActions(List<TestAction> actions) {
            builder.actions(actions);
        }

        /**
         * Adds test actions to container when building object.
         * @return
         * @throws Exception
         */
        public StepTemplate getObject() throws Exception {
            return builder.build();
        }

        @Override
        public Class<?> getObjectType() {
            return StepTemplate.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public StepTemplate.Builder getBuilder() {
            return builder;
        }
    }
}
