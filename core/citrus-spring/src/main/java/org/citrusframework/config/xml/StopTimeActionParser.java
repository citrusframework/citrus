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

import org.citrusframework.actions.StopTimeAction;
import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Bean definition parser for time action in test case.
 *
 * @author Christoph Deppisch
 */
public class StopTimeActionParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(StopTimeActionFactoryBean.class);

        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("id"), "id");
        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("suffix"), "suffix");

        DescriptionElementParser.doParse(element, beanDefinition);

        return beanDefinition.getBeanDefinition();
    }

    /**
     * Test action factory bean.
     */
    public static class StopTimeActionFactoryBean extends AbstractTestActionFactoryBean<StopTimeAction, StopTimeAction.Builder> {

        private final StopTimeAction.Builder builder = new StopTimeAction.Builder();

        /**
         * Setter for timeline id.
         * @param id
         */
        public void setId(String id) {
            builder.id(id);
        }

        /**
         * Sets the suffix.
         * @param suffix
         */
        public void setSuffix(String suffix) {
            builder.suffix(suffix);
        }

        @Override
        public StopTimeAction getObject() throws Exception {
            return builder.build();
        }

        @Override
        public Class<?> getObjectType() {
            return StopTimeAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public StopTimeAction.Builder getBuilder() {
            return builder;
        }
    }
}
