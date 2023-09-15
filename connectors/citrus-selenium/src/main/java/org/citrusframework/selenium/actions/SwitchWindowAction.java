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
 * @author Christoph Deppisch
 * @since 2.7
 */
public class SwitchWindowAction extends AbstractSeleniumAction implements SeleniumAction {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(SwitchWindowAction.class);

    /** Window to select */
    private final String windowName;

    /**
     * Default constructor.
     */
    public SwitchWindowAction(Builder builder) {
        super("switch-window", builder);

        this.windowName = builder.windowName;
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
            logger.info("Switch window focus to " + windowName);

            context.setVariable(SeleniumHeaders.SELENIUM_ACTIVE_WINDOW, targetWindow);
        } else {
            logger.info("Skip switch window action as window is already focused");
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
     * Action builder.
     */
    public static class Builder extends AbstractSeleniumAction.Builder<SwitchWindowAction, Builder> {

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
        public SwitchWindowAction build() {
            return new SwitchWindowAction(this);
        }
    }
}
