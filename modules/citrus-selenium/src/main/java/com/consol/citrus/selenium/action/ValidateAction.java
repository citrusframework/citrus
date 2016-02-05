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
import com.consol.citrus.validation.matcher.ValidationMatcherUtils;
import java.lang.reflect.Method;
import java.util.Map;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.springframework.util.StringUtils;

/**
 *
 * @author Tamer Erdogan
 */
public class ValidateAction extends WebAction {

	private String pageName;
	private Map<By, String> validations;
	private Map<String, String> pageValidations;

	@Override
	public void doExecute(TestContext context) {
		super.doExecute(context);
		if (validations != null) {
			for (By by : validations.keySet()) {
				String controlValue = validations.get(by);
				if (ValidationMatcherUtils.isValidationMatcherExpression(controlValue)) {
					String actualValue = null;
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
							actualValue = element.getAttribute(attribute);
						} else {
							actualValue = element.getAttribute("value");
							if (actualValue == null || actualValue.isEmpty()) {
								actualValue = element.getText();
							}
						}
					}
					ValidationMatcherUtils.resolveValidationMatcher(by.toString(), actualValue, controlValue, context);
				} else {
					webClient.validate(by, controlValue, null);
				}
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
				for (String pageAction : pageValidations.keySet()) {
					String controlValue = pageValidations.get(pageAction);
					logger.debug("running the action {} on the page object {}", pageAction, pageName);
					logger.info("Invoking method '" + pageAction + "' on instance '" + pageClass + "'");
					Method actionMethod = pageObj.getClass().getMethod("get" + pageAction);
					String actualValue = (String) actionMethod.invoke(pageObj);
					if (ValidationMatcherUtils.isValidationMatcherExpression(controlValue)) {
						ValidationMatcherUtils.resolveValidationMatcher(pageAction, actualValue, controlValue, context);
					} else {
						webClient.validate(actualValue, controlValue, null);
					}
				}
			} catch (Exception ex) {
				logger.error(ex.getMessage());
				throw new CitrusRuntimeException(ex);
			}
		}
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

	public Map<By, String> getValidations() {
		return this.validations;
	}

	public void setValidations(Map<By, String> validations) {
		this.validations = validations;
	}

	public Map<String, String> getPageValidations() {
		return pageValidations;
	}

	public void setPageValidations(Map<String, String> pageValidations) {
		this.pageValidations = pageValidations;
	}

}
