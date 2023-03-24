/*
 * Copyright 2006-2017 the original author or authors.
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
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class HoverAction extends FindElementAction {

    /**
     * Default constructor.
     */
    public HoverAction(Builder builder) {
        super("hover", builder);
    }

    @Override
    protected void execute(WebElement webElement, SeleniumBrowser browser, TestContext context) {
        super.execute(webElement, browser, context);

        Actions builder = new Actions(browser.getWebDriver());
        builder.moveToElement(webElement).perform();
    }

    /**
     * Action builder.
     */
    public static class Builder extends ElementActionBuilder<HoverAction, Builder> {

        @Override
        public HoverAction build() {
            return new HoverAction(this);
        }
    }
}
