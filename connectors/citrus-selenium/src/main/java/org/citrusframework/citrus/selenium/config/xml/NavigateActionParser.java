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

import org.citrusframework.citrus.config.util.BeanDefinitionParserUtils;
import org.citrusframework.citrus.selenium.actions.NavigateAction;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class NavigateActionParser extends AbstractBrowserActionParser {

    @Override
    protected void parseAction(BeanDefinitionBuilder beanDefinition, Element element, ParserContext parserContext) {
        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("page"), "page");
    }

    @Override
    protected Class<NavigateActionFactoryBean> getBrowserActionClass() {
        return NavigateActionFactoryBean.class;
    }

    /**
     * Test action factory bean.
     */
    public static class NavigateActionFactoryBean extends AbstractSeleniumActionFactoryBean<NavigateAction, NavigateAction.Builder> {

        private final NavigateAction.Builder builder = new NavigateAction.Builder();

        /**
         * Sets the page url.
         * @param page
         */
        public void setPage(String page) {
            builder.page(page);
        }

        @Override
        public NavigateAction getObject() throws Exception {
            return builder.build();
        }

        @Override
        public Class<?> getObjectType() {
            return NavigateAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public NavigateAction.Builder getBuilder() {
            return builder;
        }
    }
}
