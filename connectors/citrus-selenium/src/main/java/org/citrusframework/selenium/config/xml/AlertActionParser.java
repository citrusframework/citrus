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
import org.citrusframework.selenium.actions.AlertAction;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class AlertActionParser extends AbstractBrowserActionParser {

    @Override
    protected void parseAction(BeanDefinitionBuilder beanDefinition, Element element, ParserContext parserContext) {
        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("accept"), "accept");
        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("text"), "text");

        Element textElement = DomUtils.getChildElementByTagName(element, "alert-text");
        if (textElement != null) {
            BeanDefinitionParserUtils.setPropertyValue(beanDefinition, DomUtils.getTextValue(textElement), "text");
        }
    }

    @Override
    protected Class<AlertActionFactoryBean> getBrowserActionClass() {
        return AlertActionFactoryBean.class;
    }

    /**
     * Test action factory bean.
     */
    public static class AlertActionFactoryBean extends AbstractSeleniumActionFactoryBean<AlertAction, AlertAction.Builder> {

        private final AlertAction.Builder builder = new AlertAction.Builder();

        /**
         * Sets the accept.
         *
         * @param accept
         */
        public void setAccept(boolean accept) {
            if (accept) {
                builder.accept();
            } else {
                builder.dismiss();
            }
        }

        /**
         * Sets the text.
         *
         * @param text
         */
        public void setText(String text) {
            builder.text(text);
        }

        @Override
        public AlertAction getObject() throws Exception {
            return builder.build();
        }

        @Override
        public Class<?> getObjectType() {
            return AlertAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public AlertAction.Builder getBuilder() {
            return builder;
        }
    }
}
