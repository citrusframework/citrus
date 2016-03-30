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

import com.consol.citrus.context.TestContext;
import com.consol.citrus.model.config.selenium.BrowserTypeEnum;
import com.consol.citrus.selenium.action.PageAction;
import com.consol.citrus.selenium.action.StartAction;
import com.consol.citrus.selenium.action.StopAction;
import com.consol.citrus.testng.AbstractTestNGCitrusTest;
import org.testng.annotations.Test;

/**
 *
 * @author Tamer Erdogan
 */
public class WebClientTest extends AbstractTestNGCitrusTest {

	WebClientConfiguration clientConfiguration;

	@Test
	public void webClientTest() {
		clientConfiguration = new WebClientConfiguration();
		clientConfiguration.setBrowserType(BrowserTypeEnum.HTML_UNIT.value());
		clientConfiguration.setStartUrl("http://www.example.com");

		WebClient webClient = new WebClient(clientConfiguration);

		TestContext testContext = new TestContext();

		StartAction startAction = new StartAction();
		startAction.setWebClient(webClient);
		startAction.setUrl(clientConfiguration.getStartUrl());
		startAction.doExecute(testContext);

		PageAction pageAction = new PageAction();
		pageAction.setWebClient(webClient);
		pageAction.setPageName("ExamplePage");
		pageAction.setPageAction("hasHeader");
		pageAction.doExecute(testContext);

		StopAction stopAction = new StopAction();
		stopAction.setWebClient(webClient);
		stopAction.doExecute(testContext);
	}
}
