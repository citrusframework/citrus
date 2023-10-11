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

import org.citrusframework.script.GroovyAction;
import org.citrusframework.util.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Bean definition parser for groovy action in test case.
 *
 * @author Christoph Deppisch
 */
public class GroovyActionParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(GroovyActionFactoryBean.class);

        DescriptionElementParser.doParse(element, beanDefinition);

        String useScriptTemplate = element.getAttribute("use-script-template");
        if (StringUtils.hasText(useScriptTemplate)) {
            beanDefinition.addPropertyValue("useScriptTemplate", Boolean.valueOf(useScriptTemplate));
        }

        String scriptTemplatePath = element.getAttribute("script-template");
        if (StringUtils.hasText(scriptTemplatePath)) {
            beanDefinition.addPropertyValue("scriptTemplatePath", scriptTemplatePath);
        }

        if (DomUtils.getTextValue(element).length() > 0) {
            beanDefinition.addPropertyValue("script", DomUtils.getTextValue(element));
        }

        String filePath = element.getAttribute("resource");
        if (StringUtils.hasText(filePath)) {
            beanDefinition.addPropertyValue("scriptResourcePath", filePath);
        }

        return beanDefinition.getBeanDefinition();
    }

    /**
     * Test action factory bean.
     */
    public static class GroovyActionFactoryBean extends AbstractTestActionFactoryBean<GroovyAction, GroovyAction.Builder> {

        private final GroovyAction.Builder builder = new GroovyAction.Builder();

        /**
         * Set the groovy script code.
         * @param script the script to set
         */
        public void setScript(String script) {
            builder.script(script);
        }

        /**
         * Set file resource.
         * @param fileResource the fileResource to set
         */
        public void setScriptResourcePath(String fileResource) {
            builder.scriptResourcePath(fileResource);
        }

        /**
         * Sets the script template.
         * @param scriptTemplate
         */
        public void setScriptTemplate(String scriptTemplate) {
            builder.template(scriptTemplate);
        }

        /**
         * Set the script template resource.
         * @param scriptTemplate the scriptTemplate to set
         */
        public void setScriptTemplatePath(String scriptTemplate) {
            builder.template(scriptTemplate);
        }

        /**
         * Prevent script template usage if false.
         * @param useScriptTemplate the useScriptTemplate to set
         */
        public void setUseScriptTemplate(boolean useScriptTemplate) {
            builder.useScriptTemplate(useScriptTemplate);
        }

        @Override
        public GroovyAction getObject() throws Exception {
            return builder.build();
        }

        @Override
        public Class<?> getObjectType() {
            return GroovyAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public GroovyAction.Builder getBuilder() {
            return builder;
        }
    }
}
