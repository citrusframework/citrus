/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.config.xml;

import com.consol.citrus.actions.PurgeEndpointAction;
import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.*;

/**
 * @author Christoph Deppisch
 * @since 2.4
 */
public class PurgeEndpointActionParser implements BeanDefinitionParser {

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(PurgeEndpointAction.class);

        DescriptionElementParser.doParse(element, beanDefinition);

        Element messageSelectorElement = DomUtils.getChildElementByTagName(element, "selector");
        if (messageSelectorElement != null) {
            Element selectorStringElement = DomUtils.getChildElementByTagName(messageSelectorElement, "value");
            if (selectorStringElement != null) {
                beanDefinition.addPropertyValue("messageSelectorString", DomUtils.getTextValue(selectorStringElement));
            }

            Map<String, String> messageSelector = new HashMap<String, String>();
            List<?> messageSelectorElements = DomUtils.getChildElementsByTagName(messageSelectorElement, "element");
            for (Iterator<?> iter = messageSelectorElements.iterator(); iter.hasNext();) {
                Element selectorElement = (Element) iter.next();
                messageSelector.put(selectorElement.getAttribute("name"), selectorElement.getAttribute("value"));
            }
            beanDefinition.addPropertyValue("messageSelector", messageSelector);
        }

        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("receive-timeout"), "receiveTimeout");

        List<String> endpointNames = new ArrayList<String>();
        ManagedList<BeanDefinition> endpointRefs = new ManagedList<BeanDefinition>();
        List<?> endpointElements = DomUtils.getChildElementsByTagName(element, "endpoint");
        for (Iterator<?> iter = endpointElements.iterator(); iter.hasNext();) {
            Element endpoint = (Element) iter.next();
            String endpointName = endpoint.getAttribute("name");
            String endpointRef = endpoint.getAttribute("ref");

            if (StringUtils.hasText(endpointName)) {
                endpointNames.add(endpointName);
            } else if (StringUtils.hasText(endpointRef)) {
                endpointRefs.add(BeanDefinitionBuilder.childBeanDefinition(endpointRef).getBeanDefinition());
            } else {
                throw new BeanCreationException("Element 'endpoint' must set one of the attributes 'name' or 'ref'");
            }
        }

        beanDefinition.addPropertyValue("endpointNames", endpointNames);
        beanDefinition.addPropertyValue("endpoints", endpointRefs);

        return beanDefinition.getBeanDefinition();
    }
}
