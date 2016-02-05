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
package com.consol.citrus.selenium.action;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.selenium.client.WebClientConfiguration;
import com.consol.citrus.selenium.model.WebPage;
import java.lang.reflect.Method;
import java.util.Map;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.util.StringUtils;

/**
 *
 * @author Tamer Erdogan
 */
public class ExtractAction extends WebAction {

	private Map<By, String> elements;
	private String pageName;
	private Map<String, String> pageActions;

	@Override
	public void doExecute(TestContext context) {
		super.doExecute(context);

		if (elements != null) {
			for (By by : elements.keySet()) {
				String variable = elements.get(by);
				String value = null;
				logger.info("extracting the element by <{}>", by);

				WebElement element = webClient.findElement(by);

				String attribute = null;
				// check if the by has attribute selection
				if (by instanceof By.ByXPath) {
					// TODO: refactor the following
					String xpathExpression = by.toString().replaceAll("By.xpath:\\s*", "");
					if (xpathExpression.contains("/@")) {
						// we found attribute selection.
						String[] parts = xpathExpression.split("/@");
						attribute = parts[1];
						String newXpathExpresseion = parts[0];
						element = webClient.findElement(By.xpath(newXpathExpresseion));
					}
				}

				if (element != null) {
					if (attribute != null) {
						value = element.getAttribute(attribute);
					} else {
						value = element.getAttribute("value");
						if (value == null || value.isEmpty()) {
							value = element.getText();
						}
					}
				}
				context.setVariable(variable, value);
			}
		}

		if (StringUtils.hasText(pageName)) {
			WebPage pageObj;
			try {
				logger.debug("Initializing the page object {}", pageName);
				Class pageClass = Class.forName(pageName);
				pageObj = webClient.createPage(pageClass);
				logger.debug("page object {} is succesfully created.", pageName);
				webClient.verifyPage(pageObj);
				for (String pageAction : pageActions.keySet()) {
					String variable = pageActions.get(pageAction);
					logger.debug("running the action {} on the page object {}", pageAction, pageName);
					logger.info("Invoking method '" + pageAction + "' on instance '" + pageClass + "'");
					Method actionMethod = pageObj.getClass().getMethod("get" + pageAction);
					String value = (String) actionMethod.invoke(pageObj);
					context.setVariable(variable, value);
				}
			} catch (Exception ex) {
				logger.error(ex.getMessage());
				throw new CitrusRuntimeException(ex);
			}
		}
	}

	public Map<By, String> getElements() {
		return elements;
	}

	public void setElements(Map<By, String> elements) {
		this.elements = elements;
	}

	public Map<String, String> getPageActions() {
		return pageActions;
	}

	public void setPageActions(Map<String, String> pageActions) {
		this.pageActions = pageActions;
	}

	public String getPageName() {
		return pageName;
	}

	public void setPageName(String name) {
		if (name.contains(".")) {
			this.pageName = name;
		} else if (StringUtils.hasText(webClient.getModelNamespace())) {
			this.pageName = webClient.getModelNamespace() + "." + name;
		} else {
			this.pageName = WebClientConfiguration.PAGE_MODEL_NAMESPACE + "." + name;
		}
		setName(getName() + ":" + name);
	}

}
