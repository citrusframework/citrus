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

import com.consol.citrus.selenium.model.WebPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.events.WebDriverEventListener;

/**
 *
 * @author Tamer Erdogan
 */
public interface WebDriverContainer {

	void addListener(WebDriverEventListener listener);

	void clearBrowserCache();

	void closeWebDriver();

	String getCurrentFrameUrl();

	String getCurrentUrl();

	String getPageSource();

	WebDriver getWebDriver();

	boolean hasWebDriverStarted();

	void verifyPage(WebPage page);

}
