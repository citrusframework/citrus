/*
 * Copyright 2006-2017 the original author or authors.
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

import org.citrusframework.selenium.actions.*;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class HoverActionParser extends FindElementActionParser {

    @Override
    protected void parseElement(BeanDefinitionBuilder beanDefinition, Element element, ParserContext parserContext) {
    }

    @Override
    protected Class<HoverActionFactoryBean> getBrowserActionClass() {
        return HoverActionFactoryBean.class;
    }

    /**
     * Test action factory bean.
     */
    public static final class HoverActionFactoryBean extends ElementActionFactoryBean<HoverAction, HoverAction.Builder> {

        private final HoverAction.Builder builder = new HoverAction.Builder();

        @Override
        public HoverAction getObject() throws Exception {
            return getObject(builder);
        }

        @Override
        public Class<?> getObjectType() {
            return HoverAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public HoverAction.Builder getBuilder() {
            return builder;
        }
    }
}
