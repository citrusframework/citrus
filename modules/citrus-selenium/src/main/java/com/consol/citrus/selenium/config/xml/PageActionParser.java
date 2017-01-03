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

package com.consol.citrus.selenium.config.xml;

import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import com.consol.citrus.selenium.actions.*;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

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
    protected Class<? extends AbstractSeleniumAction> getBrowserActionClass() {
        return PageAction.class;
    }
}
