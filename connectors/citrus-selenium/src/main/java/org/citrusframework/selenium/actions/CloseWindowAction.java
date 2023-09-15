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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Close opened window by name.
 *
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class CloseWindowAction extends AbstractSeleniumAction implements SeleniumAction {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CloseWindowAction.class);

    /** Window name */
    private final String windowName;

    /**
     * Default constructor.
     */
    public CloseWindowAction(Builder builder) {
        super("close-window", builder);

        this.windowName = builder.windowName;
    }

    @Override
    protected void execute(SeleniumBrowser browser, TestContext context) {
        if (!context.getVariables().containsKey(windowName)) {
            throw new CitrusRuntimeException("Failed to find window handle for window " + windowName);
        }

        Set<String> handles = browser.getWebDriver().getWindowHandles();
        if (!handles.contains(context.getVariable(windowName))) {
            throw new CitrusRuntimeException("Failed to find window for handle " + context.getVariable(windowName));
        }

        logger.info("Current window: " + browser.getWebDriver().getWindowHandle());
        logger.info("Window to close: " + context.getVariable(windowName));

        if (browser.getWebDriver().getWindowHandle().equals((context.getVariable(windowName)))) {
            browser.getWebDriver().close();

            logger.info("Switch back to main window!");
            if (context.getVariables().containsKey(SeleniumHeaders.SELENIUM_LAST_WINDOW)) {
                browser.getWebDriver().switchTo().window(context.getVariable(SeleniumHeaders.SELENIUM_LAST_WINDOW));
                context.setVariable(SeleniumHeaders.SELENIUM_ACTIVE_WINDOW, context.getVariable(SeleniumHeaders.SELENIUM_LAST_WINDOW));
            } else {
                browser.getWebDriver().switchTo().defaultContent();
                context.setVariable(SeleniumHeaders.SELENIUM_ACTIVE_WINDOW, browser.getWebDriver().getWindowHandle());
            }
        } else {
            String activeWindow = browser.getWebDriver().getWindowHandle();

            browser.getWebDriver().switchTo().window(context.getVariable(windowName));
            browser.getWebDriver().close();

            if (context.getVariables().containsKey(SeleniumHeaders.SELENIUM_ACTIVE_WINDOW)) {
                browser.getWebDriver().switchTo().window(context.getVariable(SeleniumHeaders.SELENIUM_ACTIVE_WINDOW));
            } else {
                browser.getWebDriver().switchTo().window(activeWindow);
                context.setVariable(SeleniumHeaders.SELENIUM_ACTIVE_WINDOW, activeWindow);
            }
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
    public static class Builder extends AbstractSeleniumAction.Builder<CloseWindowAction, Builder> {

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
        public CloseWindowAction build() {
            return new CloseWindowAction(this);
        }
    }
}
