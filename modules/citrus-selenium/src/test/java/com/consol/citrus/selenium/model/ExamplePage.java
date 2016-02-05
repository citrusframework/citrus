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
package com.consol.citrus.selenium.model;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

/**
 *
 * @author Tamer Erdogan
 */
public class ExamplePage implements WebPage {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@FindBy(how = How.TAG_NAME, using = "h1")
	WebElement header;

	@Override
	public String getPageUrl() {
		return "/";
	}

	public void hasHeader() {
		Assert.assertNotNull(header);
		logger.info(header.getText());
		Assert.assertEquals("Example Domain", header.getText());
	}

	public String getHeader() {
		return header.getText();
	}
}
