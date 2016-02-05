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
package com.consol.citrus.selenium.xml;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 *
 * @author Tamer Erdogan
 */
public class TestcaseNamespaceHandler extends NamespaceHandlerSupport {

	@Override
	public void init() {
		registerBeanDefinitionParser("page", new PageActionParser());
		registerBeanDefinitionParser("validate", new ValidateActionParser());
		registerBeanDefinitionParser("goto", new GotoActionParser());
		registerBeanDefinitionParser("start", new StartActionParser());
		registerBeanDefinitionParser("stop", new StopActionParser());
		registerBeanDefinitionParser("clearCache", new ClearCacheActionParser());
		registerBeanDefinitionParser("click", new ClickActionParser());
		registerBeanDefinitionParser("setInput", new SetInputActionParser());
		registerBeanDefinitionParser("browser", new BrowserActionParser());
		registerBeanDefinitionParser("extract", new ExtractActionParser());
	}

}
