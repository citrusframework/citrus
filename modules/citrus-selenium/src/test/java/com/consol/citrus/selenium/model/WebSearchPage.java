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

import java.util.Map;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Tamer Erdogan
 */
public class WebSearchPage implements WebPage {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@FindBy(how = How.NAME, using = "q")
	WebElement q;

	@FindBy(how = How.ID, using = "btn")
	WebElement submit;

	@FindBy(how = How.ID_OR_NAME, using = "results")
	WebElement results;

	@Override
	public String getPageUrl() {
		return "/";
	}

	public void search(Map<String, String> params) {
		String query = params.get("query");
		doSearch(query);
	}

	private void doSearch(String query) {
		q.sendKeys(query);
		submit.click();
	}

	public String getSearchResults() {
		return results.getText();
	}
}
