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

import org.citrusframework.actions.LoadPropertiesAction;
import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Bean definition parser for load-properties action in test case.
 *
 * @author Christoph Deppisch
 */
public class LoadPropertiesActionParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(LoadPropertiesActionFactoryBean.class);

        DescriptionElementParser.doParse(element, beanDefinition);

        Element propertiesElement = DomUtils.getChildElementByTagName(element, "properties");
        if (propertiesElement != null) {
            BeanDefinitionParserUtils.setPropertyValue(beanDefinition, propertiesElement.getAttribute("file"), "filePath");
        } else {
            throw new BeanCreationException("Missing properties file definition for load action");
        }

        return beanDefinition.getBeanDefinition();
    }

    /**
     * Test action factory bean.
     */
    public static class LoadPropertiesActionFactoryBean extends AbstractTestActionFactoryBean<LoadPropertiesAction, LoadPropertiesAction.Builder> {

        private final LoadPropertiesAction.Builder builder = new LoadPropertiesAction.Builder();

        /**
         * File path setter.
         * @param file the file to set
         */
        public void setFilePath(String file) {
            builder.filePath(file);
        }

        @Override
        public LoadPropertiesAction getObject() throws Exception {
            return builder.build();
        }

        @Override
        public Class<?> getObjectType() {
            return LoadPropertiesAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public LoadPropertiesAction.Builder getBuilder() {
            return builder;
        }
    }
}
