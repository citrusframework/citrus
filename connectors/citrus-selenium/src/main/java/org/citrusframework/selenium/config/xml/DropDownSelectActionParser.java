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

import java.util.ArrayList;
import java.util.List;

import org.citrusframework.config.util.BeanDefinitionParserUtils;
import org.citrusframework.selenium.actions.DropDownSelectAction;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.CollectionUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class DropDownSelectActionParser extends FindElementActionParser {

    @Override
    protected void parseElement(BeanDefinitionBuilder beanDefinition, Element element, ParserContext parserContext) {
        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("option"), "option");

        List<String> options = new ArrayList<>();
        Element optionsElement = DomUtils.getChildElementByTagName(element, "options");
        if (optionsElement != null) {
            List<Element> optionElements = DomUtils.getChildElementsByTagName(optionsElement, "option");
            if (!CollectionUtils.isEmpty(optionElements)) {
                for (Element option : optionElements) {
                    options.add(option.getAttribute("name"));
                }
            }
        }

        beanDefinition.addPropertyValue("options", options);

    }

    @Override
    protected Class<DropDownSelectActionFactoryBean> getBrowserActionClass() {
        return DropDownSelectActionFactoryBean.class;
    }

    /**
     * Test action factory bean.
     */
    public static final class DropDownSelectActionFactoryBean extends ElementActionFactoryBean<DropDownSelectAction, DropDownSelectAction.Builder> {

        private final DropDownSelectAction.Builder builder = new DropDownSelectAction.Builder();

        /**
         * Sets the option.
         *
         * @param option
         */
        public void setOption(String option) {
            builder.option(option);
        }

        /**
         * Sets the options.
         *
         * @param options
         */
        public void setOptions(List<String> options) {
            builder.options(options);
        }

        @Override
        public DropDownSelectAction getObject() throws Exception {
            return getObject(builder);
        }

        @Override
        public Class<?> getObjectType() {
            return DropDownSelectAction.class;
        }

        /**
         * Obtains the builder.
         * @return the builder implementation.
         */
        @Override
        public DropDownSelectAction.Builder getBuilder() {
            return builder;
        }
    }
}
