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

import java.util.Set;

import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.selenium.endpoint.SeleniumBrowser;
import org.citrusframework.selenium.endpoint.SeleniumHeaders;
import org.citrusframework.util.StringUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class OpenWindowAction extends AbstractSeleniumAction {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(OpenWindowAction.class);

    /** Window name to open */
    private final String windowName;

    /**
     * Default constructor.
     */
    public OpenWindowAction(Builder builder) {
        super("open-window", builder);

        this.windowName = builder.windowName;
    }

    @Override
    protected void execute(SeleniumBrowser browser, TestContext context) {
        Set<String> windowHandles = browser.getWebDriver().getWindowHandles();
        String newWindow = null;
        String lastWindow = browser.getWebDriver().getWindowHandle();
        context.setVariable(SeleniumHeaders.SELENIUM_LAST_WINDOW, lastWindow);

        if (browser.getWebDriver() instanceof JavascriptExecutor) {
            ((JavascriptExecutor) browser.getWebDriver()).executeScript("window.open();");
        }

        Set<String> newWindowHandles = browser.getWebDriver().getWindowHandles();

        for (String window : newWindowHandles) {
            if (!windowHandles.contains(window)) {
                newWindow = window;
            }
        }

        if (!StringUtils.isEmpty(newWindow)) {
            browser.getWebDriver().switchTo().window(newWindow);
            logger.info("Open window: " + newWindow);
            context.setVariable(SeleniumHeaders.SELENIUM_ACTIVE_WINDOW, newWindow);
            context.setVariable(windowName, newWindow);
        } else {
            throw new CitrusRuntimeException("Failed to open new window");
        }

    }

    /**
     * Gets the windowName.
     * @return
     */
    public String getWindowName() {
        return windowName;
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractSeleniumAction.Builder<OpenWindowAction, Builder> {

        private String windowName = SeleniumHeaders.SELENIUM_ACTIVE_WINDOW;

        /**
         * Set window name.
         * @param name
         * @return
         */
        public Builder window(String name) {
            this.windowName = name;
            return this;
        }

        @Override
        public OpenWindowAction build() {
            return new OpenWindowAction(this);
        }
    }
}
