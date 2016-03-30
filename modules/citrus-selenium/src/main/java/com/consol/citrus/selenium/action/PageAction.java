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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 *
 * @author Tamer Erdogan
 * @param <T>
 */
public class PageAction<T extends WebPage> extends WebAction {

	private Map<String, String> actionParameters;

	private String pageAction;
	private String pageName;

	@SuppressWarnings("unchecked")
	@Override
	public void doExecute(TestContext context) {
		super.doExecute(context);

		if (StringUtils.hasText(pageName)) {
			WebPage pageObj;
			try {
				logger.debug("Initializing the page object {}", pageName);
				Class pageClass = Class.forName(pageName);
				pageObj = webClient.createPage(pageClass);
				logger.debug("page object {} is succesfully created.", pageName);
				webClient.verifyPage(pageObj);

				if (StringUtils.hasText(pageAction)) {
					logger.debug("running the action {} on the page object {}", pageAction, pageName);
					logger.info("Invoking method '" + pageAction + "' on instance '" + pageClass + "'");
					Method actionMethod;
					if (actionParameters == null || actionParameters.isEmpty()) {
						actionMethod = pageObj.getClass().getMethod(pageAction);
						actionMethod.invoke(pageObj);
					} else {
						actionMethod = getMethod(pageClass, new Class<?>[]{Map.class});
						Map<String, String> methodArgs = new HashMap<>();
						for (String parameterName : actionParameters.keySet()) {
							String parameterValue = context.replaceDynamicContentInString(actionParameters.get(parameterName));
							methodArgs.put(parameterName, parameterValue);
						}
						actionMethod.invoke(pageObj, methodArgs);
					}
				}
			} catch (Exception ex) {
				logger.error(ex.getMessage());
				throw new CitrusRuntimeException(ex);
			}
		}
	}

	public Map<String, String> getActionParameters() {
		return actionParameters;
	}

	public void setActionParameters(Map<String, String> params) {
		this.actionParameters = params;
	}

	public String getPageAction() {
		return pageAction;
	}

	public void setPageAction(String pageAction) {
		this.pageAction = pageAction;
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
		setName(name);
	}

	private Method getMethod(Class<T> pageClass, Class<?>[] methodTypes) throws IllegalArgumentException, InvocationTargetException, IllegalAccessException, CitrusRuntimeException {
		Method methodToRun = ReflectionUtils.findMethod(pageClass, pageAction, methodTypes);

		if (methodToRun == null) {
			throw new CitrusRuntimeException("Unable to find method '" + pageAction + "("
					+ StringUtils.arrayToCommaDelimitedString(methodTypes) + ")' for class '" + pageClass + "'");
		}
		return methodToRun;
	}

}
