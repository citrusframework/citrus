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
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.util.LocatorResolver;
import org.citrusframework.util.StringUtils;

public class OpenAction extends AbstractPlaywrightAction {

    private final String url;

    public OpenAction(Builder builder) {
        super("open", builder);
        this.url = builder.url;
    }

    @Override
    protected void execute(PlaywrightBrowser browser, TestContext context) {
        browser.getCurrentPage().navigate(LocatorResolver.resolve(url, context));
    }

    public String getUrl() {
        return url;
    }

    public static class Builder extends AbstractPlaywrightAction.Builder<OpenAction, Builder> {
        private String url;

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        @Override
        public OpenAction build() {
            if (!StringUtils.hasText(url)) {
                throw new CitrusRuntimeException("Missing Playwright URL - call url(...) before building open action");
            }
            return new OpenAction(this);
        }
    }
}
