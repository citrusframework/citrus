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

import org.citrusframework.actions.FailAction;
import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Bean definition parser for fail action in test case.
 *
 * @author Christoph Deppisch
 */
public class FailActionParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(FailActionFactoryBean.class);

        DescriptionElementParser.doParse(element, beanDefinition);
        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("message"), "message");

        return beanDefinition.getBeanDefinition();
    }

    /**
     * Test action factory bean.
     */
    public static class FailActionFactoryBean extends AbstractTestActionFactoryBean<FailAction, FailAction.Builder> {

        private final FailAction.Builder builder = new FailAction.Builder();

        /**
         * Setter for user defined cause message.
         * @param message the failure cause message.
         */
        public void setMessage(String message) {
            builder.message(message);
        }

        @Override
        public FailAction getObject() throws Exception {
            return builder.build();
        }

        @Override
        public Class<?> getObjectType() {
            return FailAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public FailAction.Builder getBuilder() {
            return builder;
        }
    }
}
