/*
 * Copyright 2006-2010 the original author or authors.
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

import java.util.List;

import org.citrusframework.config.CitrusNamespaceParserRegistry;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Abstract parser implementation that is aware of several embedded test actions of a container. Bean definitions that use
 * this parser component must have an 'actions' property of type {@link List} in order to receive the list of embedded test actions.
 *
 * @author Christoph Deppisch
 */
public abstract class ActionContainerParser implements BeanDefinitionParser {

    /**
     * Prevent instantiation.
     */
    private ActionContainerParser() {
    }

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        doParse(element, parserContext, builder, "actions");
    }

    public static void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder, String propertyName) {
        ManagedList<BeanDefinition> actions = new ManagedList<>();

        List<Element> childElements = DomUtils.getChildElements(element);

        for (Element action : childElements) {
            if (action.getLocalName().equals("description")) {
                continue;
            }

            BeanDefinitionParser parser = null;
            if (action.getNamespaceURI().equals(element.getNamespaceURI())) {
                parser = CitrusNamespaceParserRegistry.getBeanParser(action.getLocalName());
            }

            if (parser == null) {
                actions.add(parserContext.getReaderContext().getNamespaceHandlerResolver().resolve(action.getNamespaceURI()).parse(action, parserContext));
            } else {
                actions.add(parser.parse(action, parserContext));
            }
        }

        if (actions.size() > 0) {
            builder.addPropertyValue(propertyName, actions);
        }
    }
}
