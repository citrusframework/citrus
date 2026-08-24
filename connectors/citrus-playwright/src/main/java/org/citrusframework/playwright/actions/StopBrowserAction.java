/*
 * Copyright the original author or authors.
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

package org.citrusframework.playwright.actions;

import org.citrusframework.context.TestContext;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.support.PlaywrightBrowserScope;

public class StopBrowserAction extends AbstractPlaywrightAction {

    public StopBrowserAction(Builder builder) {
        super("stop", builder);
    }

    @Override
    protected void execute(PlaywrightBrowser browser, TestContext context) {
        browser.stop();
        if (PlaywrightBrowserScope.isBoundTo(browser)) {
            PlaywrightBrowserScope.clear();
        }
    }

    public static class Builder extends AbstractPlaywrightAction.Builder<StopBrowserAction, Builder> {
        @Override
        public StopBrowserAction build() {
            return new StopBrowserAction(this);
        }
    }
}
