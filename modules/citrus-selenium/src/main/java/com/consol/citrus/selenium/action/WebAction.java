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

import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.selenium.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 *
 * @author Tamer Erdogan
 */
public class WebAction extends AbstractTestAction {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	protected String name;
	protected String url;
	protected WebClient webClient;

	@Override
	public void doExecute(TestContext context) {
		if (StringUtils.hasText(url)) {
			String convertedUrl = context.replaceDynamicContentInString(url);
			logger.info("navigating to url <{}>", convertedUrl);
			webClient.navigateTo(convertedUrl);
		}
	}

	public WebClient getWebClient() {
		return webClient;
	}

	public void setWebClient(WebClient client) {
		this.webClient = client;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
