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

package com.consol.citrus.selenium.config.xml;

import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import com.consol.citrus.selenium.actions.AbstractSeleniumAction;
import com.consol.citrus.selenium.actions.AlertAction;
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
    protected Class<? extends AbstractSeleniumAction> getBrowserActionClass() {
        return AlertAction.class;
    }
}
