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

package org.citrusframework.selenium.actions;

import org.citrusframework.context.TestContext;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;

/**
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class ClearBrowserCacheAction extends AbstractSeleniumAction {

    /**
     * Default constructor.
     */
    public ClearBrowserCacheAction(Builder builder) {
        super("clear-cache", builder);
    }

    @Override
    protected void execute(SeleniumBrowser browser, TestContext context) {
        browser.getWebDriver().manage().deleteAllCookies();
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractSeleniumAction.Builder<ClearBrowserCacheAction, Builder> {

        @Override
        public ClearBrowserCacheAction build() {
            return new ClearBrowserCacheAction(this);
        }
    }
}
