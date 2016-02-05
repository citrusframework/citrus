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
import com.consol.citrus.message.ErrorHandlingStrategy;
import com.consol.citrus.selenium.client.WebClient;
import com.consol.citrus.selenium.client.WebClientConfiguration;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * WebBrowser client parser sets properties on bean definition for client
 * component.
 *
 * @author Tamer Erdogan
 */
public class WebClientConfigurationParser extends AbstractBeanDefinitionParser {

	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(WebClient.class);

		BeanDefinitionBuilder configurationBuilder = BeanDefinitionBuilder.genericBeanDefinition(WebClientConfiguration.class);

		BeanDefinitionParserUtils.setPropertyValue(configurationBuilder, element.getAttribute("browser-type"), "browserType");
		BeanDefinitionParserUtils.setPropertyValue(configurationBuilder, element.getAttribute("start-url"), "startUrl");
		BeanDefinitionParserUtils.setPropertyValue(configurationBuilder, element.getAttribute("model-namespace"), "modelNamespace");
		BeanDefinitionParserUtils.setPropertyValue(configurationBuilder, element.getAttribute("selenium-server"), "seleniumServer");
		BeanDefinitionParserUtils.setPropertyValue(configurationBuilder, element.getAttribute("enable-javascript"), "enableJavascript");

		if (element.hasAttribute("error-strategy")) {
			configurationBuilder.addPropertyValue("errorHandlingStrategy",
					ErrorHandlingStrategy.fromName(element.getAttribute("error-strategy")));
		}

		String clientConfigurationId = element.getAttribute(ID_ATTRIBUTE) + "Configuration";
		BeanDefinitionParserUtils.registerBean(clientConfigurationId, configurationBuilder.getBeanDefinition(), parserContext, shouldFireEvents());

		builder.addConstructorArgReference(clientConfigurationId);

		return builder.getBeanDefinition();
	}
}
