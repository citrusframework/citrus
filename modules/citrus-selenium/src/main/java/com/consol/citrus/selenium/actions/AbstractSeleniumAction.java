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

import com.consol.citrus.actions.AbstractTestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.selenium.endpoint.SeleniumBrowser;
import com.consol.citrus.selenium.endpoint.SeleniumHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tamer Erdogan, Christoph Deppisch
 * @since 2.7
 */
public abstract class AbstractSeleniumAction extends AbstractTestAction implements SeleniumAction {

    /** Logger */
    protected Logger log = LoggerFactory.getLogger(getClass());

    /** Selenium browser instance  */
    private SeleniumBrowser browser;

    public AbstractSeleniumAction(String name) {
        setName("selenium:" + name);
    }

    @Override
    public void doExecute(TestContext context) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("Executing Selenium browser command '%s'", getName()));
        }

        if (browser == null) {
            if (context.getVariables().containsKey(SeleniumHeaders.SELENIUM_BROWSER)) {
                browser = context.getApplicationContext().getBean(context.getVariable(SeleniumHeaders.SELENIUM_BROWSER), SeleniumBrowser.class);
            } else {
                throw new CitrusRuntimeException("Failed to get active browser instance, " +
                        "either set explicit browser for action or start browser instance");
            }
        }

        execute(browser, context);

        log.info(String.format("Selenium browser command execution successful: '%s'", getName()));
    }

    protected abstract void execute(SeleniumBrowser browser, TestContext context);

    /**
     * Gets the Selenium browser.
     * @return
     */
    public SeleniumBrowser getBrowser() {
        return browser;
    }

    /**
     * Sets the Selenium browser.
     * @param browser
     */
    public void setBrowser(SeleniumBrowser browser) {
        this.browser = browser;
    }
}
