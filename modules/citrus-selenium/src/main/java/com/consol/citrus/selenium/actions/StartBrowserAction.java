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
import com.consol.citrus.selenium.endpoint.SeleniumBrowser;
import com.consol.citrus.selenium.endpoint.SeleniumHeaders;
import org.springframework.util.StringUtils;

/**
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public class StartBrowserAction extends AbstractSeleniumAction {

    /**
     * Allow already started browser.
     */
    private boolean allowAlreadyStarted = true;

    /**
     * Default constructor.
     */
    public StartBrowserAction() {
        super("start");
    }

    @Override
    protected void execute(SeleniumBrowser browser, TestContext context) {
        if (!allowAlreadyStarted && browser.isStarted()) {
            log.warn("There are some open web browsers. They will be stopped.");
            browser.stop();
        } else if (browser.isStarted()) {
            log.info("Browser already started - skip start action");
            context.setVariable(SeleniumHeaders.SELENIUM_BROWSER, browser.getName());
            return;
        }

        log.info("Opening browser of type {}", browser.getEndpointConfiguration().getBrowserType());
        browser.start();

        if (StringUtils.hasText(getBrowser().getEndpointConfiguration().getStartPageUrl())) {
            NavigateAction openStartPage = new NavigateAction();
            openStartPage.setPage(getBrowser().getEndpointConfiguration().getStartPageUrl());
            openStartPage.execute(browser, context);
        }

        context.setVariable(SeleniumHeaders.SELENIUM_BROWSER, browser.getName());
    }

    /**
     * Sets the already started rules.
     * @param allowAlreadyStarted
     */
    public void setAllowAlreadyStarted(boolean allowAlreadyStarted) {
        this.allowAlreadyStarted = allowAlreadyStarted;
    }

    /**
     * Gets the already started rules.
     * @return
     */
    public boolean isAllowAlreadyStarted() {
        return allowAlreadyStarted;
    }
}
