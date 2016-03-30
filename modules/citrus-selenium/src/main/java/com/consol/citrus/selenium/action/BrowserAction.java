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
import java.lang.reflect.Method;

/**
 *
 * @author Tamer Erdogan
 */
public class BrowserAction extends WebAction {

	private String action;

	@Override
	public void doExecute(TestContext context) {
		super.doExecute(context);
		try {
			logger.info("Doing the following browser action: {}", action);
			Method actionMethod = webClient.getClass().getMethod(action);
			actionMethod.invoke(webClient);
		} catch (Exception ex) {
			logger.error(ex.getMessage());
			throw new CitrusRuntimeException(ex);
		}
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
}
