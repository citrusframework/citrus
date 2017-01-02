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

package com.consol.citrus.selenium.actions;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.selenium.endpoint.SeleniumBrowser;

import java.util.Set;

/**
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class CloseWindowAction extends AbstractSeleniumAction {

    private String windowName = "selenium_active_window";

    /**
     * Default constructor.
     */
    public CloseWindowAction() {
        super("close-window");
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

        log.info("Current window: " + browser.getWebDriver().getWindowHandle());
        log.info("Window to close: " + context.getVariable(windowName));

        if (browser.getWebDriver().getWindowHandle().equals((context.getVariable(windowName)))) {
            browser.getWebDriver().close();
        } else {
            String activeWindow = browser.getWebDriver().getWindowHandle();

            browser.getWebDriver().switchTo().window(context.getVariable(windowName));
            browser.getWebDriver().close();

            if (context.getVariables().containsKey("selenium_active_window")) {
                browser.getWebDriver().switchTo().window(context.getVariable("selenium_active_window"));
            } else {
                browser.getWebDriver().switchTo().window(activeWindow);
                context.setVariable("selenium_active_window", activeWindow);
            }
        }

        log.info("Switch back to main window!");
        if (context.getVariables().containsKey("selenium_last_window")) {
            browser.getWebDriver().switchTo().window(context.getVariable("selenium_last_window"));
        } else {
            browser.getWebDriver().switchTo().defaultContent();
        }
    }

    public void setWindowName(String windowName) {
        this.windowName = windowName;
    }

    public String getWindowName() {
        return windowName;
    }
}
