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

package org.citrusframework.config.xml;

import org.citrusframework.functions.FunctionLibrary;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class FunctionLibraryParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(FunctionLibrary.class);
        parseFunctionDefinitions(builder, element);

        builder.addPropertyValue("prefix", element.getAttribute("prefix"));
        builder.addPropertyValue("name", element.getAttribute("id"));

        parserContext.getRegistry().registerBeanDefinition(element.getAttribute("id"), builder.getBeanDefinition());

        return null;
    }

    /**
     * Parses all variable definitions and adds those to the bean definition
     * builder as property value.
     * @param builder the target bean definition builder.
     * @param element the source element.
     */
    private void parseFunctionDefinitions(BeanDefinitionBuilder builder, Element element) {
        ManagedMap<String, Object> functions = new ManagedMap<String, Object>();
        for (Element function : DomUtils.getChildElementsByTagName(element, "function")) {
            if (function.hasAttribute("ref")) {
                functions.put(function.getAttribute("name"), new RuntimeBeanReference(function.getAttribute("ref")));
            } else {
                functions.put(function.getAttribute("name"), BeanDefinitionBuilder.rootBeanDefinition(function.getAttribute("class")).getBeanDefinition());
            }
        }

        if (!functions.isEmpty()) {
            builder.addPropertyValue("members", functions);
        }
    }
}
