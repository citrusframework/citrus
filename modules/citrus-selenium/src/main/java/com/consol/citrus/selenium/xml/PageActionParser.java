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
import com.consol.citrus.selenium.action.PageAction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class PageActionParser extends WebActionParser {

	/**
	 * @param element
	 * @param parserContext
	 * @return
	 */
	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(PageAction.class);
		this.doParse(element, builder);

		BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("pageName").trim(), "pageName");
		BeanDefinitionParserUtils.setPropertyValue(builder, element.getAttribute("action").trim(), "pageAction");

		Map<String, String> actionParameters = new HashMap<>();
		List<Element> parameters = DomUtils.getChildElementsByTagName(element, "parameter");
		for (Element paramElement : parameters) {
			String name = paramElement.getAttribute("name");
			String value = paramElement.getAttribute("value");
			actionParameters.put(name, value);
		}
		builder.addPropertyValue("actionParameters", actionParameters);

		return builder.getBeanDefinition();
	}
}
