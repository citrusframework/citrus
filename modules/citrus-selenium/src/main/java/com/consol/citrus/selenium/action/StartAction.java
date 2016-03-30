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
import java.net.MalformedURLException;
import java.net.URL;
import org.springframework.util.StringUtils;

/**
 *
 * @author Tamer Erdogan
 */
public class StartAction extends WebAction {

	@Override
	public void doExecute(TestContext context) {
		logger.info("Starting the WebClient.");
		webClient.start();
		if (StringUtils.hasText(url)) {
			String convertedUrl = context.replaceDynamicContentInString(url);
			try {
				URL pageUrl = new URL(convertedUrl);
			} catch (MalformedURLException ex) {
				if (StringUtils.hasText(webClient.getStartUrl())) {
					String baseUrl = webClient.getStartUrl();
					String lastChar = baseUrl.substring(baseUrl.length() - 1);
					if (!lastChar.equals("/")) {
						baseUrl = baseUrl + "/";
					}
					this.url = baseUrl + convertedUrl;
				} else {
					throw new CitrusRuntimeException(ex);
				}
			}
		}

		super.doExecute(context);
	}
}
