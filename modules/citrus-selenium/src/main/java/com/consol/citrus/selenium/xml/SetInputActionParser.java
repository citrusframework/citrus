/*
 * Copyright 2016 the original author or authors.
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
package com.consol.citrus.selenium.xml;

import com.consol.citrus.selenium.action.SetInputAction;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.By;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 *
 * @author Tamer Erdogan
 */
public class SetInputActionParser extends WebActionParser {

	/**
	 * @param element
	 * @param parserContext
	 * @return
	 */
	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		actionClass = SetInputAction.class;
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(actionClass);
		this.doParse(element, builder);

		Map<By, String> fields = new LinkedHashMap<>();
		List<Element> fieldElements = DomUtils.getChildElementsByTagName(element, "field");
		for (Element fieldElement : fieldElements) {
			String value = fieldElement.getAttribute("value");
			By by = getByFromElement(fieldElement);
			fields.put(by, value);
		}
		builder.addPropertyValue("fields", fields);

		return builder.getBeanDefinition();
	}
}
