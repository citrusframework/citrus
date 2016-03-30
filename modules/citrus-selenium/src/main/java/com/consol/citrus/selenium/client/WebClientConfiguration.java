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
package com.consol.citrus.selenium.client;

import com.consol.citrus.message.ErrorHandlingStrategy;
import com.consol.citrus.model.config.selenium.BrowserTypeEnum;

/**
 *
 * @author Tamer Erdogan
 */
public class WebClientConfiguration {

	public static final String PAGE_MODEL_NAMESPACE = "com.consol.citrus.selenium.model";

	/**
	 * The default WebDriver is HtmlUnit.
	 */
	private BrowserTypeEnum browserType = BrowserTypeEnum.HTML_UNIT;

	/**
	 * enable Javascript
	 */
	private Boolean enableJavascript = true;

	/**
	 * Should errors be handled within client or simply throw exception
	 */
	private ErrorHandlingStrategy errorHandlingStrategy = ErrorHandlingStrategy.PROPAGATE;

	/**
	 * Page model namespace
	 */
	private String modelNamespace = PAGE_MODEL_NAMESPACE;

	/**
	 * start url
	 */
	private String startUrl;

	/**
	 *
	 * Remote Address of the Selenium Server
	 */
	private String seleniumServer;

	/**
	 * Gets the browserType.
	 *
	 * @return the browserType
	 */
	public BrowserTypeEnum getBrowserType() {
		return browserType;
	}

	/**
	 * Sets the browserType.
	 *
	 * @param browser
	 */
	public void setBrowserType(String browser) {
		this.browserType = BrowserTypeEnum.fromValue(browser);
	}

	/**
	 * Gets the requestMethod.
	 *
	 * @return the requestMethod
	 */
	public Boolean getEnableJavascript() {
		return enableJavascript;
	}

	/**
	 * Set the reply message correlator.
	 *
	 * @param enableJavascript
	 */
	public void setEnableJavascript(Boolean enableJavascript) {
		this.enableJavascript = enableJavascript;
	}

	/**
	 * Gets the errorHandlingStrategy.
	 *
	 * @return the errorHandlingStrategy
	 */
	public ErrorHandlingStrategy getErrorHandlingStrategy() {
		return errorHandlingStrategy;
	}

	/**
	 * Sets the errorHandlingStrategy.
	 *
	 * @param errorHandlingStrategy the errorHandlingStrategy to set
	 */
	public void setErrorHandlingStrategy(ErrorHandlingStrategy errorHandlingStrategy) {
		this.errorHandlingStrategy = errorHandlingStrategy;
	}

	public String getModelNamespace() {
		return modelNamespace;
	}

	public void setModelNamespace(String namespace) {
		this.modelNamespace = namespace;
	}

	public String getStartUrl() {
		return startUrl;
	}

	public void setStartUrl(String url) {
		this.startUrl = url;
	}

	public String getSeleniumServer() {
		return seleniumServer;
	}

	public void setSeleniumServer(String seleniumServer) {
		this.seleniumServer = seleniumServer;
	}

}
