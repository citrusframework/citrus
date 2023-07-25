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
import org.citrusframework.selenium.actions.SetInputAction;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class SetInputActionParser extends FindElementActionParser {

    @Override
    protected void parseElement(BeanDefinitionBuilder beanDefinition, Element element, ParserContext parserContext) {
        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("value"), "value");
    }

    @Override
    protected Class<SetInputActionFactoryBean> getBrowserActionClass() {
        return SetInputActionFactoryBean.class;
    }

    /**
     * Test action factory bean.
     */
    public static final class SetInputActionFactoryBean extends ElementActionFactoryBean<SetInputAction, SetInputAction.Builder> {

        private final SetInputAction.Builder builder = new SetInputAction.Builder();

        /**
         * Sets the value.
         * @param value
         */
        public void setValue(String value) {
            builder.value(value);
        }

        @Override
        public SetInputAction getObject() throws Exception {
            return getObject(builder);
        }

        @Override
        public Class<?> getObjectType() {
            return SetInputAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public SetInputAction.Builder getBuilder() {
            return builder;
        }
    }
}
