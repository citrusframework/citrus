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
import org.citrusframework.selenium.actions.OpenWindowAction;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class OpenWindowActionParser extends AbstractBrowserActionParser {

    @Override
    protected void parseAction(BeanDefinitionBuilder beanDefinition, Element element, ParserContext parserContext) {
        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("name"), "windowName");
    }

    @Override
    protected Class<OpenWindowActionFactoryBean> getBrowserActionClass() {
        return OpenWindowActionFactoryBean.class;
    }

    /**
     * Test action factory bean.
     */
    public static final class OpenWindowActionFactoryBean extends AbstractSeleniumActionFactoryBean<OpenWindowAction, OpenWindowAction.Builder> {

        private final OpenWindowAction.Builder builder = new OpenWindowAction.Builder();

        /**
         * Sets the windowName.
         * @param windowName
         */
        public void setWindowName(String windowName) {
            builder.window(windowName);
        }

        @Override
        public OpenWindowAction getObject() throws Exception {
            return builder.build();
        }

        @Override
        public Class<?> getObjectType() {
            return OpenWindowAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public OpenWindowAction.Builder getBuilder() {
            return builder;
        }
    }
}
