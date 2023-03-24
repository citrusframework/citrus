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

import org.citrusframework.container.Parallel;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Bean definition parser for parallel container in test case.
 *
 * @author Christoph Deppisch
 */
public class ParallelParser implements BeanDefinitionParser {

    @Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ParallelFactoryBean.class);

        DescriptionElementParser.doParse(element, builder);
        ActionContainerParser.doParse(element, parserContext, builder);

        return builder.getBeanDefinition();
    }

    /**
     * Test action factory bean.
     */
    public static class ParallelFactoryBean extends AbstractTestContainerFactoryBean<Parallel, Parallel.Builder> {

        private final Parallel.Builder builder = new Parallel.Builder();

        @Override
        public Parallel getObject() throws Exception {
            return getObject(builder.build());
        }

        @Override
        public Class<?> getObjectType() {
            return Parallel.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public Parallel.Builder getBuilder() {
            return builder;
        }
    }
}
