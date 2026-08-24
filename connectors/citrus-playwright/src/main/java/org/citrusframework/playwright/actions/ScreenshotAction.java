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

import java.nio.file.Path;

import org.citrusframework.context.TestContext;
import org.citrusframework.playwright.endpoint.PlaywrightBrowser;
import org.citrusframework.playwright.util.LocatorResolver;

public class ScreenshotAction extends AbstractPlaywrightAction {

    private final String path;
    private final String variable;

    public ScreenshotAction(Builder builder) {
        super("screenshot", builder);
        this.path = builder.path;
        this.variable = builder.variable;
    }

    public String getPath() {
        return path;
    }

    public String getVariable() {
        return variable;
    }

    @Override
    protected void execute(PlaywrightBrowser browser, TestContext context) {
        String resolvedPath = LocatorResolver.resolve(path, context);
        byte[] bytes = browser.getCurrentPage().screenshot(new com.microsoft.playwright.Page.ScreenshotOptions()
                .setPath(Path.of(resolvedPath)));
        if (variable != null) {
            context.setVariable(variable, resolvedPath != null ? resolvedPath : bytes);
        }
    }

    public static class Builder extends AbstractPlaywrightAction.Builder<ScreenshotAction, Builder> {
        private String path = "target/playwright/screenshot.png";
        private String variable;

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder variable(String variable) {
            this.variable = variable;
            return this;
        }

        @Override
        public ScreenshotAction build() {
            return new ScreenshotAction(this);
        }
    }
}
