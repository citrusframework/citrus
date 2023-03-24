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
import org.citrusframework.container.RepeatOnErrorUntilTrue;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Bean definition parser for repeat-on-error-until-true container in test case.
 *
 * @author Christoph Deppisch
 */
public class RepeatOnErrorUntilTrueParser extends AbstractIterationTestActionParser {

    @Override
	public BeanDefinitionBuilder parseComponent(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(RepeatOnErrorUntilTrueFactoryBean.class);

        BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("auto-sleep"), "autoSleep");

        return builder;
    }

    /**
     * Test action factory bean.
     */
    public static class RepeatOnErrorUntilTrueFactoryBean extends AbstractIteratingTestContainerFactoryBean<RepeatOnErrorUntilTrue, RepeatOnErrorUntilTrue.Builder> {

        private final RepeatOnErrorUntilTrue.Builder builder = new RepeatOnErrorUntilTrue.Builder();

        /**
         * Setter for auto sleep time (in milliseconds).
         * @param autoSleep
         */
        public void setAutoSleep(Long autoSleep) {
            this.builder.autoSleep(autoSleep);
        }

        @Override
        public RepeatOnErrorUntilTrue getObject() throws Exception {
            return getObject(builder.build());
        }

        @Override
        public Class<?> getObjectType() {
            return RepeatOnErrorUntilTrue.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public RepeatOnErrorUntilTrue.Builder getBuilder() {
            return builder;
        }
    }
}
