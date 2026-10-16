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

package org.citrusframework.selenium.actions;

import org.citrusframework.api.actions.selenium.SeleniumDoubleClickActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

/**
 * Performs double click on web element.
 *
 * @since 5.1
 */
public class DoubleClickAction extends FindElementAction {

    /**
     * Default constructor.
     */
    public DoubleClickAction(Builder builder) {
        super("double-click", builder);
    }

    @Override
    protected void execute(WebElement webElement, SeleniumBrowser browser, TestContext context) {
        super.execute(webElement, browser, context);

        Actions builder = new Actions(browser.getWebDriver());
        builder.doubleClick(webElement).perform();
    }

    /**
     * Action builder.
     */
    public static class Builder extends ElementActionBuilder<DoubleClickAction, Builder>
            implements SeleniumDoubleClickActionBuilder<DoubleClickAction, Builder> {

        @Override
        public DoubleClickAction build() {
            return new DoubleClickAction(this);
        }
    }
}
