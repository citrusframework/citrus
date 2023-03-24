/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.selenium.config.handler;

import org.citrusframework.selenium.config.xml.*;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class SeleniumTestcaseNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("start", new StartBrowserActionParser());
        registerBeanDefinitionParser("stop", new StopBrowserActionParser());
        registerBeanDefinitionParser("open-window", new OpenWindowActionParser());
        registerBeanDefinitionParser("close-window", new CloseWindowActionParser());
        registerBeanDefinitionParser("switch-window", new SwitchWindowActionParser());
        registerBeanDefinitionParser("clear-cache", new ClearBrowserCacheActionParser());
        registerBeanDefinitionParser("find", new FindElementActionParser());
        registerBeanDefinitionParser("page", new PageActionParser());
        registerBeanDefinitionParser("click", new ClickActionParser());
        registerBeanDefinitionParser("hover", new HoverActionParser());
        registerBeanDefinitionParser("set-input", new SetInputActionParser());
        registerBeanDefinitionParser("check-input", new CheckInputActionParser());
        registerBeanDefinitionParser("dropdown-select", new DropDownSelectActionParser());
        registerBeanDefinitionParser("wait", new WaitUntilActionParser());
        registerBeanDefinitionParser("javascript", new JavaScriptActionParser());
        registerBeanDefinitionParser("screenshot", new MakeScreenshotActionParser());
        registerBeanDefinitionParser("navigate", new NavigateActionParser());
        registerBeanDefinitionParser("alert", new AlertActionParser());
        registerBeanDefinitionParser("store-file", new StoreFileActionParser());
        registerBeanDefinitionParser("get-stored-file", new GetStoredFileActionParser());
    }
}
