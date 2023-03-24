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
import org.citrusframework.selenium.endpoint.SeleniumHeaders;

/**
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class StopBrowserAction extends AbstractSeleniumAction {

    /**
     * Default constructor.
     */
    public StopBrowserAction(Builder builder) {
        super("stop", builder);
    }

    @Override
    protected void execute(SeleniumBrowser browser, TestContext context) {
        log.info("Stopping browser of type {}", browser.getEndpointConfiguration().getBrowserType());
        browser.stop();

        context.getVariables().remove(SeleniumHeaders.SELENIUM_BROWSER);
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractSeleniumAction.Builder<StopBrowserAction, StopBrowserAction.Builder> {

        @Override
        public StopBrowserAction build() {
            return new StopBrowserAction(this);
        }
    }
}
