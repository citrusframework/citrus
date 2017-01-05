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
import com.consol.citrus.selenium.endpoint.SeleniumHeaders;

import java.util.Set;

/**
 * @author Christoph Deppisch
 * @since 2.7
 */
public class SwitchWindowAction extends AbstractSeleniumAction implements SeleniumWindowAction {

    /** Window to select */
    private String windowName = SeleniumHeaders.SELENIUM_ACTIVE_WINDOW;

    /**
     * Default constructor.
     */
    public SwitchWindowAction() {
        super("switch-window");
    }

    @Override
    protected void execute(SeleniumBrowser browser, TestContext context) {
        if (!context.getVariables().containsKey(windowName)) {
            throw new CitrusRuntimeException("Failed to find window handle for window " + windowName);
        }

        String targetWindow = context.getVariable(windowName);
        Set<String> handles = browser.getWebDriver().getWindowHandles();
        if (!handles.contains(targetWindow)) {
            throw new CitrusRuntimeException("Failed to find window for handle " + context.getVariable(windowName));
        }

        String lastWindow = browser.getWebDriver().getWindowHandle();
        if (!lastWindow.equals(targetWindow)) {
            context.setVariable(SeleniumHeaders.SELENIUM_LAST_WINDOW, lastWindow);

            browser.getWebDriver().switchTo().window(targetWindow);
            log.info("Switch window focus to " + windowName);

            context.setVariable(SeleniumHeaders.SELENIUM_ACTIVE_WINDOW, targetWindow);
        } else {
            log.info("Skip switch window action as window is already focused");
        }
    }

    /**
     * Gets the windowName.
     *
     * @return
     */
    public String getWindowName() {
        return windowName;
    }

    /**
     * Sets the windowName.
     *
     * @param windowName
     */
    public void setWindowName(String windowName) {
        this.windowName = windowName;
    }
}
