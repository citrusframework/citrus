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

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.selenium.actions.PageAction;
import org.citrusframework.selenium.model.PageValidator;
import org.citrusframework.selenium.model.WebPage;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class PageActionParser extends AbstractBrowserActionParser {

    @Override
    protected void parseAction(BeanDefinitionBuilder beanDefinition, Element element, ParserContext parserContext) {
        BeanDefinitionParserUtils.setPropertyReference(beanDefinition, element.getAttribute("name"), "page");
        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("type"), "type");
        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("action"), "action");
        BeanDefinitionParserUtils.setPropertyReference(beanDefinition, element.getAttribute("validator"), "validator");

        List<String> arguments = new ArrayList<>();
        Element argumentsContainer = DomUtils.getChildElementByTagName(element, "arguments");
        if (argumentsContainer != null) {
            List<Element> argumentElements = DomUtils.getChildElementsByTagName(argumentsContainer, "argument");
            for (Element argument : argumentElements) {
                arguments.add(DomUtils.getTextValue(argument));
            }

            beanDefinition.addPropertyValue("arguments", arguments);
        }
    }

    @Override
    protected Class<PageActionFactoryBean> getBrowserActionClass() {
        return PageActionFactoryBean.class;
    }

    /**
     * Test action factory bean.
     */
    public static class PageActionFactoryBean extends AbstractSeleniumActionFactoryBean<PageAction, PageAction.Builder> {

        private final PageAction.Builder builder = new PageAction.Builder();

        /**
         * Sets the page.
         *
         * @param page
         */
        public void setPage(WebPage page) {
            builder.page(page);
        }

        /**
         * Sets the action.
         *
         * @param action
         */
        public void setAction(String action) {
            builder.action(action);
        }

        /**
         * Sets the validator.
         *
         * @param validator
         */
        public void setValidator(PageValidator validator) {
            builder.validator(validator);
        }

        /**
         * Sets the type.
         *
         * @param type
         */
        public void setType(String type) {
            builder.type(type);
        }

        /**
         * Sets the arguments.
         *
         * @param arguments
         */
        public void setArguments(List<String> arguments) {
            builder.arguments(arguments);
        }

        @Override
        public PageAction getObject() throws Exception {
            return builder.build();
        }

        @Override
        public Class<?> getObjectType() {
            return PageAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public PageAction.Builder getBuilder() {
            return builder;
        }
    }
}
