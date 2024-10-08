/*
 * Copyright the original author or authors.
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

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Bean definition parser for call template action in test case.
 *
 */
public class CallTemplateParser implements BeanDefinitionParser {

    @Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition;

        String templateName = element.getAttribute("name");

        beanDefinition = BeanDefinitionBuilder.childBeanDefinition(templateName);
        beanDefinition.addPropertyValue("name", element.getLocalName() + ":" + templateName);

        DescriptionElementParser.doParse(element, beanDefinition);

        List<?> parameterElements = DomUtils.getChildElementsByTagName(element, "parameter");

        if (!isEmpty(parameterElements)) {
            Map<String, String> parameters = new LinkedHashMap<>();

            for (Object o : parameterElements) {
                Element parameterElement = (Element) o;
                final String name = parameterElement.getAttribute("name");
                String value = null;
                if (parameterElement.hasAttribute("value")) {
                    value = parameterElement.getAttribute("value");
                } else {
                    Element valueElement = DomUtils.getChildElementByTagName(parameterElement, "value");
                    if (valueElement != null) {
                        value = valueElement.getTextContent();
                    }
                }
                if (value != null) {
                    parameters.put(name, value);
                } else {
                    throw new BeanCreationException("Please provide either value attribute or value element for parameter");
                }
            }

            beanDefinition.addPropertyValue("parameter", parameters);
        }

        return beanDefinition.getBeanDefinition();
    }
}
