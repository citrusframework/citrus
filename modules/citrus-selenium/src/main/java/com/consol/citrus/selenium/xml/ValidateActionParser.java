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

import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import com.consol.citrus.selenium.action.ValidateAction;
import java.lang.reflect.Method;
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
 * WebBrowser client parser sets properties on bean definition for client
 * component.
 *
 * @author Tamer Erdogan
 */
public class ValidateActionParser extends WebActionParser {

	/**
	 * @param element
	 * @param parserContext
	 * @return
	 */
	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(ValidateAction.class);
		this.doParse(element, builder);

		String pageName = element.getAttribute("pageName");
		BeanDefinitionParserUtils.setPropertyValue(builder, pageName, "pageName");

		Map<By, String> validations = new LinkedHashMap<>();
		List<Element> validationElements = DomUtils.getChildElementsByTagName(element, "element");
		for (Element validateElement : validationElements) {
			String value = validateElement.getAttribute("value");
			By by = getBy(validateElement.getAttribute("by"), validateElement.getAttribute("select"));
			validations.put(by, value);
		}
		builder.addPropertyValue("validations", validations);

		Map<String, String> pageValidations = new LinkedHashMap<>();
		List<Element> pageValidationElements = DomUtils.getChildElementsByTagName(element, "page");
		for (Element validateElement : pageValidationElements) {
			String pageAction = validateElement.getAttribute("get");
			String value = validateElement.getAttribute("value");
			pageValidations.put(pageAction, value);
		}
		builder.addPropertyValue("pageValidations", pageValidations);

		return builder.getBeanDefinition();
	}

	private By getBy(String byWhat, String selector) {
		By by = null;
		try {
			Method byMethod = By.class.getMethod(byWhat, String.class);
			by = (By) byMethod.invoke(null, selector);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return by;
	}
}
