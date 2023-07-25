/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.selenium.config.xml;

import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.selenium.actions.*;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class WaitUntilActionParser extends FindElementActionParser {

    @Override
    protected void parseElement(BeanDefinitionBuilder beanDefinition, Element element, ParserContext parserContext) {
        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("until"), "condition");
    }

    @Override
    protected Class<WaitUntilActionFactoryBean> getBrowserActionClass() {
        return WaitUntilActionFactoryBean.class;
    }

    /**
     * Test action factory bean.
     */
    public static final class WaitUntilActionFactoryBean extends ElementActionFactoryBean<WaitUntilAction, WaitUntilAction.Builder> {

        private final WaitUntilAction.Builder builder = new WaitUntilAction.Builder();

        /**
         * Sets the timeout.
         * @param timeout
         */
        public void setTimeout(Long timeout) {
            builder.timeout(timeout);
        }

        /**
         * Sets the condition.
         * @param condition
         */
        public void setCondition(String condition) {
            builder.condition(condition);
        }

        @Override
        public WaitUntilAction getObject() throws Exception {
            return getObject(builder);
        }

        @Override
        public Class<?> getObjectType() {
            return WaitUntilAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public WaitUntilAction.Builder getBuilder() {
            return builder;
        }
    }
}
