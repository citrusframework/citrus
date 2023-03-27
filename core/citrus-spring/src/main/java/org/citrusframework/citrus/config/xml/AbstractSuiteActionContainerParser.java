/*
 * Copyright 2006-2014 the original author or authors.
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

package org.citrusframework.citrus.config.xml;

import org.citrusframework.citrus.container.AbstractSuiteActionContainer;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.*;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public abstract class AbstractSuiteActionContainerParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(getContainerClass());

        builder.addPropertyValue("name", element.getAttribute("id"));

        if (element.hasAttribute("suites")) {
            List<String> suiteNames = Arrays.asList(StringUtils.commaDelimitedListToStringArray(element.getAttribute("suites")));
            builder.addPropertyValue("suiteNames", suiteNames);
        }

        if (element.hasAttribute("groups")) {
            List<String> groups = Arrays.asList(StringUtils.commaDelimitedListToStringArray(element.getAttribute("groups")));
            builder.addPropertyValue("testGroups", groups);
        }

        Map<String, String> envProperties = new HashMap<>();
        Element envElement = DomUtils.getChildElementByTagName(element, "env");
        if (envElement != null) {
            List<Element> propertyElements = DomUtils.getChildElementsByTagName(envElement, "property");
            for (Element property : propertyElements) {
                envProperties.put(property.getAttribute("name"), property.getAttribute("value"));
            }
        }

        if (!envProperties.isEmpty()) {
            builder.addPropertyValue("env", envProperties);
        }

        Map<String, String> systemProperties = new HashMap<>();
        Element systemElement = DomUtils.getChildElementByTagName(element, "system");
        if (systemElement != null) {
            List<Element> propertyElements = DomUtils.getChildElementsByTagName(systemElement, "property");
            for (Element property : propertyElements) {
                systemProperties.put(property.getAttribute("name"), property.getAttribute("value"));
            }
        }

        if (!systemProperties.isEmpty()) {
            builder.addPropertyValue("systemProperties", systemProperties);
        }

        ActionContainerParser.doParse(DomUtils.getChildElementByTagName(element, "actions"), parserContext, builder);

        parserContext.getRegistry().registerBeanDefinition(element.getAttribute("id"), builder.getBeanDefinition());

        return null;
    }

    /**
     * Subclasses provide suite container class.
     * @return
     */
    protected abstract Class<? extends AbstractSuiteActionContainer> getContainerClass();
}
