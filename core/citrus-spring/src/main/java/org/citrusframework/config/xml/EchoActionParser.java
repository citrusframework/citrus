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

import org.citrusframework.actions.EchoAction;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Bean definition parser for echo action in test case.
 *
 * @author Christoph Deppisch
 */
public class EchoActionParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(EchoActionFactoryBean.class);

        DescriptionElementParser.doParse(element, beanDefinition);

        Element messageElement = DomUtils.getChildElementByTagName(element, "message");
        if (messageElement != null) {
            beanDefinition.addPropertyValue("message", DomUtils.getTextValue(messageElement));
        }

        return beanDefinition.getBeanDefinition();
    }

    /**
     * Test action factory bean.
     */
    public static class EchoActionFactoryBean extends AbstractTestActionFactoryBean<EchoAction, EchoAction.Builder> {

        private final EchoAction.Builder builder = new EchoAction.Builder();

        public void setMessage(String message) {
            builder.message(message);
        }

        @Override
        public EchoAction getObject() throws Exception {
            return builder.build();
        }

        @Override
        public Class<?> getObjectType() {
            return EchoAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public EchoAction.Builder getBuilder() {
            return builder;
        }
    }
}
