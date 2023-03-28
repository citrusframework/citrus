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

import org.citrusframework.container.RepeatUntilTrue;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Bean definition parser for repeat-until-true container in test case.
 *
 * @author Christoph Deppisch
 */
public class RepeatUntilTrueParser extends AbstractIterationTestActionParser {

    @Override
	public BeanDefinitionBuilder parseComponent(Element element, ParserContext parserContext) {
        return BeanDefinitionBuilder.rootBeanDefinition(RepeatUntilTrueFactoryBean.class);
    }

    /**
     * Test action factory bean.
     */
    public static class RepeatUntilTrueFactoryBean extends AbstractIteratingTestContainerFactoryBean<RepeatUntilTrue, RepeatUntilTrue.Builder> {

        private final RepeatUntilTrue.Builder builder = new RepeatUntilTrue.Builder();

        @Override
        public RepeatUntilTrue getObject() throws Exception {
            return getObject(builder.build());
        }

        @Override
        public Class<?> getObjectType() {
            return RepeatUntilTrue.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public RepeatUntilTrue.Builder getBuilder() {
            return builder;
        }
    }
}
