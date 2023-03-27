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

package org.citrusframework.citrus.selenium.config.xml;

import org.citrusframework.citrus.selenium.actions.StartBrowserAction;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class StartBrowserActionParser extends AbstractBrowserActionParser {

    @Override
    protected void parseAction(BeanDefinitionBuilder beanDefinition, Element element, ParserContext parserContext) {
    }

    @Override
    protected Class<StartBrowserActionFactoryBean> getBrowserActionClass() {
        return StartBrowserActionFactoryBean.class;
    }

    /**
     * Test action factory bean.
     */
    public static final class StartBrowserActionFactoryBean extends AbstractSeleniumActionFactoryBean<StartBrowserAction, StartBrowserAction.Builder> {

        private final StartBrowserAction.Builder builder = new StartBrowserAction.Builder();

        /**
         * Sets the already started rules.
         * @param allowAlreadyStarted
         */
        public void setAllowAlreadyStarted(boolean allowAlreadyStarted) {
            builder.allowAlreadyStarted(allowAlreadyStarted);
        }

        @Override
        public StartBrowserAction getObject() throws Exception {
            return builder.build();
        }

        @Override
        public Class<?> getObjectType() {
            return StartBrowserAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public StartBrowserAction.Builder getBuilder() {
            return builder;
        }
    }
}
