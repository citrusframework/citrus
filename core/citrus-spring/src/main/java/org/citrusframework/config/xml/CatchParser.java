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

import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.container.Catch;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Bean definition parser for catch action in test case.
 *
 * @author Christoph Deppisch
 */
public class CatchParser implements BeanDefinitionParser {

    @Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(CatchFactoryBean.class);

        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("exception"), "exception");

        DescriptionElementParser.doParse(element, builder);
        ActionContainerParser.doParse(element, parserContext, builder);

        return builder.getBeanDefinition();
    }

    /**
     * Test action factory bean.
     */
    public static class CatchFactoryBean extends AbstractTestContainerFactoryBean<Catch, Catch.Builder> {

        private final Catch.Builder builder = new Catch.Builder();

        /**
         * Set the exception that is caught.
         * @param exception the exception to set
         */
        public void setException(String exception) {
            this.builder.exception(exception);
        }

        @Override
        public Catch getObject() throws Exception {
            return getObject(builder.build());
        }

        @Override
        public Class<?> getObjectType() {
            return Catch.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public Catch.Builder getBuilder() {
            return builder;
        }
    }
}
