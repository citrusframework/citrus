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

import java.util.List;
import java.util.Map;

import org.citrusframework.TestAction;
import org.citrusframework.container.Template;
import org.citrusframework.util.StringUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Bean definition parser for template definition in test case.
 *
 * @author Christoph Deppisch
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class TemplateParser implements BeanDefinitionParser {

    @Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(TemplateFactoryBean.class);

        DescriptionElementParser.doParse(element, builder);

        String name = element.getAttribute("name");
        if (!StringUtils.hasText(name)) {
            throw new BeanCreationException("Must specify proper template name attribute");
        }

        builder.addPropertyValue("name", element.getLocalName() + "(" + element.getAttribute("name") + ")");
        builder.addPropertyValue("templateName", element.getAttribute("name"));

        String globalContext = element.getAttribute("global-context");
        if (StringUtils.hasText(globalContext)) {
            builder.addPropertyValue("globalContext", globalContext);
        }

        ActionContainerParser.doParse(element, parserContext, builder);

        parserContext.getRegistry().registerBeanDefinition(name, builder.getBeanDefinition());
        return parserContext.getRegistry().getBeanDefinition(name);
    }

    /**
     * Test action factory bean.
     */
    public static class TemplateFactoryBean extends AbstractTestActionFactoryBean<Template, Template.Builder> {

        private final Template.Builder builder = new Template.Builder();

        /**
         * Sets the template name.
         * @param templateName
         */
        public void setTemplateName(String templateName) {
            builder.templateName(templateName);
        }

        /**
         * Sets the test actions.
         * @param actions
         */
        public void setActions(List<TestAction> actions) {
            builder.actions(actions);
        }

        /**
         * Set parameter before execution.
         * @param parameter the parameter to set
         */
        public void setParameter(Map<String, String> parameter) {
            builder.parameters(parameter);
        }

        /**
         * Boolean flag marking the template variables should also affect
         * variables in test case.
         * @param globalContext the globalContext to set
         */
        public void setGlobalContext(boolean globalContext) {
            builder.globalContext(globalContext);
        }

        /**
         * Adds test actions to container when building object.
         * @return
         * @throws Exception
         */
        public Template getObject() throws Exception {
            return builder.build();
        }

        @Override
        public Class<?> getObjectType() {
            return Template.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public Template.Builder getBuilder() {
            return builder;
        }
    }
}
