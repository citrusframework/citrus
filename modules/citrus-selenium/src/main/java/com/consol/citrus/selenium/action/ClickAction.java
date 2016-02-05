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
import org.openqa.selenium.By;

/**
 *
 * @author Tamer Erdogan
 */
public class ClickAction extends WebAction {

	private By by;

	@Override
	public void doExecute(TestContext context) {
		super.doExecute(context);
		logger.info("clicking the element by <{}>", by);
		webClient.click(by);
	}

	public By getBy() {
		return by;
	}

	public void setBy(By by) {
		this.by = by;
	}

}
