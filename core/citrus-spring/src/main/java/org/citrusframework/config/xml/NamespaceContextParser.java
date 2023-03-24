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

import org.citrusframework.xml.namespace.NamespaceContextBuilder;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.*;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class NamespaceContextParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(NamespaceContextBuilder.class);
        parseNamespaceDefinitions(builder, element);

        parserContext.getRegistry().registerBeanDefinition(NamespaceContextBuilder.DEFAULT_BEAN_ID, builder.getBeanDefinition());

        return null;
    }

    /**
     * Parses all variable definitions and adds those to the bean definition
     * builder as property value.
     * @param builder the target bean definition builder.
     * @param element the source element.
     */
    private void parseNamespaceDefinitions(BeanDefinitionBuilder builder, Element element) {
        Map<String, String> namespaces = new LinkedHashMap<String, String>();
        for (Element namespace : DomUtils.getChildElementsByTagName(element, "namespace")) {
            namespaces.put(namespace.getAttribute("prefix"), namespace.getAttribute("uri"));
        }

        if (!namespaces.isEmpty()) {
            builder.addPropertyValue("namespaceMappings", namespaces);
        }
    }
}
